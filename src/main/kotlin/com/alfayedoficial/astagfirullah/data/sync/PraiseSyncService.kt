package com.alfayedoficial.astagfirullah.data.sync

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.api.ApiResult
import com.alfayedoficial.astagfirullah.data.api.PraiseApiService
import com.alfayedoficial.astagfirullah.data.api.SettingsApiService
import com.alfayedoficial.astagfirullah.data.api.SettingsResult
import com.alfayedoficial.astagfirullah.data.cache.PluginUpdateCacheService
import com.alfayedoficial.astagfirullah.data.cache.PraiseCacheService
import com.alfayedoficial.astagfirullah.data.model.SettingsData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service responsible for synchronizing praises and checking for plugin updates.
 * Handles daily sync, version checking, and cache updates.
 *
 * Sync Flow:
 * 1. Fetch settings from server to get latest praise_version and plugin version
 * 2. Compare praise_version with local version
 * 3. If server praise_version > local version, fetch new praises
 * 4. Check for plugin updates and cache the result
 */
@Service(Service.Level.APP)
class PraiseSyncService {

    private val logger = Logger.getInstance(PraiseSyncService::class.java)
    private val isSyncing = AtomicBoolean(false)

    // Callback for plugin update notifications
    var onUpdateAvailable: ((UpdateInfo) -> Unit)? = null

    companion object {
        @JvmStatic
        fun getInstance(): PraiseSyncService {
            return ApplicationManager.getApplication().getService(PraiseSyncService::class.java)
        }
    }

    /**
     * Result of a sync operation
     */
    sealed class SyncResult {
        /** Sync completed successfully, data was updated */
        data class Success(val phraseCount: Int, val version: Int) : SyncResult()

        /** Already up-to-date with server */
        data class AlreadyUpToDate(val version: Int) : SyncResult()

        /** Sync failed with error */
        data class Error(val message: String) : SyncResult()
    }

    /**
     * Information about an available plugin update
     */
    data class UpdateInfo(
        val currentVersion: String,
        val newVersion: String,
        val updateType: String,  // "NORMAL" or "EMERGENCY"
        val updateUrl: String
    )

    /**
     * Performs sync if needed (once per day).
     * This is the main entry point that handles both praise sync and update checks.
     *
     * @param forceSync Force sync even if already synced today
     * @return SyncResult indicating the outcome
     */
    suspend fun syncIfNeeded(forceSync: Boolean = false): SyncResult {
        val praiseCacheService = PraiseCacheService.getInstance()
        val updateCacheService = PluginUpdateCacheService.getInstance()

        // Check if sync is needed
        if (!forceSync && !praiseCacheService.needsSync() && !updateCacheService.needsUpdateCheck()) {
            logger.debug("Sync not needed - already synced today")
            return SyncResult.AlreadyUpToDate(praiseCacheService.getCurrentVersion())
        }

        // Prevent concurrent syncs
        if (!isSyncing.compareAndSet(false, true)) {
            logger.debug("Sync already in progress")
            return SyncResult.AlreadyUpToDate(praiseCacheService.getCurrentVersion())
        }

        return try {
            performSync(praiseCacheService, updateCacheService, forceSync)
        } finally {
            isSyncing.set(false)
        }
    }

    /**
     * Forces a sync regardless of daily check.
     */
    suspend fun forceSync(): SyncResult {
        return syncIfNeeded(forceSync = true)
    }

    /**
     * Performs the actual sync operation.
     * First fetches settings, then syncs praises if needed.
     */
    private suspend fun performSync(
        praiseCacheService: PraiseCacheService,
        updateCacheService: PluginUpdateCacheService,
        forceSync: Boolean
    ): SyncResult {
        return withContext(Dispatchers.IO) {
            logger.debug("Starting sync...")

            // Step 1: Fetch settings from server
            val settingsResult = SettingsApiService.fetchSettings()

            when (settingsResult) {
                is SettingsResult.Success -> {
                    val settings = settingsResult.settings

                    // Step 2: Cache update info and check for plugin updates
                    handlePluginUpdateCheck(settings, updateCacheService)

                    // Step 3: Check if praise sync is needed based on server praise_version
                    val localPraiseVersion = praiseCacheService.getCurrentVersion()
                    val serverPraiseVersion = settings.praiseVersion

                    logger.debug("Praise versions - local: $localPraiseVersion, server: $serverPraiseVersion")

                    if (serverPraiseVersion > localPraiseVersion || forceSync) {
                        // New praises available, fetch them
                        performPraiseSync(praiseCacheService, localPraiseVersion)
                    } else {
                        // Already up-to-date
                        praiseCacheService.markSyncCompleted(localPraiseVersion)
                        logger.debug("Praises already up-to-date with version: $localPraiseVersion")
                        SyncResult.AlreadyUpToDate(localPraiseVersion)
                    }
                }

                is SettingsResult.Error -> {
                    logger.warn("Failed to fetch settings: ${settingsResult.message}")
                    // Fall back to direct praise sync (legacy behavior)
                    performPraiseSync(praiseCacheService, praiseCacheService.getCurrentVersion())
                }
            }
        }
    }

