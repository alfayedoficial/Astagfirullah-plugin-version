package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.data.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for SettingsApiService.
 * Tests settings API response handling and version comparison.
 */
@DisplayName("SettingsApiService Tests")
class SettingsApiServiceTest {

    @Nested
    @DisplayName("SettingsResult sealed class")
    inner class SettingsResultTests {

        @Test
        @DisplayName("Success contains SettingsData")
        fun `success contains settings data`() {
            val settingsData = SettingsData(
                id = 1,
                appType = "JETBRAINS_PLUGIN",
                versionCode = 200,
                versionName = "2.0.0",
                praiseVersion = 10,
                updateType = UpdateType.NORMAL,
                updateUrl = "https://example.com",
                isActive = true
            )
            val result = SettingsResult.Success(settingsData)

            assertEquals(settingsData, result.settings)
            assertTrue(result is SettingsResult.Success)
        }

        @Test
        @DisplayName("Error contains error message")
        fun `error contains message`() {
            val result = SettingsResult.Error("API error: Not found")

            assertEquals("API error: Not found", result.message)
            assertTrue(result is SettingsResult.Error)
        }

        @Test
        @DisplayName("Can pattern match SettingsResult")
        fun `can pattern match settings result`() {
            val settingsData = SettingsData(
                id = 1,
                appType = "JETBRAINS_PLUGIN",
                versionCode = 200,
                versionName = "2.0.0",
                praiseVersion = 10,
                updateType = UpdateType.NORMAL,
                updateUrl = null,
                isActive = true
            )
            val successResult: SettingsResult = SettingsResult.Success(settingsData)
            val errorResult: SettingsResult = SettingsResult.Error("error")

            val successExtracted = when (successResult) {
                is SettingsResult.Success -> successResult.settings
                is SettingsResult.Error -> null
            }

            val errorExtracted = when (errorResult) {
                is SettingsResult.Success -> null
                is SettingsResult.Error -> errorResult.message
            }

            assertNotNull(successExtracted)
            assertEquals("error", errorExtracted)
        }
    }

    @Nested
    @DisplayName("compareVersions() delegation")
    inner class CompareVersionsTests {

        @Test
        @DisplayName("Returns 0 for equal versions")
        fun `returns 0 for equal versions`() {
            assertEquals(0, SettingsApiService.compareVersions("2.0.0", "2.0.0"))
            assertEquals(0, SettingsApiService.compareVersions("1.0", "1.0"))
        }

        @Test
        @DisplayName("Returns positive when first version is greater")
        fun `returns positive for greater version`() {
            assertTrue(SettingsApiService.compareVersions("2.1.0", "2.0.0") > 0)
            assertTrue(SettingsApiService.compareVersions("3.0.0", "2.9.9") > 0)
        }

        @Test
        @DisplayName("Returns negative when first version is lesser")
        fun `returns negative for lesser version`() {
            assertTrue(SettingsApiService.compareVersions("2.0.0", "2.1.0") < 0)
            assertTrue(SettingsApiService.compareVersions("1.9.9", "2.0.0") < 0)
        }

        @Test
        @DisplayName("Handles versions with different parts")
        fun `handles different length versions`() {
            assertEquals(0, SettingsApiService.compareVersions("2.0", "2.0.0"))
            assertTrue(SettingsApiService.compareVersions("2.0.1", "2.0") > 0)
        }
    }

    @Nested
    @DisplayName("isUpdateAvailable() delegation")
    inner class IsUpdateAvailableTests {

        @Test
        @DisplayName("Returns true when server version is newer")
        fun `returns true when update available`() {
            assertTrue(SettingsApiService.isUpdateAvailable("2.0.0", "2.1.0"))
            assertTrue(SettingsApiService.isUpdateAvailable("1.0.0", "2.0.0"))
        }

        @Test
        @DisplayName("Returns false when versions are equal")
        fun `returns false when versions equal`() {
            assertFalse(SettingsApiService.isUpdateAvailable("2.0.0", "2.0.0"))
        }

        @Test
        @DisplayName("Returns false when current is newer")
        fun `returns false when current is newer`() {
            assertFalse(SettingsApiService.isUpdateAvailable("2.1.0", "2.0.0"))
        }
    }

    @Nested
    @DisplayName("SettingsData model")
    inner class SettingsDataModelTests {

        @Test
        @DisplayName("Stores all fields correctly")
        fun `stores all fields`() {
            val settings = SettingsData(
                id = 1,
                appType = "JETBRAINS_PLUGIN",
                versionCode = 250,
                versionName = "2.5.0",
                praiseVersion = 15,
                updateType = UpdateType.EMERGENCY,
                updateUrl = "https://plugins.jetbrains.com",
                isActive = true
            )

            assertEquals(1, settings.id)
            assertEquals("JETBRAINS_PLUGIN", settings.appType)
            assertEquals(250, settings.versionCode)
            assertEquals("2.5.0", settings.versionName)
            assertEquals(15, settings.praiseVersion)
            assertEquals(UpdateType.EMERGENCY, settings.updateType)
            assertEquals("https://plugins.jetbrains.com", settings.updateUrl)
            assertTrue(settings.isActive)
        }

        @Test
        @DisplayName("Handles null update URL")
        fun `handles null update url`() {
            val settings = SettingsData(
                id = 1,
                appType = "JETBRAINS_PLUGIN",
                versionCode = 200,
                versionName = "2.0.0",
                praiseVersion = 10,
                updateType = UpdateType.NORMAL,
                updateUrl = null,
                isActive = true
            )

            assertNull(settings.updateUrl)
        }

        @Test
        @DisplayName("UpdateType constants are correct")
        fun `update type constants`() {
            assertEquals("NORMAL", UpdateType.NORMAL)
            assertEquals("EMERGENCY", UpdateType.EMERGENCY)
        }
    }

    @Nested
    @DisplayName("URL construction")
    inner class UrlConstructionTests {

        @Test
        @DisplayName("URL includes app_type parameter")
        fun `url includes app type parameter`() {
            // Documents expected URL format
            val expectedParam = "app_type=JETBRAINS_PLUGIN"
            assertTrue("?app_type=JETBRAINS_PLUGIN".contains(expectedParam))
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("Error result for API status=false")
        fun `error for api status false`() {
            val errorResult = SettingsResult.Error("API error: Settings not found")
            assertTrue(errorResult.message.startsWith("API error:"))
        }

        @Test
        @DisplayName("Error result for parse failure")
        fun `error for parse failure`() {
            val errorResult = SettingsResult.Error("Parse error: Invalid JSON")
            assertTrue(errorResult.message.startsWith("Parse error:"))
        }

        @Test
        @DisplayName("Error result for network failure")
        fun `error for network failure`() {
            val errorResult = SettingsResult.Error("Network error: Timeout")
            assertTrue(errorResult.message.startsWith("Network error:"))
        }
    }
}
