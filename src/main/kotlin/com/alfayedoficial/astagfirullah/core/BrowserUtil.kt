package com.alfayedoficial.astagfirullah.core

import com.intellij.openapi.diagnostic.Logger
import java.awt.Desktop
import java.net.URI

/**
 * Utility object for browser-related operations.
 * Provides a single point for opening URLs in the system browser.
 */
object BrowserUtil {

    private val logger = Logger.getInstance(BrowserUtil::class.java)

    /**
     * Opens a URL in the system's default browser.
     * Handles all error cases gracefully with logging.
     *
     * @param url The URL string to open
     * @return true if the browser was opened successfully, false otherwise
     */
    fun openUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri)
                logger.debug("Opened URL in browser: $url")
                true
            } else {
                logger.warn("Desktop browsing not supported on this system")
                false
            }
        } catch (e: Exception) {
            logger.warn("Failed to open URL: $url", e)
            false
        }
    }

    /**
     * Opens the plugin marketplace page.
     */
    fun openPluginPage(): Boolean = openUrl(Constants.PLUGIN_MARKETPLACE_URL)

    /**
     * Opens the developer's LinkedIn profile.
     */
    fun openDeveloperProfile(): Boolean = openUrl(Constants.DEVELOPER_LINKEDIN_URL)

    /**
     * Opens LinkedIn share dialog for the plugin.
     */
    fun shareOnLinkedIn(): Boolean = openUrl(
        "${Constants.LINKEDIN_SHARE_BASE_URL}${Constants.PLUGIN_MARKETPLACE_URL}"
    )
}
