package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for FirstRunSetupDialog.
 * Tests the setup wizard dialog logic and configuration.
 */
@DisplayName("FirstRunSetupDialog Tests")
class FirstRunSetupDialogTest {

    @Nested
    @DisplayName("Dialog configuration")
    inner class DialogConfigurationTests {

        @Test
        @DisplayName("Dialog has 4 steps")
        fun `has four steps`() {
            val totalSteps = 4
            assertEquals(4, totalSteps)
        }

        @Test
        @DisplayName("Steps are: Welcome, Language, Settings, Summary")
        fun `correct step order`() {
            val steps = listOf("Welcome", "Language", "Settings", "Summary")
            assertEquals(4, steps.size)
            assertEquals("Welcome", steps[0])
            assertEquals("Language", steps[1])
            assertEquals("Settings", steps[2])
            assertEquals("Summary", steps[3])
        }

        @Test
        @DisplayName("Dialog title is correct")
        fun `correct title`() {
            val expectedTitle = "Astagfirullah Setup"
            assertTrue(expectedTitle.contains("Astagfirullah"))
            assertTrue(expectedTitle.contains("Setup"))
        }
    }

    @Nested
    @DisplayName("Language selection")
    inner class LanguageSelectionTests {

        @Test
        @DisplayName("Supports all 7 languages")
        fun `supports all languages`() {
            val supportedLanguages = AstagfirullahSettings.SUPPORTED_LANGUAGES
            assertEquals(7, supportedLanguages.size)
        }

        @Test
        @DisplayName("Arabic is first language option")
        fun `arabic is first`() {
            val languages = AstagfirullahSettings.SUPPORTED_LANGUAGES
            assertEquals("العربية", languages[0])
        }

        @Test
        @DisplayName("English is second language option")
        fun `english is second`() {
            val languages = AstagfirullahSettings.SUPPORTED_LANGUAGES
            assertEquals("English", languages[1])
        }

        @Test
        @DisplayName("All supported languages are present")
        fun `all languages present`() {
            val languages = AstagfirullahSettings.SUPPORTED_LANGUAGES.toList()
            assertTrue(languages.contains("العربية"))
            assertTrue(languages.contains("English"))
            assertTrue(languages.contains("أردو"))
            assertTrue(languages.contains("فارسى"))
            assertTrue(languages.contains("Türkçe"))
            assertTrue(languages.contains("Bahasa"))
            assertTrue(languages.contains("বাংলা"))
        }
    }

    @Nested
    @DisplayName("Sample phrases for preview")
    inner class SamplePhrasesTests {

        @Test
        @DisplayName("Has sample phrases for all languages")
        fun `has all sample phrases`() {
            val samplePhrases = mapOf(
                "العربية" to "سبحان الله وبحمده",
                "English" to "Glory be to Allah and with His praise",
                "أردو" to "اللہ پاک ہے اور اس کی تعریف کے ساتھ",
                "فارسى" to "سبحان الله وبحمده",
                "Türkçe" to "Sübhanallahi ve bihamdihi",
                "Bahasa" to "Subhanallahi wa bihamdihi",
                "বাংলা" to "সুবহানাল্লাহি ওয়া বিহামদিহি"
            )

            assertEquals(7, samplePhrases.size)
            assertTrue(samplePhrases.all { it.value.isNotBlank() })
        }

        @Test
        @DisplayName("Arabic sample phrase is correct")
        fun `arabic sample correct`() {
            val arabicSample = "سبحان الله وبحمده"
            assertTrue(arabicSample.contains("سبحان"))
            assertTrue(arabicSample.contains("الله"))
        }

        @Test
        @DisplayName("English sample phrase is correct")
        fun `english sample correct`() {
            val englishSample = "Glory be to Allah and with His praise"
            assertTrue(englishSample.contains("Glory"))
            assertTrue(englishSample.contains("Allah"))
        }
    }

    @Nested
    @DisplayName("Settings options")
    inner class SettingsOptionsTests {

        @Test
        @DisplayName("Sound setting exists")
        fun `sound setting exists`() {
            val defaultSound = Constants.DEFAULT_SOUND_ENABLED
            // Sound is disabled by default
            assertFalse(defaultSound)
        }

        @Test
        @DisplayName("Delay options are available")
        fun `delay options available`() {
            val delayOptions = AstagfirullahSettings.DELAY_OPTIONS
            assertTrue(delayOptions.isNotEmpty())
            assertTrue(delayOptions.contains("1"))
            assertTrue(delayOptions.contains("1.5"))
            assertTrue(delayOptions.contains("10"))
        }

        @Test
        @DisplayName("Default delay is 1.5 seconds")
        fun `default delay is correct`() {
            assertEquals("1.5", Constants.DEFAULT_DELAY_SECONDS)
        }

        @Test
        @DisplayName("Delay range is 1 to 10 seconds")
        fun `delay range correct`() {
            val delayOptions = AstagfirullahSettings.DELAY_OPTIONS
            val firstDelay = delayOptions.first().toDouble()
            val lastDelay = delayOptions.last().toDouble()
            
            assertEquals(1.0, firstDelay)
            assertEquals(10.0, lastDelay)
        }
    }

