package com.alfayedoficial.astagfirullah.data.model

import com.alfayedoficial.astagfirullah.core.Constants.API_APP_TYPE
import com.google.gson.annotations.SerializedName

// ==================== User & Token Models ====================

/**
 * Authenticated user data
 */
data class AuthUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String? = null,
    @SerializedName("is_service_running") val isServiceRunning: Boolean? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

/**
 * Authentication token data
 */
data class AuthTokens(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "Bearer",
    @SerializedName("expires_in") val expiresIn: Long? = null
)

// ==================== Request Models ====================

/**
 * Login request body
 */
data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("remember_me") val rememberMe: Boolean = false,
    @SerializedName("app_type") val appType: String = API_APP_TYPE
)

/**
 * Registration request body
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("role") val role: String = "user",
    @SerializedName("app_type") val appType: String = API_APP_TYPE
)

data class SocialRegisterRequest(
    val name: String,
    @SerializedName("social_id") val socialId: String,
    @SerializedName("role") val role: String = "user",
    @SerializedName("app_type") val appType: String = API_APP_TYPE
)
// ==================== API Response Models ====================

/**
 * Login API response wrapper
 */
data class AuthLoginResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthLoginData?
)

/**
 * Login response data containing user and token
 */
data class AuthLoginData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUser
)

/**
 * Register API response wrapper
 */
data class AuthRegisterResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthRegisterData?
)

/**
 * Register response data containing user and token
 */
data class AuthRegisterData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUser
)

/**
 * Get current user (me) API response wrapper
 */
data class AuthMeResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthUser?
)

/**
 * Generic auth error response
 */
data class AuthErrorResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
)

// ==================== Result Sealed Classes ====================

/**
 * Result of authentication operations (login/register)
 */
sealed class AuthResult {
    data class Success(
        val user: AuthUser,
        val token: String,
        val tokenType: String = "Bearer"
    ) : AuthResult()

    data class Error(
        val message: String,
        val fieldErrors: Map<String, String>? = null
    ) : AuthResult()
}

/**
 * Result of getting current user
 */
sealed class AuthMeResult {
    data class Success(val user: AuthUser) : AuthMeResult()
    data class Error(val message: String) : AuthMeResult()
}

/**
 * Result of logout operation
 */
sealed class LogoutResult {
    object Success : LogoutResult()
    data class Error(val message: String) : LogoutResult()
}

/**
 * Result of delete account operation
 */
sealed class DeleteAccountResult {
    object Success : DeleteAccountResult()
    data class Error(val message: String) : DeleteAccountResult()
}

/**
 * Validation result for form fields
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
