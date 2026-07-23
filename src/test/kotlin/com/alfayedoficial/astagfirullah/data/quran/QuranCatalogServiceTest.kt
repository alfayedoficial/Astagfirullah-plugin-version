package com.alfayedoficial.astagfirullah.data.quran

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("QuranCatalogService Tests")
class QuranCatalogServiceTest {

    private fun reciter(server: String) =
        QuranReciter(reciterId = 1, label = "Test", server = server, availableSurahs = (1..114).toSet())

    @Nested
    @DisplayName("Audio URL Tests")
    inner class AudioUrlTests {

        @Test
        @DisplayName("Zero-pads the surah number to three digits")
        fun padsSurahNumber() {
            val r = reciter("https://server6.mp3quran.net/akdr/")
            assertEquals("https://server6.mp3quran.net/akdr/001.mp3", QuranCatalogService.audioUrl(r, 1))
            assertEquals("https://server6.mp3quran.net/akdr/002.mp3", QuranCatalogService.audioUrl(r, 2))
            assertEquals("https://server6.mp3quran.net/akdr/114.mp3", QuranCatalogService.audioUrl(r, 114))
        }

        @Test
        @DisplayName("Handles a two-digit surah number")
        fun twoDigit() {
            val r = reciter("https://s/x/")
            assertEquals("https://s/x/036.mp3", QuranCatalogService.audioUrl(r, 36))
        }
    }

    @Nested
    @DisplayName("Language Mapping Tests")
    inner class LanguageMappingTests {

        @Test
        @DisplayName("Maps every plugin language to its mp3quran locale code")
        fun mapsAllPluginLanguages() {
            assertEquals("ar", QuranCatalogService.languageCode("العربية"))
            assertEquals("eng", QuranCatalogService.languageCode("English"))
            assertEquals("ur", QuranCatalogService.languageCode("أردو"))
            assertEquals("fa", QuranCatalogService.languageCode("فارسى"))
            assertEquals("tr", QuranCatalogService.languageCode("Türkçe"))
            assertEquals("id", QuranCatalogService.languageCode("Bahasa"))
            assertEquals("bn", QuranCatalogService.languageCode("বাংলা"))
        }

        @Test
        @DisplayName("Falls back to English for an unknown language")
        fun fallsBackToEnglish() {
            assertEquals("eng", QuranCatalogService.languageCode("Klingon"))
            assertEquals("eng", QuranCatalogService.languageCode(""))
        }
    }
}
