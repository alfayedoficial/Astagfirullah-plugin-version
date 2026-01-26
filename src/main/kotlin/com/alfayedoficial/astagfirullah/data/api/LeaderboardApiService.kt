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
                        LeaderboardResult.Success(response.data)
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
            append("&page=$page")
            append("&limit=$limit")
        }
    }

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