package com.alfayedoficial.astagfirullah.data.sync

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.api.ApiResult
import com.alfayedoficial.astagfirullah.data.api.PraiseApiService
import com.alfayedoficial.astagfirullah.data.api.SettingsApiService
import com.alfayedoficial.astagfirullah.data.api.SettingsResult
import com.alfayedoficial.astagfirullah.data.cache.PluginUpdateCacheService
import com.alfayedoficial.astagfirullah.data.cache.PraiseCacheService
import com.alfayedoficial.astagfirullah.data.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Comprehensive unit tests for PraiseSyncService.
 * Tests sync flow, concurrent sync prevention, daily check logic,
 * update handling, and error handling scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PraiseSyncServiceTest {

    @Mock
    private lateinit var mockPraiseCacheService: PraiseCacheService

    @Mock
    private lateinit var mockUpdateCacheService: PluginUpdateCacheService

    private lateinit var mockedPraiseCacheStatic: MockedStatic<PraiseCacheService>
    private lateinit var mockedUpdateCacheStatic: MockedStatic<PluginUpdateCacheService>
    private lateinit var mockedSettingsApiStatic: MockedStatic<SettingsApiService>
    private lateinit var mockedPraiseApiStatic: MockedStatic<PraiseApiService>

    private lateinit var syncService: PraiseSyncService

    private lateinit var closeable: AutoCloseable

    @BeforeEach
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)

        // Mock static getInstance() methods
        mockedPraiseCacheStatic = mockStatic(PraiseCacheService::class.java)
        mockedUpdateCacheStatic = mockStatic(PluginUpdateCacheService::class.java)
        mockedSettingsApiStatic = mockStatic(SettingsApiService::class.java)
        mockedPraiseApiStatic = mockStatic(PraiseApiService::class.java)

        mockedPraiseCacheStatic.`when`<PraiseCacheService> { PraiseCacheService.getInstance() }
            .thenReturn(mockPraiseCacheService)
        mockedUpdateCacheStatic.`when`<PluginUpdateCacheService> { PluginUpdateCacheService.getInstance() }
            .thenReturn(mockUpdateCacheService)

        // Create a fresh instance for each test
        syncService = PraiseSyncService()
    }

    @AfterEach
    fun tearDown() {
        mockedPraiseCacheStatic.close()
        mockedUpdateCacheStatic.close()
        mockedSettingsApiStatic.close()
        mockedPraiseApiStatic.close()
        closeable.close()
    }

    // ===========================================
    // 1. SYNC FLOW TESTS
    // ===========================================

    @Nested
    @DisplayName("Sync Flow Tests")
    inner class SyncFlowTests {

        @Test
        @DisplayName("Should return AlreadyUpToDate when sync is not needed")
        fun testSyncNotNeeded() = runTest {
            // Given: no sync needed (already synced today)
            whenever(mockPraiseCacheService.needsSync()).thenReturn(false)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(5)

            // When
            val result = syncService.syncIfNeeded(forceSync = false)

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
            assertEquals(5, (result as PraiseSyncService.SyncResult.AlreadyUpToDate).version)
        }

        @Test
        @DisplayName("Should perform full sync when praise sync is needed")
        fun testFullSyncWhenPraiseSyncNeeded() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            val apiResponse = createApiResponse(version = 2, praiseCount = 5)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(eq(1)) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(createCachedPraises(5))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded(forceSync = false)

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.Success)
            assertEquals(5, (result as PraiseSyncService.SyncResult.Success).phraseCount)
            assertEquals(2, result.version)
        }

        @Test
        @DisplayName("Should perform sync when update check is needed")
        fun testSyncWhenUpdateCheckNeeded() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(false)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(true)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(3)

            val settingsData = createSettingsData(praiseVersion = 3, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded(forceSync = false)

            // Then: Should mark sync completed since versions match
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
            verify(mockPraiseCacheService).markSyncCompleted(3)
        }

        @Test
        @DisplayName("Should update cache when new praises are available")
        fun testCacheUpdateWithNewPraises() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)

            val settingsData = createSettingsData(praiseVersion = 3, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            val praises = createCachedPraises(10)
            val apiResponse = createApiResponse(version = 3, praiseCount = 10)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(eq(1)) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(praises)

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.Success)
            verify(mockPraiseCacheService).updateCache(praises, 3)
        }

        @Test
        @DisplayName("Should return AlreadyUpToDate when server version matches local version")
        fun testVersionBasedSyncDecision_NoNewVersion() = runTest {
            // Given: server version equals local version
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(5)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)

            val settingsData = createSettingsData(praiseVersion = 5, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should not fetch praises since version matches
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
            verify(mockPraiseCacheService).markSyncCompleted(5)
            mockedPraiseApiStatic.verify({ PraiseApiService.fetchPraises(any()) }, never())
        }

        @Test
        @DisplayName("Should fetch new praises when server version is higher than local")
        fun testVersionBasedSyncDecision_NewVersionAvailable() = runTest {
            // Given: server version > local version
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(2)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)

            val settingsData = createSettingsData(praiseVersion = 5, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            val apiResponse = createApiResponse(version = 5, praiseCount = 3)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(eq(2)) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(createCachedPraises(3))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.Success)
            mockedPraiseApiStatic.verify({ PraiseApiService.fetchPraises(eq(2)) })
        }
    }

    // ===========================================
    // 2. CONCURRENT SYNC PREVENTION TESTS
    // ===========================================

    @Nested
    @DisplayName("Concurrent Sync Prevention Tests")
    inner class ConcurrentSyncPreventionTests {

        @Test
        @DisplayName("Should prevent concurrent syncs")
        fun testConcurrentSyncPrevention() = runTest {
            // Given: First sync starts
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenAnswer {
                    // Simulate slow network call
                    Thread.sleep(100)
                    SettingsResult.Success(settingsData)
                }

            val apiResponse = createApiResponse(version = 2, praiseCount = 5)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(createCachedPraises(5))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            val syncCount = AtomicInteger(0)
            val latch = CountDownLatch(2)

            // When: Two concurrent sync attempts
            thread {
                kotlinx.coroutines.runBlocking {
                    val result = syncService.syncIfNeeded()
                    if (result is PraiseSyncService.SyncResult.Success) {
                        syncCount.incrementAndGet()
                    }
                    latch.countDown()
                }
            }

            Thread.sleep(10) // Ensure first thread starts first

            thread {
                kotlinx.coroutines.runBlocking {
                    val result = syncService.syncIfNeeded()
                    // Second call should get AlreadyUpToDate because sync is in progress
                    if (result is PraiseSyncService.SyncResult.AlreadyUpToDate) {
                        syncCount.incrementAndGet()
                    }
                    latch.countDown()
                }
            }

            latch.await(5, TimeUnit.SECONDS)

            // Then: Both should complete but only one should trigger actual sync
            assertEquals(2, syncCount.get())
        }

        @Test
        @DisplayName("Should track sync in progress state correctly")
        fun testSyncStateManagement() = runTest {
            // Initially not syncing
            assertFalse(syncService.isSyncInProgress())

            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 1, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When: sync completes
            val result = syncService.syncIfNeeded()

            // Then: should no longer be syncing
            assertFalse(syncService.isSyncInProgress())
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
        }

        @Test
        @DisplayName("Should reset sync state even after error")
        fun testSyncStateResetAfterError() = runTest {
            // Given: settings fetch fails
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Error("Network error"))

            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Error("Network error"))

            // When
            val result = syncService.syncIfNeeded()

            // Then: should reset sync state even after error
            assertFalse(syncService.isSyncInProgress())
            assertTrue(result is PraiseSyncService.SyncResult.Error)
        }
    }

    // ===========================================
    // 3. DAILY CHECK LOGIC TESTS
    // ===========================================

    @Nested
    @DisplayName("Daily Check Logic Tests")
    inner class DailyCheckLogicTests {

        @Test
        @DisplayName("Should sync when needsSync returns true")
        fun testShouldSyncBasedOnTiming_SyncNeeded() = runTest {
            // Given: sync needed today
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 1, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should perform sync
            mockedSettingsApiStatic.verify({ SettingsApiService.fetchSettings() })
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
        }

        @Test
        @DisplayName("Should skip sync when already synced today")
        fun testShouldSyncBasedOnTiming_AlreadySynced() = runTest {
            // Given: already synced today
            whenever(mockPraiseCacheService.needsSync()).thenReturn(false)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(5)

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should not perform sync
            mockedSettingsApiStatic.verify({ SettingsApiService.fetchSettings() }, never())
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
            assertEquals(5, (result as PraiseSyncService.SyncResult.AlreadyUpToDate).version)
        }

        @Test
        @DisplayName("Force sync should bypass daily check")
        fun testForceSyncBehavior() = runTest {
            // Given: no sync needed (already synced today)
            whenever(mockPraiseCacheService.needsSync()).thenReturn(false)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            val apiResponse = createApiResponse(version = 2, praiseCount = 3)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(createCachedPraises(3))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When: force sync
            val result = syncService.forceSync()

            // Then: Should perform sync regardless of daily check
            mockedSettingsApiStatic.verify({ SettingsApiService.fetchSettings() })
            assertTrue(result is PraiseSyncService.SyncResult.Success)
        }

        @Test
        @DisplayName("Force sync should fetch praises even when versions match")
        fun testForceSyncFetchesPraisesEvenWhenVersionsMatch() = runTest {
            // Given: versions match
            whenever(mockPraiseCacheService.needsSync()).thenReturn(false)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(5)

            val settingsData = createSettingsData(praiseVersion = 5, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            // API returns empty praises (up to date)
            val apiResponse = createApiResponse(version = 5, praiseCount = 0)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When: force sync
            val result = syncService.forceSync()

            // Then: Should still call praise API due to force flag
            mockedPraiseApiStatic.verify({ PraiseApiService.fetchPraises(any()) })
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
        }
    }

    // ===========================================
    // 4. UPDATE HANDLING TESTS
    // ===========================================

    @Nested
    @DisplayName("Update Handling Tests")
    inner class UpdateHandlingTests {

        @Test
        @DisplayName("Should trigger update notification when new version available")
        fun testUpdateNotificationTriggering() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)
            whenever(mockUpdateCacheService.wasNotificationShown()).thenReturn(false)
            whenever(mockUpdateCacheService.isVersionDismissed(any())).thenReturn(false)

            val settingsData = createSettingsData(
                praiseVersion = 1,
                versionName = "2.1.0",
                updateType = "NORMAL",
                updateUrl = "https://plugins.jetbrains.com/plugin/24628"
            )
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(any(), any())
            }.thenReturn(true)

            var capturedUpdateInfo: PraiseSyncService.UpdateInfo? = null
            syncService.onUpdateAvailable = { updateInfo ->
                capturedUpdateInfo = updateInfo
            }

            // When
            syncService.syncIfNeeded()

            // Then
            assertNotNull(capturedUpdateInfo)
            assertEquals(Constants.PLUGIN_VERSION, capturedUpdateInfo!!.currentVersion)
            assertEquals("2.1.0", capturedUpdateInfo!!.newVersion)
            assertEquals("NORMAL", capturedUpdateInfo!!.updateType)
            verify(mockUpdateCacheService).markNotificationShown()
        }

        @Test
        @DisplayName("Should handle emergency updates differently")
        fun testEmergencyVsNormalUpdates() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)
            whenever(mockUpdateCacheService.wasNotificationShown()).thenReturn(false)
            whenever(mockUpdateCacheService.isVersionDismissed(any())).thenReturn(false)

            val settingsData = createSettingsData(
                praiseVersion = 1,
                versionName = "2.1.0",
                updateType = "EMERGENCY",
                updateUrl = "https://plugins.jetbrains.com/plugin/24628"
            )
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(any(), any())
            }.thenReturn(true)

            var capturedUpdateInfo: PraiseSyncService.UpdateInfo? = null
            syncService.onUpdateAvailable = { updateInfo ->
                capturedUpdateInfo = updateInfo
            }

            // When
            syncService.syncIfNeeded()

            // Then: Emergency updates should NOT mark notification as shown
            assertNotNull(capturedUpdateInfo)
            assertEquals("EMERGENCY", capturedUpdateInfo!!.updateType)
            verify(mockUpdateCacheService, never()).markNotificationShown()
        }

        @Test
        @DisplayName("Should not show notification if already shown")
        fun testNoNotificationIfAlreadyShown() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)
            whenever(mockUpdateCacheService.wasNotificationShown()).thenReturn(true)  // Already shown

            val settingsData = createSettingsData(praiseVersion = 1, versionName = "2.1.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(any(), any())
            }.thenReturn(true)

            var notificationCalled = false
            syncService.onUpdateAvailable = { notificationCalled = true }

            // When
            syncService.syncIfNeeded()

            // Then: Should not trigger notification
            assertFalse(notificationCalled)
        }

        @Test
        @DisplayName("Should not show notification if version is dismissed")
        fun testNoNotificationIfVersionDismissed() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)
            whenever(mockUpdateCacheService.wasNotificationShown()).thenReturn(false)
            whenever(mockUpdateCacheService.isVersionDismissed("2.1.0")).thenReturn(true)

            val settingsData = createSettingsData(praiseVersion = 1, versionName = "2.1.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(any(), any())
            }.thenReturn(true)

            var notificationCalled = false
            syncService.onUpdateAvailable = { notificationCalled = true }

            // When
            syncService.syncIfNeeded()

            // Then: Should not trigger notification
            assertFalse(notificationCalled)
        }

        @Test
        @DisplayName("Should clear dismissed version when current version matches server")
        fun testClearDismissedVersionWhenUpToDate() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 1, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(any(), any())
            }.thenReturn(false)  // Up to date

            // When
            syncService.syncIfNeeded()

            // Then
            verify(mockUpdateCacheService).clearDismissedVersion()
        }
    }

    // ===========================================
    // 5. ERROR HANDLING TESTS
    // ===========================================

    @Nested
    @DisplayName("Error Handling Tests")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle network failure in settings fetch")
        fun testNetworkFailureHandling_SettingsFetch() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Error("Network error: Connection refused"))

            // Fallback to praise API also fails
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Error("Network error"))

            // When
            val result = syncService.syncIfNeeded()

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.Error)
            assertEquals("Network error", (result as PraiseSyncService.SyncResult.Error).message)
        }

        @Test
        @DisplayName("Should fallback to praise API when settings fetch fails")
        fun testFallbackToPraiseApiOnSettingsFailure() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Error("Settings API unavailable"))

            val apiResponse = createApiResponse(version = 2, praiseCount = 5)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(createCachedPraises(5))

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should fallback to praise API
            assertTrue(result is PraiseSyncService.SyncResult.Success)
            mockedPraiseApiStatic.verify({ PraiseApiService.fetchPraises(any()) })
        }

        @Test
        @DisplayName("Should handle partial sync failure - praises fetch fails after settings success")
        fun testPartialSyncFailure() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            // Praise fetch fails
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Error("Praise API timeout"))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should return error but update cache should still be updated
            assertTrue(result is PraiseSyncService.SyncResult.Error)
            assertEquals("Praise API timeout", (result as PraiseSyncService.SyncResult.Error).message)
            verify(mockUpdateCacheService).updateCache(any(), any(), any(), any(), any())
        }

        @Test
        @DisplayName("Should handle null data in API response")
        fun testNullDataHandling() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            // API returns response with null data
            val apiResponse = ApiResponse(status = true, message = "Success", data = null)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then
            assertTrue(result is PraiseSyncService.SyncResult.Error)
            assertEquals("No data received", (result as PraiseSyncService.SyncResult.Error).message)
        }

        @Test
        @DisplayName("Should handle empty praises after category filter")
        fun testEmptyPraisesAfterCategoryFilter() = runTest {
            // Given
            whenever(mockPraiseCacheService.needsSync()).thenReturn(true)
            whenever(mockUpdateCacheService.needsUpdateCheck()).thenReturn(false)
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(1)

            val settingsData = createSettingsData(praiseVersion = 2, versionName = "2.0.0")
            mockedSettingsApiStatic.`when`<SettingsResult> { SettingsApiService.fetchSettings() }
                .thenReturn(SettingsResult.Success(settingsData))

            val apiResponse = createApiResponse(version = 2, praiseCount = 5)
            mockedPraiseApiStatic.`when`<ApiResult> { PraiseApiService.fetchPraises(any()) }
                .thenReturn(ApiResult.Success(apiResponse))
            // No praises match the category filter
            mockedPraiseApiStatic.`when`<List<CachedPraise>> { PraiseApiService.parsePraises(any(), any()) }
                .thenReturn(emptyList())

            setupUpdateCheckMocks(settingsData, isUpdateAvailable = false)

            // When
            val result = syncService.syncIfNeeded()

            // Then: Should return AlreadyUpToDate since no praises matched filter
            assertTrue(result is PraiseSyncService.SyncResult.AlreadyUpToDate)
            verify(mockPraiseCacheService).markSyncCompleted(2)
        }
    }

    // ===========================================
    // HELPER TESTS
    // ===========================================

    @Nested
    @DisplayName("Helper Method Tests")
    inner class HelperMethodTests {

        @Test
        @DisplayName("getCurrentVersion should return version from cache")
        fun testGetCurrentVersion() {
            // Given
            whenever(mockPraiseCacheService.getCurrentVersion()).thenReturn(10)

            // When
            val version = syncService.getCurrentVersion()

            // Then
            assertEquals(10, version)
        }

        @Test
        @DisplayName("getCurrentVersion should return 0 on exception")
        fun testGetCurrentVersionOnException() {
            // Given
            whenever(mockPraiseCacheService.getCurrentVersion()).thenThrow(RuntimeException("Service not available"))

            // When
            val version = syncService.getCurrentVersion()

            // Then
            assertEquals(0, version)
        }

        @Test
        @DisplayName("hasCachedData should return true when data exists")
        fun testHasCachedDataTrue() {
            // Given
            whenever(mockPraiseCacheService.hasCachedData()).thenReturn(true)

            // When
            val hasCachedData = syncService.hasCachedData()

            // Then
            assertTrue(hasCachedData)
        }

        @Test
        @DisplayName("hasCachedData should return false on exception")
        fun testHasCachedDataOnException() {
            // Given
            whenever(mockPraiseCacheService.hasCachedData()).thenThrow(RuntimeException("Error"))

            // When
            val hasCachedData = syncService.hasCachedData()

            // Then
            assertFalse(hasCachedData)
        }

        @Test
        @DisplayName("isPluginUpdateAvailable should check version comparison")
        fun testIsPluginUpdateAvailable() {
            // Given
            whenever(mockUpdateCacheService.getLatestVersionName()).thenReturn("2.1.0")
            mockedSettingsApiStatic.`when`<Boolean> {
                SettingsApiService.isUpdateAvailable(Constants.PLUGIN_VERSION, "2.1.0")
            }.thenReturn(true)

            // When
            val isUpdateAvailable = syncService.isPluginUpdateAvailable()

            // Then
            assertTrue(isUpdateAvailable)
        }

        @Test
        @DisplayName("isPluginUpdateAvailable should return false when no server version")
        fun testIsPluginUpdateAvailableNoServerVersion() {
            // Given
            whenever(mockUpdateCacheService.getLatestVersionName()).thenReturn("")

            // When
            val isUpdateAvailable = syncService.isPluginUpdateAvailable()

            // Then
            assertFalse(isUpdateAvailable)
        }

        @Test
        @DisplayName("getLatestPluginVersion should return version from cache")
        fun testGetLatestPluginVersion() {
            // Given
            whenever(mockUpdateCacheService.getLatestVersionName()).thenReturn("2.5.0")

            // When
            val latestVersion = syncService.getLatestPluginVersion()

            // Then
            assertEquals("2.5.0", latestVersion)
        }

        @Test
        @DisplayName("getLatestPluginVersion should return empty string on exception")
        fun testGetLatestPluginVersionOnException() {
            // Given
            whenever(mockUpdateCacheService.getLatestVersionName()).thenThrow(RuntimeException("Error"))

            // When
            val latestVersion = syncService.getLatestPluginVersion()

            // Then
            assertEquals("", latestVersion)
        }
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private fun createSettingsData(
        praiseVersion: Int = 1,
        versionName: String = "2.0.0",
        versionCode: Int = 1,
        updateType: String = "NORMAL",
        updateUrl: String? = null
    ): SettingsData {
        return SettingsData(
            id = 1,
            appType = "JETBRAINS_PLUGIN",
            versionCode = versionCode,
            versionName = versionName,
            praiseVersion = praiseVersion,
            updateType = updateType,
            updateUrl = updateUrl,
            isActive = true
        )
    }

    private fun createApiResponse(version: Int, praiseCount: Int): ApiResponse {
        val praises = (1..praiseCount).map { id ->
            Praise(
                id = id,
                categories = listOf(
                    PraiseCategory(id = id, categoryId = 1, praiseId = id, count = 3)
                ),
                translations = listOf(
                    PraiseTranslation(id = id * 10, praiseId = id, langId = 1, name = "Arabic $id", sound = null),
                    PraiseTranslation(id = id * 10 + 1, praiseId = id, langId = 2, name = "English $id", sound = null)
                )
            )
        }

        return ApiResponse(
            status = true,
            message = "Success",
            data = PraiseData(version = version, praises = praises)
        )
    }

    private fun createCachedPraises(count: Int): List<CachedPraise> {
        return (1..count).map { id ->
            CachedPraise(
                id = id,
                arabicText = "Arabic praise $id",
                englishText = "English praise $id",
                categoryId = 1,
                count = 3
            )
        }
    }

    private fun setupUpdateCheckMocks(settingsData: SettingsData, isUpdateAvailable: Boolean) {
        mockedSettingsApiStatic.`when`<Boolean> {
            SettingsApiService.isUpdateAvailable(any(), any())
        }.thenReturn(isUpdateAvailable)

        if (!isUpdateAvailable) {
            // When up to date, clear dismissed version is called
            // No additional setup needed
        }
    }
}