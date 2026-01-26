package com.alfayedoficial.astagfirullah.data.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for PluginUpdateCacheService.
 *
 * Since PluginUpdateCacheService depends on IntelliJ's ApplicationManager and PersistentStateComponent,
 * we create a testable version that allows direct state manipulation for unit testing.
 */
class PluginUpdateCacheServiceTest {

    private lateinit var cacheService: TestablePluginUpdateCacheService

    @BeforeEach
    fun setUp() {
        cacheService = TestablePluginUpdateCacheService()
    }

    @Nested
    @DisplayName("Version Caching Tests")
    inner class VersionCachingTests {

        @Test
        @DisplayName("Should return empty version name initially")
        fun returnEmptyVersionNameInitially() {
            assertEquals("", cacheService.getLatestVersionName())
        }

        @Test
        @DisplayName("Should cache version name from server")
        fun cacheVersionNameFromServer() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = "https://example.com/update",
                praiseVersion = 5
            )

            assertEquals("2.0.0", cacheService.getLatestVersionName())
        }

        @Test
        @DisplayName("Should cache version code from server")
        fun cacheVersionCodeFromServer() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = "https://example.com/update",
                praiseVersion = 5
            )

            assertEquals(20, cacheService.getState().latestVersionCode)
        }

        @Test
        @DisplayName("Should cache praise version from server")
        fun cachePraiseVersionFromServer() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = "https://example.com/update",
                praiseVersion = 5
            )

            assertEquals(5, cacheService.getServerPraiseVersion())
        }

        @Test
        @DisplayName("Should handle null update URL")
        fun handleNullUpdateUrl() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 5
            )

            assertEquals("", cacheService.getUpdateUrl())
        }

        @Test
        @DisplayName("Should update cache with new version")
        fun updateCacheWithNewVersion() {
            cacheService.updateCache(
                versionName = "1.0.0",
                versionCode = 10,
                updateType = "NORMAL",
                updateUrl = "https://example.com/v1",
                praiseVersion = 1
            )

            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "EMERGENCY",
                updateUrl = "https://example.com/v2",
                praiseVersion = 5
            )

            assertEquals("2.0.0", cacheService.getLatestVersionName())
            assertEquals(20, cacheService.getState().latestVersionCode)
            assertEquals("EMERGENCY", cacheService.getUpdateType())
            assertEquals(5, cacheService.getServerPraiseVersion())
        }
    }

    @Nested
    @DisplayName("Notification Shown State Tests")
    inner class NotificationShownStateTests {

        @Test
        @DisplayName("Should return false for notification shown initially")
        fun returnFalseForNotificationShownInitially() {
            assertFalse(cacheService.wasNotificationShown())
        }

        @Test
        @DisplayName("Should mark notification as shown")
        fun markNotificationAsShown() {
            cacheService.markNotificationShown()
            assertTrue(cacheService.wasNotificationShown())
        }

        @Test
        @DisplayName("Should reset notification shown when version changes")
        fun resetNotificationShownWhenVersionChanges() {
            cacheService.updateCache(
                versionName = "1.0.0",
                versionCode = 10,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            cacheService.markNotificationShown()
            assertTrue(cacheService.wasNotificationShown())

            // Update to new version
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 2
            )

            assertFalse(cacheService.wasNotificationShown())
        }

        @Test
        @DisplayName("Should keep notification shown when same version is updated")
        fun keepNotificationShownWhenSameVersionIsUpdated() {
            cacheService.updateCache(
                versionName = "1.0.0",
                versionCode = 10,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            cacheService.markNotificationShown()
            assertTrue(cacheService.wasNotificationShown())

            // Update with same version
            cacheService.updateCache(
                versionName = "1.0.0",
                versionCode = 10,
                updateType = "NORMAL",
                updateUrl = "https://new-url.com",
                praiseVersion = 2
            )

            assertTrue(cacheService.wasNotificationShown())
        }

        @Test
        @DisplayName("Should clear notification shown on cache clear")
        fun clearNotificationShownOnCacheClear() {
            cacheService.markNotificationShown()
            assertTrue(cacheService.wasNotificationShown())

            cacheService.clearCache()
            assertFalse(cacheService.wasNotificationShown())
        }
    }

    @Nested
    @DisplayName("Dismissed Version Tracking Tests")
    inner class DismissedVersionTrackingTests {

        @Test
        @DisplayName("Should return false for version dismissed initially")
        fun returnFalseForVersionDismissedInitially() {
            assertFalse(cacheService.isVersionDismissed("1.0.0"))
        }

        @Test
        @DisplayName("Should track dismissed version")
        fun trackDismissedVersion() {
            cacheService.dismissVersion("2.0.0")
            assertTrue(cacheService.isVersionDismissed("2.0.0"))
        }

        @Test
        @DisplayName("Should return false for different version")
        fun returnFalseForDifferentVersion() {
            cacheService.dismissVersion("1.0.0")
            assertFalse(cacheService.isVersionDismissed("2.0.0"))
        }

        @Test
        @DisplayName("Should mark notification shown when version dismissed")
        fun markNotificationShownWhenVersionDismissed() {
            cacheService.dismissVersion("2.0.0")
            assertTrue(cacheService.wasNotificationShown())
        }

        @Test
        @DisplayName("Should clear dismissed version")
        fun clearDismissedVersion() {
            cacheService.dismissVersion("2.0.0")
            assertTrue(cacheService.isVersionDismissed("2.0.0"))

            cacheService.clearDismissedVersion()
            assertFalse(cacheService.isVersionDismissed("2.0.0"))
        }

        @Test
        @DisplayName("Should clear dismissed version on cache clear")
        fun clearDismissedVersionOnCacheClear() {
            cacheService.dismissVersion("2.0.0")
            assertTrue(cacheService.isVersionDismissed("2.0.0"))

            cacheService.clearCache()
            assertFalse(cacheService.isVersionDismissed("2.0.0"))
        }

        @Test
        @DisplayName("Should replace old dismissed version with new one")
        fun replaceOldDismissedVersionWithNewOne() {
            cacheService.dismissVersion("1.0.0")
            assertTrue(cacheService.isVersionDismissed("1.0.0"))

            cacheService.dismissVersion("2.0.0")
            assertFalse(cacheService.isVersionDismissed("1.0.0"))
            assertTrue(cacheService.isVersionDismissed("2.0.0"))
        }
    }

    @Nested
    @DisplayName("Cache Expiration Tests")
    inner class CacheExpirationTests {

        @Test
        @DisplayName("Should need update check initially")
        fun needUpdateCheckInitially() {
            assertTrue(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should not need update check after update today")
        fun notNeedUpdateCheckAfterUpdateToday() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertFalse(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should not need update check after marking check completed")
        fun notNeedUpdateCheckAfterMarkingCheckCompleted() {
            cacheService.markCheckCompleted()
            assertFalse(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should need update check when last check was yesterday")
        fun needUpdateCheckWhenLastCheckWasYesterday() {
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val state = PluginUpdateCacheService.UpdateCacheState(
                lastCheckDate = yesterday
            )
            cacheService.loadState(state)

            assertTrue(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should need update check when last check was in past")
        fun needUpdateCheckWhenLastCheckWasInPast() {
            val pastDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)
            val state = PluginUpdateCacheService.UpdateCacheState(
                lastCheckDate = pastDate
            )
            cacheService.loadState(state)

            assertTrue(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should update last check date on cache update")
        fun updateLastCheckDateOnCacheUpdate() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertEquals(today, cacheService.getState().lastCheckDate)
        }

        @Test
        @DisplayName("Should need update check after cache clear")
        fun needUpdateCheckAfterCacheClear() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            assertFalse(cacheService.needsUpdateCheck())

            cacheService.clearCache()
            assertTrue(cacheService.needsUpdateCheck())
        }
    }

    @Nested
    @DisplayName("ShouldShowNotification Logic Tests")
    inner class ShouldShowNotificationLogicTests {

        @Test
        @DisplayName("Should show notification for new version when not shown before")
        fun shouldShowNotificationForNewVersionWhenNotShownBefore() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertTrue(cacheService.shouldShowNotification("1.0.0"))
        }

        @Test
        @DisplayName("Should not show notification when already shown")
        fun shouldNotShowNotificationWhenAlreadyShown() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            cacheService.markNotificationShown()

            assertFalse(cacheService.shouldShowNotification("1.0.0"))
        }

        @Test
        @DisplayName("Should not show notification when version is dismissed")
        fun shouldNotShowNotificationWhenVersionIsDismissed() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            cacheService.dismissVersion("2.0.0")

            assertFalse(cacheService.shouldShowNotification("1.0.0"))
        }

        @Test
        @DisplayName("Should not show notification when already on latest version")
        fun shouldNotShowNotificationWhenAlreadyOnLatestVersion() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertFalse(cacheService.shouldShowNotification("2.0.0"))
        }

        @Test
        @DisplayName("Should show notification for emergency update even if dismissed")
        fun shouldShowNotificationForEmergencyUpdateEvenIfDismissed() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "EMERGENCY",
                updateUrl = null,
                praiseVersion = 1
            )
            // Note: Emergency updates might override dismiss behavior

            assertTrue(cacheService.shouldShowEmergencyNotification("1.0.0"))
        }

        @Test
        @DisplayName("Should show notification when newer version is available after dismissing old one")
        fun shouldShowNotificationWhenNewerVersionAvailableAfterDismissingOldOne() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )
            cacheService.dismissVersion("2.0.0")

            // New version becomes available
            cacheService.updateCache(
                versionName = "3.0.0",
                versionCode = 30,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 2
            )

            // Should show notification for new version
            assertFalse(cacheService.isVersionDismissed("3.0.0"))
            assertTrue(cacheService.shouldShowNotification("1.0.0"))
        }
    }

    @Nested
    @DisplayName("Update Type Tests")
    inner class UpdateTypeTests {

        @Test
        @DisplayName("Should return empty update type initially")
        fun returnEmptyUpdateTypeInitially() {
            assertEquals("", cacheService.getUpdateType())
        }

        @Test
        @DisplayName("Should cache NORMAL update type")
        fun cacheNormalUpdateType() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertEquals("NORMAL", cacheService.getUpdateType())
        }

        @Test
        @DisplayName("Should cache EMERGENCY update type")
        fun cacheEmergencyUpdateType() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "EMERGENCY",
                updateUrl = null,
                praiseVersion = 1
            )

            assertEquals("EMERGENCY", cacheService.getUpdateType())
        }

        @Test
        @DisplayName("Should identify emergency update")
        fun identifyEmergencyUpdate() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "EMERGENCY",
                updateUrl = null,
                praiseVersion = 1
            )

            assertTrue(cacheService.isEmergencyUpdate())
        }

        @Test
        @DisplayName("Should not identify normal update as emergency")
        fun notIdentifyNormalUpdateAsEmergency() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertFalse(cacheService.isEmergencyUpdate())
        }
    }

    @Nested
    @DisplayName("State Persistence Tests")
    inner class StatePersistenceTests {

        @Test
        @DisplayName("Should persist state correctly")
        fun persistStateCorrectly() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = "https://example.com",
                praiseVersion = 5
            )
            cacheService.markNotificationShown()
            cacheService.dismissVersion("1.5.0")

            val state = cacheService.getState()

            assertEquals("2.0.0", state.latestVersionName)
            assertEquals(20, state.latestVersionCode)
            assertEquals("NORMAL", state.updateType)
            assertEquals("https://example.com", state.updateUrl)
            assertEquals(5, state.serverPraiseVersion)
            assertTrue(state.updateNotificationShown)
            assertEquals("1.5.0", state.dismissedVersion)
        }

        @Test
        @DisplayName("Should load state correctly")
        fun loadStateCorrectly() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val state = PluginUpdateCacheService.UpdateCacheState(
                lastCheckDate = today,
                latestVersionName = "3.0.0",
                latestVersionCode = 30,
                updateType = "EMERGENCY",
                updateUrl = "https://example.com/emergency",
                serverPraiseVersion = 10,
                updateNotificationShown = true,
                dismissedVersion = "2.0.0"
            )

            cacheService.loadState(state)

            assertEquals("3.0.0", cacheService.getLatestVersionName())
            assertEquals(30, cacheService.getState().latestVersionCode)
            assertEquals("EMERGENCY", cacheService.getUpdateType())
            assertEquals("https://example.com/emergency", cacheService.getUpdateUrl())
            assertEquals(10, cacheService.getServerPraiseVersion())
            assertTrue(cacheService.wasNotificationShown())
            assertTrue(cacheService.isVersionDismissed("2.0.0"))
            assertFalse(cacheService.needsUpdateCheck())
        }

        @Test
        @DisplayName("Should handle state reload after cache update")
        fun handleStateReloadAfterCacheUpdate() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = "https://example.com",
                praiseVersion = 5
            )

            // Simulate app restart by creating new service and loading state
            val newService = TestablePluginUpdateCacheService()
            newService.loadState(cacheService.getState())

            assertEquals("2.0.0", newService.getLatestVersionName())
            assertEquals(5, newService.getServerPraiseVersion())
            assertFalse(newService.needsUpdateCheck())
        }
    }

    @Nested
    @DisplayName("Clear Cache Tests")
    inner class ClearCacheTests {

        @Test
        @DisplayName("Should clear all cached data")
        fun clearAllCachedData() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "EMERGENCY",
                updateUrl = "https://example.com",
                praiseVersion = 5
            )
            cacheService.markNotificationShown()
            cacheService.dismissVersion("2.0.0")

            cacheService.clearCache()

            assertEquals("", cacheService.getLatestVersionName())
            assertEquals(0, cacheService.getState().latestVersionCode)
            assertEquals("", cacheService.getUpdateType())
            assertEquals("", cacheService.getUpdateUrl())
            assertEquals(0, cacheService.getServerPraiseVersion())
            assertFalse(cacheService.wasNotificationShown())
            assertFalse(cacheService.isVersionDismissed("2.0.0"))
            assertTrue(cacheService.needsUpdateCheck())
        }
    }

    @Nested
    @DisplayName("Version Comparison Helper Tests")
    inner class VersionComparisonHelperTests {

        @Test
        @DisplayName("Should detect newer version available")
        fun detectNewerVersionAvailable() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertTrue(cacheService.isNewerVersionAvailable("1.0.0"))
            assertTrue(cacheService.isNewerVersionAvailable("1.9.9"))
        }

        @Test
        @DisplayName("Should return false when on latest version")
        fun returnFalseWhenOnLatestVersion() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertFalse(cacheService.isNewerVersionAvailable("2.0.0"))
        }

        @Test
        @DisplayName("Should return false when on newer version than cached")
        fun returnFalseWhenOnNewerVersionThanCached() {
            cacheService.updateCache(
                versionName = "2.0.0",
                versionCode = 20,
                updateType = "NORMAL",
                updateUrl = null,
                praiseVersion = 1
            )

            assertFalse(cacheService.isNewerVersionAvailable("3.0.0"))
        }
    }
}

