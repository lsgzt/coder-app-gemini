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
        private const val TIMEOUT_MS = 30_000L
    }

    suspend fun execute(code: String, stdInput: String? = null): ExecutionResult {
        val startTime = System.currentTimeMillis()

        return withTimeoutOrNull(TIMEOUT_MS) {
            try {
                executeRhino(code, startTime, stdInput)
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

    /**
     * Execute with real-time interactive prompt() via TerminalManager.
     */
    suspend fun executeInteractive(
        code: String,
        terminalManager: TerminalManager
    ): ExecutionResult {
        val startTime = System.currentTimeMillis()

        return withTimeoutOrNull(TIMEOUT_MS) {
            try {
                executeRhinoInteractive(code, startTime, terminalManager)
            } catch (e: RhinoException) {
                ExecutionResult(
                    output = "",
                    error = formatJsError(e),
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    isSuccess = false
                )
            } catch (e: Exception) {
                ExecutionResult(
                    output = "",
                    error = "JavaScript Error: ${e.message}",
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

    private fun executeRhino(code: String, startTime: Long, stdInput: String?): ExecutionResult {
        val outputBuilder = StringBuilder()

        val cx = Context.enter()
        try {
            cx.optimizationLevel = -1
            cx.languageVersion = Context.VERSION_ES6

            val scope = cx.initStandardObjects()

            val escapedInput = stdInput?.replace("\\", "\\\\")
                ?.replace("\n", "\\n")
                ?.replace("\r", "\\r")
                ?.replace("\"", "\\\"") ?: ""

            val consoleScript = """
                var _inputLines = "$escapedInput" ? "$escapedInput".split('\n') : [];
                var _inputIndex = 0;
                function prompt(message) {
                    if (message) console.log(message);
                    if (_inputIndex < _inputLines.length && _inputLines[_inputIndex] !== undefined) {
                        return _inputLines[_inputIndex++];
                    }
                    return null;
                }
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
                        console._output.push('[Error] ' + args.map(String).join(' '));
                    },
                    warn: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push('[Warning] ' + args.map(String).join(' '));
                    },
                    info: function() {
                        var args = Array.prototype.slice.call(arguments);
                        console._output.push('[Info] ' + args.map(String).join(' '));
                    },
                    dir: function(obj) { console._output.push(JSON.stringify(obj, null, 2)); },
                    table: function(data) { console._output.push(JSON.stringify(data, null, 2)); }
                };
            """.trimIndent()

            cx.evaluateString(scope, consoleScript, "console_setup", 1, null)

            val wrappedCode = """
                try {
                    $code
                } catch(e) {
                    console.error(e.name + ': ' + e.message);
                }
            """.trimIndent()

            cx.evaluateString(scope, wrappedCode, "user_code", 1, null)

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

            val output = outputBuilder.toString().trimEnd()
            return ExecutionResult(
                output = output,
                error = null,
                executionTimeMs = System.currentTimeMillis() - startTime,
                isSuccess = true
            )
        } finally {
            Context.exit()
        }
    }

    private fun executeRhinoInteractive(
        code: String,
        startTime: Long,
        terminalManager: TerminalManager
    ): ExecutionResult {
        val cx = Context.enter()
        try {
            cx.optimizationLevel = -1
            cx.languageVersion = Context.VERSION_ES6

            val scope = cx.initStandardObjects()

            // Define prompt() that blocks on TerminalManager input
            // We inject a Java object as a bridge
            val inputBridge = object : ScriptableObject() {
                override fun getClassName() = "InputBridge"

                fun requestInput(promptMsg: String?): String {
                    return terminalManager.requestInput(promptMsg ?: "")
                }
            }
            ScriptableObject.putProperty(scope, "_inputBridge", inputBridge)

            val consoleScript = """
                function prompt(message) {
                    if (message) { java.lang.System.out.print(""); _outputCallback(String(message)); }
                    return String(_inputBridge.requestInput(message || ""));
                }
                
                var _outputParts = [];
                function _outputCallback(text) {
                    _outputParts.push(text);
                }
                
                var console = {
                    _output: [],
                    log: function() {
                        var args = Array.prototype.slice.call(arguments);
                        var line = args.map(function(a) {
                            if (typeof a === 'object') return JSON.stringify(a, null, 2);
                            return String(a);
                        }).join(' ');
                        console._output.push(line);
                        _outputCallback(line + '\n');
                    },
                    error: function() {
                        var args = Array.prototype.slice.call(arguments);
                        var line = '[Error] ' + args.map(String).join(' ');
                        console._output.push(line);
                        _outputCallback(line + '\n');
                    },
                    warn: function() {
                        var args = Array.prototype.slice.call(arguments);
                        var line = '[Warning] ' + args.map(String).join(' ');
                        console._output.push(line);
                        _outputCallback(line + '\n');
                    },
                    info: function() {
                        var args = Array.prototype.slice.call(arguments);
                        var line = '[Info] ' + args.map(String).join(' ');
                        console._output.push(line);
                        _outputCallback(line + '\n');
                    },
                    dir: function(obj) {
                        var s = JSON.stringify(obj, null, 2);
                        console._output.push(s);
                        _outputCallback(s + '\n');
                    },
                    table: function(data) {
                        var s = JSON.stringify(data, null, 2);
                        console._output.push(s);
                        _outputCallback(s + '\n');
                    }
                };
            """.trimIndent()

            // We need a real output callback for streaming
            val outputCallbackObj = object : org.mozilla.javascript.BaseFunction() {
                override fun call(
                    cx: Context?,
                    scope: org.mozilla.javascript.Scriptable?,
                    thisObj: org.mozilla.javascript.Scriptable?,
                    args: Array<out Any?>?
                ): Any? {
                    val text = args?.firstOrNull()?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        terminalManager.appendOutput(text.trimEnd('\n'))
                    }
                    return org.mozilla.javascript.Undefined.instance
                }
            }
            ScriptableObject.putProperty(scope, "_outputCallback", outputCallbackObj)

            cx.evaluateString(scope, consoleScript, "console_setup", 1, null)

            val wrappedCode = """
                try {
                    $code
                } catch(e) {
                    console.error(e.name + ': ' + e.message);
                }
            """.trimIndent()

            cx.evaluateString(scope, wrappedCode, "user_code", 1, null)

            return ExecutionResult(
                output = "", // Already streamed to terminal
                error = null,
                executionTimeMs = System.currentTimeMillis() - startTime,
                isSuccess = true
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
