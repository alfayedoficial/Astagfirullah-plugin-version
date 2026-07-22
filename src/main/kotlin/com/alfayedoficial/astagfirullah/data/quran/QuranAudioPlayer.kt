package com.alfayedoficial.astagfirullah.data.quran

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.SampleBuffer
import java.io.BufferedInputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

/**
 * Streams and plays an MP3 from a URL, decoding with JLayer and writing PCM to a
 * SourceDataLine so we get real play / pause / resume / stop control.
 *
 * The IDE's stock javax.sound.sampled cannot decode MP3, so decoding is done frame-by-frame
 * with JLayer's [Decoder]/[Bitstream] rather than the mp3spi ServiceLoader, which is
 * unreliable across a plugin classloader. One playback thread at a time; starting a new
 * track stops the previous one.
 *
 * Application-level service: a single player shared across projects, so audio from one
 * project window does not stack on another's.
 */
@Service(Service.Level.APP)
class QuranAudioPlayer {

    private val logger = Logger.getInstance(QuranAudioPlayer::class.java)

    enum class State { IDLE, LOADING, PLAYING, PAUSED, ENDED, ERROR }

    /** Called on the EDT whenever the state changes. */
    var onState: ((state: State, title: String, message: String?) -> Unit)? = null

    /** Called on the EDT roughly a few times a second while playing. totalMs is 0 if unknown. */
    var onProgress: ((elapsedMs: Long, totalMs: Long) -> Unit)? = null

    private val lock = Object()
    private var playThread: Thread? = null

    // Guarded by [lock].
    private var paused = false

    // Bumped on every stop()/play() so a stale thread knows it has been superseded.
    private val stopRequested = AtomicBoolean(false)

    @Volatile private var currentTitle: String = ""

    val isActive: Boolean get() = playThread?.isAlive == true

    fun playPauseToggle() {
        synchronized(lock) {
            if (playThread?.isAlive != true) return
            paused = !paused
            if (!paused) lock.notifyAll()
        }
        publishState(if (isPausedNow()) State.PAUSED else State.PLAYING, currentTitle, null)
    }

    private fun isPausedNow(): Boolean = synchronized(lock) { paused }

    /** Starts a new track. Any current track is stopped first. */
    fun play(url: String, title: String) {
        stop()
        currentTitle = title
        stopRequested.set(false)
        synchronized(lock) { paused = false }
        publishState(State.LOADING, title, null)

        val thread = Thread({ streamAndPlay(url, title) }, "Astagfirullah-QuranPlayer")
        thread.isDaemon = true
        playThread = thread
        thread.start()
    }

    /** Stops the current track and blocks briefly for the thread to unwind. */
    fun stop() {
        val t = playThread ?: return
        stopRequested.set(true)
        synchronized(lock) {
            paused = false
            lock.notifyAll() // wake a paused thread so it can see the stop
        }
        if (t !== Thread.currentThread()) {
            t.join(1500)
        }
        playThread = null
    }

    private fun streamAndPlay(url: String, title: String) {
        var connection: HttpURLConnection? = null
        var line: SourceDataLine? = null
        var bitstream: Bitstream? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("User-Agent", "Astagfirullah-IntelliJ-Plugin")
            }
            val streamBytes = connection.contentLengthLong
            // FillInputStream is essential, not an optimisation: JLayer's Bitstream mis-syncs
            // on the partial reads a live socket returns and decodes only a single frame
            // (verified: 1 frame from the socket vs 3200+ from the identical bytes in a
            // file). Guaranteeing each read is fully satisfied makes true streaming work
            // with no temp file and no full-download latency.
            val input = BufferedInputStream(FillInputStream(connection.inputStream), 64 * 1024)
            bitstream = Bitstream(input)
            val decoder = Decoder()

            var totalMs = 0L
            var elapsedMs = 0.0
            var lastPublish = 0L
            var started = false

