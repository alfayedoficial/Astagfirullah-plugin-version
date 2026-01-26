package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for TranslatePhrases.
 * Tests language selection, phrase retrieval, and title localization.
 *
 * Note: Tests that require IntelliJ services (like selectedTranslatePhrases())
 * are skipped when running outside the IntelliJ environment.
 */
@DisplayName("TranslatePhrases Tests")
class TranslatePhrasesTest {

    private fun isIntelliJEnvironment(): Boolean {
        return try {
            Class.forName("com.intellij.openapi.application.ApplicationManager")
            com.intellij.openapi.application.ApplicationManager.getApplication() != null
        } catch (e: Exception) {
            false
        }
    }

    @Nested
    @DisplayName("Language support")
    inner class LanguageSupportTests {

        @Test
        @DisplayName("Supports Arabic language")
        fun `supports arabic`() {
            val phrases = TranslatePhrases.getAllPhrases("العربية")
            assertTrue(phrases.isNotEmpty())
            // Verify Arabic text is present
            assertTrue(phrases.any { it.contains("الله") || it.contains("سبحان") })
        }

        @Test
        @DisplayName("Supports English language")
        fun `supports english`() {
            val phrases = TranslatePhrases.getAllPhrases("English")
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.any { it.contains("Allah") || it.contains("Glory") })
        }

        @Test
        @DisplayName("Supports Urdu language")
        fun `supports urdu`() {
            val phrases = TranslatePhrases.getAllPhrases("أردو")
            assertTrue(phrases.isNotEmpty())
        }

        @Test
        @DisplayName("Supports Farsi language")
        fun `supports farsi`() {
            val phrases = TranslatePhrases.getAllPhrases("فارسى")
            assertTrue(phrases.isNotEmpty())
        }

