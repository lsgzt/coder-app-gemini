package com.pocketdev.app.execution

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.pocketdev.app.data.models.ExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class PythonEngine(private val context: Context) {

    companion object {
        private const val TIMEOUT_MS = 10_000L
    }

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    suspend fun execute(code: String): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        return@withContext withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val py = Python.getInstance()
                val codeRunner = py.getModule("code_runner")
                val result = codeRunner.callAttr("execute_code", code)
                
                val output = result.callAttr("get", "output")?.toString() ?: ""
                val error = result.callAttr("get", "error")?.toString() ?: ""
                
                ExecutionResult(
                    output = output,
                    error = error,
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    isSuccess = error.isEmpty()
                )
            } catch (e: Exception) {
                ExecutionResult(
                    output = "",
                    error = e.message ?: "Unknown error",
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    isSuccess = false
                )
            }
        } ?: ExecutionResult(
            output = "",
            error = "Execution timeout",
            executionTimeMs = TIMEOUT_MS,
            isSuccess = false
        )
    }
}
