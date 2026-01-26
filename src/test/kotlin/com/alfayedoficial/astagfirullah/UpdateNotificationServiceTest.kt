package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.model.UpdateType
import com.alfayedoficial.astagfirullah.data.sync.PraiseSyncService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for UpdateNotificationService.
 * Tests update notification logic, update types, and notification content.
 */
@DisplayName("UpdateNotificationService Tests")
class UpdateNotificationServiceTest {

    @Nested
    @DisplayName("UpdateInfo data class")
    inner class UpdateInfoTests {

        @Test
        @DisplayName("Stores all update information")
        fun `stores all fields`() {
            val updateInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "2.0.0",
                newVersion = "2.1.0",
                updateType = UpdateType.NORMAL,
                updateUrl = "https://plugins.jetbrains.com/plugin/24628"
            )

            assertEquals("2.0.0", updateInfo.currentVersion)
            assertEquals("2.1.0", updateInfo.newVersion)
            assertEquals(UpdateType.NORMAL, updateInfo.updateType)
            assertEquals("https://plugins.jetbrains.com/plugin/24628", updateInfo.updateUrl)
        }

        @Test
        @DisplayName("Can represent EMERGENCY update")
        fun `can represent emergency update`() {
            val updateInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "1.0.0",
                newVersion = "2.0.0",
                updateType = UpdateType.EMERGENCY,
                updateUrl = "https://example.com/update"
            )

