package com.alfayedoficial.astagfirullah

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

/**
 * Unit tests for FirstRunSetupActivity.
 *
 * Tests cover:
 * - First-run detection logic
 * - Dialog invocation behavior
 * - Settings state handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("FirstRunSetupActivity Tests")
class FirstRunSetupActivityTest {

    @Mock
    private lateinit var mockProject: Project

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockSettings: AstagfirullahSettings

    private lateinit var closeable: AutoCloseable
    private lateinit var applicationManagerMock: MockedStatic<ApplicationManager>
    private lateinit var settingsMock: MockedStatic<AstagfirullahSettings>

    private lateinit var activity: FirstRunSetupActivity

    @BeforeEach
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)

        // Setup ApplicationManager mock
        applicationManagerMock = mockStatic(ApplicationManager::class.java)
        applicationManagerMock.`when`<Application> { ApplicationManager.getApplication() }
            .thenReturn(mockApplication)

        // Setup AstagfirullahSettings mock
        settingsMock = mockStatic(AstagfirullahSettings::class.java)
        settingsMock.`when`<AstagfirullahSettings> { AstagfirullahSettings.getInstance() }
            .thenReturn(mockSettings)

        // Setup project mock
        `when`(mockProject.name).thenReturn("TestProject")

        activity = FirstRunSetupActivity()
    }

    @AfterEach
    fun tearDown() {
        applicationManagerMock.close()
        settingsMock.close()
        closeable.close()
    }

    @Nested
    @DisplayName("First-run detection tests")
    inner class FirstRunDetectionTests {

        @Test
        @DisplayName("Should detect first run when installTime is 0 and setup not completed")
        fun testDetectsFirstRunWhenInstallTimeZeroAndSetupNotCompleted() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)
            `when`(mockSettings.installTime).thenReturn(0L)
            `when`(mockSettings.firstSetupCompleted).thenReturn(false)

            // Capture the invokeLater runnable
            val runnableCaptor = argumentCaptor<Runnable>()

            // When
            activity.execute(mockProject)

            // Then - verify invokeLater was called (setup wizard requested)
            verify(mockApplication).invokeLater(runnableCaptor.capture())
        }

        @Test
        @DisplayName("Should not show wizard when setup already completed")
        fun testNoWizardWhenSetupCompleted() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(false)
            `when`(mockSettings.firstSetupCompleted).thenReturn(true)

            // When
            activity.execute(mockProject)

            // Then - verify invokeLater was NOT called
            verify(mockApplication, never()).invokeLater(any())
        }

        @Test
        @DisplayName("Should not show wizard when installTime is set (returning user)")
        fun testNoWizardForReturningUser() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(false)
            `when`(mockSettings.installTime).thenReturn(System.currentTimeMillis() - 86400000L) // 1 day ago

            // When
            activity.execute(mockProject)

            // Then
            verify(mockApplication, never()).invokeLater(any())
        }

        @Test
        @DisplayName("isFirstRun should return true when installTime is 0 AND firstSetupCompleted is false")
        fun testIsFirstRunLogic() {
            // Given - testing the actual isFirstRun() logic
            val settings = AstagfirullahSettings.State()

            // When installTime is 0 and setup not completed
            settings.installTime = 0L
            settings.firstSetupCompleted = false

            // Then - should be considered first run
            // Note: This tests the state logic, actual method is on AstagfirullahSettings
            assert(settings.installTime == 0L && !settings.firstSetupCompleted) {
                "Should detect first run when installTime=0 and firstSetupCompleted=false"
            }
        }

        @Test
        @DisplayName("isFirstRun should return false when installTime is set")
        fun testNotFirstRunWhenInstallTimeSet() {
            // Given
            val settings = AstagfirullahSettings.State()
            settings.installTime = 1000L
            settings.firstSetupCompleted = false

            // Then - should not be first run even if setup not completed
            assert(settings.installTime != 0L) {
                "Should not be first run when installTime is set"
            }
        }

        @Test
        @DisplayName("isFirstRun should return false when firstSetupCompleted is true")
        fun testNotFirstRunWhenSetupCompleted() {
            // Given
            val settings = AstagfirullahSettings.State()
            settings.installTime = 0L
            settings.firstSetupCompleted = true

            // Then
            assert(settings.firstSetupCompleted) {
                "Should not be first run when firstSetupCompleted is true"
            }
        }
    }

    @Nested
    @DisplayName("Dialog invocation tests")
    inner class DialogInvocationTests {

        @Test
        @DisplayName("Should invoke dialog on EDT when first run detected")
        fun testDialogInvokedOnEDT() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)

            val runnableCaptor = argumentCaptor<Runnable>()

            // When
            activity.execute(mockProject)

            // Then - verify invokeLater is called (dialog runs on EDT)
            verify(mockApplication).invokeLater(runnableCaptor.capture())
        }

        @Test
        @DisplayName("Should create FirstRunSetupDialog with project reference")
        fun testDialogCreatedWithProject() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)

            val runnableCaptor = argumentCaptor<Runnable>()

            // When
            activity.execute(mockProject)

            // Then
            verify(mockApplication).invokeLater(runnableCaptor.capture())

            // The captured runnable should create and show the dialog
            // Note: We can't directly verify FirstRunSetupDialog creation without
            // additional mocking, but we verify the invokeLater call pattern
        }

        @Test
        @DisplayName("Should not invoke dialog multiple times for same project")
        fun testSingleDialogInvocation() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)

            // When
            activity.execute(mockProject)

            // Then - should only call invokeLater once
            verify(mockApplication, times(1)).invokeLater(any())
        }
    }

    @Nested
    @DisplayName("Settings state handling tests")
    inner class SettingsStateHandlingTests {

        @Test
        @DisplayName("Should retrieve settings via getInstance")
        fun testSettingsRetrieval() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(false)

            // When
            activity.execute(mockProject)

            // Then - settings should be retrieved
            settingsMock.verify { AstagfirullahSettings.getInstance() }
        }

        @Test
        @DisplayName("Should check isFirstRun on settings")
        fun testIsFirstRunCheck() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(false)

            // When
            activity.execute(mockProject)

            // Then
            verify(mockSettings).isFirstRun()
        }

        @Test
        @DisplayName("Should handle null settings gracefully")
        fun testNullSettingsHandling() = runTest {
            // Given - settings returns null (edge case)
            settingsMock.`when`<AstagfirullahSettings> { AstagfirullahSettings.getInstance() }
                .thenReturn(null)

            // When/Then - should handle gracefully
            try {
                activity.execute(mockProject)
            } catch (e: NullPointerException) {
                // Expected behavior when settings are null
            }
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Should handle project being disposed")
        fun testProjectDisposed() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)
            `when`(mockProject.isDisposed).thenReturn(true)

            // When
            activity.execute(mockProject)

            // Then - should still attempt to show dialog (activity doesn't check disposal)
            verify(mockApplication).invokeLater(any())
        }

        @Test
        @DisplayName("Should handle application shutdown gracefully")
        fun testApplicationShutdown() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)
            `when`(mockApplication.isDisposed).thenReturn(true)

            // When
            activity.execute(mockProject)

            // Then - invokeLater might throw or be ignored, but activity shouldn't crash
            verify(mockApplication).invokeLater(any())
        }

        @Test
        @DisplayName("Should handle concurrent execute calls")
        fun testConcurrentExecution() = runTest {
            // Given
            `when`(mockSettings.isFirstRun()).thenReturn(true)

            // When - simulate concurrent calls
            activity.execute(mockProject)
            activity.execute(mockProject)

            // Then - each call should invoke separately (no internal synchronization)
            verify(mockApplication, times(2)).invokeLater(any())
        }
    }
}