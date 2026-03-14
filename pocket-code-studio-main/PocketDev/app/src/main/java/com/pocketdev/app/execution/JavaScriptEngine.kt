package com.pocketdev.app.execution

import com.pocketdev.app.data.models.ExecutionResult
import kotlinx.coroutines.withTimeoutOrNull
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.RhinoException
import org.mozilla.javascript.ScriptableObject
import java.io.StringWriter

class JavaScriptEngine {

    companion object {
        private const val TIMEOUT_MS = 10_000L
    }

    suspend fun execute(code: String): ExecutionResult {
        val startTime = System.currentTimeMillis()

        return withTimeoutOrNull(TIMEOUT_MS) {
            try {
                executeRhino(code, startTime)
            } catch (e: RhinoException) {
                val executionTime = System.currentTimeMillis() - startTime
                ExecutionResult(
                    output = "",
                    error = formatJsError(e),
                    executionTimeMs = executionTime,
                    isSuccess = false
                )
            } catch (e: Exception) {
                val executionTime = System.currentTimeMillis() - startTime
                ExecutionResult(
                    output = "",
                    error = "JavaScript Error: ${e.message}",
                    executionTimeMs = executionTime,
                    isSuccess = false
                )
            }
        } ?: ExecutionResult(
            output = "",
            error = null,
            executionTimeMs = TIMEOUT_MS,
            isSuccess = false,
            isTimeout = true
        )
    }

    private fun executeRhino(code: String, startTime: Long): ExecutionResult {
        val outputBuilder = StringBuilder()
        val errorBuilder = StringBuilder()

        val cx = Context.enter()
        try {
            cx.optimizationLevel = -1 // Interpreted mode for Android compatibility
            cx.languageVersion = Context.VERSION_ES6

            val scope = cx.initStandardObjects()

            // Implement console object
            val consoleScript = """
                var console = {
                    _output: [],
                    log: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push(args.map(function(a) {
                            if (typeof a === 'object') return JSON.stringify(a, null, 2);
                            return String(a);
                        }).join(' '));
                    },
                    error: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push('[Error] ' + args.map(function(a) {
                            if (typeof a === 'object') return JSON.stringify(a, null, 2);
                            return String(a);
                        }).join(' '));
                    },
                    warn: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push('[Warning] ' + args.map(function(a) {
                            if (typeof a === 'object') return JSON.stringify(a, null, 2);
                            return String(a);
                        }).join(' '));
                    },
                    info: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push('[Info] ' + args.map(function(a) {
                            if (typeof a === 'object') return JSON.stringify(a, null, 2);
                            return String(a);
                        }).join(' '));
                    },
                    dir: function(obj) {
                        console._output.push(JSON.stringify(obj, null, 2));
                    },
                    table: function(data) {
                        console._output.push(JSON.stringify(data, null, 2));
                    }
                };
            """.trimIndent()

            cx.evaluateString(scope, consoleScript, "console_setup", 1, null)

            // Wrap user code to catch errors and get output
            val wrappedCode = """
                try {
                    $code
                } catch(e) {
                    console.error(e.name + ': ' + e.message);
                }
            """.trimIndent()

            cx.evaluateString(scope, wrappedCode, "user_code", 1, null)

            // Retrieve console output
            val consoleObj = scope.get("console", scope) as? ScriptableObject
            if (consoleObj != null) {
                val outputArray = consoleObj.get("_output", consoleObj)
                if (outputArray is NativeArray) {
                    for (i in 0 until outputArray.length.toInt()) {
                        val line = outputArray.get(i.toInt(), outputArray)?.toString() ?: ""
                        outputBuilder.appendLine(line)
                    }
                }
            }

            val executionTime = System.currentTimeMillis() - startTime
            val output = outputBuilder.toString().trimEnd()
            val error = errorBuilder.toString().trimEnd().takeIf { it.isNotBlank() }

            return ExecutionResult(
                output = output,
                error = error,
                executionTimeMs = executionTime,
                isSuccess = error == null
            )
        } finally {
            Context.exit()
        }
    }

    private fun formatJsError(e: RhinoException): String {
        return buildString {
            append("JavaScript Error")
            val lineNumber = e.lineNumber()
            if (lineNumber > 0) {
                append(" (line $lineNumber)")
            }
            append(":\n")
            append(e.details())
        }
    }
}
