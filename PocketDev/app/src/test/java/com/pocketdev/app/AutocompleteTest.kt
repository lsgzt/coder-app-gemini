package com.pocketdev.app

import com.pocketdev.app.data.models.Language
import com.pocketdev.app.editor.AutocompleteEngine
import org.junit.Assert.*
import org.junit.Test

class AutocompleteTest {

    @Test
    fun `python keywords are suggested`() {
        val suggestions = AutocompleteEngine.getSuggestions("def", 3, Language.PYTHON)
        assertTrue(suggestions.any { it.text == "def" || it.text.startsWith("def") })
    }

    @Test
    fun `javascript keywords are suggested`() {
        val suggestions = AutocompleteEngine.getSuggestions("const", 5, Language.JAVASCRIPT)
        assertTrue(suggestions.any { it.text.startsWith("const") })
    }

    @Test
    fun `html tags are suggested`() {
        val suggestions = AutocompleteEngine.getSuggestions("div", 3, Language.HTML)
        assertTrue(suggestions.any { it.text == "div" || it.text.startsWith("div") })
    }

    @Test
    fun `no suggestions for empty prefix`() {
        val suggestions = AutocompleteEngine.getSuggestions("", 0, Language.PYTHON)
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `no suggestions for single char`() {
        val suggestions = AutocompleteEngine.getSuggestions("d", 1, Language.PYTHON)
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `json has no completions`() {
        val completions = AutocompleteEngine.getCompletionsForLanguage(Language.JSON)
        assertTrue(completions.isEmpty())
    }

    @Test
    fun `kotlin completions include val and var`() {
        val completions = AutocompleteEngine.getCompletionsForLanguage(Language.KOTLIN)
        assertTrue(completions.any { it.text == "val" })
        assertTrue(completions.any { it.text == "var" })
    }

    @Test
    fun `cpp completions include cout and cin`() {
        val completions = AutocompleteEngine.getCompletionsForLanguage(Language.CPP)
        assertTrue(completions.any { it.text == "cout" })
        assertTrue(completions.any { it.text == "cin" })
    }
}
