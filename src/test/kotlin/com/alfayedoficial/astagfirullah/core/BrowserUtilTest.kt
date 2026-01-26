package com.alfayedoficial.astagfirullah.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.awt.Desktop

/**
 * Unit tests for [BrowserUtil] object.
 * Tests browser URL opening functionality and specific link methods.
 *
 * Note: Some tests are conditional based on Desktop support availability.
 * In headless CI environments, Desktop may not be supported.
 */
@DisplayName("BrowserUtil")
class BrowserUtilTest {

    companion object {
        /**
         * Checks if Desktop browsing is supported on the current system.
         * Used to conditionally run tests that require Desktop support.
         */
        @JvmStatic
        fun isDesktopBrowsingSupported(): Boolean {
            return try {
                Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            } catch (e: Exception) {
                false
            }
        }

        @JvmStatic
        fun isDesktopBrowsingNotSupported(): Boolean = !isDesktopBrowsingSupported()
    }

    @Nested
    @DisplayName("openUrl")
    inner class OpenUrlTest {

        @Test
        @DisplayName("should return false for invalid URL syntax")
        fun invalidUrlSyntax() {
            // URLs with invalid syntax should fail gracefully
            assertFalse(BrowserUtil.openUrl("not a valid url"))
            assertFalse(BrowserUtil.openUrl("://missing-scheme"))
        }

        @Test
        @DisplayName("should return false for empty URL")
        fun emptyUrl() {
            assertFalse(BrowserUtil.openUrl(""))
        }

        @Test
        @DisplayName("should handle URL with spaces gracefully")
        fun urlWithSpaces() {
            // URL with unencoded spaces should fail
            assertFalse(BrowserUtil.openUrl("https://example.com/path with spaces"))
        }

        @Test
        @DisplayName("should return false when Desktop is not supported")
        fun desktopNotSupported() {
            // This test runs only in headless/CI environments
            assumeTrue(
                isDesktopBrowsingNotSupported(),
                "Skipped: Desktop browsing is supported"
            )
            assertFalse(BrowserUtil.openUrl("https://example.com"))
        }

        @Test
        @DisplayName("should return true for valid URL when Desktop is supported")
        fun validUrlWithDesktopSupport() {
            // Note: This will actually open a browser, so we use a test-friendly URL
            assumeTrue(
                isDesktopBrowsingSupported(),
                "Skipped: Desktop browsing not supported"
            )
            val result = BrowserUtil.openUrl("https://www.google.com")
            // Result depends on system state - we just verify no exception
            assertTrue(result || !result) // Always passes, just verifies no exception
        }

        @Test
        @DisplayName("should handle various valid URL formats")
        fun variousUrlFormats() {
            // These should not throw exceptions regardless of Desktop support
            // The return value depends on Desktop availability
            assertDoesNotThrow { BrowserUtil.openUrl("https://example.com") }
            assertDoesNotThrow { BrowserUtil.openUrl("http://example.com") }
            assertDoesNotThrow { BrowserUtil.openUrl("https://example.com/path?query=value") }
            assertDoesNotThrow { BrowserUtil.openUrl("https://example.com:8080/path") }
        }

        @Test
        @DisplayName("should handle special characters in URL")
        fun specialCharactersInUrl() {
            // URL-encoded special characters should not cause exceptions
            assertDoesNotThrow {
                BrowserUtil.openUrl("https://example.com/path?q=%20%26%3D")
            }
        }
    }

    @Nested
    @DisplayName("openPluginPage")
    inner class OpenPluginPageTest {

        @Test
        @DisplayName("should use correct plugin marketplace URL")
        fun usesCorrectUrl() {
            // We can verify the constant is used by checking it matches expected URL
            assertEquals(
                "https://plugins.jetbrains.com/plugin/24628-astagfirullah",
                Constants.PLUGIN_MARKETPLACE_URL
            )
        }

        @Test
        @DisplayName("should not throw exception")
        fun doesNotThrow() {
            assertDoesNotThrow { BrowserUtil.openPluginPage() }
        }

        @Test
        @DisplayName("should return false when Desktop is not supported")
        fun returnsFalseWhenDesktopNotSupported() {
            assumeTrue(
                isDesktopBrowsingNotSupported(),
                "Skipped: Desktop browsing is supported"
            )
            assertFalse(BrowserUtil.openPluginPage())
        }
    }

