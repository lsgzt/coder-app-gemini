package com.pocketdev.app.execution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.LinkedBlockingQueue

enum class TerminalMessageType {
    NORMAL, ERROR, AGENT, STATUS, INPUT_PROMPT
}

data class TerminalMessage(
    val text: String,
    val type: TerminalMessageType
)

class TerminalManager {
    private val _messages = MutableStateFlow<List<TerminalMessage>>(emptyList())
    val messages: StateFlow<List<TerminalMessage>> = _messages.asStateFlow()

    // Interactive input support
    private val _isWaitingForInput = MutableStateFlow(false)
    val isWaitingForInput: StateFlow<Boolean> = _isWaitingForInput.asStateFlow()

    val inputQueue = LinkedBlockingQueue<String>()

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

    fun appendInputPrompt(text: String) {
        _messages.value += TerminalMessage(text, TerminalMessageType.INPUT_PROMPT)
    }

    fun requestInput(prompt: String): String {
        if (prompt.isNotEmpty()) {
            appendInputPrompt(prompt)
        }
        _isWaitingForInput.value = true
        // Block until input is provided
        val input = inputQueue.take()
        _isWaitingForInput.value = false
        appendOutput("> $input")
        return input
    }

    fun sendInput(input: String) {
        inputQueue.put(input)
    }

    fun clearTerminal() {
        _messages.value = emptyList()
        inputQueue.clear()
        _isWaitingForInput.value = false
    }
}
