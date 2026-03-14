package com.pocketdev.app.data.db

import androidx.room.TypeConverter
import com.pocketdev.app.data.models.Language

class Converters {
    @TypeConverter
    fun fromLanguage(language: Language): String = language.name

    @TypeConverter
    fun toLanguage(name: String): Language = Language.valueOf(name)
}
