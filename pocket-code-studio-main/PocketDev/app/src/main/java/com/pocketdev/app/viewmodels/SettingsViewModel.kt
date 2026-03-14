package com.pocketdev.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketdev.app.utils.PreferencesManager
import com.pocketdev.app.utils.SecureStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val secureStorage = SecureStorage(application)

    val theme: StateFlow<String> = prefsManager.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, PreferencesManager.THEME_DARK)

    val fontSize: StateFlow<Int> = prefsManager.fontSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, PreferencesManager.DEFAULT_FONT_SIZE)

    val tabSize: StateFlow<Int> = prefsManager.tabSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, PreferencesManager.DEFAULT_TAB_SIZE)

    val autoSave: StateFlow<Boolean> = prefsManager.autoSave
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val autocomplete: StateFlow<Boolean> = prefsManager.autocomplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val lineNumbers: StateFlow<Boolean> = prefsManager.lineNumbers
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val wordWrap: StateFlow<Boolean> = prefsManager.wordWrap
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val aiModel: StateFlow<String> = prefsManager.aiModel
        .stateIn(viewModelScope, SharingStarted.Eagerly, PreferencesManager.DEFAULT_AI_MODEL)

    private val _apiKeyState = MutableStateFlow<ApiKeyState>(ApiKeyState.NotSet)
    val apiKeyState: StateFlow<ApiKeyState> = _apiKeyState.asStateFlow()

    init {
        checkApiKey()
    }

    private fun checkApiKey() {
        _apiKeyState.value = if (secureStorage.hasApiKey()) {
            ApiKeyState.Set(maskApiKey(secureStorage.groqApiKey))
        } else {
            ApiKeyState.NotSet
        }
    }

    fun setApiKey(key: String) {
        if (key.isBlank()) {
            _apiKeyState.value = ApiKeyState.Error("API key cannot be empty")
            return
        }
        if (!secureStorage.validateApiKey(key)) {
            _apiKeyState.value = ApiKeyState.Error(
                "Invalid API key format. Groq API keys start with 'gsk_'"
            )
            return
        }
        secureStorage.groqApiKey = key
        _apiKeyState.value = ApiKeyState.Set(maskApiKey(key))
    }

    fun clearApiKey() {
        secureStorage.clearApiKey()
        _apiKeyState.value = ApiKeyState.NotSet
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { prefsManager.setTheme(theme) }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch { prefsManager.setFontSize(size) }
    }

    fun setTabSize(size: Int) {
        viewModelScope.launch { prefsManager.setTabSize(size) }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setAutoSave(enabled) }
    }

    fun setAutocomplete(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setAutocomplete(enabled) }
    }

    fun setLineNumbers(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setLineNumbers(enabled) }
    }

    fun setWordWrap(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setWordWrap(enabled) }
    }

    fun setAiModel(model: String) {
        viewModelScope.launch { prefsManager.setAiModel(model) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { prefsManager.resetToDefaults() }
    }

    private fun maskApiKey(key: String): String {
        return if (key.length > 8) {
            "${key.take(7)}${"*".repeat(key.length - 11)}${key.takeLast(4)}"
        } else "****"
    }

    sealed class ApiKeyState {
        object NotSet : ApiKeyState()
        data class Set(val maskedKey: String) : ApiKeyState()
        data class Error(val message: String) : ApiKeyState()
    }
}
