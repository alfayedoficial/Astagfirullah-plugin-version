package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Service for tracking plugin usage statistics.
 * Persists statistics across IDE sessions.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahStatistics",
    storages = [Storage("astagfirullah-stats.xml")]
)
class StatisticsService : PersistentStateComponent<StatisticsService.State> {

    private var myState = State()

    companion object {
        @JvmStatic
        fun getInstance(): StatisticsService {
            return ApplicationManager.getApplication().getService(StatisticsService::class.java)
        }
    }

    data class State(
        var totalPhrasesDisplayed: Long = 0L,
        var totalSessionsCount: Long = 0L,
        var todayPhrasesDisplayed: Int = 0,
        var lastActiveDate: String = "",
        var favoriteLanguage: String = Constants.DEFAULT_LANGUAGE,
        var languageUsageCount: MutableMap<String, Int> = mutableMapOf()
    )

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
        // Reset daily counter if it's a new day
        checkAndResetDailyCounter()
    }

    private fun checkAndResetDailyCounter() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        if (myState.lastActiveDate != today) {
            myState.todayPhrasesDisplayed = 0
            myState.lastActiveDate = today
        }
    }

    /**
     * Records that phrases were displayed.
     * @param count Number of phrases displayed
     * @param language The language used
     */
    fun recordPhrasesDisplayed(count: Int, language: String) {
        checkAndResetDailyCounter()

        myState.totalPhrasesDisplayed += count
        myState.todayPhrasesDisplayed += count

        // Track language usage
        val currentCount = myState.languageUsageCount.getOrDefault(language, 0)
        myState.languageUsageCount[language] = currentCount + 1

        // Update favorite language
        updateFavoriteLanguage()
    }

    /**
     * Records a new session started.
     */
    fun recordSessionStarted() {
        myState.totalSessionsCount++
    }

    private fun updateFavoriteLanguage() {
        myState.favoriteLanguage = myState.languageUsageCount
            .maxByOrNull { it.value }?.key ?: Constants.DEFAULT_LANGUAGE
    }

    // Getters for statistics
    val totalPhrasesDisplayed: Long get() = myState.totalPhrasesDisplayed
    val totalSessionsCount: Long get() = myState.totalSessionsCount
    val todayPhrasesDisplayed: Int get() {
        checkAndResetDailyCounter()
        return myState.todayPhrasesDisplayed
    }
    val favoriteLanguage: String get() = myState.favoriteLanguage
}
