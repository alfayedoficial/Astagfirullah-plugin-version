package com.alfayedoficial.astagfirullah.data.cache

import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.alfayedoficial.astagfirullah.data.model.LeaderboardPeriod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Service for caching leaderboard data locally.
 * Provides offline access and reduces API calls.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahLeaderboard",
    storages = [Storage("astagfirullah-leaderboard.xml")]
)
class LeaderboardCacheService : PersistentStateComponent<LeaderboardCacheService.LeaderboardState> {

    private val logger = Logger.getInstance(LeaderboardCacheService::class.java)
    private val gson = Gson()
    private var myState = LeaderboardState()

    // In-memory cache for quick access
    private var cachedEntries: List<LeaderboardEntry>? = null
    private var cachedCurrentUserRank: LeaderboardEntry? = null

    companion object {
        private const val CACHE_EXPIRY_MINUTES = 30L // Cache expires after 30 minutes

        @JvmStatic
        fun getInstance(): LeaderboardCacheService {
            return ApplicationManager.getApplication().getService(LeaderboardCacheService::class.java)
        }
    }

    /**
     * Leaderboard cache state persisted to disk
     */
    data class LeaderboardState(
        var lastFetchTime: String = "",
        var lastPeriod: String = "all",
        var lastPage: Int = 1,
        var totalPages: Int = 1,
        var totalEntries: Int = 0,
        var entriesJson: String = "[]",
        var currentUserRankJson: String = ""
    )

    override fun getState(): LeaderboardState = myState

    override fun loadState(state: LeaderboardState) {
        XmlSerializerUtil.copyBean(state, myState)
        loadEntriesFromJson()
        logger.debug("Leaderboard cache loaded: ${cachedEntries?.size ?: 0} entries")
    }

    /**
     * Checks if cache needs to be refreshed.
     * Returns true if cache is empty, expired, or period changed.
     */
    fun needsRefresh(period: LeaderboardPeriod = LeaderboardPeriod.ALL_TIME): Boolean {
        if (myState.lastFetchTime.isEmpty()) return true
        if (myState.lastPeriod != period.apiValue) return true
        if (cachedEntries.isNullOrEmpty()) return true

        return try {
            val lastFetch = LocalDateTime.parse(myState.lastFetchTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val minutesSince = ChronoUnit.MINUTES.between(lastFetch, LocalDateTime.now())
            minutesSince >= CACHE_EXPIRY_MINUTES
        } catch (e: Exception) {
            logger.debug("Error parsing last fetch time: ${e.message}")
            true
        }
    }

    /**
     * Gets cached leaderboard entries.
     */
    fun getCachedEntries(): List<LeaderboardEntry> {
        if (cachedEntries == null) {
            loadEntriesFromJson()
        }
        return cachedEntries ?: emptyList()
    }

    /**
     * Gets the cached current user's rank entry.
     */
    fun getCurrentUserRank(): LeaderboardEntry? {
        if (cachedCurrentUserRank == null && myState.currentUserRankJson.isNotEmpty()) {
            loadCurrentUserRankFromJson()
        }
        return cachedCurrentUserRank
    }

    /**
     * Checks if we have any cached data.
     */
    fun hasCachedData(): Boolean = getCachedEntries().isNotEmpty()

    /**
     * Gets the last cached period.
     */
    fun getLastPeriod(): LeaderboardPeriod = LeaderboardPeriod.fromApiValue(myState.lastPeriod)

    /**
     * Gets the last cached page number.
     */
    fun getLastPage(): Int = myState.lastPage

    /**
     * Gets the total number of pages.
     */
    fun getTotalPages(): Int = myState.totalPages

    /**
     * Checks if there are more pages to load.
     */
    fun hasMorePages(): Boolean = myState.lastPage < myState.totalPages

    /**
     * Updates the cache with new leaderboard data.
     *
     * @param entries List of leaderboard entries
     * @param period The time period filter used
     */
    fun updateCache(
        entries: List<LeaderboardEntry>,
        period: LeaderboardPeriod
    ) {
        try {
            myState.entriesJson = gson.toJson(entries)
            myState.lastFetchTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            myState.lastPeriod = period.apiValue
            myState.lastPage = 1
            myState.totalPages = 1
            myState.totalEntries = entries.size

            // Update in-memory cache
            cachedEntries = entries

            logger.debug("Leaderboard cache updated: ${entries.size} entries, period=$period")
        } catch (e: Exception) {
            logger.error("Failed to update leaderboard cache: ${e.message}", e)
        }
    }

    /**
     * Clears the leaderboard cache.
     */
    fun clearCache() {
        myState.lastFetchTime = ""
        myState.lastPeriod = "all"
        myState.lastPage = 1
        myState.totalPages = 1
        myState.totalEntries = 0
        myState.entriesJson = "[]"
        myState.currentUserRankJson = ""
        cachedEntries = emptyList()
        cachedCurrentUserRank = null
        logger.debug("Leaderboard cache cleared")
    }

    /**
     * Loads entries from JSON state into memory.
     */
    private fun loadEntriesFromJson() {
        try {
            val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
            cachedEntries = gson.fromJson(myState.entriesJson, type) ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to load leaderboard entries from cache", e)
            cachedEntries = emptyList()
        }
    }

    /**
     * Loads current user rank from JSON state into memory.
     */
    private fun loadCurrentUserRankFromJson() {
        try {
            if (myState.currentUserRankJson.isNotEmpty()) {
                cachedCurrentUserRank = gson.fromJson(myState.currentUserRankJson, LeaderboardEntry::class.java)
            }
        } catch (e: Exception) {
            logger.error("Failed to load current user rank from cache", e)
            cachedCurrentUserRank = null
        }
    }
}