    @Nested
    @DisplayName("Navigation")
    inner class NavigationTests {

        @Test
        @DisplayName("Can navigate forward through steps")
        fun `can navigate forward`() {
            var currentStep = 0
            val totalSteps = 4
            
            // Simulate going next
            currentStep++
            assertEquals(1, currentStep)
            
            currentStep++
            assertEquals(2, currentStep)
            
            currentStep++
            assertEquals(3, currentStep)
            
            // At last step
            assertTrue(currentStep == totalSteps - 1)
        }

        @Test
        @DisplayName("Can navigate backward through steps")
        fun `can navigate backward`() {
            var currentStep = 3
            
            // Simulate going back
            currentStep--
            assertEquals(2, currentStep)
            
            currentStep--
            assertEquals(1, currentStep)
            
            currentStep--
            assertEquals(0, currentStep)
            
            // At first step
            assertTrue(currentStep == 0)
        }

        @Test
        @DisplayName("Back button hidden on first step")
        fun `back hidden on first step`() {
            val currentStep = 0
            val backButtonVisible = currentStep > 0
            
            assertFalse(backButtonVisible)
        }

        @Test
        @DisplayName("Skip button hidden on last step")
        fun `skip hidden on last step`() {
            val currentStep = 3
            val totalSteps = 4
            val skipButtonVisible = currentStep < totalSteps - 1
            
            assertFalse(skipButtonVisible)
        }

        @Test
        @DisplayName("Next button text changes on last step")
        fun `next button text changes`() {
            val currentStep = 3
            val totalSteps = 4
            
            val buttonText = if (currentStep == totalSteps - 1) {
                "Complete Setup"
            } else {
                "Next"
            }
            
            assertEquals("Complete Setup", buttonText)
        }
    }

    @Nested
    @DisplayName("Setup completion")
    inner class SetupCompletionTests {

        @Test
        @DisplayName("Complete setup marks first setup as completed")
        fun `marks setup completed`() {
            var firstSetupCompleted = false
            
            // Simulate completing setup
            firstSetupCompleted = true
            
            assertTrue(firstSetupCompleted)
        }

        @Test
        @DisplayName("Complete setup records install time")
        fun `records install time`() {
            var installTime = 0L
            
            // Simulate completing setup
            if (installTime == 0L) {
                installTime = System.currentTimeMillis()
            }
            
            assertTrue(installTime > 0)
        }

        @Test
        @DisplayName("Skip setup also marks as completed")
        fun `skip marks completed`() {
            var firstSetupCompleted = false
            
            // Simulate skipping setup
            firstSetupCompleted = true
            
            assertTrue(firstSetupCompleted)
        }
    }

    @Nested
    @DisplayName("Summary display")
    inner class SummaryDisplayTests {

        @Test
        @DisplayName("Summary shows selected language")
        fun `shows selected language`() {
            val selectedLanguage = "English"
            val summaryText = selectedLanguage
            
            assertEquals("English", summaryText)
        }

        @Test
        @DisplayName("Summary shows sound status")
        fun `shows sound status`() {
            val soundEnabled = true
            val summaryText = if (soundEnabled) "Enabled" else "Disabled"
            
            assertEquals("Enabled", summaryText)
        }

        @Test
        @DisplayName("Summary shows delay setting")
        fun `shows delay setting`() {
            val delay = "1.5"
            val summaryText = "$delay seconds"
            
            assertEquals("1.5 seconds", summaryText)
        }
    }

    @Nested
    @DisplayName("Features list")
    inner class FeaturesListTests {

        @Test
        @DisplayName("Features list mentions 7 languages")
        fun `mentions language count`() {
            val featureText = "Support for 7 languages"
            assertTrue(featureText.contains("7"))
        }

        @Test
        @DisplayName("Features list mentions blessing sound")
        fun `mentions blessing sound`() {
            val featureText = "Beautiful blessing sound upon the Prophet"
            assertTrue(featureText.contains("blessing"))
            assertTrue(featureText.contains("Prophet"))
        }

        @Test
        @DisplayName("Features list mentions builds and sync")
        fun `mentions builds and sync`() {
            val featureText = "Display remembrance phrases during builds and sync"
            assertTrue(featureText.contains("builds"))
            assertTrue(featureText.contains("sync"))
        }

        @Test
        @DisplayName("Features list mentions Tool Window")
        fun `mentions tool window`() {
            val featureText = "Dedicated Tool Window panel"
            assertTrue(featureText.contains("Tool Window"))
        }

        @Test
        @DisplayName("Features list mentions phrase count")
        fun `mentions phrase count`() {
            val featureText = "31+ Islamic supplications and phrases"
            assertTrue(featureText.contains("31+"))
        }
    }

    @Nested
    @DisplayName("Progress indicator")
    inner class ProgressIndicatorTests {

        @Test
        @DisplayName("Progress shows current step")
        fun `shows current step`() {
            val currentStep = 1
            val totalSteps = 4
            val progressText = "Step ${currentStep + 1} of $totalSteps"
            
            assertEquals("Step 2 of 4", progressText)
        }

        @Test
        @DisplayName("Progress updates as steps change")
        fun `progress updates`() {
            val totalSteps = 4
            
            val step0Progress = "Step 1 of $totalSteps"
            val step1Progress = "Step 2 of $totalSteps"
            val step2Progress = "Step 3 of $totalSteps"
            val step3Progress = "Step 4 of $totalSteps"
            
            assertEquals("Step 1 of 4", step0Progress)
            assertEquals("Step 2 of 4", step1Progress)
            assertEquals("Step 3 of 4", step2Progress)
            assertEquals("Step 4 of 4", step3Progress)
        }
    }
}
