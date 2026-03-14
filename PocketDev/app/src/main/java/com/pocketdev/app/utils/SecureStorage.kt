package com.pocketdev.app.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    companion object {
        private const val PREFS_NAME = "pocket_dev_secure"
        private const val KEY_GROQ_API_KEY = "groq_api_key"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var groqApiKey: String
        get() = sharedPreferences.getString(KEY_GROQ_API_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_GROQ_API_KEY, value).apply()

    fun hasApiKey(): Boolean = groqApiKey.isNotBlank()

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_GROQ_API_KEY).apply()
    }

    fun validateApiKey(key: String): Boolean {
        // Groq API keys typically start with "gsk_"
        return key.startsWith("gsk_") && key.length > 20
    }
}
