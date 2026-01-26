package com.alfayedoficial.astagfirullah.data.sync

import com.alfayedoficial.astagfirullah.data.api.LeaderboardApiService
import com.alfayedoficial.astagfirullah.data.cache.AuthCacheService
import com.alfayedoficial.astagfirullah.data.cache.LeaderboardCacheService
import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.alfayedoficial.astagfirullah.data.model.LeaderboardPeriod
import com.alfayedoficial.astagfirullah.data.model.LeaderboardResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service for coordinating leaderboard data fetching and caching.
 * Provides a unified interface for leaderboard operations.
 */
@Service(Service.Level.APP)
class LeaderboardSyncService {

    private val logger = Logger.getInstance(LeaderboardSyncService::class.java)
    private val isFetching = AtomicBoolean(false)

    // Callback for leaderboard state changes
    var onLeaderboardChanged: ((LeaderboardState) -> Unit)? = null

    companion object {
        @JvmStatic
        fun getInstance(): LeaderboardSyncService {
            return ApplicationManager.getApplication().getService(LeaderboardSyncService::class.java)
        }
    }

    /**
     * Leaderboard state for UI updates
     */
    sealed class LeaderboardState {
        object Loading : LeaderboardState()
        data class Success(
            val entries: List<LeaderboardEntry>,
            val currentUserRank: LeaderboardEntry?,
            val hasMore: Boolean,
            val period: LeaderboardPeriod
        ) : LeaderboardState()
        data class Error(val message: String) : LeaderboardState()
        object Empty : LeaderboardState()
    }

    /**
     * Result of fetch operation
     */
    sealed class FetchResult {
        data class Success(
            val entries: List<LeaderboardEntry>,
            val currentUserRank: LeaderboardEntry?,
            val hasMore: Boolean,
            val period: LeaderboardPeriod
        ) : FetchResult()
        data class Error(val message: String) : FetchResult()
    }

    /**
     * Fetches the leaderboard with the specified parameters.
     *
     * @param period Time period filter
     * @param page Page number (1-based) - currently unused as API returns all results
     * @param forceRefresh If true, bypasses cache
     * @return FetchResult indicating success or failure
     */
    fun fetchLeaderboard(
        period: LeaderboardPeriod = LeaderboardPeriod.ALL_TIME,
        page: Int = 1,
        forceRefresh: Boolean = false
    ): FetchResult {
        val cache = LeaderboardCacheService.getInstance()

        // Check cache first (unless force refresh)
        if (!forceRefresh && page == 1 && !cache.needsRefresh(period)) {
            val cachedEntries = cache.getCachedEntries()
            if (cachedEntries.isNotEmpty()) {
                logger.debug("Using cached leaderboard data")
                return FetchResult.Success(
                    entries = cachedEntries,
                    currentUserRank = findCurrentUserRank(cachedEntries),
                    hasMore = false,
                    period = period
                )
            }
        }

        if (!isFetching.compareAndSet(false, true)) {
            return FetchResult.Error("Fetch in progress")
        }

        try {
            onLeaderboardChanged?.invoke(LeaderboardState.Loading)

            // Get auth token if logged in
            val token = AuthCacheService.getInstance().getAuthToken()

            // Fetch from API
            when (val result = LeaderboardApiService.fetchTopUsers(period, page, token = token)) {
                is LeaderboardResult.Success -> {
                    val entries = result.entries

                    // Update cache
                    cache.updateCache(
                        entries = entries,
                        period = period
                    )

                    val currentUserRank = findCurrentUserRank(entries)

                    val fetchResult = FetchResult.Success(
                        entries = entries,
                        currentUserRank = currentUserRank,
                        hasMore = false, // API returns all results
                        period = period
                    )

                    onLeaderboardChanged?.invoke(
                        if (entries.isEmpty()) {
                            LeaderboardState.Empty
                        } else {
                            LeaderboardState.Success(
                                entries = entries,
                                currentUserRank = currentUserRank,
                                hasMore = false,
                                period = period
                            )
                        }
                    )

                    return fetchResult
                }

                is LeaderboardResult.Error -> {
                    logger.warn("Leaderboard fetch failed: ${result.message}")

                    // Try to return cached data on error
                    val cachedEntries = cache.getCachedEntries()
                    if (cachedEntries.isNotEmpty()) {
                        logger.debug("Returning cached data after fetch error")
                        val state = LeaderboardState.Success(
                            entries = cachedEntries,
                            currentUserRank = findCurrentUserRank(cachedEntries),
                            hasMore = false,
                            period = cache.getLastPeriod()
                        )
                        onLeaderboardChanged?.invoke(state)
                        return FetchResult.Success(
                            entries = cachedEntries,
                            currentUserRank = findCurrentUserRank(cachedEntries),
                            hasMore = false,
                            period = cache.getLastPeriod()
                        )
                    }

                    onLeaderboardChanged?.invoke(LeaderboardState.Error(result.message))
                    return FetchResult.Error(result.message)
                }
            }
        } finally {
            isFetching.set(false)
        }
    }

