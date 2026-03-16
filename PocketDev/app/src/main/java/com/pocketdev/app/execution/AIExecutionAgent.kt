package com.pocketdev.app.execution

import com.pocketdev.app.data.models.Language
import com.pocketdev.app.repository.GroqRepository

class AIExecutionAgent(
    private val executionManager: ExecutionManager,
    private val groqRepository: GroqRepository,
    private val terminalManager: TerminalManager,
    private val updateCode: (String) -> Unit,
    private val getApiKey: suspend () -> String
) {
    private val MAX_ATTEMPTS = 5

    data class AttemptHistory(
        val code: String,
        val error: String,
        val aiFix: String? = null
    )

    suspend fun runWithAiFix(initialCode: String, language: Language, input: String): com.pocketdev.app.data.models.ExecutionResult? {
        var currentCode = initialCode
        val history = mutableListOf<AttemptHistory>()
        val apiKey = getApiKey()

        terminalManager.appendStatusMessage("Running code...")

        for (attempt in 1..MAX_ATTEMPTS) {
            val result = executionManager.execute(currentCode, language, input)

            if (result.isSuccess) {
                terminalManager.appendOutput(result.output)
                terminalManager.appendStatusMessage("Execution successful.")
                return result
            } else {
                terminalManager.appendError(result.error ?: "Unknown error")
                
                if (attempt == MAX_ATTEMPTS) {
                    terminalManager.appendAgentMessage("AI could not fix the code automatically.")
                    return result
                }

                terminalManager.appendAgentMessage("Error detected. Asking AI to fix...\nAI attempt $attempt/$MAX_ATTEMPTS")
                
                // Record history before AI fix
                val currentAttempt = AttemptHistory(currentCode, result.error ?: "Unknown error")
                history.add(currentAttempt)

                val historyText = history.takeLast(2).joinToString("\n---\n") {
                    "Error:\n${it.error}\nAI Fix:\n${it.aiFix ?: "None"}"
                }

                val aiResult = groqRepository.autoFixCode(currentCode, result.error ?: "Unknown error", historyText, language, apiKey)
                
                if (aiResult.isSuccess && aiResult.correctedCode != null) {
                    // Update history with AI fix
                    history[history.size - 1] = currentAttempt.copy(aiFix = aiResult.correctedCode)
                    
                    val thoughtProcess = aiResult.content.replace("```${language.displayName.lowercase()}\n${aiResult.correctedCode}\n```", "").trim()
                    if (thoughtProcess.isNotBlank()) {
                        terminalManager.appendAgentMessage("AI Thought Process:\n$thoughtProcess")
                    }
                    
                    terminalManager.appendAgentMessage("AI Applied Fix:\n${aiResult.correctedCode}")
                    
                    currentCode = aiResult.correctedCode
                    updateCode(currentCode)
                    terminalManager.appendStatusMessage("Running updated code...")
                } else {
                    terminalManager.appendAgentMessage("AI failed to generate a fix: ${aiResult.errorMessage}")
                    return result
                }
            }
        }
        return null
    }
}
