package com.pocketdev.app.api.models

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null
)

data class Choice(
    val index: Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

data class ErrorResponse(
    val error: ErrorDetail?
)

data class ErrorDetail(
    val message: String,
    val type: String?,
    val code: String?
)
