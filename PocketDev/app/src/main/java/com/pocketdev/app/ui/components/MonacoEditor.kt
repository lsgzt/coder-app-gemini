package com.pocketdev.app.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pocketdev.app.data.models.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonacoEditor(
    code: String,
    language: Language,
    fontSize: Int,
    lineNumbers: Boolean,
    wordWrap: Boolean,
    theme: String,
    onCodeChange: (String) -> Unit,
    selection: androidx.compose.ui.text.TextRange? = null,
    onSelectionChange: ((androidx.compose.ui.text.TextRange) -> Unit)? = null,
    ghostSuggestion: String? = null,
    modifier: Modifier = Modifier
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isEditorReady by remember { mutableStateOf(false) }
    
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }
    
    val monacoTheme = if (isDark) "vs-dark" else "vs"
    val bgColor = if (isDark) "#1e1e1e" else "#ffffff"

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: $bgColor; }
                #container { width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="container"></div>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs/loader.js"></script>
            <script>
                require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs' }});
                require(['vs/editor/editor.main'], function() {
                    window.editor = monaco.editor.create(document.getElementById('container'), {
                        value: "",
                        language: "javascript",
                        theme: "$monacoTheme",
                        automaticLayout: true,
                        fontSize: 14,
                        lineNumbers: "on",
                        wordWrap: "off",
                        wrappingIndent: "indent",
                        lineNumbersMinChars: 4,
                        lineDecorationsWidth: 10,
                        minimap: { enabled: false },
                        scrollBeyondLastLine: false,
                        padding: { top: 16 }
                    });

                    var isUpdatingFromKotlin = false;
                    var decorations = [];

                    window.editor.onDidChangeModelContent(function(e) {
                        if (!isUpdatingFromKotlin) {
                            Android.onCodeChange(window.editor.getValue());
                        }
                    });

                    window.editor.onDidChangeCursorSelection(function(e) {
                        if (!isUpdatingFromKotlin) {
                            var offset = window.editor.getModel().getOffsetAt(e.selection.getPosition());
                            Android.onSelectionChange(offset);
                        }
                    });

                    window.updateCode = function(code) {
                        if (window.editor.getValue() !== code) {
                            isUpdatingFromKotlin = true;
                            var position = window.editor.getPosition();
                            window.editor.setValue(code);
                            window.editor.setPosition(position);
                            isUpdatingFromKotlin = false;
                        }
                    };

                    window.updateLanguage = function(lang) {
                        monaco.editor.setModelLanguage(window.editor.getModel(), lang);
                    };

                    window.updateOptions = function(fontSize, lineNumbers, wordWrap) {
                        window.editor.updateOptions({
                            fontSize: fontSize,
                            lineNumbers: lineNumbers ? "on" : "off",
                            wordWrap: wordWrap ? "on" : "off"
                        });
                    };

                    window.updateTheme = function(theme, bgColor) {
                        monaco.editor.setTheme(theme);
                        document.body.style.backgroundColor = bgColor;
                    };

                    window.updateGhostSuggestion = function(suggestion, offset) {
                        if (!suggestion) {
                            decorations = window.editor.deltaDecorations(decorations, []);
                            return;
                        }
                        var pos = window.editor.getModel().getPositionAt(offset);
                        decorations = window.editor.deltaDecorations(decorations, [
                            {
                                range: new monaco.Range(pos.lineNumber, pos.column, pos.lineNumber, pos.column),
                                options: {
                                    after: {
                                        content: suggestion,
                                        inlineClassName: 'ghost-text'
                                    }
                                }
                            }
                        ]);
                    };

                    // Inject CSS for ghost text
                    var style = document.createElement('style');
                    style.innerHTML = '.ghost-text { color: #808080 !important; font-style: italic; }';
                    document.head.appendChild(style);

                    Android.onEditorReady();
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                
                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun onCodeChange(newCode: String) {
                        onCodeChange(newCode)
                    }

                    @JavascriptInterface
                    fun onSelectionChange(offset: Int) {
                        onSelectionChange?.invoke(androidx.compose.ui.text.TextRange(offset))
                    }

                    @JavascriptInterface
                    fun onEditorReady() {
                        post {
                            isEditorReady = true
                        }
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                    }
                }

                loadDataWithBaseURL("https://pocketdev.local", htmlContent, "text/html", "UTF-8", null)
                webView = this
            }
        },
        modifier = modifier.fillMaxSize()
    )

    LaunchedEffect(isEditorReady, code) {
        if (isEditorReady) {
            val escapedCode = code.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
            webView?.evaluateJavascript("if(window.updateCode) window.updateCode(\"$escapedCode\");", null)
        }
    }

    LaunchedEffect(isEditorReady, language) {
        if (isEditorReady) {
            val monacoLang = when (language) {
                Language.PYTHON -> "python"
                Language.JAVASCRIPT -> "javascript"
                Language.HTML -> "html"
                else -> "plaintext"
            }
            webView?.evaluateJavascript("if(window.updateLanguage) window.updateLanguage(\"$monacoLang\");", null)
        }
    }

    LaunchedEffect(isEditorReady, fontSize, lineNumbers, wordWrap) {
        if (isEditorReady) {
            webView?.evaluateJavascript("if(window.updateOptions) window.updateOptions($fontSize, $lineNumbers, $wordWrap);", null)
        }
    }

    LaunchedEffect(isEditorReady, monacoTheme, bgColor) {
        if (isEditorReady) {
            webView?.evaluateJavascript("if(window.updateTheme) window.updateTheme(\"$monacoTheme\", \"$bgColor\");", null)
        }
    }

    LaunchedEffect(isEditorReady, ghostSuggestion, selection) {
        if (isEditorReady) {
            val escapedSuggestion = ghostSuggestion?.replace("\\", "\\\\")?.replace("\"", "\\\"")?.replace("\n", "\\n")?.replace("\r", "") ?: ""
            val offset = selection?.start ?: 0
            webView?.evaluateJavascript("if(window.updateGhostSuggestion) window.updateGhostSuggestion(\"$escapedSuggestion\", $offset);", null)
        }
    }
}
