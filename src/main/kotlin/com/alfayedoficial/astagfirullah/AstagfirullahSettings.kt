package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for the Astagfirullah plugin.
 * Uses IntelliJ's PersistentStateComponent for proper state management.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahSettings",
    storages = [Storage("astagfirullah.xml")]
)
class AstagfirullahSettings : PersistentStateComponent<AstagfirullahSettings.State> {

    private var myState = State()

    companion object {
        @JvmStatic
        fun getInstance(): AstagfirullahSettings {
            return ApplicationManager.getApplication().getService(AstagfirullahSettings::class.java)
        }

        // Supported languages
        val SUPPORTED_LANGUAGES = arrayOf(
            "العربية",      // Arabic
            "English",      // English
            "أردو",         // Urdu
            "فارسى",        // Farsi/Persian
            "Türkçe",       // Turkish
            "Bahasa",       // Indonesian
            "বাংলা"         // Bengali
        )

        // Delay options in seconds
        val DELAY_OPTIONS = arrayOf(
            "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5",
            "5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5",
            "9", "9.5", "10"
        )
    }

    /**
     * State class holding all plugin settings.
     * All fields must have default values for XML serialization.
     */
    data class State(
        var language: String = Constants.DEFAULT_LANGUAGE,
        var delaySeconds: String = Constants.DEFAULT_DELAY_SECONDS,
        var soundEnabled: Boolean = Constants.DEFAULT_SOUND_ENABLED,
        var showOnStartup: Boolean = Constants.DEFAULT_SHOW_ON_STARTUP,
        var installTime: Long = 0L,
        var firstRatingTime: String = Constants.RATING_STATE_FIRST,
        var ratingPrompted: Boolean = false,
        var firstSetupCompleted: Boolean = false,
        var lastRatingPromptTime: Long = 0L
    )

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    // Convenience methods for accessing settings

    var language: String
        get() = myState.language
        set(value) { myState.language = value }

    var delaySeconds: String
        get() = myState.delaySeconds
        set(value) { myState.delaySeconds = value }

    var soundEnabled: Boolean
        get() = myState.soundEnabled
        set(value) { myState.soundEnabled = value }

    var showOnStartup: Boolean
        get() = myState.showOnStartup
        set(value) { myState.showOnStartup = value }

    var installTime: Long
        get() = myState.installTime
        set(value) { myState.installTime = value }

    var firstRatingTime: String
        get() = myState.firstRatingTime
        set(value) { myState.firstRatingTime = value }

    var ratingPrompted: Boolean
        get() = myState.ratingPrompted
        set(value) { myState.ratingPrompted = value }

    var firstSetupCompleted: Boolean
        get() = myState.firstSetupCompleted
        set(value) { myState.firstSetupCompleted = value }

    var lastRatingPromptTime: Long
        get() = myState.lastRatingPromptTime
        set(value) { myState.lastRatingPromptTime = value }

    /**
     * Checks if this is the first run of the plugin.
     * @return true if setup has not been completed yet
     */
    fun isFirstRun(): Boolean = installTime == 0L && !firstSetupCompleted

    /**
     * Converts the delay seconds string to milliseconds.
     */
    fun getDelayMillis(): Long {
        return try {
            (delaySeconds.toDouble() * 1000).toLong()
        } catch (e: NumberFormatException) {
            Constants.DEFAULT_DELAY_MILLIS
        }
    }
}
