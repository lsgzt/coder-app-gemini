package com.pocketdev.app

import com.pocketdev.app.data.models.Language
import org.junit.Assert.*
import org.junit.Test

class LanguageTest {

    @Test
    fun `all 8 languages exist`() {
        assertEquals(8, Language.values().size)
    }

    @Test
    fun `executable languages are python javascript and html`() {
        val exec = Language.executableLanguages()
        assertTrue(exec.contains(Language.PYTHON))
        assertTrue(exec.contains(Language.JAVASCRIPT))
        assertTrue(exec.contains(Language.HTML))
        assertEquals(3, exec.size)
    }

    @Test
    fun `autocomplete languages exclude json`() {
        val ac = Language.autocompleteLanguages()
        assertFalse(ac.contains(Language.JSON))
        assertEquals(7, ac.size)
    }

    @Test
    fun `language from display name works`() {
        assertEquals(Language.PYTHON, Language.fromDisplayName("Python"))
        assertEquals(Language.JAVASCRIPT, Language.fromDisplayName("JavaScript"))
        assertEquals(Language.HTML, Language.fromDisplayName("HTML"))
    }

    @Test
    fun `language from extension works`() {
        assertEquals(Language.PYTHON, Language.fromExtension(".py"))
        assertEquals(Language.JAVASCRIPT, Language.fromExtension(".js"))
        assertEquals(Language.HTML, Language.fromExtension(".html"))
        assertEquals(Language.KOTLIN, Language.fromExtension(".kt"))
    }

    @Test
    fun `each language has correct extension`() {
        assertEquals(".py", Language.PYTHON.extension)
        assertEquals(".js", Language.JAVASCRIPT.extension)
        assertEquals(".html", Language.HTML.extension)
        assertEquals(".css", Language.CSS.extension)
        assertEquals(".java", Language.JAVA.extension)
        assertEquals(".cpp", Language.CPP.extension)
        assertEquals(".kt", Language.KOTLIN.extension)
        assertEquals(".json", Language.JSON.extension)
    }
}
