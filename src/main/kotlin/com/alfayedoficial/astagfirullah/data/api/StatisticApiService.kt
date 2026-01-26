package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.cache.AuthCacheService
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Service for sending statistics to the API silently in background.
 * Only sends when user is logged in. No logging or status display.
 */
object StatisticApiService {

    /**
     * Sends phrase display count to the API silently.
     * Only sends if user is logged in.
     *
     * @param count Number of phrases displayed
     */
    fun sendStatistic(count: Int) {
        // Only send if user is logged in
        if (!AuthCacheService.getInstance().isLoggedIn()) {
            return
        }

        if (count <= 0) {
            return
        }

        try {
            val token = AuthCacheService.getInstance().getAuthToken() ?: return
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_STATISTIC_CREATE_ENDPOINT}"
            postFormDataSilently(url, token, count)
        } catch (_: Exception) {
            // Silent - ignore all errors
        }
    }

    /**
     * Sends a POST request with form-data silently (no logging).
     */
    private fun postFormDataSilently(url: String, token: String, count: Int) {
        var connection: HttpURLConnection? = null
        try {
            val boundary = "----FormBoundary${UUID.randomUUID().toString().replace("-", "")}"

            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Accept", "application/json")
            }

            // Build form-data body
            val body = buildString {
                // count field
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"count\"\r\n\r\n")
                append("$count\r\n")

                // app_type field
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"app_type\"\r\n\r\n")
                append("${Constants.API_APP_TYPE}\r\n")

                // End boundary
                append("--$boundary--\r\n")
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body)
                writer.flush()
            }

            // Just read response to complete the request, ignore result
            connection.responseCode
        } catch (_: Exception) {
            // Silent - ignore all errors
        } finally {
            connection?.disconnect()
        }
    }
}