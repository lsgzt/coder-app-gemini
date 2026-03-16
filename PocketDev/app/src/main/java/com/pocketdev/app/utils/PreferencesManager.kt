package com.pocketdev.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pocket_dev_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_FONT_SIZE = intPreferencesKey("font_size")
        val KEY_TAB_SIZE = intPreferencesKey("tab_size")
        val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save")
        val KEY_AUTOCOMPLETE = booleanPreferencesKey("autocomplete")
        val KEY_LINE_NUMBERS = booleanPreferencesKey("line_numbers")
        val KEY_WORD_WRAP = booleanPreferencesKey("word_wrap")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_LAST_PROJECT_ID = longPreferencesKey("last_project_id")
        val KEY_UNSAVED_CODE = stringPreferencesKey("unsaved_code")
        val KEY_UNSAVED_LANGUAGE = stringPreferencesKey("unsaved_language")
        val KEY_AI_MODEL = stringPreferencesKey("ai_model")

        const val DEFAULT_AI_MODEL = "llama-3.3-70b-versatile"

        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_AUTO = "auto"

        const val DEFAULT_FONT_SIZE = 14
        const val DEFAULT_TAB_SIZE = 4
    }

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: THEME_DARK
    }

    val fontSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_FONT_SIZE] ?: DEFAULT_FONT_SIZE
    }

    val tabSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TAB_SIZE] ?: DEFAULT_TAB_SIZE
    }

    val autoSave: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_SAVE] ?: true
    }

    val autocomplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTOCOMPLETE] ?: true
    }

    val lineNumbers: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LINE_NUMBERS] ?: true
    }

    val wordWrap: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WORD_WRAP] ?: false
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    val lastProjectId: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_PROJECT_ID] ?: -1L
    }

    val unsavedCode: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_UNSAVED_CODE]
    }

    val unsavedLanguage: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_UNSAVED_LANGUAGE]
    }

    val aiModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AI_MODEL] ?: DEFAULT_AI_MODEL
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME] = theme }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_FONT_SIZE] = size }
    }

    suspend fun setTabSize(size: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_TAB_SIZE] = size }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_SAVE] = enabled }
    }

    suspend fun setAutocomplete(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTOCOMPLETE] = enabled }
    }

    suspend fun setLineNumbers(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_LINE_NUMBERS] = enabled }
    }

    suspend fun setWordWrap(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_WORD_WRAP] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setLastProjectId(id: Long) {
        context.dataStore.edit { prefs -> prefs[KEY_LAST_PROJECT_ID] = id }
    }

    suspend fun setUnsavedCode(code: String?) {
        context.dataStore.edit { prefs -> 
            if (code == null) {
                prefs.remove(KEY_UNSAVED_CODE)
            } else {
                prefs[KEY_UNSAVED_CODE] = code 
            }
        }
    }

    suspend fun setUnsavedLanguage(language: String?) {
        context.dataStore.edit { prefs -> 
            if (language == null) {
                prefs.remove(KEY_UNSAVED_LANGUAGE)
            } else {
                prefs[KEY_UNSAVED_LANGUAGE] = language 
            }
        }
    }

    suspend fun setAiModel(model: String) {
        context.dataStore.edit { prefs -> prefs[KEY_AI_MODEL] = model }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = THEME_DARK
            prefs[KEY_FONT_SIZE] = DEFAULT_FONT_SIZE
            prefs[KEY_TAB_SIZE] = DEFAULT_TAB_SIZE
            prefs[KEY_AUTO_SAVE] = true
            prefs[KEY_AUTOCOMPLETE] = true
            prefs[KEY_LINE_NUMBERS] = true
            prefs[KEY_WORD_WRAP] = false
            prefs[KEY_AI_MODEL] = DEFAULT_AI_MODEL
        }
    }
}
