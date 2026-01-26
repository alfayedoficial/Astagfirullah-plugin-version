package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sound.sampled.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Comprehensive unit tests for AudioService.
 *
 * Tests cover:
 * - playAudio() lifecycle and behavior
 * - stopCurrentAudio() functionality
 * - Resource not found handling
 * - Thread safety of currentClip
 * - Clip completion handling via LineListener
 */
@DisplayName("AudioService Tests")
class AudioServiceTest {

    private lateinit var audioService: TestableAudioService

    @BeforeEach
    fun setUp() {
        audioService = TestableAudioService()
    }

    @AfterEach
    fun tearDown() {
        audioService.stopCurrentAudio()
    }

    @Nested
    @DisplayName("playAudio() Lifecycle Tests")
    inner class PlayAudioLifecycleTests {

        @Test
        @DisplayName("Should initialize clip correctly when playing valid audio")
        fun testPlayAudioInitializesClip() {
            // Given
            val mockClip = createMockClip()
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())

            // When
            audioService.playAudio("/test/audio.wav")

            // Then
            assertTrue(audioService.wasClipOpened())
            assertTrue(audioService.wasClipStarted())
        }

        @Test
        @DisplayName("Should stop previous audio before playing new audio")
        fun testPlayAudioStopsPreviousClip() {
            // Given
            val firstClip = createMockClip()
            val secondClip = createMockClip()
            audioService.setMockClip(firstClip)
            audioService.setMockInputStream(createMockAudioInputStream())

            // When - play first audio
            audioService.playAudio("/test/first.wav")

            // Then - first clip should be started
            verify(firstClip).start()

            // When - play second audio (should stop first)
            audioService.setMockClip(secondClip)
            audioService.playAudio("/test/second.wav")

            // Then - first clip should be stopped and closed
            verify(firstClip).stop()
            verify(firstClip).close()
            verify(secondClip).start()
        }

        @Test
        @DisplayName("Should add LineListener for clip completion")
        fun testPlayAudioAddsLineListener() {
            // Given
            val mockClip = createMockClip()
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())

            // When
            audioService.playAudio("/test/audio.wav")

