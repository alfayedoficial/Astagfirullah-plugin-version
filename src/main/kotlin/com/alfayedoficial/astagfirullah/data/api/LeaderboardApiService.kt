package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.model.*
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Service for handling leaderboard API calls.
 * Fetches top users with support for time period filtering and pagination.
 */
object LeaderboardApiService {

    private val logger = Logger.getInstance(LeaderboardApiService::class.java)
    private val gson = Gson()

    /**
     * Fetches the top users leaderboard.
     *
     * @param period Time period filter (daily, weekly, monthly, all-time)
     * @param page Page number for pagination (1-based)
     * @param limit Number of entries per page
     * @param token Optional authentication token for getting current user's rank
     * @return LeaderboardResult.Success with data, or LeaderboardResult.Error with message
     */
    fun fetchTopUsers(
        period: LeaderboardPeriod = LeaderboardPeriod.ALL_TIME,
        page: Int = 1,
        limit: Int = Constants.LEADERBOARD_DEFAULT_LIMIT,
        token: String? = null
    ): LeaderboardResult {
        // /statistic/top-users is behind auth:sanctum on the backend, so a guest request can
        // only ever come back 401. Fail fast without a network round-trip.
        if (token.isNullOrBlank()) {
            return LeaderboardResult.Error(SIGN_IN_REQUIRED)
        }

        return try {
            val url = buildUrl(period, page, limit)

            logger.debug("Fetching leaderboard: period=${period.apiValue}, page=$page, limit=$limit")

            val result = if (token != null) {
                ApiHelper.getWithAuth(url, token)
            } else {
                ApiHelper.get(url)
            }

            when (result) {
                is ApiHelper.HttpResult.Success -> {
                    val response = gson.fromJson(result.body, LeaderboardResponse::class.java)

                    if (response.status && response.data != null) {
                        logger.info("Leaderboard fetched: ${response.data.size} entries")
                        LeaderboardResult.Success(applyLimit(response.data, limit))
                    } else {
                        logger.warn("Leaderboard fetch failed: ${response.message}")
                        LeaderboardResult.Error(response.message)
                    }
                }

                is ApiHelper.HttpResult.Error -> {
                    logger.warn("Leaderboard HTTP error: ${result.message}")
                    LeaderboardResult.Error(getErrorMessage(result.errorBody) ?: result.message)
                }
            }
        } catch (e: Exception) {
            logger.error("Leaderboard fetch exception: ${e.message}", e)
            LeaderboardResult.Error("Failed to fetch leaderboard: ${e.message}")
        }
    }

    /**
     * Fetches a mini leaderboard for the tool window (top 5 users).
     *
     * @param token Optional authentication token for getting current user's rank
     * @return LeaderboardResult.Success with data, or LeaderboardResult.Error with message
     */
    fun fetchMiniLeaderboard(token: String? = null): LeaderboardResult {
        return fetchTopUsers(
            period = LeaderboardPeriod.ALL_TIME,
            page = 1,
            limit = Constants.LEADERBOARD_TOOL_WINDOW_LIMIT,
            token = token
        )
    }

    /**
     * Builds the leaderboard API URL with query parameters.
     */
    private fun buildUrl(period: LeaderboardPeriod, page: Int, limit: Int): String {
        return buildString {
            append(Constants.API_BASE_URL_V1)
            append(Constants.API_LEADERBOARD_ENDPOINT)
            append("?app_type=${Constants.API_APP_TYPE}")
            append("&period=${period.apiValue}")
            // /statistic/top-users ignores `period`; it only filters by start_date/end_date
            // (defaulting to "since the start of last year"). Without these every tab --
            // Today, This Week, This Month, All Time -- returned the same list.
            val (start, end) = dateRange(period)
            append("&start_date=$start")
            append("&end_date=$end")
            append("&page=$page")
            append("&limit=$limit")
        }
    }

    /**
     * The backend ignores `page`/`limit` and always returns its top [SERVER_TOP_N], plus the
     * caller's own row appended (rank > [SERVER_TOP_N]) when they are outside it. Apply the
     * limit here, keeping that appended self row so "your rank" still shows.
     */
    internal fun applyLimit(entries: List<LeaderboardEntry>, limit: Int): List<LeaderboardEntry> =
        entries.filter { it.rank <= limit || it.rank > SERVER_TOP_N }

    /** Size of the backend's fixed top-users list (StatisticController::getTopUsers). */
    private const val SERVER_TOP_N = 20

    /** Error returned for guests; the UI shows it in place of the list. */
    const val SIGN_IN_REQUIRED = "Sign in to see the leaderboard"

    /**
     * Maps a [LeaderboardPeriod] to the inclusive yyyy-MM-dd range the backend expects.
     * Weeks start on Monday, matching Carbon::startOfWeek() used by the backend's own weekly stats.
     */
    internal fun dateRange(
        period: LeaderboardPeriod,
        today: java.time.LocalDate = java.time.LocalDate.now()
    ): Pair<String, String> {
        val start = when (period) {
            LeaderboardPeriod.DAILY -> today
            LeaderboardPeriod.WEEKLY -> today.with(
                java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
            )
            LeaderboardPeriod.MONTHLY -> today.withDayOfMonth(1)
            LeaderboardPeriod.ALL_TIME -> ALL_TIME_START
        }
        return start.toString() to today.toString()
    }

    /** Fixed early start so "All Time" spans every year; the backend default is only the start of last year. */
    private val ALL_TIME_START: java.time.LocalDate = java.time.LocalDate.of(2020, 1, 1)

    /**
     * Extracts the main error message from error response body.
     */
    private fun getErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val jsonObject = gson.fromJson(errorBody, Map::class.java)
            jsonObject["message"]?.toString()
        } catch (e: Exception) {
            logger.debug("Could not parse error message: ${e.message}")
            null
        }
    }
}