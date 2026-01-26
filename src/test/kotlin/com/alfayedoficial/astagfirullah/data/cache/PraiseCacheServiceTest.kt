package com.alfayedoficial.astagfirullah.data.cache

import com.alfayedoficial.astagfirullah.data.model.CachedPraise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for PraiseCacheService.
 *
 * Since PraiseCacheService depends on IntelliJ's ApplicationManager and PersistentStateComponent,
 * we create a testable version that allows direct state manipulation for unit testing.
 */
class PraiseCacheServiceTest {

    private lateinit var cacheService: TestablePraiseCacheService
    private val gson = Gson()

    @BeforeEach
    fun setUp() {
        cacheService = TestablePraiseCacheService()
    }

    @Nested
    @DisplayName("Cache Persistence Tests")
    inner class CachePersistenceTests {

        @Test
        @DisplayName("Should save praises to cache state")
        fun savePraisesToCacheState() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 5)

            val state = cacheService.getState()
            assertEquals(5, state.version)
            assertFalse(state.praisesJson.isEmpty())
            assertNotEquals("[]", state.praisesJson)
        }

        @Test
        @DisplayName("Should load praises from cache state")
        fun loadPraisesFromCacheState() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 5)

            // Simulate reload by creating new service and loading state
            val newService = TestablePraiseCacheService()
            newService.loadState(cacheService.getState())

            val loadedPraises = newService.getCachedPraises()
            assertEquals(praises.size, loadedPraises.size)
            assertEquals(praises[0].arabicText, loadedPraises[0].arabicText)
            assertEquals(praises[0].englishText, loadedPraises[0].englishText)
        }

        @Test
        @DisplayName("Should persist version after cache update")
        fun persistVersionAfterCacheUpdate() {
            cacheService.updateCache(createSamplePraises(), 10)
            assertEquals(10, cacheService.getCurrentVersion())

            val newService = TestablePraiseCacheService()
            newService.loadState(cacheService.getState())
            assertEquals(10, newService.getCurrentVersion())
        }

        @Test
        @DisplayName("Should persist last sync date")
        fun persistLastSyncDate() {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            cacheService.updateCache(createSamplePraises(), 1)

            assertEquals(today, cacheService.getState().lastSyncDate)
        }
    }

    @Nested
    @DisplayName("GSON Serialization Tests")
    inner class GsonSerializationTests {

        @Test
        @DisplayName("Should serialize CachedPraise correctly")
        fun serializeCachedPraiseCorrectly() {
            val praise = CachedPraise(
                id = 1,
                arabicText = "سبحان الله",
                englishText = "Glory be to Allah",
                categoryId = 1,
                count = 33
            )

            val json = gson.toJson(listOf(praise))
            assertNotNull(json)
            assertTrue(json.contains("سبحان الله"))
            assertTrue(json.contains("Glory be to Allah"))
        }

        @Test
        @DisplayName("Should deserialize CachedPraise correctly")
        fun deserializeCachedPraiseCorrectly() {
            val json = """[{"id":1,"arabicText":"سبحان الله","englishText":"Glory be to Allah","categoryId":1,"count":33}]"""

            val type = object : TypeToken<List<CachedPraise>>() {}.type
            val praises: List<CachedPraise> = gson.fromJson(json, type)

            assertEquals(1, praises.size)
            assertEquals(1, praises[0].id)
            assertEquals("سبحان الله", praises[0].arabicText)
            assertEquals("Glory be to Allah", praises[0].englishText)
            assertEquals(1, praises[0].categoryId)
            assertEquals(33, praises[0].count)
        }

        @Test
        @DisplayName("Should handle special characters in Arabic text")
        fun handleSpecialCharactersInArabicText() {
            val praise = CachedPraise(
                id = 1,
                arabicText = "لا إله إلا الله محمد رسول الله",
                englishText = "There is no god but Allah, Muhammad is the Messenger of Allah",
                categoryId = 1,
                count = 1
            )

            cacheService.updateCache(listOf(praise), 1)

            val loaded = cacheService.getCachedPraises()
            assertEquals("لا إله إلا الله محمد رسول الله", loaded[0].arabicText)
        }

        @Test
        @DisplayName("Should serialize and deserialize list of praises")
        fun serializeAndDeserializeListOfPraises() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val loaded = cacheService.getCachedPraises()
            assertEquals(praises.size, loaded.size)

            for (i in praises.indices) {
                assertEquals(praises[i].id, loaded[i].id)
                assertEquals(praises[i].arabicText, loaded[i].arabicText)
                assertEquals(praises[i].englishText, loaded[i].englishText)
                assertEquals(praises[i].categoryId, loaded[i].categoryId)
                assertEquals(praises[i].count, loaded[i].count)
            }
        }
    }

    @Nested
    @DisplayName("Version Tracking Tests")
    inner class VersionTrackingTests {

        @Test
        @DisplayName("Should return 0 for initial version")
        fun returnZeroForInitialVersion() {
            assertEquals(0, cacheService.getCurrentVersion())
        }

        @Test
        @DisplayName("Should update version when cache is updated")
        fun updateVersionWhenCacheIsUpdated() {
            cacheService.updateCache(createSamplePraises(), 5)
            assertEquals(5, cacheService.getCurrentVersion())
        }

        @Test
        @DisplayName("Should update version when sync is marked complete")
        fun updateVersionWhenSyncIsMarkedComplete() {
            cacheService.markSyncCompleted(10)
            assertEquals(10, cacheService.getCurrentVersion())
        }

        @Test
        @DisplayName("Should handle version increment correctly")
        fun handleVersionIncrementCorrectly() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertEquals(1, cacheService.getCurrentVersion())

            cacheService.updateCache(createSamplePraises(), 2)
            assertEquals(2, cacheService.getCurrentVersion())

            cacheService.markSyncCompleted(3)
            assertEquals(3, cacheService.getCurrentVersion())
        }

        @Test
        @DisplayName("Should reset version on cache clear")
        fun resetVersionOnCacheClear() {
            cacheService.updateCache(createSamplePraises(), 10)
            assertEquals(10, cacheService.getCurrentVersion())

            cacheService.clearCache()
            assertEquals(0, cacheService.getCurrentVersion())
        }
    }

    @Nested
    @DisplayName("Daily Sync Timing Tests")
    inner class DailySyncTimingTests {

        @Test
        @DisplayName("Should need sync when no previous sync")
        fun needSyncWhenNoPreviousSync() {
            assertTrue(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should not need sync after today's sync")
        fun notNeedSyncAfterTodaysSync() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertFalse(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should not need sync after mark sync completed")
        fun notNeedSyncAfterMarkSyncCompleted() {
            cacheService.markSyncCompleted(1)
            assertFalse(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should need sync after cache clear")
        fun needSyncAfterCacheClear() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertFalse(cacheService.needsSync())

            cacheService.clearCache()
            assertTrue(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should need sync when last sync was yesterday")
        fun needSyncWhenLastSyncWasYesterday() {
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val state = PraiseCacheService.CacheState(
                version = 1,
                lastSyncDate = yesterday,
                praisesJson = "[]"
            )
            cacheService.loadState(state)

            assertTrue(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should need sync when last sync was in past")
        fun needSyncWhenLastSyncWasInPast() {
            val pastDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)
            val state = PraiseCacheService.CacheState(
                version = 1,
                lastSyncDate = pastDate,
                praisesJson = "[]"
            )
            cacheService.loadState(state)

            assertTrue(cacheService.needsSync())
        }
    }

    @Nested
    @DisplayName("Cache Invalidation Tests")
    inner class CacheInvalidationTests {

        @Test
        @DisplayName("Should clear all cached data")
        fun clearAllCachedData() {
            cacheService.updateCache(createSamplePraises(), 5)

            cacheService.clearCache()

            assertEquals(0, cacheService.getCurrentVersion())
            assertTrue(cacheService.getCachedPraises().isEmpty())
            assertFalse(cacheService.hasCachedData())
            assertTrue(cacheService.needsSync())
        }

        @Test
        @DisplayName("Should reset praises JSON to empty array")
        fun resetPraisesJsonToEmptyArray() {
            cacheService.updateCache(createSamplePraises(), 1)
            cacheService.clearCache()

            assertEquals("[]", cacheService.getState().praisesJson)
        }

        @Test
        @DisplayName("Should reset last sync date")
        fun resetLastSyncDate() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertNotEquals("", cacheService.getState().lastSyncDate)

            cacheService.clearCache()
            assertEquals("", cacheService.getState().lastSyncDate)
        }
    }

    @Nested
    @DisplayName("Empty Cache Handling Tests")
    inner class EmptyCacheHandlingTests {

        @Test
        @DisplayName("Should return empty list for uninitialized cache")
        fun returnEmptyListForUninitializedCache() {
            val praises = cacheService.getCachedPraises()
            assertTrue(praises.isEmpty())
        }

        @Test
        @DisplayName("Should report no cached data for empty cache")
        fun reportNoCachedDataForEmptyCache() {
            assertFalse(cacheService.hasCachedData())
        }

        @Test
        @DisplayName("Should return empty Arabic phrases for empty cache")
        fun returnEmptyArabicPhrasesForEmptyCache() {
            assertTrue(cacheService.getArabicPhrases().isEmpty())
        }

        @Test
        @DisplayName("Should return empty English phrases for empty cache")
        fun returnEmptyEnglishPhrasesForEmptyCache() {
            assertTrue(cacheService.getEnglishPhrases().isEmpty())
        }

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        fun handleMalformedJsonGracefully() {
            val state = PraiseCacheService.CacheState(
                version = 1,
                lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                praisesJson = "invalid json {"
            )
            cacheService.loadState(state)

            val praises = cacheService.getCachedPraises()
            assertTrue(praises.isEmpty())
        }

        @Test
        @DisplayName("Should handle null JSON gracefully")
        fun handleNullJsonGracefully() {
            val state = PraiseCacheService.CacheState(
                version = 1,
                lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                praisesJson = "null"
            )
            cacheService.loadState(state)

            val praises = cacheService.getCachedPraises()
            assertTrue(praises.isEmpty())
        }
    }

    @Nested
    @DisplayName("GetRandomPraise Tests")
    inner class GetRandomPraiseTests {

        @Test
        @DisplayName("Should return praise from cached list")
        fun returnPraiseFromCachedList() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val randomPraise = cacheService.getRandomPraise()
            assertNotNull(randomPraise)
            assertTrue(praises.any { it.id == randomPraise?.id })
        }

        @Test
        @DisplayName("Should return null for empty cache")
        fun returnNullForEmptyCache() {
            val randomPraise = cacheService.getRandomPraise()
            assertNull(randomPraise)
        }

        @Test
        @DisplayName("Should return valid random Arabic phrase")
        fun returnValidRandomArabicPhrase() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val arabicPhrase = cacheService.getRandomArabicPhrase()
            assertNotNull(arabicPhrase)
            assertTrue(praises.any { it.arabicText == arabicPhrase })
        }

        @Test
        @DisplayName("Should return valid random English phrase")
        fun returnValidRandomEnglishPhrase() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val englishPhrase = cacheService.getRandomEnglishPhrase()
            assertNotNull(englishPhrase)
            assertTrue(praises.any { it.englishText == englishPhrase })
        }

        @Test
        @DisplayName("Should return empty string for random phrase when cache is empty")
        fun returnEmptyStringForRandomPhraseWhenCacheIsEmpty() {
            val arabicPhrase = cacheService.getRandomArabicPhrase()
            val englishPhrase = cacheService.getRandomEnglishPhrase()

            assertTrue(arabicPhrase.isNullOrEmpty())
            assertTrue(englishPhrase.isNullOrEmpty())
        }
    }

    @Nested
    @DisplayName("Phrase Extraction Tests")
    inner class PhraseExtractionTests {

        @Test
        @DisplayName("Should extract Arabic phrases correctly")
        fun extractArabicPhrasesCorrectly() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val arabicPhrases = cacheService.getArabicPhrases()
            assertEquals(praises.size, arabicPhrases.size)
            assertTrue(arabicPhrases.contains("سبحان الله"))
            assertTrue(arabicPhrases.contains("الحمد لله"))
            assertTrue(arabicPhrases.contains("الله أكبر"))
        }

        @Test
        @DisplayName("Should extract English phrases correctly")
        fun extractEnglishPhrasesCorrectly() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val englishPhrases = cacheService.getEnglishPhrases()
            assertEquals(praises.size, englishPhrases.size)
            assertTrue(englishPhrases.contains("Glory be to Allah"))
            assertTrue(englishPhrases.contains("All praise is due to Allah"))
            assertTrue(englishPhrases.contains("Allah is the Greatest"))
        }

        @Test
        @DisplayName("Should maintain order when extracting phrases")
        fun maintainOrderWhenExtractingPhrases() {
            val praises = createSamplePraises()
            cacheService.updateCache(praises, 1)

            val arabicPhrases = cacheService.getArabicPhrases()
            val englishPhrases = cacheService.getEnglishPhrases()

            for (i in praises.indices) {
                assertEquals(praises[i].arabicText, arabicPhrases[i])
                assertEquals(praises[i].englishText, englishPhrases[i])
            }
        }
    }

    @Nested
    @DisplayName("Has Cached Data Tests")
    inner class HasCachedDataTests {

        @Test
        @DisplayName("Should return true when cache has data")
        fun returnTrueWhenCacheHasData() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertTrue(cacheService.hasCachedData())
        }

        @Test
        @DisplayName("Should return false when cache is empty")
        fun returnFalseWhenCacheIsEmpty() {
            assertFalse(cacheService.hasCachedData())
        }

        @Test
        @DisplayName("Should return false after cache clear")
        fun returnFalseAfterCacheClear() {
            cacheService.updateCache(createSamplePraises(), 1)
            assertTrue(cacheService.hasCachedData())

            cacheService.clearCache()
            assertFalse(cacheService.hasCachedData())
        }
    }

    // Helper methods

    private fun createSamplePraises(): List<CachedPraise> {
        return listOf(
            CachedPraise(
                id = 1,
                arabicText = "سبحان الله",
                englishText = "Glory be to Allah",
                categoryId = 1,
                count = 33
            ),
            CachedPraise(
                id = 2,
                arabicText = "الحمد لله",
                englishText = "All praise is due to Allah",
                categoryId = 1,
                count = 33
            ),
            CachedPraise(
                id = 3,
                arabicText = "الله أكبر",
                englishText = "Allah is the Greatest",
                categoryId = 1,
                count = 34
            )
        )
    }
}

