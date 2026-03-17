package com.pocketdev.app.repository

import com.pocketdev.app.api.models.ChatRequest
import com.pocketdev.app.api.models.Message
import com.pocketdev.app.api.service.RetrofitClient
import com.pocketdev.app.data.models.AiResult
import com.pocketdev.app.data.models.Language
import kotlinx.coroutines.delay
import java.io.IOException

class GroqRepository {

    private val apiService = RetrofitClient.groqApiService

    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L
    }

    private fun buildProjectContext(files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String): String {
        return buildString {
            append("Files:\n")
            files.forEach { file ->
                append("--- ${file.name} ---\n")
                if (file.name == activeFileName) {
                    val lines = file.code.lines()
                    lines.forEachIndexed { index, line ->
                        append("${index + 1}: $line\n")
                    }
                } else {
                    append("// Summary of ${file.name}\n")
                    val lines = file.code.lines()
                    var inComment = false
                    lines.forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("/*")) inComment = true
                        if (!inComment && trimmed.isNotEmpty() && !trimmed.startsWith("//")) {
                            if (trimmed.startsWith("fun ") || trimmed.startsWith("class ") || 
                                trimmed.startsWith("def ") || trimmed.startsWith("function ") ||
                                trimmed.startsWith("public ") || trimmed.startsWith("private ")) {
                                append("${index + 1}: $line\n")
                            }
                        }
                        if (trimmed.endsWith("*/")) inComment = false
                    }
                }
                append("\n")
            }
        }
    }

    suspend fun fixBug(files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Analyze this project and identify any bugs, errors, or issues.\n\n")
            append(buildProjectContext(files, activeFileName))
            append("Please provide:\n")
            append("1. A clear explanation of what bugs/issues were found\n")
            append("2. The corrected code as patches\n")
            append("3. An explanation of each fix\n\n")
            append("Format your response as:\n")
            append("ISSUES FOUND:\n[explanation]\n\n")
            append("Then, provide the fixes as patches using the following format:\n")
            append("FILE: main.kt\n")
            append("EDIT_START: start_line_number\n")
            append("EDIT_END: end_line_number\n")
            append("NEW_CODE:\n")
            append("new lines to replace them with\n")
            append("EOF\n\n")
            append("You can include multiple patches.\n")
        }
        return callGroqForMultiEdit(prompt, files, apiKey, model)
    }

    suspend fun getGhostSuggestion(code: String, cursorPosition: Int, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("You are an inline code completion assistant for ${language.displayName}.\n")
            append("Provide the next few lines of code to complete the user's thought.\n\n")
            append("Current Code:\n")
            append("```${language.displayName.lowercase()}\n")
            append(code.substring(0, cursorPosition))
            append("<CURSOR>")
            append(code.substring(cursorPosition))
            append("\n```\n\n")
            append("Return ONLY the suggested code that should replace <CURSOR>. Do not include explanations or markdown blocks.")
        }
        val result = callGroqWithRetry(prompt, apiKey, model, extractCode = true)
        return if (result.isSuccess) {
            val suggestion = result.correctedCode ?: result.content
            result.copy(content = suggestion.trim())
        } else {
            result
        }
    }
    suspend fun explainCode(files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Explain the code in simple, beginner-friendly terms.\n\n")
            append(buildProjectContext(files, activeFileName))
            append("Break down what each part does step-by-step. ")
            append("Use simple language suitable for students who are learning to code. ")
            append("Include:\n")
            append("1. What the code does overall\n")
            append("2. A step-by-step breakdown of each part\n")
            append("3. Any important concepts used\n")
            append("4. Tips for beginners")
        }
        return callGroqForMultiEdit(prompt, files, apiKey, model)
    }

    suspend fun improveCode(files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Suggest improvements for this project.\n\n")
            append(buildProjectContext(files, activeFileName))
            append("Focus on:\n")
            append("1. Best practices\n")
            append("2. Performance optimization\n")
            append("3. Code readability and maintainability\n")
            append("4. Error handling\n")
            append("5. Modern language features\n\n")
            append("Format your response as:\n")
            append("IMPROVEMENTS SUGGESTED:\n[list of improvements]\n\n")
            append("Then, provide the improvements as patches using the following format:\n")
            append("FILE: main.kt\n")
            append("EDIT_START: start_line_number\n")
            append("EDIT_END: end_line_number\n")
            append("NEW_CODE:\n")
            append("new lines to replace them with\n")
            append("EOF\n\n")
            append("You can include multiple patches.\n")
        }
        return callGroqForMultiEdit(prompt, files, apiKey, model)
    }

    suspend fun autoFixCode(currentCode: String, error: String, historyText: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("You are fixing code that failed to execute.\n\n")
            append("Current Code:\n$currentCode\n\n")
            append("Runtime Error:\n$error\n\n")
            if (historyText.isNotBlank()) {
                append("Previous Attempts:\n$historyText\n\n")
            }
            append("Task:\nFix the code so it runs correctly.\n\n")
            append("Rules:\n")
            append("Return ONLY edits using the following patch format:\n")
            append("FILE: main${language.extension}\n")
            append("EDIT_START: start_line_number\n")
            append("EDIT_END: end_line_number\n")
            append("NEW_CODE:\n")
            append("new lines to replace them with\n")
            append("EOF\n\n")
            append("You can include multiple patches.\n")
            append("First, provide a brief thought process explaining what went wrong and how you will fix it.\n")
            append("Then, provide the patches.\n")
            append("Avoid repeating previous failed fixes.")
        }
        val file = com.pocketdev.app.data.models.ProjectFile(name = "main${language.extension}", language = language, code = currentCode)
        return callGroqForMultiEdit(prompt, listOf(file), apiKey, model)
    }

    suspend fun modifyCode(prompt: String, files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val fullPrompt = buildString {
            append("You are an expert developer. Please modify the code according to this request: $prompt\n\n")
            append(buildProjectContext(files, activeFileName))
            append("Return ONLY edits using the following patch format:\n")
            append("FILE: main.kt\n")
            append("EDIT_START: start_line_number\n")
            append("EDIT_END: end_line_number\n")
            append("NEW_CODE:\n")
            append("new lines to replace them with\n")
            append("EOF\n\n")
            append("You can include multiple patches.\n")
        }
        return callGroqForMultiEdit(fullPrompt, files, apiKey, model)
    }

    suspend fun askFollowUp(previousPrompt: String, previousResponse: String, question: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Previous Context:\n$previousPrompt\n\n")
            append("Your Previous Response:\n$previousResponse\n\n")
            append("User Follow-up Question:\n$question\n\n")
            append("Please answer the follow-up question based on the context above.")
        }
        return callGroqWithRetry(prompt, apiKey, model, extractCode = false)
    }

    private suspend fun callGroqWithRetry(prompt: String, apiKey: String, model: String = "llama-3.3-70b-versatile", extractCode: Boolean = true): AiResult {
        var lastError = ""
        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = callGroq(prompt, apiKey, model, extractCode)
                if (result.isSuccess) return result
                lastError = result.errorMessage ?: "Unknown error"
                if (lastError.contains("rate_limit", ignoreCase = true)) {
                    delay(BASE_DELAY_MS * (attempt + 1) * 2)
                } else {
                    delay(BASE_DELAY_MS * (attempt + 1))
                }
            } catch (e: IOException) {
                lastError = "Network error: ${e.message}"
                delay(BASE_DELAY_MS * (attempt + 1))
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                delay(BASE_DELAY_MS * (attempt + 1))
            }
        }
        return AiResult(
            content = "",
            isSuccess = false,
            errorMessage = lastError
        )
    }

    private suspend fun callGroq(prompt: String, apiKey: String, model: String = "llama-3.3-70b-versatile", extractCode: Boolean = true): AiResult {
        val truncatedPrompt = if (prompt.length > 20000) prompt.take(20000) + "\n...[truncated]" else prompt
        val request = ChatRequest(
            model = model,
            messages = listOf(
                Message(
                    role = "system",
                    content = "You are a helpful coding assistant for students learning to program. " +
                            "Provide clear, educational explanations and high-quality code examples. " +
                            "Be encouraging and beginner-friendly in your responses."
                ),
                Message(role = "user", content = truncatedPrompt)
            ),
            temperature = 0.7,
            maxTokens = 2048
        )

        val response = apiService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        return if (response.isSuccessful) {
            val body = response.body()
            val content = body?.choices?.firstOrNull()?.message?.content.orEmpty()
            AiResult(
                content = content,
                correctedCode = if (extractCode) extractCodeBlock(content) else null,
                isSuccess = true
            )
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val errorMsg = when (response.code()) {
                401 -> "Invalid API key. Please check your Groq API key in Settings."
                429 -> "Rate limit exceeded. Please wait a moment and try again."
                500, 502, 503 -> "Groq server error. Please try again later."
                else -> "API error ${response.code()}: $errorBody"
            }
            AiResult(content = "", isSuccess = false, errorMessage = errorMsg)
        }
    }

    suspend fun editCode(prompt: String, files: List<com.pocketdev.app.data.models.ProjectFile>, activeFileName: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val fullPrompt = buildString {
            append("You are modifying a project with multiple files.\n\n")
            append("Instruction:\n$prompt\n\n")
            append(buildProjectContext(files, activeFileName))
            append("Rules:\n")
            append("Return ONLY edits using the following patch format:\n")
            append("FILE: filename.ext\n")
            append("EDIT_START: start_line_number\n")
            append("EDIT_END: end_line_number\n")
            append("NEW_CODE:\n")
            append("new lines to replace them with\n")
            append("EOF\n\n")
            append("You can include multiple patches.\n")
            append("Do not include explanations.\n")
        }
        
        var lastError = ""
        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = callGroqForMultiEdit(fullPrompt, files, apiKey, model)
                if (result.isSuccess) return result
                lastError = result.errorMessage ?: "Unknown error"
                if (lastError.contains("rate_limit", ignoreCase = true)) {
                    delay(BASE_DELAY_MS * (attempt + 1) * 2)
                } else {
                    delay(BASE_DELAY_MS * (attempt + 1))
                }
            } catch (e: IOException) {
                lastError = "Network error: ${e.message}"
                delay(BASE_DELAY_MS * (attempt + 1))
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                delay(BASE_DELAY_MS * (attempt + 1))
            }
        }
        return AiResult(
            content = "",
            isSuccess = false,
            errorMessage = lastError
        )
    }

    private suspend fun callGroqForMultiEdit(prompt: String, originalFiles: List<com.pocketdev.app.data.models.ProjectFile>, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val truncatedPrompt = if (prompt.length > 20000) prompt.take(20000) + "\n...[truncated]" else prompt
        val request = ChatRequest(
            model = model,
            messages = listOf(
                Message(
                    role = "system",
                    content = "You are a precise code editor. Follow the formatting rules strictly."
                ),
                Message(role = "user", content = truncatedPrompt)
            ),
            temperature = 0.2,
            maxTokens = 2048
        )

        val response = apiService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        return if (response.isSuccessful) {
            val body = response.body()
            val content = body?.choices?.firstOrNull()?.message?.content.orEmpty()
            
            val patches = mutableListOf<com.pocketdev.app.data.models.FilePatch>()
            val patchRegex = Regex("FILE:\\s*(.+)\\nEDIT_START:\\s*(\\d+)\\nEDIT_END:\\s*(\\d+)\\nNEW_CODE:\\n([\\s\\S]*?)\\nEOF")
            val matches = patchRegex.findAll(content).toList()
            
            if (matches.isNotEmpty()) {
                for (match in matches) {
                    val fileName = match.groupValues[1].trim()
                    val editStart = match.groupValues[2].toIntOrNull() ?: continue
                    val editEnd = match.groupValues[3].toIntOrNull() ?: continue
                    val newCode = match.groupValues[4]
                    
                    patches.add(
                        com.pocketdev.app.data.models.FilePatch(
                            fileName = fileName,
                            editStart = editStart,
                            editEnd = editEnd,
                            newCode = newCode
                        )
                    )
                }
                
                AiResult(
                    content = content,
                    isSuccess = true,
                    isEdit = true,
                    patches = patches
                )
            } else {
                AiResult(
                    content = content,
                    isSuccess = false,
                    errorMessage = "AI response did not contain valid patches."
                )
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val errorMsg = when (response.code()) {
                401 -> "Invalid API key. Please check your Groq API key in Settings."
                429 -> "Rate limit exceeded. Please wait a moment and try again."
                500, 502, 503 -> "Groq server error. Please try again later."
                else -> "API error ${response.code()}: $errorBody"
            }
            AiResult(content = "", isSuccess = false, errorMessage = errorMsg)
        }
    }

    private fun extractCodeBlock(content: String): String? {
        val codeBlockRegex = Regex("```[\\w]*\\n([\\s\\S]*?)```")
        val match = codeBlockRegex.find(content)
        return match?.groupValues?.get(1)?.trim() ?: content.trim()
    }
}
