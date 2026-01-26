package com.alfayedoficial.astagfirullah.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

/**
 * Unit tests for [Constants] object.
 * Verifies that all constants are properly defined and have valid values.
 */
@DisplayName("Constants")
class ConstantsTest {

    @Nested
    @DisplayName("Plugin Information")
    inner class PluginInformationTest {

        @Test
        @DisplayName("PLUGIN_ID should be a valid package identifier")
        fun pluginIdIsValidIdentifier() {
            val pluginId = Constants.PLUGIN_ID
            assertNotNull(pluginId)
            assertTrue(pluginId.isNotBlank())
            assertTrue(pluginId.matches(Regex("^[a-z][a-z0-9]*([.][a-z][a-z0-9]*)*$")))
            assertEquals("com.alfayedoficial.astagfirullah", pluginId)
        }

        @Test
        @DisplayName("PLUGIN_VERSION should follow semantic versioning")
        fun pluginVersionIsSemanticVersion() {
            val version = Constants.PLUGIN_VERSION
            assertNotNull(version)
            assertTrue(version.isNotBlank())
            // Should match semantic versioning pattern (MAJOR.MINOR.PATCH)
            assertTrue(version.matches(Regex("^\\d+\\.\\d+\\.\\d+$")))
        }

        @Test
        @DisplayName("PLUGIN_NAME should be non-empty")
        fun pluginNameIsNonEmpty() {
            val name = Constants.PLUGIN_NAME
            assertNotNull(name)
            assertTrue(name.isNotBlank())
            assertEquals("Astagfirullah", name)
        }
    }

    @Nested
    @DisplayName("API Configuration")
    inner class ApiConfigurationTest {

        @Test
        @DisplayName("API_BASE_URL should be valid HTTPS URL")
        fun apiBaseUrlIsValidHttps() {
            val url = Constants.API_BASE_URL
            assertNotNull(url)
            assertTrue(url.startsWith("https://"))
            assertDoesNotThrow { URI(url) }
        }

        @Test
        @DisplayName("API endpoints should start with slash")
        fun apiEndpointsStartWithSlash() {
            assertTrue(Constants.API_PRAISE_LIST_ENDPOINT.startsWith("/"))
            assertTrue(Constants.API_SETTINGS_ENDPOINT.startsWith("/"))
        }

        @Test
        @DisplayName("API_TIMEOUT_SECONDS should be positive and reasonable")
        fun apiTimeoutIsPositiveAndReasonable() {
            val timeout = Constants.API_TIMEOUT_SECONDS
            assertTrue(timeout > 0)
            assertTrue(timeout <= 120) // Should not exceed 2 minutes
            assertEquals(30L, timeout)
        }

        @Test
        @DisplayName("API_CATEGORY_FILTER should be positive")
        fun apiCategoryFilterIsPositive() {
            assertTrue(Constants.API_CATEGORY_FILTER > 0)
        }

        @Test
        @DisplayName("API_APP_TYPE should be non-empty")
        fun apiAppTypeIsNonEmpty() {
            assertTrue(Constants.API_APP_TYPE.isNotBlank())
            assertEquals("JETBRAINS_PLUGIN", Constants.API_APP_TYPE)
        }
    }

    @Nested
    @DisplayName("URLs")
    inner class UrlsTest {

        @Test
        @DisplayName("PLUGIN_MARKETPLACE_URL should be valid JetBrains marketplace URL")
        fun pluginMarketplaceUrlIsValid() {
            val url = Constants.PLUGIN_MARKETPLACE_URL
            assertNotNull(url)
            assertTrue(url.startsWith("https://plugins.jetbrains.com/"))
            assertDoesNotThrow { URI(url) }
        }

        @Test
        @DisplayName("DEVELOPER_LINKEDIN_URL should be valid LinkedIn URL")
        fun developerLinkedInUrlIsValid() {
            val url = Constants.DEVELOPER_LINKEDIN_URL
            assertNotNull(url)
            assertTrue(url.startsWith("https://www.linkedin.com/"))
            assertDoesNotThrow { URI(url) }
        }

        @Test
        @DisplayName("LINKEDIN_SHARE_BASE_URL should be valid LinkedIn share URL")
        fun linkedInShareBaseUrlIsValid() {
            val url = Constants.LINKEDIN_SHARE_BASE_URL
            assertNotNull(url)
            assertTrue(url.startsWith("https://www.linkedin.com/shareArticle"))
            assertTrue(url.contains("mini=true"))
            assertTrue(url.endsWith("url="))
        }

        @Test
        @DisplayName("All URLs should use HTTPS")
        fun allUrlsUseHttps() {
            assertTrue(Constants.PLUGIN_MARKETPLACE_URL.startsWith("https://"))
            assertTrue(Constants.DEVELOPER_LINKEDIN_URL.startsWith("https://"))
            assertTrue(Constants.LINKEDIN_SHARE_BASE_URL.startsWith("https://"))
            assertTrue(Constants.API_BASE_URL.startsWith("https://"))
        }
    }

