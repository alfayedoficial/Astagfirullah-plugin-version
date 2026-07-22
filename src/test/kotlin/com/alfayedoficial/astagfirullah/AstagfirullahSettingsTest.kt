package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.util.xmlb.XmlSerializerUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Comprehensive unit tests for AstagfirullahSettings.
 * Tests settings persistence, conversions, and state management.
 */
@DisplayName("AstagfirullahSettings Tests")
class AstagfirullahSettingsTest {

    private lateinit var settings: AstagfirullahSettings

    @BeforeEach
    fun setUp() {
        settings = AstagfirullahSettings()
    }

    @Nested
    @DisplayName("Default Values Tests")
    inner class DefaultValuesTests {

        @Test
        @DisplayName("Should have default language set to Arabic")
        fun defaultLanguageShouldBeArabic() {
            assertEquals(Constants.DEFAULT_LANGUAGE, settings.language)
            assertEquals("العربية", settings.language)
        }

        @Test
        @DisplayName("Should have default delay seconds set to 1.5")
        fun defaultDelaySecondsShouldBe1Point5() {
            assertEquals(Constants.DEFAULT_DELAY_SECONDS, settings.delaySeconds)
            assertEquals("1.5", settings.delaySeconds)
        }

        @Test
        @DisplayName("Should have sound disabled by default")
        fun soundShouldBeDisabledByDefault() {
            assertFalse(settings.soundEnabled)
            assertEquals(Constants.DEFAULT_SOUND_ENABLED, settings.soundEnabled)
        }

        @Test
        @DisplayName("Should have show on startup enabled by default")
        fun showOnStartupShouldBeEnabledByDefault() {
            assertTrue(settings.showOnStartup)
            assertEquals(Constants.DEFAULT_SHOW_ON_STARTUP, settings.showOnStartup)
        }

        @Test
        @DisplayName("Should have install time set to 0 by default")
        fun installTimeShouldBeZeroByDefault() {
            assertEquals(0L, settings.installTime)
        }

        @Test
        @DisplayName("Should have first rating time set to RATING_STATE_FIRST by default")
        fun firstRatingTimeShouldBeFirstStateByDefault() {
            assertEquals(Constants.RATING_STATE_FIRST, settings.firstRatingTime)
        }

        @Test
        @DisplayName("Should have rating prompted set to false by default")
        fun ratingPromptedShouldBeFalseByDefault() {
            assertFalse(settings.ratingPrompted)
        }

        @Test
        @DisplayName("Should have first setup completed set to false by default")
        fun firstSetupCompletedShouldBeFalseByDefault() {
            assertFalse(settings.firstSetupCompleted)
        }

        @Test
        @DisplayName("Should have last rating prompt time set to 0 by default")
        fun lastRatingPromptTimeShouldBeZeroByDefault() {
            assertEquals(0L, settings.lastRatingPromptTime)
        }
    }

    @Nested
    @DisplayName("State Class Tests")
    inner class StateClassTests {

        @Test
        @DisplayName("State data class should have correct default values")
        fun stateDataClassShouldHaveCorrectDefaults() {
            val state = AstagfirullahSettings.State()

            assertEquals(Constants.DEFAULT_LANGUAGE, state.language)
            assertEquals(Constants.DEFAULT_DELAY_SECONDS, state.delaySeconds)
            assertEquals(Constants.DEFAULT_SOUND_ENABLED, state.soundEnabled)
            assertEquals(Constants.DEFAULT_SHOW_ON_STARTUP, state.showOnStartup)
            assertEquals(0L, state.installTime)
            assertEquals(Constants.RATING_STATE_FIRST, state.firstRatingTime)
            assertFalse(state.ratingPrompted)
            assertFalse(state.firstSetupCompleted)
            assertEquals(0L, state.lastRatingPromptTime)
        }

        @Test
        @DisplayName("State data class should support copying with modified values")
        fun stateDataClassShouldSupportCopying() {
            val originalState = AstagfirullahSettings.State()
            val copiedState = originalState.copy(
                language = "English",
                delaySeconds = "5",
                soundEnabled = true,
                showOnStartup = false
            )

            // Original should be unchanged
            assertEquals(Constants.DEFAULT_LANGUAGE, originalState.language)
            assertEquals(Constants.DEFAULT_DELAY_SECONDS, originalState.delaySeconds)
            assertFalse(originalState.soundEnabled)
            assertTrue(originalState.showOnStartup)

            // Copied should have new values
            assertEquals("English", copiedState.language)
            assertEquals("5", copiedState.delaySeconds)
            assertTrue(copiedState.soundEnabled)
            assertFalse(copiedState.showOnStartup)
        }

        @Test
        @DisplayName("State should support equality comparison")
        fun stateShouldSupportEquality() {
            val state1 = AstagfirullahSettings.State()
            val state2 = AstagfirullahSettings.State()

            assertEquals(state1, state2)

            state2.language = "English"
            assertNotEquals(state1, state2)
        }
    }

