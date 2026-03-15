package com.pocketdev.app.execution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TerminalMessageType {
    NORMAL, ERROR, AGENT, STATUS
}

data class TerminalMessage(
    val text: String,
    val type: TerminalMessageType
)

class TerminalManager {
    private val _messages = MutableStateFlow<List<TerminalMessage>>(emptyList())
    val messages: StateFlow<List<TerminalMessage>> = _messages.asStateFlow()

    fun appendOutput(text: String) {
        if (text.isNotEmpty()) {
            _messages.value += TerminalMessage(text, TerminalMessageType.NORMAL)
        }
    }

    fun appendError(text: String) {
        if (text.isNotEmpty()) {
            _messages.value += TerminalMessage(text, TerminalMessageType.ERROR)
        }
    }

    fun appendAgentMessage(text: String) {
        if (text.isNotEmpty()) {
            _messages.value += TerminalMessage(text, TerminalMessageType.AGENT)
        }
    }

    fun appendStatusMessage(text: String) {
        if (text.isNotEmpty()) {
            _messages.value += TerminalMessage(text, TerminalMessageType.STATUS)
        }
    }

    fun clearTerminal() {
        _messages.value = emptyList()
    }
}