            while (!stopRequested.get()) {
                // Honour pause: block here until resumed or stopped.
                synchronized(lock) {
                    while (paused && !stopRequested.get()) {
                        try {
                            lock.wait()
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
                if (stopRequested.get()) break

                val header: Header = bitstream.readFrame() ?: break // EOF

                if (!started) {
                    if (streamBytes > 0) totalMs = header.total_ms(streamBytes.toInt()).toLong()
                }

                val output = decoder.decodeFrame(header, bitstream) as SampleBuffer

                if (!started) {
                    line = openLine(decoder.outputFrequency, decoder.outputChannels)
                    if (line == null) {
                        publishState(State.ERROR, title, "No audio output available")
                        return
                    }
                    line.start()
                    started = true
                    publishState(State.PLAYING, title, null)
                }

                writePcm(line!!, output.buffer, output.bufferLength)
                bitstream.closeFrame()

                elapsedMs += header.ms_per_frame()
                val now = System.currentTimeMillis()
                if (now - lastPublish >= 250) {
                    lastPublish = now
                    val e = elapsedMs.toLong()
                    publishProgress(e, totalMs)
                }
            }

            line?.drain()
            if (stopRequested.get()) {
                publishState(State.IDLE, title, null)
            } else {
                publishProgress(if (totalMs > 0) totalMs else elapsedMs.toLong(), totalMs)
                publishState(State.ENDED, title, null)
            }
        } catch (e: Exception) {
            if (!stopRequested.get()) {
                logger.warn("Quran playback failed for $url", e)
                publishState(State.ERROR, title, e.message ?: "Playback failed")
            }
        } finally {
            runCatching { line?.stop(); line?.close() }
            runCatching { bitstream?.close() }
            runCatching { connection?.disconnect() }
        }
    }

    private fun openLine(sampleRate: Int, channels: Int): SourceDataLine? {
        return try {
            val format = AudioFormat(sampleRate.toFloat(), 16, channels, true, false) // signed, little-endian
            val info = DataLine.Info(SourceDataLine::class.java, format)
            if (!AudioSystem.isLineSupported(info)) return null
            (AudioSystem.getLine(info) as SourceDataLine).apply { open(format) }
        } catch (e: Exception) {
            logger.warn("Could not open audio line", e)
            null
        }
    }

    /** JLayer gives native-endian shorts; write them as signed little-endian 16-bit PCM. */
    private fun writePcm(line: SourceDataLine, samples: ShortArray, length: Int) {
        val bytes = ByteArray(length * 2)
        var b = 0
        for (i in 0 until length) {
            val s = samples[i].toInt()
            bytes[b++] = (s and 0xFF).toByte()
            bytes[b++] = ((s shr 8) and 0xFF).toByte()
        }
        line.write(bytes, 0, bytes.size)
    }

    private fun publishState(state: State, title: String, message: String?) {
        ApplicationManager.getApplication().invokeLater { onState?.invoke(state, title, message) }
    }

    private fun publishProgress(elapsedMs: Long, totalMs: Long) {
        ApplicationManager.getApplication().invokeLater { onProgress?.invoke(elapsedMs, totalMs) }
    }

    /**
     * Loops each read until the requested length is filled (or EOF), so JLayer never sees a
     * short read from the socket. See the call site for why this is required.
     */
    private class FillInputStream(source: InputStream) : FilterInputStream(source) {
        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            var done = 0
            while (done < len) {
                val r = `in`.read(b, off + done, len - done)
                if (r == -1) return if (done == 0) -1 else done
                done += r
            }
            return done
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): QuranAudioPlayer =
            ApplicationManager.getApplication().getService(QuranAudioPlayer::class.java)

        fun formatTime(ms: Long): String {
            if (ms <= 0) return "0:00"
            val totalSec = ms / 1000
            val m = totalSec / 60
            val s = totalSec % 60
            return "%d:%02d".format(m, s)
        }
    }
}
