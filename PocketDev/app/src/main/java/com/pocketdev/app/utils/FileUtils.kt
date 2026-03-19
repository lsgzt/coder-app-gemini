package com.pocketdev.app.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.pocketdev.app.data.models.Language
import java.io.File
import java.io.IOException

object FileUtils {

    fun exportCode(context: Context, code: String, fileName: String, language: Language): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportToDownloadsQ(context, code, fileName, language)
            } else {
                exportToDownloadsLegacy(code, fileName)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun exportToDownloadsQ(
        context: Context,
        code: String,
        fileName: String,
        language: Language
    ): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName${language.extension}")
            put(MediaStore.Downloads.MIME_TYPE, getMimeType(language))
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return false

        try {
            resolver.openOutputStream(uri)?.use { output ->
                output.write(code.toByteArray(Charsets.UTF_8))
            }
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            return true
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            return false
        }
    }

    private fun exportToDownloadsLegacy(code: String, fileName: String): Boolean {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        file.writeText(code, Charsets.UTF_8)
        return true
    }

    fun readCodeFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (c.moveToFirst() && nameIndex >= 0) c.getString(nameIndex) else null
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }

    fun detectLanguageFromFileName(fileName: String): Language? {
        return when {
            fileName.endsWith(".py") -> Language.PYTHON
            fileName.endsWith(".js") -> Language.JAVASCRIPT
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> Language.HTML
            fileName.endsWith(".css") -> Language.CSS
            fileName.endsWith(".java") -> Language.JAVA
            fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".cxx") -> Language.CPP
            fileName.endsWith(".kt") -> Language.KOTLIN
            fileName.endsWith(".json") -> Language.JSON
            else -> null
        }
    }

    private fun getMimeType(language: Language): String {
        return when (language) {
            Language.PYTHON -> "text/x-python"
            Language.JAVASCRIPT -> "text/javascript"
            Language.HTML -> "text/html"
            Language.CSS -> "text/css"
            Language.JAVA -> "text/x-java"
            Language.CPP -> "text/x-c"
            Language.KOTLIN -> "text/x-kotlin"
            Language.JSON -> "application/json"
        }
    }
}
