package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.intellij.openapi.diagnostic.Logger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service for Firebase Anonymous Authentication.
 * Uses Firebase REST API to create anonymous users and get their UUID.
 */
object FirebaseAuthService {

    private val logger = Logger.getInstance(FirebaseAuthService::class.java)
    private val gson = Gson()

    /**
     * Signs in anonymously to Firebase and returns the user's UUID (localId).
     *
     * @return FirebaseAuthResult.Success with UUID, or FirebaseAuthResult.Error
     */
    fun signInAnonymously(): FirebaseAuthResult {
        return try {
            val url = "${Constants.FIREBASE_AUTH_URL}?key=${Constants.FIREBASE_API_KEY}"

            logger.debug("Attempting Firebase anonymous sign-in")

            val result = postJson(url, FirebaseSignUpRequest(returnSecureToken = true))

            when (result) {
                is HttpResult.Success -> {
                    val response = gson.fromJson(result.body, FirebaseSignUpResponse::class.java)

                    if (response.localId.isNotEmpty()) {
                        logger.info("Firebase anonymous sign-in successful: ${response.localId}")
                        FirebaseAuthResult.Success(
                            uid = response.localId,
                            idToken = response.idToken,
                            refreshToken = response.refreshToken
                        )
                    } else {
                        logger.warn("Firebase sign-in failed: No localId returned")
                        FirebaseAuthResult.Error("Failed to get Firebase user ID")
                    }
                }

                is HttpResult.Error -> {
                    logger.warn("Firebase HTTP error: ${result.message}")
                    FirebaseAuthResult.Error(parseFirebaseError(result.errorBody) ?: result.message)
                }
            }
        } catch (e: Exception) {
            logger.error("Firebase sign-in exception: ${e.message}", e)
            FirebaseAuthResult.Error("Firebase authentication failed: ${e.message}")
        }
    }

    /**
     * Sends a POST request with JSON body.
     */
    private fun postJson(url: String, request: FirebaseSignUpRequest): HttpResult {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val body = gson.toJson(request)

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val responseCode = connection.responseCode

            return if (responseCode in 200..299) {
                val responseBody = connection.inputStream.bufferedReader().readText()
                HttpResult.Success(responseBody)
            } else {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.readText() ?: ""
                } catch (e: Exception) {
                    ""
                }
                HttpResult.Error("HTTP $responseCode", errorBody)
            }
        } catch (e: Exception) {
            return HttpResult.Error(e.message ?: "Unknown error", null)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Parses Firebase error response.
     */
    private fun parseFirebaseError(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val errorResponse = gson.fromJson(errorBody, FirebaseErrorResponse::class.java)
            errorResponse.error?.message
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Request/Response Models ====================

    /**
     * Firebase sign-up request body
     */
    data class FirebaseSignUpRequest(
        val returnSecureToken: Boolean = true
    )

    /**
     * Firebase sign-up response
     */
    data class FirebaseSignUpResponse(
        val localId: String = "",
        val idToken: String = "",
        val refreshToken: String = "",
        val expiresIn: String = ""
    )

    /**
     * Firebase error response
     */
    data class FirebaseErrorResponse(
        val error: FirebaseError? = null
    )

    data class FirebaseError(
        val code: Int = 0,
        val message: String = "",
        val errors: List<FirebaseErrorDetail>? = null
    )

    data class FirebaseErrorDetail(
        val message: String = "",
        val domain: String = "",
        val reason: String = ""
    )

    // ==================== Result Classes ====================

    sealed class HttpResult {
        data class Success(val body: String) : HttpResult()
        data class Error(val message: String, val errorBody: String? = null) : HttpResult()
    }

    /**
     * Result of Firebase authentication
     */
    sealed class FirebaseAuthResult {
        data class Success(
            val uid: String,
            val idToken: String,
            val refreshToken: String
        ) : FirebaseAuthResult()

        data class Error(val message: String) : FirebaseAuthResult()
    }
}