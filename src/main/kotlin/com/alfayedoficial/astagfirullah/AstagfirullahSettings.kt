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
        var lastRatingPromptTime: Long = 0L,
        /** Whether the once-a-day dhikr dialog is shown when the IDE opens. */
        var dailyDhikrEnabled: Boolean = Constants.DEFAULT_DAILY_DHIKR_ENABLED,
        /**
         * ISO date (yyyy-MM-dd) the daily dhikr dialog last appeared, used to cap it at
         * once per day. Stored as a string rather than an epoch so the comparison is a
         * plain calendar-day check and cannot drift with timezone maths.
         */
        var lastDailyDhikrDate: String = "",
        /**
         * The plugin version whose "What's New" screen the user has already seen. When it
         * differs from the running version, the What's New dialog is shown once on the next
         * IDE open. Empty means it has never been shown.
         */
        var lastWhatsNewVersion: String = "",
        /**
         * Whether anonymous, aggregate usage statistics (a count of remembrance phrases
         * displayed) may be sent. Default on; the user can opt out in Settings. No personal
         * data is ever sent — only a random device id, a count, and the platform.
         */
        var anonymousStatsEnabled: Boolean = true,
        /** Random per-install id for anonymous stats. Generated lazily; contains no PII. */
        var telemetryDeviceId: String = "",
        /** Phrases displayed but not yet acknowledged by the backend, carried across restarts. */
        var pendingStatsCount: Int = 0,
        /** Epoch millis of the last successful stats flush; 0 if never. */
        var lastStatsFlushTime: Long = 0L,
        /**
         * Last "Total dhikr" figure fetched from the server, shown in Settings. Cached so it
         * can be displayed offline; -1 means never fetched.
         */
        var cachedTotalDhikr: Long = -1L
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

    var dailyDhikrEnabled: Boolean
        get() = myState.dailyDhikrEnabled
        set(value) { myState.dailyDhikrEnabled = value }

    var lastDailyDhikrDate: String
        get() = myState.lastDailyDhikrDate
        set(value) { myState.lastDailyDhikrDate = value }

    /**
     * Whether the daily dhikr dialog should be shown for [today].
     *
     * Caps the dialog at once per calendar day. IntelliJ fires a startup activity per
     * OPENED PROJECT, so without this check a developer opening three projects in one
     * morning would get three popups.
     */
    fun shouldShowDailyDhikr(today: String): Boolean =
        dailyDhikrEnabled && lastDailyDhikrDate != today

    var lastWhatsNewVersion: String
        get() = myState.lastWhatsNewVersion
        set(value) { myState.lastWhatsNewVersion = value }

    var anonymousStatsEnabled: Boolean
        get() = myState.anonymousStatsEnabled
        set(value) { myState.anonymousStatsEnabled = value }

    var telemetryDeviceId: String
        get() = myState.telemetryDeviceId
        set(value) { myState.telemetryDeviceId = value }

    var pendingStatsCount: Int
        get() = myState.pendingStatsCount
        set(value) { myState.pendingStatsCount = value }

    var lastStatsFlushTime: Long
        get() = myState.lastStatsFlushTime
        set(value) { myState.lastStatsFlushTime = value }

    var cachedTotalDhikr: Long
        get() = myState.cachedTotalDhikr
        set(value) { myState.cachedTotalDhikr = value }

    /** Returns the device id, generating and persisting one on first use. */
    @Synchronized
    fun getOrCreateDeviceId(): String {
        if (myState.telemetryDeviceId.isBlank()) {
            myState.telemetryDeviceId = java.util.UUID.randomUUID().toString()
        }
        return myState.telemetryDeviceId
    }

    /**
     * Whether the "What's New" dialog should be shown for [currentVersion].
     *
     * True when the running version differs from the last version whose What's New the user
     * saw. A fresh install is handled by the caller: [FirstRunSetupActivity] records the
     * current version after the setup wizard so a new user never sees both the wizard and a
     * What's New for the same version.
     */
    fun shouldShowWhatsNew(currentVersion: String): Boolean =
        lastWhatsNewVersion != currentVersion

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
