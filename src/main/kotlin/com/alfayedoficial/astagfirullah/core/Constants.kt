package com.alfayedoficial.astagfirullah.core

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

/**
 * Central location for all plugin constants.
 * Eliminates magic strings and numbers throughout the codebase.
 */
object Constants {

    // Plugin Information
    const val PLUGIN_ID = "com.alfayedoficial.astagfirullah"
    const val PLUGIN_NAME = "Astagfirullah"

    /**
     * Fallback used only when the plugin descriptor is unavailable (e.g. plain unit tests
     * with no running IntelliJ Platform). Never read this directly — use [PLUGIN_VERSION].
     */
    private const val FALLBACK_VERSION = "2.0.1"

    /**
     * The running plugin's version, read from the plugin descriptor that Gradle's
     * `patchPluginXml` stamps from `project.version`.
     *
     * This used to be a hardcoded constant, which made the version live in THREE places
     * (build.gradle.kts, plugin.xml, here) and silently drift. That drift is not cosmetic:
     * [com.alfayedoficial.astagfirullah.data.api.SettingsApiService.isUpdateAvailable]
     * compares this value against the server's latest version, so a stale constant makes
     * the plugin nag every user to "update" to a build they are already running.
     */
    @JvmStatic
    val PLUGIN_VERSION: String by lazy {
        runCatching {
            PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.version
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: FALLBACK_VERSION
    }

    // API Configuration
    const val API_BASE_URL = "https://astaghfirullah.4fdev.com/api/v2"
    const val API_BASE_URL_V1 = "https://astaghfirullah.4fdev.com/api/v1"  // Auth uses v1
    const val API_PRAISE_LIST_ENDPOINT = "/praise/list"
    const val API_SETTINGS_ENDPOINT = "/settings"
    const val API_TIMEOUT_SECONDS = 30L
    const val API_CATEGORY_FILTER = 1  // Filter by category_id = 1
    const val API_APP_TYPE = "JETBRAINS_PLUGIN"  // App type for settings API

    // Auth API Endpoints (uses v1)
    const val API_AUTH_LOGIN_ENDPOINT = "/auth/login"
    const val API_AUTH_REGISTER_ENDPOINT = "/auth/register"
    const val API_AUTH_ME_ENDPOINT = "/auth/me"
    const val API_AUTH_LOGOUT_ENDPOINT = "/auth/logout"
    const val API_AUTH_DELETE_ACCOUNT_ENDPOINT = "/auth/deleteAccount"

    // Firebase Configuration (for anonymous auth)
    const val FIREBASE_API_KEY = "AIzaSyDPGs_6FVQUpA0_cfgQ3qvpXOWMOLgAt5g" // Replace with actual key
    const val FIREBASE_AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp"

    // Leaderboard API Endpoints (uses v1)
    const val API_LEADERBOARD_ENDPOINT = "/statistic/top-users"
    const val API_STATISTIC_CREATE_ENDPOINT = "/statistic/create"
    const val LEADERBOARD_DEFAULT_LIMIT = 50
    const val LEADERBOARD_TOOL_WINDOW_LIMIT = 5

    // Validation Constants
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_NAME_LENGTH = 100
    const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"

    // URLs
    const val PLUGIN_MARKETPLACE_URL = "https://plugins.jetbrains.com/plugin/24628-astagfirullah"
    const val DEVELOPER_LINKEDIN_URL = "https://www.linkedin.com/in/alfayedoficial"
    const val LINKEDIN_SHARE_BASE_URL = "https://www.linkedin.com/shareArticle?mini=true&url="

    // Resources
    const val BLESSING_AUDIO_PATH = "/raw/mohmmed.wav"
    const val PLUGIN_ICON_PATH = "/icons/pluginIconSmall.svg"

    // Default Settings
    const val DEFAULT_LANGUAGE = "العربية"
    const val DEFAULT_DELAY_SECONDS = "1.5"
    const val DEFAULT_DELAY_MILLIS = 1500L
    const val DEFAULT_SOUND_ENABLED = false
    const val DEFAULT_SHOW_ON_STARTUP = true

    // Display Configuration
    const val PHRASES_PER_DISPLAY = 6
    const val STARTUP_DISPLAY_SECONDS = 5

    // Daily dhikr startup dialog
    /** Seconds the dialog stays up before dismissing itself. */
    const val DAILY_DHIKR_COUNTDOWN_SECONDS = 5
    /** Countdown tick interval; 1s so the label counts down in whole seconds. */
    const val DAILY_DHIKR_TICK_MS = 1000
    const val DEFAULT_DAILY_DHIKR_ENABLED = true

    // Rating Prompt Timing (in milliseconds)
    const val FIRST_RATING_DELAY_MS = 2 * 60 * 1000L          // 2 minutes
    const val SECOND_RATING_DELAY_MS = 2 * 24 * 60 * 60 * 1000L // 2 days
    const val REMIND_LATER_DELAY_MS = 10 * 60 * 1000L          // 10 minutes

    // Rating States
    const val RATING_STATE_FIRST = "1"
    const val RATING_STATE_SECOND = "2"
    const val RATING_STATE_DONE = "3"

    // Notification
    const val NOTIFICATION_GROUP_ID = "AstagfirullahNotifications"
}