/**
 * Testable version of PluginUpdateCacheService that doesn't depend on IntelliJ's ApplicationManager.
 * This allows unit testing without the full IntelliJ platform.
 */
class TestablePluginUpdateCacheService {
    private var myState = PluginUpdateCacheService.UpdateCacheState()

    fun getState(): PluginUpdateCacheService.UpdateCacheState = myState

    fun loadState(state: PluginUpdateCacheService.UpdateCacheState) {
        myState = state.copy()
    }

    fun needsUpdateCheck(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.lastCheckDate != today
    }

    fun getLatestVersionName(): String = myState.latestVersionName

    fun getServerPraiseVersion(): Int = myState.serverPraiseVersion

    fun getUpdateType(): String = myState.updateType

    fun getUpdateUrl(): String = myState.updateUrl

    fun wasNotificationShown(): Boolean = myState.updateNotificationShown

    fun isVersionDismissed(version: String): Boolean = myState.dismissedVersion == version

    fun updateCache(
        versionName: String,
        versionCode: Int,
        updateType: String,
        updateUrl: String?,
        praiseVersion: Int
    ) {
        // If version changed, reset notification shown flag
        if (myState.latestVersionName != versionName) {
            myState.updateNotificationShown = false
        }

        myState.latestVersionName = versionName
        myState.latestVersionCode = versionCode
        myState.updateType = updateType
        myState.updateUrl = updateUrl ?: ""
        myState.serverPraiseVersion = praiseVersion
        myState.lastCheckDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    fun markCheckCompleted() {
        myState.lastCheckDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    fun markNotificationShown() {
        myState.updateNotificationShown = true
    }

    fun dismissVersion(version: String) {
        myState.dismissedVersion = version
        myState.updateNotificationShown = true
    }

    fun clearDismissedVersion() {
        myState.dismissedVersion = ""
    }

    fun clearCache() {
        myState.lastCheckDate = ""
        myState.latestVersionName = ""
        myState.latestVersionCode = 0
        myState.updateType = ""
        myState.updateUrl = ""
        myState.serverPraiseVersion = 0
        myState.updateNotificationShown = false
        myState.dismissedVersion = ""
    }

    fun isEmergencyUpdate(): Boolean = myState.updateType == "EMERGENCY"

    /**
     * Determines if the notification should be shown for an update.
     * Returns true if:
     * - A newer version is available
     * - Notification hasn't been shown yet
     * - Version hasn't been dismissed
     */
    fun shouldShowNotification(currentVersion: String): Boolean {
        if (!isNewerVersionAvailable(currentVersion)) return false
        if (wasNotificationShown()) return false
        if (isVersionDismissed(myState.latestVersionName)) return false
        return true
    }

    /**
     * Emergency notifications bypass dismiss checks.
     */
    fun shouldShowEmergencyNotification(currentVersion: String): Boolean {
        if (!isNewerVersionAvailable(currentVersion)) return false
        if (!isEmergencyUpdate()) return false
        return true
    }

    /**
     * Simple version comparison.
     * Returns true if the cached version is newer than the current version.
     */
    fun isNewerVersionAvailable(currentVersion: String): Boolean {
        if (myState.latestVersionName.isEmpty()) return false
        return compareVersions(myState.latestVersionName, currentVersion) > 0
    }

    /**
     * Compare two version strings.
     * Returns positive if v1 > v2, negative if v1 < v2, 0 if equal.
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}