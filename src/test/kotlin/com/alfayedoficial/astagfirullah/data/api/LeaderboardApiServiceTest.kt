package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.alfayedoficial.astagfirullah.data.model.LeaderboardPeriod
import com.alfayedoficial.astagfirullah.data.model.LeaderboardResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * The backend's /statistic/top-users filters only by start_date/end_date, so each
 * leaderboard period must map to a distinct date range.
 */
class LeaderboardApiServiceTest {

    // Wednesday
    private val today = LocalDate.of(2026, 11, 25)

    @Test
    fun `daily covers only today`() {
        assertEquals("2026-11-25" to "2026-11-25", LeaderboardApiService.dateRange(LeaderboardPeriod.DAILY, today))
    }

    @Test
    fun `weekly starts on Monday like the backend`() {
        assertEquals("2026-11-23" to "2026-11-25", LeaderboardApiService.dateRange(LeaderboardPeriod.WEEKLY, today))
    }

    @Test
    fun `weekly on a Monday starts that day`() {
        val monday = LocalDate.of(2026, 11, 23)
        assertEquals("2026-11-23" to "2026-11-23", LeaderboardApiService.dateRange(LeaderboardPeriod.WEEKLY, monday))
    }

    @Test
    fun `monthly starts on the first of the month`() {
        assertEquals("2026-11-01" to "2026-11-25", LeaderboardApiService.dateRange(LeaderboardPeriod.MONTHLY, today))
    }

    @Test
    fun `all time reaches back before the backend's default of last year`() {
        assertEquals("2020-01-01" to "2026-11-25", LeaderboardApiService.dateRange(LeaderboardPeriod.ALL_TIME, today))
    }

    private fun entry(rank: Int) = LeaderboardEntry(rank = rank, userId = rank, userName = "u$rank", totalCount = 100L - rank)

    @Test
    fun `limit trims the server's top list`() {
        val entries = (1..20).map(::entry)
        assertEquals((1..5).toList(), LeaderboardApiService.applyLimit(entries, 5).map { it.rank })
    }

    @Test
    fun `limit keeps the caller's own row appended outside the top list`() {
        val entries = (1..20).map(::entry) + entry(57)
        assertEquals(listOf(1, 2, 3, 4, 5, 57), LeaderboardApiService.applyLimit(entries, 5).map { it.rank })
    }

    @Test
    fun `guests get a sign-in error without a request`() {
        val result = LeaderboardApiService.fetchTopUsers(token = null)
        assertEquals(LeaderboardResult.Error(LeaderboardApiService.SIGN_IN_REQUIRED), result)
    }
}
