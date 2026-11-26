package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.data.model.LeaderboardPeriod
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
}
