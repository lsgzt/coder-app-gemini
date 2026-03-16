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

    suspend fun fixBug(code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Analyze this ${language.displayName} code and identify any bugs, errors, or issues.\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Please provide:\n")
            append("1. A clear explanation of what bugs/issues were found\n")
            append("2. The corrected code\n")
            append("3. An explanation of each fix\n\n")
            append("Format your response as:\n")
            append("ISSUES FOUND:\n[explanation]\n\n")
            append("CORRECTED CODE:\n```${language.displayName.lowercase()}\n[corrected code]\n```\n\n")
            append("EXPLANATION OF FIXES:\n[detailed explanation]")
        }
        return callGroqWithRetry(prompt, apiKey, model)
    }

    suspend fun explainCode(code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Explain this ${language.displayName} code in simple, beginner-friendly terms.\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Break down what each part does step-by-step. ")
            append("Use simple language suitable for students who are learning to code. ")
            append("Include:\n")
            append("1. What the code does overall\n")
            append("2. A step-by-step breakdown of each part\n")
            append("3. Any important concepts used\n")
            append("4. Tips for beginners")
        }
        return callGroqWithRetry(prompt, apiKey, model, extractCode = false)
    }

    suspend fun improveCode(code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val prompt = buildString {
            append("Suggest improvements for this ${language.displayName} code.\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Focus on:\n")
            append("1. Best practices for ${language.displayName}\n")
            append("2. Performance optimization\n")
            append("3. Code readability and maintainability\n")
            append("4. Error handling\n")
            append("5. Modern language features\n\n")
            append("Format your response as:\n")
            append("IMPROVEMENTS SUGGESTED:\n[list of improvements]\n\n")
            append("IMPROVED CODE:\n```${language.displayName.lowercase()}\n[improved code]\n```\n\n")
            append("EXPLANATION:\n[why these improvements matter]")
        }
        return callGroqWithRetry(prompt, apiKey, model)
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
            append("First, provide a brief thought process explaining what went wrong and how you will fix it.\n")
            append("Then, provide the corrected code inside a single code block.\n")
            append("Avoid repeating previous failed fixes.")
        }
        return callGroqWithRetry(prompt, apiKey, model)
    }

    suspend fun modifyCode(prompt: String, code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val fullPrompt = buildString {
            append("You are an expert ${language.displayName} developer. I have the following code:\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Please modify the code according to this request: $prompt\n\n")
            append("Return ONLY the complete modified code inside a single code block. Do not include any other text, explanations, or markdown outside the code block. The code block must contain the full updated file so it can be directly pasted.")
        }
        return callGroqWithRetry(fullPrompt, apiKey, model)
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

    suspend fun editCode(prompt: String, code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val fullPrompt = buildString {
            append("You are modifying a code file.\n\n")
            append("Instruction:\n$prompt\n\n")
            append("File:\n$code\n\n")
            append("Rules:\n")
            append("Return ONLY edits using SEARCH/REPLACE blocks.\n")
            append("Use the following format:\n")
            append("<<<<\n")
            append("exact lines to replace from the original file\n")
            append("====\n")
            append("new lines to replace them with\n")
            append(">>>>\n\n")
            append("The SEARCH block must match the original file exactly, including whitespace and indentation.\n")
            append("You can include multiple SEARCH/REPLACE blocks.\n")
            append("Do not include explanations.\n")
        }
        
        var lastError = ""
        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = callGroqForEdit(fullPrompt, code, apiKey, model)
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

    private suspend fun callGroqForEdit(prompt: String, originalCode: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
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
            
            var currentCode = originalCode
            val blockRegex = Regex("<<<<\\n([\\s\\S]*?)\\n====\\n([\\s\\S]*?)\\n>>>>")
            val matches = blockRegex.findAll(content).toList()
            
            if (matches.isNotEmpty()) {
                for (match in matches) {
                    val searchBlock = match.groupValues[1]
                    val replaceBlock = match.groupValues[2]
                    
                    if (currentCode.contains(searchBlock)) {
                        currentCode = currentCode.replaceFirst(searchBlock, replaceBlock)
                    } else {
                        // Fallback: try to match ignoring leading/trailing whitespace
                        val trimmedSearch = searchBlock.trim()
                        if (trimmedSearch.isNotEmpty() && currentCode.contains(trimmedSearch)) {
                            currentCode = currentCode.replaceFirst(trimmedSearch, replaceBlock)
                        }
                    }
                }
                
                AiResult(
                    content = content,
                    correctedCode = currentCode,
                    isSuccess = true,
                    isEdit = true
                )
            } else {
                // Fallback: If AI just returned the whole code block
                val codeRegex = Regex("```(?:[a-zA-Z]*)\\n([\\s\\S]*?)\\n```")
                val codeMatch = codeRegex.find(content)
                if (codeMatch != null) {
                    AiResult(
                        content = content,
                        correctedCode = codeMatch.groupValues[1].trim(),
                        isSuccess = true,
                        isEdit = true
                    )
                } else {
                    AiResult(
                        content = content,
                        isSuccess = false,
                        errorMessage = "AI response did not contain SEARCH/REPLACE blocks."
                    )
                }
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