    /**
     * Handles plugin update checking and notification.
     */
    private fun handlePluginUpdateCheck(
        settings: SettingsData,
        updateCacheService: PluginUpdateCacheService
    ) {
        // Update the cache with server info
        updateCacheService.updateCache(
            versionName = settings.versionName,
            versionCode = settings.versionCode,
            updateType = settings.updateType,
            updateUrl = settings.updateUrl,
            praiseVersion = settings.praiseVersion
        )

        // Check if update is available
        val currentVersion = Constants.PLUGIN_VERSION
        val serverVersion = settings.versionName

        if (SettingsApiService.isUpdateAvailable(currentVersion, serverVersion)) {
            logger.debug("Plugin update available: $currentVersion -> $serverVersion")

            // Only notify if not already shown and not dismissed
            if (!updateCacheService.wasNotificationShown() &&
                !updateCacheService.isVersionDismissed(serverVersion)
            ) {
                val updateInfo = UpdateInfo(
                    currentVersion = currentVersion,
                    newVersion = serverVersion,
                    updateType = settings.updateType,
                    updateUrl = settings.updateUrl ?: Constants.PLUGIN_MARKETPLACE_URL
                )

                // Trigger callback for notification
                onUpdateAvailable?.invoke(updateInfo)

                // Mark notification as shown (for NORMAL updates)
                // For EMERGENCY updates, we might want to show again
                if (settings.updateType != "EMERGENCY") {
                    updateCacheService.markNotificationShown()
                }
            }
        } else {
            // Version is up-to-date, clear any dismissed version
            updateCacheService.clearDismissedVersion()
        }
    }

    /**
     * Performs the actual praise sync from API.
     */
    private fun performPraiseSync(
        cacheService: PraiseCacheService,
        currentVersion: Int
    ): SyncResult {
        logger.debug("Fetching praises with local version: $currentVersion")

        return when (val result = PraiseApiService.fetchPraises(currentVersion)) {
            is ApiResult.Success -> {
                val apiData = result.response.data
                if (apiData == null) {
                    logger.warn("API returned null data")
                    return SyncResult.Error("No data received")
                }

                val serverVersion = apiData.version

                if (apiData.praises.isEmpty()) {
                    // Already up-to-date (server returns empty array when version matches)
                    cacheService.markSyncCompleted(serverVersion)
                    logger.debug("Already up-to-date with version: $serverVersion")
                    SyncResult.AlreadyUpToDate(serverVersion)
                } else {
                    // Parse and cache new praises, filtering by category
                    val praises = PraiseApiService.parsePraises(
                        result.response,
                        Constants.API_CATEGORY_FILTER
                    )

                    if (praises.isNotEmpty()) {
                        cacheService.updateCache(praises, serverVersion)
                        logger.debug("Synced ${praises.size} praises, new version: $serverVersion")
                        SyncResult.Success(praises.size, serverVersion)
                    } else {
                        // No praises matched the category filter
                        cacheService.markSyncCompleted(serverVersion)
                        logger.debug("No praises matched category filter, version: $serverVersion")
                        SyncResult.AlreadyUpToDate(serverVersion)
                    }
                }
            }

            is ApiResult.Error -> {
                logger.warn("Praise sync failed: ${result.message}")
                SyncResult.Error(result.message)
            }
        }
    }

    /**
     * Checks if a sync is currently in progress.
     */
    fun isSyncInProgress(): Boolean = isSyncing.get()

    /**
     * Gets the current cached praise version.
     */
    fun getCurrentVersion(): Int {
        return try {
            PraiseCacheService.getInstance().getCurrentVersion()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Checks if there's cached data available.
     */
    fun hasCachedData(): Boolean {
        return try {
            PraiseCacheService.getInstance().hasCachedData()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a plugin update is available.
     */
    fun isPluginUpdateAvailable(): Boolean {
        return try {
            val updateCache = PluginUpdateCacheService.getInstance()
            val serverVersion = updateCache.getLatestVersionName()
            if (serverVersion.isEmpty()) return false

            SettingsApiService.isUpdateAvailable(Constants.PLUGIN_VERSION, serverVersion)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the latest available plugin version from cache.
     */
    fun getLatestPluginVersion(): String {
        return try {
            PluginUpdateCacheService.getInstance().getLatestVersionName()
        } catch (e: Exception) {
            ""
        }
    }
}
