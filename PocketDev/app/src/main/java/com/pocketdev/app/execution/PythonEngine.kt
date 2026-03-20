package com.pocketdev.app.execution

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.pocketdev.app.data.models.ExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class PythonEngine(private val context: Context) {

    companion object {
        private const val TIMEOUT_MS = 30_000L // Increased for interactive scripts
    }

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    /**
     * Execute with pre-provided stdin (legacy, non-interactive mode)
     */
    suspend fun execute(code: String, stdInput: String = ""): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        return@withContext withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val py = Python.getInstance()
                val codeRunner = py.getModule("code_runner")
                val result = codeRunner.callAttr("execute_code", code, stdInput)
                
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

    /**
     * Execute with real-time interactive input via TerminalManager.
     * When input() is called in the script, it blocks until user provides input in the terminal.
     */
    suspend fun executeInteractive(
        code: String,
        terminalManager: TerminalManager
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        return@withContext withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val py = Python.getInstance()
                val codeRunner = py.getModule("code_runner")

                // Create input callback that blocks on TerminalManager's queue
                val inputCallback = PyObject.fromJava(object : InputCallback {
                    override fun call(): String {
                        // This blocks the Python thread until user sends input
                        return terminalManager.requestInput("")
                    }
                })

                // Create output callback for real-time output streaming
                val outputCallback = PyObject.fromJava(object : OutputCallback {
                    override fun call(text: String) {
                        // Check if output ends with a prompt (input() prints prompt to stdout)
                        terminalManager.appendOutput(text)
                    }
                })

                val result = codeRunner.callAttr(
                    "execute_code_interactive",
                    code,
                    inputCallback,
                    outputCallback
                )

                val output = result.callAttr("get", "output")?.toString() ?: ""
                val error = result.callAttr("get", "error")?.toString() ?: ""

                ExecutionResult(
                    output = "", // Output already streamed to terminal
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
            error = "Execution timeout (30s). Script may be waiting for input.",
            executionTimeMs = TIMEOUT_MS,
            isSuccess = false
        )
    }

    // Interface for Chaquopy Python-to-Kotlin callback
    interface InputCallback {
        fun call(): String
    }

    interface OutputCallback {
        fun call(text: String)
    }
}
