package com.pocketdev.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class ProjectFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val language: Language,
    val code: String
)

enum class Language(val displayName: String, val extension: String, val icon: String) {
    PYTHON("Python", ".py", "🐍"),
    JAVASCRIPT("JavaScript", ".js", "🟨"),
    HTML("HTML", ".html", "🌐"),
    CSS("CSS", ".css", "🎨"),
    JAVA("Java", ".java", "☕"),
    CPP("C++", ".cpp", "⚡"),
    KOTLIN("Kotlin", ".kt", "🎯"),
    JSON("JSON", ".json", "📋");

    companion object {
        fun fromDisplayName(name: String): Language {
            return values().find { it.displayName == name } ?: PYTHON
        }

        fun fromExtension(ext: String): Language {
            return values().find { it.extension == ext } ?: PYTHON
        }

        fun executableLanguages() = listOf(PYTHON, JAVASCRIPT, HTML)
        fun highlightLanguages() = values().toList()
        fun autocompleteLanguages() = values().filter { it != JSON }
    }
}

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val language: Language, // Primary language of the project
    val code: String = "", // Kept for backward compatibility, but we use files now
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val files: List<ProjectFile> = emptyList()
)