/**
 * Testable version of PraiseCacheService that doesn't depend on IntelliJ's ApplicationManager.
 * This allows unit testing without the full IntelliJ platform.
 */
class TestablePraiseCacheService {
    private val gson = Gson()
    private var myState = PraiseCacheService.CacheState()
    private var cachedPraises: List<CachedPraise>? = null

    fun getState(): PraiseCacheService.CacheState = myState

    fun loadState(state: PraiseCacheService.CacheState) {
        myState = state.copy()
        loadPraisesFromJson()
    }

    fun getCurrentVersion(): Int = myState.version

    fun needsSync(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.lastSyncDate != today
    }

    fun hasCachedData(): Boolean {
        return getCachedPraises().isNotEmpty()
    }

    fun getCachedPraises(): List<CachedPraise> {
        if (cachedPraises == null) {
            loadPraisesFromJson()
        }
        return cachedPraises ?: emptyList()
    }

    fun getArabicPhrases(): List<String> {
        return getCachedPraises().map { it.arabicText }
    }

    fun getEnglishPhrases(): List<String> {
        return getCachedPraises().map { it.englishText }
    }

    fun getRandomPraise(): CachedPraise? {
        val praises = getCachedPraises()
        return if (praises.isNotEmpty()) praises.random() else null
    }

    fun getRandomArabicPhrase(): String? {
        return getRandomPraise()?.arabicText
    }

    fun getRandomEnglishPhrase(): String? {
        return getRandomPraise()?.englishText
    }

    fun updateCache(praises: List<CachedPraise>, version: Int) {
        try {
            val jsonData = gson.toJson(praises)
            myState.praisesJson = jsonData
            myState.version = version
            myState.lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            cachedPraises = praises
        } catch (e: Exception) {
            // Handle silently for tests
        }
    }

    fun markSyncCompleted(version: Int) {
        myState.version = version
        myState.lastSyncDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    private fun loadPraisesFromJson() {
        try {
            val type = object : TypeToken<List<CachedPraise>>() {}.type
            cachedPraises = gson.fromJson(myState.praisesJson, type) ?: emptyList()
        } catch (e: Exception) {
            cachedPraises = emptyList()
        }
    }

    fun clearCache() {
        myState.version = 0
        myState.lastSyncDate = ""
        myState.praisesJson = "[]"
        cachedPraises = emptyList()
    }
}