package com.pocketdev.app.execution

import android.content.Context
import com.pocketdev.app.data.models.ExecutionResult

class HtmlEngine(private val context: Context) {

    // For HTML, we don't execute in background - we prepare the content for WebView rendering
    fun prepare(htmlCode: String): ExecutionResult {
        return if (htmlCode.isBlank()) {
            ExecutionResult(
                output = "",
                error = "HTML code is empty. Please write some HTML to render.",
                isSuccess = false
            )
        } else {
            // Return the HTML content as output - it will be rendered by the WebView
            ExecutionResult(
                output = htmlCode,
                error = null,
                executionTimeMs = 0,
                isSuccess = true
            )
        }
    }

    fun injectConsoleCapture(html: String): String {
        val consoleScript = """
        <script>
        (function() {
            var originalConsole = window.console;
            var messages = [];

            function capture(type, args) {
                var msg = Array.prototype.slice.call(args).map(function(a) {
                    if (typeof a === 'object') {
                        try { return JSON.stringify(a, null, 2); } catch(e) { return String(a); }
                    }
                    return String(a);
                }).join(' ');
                messages.push('[' + type + '] ' + msg);
                if (window.AndroidBridge) {
                    window.AndroidBridge.onConsoleLog(type, msg);
                }
            }

            window.console = {
                log: function() { capture('log', arguments); originalConsole.log.apply(originalConsole, arguments); },
                error: function() { capture('error', arguments); originalConsole.error.apply(originalConsole, arguments); },
                warn: function() { capture('warn', arguments); originalConsole.warn.apply(originalConsole, arguments); },
                info: function() { capture('info', arguments); originalConsole.info.apply(originalConsole, arguments); },
                getMessages: function() { return messages; }
            };

            window.addEventListener('error', function(e) {
                if (window.AndroidBridge) {
                    window.AndroidBridge.onError(e.message, e.lineno || 0);
                }
            });
        })();
        </script>
        """.trimIndent()

        // Inject script at the very beginning of the HTML
        return if (html.contains("<head>", ignoreCase = true)) {
            html.replace("<head>", "<head>$consoleScript", ignoreCase = true)
        } else if (html.contains("<html", ignoreCase = true)) {
            html.replace(Regex("<html[^>]*>"), "$0$consoleScript")
        } else {
            "$consoleScript\n$html"
        }
    }
}
