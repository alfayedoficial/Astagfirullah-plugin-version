package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

/**
 * Service responsible for audio playback functionality.
 * Handles playing Islamic audio files (blessings upon the Prophet).
 */
@Service(Service.Level.APP)
class AudioService {

    private val logger = Logger.getInstance(AudioService::class.java)
    @Volatile
    private var currentClip: Clip? = null

    companion object {
        @JvmStatic
        fun getInstance(): AudioService {
            return ApplicationManager.getApplication().getService(AudioService::class.java)
        }
    }

    /**
     * Plays the blessing upon the Prophet Muhammad audio.
     * Only plays if sound is enabled in settings.
     */
    fun playBlessingSound() {
        if (!AstagfirullahSettings.getInstance().state.soundEnabled) {
            return
        }
        playAudio(Constants.BLESSING_AUDIO_PATH)
    }

    /**
     * Plays an audio file from the resources.
     * @param resourcePath The path to the audio resource
     */
    fun playAudio(resourcePath: String) {
        try {
            // Stop any currently playing audio
            stopCurrentAudio()

            val inputStream = this::class.java.getResourceAsStream(resourcePath)
            if (inputStream == null) {
                logger.warn("Audio resource not found: $resourcePath")
                return
            }

            inputStream.use { rawStream ->
                BufferedInputStream(rawStream).use { bufferedStream ->
                    val audioInputStream = AudioSystem.getAudioInputStream(bufferedStream)
                    audioInputStream.use { audioStream ->
                        currentClip = AudioSystem.getClip().apply {
                            open(audioStream)
                            addLineListener { event ->
                                if (event.type == LineEvent.Type.STOP) {
                                    event.line.close()
                                }
                            }
                            start()
                        }
                    }
                }
            }

            logger.debug("Playing audio: $resourcePath")
        } catch (e: Exception) {
            logger.error("Failed to play audio: $resourcePath", e)
        }
    }

    /**
     * Stops the currently playing audio if any.
     */
    fun stopCurrentAudio() {
        currentClip?.let {
            if (it.isRunning) {
                it.stop()
            }
            it.close()
            currentClip = null
        }
    }

    /**
     * Checks if audio is currently playing.
     */
    fun isPlaying(): Boolean {
        return currentClip?.isRunning == true
    }
}
