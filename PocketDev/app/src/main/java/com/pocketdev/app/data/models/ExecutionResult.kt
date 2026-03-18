package com.pocketdev.app.data.models

data class ExecutionResult(
    val output: String,
    val error: String? = null,
    val executionTimeMs: Long = 0,
    val isSuccess: Boolean = true,
    val isTimeout: Boolean = false
) {
    val hasError: Boolean get() = error != null && error.isNotBlank()
    val displayOutput: String get() = buildString {
        if (output.isNotBlank()) append(output)
        if (hasError) {
            if (output.isNotBlank()) append("\n")
            append("❌ Error:\n$error")
        }
        if (isTimeout) {
            if (isNotEmpty()) append("\n")
            append("⏱️ Execution timed out after 10 seconds")
        }
    }
}

data class FilePatch(
    val fileName: String,
    val editStart: Int,
    val editEnd: Int,
    val newCode: String
)

data class AiResult(
    val content: String,
    val correctedCode: String? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val isEdit: Boolean = false,
    val patches: List<FilePatch> = emptyList(),
    val confidence: String = "MEDIUM"
)

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
