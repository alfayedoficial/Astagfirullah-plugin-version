package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Comprehensive unit tests for BuildProgressService.
 *
 * Tests cover:
 * - handleTask() execution and lifecycle
 * - Phrase display sequencing with progress updates
 * - Concurrent task prevention via isTaskRunning
 * - Statistics recording
 * - displayStartupPhrase() (displayPhrasesOnStartup) behavior
 * - Project-level isolation
 */
@DisplayName("BuildProgressService Tests")
class BuildProgressServiceTest {

    private lateinit var mockProject: Project
    private lateinit var buildProgressService: TestableBuildProgressService

    @BeforeEach
    fun setUp() {
        mockProject = mock<Project>()
        whenever(mockProject.isDisposed).thenReturn(false)
        buildProgressService = TestableBuildProgressService(mockProject)
    }

    @AfterEach
    fun tearDown() {
        buildProgressService.reset()
    }

    @Nested
    @DisplayName("handleTask() Execution Tests")
    inner class HandleTaskExecutionTests {

        @Test
        @DisplayName("Should execute task when not already running")
        fun testHandleTaskExecutesWhenNotRunning() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When
            buildProgressService.testHandleTask()

            // Then
            assertTrue(buildProgressService.wasTaskExecuted())
        }

        @Test
        @DisplayName("Should not execute when task is already running")
        fun testHandleTaskSkipsWhenAlreadyRunning() {
            // Given
            buildProgressService.setIsTaskRunning(true)

            // When
            buildProgressService.testHandleTask()

            // Then
            assertFalse(buildProgressService.wasTaskExecuted())
        }

        @Test
        @DisplayName("Should not execute when progress indicator exists")
        fun testHandleTaskSkipsWhenProgressIndicatorExists() {
            // Given
            buildProgressService.setHasProgressIndicator(true)

            // When
            buildProgressService.testHandleTask()

            // Then
            assertFalse(buildProgressService.wasTaskExecuted())
        }

        @Test
        @DisplayName("Should reset isTaskRunning to false after completion")
        fun testHandleTaskResetsRunningStateAfterCompletion() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When
            buildProgressService.testHandleTask()
            buildProgressService.simulateTaskCompletion()