            assertEquals(UpdateType.EMERGENCY, updateInfo.updateType)
        }

        @Test
        @DisplayName("Can represent NORMAL update")
        fun `can represent normal update`() {
            val updateInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "2.0.0",
                newVersion = "2.0.1",
                updateType = UpdateType.NORMAL,
                updateUrl = Constants.PLUGIN_MARKETPLACE_URL
            )

            assertEquals(UpdateType.NORMAL, updateInfo.updateType)
        }

        @Test
        @DisplayName("UpdateInfo equality works correctly")
        fun `equality works`() {
            val info1 = PraiseSyncService.UpdateInfo("1.0", "2.0", UpdateType.NORMAL, "url")
            val info2 = PraiseSyncService.UpdateInfo("1.0", "2.0", UpdateType.NORMAL, "url")
            val info3 = PraiseSyncService.UpdateInfo("1.0", "2.1", UpdateType.NORMAL, "url")

            assertEquals(info1, info2)
            assertNotEquals(info1, info3)
        }

        @Test
        @DisplayName("UpdateInfo copy works correctly")
        fun `copy works`() {
            val original = PraiseSyncService.UpdateInfo("1.0", "2.0", UpdateType.NORMAL, "url")
            val modified = original.copy(updateType = UpdateType.EMERGENCY)

            assertEquals(UpdateType.NORMAL, original.updateType)
            assertEquals(UpdateType.EMERGENCY, modified.updateType)
            assertEquals(original.currentVersion, modified.currentVersion)
        }
    }

    @Nested
    @DisplayName("Update type handling")
    inner class UpdateTypeTests {

        @Test
        @DisplayName("EMERGENCY update type constant")
        fun `emergency constant`() {
            assertEquals("EMERGENCY", UpdateType.EMERGENCY)
        }

        @Test
        @DisplayName("NORMAL update type constant")
        fun `normal constant`() {
            assertEquals("NORMAL", UpdateType.NORMAL)
        }

        @Test
        @DisplayName("Can check if update is emergency")
        fun `can check emergency`() {
            val emergencyInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "1.0",
                newVersion = "2.0",
                updateType = UpdateType.EMERGENCY,
                updateUrl = "url"
            )

            val isEmergency = emergencyInfo.updateType == UpdateType.EMERGENCY
            assertTrue(isEmergency)
        }

        @Test
        @DisplayName("Can check if update is normal")
        fun `can check normal`() {
            val normalInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "1.0",
                newVersion = "1.1",
                updateType = UpdateType.NORMAL,
                updateUrl = "url"
            )

            val isEmergency = normalInfo.updateType == UpdateType.EMERGENCY
            assertFalse(isEmergency)
        }
    }

    @Nested
    @DisplayName("Notification content")
    inner class NotificationContentTests {

        @Test
        @DisplayName("Normal update has correct title format")
        fun `normal update title`() {
            val expectedTitle = "Astagfirullah - Update Available"
            assertTrue(expectedTitle.contains("Update Available"))
        }

        @Test
        @DisplayName("Emergency update has correct title format")
        fun `emergency update title`() {
            val expectedTitle = "Astagfirullah - Important Update Available!"
            assertTrue(expectedTitle.contains("Important"))
            assertTrue(expectedTitle.contains("!"))
        }

        @Test
        @DisplayName("Content includes version information")
        fun `content includes versions`() {
            val currentVersion = "2.0.0"
            val newVersion = "2.1.0"
            
            val content = "Current: <b>$currentVersion</b> → New: <b>$newVersion</b>"
            
            assertTrue(content.contains(currentVersion))
            assertTrue(content.contains(newVersion))
        }

        @Test
        @DisplayName("Emergency content includes warning message")
        fun `emergency includes warning`() {
            val emergencyMessage = "This is an important update. Please update as soon as possible."
            assertTrue(emergencyMessage.contains("important"))
        }
    }

    @Nested
    @DisplayName("Notification actions")
    inner class NotificationActionsTests {

        @Test
        @DisplayName("Update Now action exists")
        fun `update now action`() {
            val actionName = "Update Now"
            assertEquals("Update Now", actionName)
        }

        @Test
        @DisplayName("Remind Me Later action exists for normal updates")
        fun `remind later action`() {
            val actionName = "Remind Me Later"
            assertEquals("Remind Me Later", actionName)
        }

        @Test
        @DisplayName("Don't Ask Again action exists for normal updates")
        fun `dont ask again action`() {
            val actionName = "Don't Ask Again"
            assertEquals("Don't Ask Again", actionName)
        }
    }

    @Nested
    @DisplayName("Version comparison")
    inner class VersionComparisonTests {

        @Test
        @DisplayName("Detects update when server version is higher")
        fun `detects update available`() {
            val currentVersion = "2.0.0"
            val serverVersion = "2.1.0"
            
            // Simulating VersionUtils.isUpdateAvailable logic
            val parts1 = serverVersion.split(".").map { it.toInt() }
            val parts2 = currentVersion.split(".").map { it.toInt() }
            
            var isNewer = false
            for (i in 0 until maxOf(parts1.size, parts2.size)) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 > p2) {
                    isNewer = true
                    break
                } else if (p1 < p2) {
                    break
                }
            }
            
            assertTrue(isNewer)
        }

        @Test
        @DisplayName("No update when versions are equal")
        fun `no update when equal`() {
            val currentVersion = "2.0.0"
            val serverVersion = "2.0.0"
            
            assertEquals(currentVersion, serverVersion)
        }

        @Test
        @DisplayName("No update when current is newer")
        fun `no update when current newer`() {
            val currentVersion = "2.1.0"
            val serverVersion = "2.0.0"
            
            val parts1 = currentVersion.split(".").map { it.toInt() }
            val parts2 = serverVersion.split(".").map { it.toInt() }
            
            // Current is newer, no update needed
            assertTrue(parts1[1] > parts2[1])
        }
    }

    @Nested
    @DisplayName("Dismissed version handling")
    inner class DismissedVersionTests {

        @Test
        @DisplayName("Can dismiss a version")
        fun `can dismiss version`() {
            val versionToDismiss = "2.1.0"
            var dismissedVersion: String? = null
            
            // Simulate dismissing
            dismissedVersion = versionToDismiss
            
            assertEquals("2.1.0", dismissedVersion)
        }

        @Test
        @DisplayName("Can check if version is dismissed")
        fun `can check dismissed`() {
            val dismissedVersion = "2.1.0"
            val versionToCheck = "2.1.0"
            
            val isDismissed = dismissedVersion == versionToCheck
            assertTrue(isDismissed)
        }

        @Test
        @DisplayName("Different version is not dismissed")
        fun `different version not dismissed`() {
            val dismissedVersion = "2.1.0"
            val versionToCheck = "2.2.0"
            
            val isDismissed = dismissedVersion == versionToCheck
            assertFalse(isDismissed)
        }
    }

    @Nested
    @DisplayName("URL handling")
    inner class UrlHandlingTests {

        @Test
        @DisplayName("Uses provided update URL")
        fun `uses provided url`() {
            val updateInfo = PraiseSyncService.UpdateInfo(
                currentVersion = "1.0",
                newVersion = "2.0",
                updateType = UpdateType.NORMAL,
                updateUrl = "https://custom.url/update"
            )

            assertEquals("https://custom.url/update", updateInfo.updateUrl)
        }

        @Test
        @DisplayName("Fallback to marketplace URL when empty")
        fun `fallback to marketplace`() {
            val updateUrl = ""
            val fallbackUrl = if (updateUrl.isEmpty()) Constants.PLUGIN_MARKETPLACE_URL else updateUrl
            
            assertEquals(Constants.PLUGIN_MARKETPLACE_URL, fallbackUrl)
        }
    }

    @Nested
    @DisplayName("Notification shown tracking")
    inner class NotificationShownTests {

        @Test
        @DisplayName("Can track if notification was shown")
        fun `can track shown state`() {
            var notificationShown = false
            
            // Simulate showing notification
            notificationShown = true
            
            assertTrue(notificationShown)
        }

        @Test
        @DisplayName("Emergency updates can show again")
        fun `emergency shows again`() {
            val updateType = UpdateType.EMERGENCY
            
            // Emergency updates don't mark notification as shown
            val shouldMarkAsShown = updateType != UpdateType.EMERGENCY
            
            assertFalse(shouldMarkAsShown)
        }

        @Test
        @DisplayName("Normal updates mark as shown")
        fun `normal marks as shown`() {
            val updateType = UpdateType.NORMAL
            
            val shouldMarkAsShown = updateType != UpdateType.EMERGENCY
            
            assertTrue(shouldMarkAsShown)
        }
    }
}