    /**
     * Loads more entries - currently returns error as API returns all results at once.
     */
    fun loadMore(): FetchResult {
        return FetchResult.Error("No more pages - all results returned")
    }

    /**
     * Fetches mini leaderboard for tool window (top 5).
     *
     * @param forceRefresh If true, bypasses cache
     * @return FetchResult with top 5 entries
     */
    fun fetchMiniLeaderboard(forceRefresh: Boolean = false): FetchResult {
        val cache = LeaderboardCacheService.getInstance()

        // For mini leaderboard, we can use full cache if available
        if (!forceRefresh && !cache.needsRefresh()) {
            val cachedEntries = cache.getCachedEntries().take(5)
            if (cachedEntries.isNotEmpty()) {
                return FetchResult.Success(
                    entries = cachedEntries,
                    currentUserRank = findCurrentUserRank(cache.getCachedEntries()),
                    hasMore = true,
                    period = LeaderboardPeriod.ALL_TIME
                )
            }
        }

        val token = AuthCacheService.getInstance().getAuthToken()

        return when (val result = LeaderboardApiService.fetchMiniLeaderboard(token)) {
            is LeaderboardResult.Success -> {
                FetchResult.Success(
                    entries = result.entries.take(5),
                    currentUserRank = findCurrentUserRank(result.entries),
                    hasMore = result.entries.size > 5,
                    period = LeaderboardPeriod.ALL_TIME
                )
            }

            is LeaderboardResult.Error -> {
                // Try cached data
                val cachedEntries = cache.getCachedEntries().take(5)
                if (cachedEntries.isNotEmpty()) {
                    FetchResult.Success(
                        entries = cachedEntries,
                        currentUserRank = findCurrentUserRank(cache.getCachedEntries()),
                        hasMore = true,
                        period = LeaderboardPeriod.ALL_TIME
                    )
                } else {
                    FetchResult.Error(result.message)
                }
            }
        }
    }

    /**
     * Finds the current user's rank in the entries list.
     */
    private fun findCurrentUserRank(entries: List<LeaderboardEntry>): LeaderboardEntry? {
        val userId = AuthCacheService.getInstance().getUserId() ?: return null
        if (userId <= 0) return null
        return entries.find { it.userId == userId }
    }

    /**
     * Gets cached leaderboard entries without fetching.
     */
    fun getCachedLeaderboard(): List<LeaderboardEntry> {
        return LeaderboardCacheService.getInstance().getCachedEntries()
    }

    /**
     * Gets the current user's rank from cache.
     */
    fun getCurrentUserRank(): LeaderboardEntry? {
        val entries = LeaderboardCacheService.getInstance().getCachedEntries()
        return findCurrentUserRank(entries)
    }

    /**
     * Clears the leaderboard cache.
     */
    fun clearCache() {
        LeaderboardCacheService.getInstance().clearCache()
        onLeaderboardChanged?.invoke(LeaderboardState.Empty)
    }

    /**
     * Checks if a fetch is currently in progress.
     */
    fun isFetchInProgress(): Boolean = isFetching.get()
}