    @Nested
    @DisplayName("Resources")
    inner class ResourcesTest {

        @Test
        @DisplayName("BLESSING_AUDIO_PATH should be valid resource path")
        fun blessingAudioPathIsValid() {
            val path = Constants.BLESSING_AUDIO_PATH
            assertNotNull(path)
            assertTrue(path.startsWith("/"))
            assertTrue(path.endsWith(".wav"))
        }

        @Test
        @DisplayName("PLUGIN_ICON_PATH should be valid SVG resource path")
        fun pluginIconPathIsValid() {
            val path = Constants.PLUGIN_ICON_PATH
            assertNotNull(path)
            assertTrue(path.startsWith("/"))
            assertTrue(path.endsWith(".svg"))
            assertTrue(path.contains("icon"))
        }
    }

    @Nested
    @DisplayName("Default Settings")
    inner class DefaultSettingsTest {

        @Test
        @DisplayName("DEFAULT_LANGUAGE should be non-empty")
        fun defaultLanguageIsNonEmpty() {
            assertTrue(Constants.DEFAULT_LANGUAGE.isNotBlank())
        }

        @Test
        @DisplayName("DEFAULT_DELAY_SECONDS should be valid numeric string")
        fun defaultDelaySecondsIsValidNumericString() {
            val delayStr = Constants.DEFAULT_DELAY_SECONDS
            assertNotNull(delayStr)
            val delay = delayStr.toDoubleOrNull()
            assertNotNull(delay)
            assertTrue(delay!! > 0)
        }

        @Test
        @DisplayName("DEFAULT_DELAY_MILLIS should be positive")
        fun defaultDelayMillisIsPositive() {
            val delay = Constants.DEFAULT_DELAY_MILLIS
            assertTrue(delay > 0)
            assertEquals(1500L, delay)
        }

        @Test
        @DisplayName("DEFAULT_DELAY_MILLIS should match DEFAULT_DELAY_SECONDS")
        fun defaultDelayMillisMatchesSeconds() {
            val seconds = Constants.DEFAULT_DELAY_SECONDS.toDouble()
            val millis = Constants.DEFAULT_DELAY_MILLIS
            assertEquals((seconds * 1000).toLong(), millis)
        }

        @Test
        @DisplayName("DEFAULT_SOUND_ENABLED should be defined")
        fun defaultSoundEnabledIsDefined() {
            // Just verify it's accessible and is a boolean
            assertFalse(Constants.DEFAULT_SOUND_ENABLED)
        }

        @Test
        @DisplayName("DEFAULT_SHOW_ON_STARTUP should be defined")
        fun defaultShowOnStartupIsDefined() {
            // Just verify it's accessible and is a boolean
            assertTrue(Constants.DEFAULT_SHOW_ON_STARTUP)
        }
    }

    @Nested
    @DisplayName("Display Configuration")
    inner class DisplayConfigurationTest {

        @Test
        @DisplayName("PHRASES_PER_DISPLAY should be positive")
        fun phrasesPerDisplayIsPositive() {
            val count = Constants.PHRASES_PER_DISPLAY
            assertTrue(count > 0)
            assertTrue(count <= 20) // Reasonable upper limit
        }

        @Test
        @DisplayName("STARTUP_DISPLAY_SECONDS should be positive and reasonable")
        fun startupDisplaySecondsIsPositiveAndReasonable() {
            val seconds = Constants.STARTUP_DISPLAY_SECONDS
            assertTrue(seconds > 0)
            assertTrue(seconds <= 60) // Should not exceed 1 minute
        }
    }

