package com.alfayedoficial.astagfirullah.data.cache

import com.alfayedoficial.astagfirullah.data.model.CachedPraise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
 * Service for caching praises locally.
 * Provides offline access to praises and handles sync state.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahPraiseCache",
    storages = [Storage("astagfirullah-cache.xml")]
)
class PraiseCacheService : PersistentStateComponent<PraiseCacheService.CacheState> {

    private val logger = Logger.getInstance(PraiseCacheService::class.java)
    private val gson = Gson()
    private var myState = CacheState()

    // In-memory cache for quick access
    private var cachedPraises: List<CachedPraise>? = null

    companion object {
        @JvmStatic
        fun getInstance(): PraiseCacheService {
            return ApplicationManager.getApplication().getService(PraiseCacheService::class.java)
        }
    }

    /**
     * Cache state persisted to disk
     */
    data class CacheState(
        var version: Int = 0,
        var lastSyncDate: String = "",
        var praisesJson: String = "[]"
    )

    override fun getState(): CacheState = myState

    override fun loadState(state: CacheState) {
        XmlSerializerUtil.copyBean(state, myState)
        // Load praises into memory cache
        loadPraisesFromJson()
    }

    /**
     * Gets the current cached version number.
     */
    fun getCurrentVersion(): Int = myState.version

    /**
     * Checks if sync is needed today.
     * Returns true if last sync was not today.
     */
    fun needsSync(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.lastSyncDate != today
    }

    /**
     * Checks if we have any cached data.
     */
    fun hasCachedData(): Boolean {
        return getCachedPraises().isNotEmpty()
    }

    /**
     * Gets all cached praises.
     */
    fun getCachedPraises(): List<CachedPraise> {
        if (cachedPraises == null) {
            loadPraisesFromJson()
        }
        return cachedPraises ?: emptyList()
    }

    /**
     * Gets Arabic phrases from cache.
     */
    fun getArabicPhrases(): List<String> {
        return getCachedPraises().map { it.arabicText }
    }

    /**
     * Gets English phrases from cache.
     */
    fun getEnglishPhrases(): List<String> {
        return getCachedPraises().map { it.englishText }
    }

    /**
     * Updates the cache with new praises from API.
     *
     * @param praises List of praises to cache
     * @param version The API version number
     */
    fun updateCache(praises: List<CachedPraise>, version: Int) {
        try {
            val jsonData = gson.toJson(praises)
            myState.praisesJson = jsonData
            myState.version = version
            myState.lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            // Update in-memory cache
            cachedPraises = praises

            logger.debug("Cache updated: ${praises.size} praises, version=$version")
        } catch (e: Exception) {
            logger.error("Failed to update cache: ${e.message}", e)
        }
    }

    /**
     * Marks sync as completed for today without updating data.
     * Used when API returns empty (already up-to-date).
     */
    fun markSyncCompleted(version: Int) {
        myState.version = version
        myState.lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        logger.debug("Sync marked complete, version=$version")
    }

    /**
     * Loads praises from JSON state into memory.
     */
    private fun loadPraisesFromJson() {
        try {
            val type = object : TypeToken<List<CachedPraise>>() {}.type
            cachedPraises = gson.fromJson(myState.praisesJson, type) ?: emptyList()
            logger.debug("Loaded ${cachedPraises?.size ?: 0} praises from cache")
        } catch (e: Exception) {
            logger.error("Failed to load praises from cache", e)
            cachedPraises = emptyList()
        }
    }

    /**
     * Clears the cache (for debugging/testing).
     */
    fun clearCache() {
        myState.version = 0
        myState.lastSyncDate = ""
        myState.praisesJson = "[]"
        cachedPraises = emptyList()
        logger.debug("Cache cleared")
    }
}