    @Nested
    @DisplayName("Settings Persistence Tests")
    inner class SettingsPersistenceTests {

        @Test
        @DisplayName("getState should return current state")
        fun getStateShouldReturnCurrentState() {
            settings.language = "English"
            settings.delaySeconds = "5"

            val state = settings.state

            assertEquals("English", state.language)
            assertEquals("5", state.delaySeconds)
        }

        @Test
        @DisplayName("loadState should copy values from provided state")
        fun loadStateShouldCopyValues() {
            val newState = AstagfirullahSettings.State(
                language = "Türkçe",
                delaySeconds = "3",
                soundEnabled = true,
                showOnStartup = false,
                installTime = 1234567890L,
                firstRatingTime = Constants.RATING_STATE_SECOND,
                ratingPrompted = true,
                firstSetupCompleted = true,
                lastRatingPromptTime = 9876543210L
            )

            settings.loadState(newState)

            assertEquals("Türkçe", settings.language)
            assertEquals("3", settings.delaySeconds)
            assertTrue(settings.soundEnabled)
            assertFalse(settings.showOnStartup)
            assertEquals(1234567890L, settings.installTime)
            assertEquals(Constants.RATING_STATE_SECOND, settings.firstRatingTime)
            assertTrue(settings.ratingPrompted)
            assertTrue(settings.firstSetupCompleted)
            assertEquals(9876543210L, settings.lastRatingPromptTime)
        }

        @Test
        @DisplayName("XmlSerializerUtil.copyBean should work correctly")
        fun xmlSerializerUtilCopyBeanShouldWork() {
            val sourceState = AstagfirullahSettings.State(
                language = "বাংলা",
                delaySeconds = "7.5",
                soundEnabled = true
            )
            val targetState = AstagfirullahSettings.State()

            XmlSerializerUtil.copyBean(sourceState, targetState)

            assertEquals("বাংলা", targetState.language)
            assertEquals("7.5", targetState.delaySeconds)
            assertTrue(targetState.soundEnabled)
        }
    }