    @Nested
    @DisplayName("Rating Prompt Timing")
    inner class RatingPromptTimingTest {

        @Test
        @DisplayName("FIRST_RATING_DELAY_MS should be positive")
        fun firstRatingDelayIsPositive() {
            assertTrue(Constants.FIRST_RATING_DELAY_MS > 0)
        }

        @Test
        @DisplayName("FIRST_RATING_DELAY_MS should be 2 minutes in milliseconds")
        fun firstRatingDelayIsTwoMinutes() {
            val expectedMs = 2 * 60 * 1000L
            assertEquals(expectedMs, Constants.FIRST_RATING_DELAY_MS)
        }

        @Test
        @DisplayName("SECOND_RATING_DELAY_MS should be positive")
        fun secondRatingDelayIsPositive() {
            assertTrue(Constants.SECOND_RATING_DELAY_MS > 0)
        }

        @Test
        @DisplayName("SECOND_RATING_DELAY_MS should be 2 days in milliseconds")
        fun secondRatingDelayIsTwoDays() {
            val expectedMs = 2 * 24 * 60 * 60 * 1000L
            assertEquals(expectedMs, Constants.SECOND_RATING_DELAY_MS)
        }

        @Test
        @DisplayName("REMIND_LATER_DELAY_MS should be positive")
        fun remindLaterDelayIsPositive() {
            assertTrue(Constants.REMIND_LATER_DELAY_MS > 0)
        }

        @Test
        @DisplayName("REMIND_LATER_DELAY_MS should be 10 minutes in milliseconds")
        fun remindLaterDelayIsTenMinutes() {
            val expectedMs = 10 * 60 * 1000L
            assertEquals(expectedMs, Constants.REMIND_LATER_DELAY_MS)
        }

        @Test
        @DisplayName("Rating delays should be in ascending order")
        fun ratingDelaysInAscendingOrder() {
            assertTrue(Constants.FIRST_RATING_DELAY_MS < Constants.REMIND_LATER_DELAY_MS)
            assertTrue(Constants.REMIND_LATER_DELAY_MS < Constants.SECOND_RATING_DELAY_MS)
        }
    }

    @Nested
    @DisplayName("Rating States")
    inner class RatingStatesTest {

        @Test
        @DisplayName("RATING_STATE_FIRST should be defined")
        fun ratingStateFirstIsDefined() {
            assertEquals("1", Constants.RATING_STATE_FIRST)
        }

        @Test
        @DisplayName("RATING_STATE_SECOND should be defined")
        fun ratingStateSecondIsDefined() {
            assertEquals("2", Constants.RATING_STATE_SECOND)
        }

        @Test
        @DisplayName("RATING_STATE_DONE should be defined")
        fun ratingStateDoneIsDefined() {
            assertEquals("3", Constants.RATING_STATE_DONE)
        }

        @Test
        @DisplayName("Rating states should be distinct")
        fun ratingStatesAreDistinct() {
            val states = setOf(
                Constants.RATING_STATE_FIRST,
                Constants.RATING_STATE_SECOND,
                Constants.RATING_STATE_DONE
            )
            assertEquals(3, states.size)
        }

        @Test
        @DisplayName("Rating states should be in sequence")
        fun ratingStatesAreInSequence() {
            assertTrue(Constants.RATING_STATE_FIRST.toInt() < Constants.RATING_STATE_SECOND.toInt())
            assertTrue(Constants.RATING_STATE_SECOND.toInt() < Constants.RATING_STATE_DONE.toInt())
        }
    }

    @Nested
    @DisplayName("Notification")
    inner class NotificationTest {

        @Test
        @DisplayName("NOTIFICATION_GROUP_ID should be non-empty")
        fun notificationGroupIdIsNonEmpty() {
            val groupId = Constants.NOTIFICATION_GROUP_ID
            assertNotNull(groupId)
            assertTrue(groupId.isNotBlank())
        }

        @Test
        @DisplayName("NOTIFICATION_GROUP_ID should be a valid identifier")
        fun notificationGroupIdIsValidIdentifier() {
            val groupId = Constants.NOTIFICATION_GROUP_ID
            // Should contain only alphanumeric characters (and optionally underscores)
            assertTrue(groupId.matches(Regex("^[A-Za-z][A-Za-z0-9]*$")))
        }
    }

    @Nested
    @DisplayName("Cross-Constant Consistency")
    inner class CrossConstantConsistencyTest {

        @Test
        @DisplayName("Plugin ID should appear in relevant URLs")
        fun pluginIdInUrls() {
            // The plugin name should be part of the marketplace URL slug
            assertTrue(
                Constants.PLUGIN_MARKETPLACE_URL.lowercase().contains("astagfirullah"),
                "Plugin marketplace URL should contain plugin name"
            )
        }

        @Test
        @DisplayName("API base URL and endpoints should form valid URLs")
        fun apiUrlsAreValid() {
            val praiseUrl = Constants.API_BASE_URL + Constants.API_PRAISE_LIST_ENDPOINT
            val settingsUrl = Constants.API_BASE_URL + Constants.API_SETTINGS_ENDPOINT

            assertDoesNotThrow { URI(praiseUrl) }
            assertDoesNotThrow { URI(settingsUrl) }
        }
    }
}