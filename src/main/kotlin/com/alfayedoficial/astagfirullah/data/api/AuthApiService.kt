package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.model.*
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Service for handling authentication API calls.
 * Provides login, register, get user info, and logout functionality.
 */
object AuthApiService {

    private val logger = Logger.getInstance(AuthApiService::class.java)
    private val gson = Gson()

    /**
     * Attempts to log in with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @param rememberMe Whether to remember the login
     * @return AuthResult.Success with user and token, or AuthResult.Error with message
     */
    fun login(email: String, password: String, rememberMe: Boolean = false): AuthResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_LOGIN_ENDPOINT}"
            val requestBody = gson.toJson(LoginRequest(email, password, rememberMe))

            logger.debug("Attempting login for: $email")

            when (val result = ApiHelper.post(url, requestBody)) {
                is ApiHelper.HttpResult.Success -> {
                    val response = gson.fromJson(result.body, AuthLoginResponse::class.java)

                    if (response.status && response.data != null) {
                        logger.info("Login successful for: ${response.data.user.email}")
                        AuthResult.Success(
                            user = response.data.user,
                            token = response.data.token
                        )
                    } else {
                        logger.warn("Login failed: ${response.message}")
                        AuthResult.Error(response.message)
                    }
                }

                is ApiHelper.HttpResult.Error -> {
                    val fieldErrors = parseFieldErrors(result.errorBody)
                    logger.warn("Login HTTP error: ${result.message}")
                    AuthResult.Error(
                        message = getErrorMessage(result.errorBody) ?: result.message,
                        fieldErrors = fieldErrors
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Login exception: ${e.message}", e)
            AuthResult.Error("Login failed: ${e.message}")
        }
    }

    /**
     * Registers a new user account with email and password.
     *
     * @param name User's display name
     * @param email User's email address
     * @param password User's password
     * @return AuthResult.Success with user and token, or AuthResult.Error with message
     */
    fun register(name: String, email: String, password: String): AuthResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_REGISTER_ENDPOINT}"
            val requestBody = gson.toJson(RegisterRequest(name, email, password, password))

            logger.debug("Attempting registration for: $email")

            when (val result = ApiHelper.post(url, requestBody)) {
                is ApiHelper.HttpResult.Success -> {
                    val response = gson.fromJson(result.body, AuthRegisterResponse::class.java)

                    if (response.status && response.data != null) {
                        logger.info("Registration successful for: ${response.data.user.email}")
                        AuthResult.Success(
                            user = response.data.user,
                            token = response.data.token
                        )
                    } else {
                        logger.warn("Registration failed: ${response.message}")
                        AuthResult.Error(response.message)
                    }
                }

                is ApiHelper.HttpResult.Error -> {
                    val fieldErrors = parseFieldErrors(result.errorBody)
                    logger.warn("Registration HTTP error: ${result.message}")
                    AuthResult.Error(
                        message = getErrorMessage(result.errorBody) ?: result.message,
                        fieldErrors = fieldErrors
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Registration exception: ${e.message}", e)
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Registers a new user account using Firebase Anonymous social_id.
     * Uses POST with JSON body: name, role, social_id, app_type
     *
     * @param name User's display name
     * @param socialId Firebase Anonymous UID
     * @return AuthResult.Success with user and token, or AuthResult.Error with message
     */
    fun registerWithSocialId(name: String, socialId: String): AuthResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_REGISTER_ENDPOINT}"
            val requestBody = gson.toJson(SocialRegisterRequest(
                name = name,
                socialId = socialId,
            ))

            logger.debug("Attempting social registration for: $name with social_id: $socialId")

            when (val result = ApiHelper.post(url, requestBody)) {
                is ApiHelper.HttpResult.Success -> {
                    val response = gson.fromJson(result.body, AuthRegisterResponse::class.java)

                    if (response.status && response.data != null) {
                        logger.info("Social registration successful for: ${response.data.user.name}")
                        AuthResult.Success(
                            user = response.data.user,
                            token = response.data.token
                        )
                    } else {
                        logger.warn("Social registration failed: ${response.message}")
                        AuthResult.Error(response.message)
                    }
                }

                is ApiHelper.HttpResult.Error -> {
                    val fieldErrors = parseFieldErrors(result.errorBody)
                    logger.warn("Social registration HTTP error: ${result.message}")
                    AuthResult.Error(
                        message = getErrorMessage(result.errorBody) ?: result.message,
                        fieldErrors = fieldErrors
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Social registration exception: ${e.message}", e)
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Gets the current authenticated user's information.
     *
     * @param token The authentication token
     * @return AuthMeResult.Success with user, or AuthMeResult.Error with message
     */
    fun getMe(token: String): AuthMeResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_ME_ENDPOINT}"

            logger.debug("Fetching current user info")

            when (val result = ApiHelper.getWithAuth(url, token)) {
                is ApiHelper.HttpResult.Success -> {
                    val response = gson.fromJson(result.body, AuthMeResponse::class.java)

                    if (response.status && response.data != null) {
                        logger.debug("User info fetched: ${response.data.email}")
                        AuthMeResult.Success(response.data)
                    } else {
                        logger.warn("Get user failed: ${response.message}")
                        AuthMeResult.Error(response.message)
                    }
                }

                is ApiHelper.HttpResult.Error -> {
                    logger.warn("Get user HTTP error: ${result.message}")
                    AuthMeResult.Error(getErrorMessage(result.errorBody) ?: result.message)
                }
            }
        } catch (e: Exception) {
            logger.error("Get user exception: ${e.message}", e)
            AuthMeResult.Error("Failed to get user info: ${e.message}")
        }
    }

    /**
     * Logs out the current user.
     *
     * @param token The authentication token
     * @return LogoutResult.Success or LogoutResult.Error with message
     */
    fun logout(token: String): LogoutResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_LOGOUT_ENDPOINT}"

            logger.debug("Attempting logout")

            when (val result = ApiHelper.postWithAuth(url, "{}", token)) {
                is ApiHelper.HttpResult.Success -> {
                    logger.info("Logout successful")
                    LogoutResult.Success
                }

                is ApiHelper.HttpResult.Error -> {
                    // Even if API call fails, we consider logout successful locally
                    logger.warn("Logout HTTP error (proceeding anyway): ${result.message}")
                    LogoutResult.Success
                }
            }
        } catch (e: Exception) {
            logger.error("Logout exception (proceeding anyway): ${e.message}", e)
            // Even on exception, allow local logout
            LogoutResult.Success
        }
    }

    /**
     * Deletes the current user's account.
     *
     * @param token The authentication token
     * @return DeleteAccountResult.Success or DeleteAccountResult.Error with message
     */
    fun deleteAccount(token: String): DeleteAccountResult {
        return try {
            val url = "${Constants.API_BASE_URL_V1}${Constants.API_AUTH_DELETE_ACCOUNT_ENDPOINT}"

            logger.debug("Attempting to delete account")

            when (val result = ApiHelper.deleteWithAuth(url, token)) {
                is ApiHelper.HttpResult.Success -> {
                    logger.info("Account deleted successfully")
                    DeleteAccountResult.Success
                }

                is ApiHelper.HttpResult.Error -> {
                    logger.warn("Delete account HTTP error: ${result.message}")
                    DeleteAccountResult.Error(getErrorMessage(result.errorBody) ?: result.message)
                }
            }
        } catch (e: Exception) {
            logger.error("Delete account exception: ${e.message}", e)
            DeleteAccountResult.Error("Failed to delete account: ${e.message}")
        }
    }

    /**
     * Parses field-specific errors from error response body.
     */
    private fun parseFieldErrors(errorBody: String?): Map<String, String>? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val errorResponse = gson.fromJson(errorBody, AuthErrorResponse::class.java)
            errorResponse.errors?.mapValues { (_, messages) ->
                messages.firstOrNull() ?: ""
            }?.filterValues { it.isNotEmpty() }
        } catch (e: Exception) {
            logger.debug("Could not parse field errors: ${e.message}")
            null
        }
    }

    /**
     * Extracts the main error message from error response body.
     */
    private fun getErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val errorResponse = gson.fromJson(errorBody, AuthErrorResponse::class.java)
            errorResponse.message.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            logger.debug("Could not parse error message: ${e.message}")
            null
        }
    }
}