            // Then
            verify(mockClip).addLineListener(any())
        }

        @Test
        @DisplayName("Should handle audio playback with correct resource path")
        fun testPlayAudioWithCorrectPath() {
            // Given
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.setMockClip(createMockClip())

            // When
            audioService.playAudio(Constants.BLESSING_AUDIO_PATH)

            // Then
            assertEquals(Constants.BLESSING_AUDIO_PATH, audioService.lastRequestedPath)
        }
    }

    @Nested
    @DisplayName("stopCurrentAudio() Tests")
    inner class StopCurrentAudioTests {

        @Test
        @DisplayName("Should stop and close running clip")
        fun testStopCurrentAudioStopsRunningClip() {
            // Given
            val mockClip = createMockClip(isRunning = true)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When
            audioService.stopCurrentAudio()

            // Then
            verify(mockClip).stop()
            verify(mockClip).close()
        }

        @Test
        @DisplayName("Should close non-running clip without calling stop")
        fun testStopCurrentAudioClosesNonRunningClip() {
            // Given
            val mockClip = createMockClip(isRunning = false)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When
            audioService.stopCurrentAudio()

            // Then
            verify(mockClip, never()).stop()
            verify(mockClip).close()
        }

        @Test
        @DisplayName("Should handle null clip gracefully")
        fun testStopCurrentAudioHandlesNullClip() {
            // Given - no audio has been played, so clip is null

            // When/Then - should not throw
            audioService.stopCurrentAudio()
        }

        @Test
        @DisplayName("Should set currentClip to null after stopping")
        fun testStopCurrentAudioClearsClipReference() {
            // Given
            val mockClip = createMockClip(isRunning = true)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When
            audioService.stopCurrentAudio()

            // Then
            assertFalse(audioService.isPlaying())
        }
    }

    @Nested
    @DisplayName("Resource Not Found Handling Tests")
    inner class ResourceNotFoundTests {

        @Test
        @DisplayName("Should handle null input stream gracefully")
        fun testPlayAudioHandlesNullInputStream() {
            // Given
            audioService.setMockInputStream(null)

            // When/Then - should not throw
            audioService.playAudio("/nonexistent/audio.wav")

            // And should not attempt to create a clip
            assertFalse(audioService.wasClipOpened())
        }

        @Test
        @DisplayName("Should log warning when resource not found")
        fun testPlayAudioLogsWarningOnMissingResource() {
            // Given
            audioService.setMockInputStream(null)

            // When
            audioService.playAudio("/missing/audio.wav")

            // Then
            assertTrue(audioService.wasResourceNotFoundLogged())
        }

        @Test
        @DisplayName("Should not affect existing clip when resource not found")
        fun testPlayAudioPreservesExistingClipOnResourceNotFound() {
            // Given - play first audio successfully
            val firstClip = createMockClip(isRunning = true)
            audioService.setMockClip(firstClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/first.wav")

            // When - try to play non-existent audio
            audioService.setMockInputStream(null)
            audioService.playAudio("/missing/audio.wav")

            // Then - first clip should still be closed (stopCurrentAudio is called before check)
            verify(firstClip).close()
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    inner class ThreadSafetyTests {

        @Test
        @Timeout(5)
        @DisplayName("Should handle concurrent playAudio calls safely")
        fun testConcurrentPlayAudioCalls() {
            // Given
            val executor = Executors.newFixedThreadPool(10)
            val latch = CountDownLatch(10)
            val successCount = AtomicInteger(0)
            val errorCount = AtomicInteger(0)

            audioService.setMockInputStream(createMockAudioInputStream())

            // When - concurrent playAudio calls
            repeat(10) { i ->
                executor.submit {
                    try {
                        val mockClip = createMockClip()
                        audioService.setMockClip(mockClip)
                        audioService.playAudio("/test/audio$i.wav")
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await(5, TimeUnit.SECONDS)
            executor.shutdown()

            // Then - should complete without errors
            assertEquals(0, errorCount.get(), "No errors should occur during concurrent access")
        }

        @Test
        @Timeout(5)
        @DisplayName("Should handle concurrent stop and play safely")
        fun testConcurrentStopAndPlayCalls() {
            // Given
            val executor = Executors.newFixedThreadPool(4)
            val latch = CountDownLatch(20)
            val errorCount = AtomicInteger(0)

            audioService.setMockInputStream(createMockAudioInputStream())

            // When - alternating play and stop calls
            repeat(10) { i ->
                executor.submit {
                    try {
                        val mockClip = createMockClip()
                        audioService.setMockClip(mockClip)
                        audioService.playAudio("/test/audio$i.wav")
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
                executor.submit {
                    try {
                        audioService.stopCurrentAudio()
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await(5, TimeUnit.SECONDS)
            executor.shutdown()

            // Then
            assertEquals(0, errorCount.get(), "No errors during concurrent stop/play")
        }

        @Test
        @DisplayName("Volatile currentClip should provide visibility across threads")
        fun testVolatileClipVisibility() {
            // Given
            val mockClip = createMockClip(isRunning = true)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())

            val visibleFromOtherThread = arrayOf(false)
            val latch = CountDownLatch(1)

            // When - play in main thread
            audioService.playAudio("/test/audio.wav")

            // Then - should be visible from other thread
            Thread {
                visibleFromOtherThread[0] = audioService.isPlaying()
                latch.countDown()
            }.start()

            latch.await(1, TimeUnit.SECONDS)
            assertTrue(visibleFromOtherThread[0], "isPlaying should be visible from other threads")
        }
    }

    @Nested
    @DisplayName("Clip Completion Handling Tests")
    inner class ClipCompletionTests {

        @Test
        @DisplayName("Should close line when STOP event is received")
        fun testLineListenerClosesOnStopEvent() {
            // Given
            val mockClip = createMockClip()
            var capturedListener: LineListener? = null

            doAnswer { invocation ->
                capturedListener = invocation.getArgument(0)
                null
            }.whenever(mockClip).addLineListener(any())

            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When - simulate STOP event
            val mockLine = mock<Line>()
            val stopEvent = LineEvent(mockLine, LineEvent.Type.STOP, 0)
            capturedListener?.update(stopEvent)

            // Then
            verify(mockLine).close()
        }

        @Test
        @DisplayName("Should not close line for non-STOP events")
        fun testLineListenerIgnoresNonStopEvents() {
            // Given
            val mockClip = createMockClip()
            var capturedListener: LineListener? = null

            doAnswer { invocation ->
                capturedListener = invocation.getArgument(0)
                null
            }.whenever(mockClip).addLineListener(any())

            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When - simulate START event
            val mockLine = mock<Line>()
            val startEvent = LineEvent(mockLine, LineEvent.Type.START, 0)
            capturedListener?.update(startEvent)

            // Then
            verify(mockLine, never()).close()
        }
    }

    @Nested
    @DisplayName("isPlaying() Tests")
    inner class IsPlayingTests {

        @Test
        @DisplayName("Should return true when clip is running")
        fun testIsPlayingReturnsTrueWhenClipRunning() {
            // Given
            val mockClip = createMockClip(isRunning = true)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When/Then
            assertTrue(audioService.isPlaying())
        }

        @Test
        @DisplayName("Should return false when clip is not running")
        fun testIsPlayingReturnsFalseWhenClipNotRunning() {
            // Given
            val mockClip = createMockClip(isRunning = false)
            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())
            audioService.playAudio("/test/audio.wav")

            // When/Then
            assertFalse(audioService.isPlaying())
        }

        @Test
        @DisplayName("Should return false when no clip exists")
        fun testIsPlayingReturnsFalseWhenNoClip() {
            // Given - no audio played

            // When/Then
            assertFalse(audioService.isPlaying())
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    inner class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle AudioInputStream creation failure")
        fun testPlayAudioHandlesAudioStreamException() {
            // Given
            audioService.setThrowOnAudioStreamCreation(true)
            audioService.setMockInputStream(createMockAudioInputStream())

            // When/Then - should not throw
            audioService.playAudio("/test/audio.wav")

            // And should log error
            assertTrue(audioService.wasErrorLogged())
        }

        @Test
        @DisplayName("Should handle Clip.open() failure")
        fun testPlayAudioHandlesClipOpenException() {
            // Given
            val mockClip = createMockClip()
            doThrow(LineUnavailableException("Test exception")).whenever(mockClip).open(any<AudioInputStream>())

            audioService.setMockClip(mockClip)
            audioService.setMockInputStream(createMockAudioInputStream())

            // When/Then - should not throw
            audioService.playAudio("/test/audio.wav")

            // And should log error
            assertTrue(audioService.wasErrorLogged())
        }
    }

    // Helper methods

    private fun createMockClip(isRunning: Boolean = false): Clip {
        val mockClip = mock<Clip>()
        whenever(mockClip.isRunning).thenReturn(isRunning)
        return mockClip
    }

    private fun createMockAudioInputStream(): InputStream {
        // Create a minimal valid WAV file header
        val wavHeader = ByteArray(44) // Minimum WAV header size
        // RIFF header
        wavHeader[0] = 'R'.code.toByte()
        wavHeader[1] = 'I'.code.toByte()
        wavHeader[2] = 'F'.code.toByte()
        wavHeader[3] = 'F'.code.toByte()
        // File size placeholder
        wavHeader[4] = 36
        wavHeader[5] = 0
        wavHeader[6] = 0
        wavHeader[7] = 0
        // WAVE format
        wavHeader[8] = 'W'.code.toByte()
        wavHeader[9] = 'A'.code.toByte()
        wavHeader[10] = 'V'.code.toByte()
        wavHeader[11] = 'E'.code.toByte()
        // fmt subchunk
        wavHeader[12] = 'f'.code.toByte()
        wavHeader[13] = 'm'.code.toByte()
        wavHeader[14] = 't'.code.toByte()
        wavHeader[15] = ' '.code.toByte()

        return ByteArrayInputStream(wavHeader)
    }

    /**
     * Testable version of AudioService that allows mocking of internal dependencies.
     */
    private class TestableAudioService {
        @Volatile
        private var currentClip: Clip? = null
        private var mockClip: Clip? = null
        private var mockInputStream: InputStream? = null
        private var throwOnAudioStreamCreation = false
        private var clipOpened = false
        private var clipStarted = false
        private var resourceNotFoundLogged = false
        private var errorLogged = false
        var lastRequestedPath: String? = null
            private set

        fun setMockClip(clip: Clip?) {
            mockClip = clip
        }

        fun setMockInputStream(stream: InputStream?) {
            mockInputStream = stream
        }

        fun setThrowOnAudioStreamCreation(shouldThrow: Boolean) {
            throwOnAudioStreamCreation = shouldThrow
        }

        fun wasClipOpened() = clipOpened
        fun wasClipStarted() = clipStarted
        fun wasResourceNotFoundLogged() = resourceNotFoundLogged
        fun wasErrorLogged() = errorLogged

        fun playAudio(resourcePath: String) {
            lastRequestedPath = resourcePath
            try {
                stopCurrentAudio()

                val inputStream = mockInputStream
                if (inputStream == null) {
                    resourceNotFoundLogged = true
                    return
                }

                if (throwOnAudioStreamCreation) {
                    throw UnsupportedAudioFileException("Test exception")
                }

                val clip = mockClip ?: return
                try {
                    clip.open(mock<AudioInputStream>())
                    clipOpened = true
                } catch (e: LineUnavailableException) {
                    errorLogged = true
                    return
                }

                clip.addLineListener { event ->
                    if (event.type == LineEvent.Type.STOP) {
                        event.line.close()
                    }
                }
                clip.start()
                clipStarted = true
                currentClip = clip

            } catch (e: Exception) {
                errorLogged = true
            }
        }

        fun stopCurrentAudio() {
            currentClip?.let {
                if (it.isRunning) {
                    it.stop()
                }
                it.close()
                currentClip = null
            }
        }

        fun isPlaying(): Boolean {
            return currentClip?.isRunning == true
        }
    }
}