package com.pocketdev.app.execution

import android.content.Context
import com.pocketdev.app.data.models.ExecutionResult
import com.pocketdev.app.data.models.Language
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ExecutionManager(private val context: Context) {

    private val pythonEngine: PythonEngine by lazy { PythonEngine(context) }
    private val javascriptEngine: JavaScriptEngine by lazy { JavaScriptEngine() }
    private val htmlEngine: HtmlEngine by lazy { HtmlEngine(context) }

    suspend fun execute(code: String, language: Language, stdInput: String = ""): ExecutionResult {
        return withContext(Dispatchers.IO) {
            when (language) {
                Language.PYTHON -> pythonEngine.execute(code, stdInput)
                Language.JAVASCRIPT -> javascriptEngine.execute(code, stdInput)
                Language.HTML -> htmlEngine.prepare(code)
                else -> ExecutionResult(
                    output = "",
                    error = "Execution is not supported for ${language.displayName}.\n" +
                            "You can view and edit the code, but only Python, JavaScript, and HTML can be run.",
                    isSuccess = false
                )
            }
        }
    }

    fun isExecutable(language: Language): Boolean {
        return language in Language.executableLanguages()
    }
}
