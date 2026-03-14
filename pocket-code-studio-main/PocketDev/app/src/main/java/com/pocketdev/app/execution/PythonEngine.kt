package com.pocketdev.app.execution

import android.content.Context
import com.pocketdev.app.data.models.ExecutionResult
import kotlinx.coroutines.withTimeoutOrNull

class PythonEngine(private val context: Context) {

    companion object {
        private const val TIMEOUT_MS = 10_000L
    }

    suspend fun execute(code: String): ExecutionResult {
        val startTime = System.currentTimeMillis()

        return withTimeoutOrNull(TIMEOUT_MS) {
            ExecutionResult(
                output = "Python execution is not available in this build.",
                error = "Python runtime not configured",
                executionTimeMs = System.currentTimeMillis() - startTime,
                isSuccess = false
            )
        } ?: ExecutionResult(
            output = "",
            error = "Execution timeout",
            executionTimeMs = TIMEOUT_MS,
            isSuccess = false
        )
    }
}
