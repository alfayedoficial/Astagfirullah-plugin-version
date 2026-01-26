package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.diagnostic.Logger
import java.net.HttpURLConnection
import java.net.URL

/**
 * Shared HTTP helper for API calls.
 * Eliminates code duplication between PraiseApiService and SettingsApiService.
 */
object ApiHelper {

    private val logger = Logger.getInstance(ApiHelper::class.java)

    /**
     * Result of an HTTP request
     */
    sealed class HttpResult {
        data class Success(val body: String) : HttpResult()
        data class Error(
            val message: String,
            val statusCode: Int? = null,
            val errorBody: String? = null
        ) : HttpResult()
    }

    /**
     * Performs a GET request to the specified URL.
     *
     * @param urlString The full URL to fetch
     * @param headers Optional additional headers
     * @return HttpResult containing either the response body or an error
     */
    fun get(urlString: String, headers: Map<String, String>? = null): HttpResult {
        return try {
            logger.debug("HTTP GET: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                headers?.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
            }

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                HttpResult.Success(response)
            } else {
                val errorResponse = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) { "" }
                logger.warn("HTTP error: $responseCode - $errorResponse")
                HttpResult.Error("HTTP error: $responseCode", responseCode, errorResponse)
            }
        } catch (e: Exception) {
            logger.warn("HTTP request failed: $urlString", e)
            HttpResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Performs a GET request with authentication token.
     *
     * @param urlString The full URL to fetch
     * @param token The Bearer token for authentication
     * @return HttpResult containing either the response body or an error
     */
    fun getWithAuth(urlString: String, token: String): HttpResult {
        return get(urlString, mapOf("Authorization" to "Bearer $token"))
    }

    /**
     * Performs a POST request to the specified URL.
     *
     * @param urlString The full URL to post to
     * @param body The JSON body to send
     * @param headers Optional additional headers
     * @return HttpResult containing either the response body or an error
     */
    fun post(urlString: String, body: String, headers: Map<String, String>? = null): HttpResult {
        return try {
            logger.debug("HTTP POST: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                headers?.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
            }

            // Write body
            connection.outputStream.bufferedWriter().use { it.write(body) }

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                HttpResult.Success(response)
            } else {
                val errorResponse = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) { "" }
                logger.warn("HTTP POST error: $responseCode - $errorResponse")
                HttpResult.Error("HTTP error: $responseCode", responseCode, errorResponse)
            }
        } catch (e: Exception) {
            logger.warn("HTTP POST request failed: $urlString", e)
            HttpResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Performs a POST request with authentication token.
     *
     * @param urlString The full URL to post to
     * @param body The JSON body to send
     * @param token The Bearer token for authentication
     * @return HttpResult containing either the response body or an error
     */
    fun postWithAuth(urlString: String, body: String, token: String): HttpResult {
        return post(urlString, body, mapOf("Authorization" to "Bearer $token"))
    }

    /**
     * Performs a DELETE request to the specified URL.
     *
     * @param urlString The full URL to delete
     * @param headers Optional additional headers
     * @return HttpResult containing either the response body or an error
     */
    fun delete(urlString: String, headers: Map<String, String>? = null): HttpResult {
        return try {
            logger.debug("HTTP DELETE: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "DELETE"
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                headers?.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
            }

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                val response = try {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception) { "" }
                HttpResult.Success(response)
            } else {
                val errorResponse = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) { "" }
                logger.warn("HTTP DELETE error: $responseCode - $errorResponse")
                HttpResult.Error("HTTP error: $responseCode", responseCode, errorResponse)
            }
        } catch (e: Exception) {
            logger.warn("HTTP DELETE request failed: $urlString", e)
            HttpResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Performs a DELETE request with authentication token.
     *
     * @param urlString The full URL to delete
     * @param token The Bearer token for authentication
     * @return HttpResult containing either the response body or an error
     */
    fun deleteWithAuth(urlString: String, token: String): HttpResult {
        return delete(urlString, mapOf("Authorization" to "Bearer $token"))
    }
}