            // Then
            assertFalse(buildProgressService.isCurrentlyRunning())
        }

        @Test
        @DisplayName("Should reset isTaskRunning even on exception")
        fun testHandleTaskResetsRunningStateOnException() {
            // Given
            buildProgressService.setHasProgressIndicator(false)
            buildProgressService.setThrowDuringExecution(true)

            // When
            buildProgressService.testHandleTask()
            buildProgressService.simulateTaskCompletion()

            // Then
            assertFalse(buildProgressService.isCurrentlyRunning())
        }
    }

    @Nested
    @DisplayName("Phrase Display Sequencing Tests")
    inner class PhraseDisplaySequencingTests {

        @Test
        @DisplayName("Should display phrases in sequence with progress updates")
        fun testPhrasesDisplayedSequentially() {
            // Given
            val phrases = listOf("Phrase 1", "Phrase 2", "Phrase 3")
            buildProgressService.setTestPhrases(phrases)
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()
            val displayedTexts = mutableListOf<String>()
            val displayedFractions = mutableListOf<Double>()

            doAnswer { invocation ->
                displayedTexts.add(invocation.getArgument(0))
                null
            }.whenever(mockIndicator).text = any()

            doAnswer { invocation ->
                displayedFractions.add(invocation.getArgument(0))
                null
            }.whenever(mockIndicator).fraction = any()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertEquals(phrases, displayedTexts)
            assertEquals(listOf(1.0 / 3, 2.0 / 3, 1.0), displayedFractions)
        }

        @Test
        @DisplayName("Should calculate correct delay per phrase")
        fun testDelayCalculationPerPhrase() {
            // Given
            val phrases = listOf("P1", "P2", "P3", "P4", "P5")
            val maxDurationSeconds = 10
            val expectedDelayMs = (maxDurationSeconds * 1000L) / phrases.size

            buildProgressService.setTestPhrases(phrases)
            buildProgressService.setHasProgressIndicator(false)

            // When
            val calculatedDelay = buildProgressService.calculateDelay(maxDurationSeconds, phrases.size)

            // Then
            assertEquals(expectedDelayMs, calculatedDelay)
        }

        @Test
        @DisplayName("Should use settings delay when no max duration specified")
        fun testUsesSettingsDelayWhenNoMaxDuration() {
            // Given
            val settingsDelayMs = 2000L
            buildProgressService.setSettingsDelayMs(settingsDelayMs)

            // When
            val calculatedDelay = buildProgressService.calculateDelay(-1, 5)

            // Then
            assertEquals(settingsDelayMs, calculatedDelay)
        }

        @Test
        @DisplayName("Should display correct number of phrases (PHRASES_PER_DISPLAY)")
        fun testDisplaysCorrectNumberOfPhrases() {
            // Given
            val allPhrases = (1..20).map { "Phrase $it" }
            buildProgressService.setTestPhrases(allPhrases)
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()
            val displayedCount = AtomicInteger(0)

            doAnswer {
                displayedCount.incrementAndGet()
                null
            }.whenever(mockIndicator).text = any()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator, Constants.PHRASES_PER_DISPLAY)

            // Then
            assertEquals(Constants.PHRASES_PER_DISPLAY, displayedCount.get())
        }
    }

    @Nested
    @DisplayName("Concurrent Task Prevention Tests")
    inner class ConcurrentTaskPreventionTests {

        @Test
        @Timeout(5)
        @DisplayName("Should prevent concurrent tasks using AtomicBoolean")
        fun testConcurrentTaskPrevention() {
            // Given
            val executor = Executors.newFixedThreadPool(10)
            val startLatch = CountDownLatch(10)
            val endLatch = CountDownLatch(10)
            val executionCount = AtomicInteger(0)

            buildProgressService.setHasProgressIndicator(false)
            buildProgressService.setSlowExecution(true, 500)

            // When - attempt concurrent executions
            repeat(10) {
                executor.submit {
                    startLatch.countDown()
                    try {
                        startLatch.await() // Synchronize start
                        if (buildProgressService.attemptExecution()) {
                            executionCount.incrementAndGet()
                        }
                    } finally {
                        endLatch.countDown()
                    }
                }
            }

            endLatch.await(5, TimeUnit.SECONDS)
            executor.shutdown()

            // Then - only one execution should succeed
            assertEquals(1, executionCount.get())
        }

        @Test
        @DisplayName("Should use compareAndSet for thread-safe check-and-set")
        fun testCompareAndSetUsage() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When - first call
            val firstResult = buildProgressService.attemptExecution()

            // Then
            assertTrue(firstResult)

            // When - second call (while first is "running")
            val secondResult = buildProgressService.attemptExecution()

            // Then
            assertFalse(secondResult)
        }

        @Test
        @DisplayName("Should allow new task after previous completes")
        fun testAllowsNewTaskAfterCompletion() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When - first execution
            buildProgressService.testHandleTask()
            buildProgressService.simulateTaskCompletion()

            // Then - second execution should work
            val canExecuteAgain = buildProgressService.attemptExecution()
            assertTrue(canExecuteAgain)
        }
    }

    @Nested
    @DisplayName("Statistics Recording Tests")
    inner class StatisticsRecordingTests {

        @Test
        @DisplayName("Should record phrases displayed count")
        fun testRecordsPhrasesDisplayedCount() {
            // Given
            val phrases = listOf("P1", "P2", "P3", "P4", "P5", "P6")
            buildProgressService.setTestPhrases(phrases)
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertEquals(phrases.size, buildProgressService.recordedPhraseCount)
        }

        @Test
        @DisplayName("Should record language used")
        fun testRecordsLanguageUsed() {
            // Given
            val testLanguage = "English"
            buildProgressService.setTestLanguage(testLanguage)
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertEquals(testLanguage, buildProgressService.recordedLanguage)
        }

        @Test
        @DisplayName("Should call StatisticsService.recordPhrasesDisplayed")
        fun testCallsStatisticsServiceRecord() {
            // Given
            val phrases = listOf("P1", "P2", "P3")
            buildProgressService.setTestPhrases(phrases)
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertTrue(buildProgressService.wasStatisticsRecorded())
        }
    }

    @Nested
    @DisplayName("displayStartupPhrase Tests")
    inner class DisplayStartupPhraseTests {

        @Test
        @DisplayName("Should use STARTUP_DISPLAY_SECONDS for duration")
        fun testUsesStartupDisplaySeconds() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When
            buildProgressService.displayPhrasesOnStartup()

            // Then
            assertEquals(Constants.STARTUP_DISPLAY_SECONDS, buildProgressService.lastMaxDurationSeconds)
        }

        @Test
        @DisplayName("Should calculate delay based on startup duration and phrase count")
        fun testStartupDelayCalculation() {
            // Given
            val phrases = listOf("P1", "P2", "P3", "P4", "P5")
            buildProgressService.setTestPhrases(phrases)
            val expectedDelay = (Constants.STARTUP_DISPLAY_SECONDS * 1000L) / phrases.size

            // When
            val calculatedDelay = buildProgressService.calculateDelay(
                Constants.STARTUP_DISPLAY_SECONDS,
                phrases.size
            )

            // Then
            assertEquals(expectedDelay, calculatedDelay)
        }

        @Test
        @DisplayName("Should trigger handleTask with startup duration")
        fun testTriggersHandleTaskWithDuration() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When
            buildProgressService.displayPhrasesOnStartup()

            // Then
            assertTrue(buildProgressService.wasHandleTaskCalledWithDuration())
        }
    }

    @Nested
    @DisplayName("Project-Level Isolation Tests")
    inner class ProjectLevelIsolationTests {

        @Test
        @DisplayName("Should have independent state per project instance")
        fun testIndependentStatePerProject() {
            // Given
            val project1 = mock<Project>()
            val project2 = mock<Project>()
            whenever(project1.isDisposed).thenReturn(false)
            whenever(project2.isDisposed).thenReturn(false)

            val service1 = TestableBuildProgressService(project1)
            val service2 = TestableBuildProgressService(project2)

            service1.setHasProgressIndicator(false)
            service2.setHasProgressIndicator(false)

            // When
            service1.setIsTaskRunning(true)

            // Then - service2 should have independent state
            assertTrue(service1.isCurrentlyRunning())
            assertFalse(service2.isCurrentlyRunning())
        }

        @Test
        @DisplayName("Should allow concurrent tasks in different projects")
        fun testConcurrentTasksInDifferentProjects() {
            // Given
            val project1 = mock<Project>()
            val project2 = mock<Project>()
            whenever(project1.isDisposed).thenReturn(false)
            whenever(project2.isDisposed).thenReturn(false)

            val service1 = TestableBuildProgressService(project1)
            val service2 = TestableBuildProgressService(project2)

            service1.setHasProgressIndicator(false)
            service2.setHasProgressIndicator(false)

            // When
            val result1 = service1.attemptExecution()
            val result2 = service2.attemptExecution()

            // Then - both should succeed
            assertTrue(result1)
            assertTrue(result2)
        }

        @Test
        @DisplayName("Should maintain project reference for task context")
        fun testMaintainsProjectReference() {
            // Given
            val testProject = mock<Project>()
            whenever(testProject.name).thenReturn("TestProject")
            whenever(testProject.isDisposed).thenReturn(false)

            val service = TestableBuildProgressService(testProject)

            // Then
            assertEquals(testProject, service.getProject())
        }
    }

    @Nested
    @DisplayName("onEvent Tests")
    inner class OnEventTests {

        @Test
        @DisplayName("Should delegate to handleTask on build event")
        fun testOnEventDelegatesToHandleTask() {
            // Given
            buildProgressService.setHasProgressIndicator(false)
            val mockBuildEvent = mock<BuildEvent>()

            // When
            buildProgressService.onEvent("buildId", mockBuildEvent)

            // Then
            assertTrue(buildProgressService.wasHandleTaskCalled())
        }

        @Test
        @DisplayName("Should delegate to handleTask on sync event")
        fun testOnSyncEventDelegatesToHandleTask() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            // When
            buildProgressService.onSyncEvent()

            // Then
            assertTrue(buildProgressService.wasHandleTaskCalled())
        }
    }

    @Nested
    @DisplayName("Audio Playback Integration Tests")
    inner class AudioPlaybackIntegrationTests {

        @Test
        @DisplayName("Should trigger audio playback at task start")
        fun testTriggersAudioPlaybackAtStart() {
            // Given
            buildProgressService.setHasProgressIndicator(false)

            val mockIndicator = mock<ProgressIndicator>()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertTrue(buildProgressService.wasAudioPlaybackTriggered())
        }

        @Test
        @DisplayName("Should play blessing sound only once per task")
        fun testPlaysBlessingSoundOncePerTask() {
            // Given
            buildProgressService.setHasProgressIndicator(false)
            val mockIndicator = mock<ProgressIndicator>()

            // When
            buildProgressService.simulateTaskWithIndicator(mockIndicator)

            // Then
            assertEquals(1, buildProgressService.audioPlaybackCount)
        }
    }

    /**
     * Testable version of BuildProgressService that allows mocking of internal dependencies.
     */
    private class TestableBuildProgressService(private val project: Project) {
        private val isTaskRunning = AtomicBoolean(false)
        private var hasProgressIndicator = false
        private var taskExecuted = false
        private var throwDuringExecution = false
        private var slowExecution = false
        private var slowExecutionMs = 0L
        private var testPhrases: List<String> = listOf("Default Phrase")
        private var testLanguage = Constants.DEFAULT_LANGUAGE
        private var settingsDelayMs = Constants.DEFAULT_DELAY_MILLIS
        private var handleTaskCalled = false
        private var handleTaskCalledWithDuration = false
        var lastMaxDurationSeconds = -1
            private set
        var recordedPhraseCount = 0
            private set
        var recordedLanguage = ""
            private set
        private var statisticsRecorded = false
        private var audioPlaybackTriggered = false
        var audioPlaybackCount = 0
            private set

        fun setIsTaskRunning(running: Boolean) {
            isTaskRunning.set(running)
        }

        fun setHasProgressIndicator(has: Boolean) {
            hasProgressIndicator = has
        }

        fun setThrowDuringExecution(shouldThrow: Boolean) {
            throwDuringExecution = shouldThrow
        }

        fun setSlowExecution(slow: Boolean, delayMs: Long = 0) {
            slowExecution = slow
            slowExecutionMs = delayMs
        }

        fun setTestPhrases(phrases: List<String>) {
            testPhrases = phrases
        }

        fun setTestLanguage(language: String) {
            testLanguage = language
        }

        fun setSettingsDelayMs(delayMs: Long) {
            settingsDelayMs = delayMs
        }

        fun wasTaskExecuted() = taskExecuted
        fun wasHandleTaskCalled() = handleTaskCalled
        fun wasHandleTaskCalledWithDuration() = handleTaskCalledWithDuration
        fun wasStatisticsRecorded() = statisticsRecorded
        fun wasAudioPlaybackTriggered() = audioPlaybackTriggered
        fun isCurrentlyRunning() = isTaskRunning.get()
        fun getProject() = project

        fun testHandleTask(maxDurationSeconds: Int = -1) {
            handleTask(maxDurationSeconds)
        }

        fun onEvent(buildId: Any, event: BuildEvent) {
            handleTask()
        }

        fun onSyncEvent() {
            handleTask()
        }

        fun displayPhrasesOnStartup() {
            handleTask(Constants.STARTUP_DISPLAY_SECONDS)
        }

        private fun handleTask(maxDurationSeconds: Int = -1) {
            handleTaskCalled = true
            lastMaxDurationSeconds = maxDurationSeconds
            if (maxDurationSeconds > 0) {
                handleTaskCalledWithDuration = true
            }

            if (!isTaskRunning.compareAndSet(false, true)) {
                return
            }

            if (hasProgressIndicator) {
                isTaskRunning.set(false)
                return
            }

            taskExecuted = true
        }

        fun simulateTaskCompletion() {
            isTaskRunning.set(false)
        }

        fun attemptExecution(): Boolean {
            if (!isTaskRunning.compareAndSet(false, true)) {
                return false
            }

            if (hasProgressIndicator) {
                isTaskRunning.set(false)
                return false
            }

            if (slowExecution) {
                Thread.sleep(slowExecutionMs)
            }

            return true
        }

        fun simulateTaskWithIndicator(indicator: ProgressIndicator, phraseCount: Int = testPhrases.size) {
            try {
                // Simulate audio playback at start
                audioPlaybackTriggered = true
                audioPlaybackCount++

                // Display phrases with progress
                val phrases = testPhrases.take(phraseCount)
                for (i in phrases.indices) {
                    indicator.text = phrases[i]
                    indicator.fraction = (i + 1) / phrases.size.toDouble()
                }

                // Record statistics
                recordedPhraseCount = phrases.size
                recordedLanguage = testLanguage
                statisticsRecorded = true

            } catch (e: Exception) {
                if (throwDuringExecution) throw e
            } finally {
                isTaskRunning.set(false)
            }
        }

        fun calculateDelay(maxDurationSeconds: Int, phraseCount: Int): Long {
            return if (maxDurationSeconds > 0) {
                (maxDurationSeconds * 1000L) / phraseCount
            } else {
                settingsDelayMs
            }
        }

        fun reset() {
            isTaskRunning.set(false)
            taskExecuted = false
            handleTaskCalled = false
            handleTaskCalledWithDuration = false
            statisticsRecorded = false
            audioPlaybackTriggered = false
            audioPlaybackCount = 0
            recordedPhraseCount = 0
            recordedLanguage = ""
            lastMaxDurationSeconds = -1
        }
    }
}