package com.pocketdev.app.ui.components

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pocketdev.app.data.models.Language

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
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
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
                .loading { display: flex; justify-content: center; align-items: center; height: 100%; color: ${if (isDark) "#888" else "#666"}; font-family: sans-serif; }
            </style>
        </head>
        <body>
            <div id="container"><div class="loading">Loading editor...</div></div>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs/loader.js"></script>
            <script>
                var loadTimeout;
                var editorReady = false;
                
                function initEditor() {
                    require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs' }});
                    require(['vs/editor/editor.main'], function() {
                        clearTimeout(loadTimeout);
                        editorReady = true;
                        
                        window.editor = monaco.editor.create(document.getElementById('container'), {
                            value: "",
                            language: "python",
                            theme: "$monacoTheme",
                            automaticLayout: true,
                            fontSize: $fontSize,
                            lineNumbers: ${if (lineNumbers) "\"on\"" else "\"off\""},
                            wordWrap: ${if (wordWrap) "\"on\"" else "\"off\""},
                            wrappingIndent: "indent",
                            lineNumbersMinChars: 4,
                            lineDecorationsWidth: 10,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false,
                            padding: { top: 16 },
                            renderLineHighlight: "line",
                            cursorBlinking: "smooth",
                            smoothScrolling: true
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
                            if (window.editor && window.editor.getValue() !== code) {
                                isUpdatingFromKotlin = true;
                                var position = window.editor.getPosition();
                                window.editor.setValue(code);
                                window.editor.setPosition(position);
                                isUpdatingFromKotlin = false;
                            }
                        };

                        window.updateLanguage = function(lang) {
                            if (window.editor) {
                                monaco.editor.setModelLanguage(window.editor.getModel(), lang);
                            }
                        };

                        window.updateOptions = function(fontSize, lineNumbers, wordWrap) {
                            if (window.editor) {
                                window.editor.updateOptions({
                                    fontSize: fontSize,
                                    lineNumbers: lineNumbers ? "on" : "off",
                                    wordWrap: wordWrap ? "on" : "off"
                                });
                            }
                        };

                        window.updateTheme = function(theme, bgColor) {
                            monaco.editor.setTheme(theme);
                            document.body.style.backgroundColor = bgColor;
                        };

                        window.updateGhostSuggestion = function(suggestion, offset) {
                            if (!window.editor) return;
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
                    }, function(error) {
                        console.error('Monaco load error:', error);
                        Android.onEditorError('Failed to load Monaco editor');
                    });
                }
                
                // Start loading
                loadTimeout = setTimeout(function() {
                    if (!editorReady) {
                        Android.onEditorError('Editor load timeout');
                    }
                }, 15000);
                
                initEditor();
            </script>
        </body>
        </html>
    """.trimIndent()

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        blockNetworkLoads = false
                        loadsImagesAutomatically = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    
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
                                isLoading = false
                                hasError = false
                            }
                        }
                        
                        @JavascriptInterface
                        fun onEditorError(error: String) {
                            post {
                                hasError = true
                                isLoading = false
                            }
                        }
                    }, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                        
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                isLoading = false
                            }
                        }
                        
                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            handler?.proceed()
                        }
                    }
                    
                    webChromeClient = WebChromeClient()

                    loadDataWithBaseURL("https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/", htmlContent, "text/html", "UTF-8", null)
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    LaunchedEffect(isEditorReady, code) {
        if (isEditorReady && webView != null) {
            val escapedCode = code
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
            webView?.evaluateJavascript("if(window.updateCode) window.updateCode(\"$escapedCode\");", null)
        }
    }

    LaunchedEffect(isEditorReady, language) {
        if (isEditorReady && webView != null) {
            val monacoLang = when (language) {
                Language.PYTHON -> "python"
                Language.JAVASCRIPT -> "javascript"
                Language.HTML -> "html"
                Language.CSS -> "css"
                Language.JSON -> "json"
                Language.KOTLIN -> "kotlin"
                Language.JAVA -> "java"
                Language.CPP -> "cpp"
                else -> "plaintext"
            }
            webView?.evaluateJavascript("if(window.updateLanguage) window.updateLanguage(\"$monacoLang\");", null)
        }
    }

    LaunchedEffect(isEditorReady, fontSize, lineNumbers, wordWrap) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript("if(window.updateOptions) window.updateOptions($fontSize, $lineNumbers, $wordWrap);", null)
        }
    }

    LaunchedEffect(isEditorReady, monacoTheme, bgColor) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript("if(window.updateTheme) window.updateTheme(\"$monacoTheme\", \"$bgColor\");", null)
        }
    }

    LaunchedEffect(isEditorReady, ghostSuggestion, selection) {
        if (isEditorReady && webView != null) {
            val escapedSuggestion = ghostSuggestion
                ?.replace("\\", "\\\\")
                ?.replace("\"", "\\\"")
                ?.replace("\n", "\\n")
                ?.replace("\r", "\\r") 
                ?: ""
            val offset = selection?.start ?: 0
            webView?.evaluateJavascript("if(window.updateGhostSuggestion) window.updateGhostSuggestion(\"$escapedSuggestion\", $offset);", null)
        }
    }
}
