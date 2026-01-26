package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.util.xmlb.XmlSerializerUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for StatisticsService.
 * Tests statistics tracking, persistence, and daily counter reset logic.
 */
@DisplayName("StatisticsService Tests")
class StatisticsServiceTest {

    private lateinit var statisticsService: StatisticsService

    @BeforeEach
    fun setUp() {
        statisticsService = StatisticsService()
    }

    @Nested
    @DisplayName("Default Values Tests")
    inner class DefaultValuesTests {

        @Test
        @DisplayName("Should have totalPhrasesDisplayed set to 0 by default")
        fun totalPhrasesDisplayedShouldBeZeroByDefault() {
            assertEquals(0L, statisticsService.totalPhrasesDisplayed)
        }

        @Test
        @DisplayName("Should have totalSessionsCount set to 0 by default")
        fun totalSessionsCountShouldBeZeroByDefault() {
            assertEquals(0L, statisticsService.totalSessionsCount)
        }

        @Test
        @DisplayName("Should have todayPhrasesDisplayed set to 0 by default")
        fun todayPhrasesDisplayedShouldBeZeroByDefault() {
            assertEquals(0, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("Should have favoriteLanguage set to default language by default")
        fun favoriteLanguageShouldBeDefaultLanguageByDefault() {
            assertEquals(Constants.DEFAULT_LANGUAGE, statisticsService.favoriteLanguage)
        }
    }

    @Nested
    @DisplayName("State Class Tests")
    inner class StateClassTests {

        @Test
        @DisplayName("State data class should have correct default values")
        fun stateDataClassShouldHaveCorrectDefaults() {
            val state = StatisticsService.State()

            assertEquals(0L, state.totalPhrasesDisplayed)
            assertEquals(0L, state.totalSessionsCount)
            assertEquals(0, state.todayPhrasesDisplayed)
            assertEquals("", state.lastActiveDate)
            assertEquals(Constants.DEFAULT_LANGUAGE, state.favoriteLanguage)
            assertTrue(state.languageUsageCount.isEmpty())
        }

        @Test
        @DisplayName("State data class should support copying with modified values")
        fun stateDataClassShouldSupportCopying() {
            val originalState = StatisticsService.State()
            val copiedState = originalState.copy(
                totalPhrasesDisplayed = 100L,
                totalSessionsCount = 10L,
                todayPhrasesDisplayed = 5,
                favoriteLanguage = "English"
            )

            // Original should be unchanged
            assertEquals(0L, originalState.totalPhrasesDisplayed)
            assertEquals(0L, originalState.totalSessionsCount)
            assertEquals(0, originalState.todayPhrasesDisplayed)

            // Copied should have new values
            assertEquals(100L, copiedState.totalPhrasesDisplayed)
            assertEquals(10L, copiedState.totalSessionsCount)
            assertEquals(5, copiedState.todayPhrasesDisplayed)
            assertEquals("English", copiedState.favoriteLanguage)
        }

        @Test
        @DisplayName("State should support equality comparison")
        fun stateShouldSupportEquality() {
            val state1 = StatisticsService.State()
            val state2 = StatisticsService.State()

            assertEquals(state1, state2)

            state2.totalPhrasesDisplayed = 100L
            assertNotEquals(state1, state2)
        }
    }

    @Nested
    @DisplayName("Record Phrases Displayed Tests")
    inner class RecordPhrasesDisplayedTests {

        @Test
        @DisplayName("recordPhrasesDisplayed should increment totalPhrasesDisplayed")
        fun recordPhrasesDisplayedShouldIncrementTotal() {
            statisticsService.recordPhrasesDisplayed(5, "English")

            assertEquals(5L, statisticsService.totalPhrasesDisplayed)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should accumulate totalPhrasesDisplayed")
        fun recordPhrasesDisplayedShouldAccumulateTotal() {
            statisticsService.recordPhrasesDisplayed(5, "English")
            statisticsService.recordPhrasesDisplayed(3, "English")
            statisticsService.recordPhrasesDisplayed(2, "Arabic")

            assertEquals(10L, statisticsService.totalPhrasesDisplayed)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should increment todayPhrasesDisplayed")
        fun recordPhrasesDisplayedShouldIncrementToday() {
            statisticsService.recordPhrasesDisplayed(5, "English")

            assertEquals(5, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should accumulate todayPhrasesDisplayed")
        fun recordPhrasesDisplayedShouldAccumulateToday() {
            statisticsService.recordPhrasesDisplayed(5, "English")
            statisticsService.recordPhrasesDisplayed(3, "English")

            assertEquals(8, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should track language usage")
        fun recordPhrasesDisplayedShouldTrackLanguageUsage() {
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")

            val state = statisticsService.state
            assertEquals(2, state.languageUsageCount["English"])
            assertEquals(1, state.languageUsageCount["Arabic"])
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should update favorite language")
        fun recordPhrasesDisplayedShouldUpdateFavoriteLanguage() {
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")

            assertEquals("English", statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should handle single phrase")
        fun recordPhrasesDisplayedShouldHandleSinglePhrase() {
            statisticsService.recordPhrasesDisplayed(1, "Türkçe")

            assertEquals(1L, statisticsService.totalPhrasesDisplayed)
            assertEquals(1, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should handle large counts")
        fun recordPhrasesDisplayedShouldHandleLargeCounts() {
            statisticsService.recordPhrasesDisplayed(1000, "English")
            statisticsService.recordPhrasesDisplayed(500, "Arabic")

            assertEquals(1500L, statisticsService.totalPhrasesDisplayed)
            assertEquals(1500, statisticsService.todayPhrasesDisplayed)
        }
    }

    @Nested
    @DisplayName("Record Session Started Tests")
    inner class RecordSessionStartedTests {

        @Test
        @DisplayName("recordSessionStarted should increment totalSessionsCount")
        fun recordSessionStartedShouldIncrementCount() {
            statisticsService.recordSessionStarted()

            assertEquals(1L, statisticsService.totalSessionsCount)
        }

        @Test
        @DisplayName("recordSessionStarted should accumulate totalSessionsCount")
        fun recordSessionStartedShouldAccumulateCount() {
            statisticsService.recordSessionStarted()
            statisticsService.recordSessionStarted()
            statisticsService.recordSessionStarted()

            assertEquals(3L, statisticsService.totalSessionsCount)
        }

        @Test
        @DisplayName("recordSessionStarted should handle many sessions")
        fun recordSessionStartedShouldHandleManySessions() {
            repeat(100) {
                statisticsService.recordSessionStarted()
            }

            assertEquals(100L, statisticsService.totalSessionsCount)
        }
    }

    @Nested
    @DisplayName("Daily Counter Reset Tests")
    inner class DailyCounterResetTests {

        @Test
        @DisplayName("Today counter should reset when loading state from previous day")
        fun todayCounterShouldResetWhenLoadingStateFromPreviousDay() {
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val oldState = StatisticsService.State(
                totalPhrasesDisplayed = 100L,
                totalSessionsCount = 10L,
                todayPhrasesDisplayed = 50,
                lastActiveDate = yesterday
            )

            statisticsService.loadState(oldState)

            // Total should be preserved
            assertEquals(100L, statisticsService.totalPhrasesDisplayed)
            assertEquals(10L, statisticsService.totalSessionsCount)

            // Today counter should be reset
            assertEquals(0, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("Today counter should not reset when loading state from today")
        fun todayCounterShouldNotResetWhenLoadingStateFromToday() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val todayState = StatisticsService.State(
                totalPhrasesDisplayed = 100L,
                totalSessionsCount = 10L,
                todayPhrasesDisplayed = 50,
                lastActiveDate = today
            )

            statisticsService.loadState(todayState)

            // All values should be preserved
            assertEquals(100L, statisticsService.totalPhrasesDisplayed)
            assertEquals(10L, statisticsService.totalSessionsCount)
            assertEquals(50, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("Today counter should update lastActiveDate when reset")
        fun todayCounterShouldUpdateLastActiveDateWhenReset() {
            val lastWeek = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_DATE)
            val oldState = StatisticsService.State(
                todayPhrasesDisplayed = 50,
                lastActiveDate = lastWeek
            )

            statisticsService.loadState(oldState)

            // Access todayPhrasesDisplayed to trigger date check
            @Suppress("UNUSED_VARIABLE")
            val unused = statisticsService.todayPhrasesDisplayed

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            assertEquals(today, statisticsService.state.lastActiveDate)
        }

        @Test
        @DisplayName("Today counter getter should check and reset if new day")
        fun todayCounterGetterShouldCheckAndResetIfNewDay() {
            // Set up state with yesterday's date
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val state = statisticsService.state
            state.todayPhrasesDisplayed = 50
            state.lastActiveDate = yesterday

            // Access the getter - it should reset
            val todayCount = statisticsService.todayPhrasesDisplayed

            assertEquals(0, todayCount)
        }

        @Test
        @DisplayName("recordPhrasesDisplayed should reset daily counter if new day")
        fun recordPhrasesDisplayedShouldResetDailyCounterIfNewDay() {
            // Set up state with yesterday's date
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val state = statisticsService.state
            state.todayPhrasesDisplayed = 100
            state.lastActiveDate = yesterday

            // Record new phrases - this should reset daily counter first
            statisticsService.recordPhrasesDisplayed(5, "English")

            // Should be 5, not 105
            assertEquals(5, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("Should handle empty lastActiveDate (fresh install)")
        fun shouldHandleEmptyLastActiveDate() {
            val state = StatisticsService.State(
                todayPhrasesDisplayed = 0,
                lastActiveDate = ""
            )

            statisticsService.loadState(state)
            statisticsService.recordPhrasesDisplayed(5, "English")

            assertEquals(5, statisticsService.todayPhrasesDisplayed)

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            assertEquals(today, statisticsService.state.lastActiveDate)
        }
    }

    @Nested
    @DisplayName("Favorite Language Tracking Tests")
    inner class FavoriteLanguageTrackingTests {

        @Test
        @DisplayName("Favorite language should default to DEFAULT_LANGUAGE")
        fun favoriteLanguageShouldDefaultToDefaultLanguage() {
            assertEquals(Constants.DEFAULT_LANGUAGE, statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("Favorite language should update based on most used language")
        fun favoriteLanguageShouldUpdateBasedOnMostUsedLanguage() {
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")

            assertEquals("English", statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("Favorite language should change when another language becomes more used")
        fun favoriteLanguageShouldChangeWhenAnotherLanguageBecomesMoreUsed() {
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "English")
            assertEquals("English", statisticsService.favoriteLanguage)

            statisticsService.recordPhrasesDisplayed(1, "Arabic")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")
            assertEquals("Arabic", statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("Should track multiple languages correctly")
        fun shouldTrackMultipleLanguagesCorrectly() {
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "Arabic")
            statisticsService.recordPhrasesDisplayed(1, "Türkçe")
            statisticsService.recordPhrasesDisplayed(1, "Bahasa")
            statisticsService.recordPhrasesDisplayed(1, "English")
            statisticsService.recordPhrasesDisplayed(1, "Türkçe")
            statisticsService.recordPhrasesDisplayed(1, "Türkçe")

            val state = statisticsService.state
            assertEquals(2, state.languageUsageCount["English"])
            assertEquals(1, state.languageUsageCount["Arabic"])
            assertEquals(3, state.languageUsageCount["Türkçe"])
            assertEquals(1, state.languageUsageCount["Bahasa"])

            assertEquals("Türkçe", statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("Favorite language should be preserved on state load")
        fun favoriteLanguageShouldBePreservedOnStateLoad() {
            val state = StatisticsService.State(
                favoriteLanguage = "বাংলা",
                languageUsageCount = mutableMapOf("বাংলা" to 10, "English" to 5)
            )

            statisticsService.loadState(state)

            assertEquals("বাংলা", statisticsService.favoriteLanguage)
        }
    }

    @Nested
    @DisplayName("Statistics Persistence Tests")
    inner class StatisticsPersistenceTests {

        @Test
        @DisplayName("getState should return current state")
        fun getStateShouldReturnCurrentState() {
            statisticsService.recordPhrasesDisplayed(10, "English")
            statisticsService.recordSessionStarted()

            val state = statisticsService.state

            assertEquals(10L, state.totalPhrasesDisplayed)
            assertEquals(1L, state.totalSessionsCount)
            assertEquals(10, state.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("loadState should copy values from provided state")
        fun loadStateShouldCopyValues() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val newState = StatisticsService.State(
                totalPhrasesDisplayed = 500L,
                totalSessionsCount = 50L,
                todayPhrasesDisplayed = 25,
                lastActiveDate = today,
                favoriteLanguage = "فارسى",
                languageUsageCount = mutableMapOf("فارسى" to 100, "English" to 50)
            )

            statisticsService.loadState(newState)

            assertEquals(500L, statisticsService.totalPhrasesDisplayed)
            assertEquals(50L, statisticsService.totalSessionsCount)
            assertEquals(25, statisticsService.todayPhrasesDisplayed)
            assertEquals("فارسى", statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("XmlSerializerUtil.copyBean should work correctly")
        fun xmlSerializerUtilCopyBeanShouldWork() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val sourceState = StatisticsService.State(
                totalPhrasesDisplayed = 1000L,
                totalSessionsCount = 100L,
                todayPhrasesDisplayed = 50,
                lastActiveDate = today,
                favoriteLanguage = "أردو"
            )
            val targetState = StatisticsService.State()

            XmlSerializerUtil.copyBean(sourceState, targetState)

            assertEquals(1000L, targetState.totalPhrasesDisplayed)
            assertEquals(100L, targetState.totalSessionsCount)
            assertEquals(50, targetState.todayPhrasesDisplayed)
            assertEquals(today, targetState.lastActiveDate)
            assertEquals("أردو", targetState.favoriteLanguage)
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    inner class GetterTests {

        @Test
        @DisplayName("totalPhrasesDisplayed getter should return state value")
        fun totalPhrasesDisplayedGetterShouldReturnStateValue() {
            statisticsService.recordPhrasesDisplayed(100, "English")
            assertEquals(100L, statisticsService.totalPhrasesDisplayed)
        }

        @Test
        @DisplayName("totalSessionsCount getter should return state value")
        fun totalSessionsCountGetterShouldReturnStateValue() {
            repeat(5) { statisticsService.recordSessionStarted() }
            assertEquals(5L, statisticsService.totalSessionsCount)
        }

        @Test
        @DisplayName("todayPhrasesDisplayed getter should return state value after check")
        fun todayPhrasesDisplayedGetterShouldReturnStateValueAfterCheck() {
            statisticsService.recordPhrasesDisplayed(25, "English")
            assertEquals(25, statisticsService.todayPhrasesDisplayed)
        }

        @Test
        @DisplayName("favoriteLanguage getter should return state value")
        fun favoriteLanguageGetterShouldReturnStateValue() {
            statisticsService.recordPhrasesDisplayed(1, "Bahasa")
            assertEquals("Bahasa", statisticsService.favoriteLanguage)
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero count in recordPhrasesDisplayed")
        fun shouldHandleZeroCountInRecordPhrasesDisplayed() {
            statisticsService.recordPhrasesDisplayed(0, "English")

            assertEquals(0L, statisticsService.totalPhrasesDisplayed)
            assertEquals(0, statisticsService.todayPhrasesDisplayed)
            // Language usage count should still be incremented
            assertEquals(1, statisticsService.state.languageUsageCount["English"])
        }

        @Test
        @DisplayName("Should handle very large numbers")
        fun shouldHandleVeryLargeNumbers() {
            val state = StatisticsService.State(
                totalPhrasesDisplayed = Long.MAX_VALUE - 1000,
                totalSessionsCount = Long.MAX_VALUE - 1000
            )

            statisticsService.loadState(state)

            assertEquals(Long.MAX_VALUE - 1000, statisticsService.totalPhrasesDisplayed)
            assertEquals(Long.MAX_VALUE - 1000, statisticsService.totalSessionsCount)
        }

        @Test
        @DisplayName("Should handle empty language usage map")
        fun shouldHandleEmptyLanguageUsageMap() {
            val state = StatisticsService.State(
                languageUsageCount = mutableMapOf()
            )

            statisticsService.loadState(state)

            assertEquals(Constants.DEFAULT_LANGUAGE, statisticsService.favoriteLanguage)
        }

        @Test
        @DisplayName("Should handle all supported languages")
        fun shouldHandleAllSupportedLanguages() {
            val languages = arrayOf(
                "العربية", "English", "أردو", "فارسى", "Türkçe", "Bahasa", "বাংলা"
            )

            for (language in languages) {
                statisticsService.recordPhrasesDisplayed(1, language)
            }

            assertEquals(7L, statisticsService.totalPhrasesDisplayed)
            assertEquals(7, statisticsService.state.languageUsageCount.size)
        }

        @Test
        @DisplayName("Should preserve language usage counts through state load")
        fun shouldPreserveLanguageUsageCountsThroughStateLoad() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val state = StatisticsService.State(
                languageUsageCount = mutableMapOf(
                    "English" to 100,
                    "Arabic" to 50,
                    "Turkish" to 25
                ),
                lastActiveDate = today
            )

            statisticsService.loadState(state)

            assertEquals(100, statisticsService.state.languageUsageCount["English"])
            assertEquals(50, statisticsService.state.languageUsageCount["Arabic"])
            assertEquals(25, statisticsService.state.languageUsageCount["Turkish"])
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Should maintain correct statistics through multiple operations")
        fun shouldMaintainCorrectStatisticsThroughMultipleOperations() {
            // Start sessions
            statisticsService.recordSessionStarted()
            statisticsService.recordSessionStarted()

            // Display phrases
            statisticsService.recordPhrasesDisplayed(10, "English")
            statisticsService.recordPhrasesDisplayed(5, "Arabic")
            statisticsService.recordPhrasesDisplayed(15, "English")

            // Verify all statistics
            assertEquals(2L, statisticsService.totalSessionsCount)
            assertEquals(30L, statisticsService.totalPhrasesDisplayed)
            assertEquals(30, statisticsService.todayPhrasesDisplayed)
            assertEquals("English", statisticsService.favoriteLanguage)
            assertEquals(2, statisticsService.state.languageUsageCount["English"])
            assertEquals(1, statisticsService.state.languageUsageCount["Arabic"])
        }

        @Test
        @DisplayName("Should correctly persist and load state")
        fun shouldCorrectlyPersistAndLoadState() {
            // Build up some statistics
            statisticsService.recordSessionStarted()
            statisticsService.recordPhrasesDisplayed(50, "Türkçe")
            statisticsService.recordPhrasesDisplayed(30, "Türkçe")
            statisticsService.recordPhrasesDisplayed(20, "English")

            // Get current state
            val savedState = statisticsService.state

            // Create new service and load state
            val newService = StatisticsService()
            newService.loadState(savedState.copy(lastActiveDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)))

            // Verify statistics are preserved
            assertEquals(statisticsService.totalSessionsCount, newService.totalSessionsCount)
            assertEquals(statisticsService.totalPhrasesDisplayed, newService.totalPhrasesDisplayed)
            assertEquals(statisticsService.favoriteLanguage, newService.favoriteLanguage)
        }
    }
}