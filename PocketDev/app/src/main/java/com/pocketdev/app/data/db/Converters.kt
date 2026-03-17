package com.pocketdev.app.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pocketdev.app.data.models.Language
import com.pocketdev.app.data.models.ProjectFile

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLanguage(language: Language): String = language.name

    @TypeConverter
    fun toLanguage(name: String): Language = Language.valueOf(name)

    @TypeConverter
    fun fromProjectFileList(files: List<ProjectFile>): String {
        return gson.toJson(files)
    }

    @TypeConverter
    fun toProjectFileList(data: String): List<ProjectFile> {
        val listType = object : TypeToken<List<ProjectFile>>() {}.type
        return gson.fromJson(data, listType) ?: emptyList()
    }
}
