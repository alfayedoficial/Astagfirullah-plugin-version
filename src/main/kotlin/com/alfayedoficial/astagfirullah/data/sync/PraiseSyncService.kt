package com.alfayedoficial.astagfirullah.data.sync

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.api.ApiResult
import com.alfayedoficial.astagfirullah.data.api.PraiseApiService
import com.alfayedoficial.astagfirullah.data.cache.PraiseCacheService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service responsible for synchronizing praises from the API.
 * Handles daily sync, version checking, and cache updates.
 */
@Service(Service.Level.APP)
class PraiseSyncService {

    private val logger = Logger.getInstance(PraiseSyncService::class.java)
    private val isSyncing = AtomicBoolean(false)

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
     * Performs sync if needed (once per day).
     * Returns the result of the sync operation.
     *
     * @param forceSync Force sync even if already synced today
     * @return SyncResult indicating the outcome
     */
    suspend fun syncIfNeeded(forceSync: Boolean = false): SyncResult {
        val cacheService = PraiseCacheService.getInstance()

        // Check if sync is needed
        if (!forceSync && !cacheService.needsSync()) {
            logger.debug("Sync not needed - already synced today")
            return SyncResult.AlreadyUpToDate(cacheService.getCurrentVersion())
        }

        // Prevent concurrent syncs
        if (!isSyncing.compareAndSet(false, true)) {
            logger.debug("Sync already in progress")
            return SyncResult.AlreadyUpToDate(cacheService.getCurrentVersion())
        }

        return try {
            performSync(cacheService)
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
     */
    private suspend fun performSync(cacheService: PraiseCacheService): SyncResult {
        return withContext(Dispatchers.IO) {
            val currentVersion = cacheService.getCurrentVersion()
            logger.debug("Starting sync with local version: $currentVersion")

            when (val result = PraiseApiService.fetchPraises(currentVersion)) {
                is ApiResult.Success -> {
                    val apiData = result.response.data
                    if (apiData == null) {
                        logger.warn("API returned null data")
                        return@withContext SyncResult.Error("No data received")
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
                    logger.warn("Sync failed: ${result.message}")
                    SyncResult.Error(result.message)
                }
            }
        }
    }

    /**
     * Checks if a sync is currently in progress.
     */
    fun isSyncInProgress(): Boolean = isSyncing.get()

    /**
     * Gets the current cached version.
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
}
