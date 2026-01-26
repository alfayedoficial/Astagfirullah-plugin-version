package com.alfayedoficial.astagfirullah.data.model

import com.google.gson.annotations.SerializedName

// ==================== Leaderboard Entry ====================

/**
 * Single entry in the leaderboard
 * Matches API response: { name, user_id, total_count, rank }
 */
data class LeaderboardEntry(
    @SerializedName("rank") val rank: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val userName: String,
    @SerializedName("total_count") val totalCount: Long
)

// ==================== API Response Models ====================

/**
 * Leaderboard API response wrapper
 * API returns: { status, message, data: [...entries] }
 */
data class LeaderboardResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<LeaderboardEntry>?
)

// ==================== Leaderboard Period Enum ====================

/**
 * Time period for leaderboard filtering
 */
enum class LeaderboardPeriod(val apiValue: String, val displayName: String) {
    DAILY("daily", "Today"),
    WEEKLY("weekly", "This Week"),
    MONTHLY("monthly", "This Month"),
    ALL_TIME("all", "All Time");

    companion object {
        fun fromApiValue(value: String): LeaderboardPeriod {
            return entries.find { it.apiValue == value } ?: ALL_TIME
        }

        fun fromDisplayName(name: String): LeaderboardPeriod {
            return entries.find { it.displayName == name } ?: ALL_TIME
        }
    }
}

// ==================== Result Sealed Classes ====================

/**
 * Result of leaderboard fetch operation
 */
sealed class LeaderboardResult {
    data class Success(
        val entries: List<LeaderboardEntry>
    ) : LeaderboardResult()

    data class Error(
        val message: String
    ) : LeaderboardResult()
}

// ==================== Leaderboard Constants ====================

/**
 * Leaderboard-related constants
 */
object LeaderboardConstants {
    const val DEFAULT_PAGE_SIZE = 50
    const val TOOL_WINDOW_PAGE_SIZE = 5
    const val MAX_PAGE_SIZE = 100

    // Medal ranks
    const val GOLD_RANK = 1
    const val SILVER_RANK = 2
    const val BRONZE_RANK = 3
}
