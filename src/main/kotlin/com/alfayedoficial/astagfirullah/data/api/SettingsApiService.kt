package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.core.VersionUtils
import com.alfayedoficial.astagfirullah.data.model.SettingsApiResponse
import com.alfayedoficial.astagfirullah.data.model.SettingsData
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Service for fetching plugin settings from the remote API.
 * Handles HTTP requests for version checking and update detection.
 */
object SettingsApiService {

    private val logger = Logger.getInstance(SettingsApiService::class.java)
    private val gson = Gson()

    /**
     * Fetches plugin settings from the API.
     * Returns server configuration including latest version info and praise database version.
     *
     * @return SettingsResult containing either the settings data or an error
     */
    fun fetchSettings(): SettingsResult {
        val urlString = "${Constants.API_BASE_URL}${Constants.API_SETTINGS_ENDPOINT}?app_type=${Constants.API_APP_TYPE}"

        logger.debug("Fetching settings from API for app_type: ${Constants.API_APP_TYPE}")

        return when (val httpResult = ApiHelper.get(urlString)) {
            is ApiHelper.HttpResult.Success -> {
                try {
                    val apiResponse = gson.fromJson(httpResult.body, SettingsApiResponse::class.java)
                    if (apiResponse.status && apiResponse.data != null) {
                        logger.debug("Settings API response: version_name=${apiResponse.data.versionName}, praise_version=${apiResponse.data.praiseVersion}")
                        SettingsResult.Success(apiResponse.data)
                    } else {
                        logger.warn("Settings API returned status=false: ${apiResponse.message}")
                        SettingsResult.Error("API error: ${apiResponse.message}")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to parse settings response", e)
                    SettingsResult.Error("Parse error: ${e.message}")
                }
            }
            is ApiHelper.HttpResult.Error -> {
                SettingsResult.Error(httpResult.message)
            }
        }
    }

    /**
     * Compares two version strings.
     * Delegates to VersionUtils for centralized version comparison logic.
     *
     * @param v1 First version string (e.g., "2.0.0")
     * @param v2 Second version string (e.g., "2.1.0")
     * @return Positive if v1 > v2, negative if v1 < v2, 0 if equal
     */
    fun compareVersions(v1: String, v2: String): Int = VersionUtils.compareVersions(v1, v2)

    /**
     * Checks if a new plugin version is available.
     * Delegates to VersionUtils for centralized version comparison logic.
     *
     * @param currentVersion Current installed plugin version
     * @param serverVersion Latest version from server
     * @return true if server version is newer
     */
    fun isUpdateAvailable(currentVersion: String, serverVersion: String): Boolean =
        VersionUtils.isUpdateAvailable(currentVersion, serverVersion)
}

/**
 * Result wrapper for Settings API calls
 */
sealed class SettingsResult {
    data class Success(val settings: SettingsData) : SettingsResult()
    data class Error(val message: String) : SettingsResult()
}