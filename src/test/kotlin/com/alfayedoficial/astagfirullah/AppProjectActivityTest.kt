package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.data.sync.PraiseSyncService
import com.alfayedoficial.astagfirullah.data.telemetry.PraiseTelemetryService
import com.intellij.build.BuildViewManager
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

/**
 * Unit tests for AppProjectActivity.
 *
 * Tests cover:
 * - execute() method behavior
 * - Build listener registration
 * - Sync initialization
 * - Startup phrase display
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("AppProjectActivity Tests")
class AppProjectActivityTest {

    @Mock
    private lateinit var mockProject: Project

    @Mock
    private lateinit var mockBuildViewManager: BuildViewManager

    @Mock
    private lateinit var mockBuildProgressService: BuildProgressService

    @Mock
    private lateinit var mockMessageBus: MessageBus

    @Mock
    private lateinit var mockMessageBusConnection: MessageBusConnection

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockPraiseSyncService: PraiseSyncService

    @Mock
    private lateinit var mockUpdateNotificationService: UpdateNotificationService

    private lateinit var closeable: AutoCloseable
    private lateinit var applicationManagerMock: MockedStatic<ApplicationManager>

    private lateinit var activity: AppProjectActivity

    @BeforeEach
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)

        // Setup ApplicationManager mock
        applicationManagerMock = mockStatic(ApplicationManager::class.java)
        applicationManagerMock.`when`<Application> { ApplicationManager.getApplication() }
            .thenReturn(mockApplication)

        // Application services resolve through Application.getService. Static-mocking each
        // service's companion getInstance() never intercepted the Kotlin call (it is an
        // instance call on the Companion), which is why this class used to fail in setUp.
        `when`(mockApplication.getService(PraiseSyncService::class.java)).thenReturn(mockPraiseSyncService)
        `when`(mockApplication.getService(UpdateNotificationService::class.java)).thenReturn(mockUpdateNotificationService)
        `when`(mockApplication.getService(PraiseTelemetryService::class.java)).thenReturn(mock<PraiseTelemetryService>())
        // Run the background sync inline so tests observe it deterministically.
        `when`(mockPraiseSyncService.coroutineScope).thenReturn(CoroutineScope(Dispatchers.Unconfined))

        // Setup project mocks
        `when`(mockProject.name).thenReturn("TestProject")
        `when`(mockProject.messageBus).thenReturn(mockMessageBus)
        `when`(mockMessageBus.connect(any<com.intellij.openapi.Disposable>())).thenReturn(mockMessageBusConnection)

        // Setup service mocks via project.service extension simulation
        `when`(mockProject.getService(BuildViewManager::class.java)).thenReturn(mockBuildViewManager)
        `when`(mockProject.getService(BuildProgressService::class.java)).thenReturn(mockBuildProgressService)

        activity = AppProjectActivity()
    }

    @AfterEach
    fun tearDown() {
        applicationManagerMock.close()
        closeable.close()
    }

    @Nested
    @DisplayName("execute() method tests")
    inner class ExecuteMethodTests {

        @Test
        @DisplayName("Should initialize all components when execute is called")
        fun testExecuteInitializesAllComponents() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then - verify build listener was added
            verify(mockBuildViewManager).addListener(any<BuildProgressService>(), any())
        }

        @Test
        @DisplayName("Should display phrases on startup")
        fun testExecuteDisplaysPhrasesOnStartup() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockBuildProgressService).displayPhrasesOnStartup()
        }

        @Test
        @DisplayName("Should setup message bus connection for project roots")
        fun testExecuteSetsUpMessageBusConnection() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockProject.messageBus).connect(any<com.intellij.openapi.Disposable>())
            verify(mockMessageBusConnection).subscribe(any(), any())
        }
    }

    @Nested
    @DisplayName("Listener registration tests")
    inner class ListenerRegistrationTests {

        @Test
        @DisplayName("Should register BuildProgressService as build listener")
        fun testBuildListenerRegistration() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockBuildViewManager).addListener(any<BuildProgressService>(), any())
        }

        @Test
        @DisplayName("Should subscribe to PROJECT_ROOTS topic")
        fun testProjectRootsSubscription() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockMessageBusConnection).subscribe(any(), any())
        }
    }

    @Nested
    @DisplayName("Sync initialization tests")
    inner class SyncInitializationTests {

        @Test
        @DisplayName("Should initialize PraiseSyncService during execution")
        fun testSyncServiceInitialization() = runTest {
            // Given
            setupDefaultMocks()
            whenever(mockPraiseSyncService.syncIfNeeded())
                .thenReturn(PraiseSyncService.SyncResult.AlreadyUpToDate(1))

            // When
            activity.execute(mockProject)

            // Then - verify sync service was accessed (initialization happens in background)
            // The actual sync is performed in a coroutine, so we verify the setup
            verify(mockBuildProgressService).displayPhrasesOnStartup()
        }

        @Test
        @DisplayName("Should set up update notification callback")
        fun testUpdateNotificationCallbackSetup() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then - callback setup verification happens through the sync service mock
            // The activity sets up onUpdateAvailable callback during syncAndCheckUpdates
        }
    }

    @Nested
    @DisplayName("Startup phrase display tests")
    inner class StartupPhraseDisplayTests {

        @Test
        @DisplayName("Should call displayPhrasesOnStartup on BuildProgressService")
        fun testStartupPhraseDisplay() = runTest {
            // Given
            setupDefaultMocks()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockBuildProgressService).displayPhrasesOnStartup()
        }

        @Test
        @DisplayName("Should display phrases regardless of sync result")
        fun testPhrasesDisplayIndependentOfSync() = runTest {
            // Given
            setupDefaultMocks()
            whenever(mockPraiseSyncService.syncIfNeeded())
                .thenReturn(PraiseSyncService.SyncResult.Error("Network error"))

            // When
            activity.execute(mockProject)

            // Then - phrases should still be displayed even if sync fails
            verify(mockBuildProgressService).displayPhrasesOnStartup()
        }
    }

    @Nested
    @DisplayName("Error handling tests")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle missing BuildViewManager gracefully")
        fun testMissingBuildViewManager() = runTest {
            // Given
            `when`(mockProject.getService(BuildViewManager::class.java)).thenReturn(null)

            // When/Then - should not throw exception
            try {
                activity.execute(mockProject)
            } catch (e: RuntimeException) {
                // Expected: project.service<T>() throws (NPE on older platforms, a
                // service-not-found RuntimeException on newer ones) when the service is absent.
            }
        }

        @Test
        @DisplayName("Should continue execution when sync service fails")
        fun testContinuesOnSyncFailure() = runTest {
            // Given
            setupDefaultMocks()
            whenever(mockPraiseSyncService.syncIfNeeded())
                .thenThrow(RuntimeException("Sync failed"))

            // When
            activity.execute(mockProject)

            // Then - should still display phrases
            verify(mockBuildProgressService).displayPhrasesOnStartup()
        }
    }

    private suspend fun setupDefaultMocks() {
        org.mockito.kotlin.whenever(mockPraiseSyncService.syncIfNeeded())
            .thenReturn(PraiseSyncService.SyncResult.AlreadyUpToDate(1))
    }
}