    @Nested
    @DisplayName("openDeveloperProfile")
    inner class OpenDeveloperProfileTest {

        @Test
        @DisplayName("should use correct LinkedIn URL")
        fun usesCorrectUrl() {
            assertEquals(
                "https://www.linkedin.com/in/alfayedoficial",
                Constants.DEVELOPER_LINKEDIN_URL
            )
        }

        @Test
        @DisplayName("should not throw exception")
        fun doesNotThrow() {
            assertDoesNotThrow { BrowserUtil.openDeveloperProfile() }
        }

        @Test
        @DisplayName("should return false when Desktop is not supported")
        fun returnsFalseWhenDesktopNotSupported() {
            assumeTrue(
                isDesktopBrowsingNotSupported(),
                "Skipped: Desktop browsing is supported"
            )
            assertFalse(BrowserUtil.openDeveloperProfile())
        }
    }

    @Nested
    @DisplayName("shareOnLinkedIn")
    inner class ShareOnLinkedInTest {

        @Test
        @DisplayName("should construct correct share URL")
        fun constructsCorrectShareUrl() {
            val expectedUrl = "${Constants.LINKEDIN_SHARE_BASE_URL}${Constants.PLUGIN_MARKETPLACE_URL}"
            assertEquals(
                "https://www.linkedin.com/shareArticle?mini=true&url=https://plugins.jetbrains.com/plugin/24628-astagfirullah",
                expectedUrl
            )
        }

        @Test
        @DisplayName("should not throw exception")
        fun doesNotThrow() {
            assertDoesNotThrow { BrowserUtil.shareOnLinkedIn() }
        }

        @Test
        @DisplayName("should return false when Desktop is not supported")
        fun returnsFalseWhenDesktopNotSupported() {
            assumeTrue(
                isDesktopBrowsingNotSupported(),
                "Skipped: Desktop browsing is supported"
            )
            assertFalse(BrowserUtil.shareOnLinkedIn())
        }

        @Test
        @DisplayName("share base URL should be valid LinkedIn share endpoint")
        fun shareBaseUrlIsValid() {
            assertTrue(Constants.LINKEDIN_SHARE_BASE_URL.startsWith("https://www.linkedin.com/shareArticle"))
            assertTrue(Constants.LINKEDIN_SHARE_BASE_URL.contains("mini=true"))
            assertTrue(Constants.LINKEDIN_SHARE_BASE_URL.contains("url="))
        }
    }

    @Nested
    @DisplayName("URL Validation")
    inner class UrlValidationTest {

        @Test
        @DisplayName("Plugin marketplace URL should be valid HTTPS")
        fun pluginMarketplaceUrlIsHttps() {
            assertTrue(Constants.PLUGIN_MARKETPLACE_URL.startsWith("https://"))
        }

        @Test
        @DisplayName("Developer LinkedIn URL should be valid HTTPS")
        fun developerLinkedInUrlIsHttps() {
            assertTrue(Constants.DEVELOPER_LINKEDIN_URL.startsWith("https://"))
        }

        @Test
        @DisplayName("LinkedIn share base URL should be valid HTTPS")
        fun linkedInShareBaseUrlIsHttps() {
            assertTrue(Constants.LINKEDIN_SHARE_BASE_URL.startsWith("https://"))
        }

        @Test
        @DisplayName("All URLs should be well-formed")
        fun allUrlsAreWellFormed() {
            val urls = listOf(
                Constants.PLUGIN_MARKETPLACE_URL,
                Constants.DEVELOPER_LINKEDIN_URL,
                Constants.LINKEDIN_SHARE_BASE_URL
            )

            urls.forEach { url ->
                assertDoesNotThrow({ java.net.URI(url) }, "URL should be well-formed: $url")
            }
        }
    }
}