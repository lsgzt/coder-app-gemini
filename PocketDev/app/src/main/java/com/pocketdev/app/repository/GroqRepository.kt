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
        return callGroqWithRetry(prompt, apiKey, model)
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

    suspend fun modifyCode(prompt: String, code: String, language: Language, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val fullPrompt = buildString {
            append("You are an expert ${language.displayName} developer. I have the following code:\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Please modify the code according to this request: $prompt\n\n")
            append("Return ONLY the complete modified code inside a single code block. Do not include any other text, explanations, or markdown outside the code block. The code block must contain the full updated file so it can be directly pasted.")
        }
        return callGroqWithRetry(fullPrompt, apiKey, model)
    }

    private suspend fun callGroqWithRetry(prompt: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        var lastError = ""
        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = callGroq(prompt, apiKey, model)
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

    private suspend fun callGroq(prompt: String, apiKey: String, model: String = "llama-3.3-70b-versatile"): AiResult {
        val request = ChatRequest(
            model = model,
            messages = listOf(
                Message(
                    role = "system",
                    content = "You are a helpful coding assistant for students learning to program. " +
                            "Provide clear, educational explanations and high-quality code examples. " +
                            "Be encouraging and beginner-friendly in your responses."
                ),
                Message(role = "user", content = prompt)
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
                correctedCode = extractCodeBlock(content),
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

    private fun extractCodeBlock(content: String): String? {
        val codeBlockRegex = Regex("```[\\w]*\\n([\\s\\S]*?)```")
        val match = codeBlockRegex.find(content)
        return match?.groupValues?.get(1)?.trim()
    }
}