    @Nested
    @DisplayName("Delay Conversion Tests")
    inner class DelayConversionTests {

        @Test
        @DisplayName("getDelayMillis should convert 1 second to 1000 milliseconds")
        fun getDelayMillisShouldConvert1SecondTo1000Ms() {
            settings.delaySeconds = "1"
            assertEquals(1000L, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should convert 1.5 seconds to 1500 milliseconds")
        fun getDelayMillisShouldConvert1Point5SecondsTo1500Ms() {
            settings.delaySeconds = "1.5"
            assertEquals(1500L, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should convert 5 seconds to 5000 milliseconds")
        fun getDelayMillisShouldConvert5SecondsTo5000Ms() {
            settings.delaySeconds = "5"
            assertEquals(5000L, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should convert 10 seconds to 10000 milliseconds")
        fun getDelayMillisShouldConvert10SecondsTo10000Ms() {
            settings.delaySeconds = "10"
            assertEquals(10000L, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should handle decimal values correctly")
        fun getDelayMillisShouldHandleDecimalValues() {
            settings.delaySeconds = "2.5"
            assertEquals(2500L, settings.getDelayMillis())

            settings.delaySeconds = "3.5"
            assertEquals(3500L, settings.getDelayMillis())

            settings.delaySeconds = "9.5"
            assertEquals(9500L, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should return default on invalid input")
        fun getDelayMillisShouldReturnDefaultOnInvalidInput() {
            settings.delaySeconds = "invalid"
            assertEquals(Constants.DEFAULT_DELAY_MILLIS, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should return default on empty string")
        fun getDelayMillisShouldReturnDefaultOnEmptyString() {
            settings.delaySeconds = ""
            assertEquals(Constants.DEFAULT_DELAY_MILLIS, settings.getDelayMillis())
        }

        @Test
        @DisplayName("getDelayMillis should handle all DELAY_OPTIONS correctly")
        fun getDelayMillisShouldHandleAllDelayOptions() {
            for (option in AstagfirullahSettings.DELAY_OPTIONS) {
                settings.delaySeconds = option
                val expectedMillis = (option.toDouble() * 1000).toLong()
                assertEquals(expectedMillis, settings.getDelayMillis(),
                    "Failed for delay option: $option")
            }
        }
    }

    @Nested
    @DisplayName("Language Selection Tests")
    inner class LanguageSelectionTests {

        @Test
        @DisplayName("Should be able to set and get language")
        fun shouldBeAbleToSetAndGetLanguage() {
            settings.language = "English"
            assertEquals("English", settings.language)
        }

        @Test
        @DisplayName("Should support all available languages")
        fun shouldSupportAllAvailableLanguages() {
            for (language in AstagfirullahSettings.SUPPORTED_LANGUAGES) {
                settings.language = language
                assertEquals(language, settings.language,
                    "Failed for language: $language")
            }
        }

        @Test
        @DisplayName("SUPPORTED_LANGUAGES should contain expected languages")
        fun supportedLanguagesShouldContainExpectedLanguages() {
            val expected = arrayOf(
                "العربية",      // Arabic
                "English",      // English
                "أردو",         // Urdu
                "فارسى",        // Farsi/Persian
                "Türkçe",       // Turkish
                "Bahasa",       // Indonesian
                "বাংলা"         // Bengali
            )

            assertArrayEquals(expected, AstagfirullahSettings.SUPPORTED_LANGUAGES)
            assertEquals(7, AstagfirullahSettings.SUPPORTED_LANGUAGES.size)
        }

        @Test
        @DisplayName("Should accept Arabic as language")
        fun shouldAcceptArabicLanguage() {
            settings.language = "العربية"
            assertEquals("العربية", settings.language)
        }

        @Test
        @DisplayName("Should accept English as language")
        fun shouldAcceptEnglishLanguage() {
            settings.language = "English"
            assertEquals("English", settings.language)
        }

        @Test
        @DisplayName("Should accept Urdu as language")
        fun shouldAcceptUrduLanguage() {
            settings.language = "أردو"
            assertEquals("أردو", settings.language)
        }
    }

    @Nested
    @DisplayName("Sound Enabled/Disabled Tests")
    inner class SoundEnabledTests {

        @Test
        @DisplayName("Should be able to enable sound")
        fun shouldBeAbleToEnableSound() {
            settings.soundEnabled = true
            assertTrue(settings.soundEnabled)
        }

        @Test
        @DisplayName("Should be able to disable sound")
        fun shouldBeAbleToDisableSound() {
            settings.soundEnabled = true
            settings.soundEnabled = false
            assertFalse(settings.soundEnabled)
        }

        @Test
        @DisplayName("Sound setting should persist in state")
        fun soundSettingShouldPersistInState() {
            settings.soundEnabled = true
            assertTrue(settings.state.soundEnabled)

            settings.soundEnabled = false
            assertFalse(settings.state.soundEnabled)
        }
    }

    @Nested
    @DisplayName("Show On Startup Tests")
    inner class ShowOnStartupTests {

        @Test
        @DisplayName("Should be able to enable show on startup")
        fun shouldBeAbleToEnableShowOnStartup() {
            settings.showOnStartup = true
            assertTrue(settings.showOnStartup)
        }

        @Test
        @DisplayName("Should be able to disable show on startup")
        fun shouldBeAbleToDisableShowOnStartup() {
            settings.showOnStartup = false
            assertFalse(settings.showOnStartup)
        }

        @Test
        @DisplayName("Show on startup setting should persist in state")
        fun showOnStartupSettingShouldPersistInState() {
            settings.showOnStartup = false
            assertFalse(settings.state.showOnStartup)

            settings.showOnStartup = true
            assertTrue(settings.state.showOnStartup)
        }
    }

    @Nested
    @DisplayName("First Run Detection Tests")
    inner class FirstRunDetectionTests {

        @Test
        @DisplayName("isFirstRun should return true when installTime is 0 and setup not completed")
        fun isFirstRunShouldReturnTrueWhenInstallTimeIsZeroAndSetupNotCompleted() {
            settings.installTime = 0L
            settings.firstSetupCompleted = false

            assertTrue(settings.isFirstRun())
        }

        @Test
        @DisplayName("isFirstRun should return false when installTime is non-zero")
        fun isFirstRunShouldReturnFalseWhenInstallTimeIsNonZero() {
            settings.installTime = System.currentTimeMillis()
            settings.firstSetupCompleted = false

            assertFalse(settings.isFirstRun())
        }

        @Test
        @DisplayName("isFirstRun should return false when setup is completed")
        fun isFirstRunShouldReturnFalseWhenSetupIsCompleted() {
            settings.installTime = 0L
            settings.firstSetupCompleted = true

            assertFalse(settings.isFirstRun())
        }

        @Test
        @DisplayName("isFirstRun should return false when both conditions are met for non-first run")
        fun isFirstRunShouldReturnFalseWhenBothConditionsAreMet() {
            settings.installTime = System.currentTimeMillis()
            settings.firstSetupCompleted = true

            assertFalse(settings.isFirstRun())
        }

        @Test
        @DisplayName("Should be able to set install time")
        fun shouldBeAbleToSetInstallTime() {
            val now = System.currentTimeMillis()
            settings.installTime = now

            assertEquals(now, settings.installTime)
        }

        @Test
        @DisplayName("Should be able to set first setup completed")
        fun shouldBeAbleToSetFirstSetupCompleted() {
            settings.firstSetupCompleted = true
            assertTrue(settings.firstSetupCompleted)
        }
    }

    @Nested
    @DisplayName("Rating Settings Tests")
    inner class RatingSettingsTests {

        @Test
        @DisplayName("Should be able to set and get firstRatingTime")
        fun shouldBeAbleToSetAndGetFirstRatingTime() {
            settings.firstRatingTime = Constants.RATING_STATE_SECOND
            assertEquals(Constants.RATING_STATE_SECOND, settings.firstRatingTime)

            settings.firstRatingTime = Constants.RATING_STATE_DONE
            assertEquals(Constants.RATING_STATE_DONE, settings.firstRatingTime)
        }

        @Test
        @DisplayName("Should be able to set and get ratingPrompted")
        fun shouldBeAbleToSetAndGetRatingPrompted() {
            settings.ratingPrompted = true
            assertTrue(settings.ratingPrompted)

            settings.ratingPrompted = false
            assertFalse(settings.ratingPrompted)
        }

        @Test
        @DisplayName("Should be able to set and get lastRatingPromptTime")
        fun shouldBeAbleToSetAndGetLastRatingPromptTime() {
            val now = System.currentTimeMillis()
            settings.lastRatingPromptTime = now

            assertEquals(now, settings.lastRatingPromptTime)
        }
    }

    @Nested
    @DisplayName("Companion Object Tests")
    inner class CompanionObjectTests {

        @Test
        @DisplayName("DELAY_OPTIONS should contain expected values")
        fun delayOptionsShouldContainExpectedValues() {
            val expected = arrayOf(
                "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5",
                "5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5",
                "9", "9.5", "10"
            )

            assertArrayEquals(expected, AstagfirullahSettings.DELAY_OPTIONS)
        }

        @Test
        @DisplayName("DELAY_OPTIONS should have 19 options")
        fun delayOptionsShouldHave19Options() {
            assertEquals(19, AstagfirullahSettings.DELAY_OPTIONS.size)
        }

        @Test
        @DisplayName("DELAY_OPTIONS should start with 1 and end with 10")
        fun delayOptionsShouldStartWith1AndEndWith10() {
            assertEquals("1", AstagfirullahSettings.DELAY_OPTIONS.first())
            assertEquals("10", AstagfirullahSettings.DELAY_OPTIONS.last())
        }
    }

    @Nested
    @DisplayName("Property Setters and Getters Tests")
    inner class PropertySettersGettersTests {

        @Test
        @DisplayName("All property setters should update state")
        fun allPropertySettersShouldUpdateState() {
            settings.language = "English"
            settings.delaySeconds = "5"
            settings.soundEnabled = true
            settings.showOnStartup = false
            settings.installTime = 123456L
            settings.firstRatingTime = "2"
            settings.ratingPrompted = true
            settings.firstSetupCompleted = true
            settings.lastRatingPromptTime = 654321L

            val state = settings.state

            assertEquals("English", state.language)
            assertEquals("5", state.delaySeconds)
            assertTrue(state.soundEnabled)
            assertFalse(state.showOnStartup)
            assertEquals(123456L, state.installTime)
            assertEquals("2", state.firstRatingTime)
            assertTrue(state.ratingPrompted)
            assertTrue(state.firstSetupCompleted)
            assertEquals(654321L, state.lastRatingPromptTime)
        }

        @Test
        @DisplayName("All property getters should return state values")
        fun allPropertyGettersShouldReturnStateValues() {
            val state = AstagfirullahSettings.State(
                language = "Bahasa",
                delaySeconds = "7",
                soundEnabled = true,
                showOnStartup = false,
                installTime = 111111L,
                firstRatingTime = "3",
                ratingPrompted = true,
                firstSetupCompleted = true,
                lastRatingPromptTime = 222222L
            )

            settings.loadState(state)

            assertEquals("Bahasa", settings.language)
            assertEquals("7", settings.delaySeconds)
            assertTrue(settings.soundEnabled)
            assertFalse(settings.showOnStartup)
            assertEquals(111111L, settings.installTime)
            assertEquals("3", settings.firstRatingTime)
            assertTrue(settings.ratingPrompted)
            assertTrue(settings.firstSetupCompleted)
            assertEquals(222222L, settings.lastRatingPromptTime)
        }
    }

    @Nested
    @DisplayName("Daily Dhikr Dialog Tests")
    inner class DailyDhikrTests {

        @Test
        @DisplayName("Daily dhikr is enabled by default")
        fun enabledByDefault() {
            assertTrue(settings.dailyDhikrEnabled)
            assertEquals(Constants.DEFAULT_DAILY_DHIKR_ENABLED, settings.dailyDhikrEnabled)
        }

        @Test
        @DisplayName("Shows on a fresh install, when no date has been recorded")
        fun showsWhenNeverShown() {
            assertEquals("", settings.lastDailyDhikrDate)
            assertTrue(settings.shouldShowDailyDhikr("2026-07-22"))
        }

        @Test
        @DisplayName("Does not show twice on the same day")
        fun doesNotShowTwiceSameDay() {
            settings.lastDailyDhikrDate = "2026-07-22"
            assertFalse(settings.shouldShowDailyDhikr("2026-07-22"))
        }

        @Test
        @DisplayName("Shows again the next day")
        fun showsAgainNextDay() {
            settings.lastDailyDhikrDate = "2026-07-22"
            assertTrue(settings.shouldShowDailyDhikr("2026-07-23"))
        }

        @Test
        @DisplayName("Never shows when the user disabled it, even on a new day")
        fun neverShowsWhenDisabled() {
            settings.dailyDhikrEnabled = false
            settings.lastDailyDhikrDate = "2026-07-22"
            assertFalse(settings.shouldShowDailyDhikr("2026-07-23"))
            settings.lastDailyDhikrDate = ""
            assertFalse(settings.shouldShowDailyDhikr("2026-07-23"))
        }

        @Test
        @DisplayName("Daily dhikr fields survive serialization round-trip")
        fun survivesSerialization() {
            settings.dailyDhikrEnabled = false
            settings.lastDailyDhikrDate = "2026-07-22"

            val restored = AstagfirullahSettings()
            XmlSerializerUtil.copyBean(settings.state, restored.state)

            assertFalse(restored.dailyDhikrEnabled)
            assertEquals("2026-07-22", restored.lastDailyDhikrDate)
        }
    }
}