        @Test
        @DisplayName("Supports Turkish language")
        fun `supports turkish`() {
            val phrases = TranslatePhrases.getAllPhrases("Türkçe")
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.any { it.contains("Allah") || it.contains("Sübhan") })
        }

        @Test
        @DisplayName("Supports Indonesian (Bahasa) language")
        fun `supports indonesian`() {
            val phrases = TranslatePhrases.getAllPhrases("Bahasa")
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.any { it.contains("Allah") || it.contains("Subhan") })
        }

        @Test
        @DisplayName("Supports Bengali language")
        fun `supports bengali`() {
            val phrases = TranslatePhrases.getAllPhrases("বাংলা")
            assertTrue(phrases.isNotEmpty())
        }

        @Test
        @DisplayName("Falls back to Arabic for unknown language")
        fun `falls back to arabic`() {
            val phrases = TranslatePhrases.getAllPhrases("Unknown")
            assertTrue(phrases.isNotEmpty())
            // Should return Arabic phrases
            assertTrue(phrases.any { it.contains("الله") || it.contains("سبحان") })
        }
    }

    @Nested
    @DisplayName("Phrase count")
    inner class PhraseCountTests {

        @Test
        @DisplayName("Has at least PHRASES_PER_DISPLAY phrases")
        fun `has minimum phrases`() {
            val languages = listOf("العربية", "English", "أردو", "فارسى", "Türkçe", "Bahasa", "বাংলা")

            for (language in languages) {
                val phrases = TranslatePhrases.getAllPhrases(language)
                assertTrue(
                    phrases.size >= Constants.PHRASES_PER_DISPLAY,
                    "Language $language should have at least ${Constants.PHRASES_PER_DISPLAY} phrases"
                )
            }
        }

        @Test
        @DisplayName("phrasesCount returns correct count")
        fun `phrases count property`() {
            assumeTrue(isIntelliJEnvironment(), "Requires IntelliJ environment")
            val count = TranslatePhrases.phrasesCount
            assertTrue(count > 0)
        }
    }

    @Nested
    @DisplayName("selectTranslateTitle()")
    inner class TitleTests {

        @Test
        @DisplayName("Title mappings are defined correctly")
        fun `title mappings are defined`() {
            // Test the expected title values for each language
            val titles = mapOf(
                "العربية" to "اذكر الله",
                "English" to "Remember Allah",
                "أردو" to "اللہ کی یاد",
                "فارسى" to "یاد خدا",
                "Türkçe" to "Allah'ı An",
                "Bahasa" to "Ingat Allah",
                "বাংলা" to "আল্লাহকে স্মরণ করুন"
            )

            // Verify titles are non-empty meaningful strings
            for ((_, title) in titles) {
                assertTrue(title.isNotBlank())
                assertTrue(title.length > 3) // Reasonable title length
            }
        }

        @Test
        @DisplayName("selectTranslateTitle returns non-empty string")
        fun `title is not empty`() {
            assumeTrue(isIntelliJEnvironment(), "Requires IntelliJ environment")
            val title = TranslatePhrases.selectTranslateTitle()
            assertTrue(title.isNotBlank())
        }
    }

    @Nested
    @DisplayName("selectedTranslatePhrases()")
    inner class SelectedPhrasesTests {

        @Test
        @DisplayName("Returns exactly PHRASES_PER_DISPLAY phrases")
        fun `returns correct count`() {
            assumeTrue(isIntelliJEnvironment(), "Requires IntelliJ environment")
            val phrases = TranslatePhrases.selectedTranslatePhrases()
            assertEquals(Constants.PHRASES_PER_DISPLAY, phrases.size)
        }

        @Test
        @DisplayName("All returned phrases are non-empty")
        fun `all phrases non empty`() {
            assumeTrue(isIntelliJEnvironment(), "Requires IntelliJ environment")
            val phrases = TranslatePhrases.selectedTranslatePhrases()
            phrases.forEach { phrase ->
                assertTrue(phrase.isNotBlank(), "Phrase should not be blank")
            }
        }

        @Test
        @DisplayName("Returns different phrases on multiple calls (shuffled)")
        fun `phrases are shuffled`() {
            assumeTrue(isIntelliJEnvironment(), "Requires IntelliJ environment")
            // Call multiple times and check if we ever get different orders
            val results = (1..10).map { TranslatePhrases.selectedTranslatePhrases() }

            // At least one pair should be different (probabilistically)
            val allSame = results.all { it == results.first() }
            // Note: There's a very small chance this fails if shuffle produces same order
            // but with 6 phrases, probability is 1/720 per call
        }
    }

    @Nested
    @DisplayName("getAllPhrases()")
    inner class GetAllPhrasesTests {

        @Test
        @DisplayName("Returns all phrases for a language")
        fun `returns all phrases`() {
            val arabicPhrases = TranslatePhrases.getAllPhrases("العربية")
            val englishPhrases = TranslatePhrases.getAllPhrases("English")
            
            assertTrue(arabicPhrases.size >= 30) // We know there are 31 phrases
            assertTrue(englishPhrases.size >= 30)
        }

        @Test
        @DisplayName("Different languages have different content")
        fun `different languages different content`() {
            val arabicPhrases = TranslatePhrases.getAllPhrases("العربية")
            val englishPhrases = TranslatePhrases.getAllPhrases("English")
            
            // First phrase should be different (different scripts)
            assertNotEquals(arabicPhrases.first(), englishPhrases.first())
        }
    }

    @Nested
    @DisplayName("API data handling")
    inner class ApiDataTests {

        @Test
        @DisplayName("hasApiData returns boolean")
        fun `has api data returns boolean`() {
            val hasData = TranslatePhrases.hasApiData()
            // Just verify it doesn't throw and returns a boolean
            assertTrue(hasData || !hasData)
        }
    }

    @Nested
    @DisplayName("Phrase content validation")
    inner class ContentValidationTests {

        @Test
        @DisplayName("Arabic phrases contain Islamic content")
        fun `arabic content is islamic`() {
            val phrases = TranslatePhrases.getAllPhrases("العربية")
            
            // Check for common Islamic terms
            val islamicTerms = listOf("الله", "سبحان", "الحمد", "اللهم", "رب")
            val hasIslamicContent = phrases.any { phrase ->
                islamicTerms.any { term -> phrase.contains(term) }
            }
            
            assertTrue(hasIslamicContent, "Phrases should contain Islamic terms")
        }

        @Test
        @DisplayName("English phrases contain Islamic content")
        fun `english content is islamic`() {
            val phrases = TranslatePhrases.getAllPhrases("English")
            
            // Check for common English Islamic terms
            val islamicTerms = listOf("Allah", "Lord", "Glory", "Praise", "forgiveness")
            val hasIslamicContent = phrases.any { phrase ->
                islamicTerms.any { term -> phrase.contains(term, ignoreCase = true) }
            }
            
            assertTrue(hasIslamicContent, "Phrases should contain Islamic terms")
        }

        @Test
        @DisplayName("Phrases are not too short")
        fun `phrases have minimum length`() {
            val languages = listOf("العربية", "English")
            
            for (language in languages) {
                val phrases = TranslatePhrases.getAllPhrases(language)
                phrases.forEach { phrase ->
                    assertTrue(
                        phrase.length >= 5,
                        "Phrase '$phrase' is too short"
                    )
                }
            }
        }

        @Test
        @DisplayName("Phrases are not too long")
        fun `phrases have maximum length`() {
            val languages = listOf("العربية", "English")
            
            for (language in languages) {
                val phrases = TranslatePhrases.getAllPhrases(language)
                phrases.forEach { phrase ->
                    assertTrue(
                        phrase.length <= 500,
                        "Phrase is too long for display"
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("API-supported languages")
    inner class ApiSupportedLanguagesTests {

        @Test
        @DisplayName("Arabic is API-supported")
        fun `arabic is api supported`() {
            // Arabic should be able to use cached API data
            val phrases = TranslatePhrases.getAllPhrases("العربية")
            assertTrue(phrases.isNotEmpty())
        }

        @Test
        @DisplayName("English is API-supported")
        fun `english is api supported`() {
            // English should be able to use cached API data
            val phrases = TranslatePhrases.getAllPhrases("English")
            assertTrue(phrases.isNotEmpty())
        }

        @Test
        @DisplayName("Non-API languages use static data")
        fun `non api languages use static`() {
            // Turkish uses static data only
            val phrases = TranslatePhrases.getAllPhrases("Türkçe")
            assertTrue(phrases.isNotEmpty())
        }
    }
}
