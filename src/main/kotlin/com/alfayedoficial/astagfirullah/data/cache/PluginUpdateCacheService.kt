package com.alfayedoficial.astagfirullah.data.cache

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Service for caching plugin update information locally.
 * Tracks latest available version and handles update check timing.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahPluginUpdateCache",
    storages = [Storage("astagfirullah-update.xml")]
)
class PluginUpdateCacheService : PersistentStateComponent<PluginUpdateCacheService.UpdateCacheState> {

    private val logger = Logger.getInstance(PluginUpdateCacheService::class.java)
    private var myState = UpdateCacheState()

    companion object {
        @JvmStatic
        fun getInstance(): PluginUpdateCacheService {
            return ApplicationManager.getApplication().getService(PluginUpdateCacheService::class.java)
        }
    }

    /**
     * Update cache state persisted to disk
     */
    data class UpdateCacheState(
        var lastCheckDate: String = "",
        var latestVersionName: String = "",
        var latestVersionCode: Int = 0,
        var updateType: String = "",  // "NORMAL" or "EMERGENCY"
        var updateUrl: String = "",
        var serverPraiseVersion: Int = 0,
        var updateNotificationShown: Boolean = false,  // Track if we've shown notification for this version
        var dismissedVersion: String = ""  // Version that user dismissed (don't show again for this version)
    )

    override fun getState(): UpdateCacheState = myState

    override fun loadState(state: UpdateCacheState) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    /**
     * Checks if update check is needed today.
     * Returns true if last check was not today.
     */
    fun needsUpdateCheck(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.lastCheckDate != today
    }

    /**
     * Gets the latest version name from server.
     */
    fun getLatestVersionName(): String = myState.latestVersionName

    /**
     * Gets the server praise version.
     */
    fun getServerPraiseVersion(): Int = myState.serverPraiseVersion

    /**
     * Gets the update type (NORMAL or EMERGENCY).
     */
    fun getUpdateType(): String = myState.updateType

    /**
     * Gets the update URL.
     */
    fun getUpdateUrl(): String = myState.updateUrl

    /**
     * Checks if notification was already shown for current available update.
     */
    fun wasNotificationShown(): Boolean = myState.updateNotificationShown

    /**
     * Checks if user dismissed this version's update notification.
     */
    fun isVersionDismissed(version: String): Boolean = myState.dismissedVersion == version

    /**
     * Updates the cache with new settings from server.
     *
     * @param versionName Latest version name from server
     * @param versionCode Latest version code from server
     * @param updateType Update type (NORMAL or EMERGENCY)
     * @param updateUrl URL for update
     * @param praiseVersion Server praise database version
     */
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

        logger.debug("Update cache updated: version=$versionName, praiseVersion=$praiseVersion")
    }

    /**
     * Marks update check as completed for today without updating data.
     */
    fun markCheckCompleted() {
        myState.lastCheckDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        logger.debug("Update check marked complete")
    }

    /**
     * Marks that the update notification has been shown.
     */
    fun markNotificationShown() {
        myState.updateNotificationShown = true
        logger.debug("Update notification marked as shown")
    }

    /**
     * Marks the version as dismissed by user.
     */
    fun dismissVersion(version: String) {
        myState.dismissedVersion = version
        myState.updateNotificationShown = true
        logger.debug("Version dismissed: $version")
    }

    /**
     * Clears the dismissed version (e.g., when a newer version is available).
     */
    fun clearDismissedVersion() {
        myState.dismissedVersion = ""
    }

    /**
     * Clears the cache (for debugging/testing).
     */
    fun clearCache() {
        myState.lastCheckDate = ""
        myState.latestVersionName = ""
        myState.latestVersionCode = 0
        myState.updateType = ""
        myState.updateUrl = ""
        myState.serverPraiseVersion = 0
        myState.updateNotificationShown = false
        myState.dismissedVersion = ""
        logger.debug("Update cache cleared")
    }
}