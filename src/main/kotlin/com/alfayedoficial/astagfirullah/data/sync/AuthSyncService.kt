package com.alfayedoficial.astagfirullah.data.sync

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.api.AuthApiService
import com.alfayedoficial.astagfirullah.data.api.FirebaseAuthService
import com.alfayedoficial.astagfirullah.data.cache.AuthCacheService
import com.alfayedoficial.astagfirullah.data.model.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service for coordinating authentication operations.
 * Bridges the API layer and cache layer, providing a unified interface for auth operations.
 */
@Service(Service.Level.APP)
class AuthSyncService {

    private val logger = Logger.getInstance(AuthSyncService::class.java)
    private val isOperating = AtomicBoolean(false)

    // Callback for auth state changes
    var onAuthStateChanged: ((AuthState) -> Unit)? = null

    companion object {
        @JvmStatic
        fun getInstance(): AuthSyncService {
            return ApplicationManager.getApplication().getService(AuthSyncService::class.java)
        }
    }

    /**
     * Authentication state for UI updates
     */
    sealed class AuthState {
        object LoggedOut : AuthState()
        data class LoggedIn(val user: AuthUser) : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }

    /**
     * Result of login operation
     */
    sealed class LoginResult {
        data class Success(val user: AuthUser) : LoginResult()
        data class Error(val message: String, val fieldErrors: Map<String, String>? = null) : LoginResult()
    }

    /**
     * Result of registration operation
     */
    sealed class RegisterResult {
        data class Success(val user: AuthUser) : RegisterResult()
        data class Error(val message: String, val fieldErrors: Map<String, String>? = null) : RegisterResult()
    }

    /**
     * Attempts to log in with the provided credentials.
     *
     * @param email User's email
     * @param password User's password
     * @param rememberMe Whether to remember the login
     * @return LoginResult indicating success or failure
     */
    fun login(email: String, password: String, rememberMe: Boolean = false): LoginResult {
        if (!isOperating.compareAndSet(false, true)) {
            return LoginResult.Error("Operation in progress")
        }

        try {
            onAuthStateChanged?.invoke(AuthState.Loading)

            // Validate inputs first
            val emailValidation = validateEmail(email)
            if (!emailValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(emailValidation.errorMessage ?: "Invalid email"))
                return LoginResult.Error(emailValidation.errorMessage ?: "Invalid email")
            }

            val passwordValidation = validatePasswordNotEmpty(password)
            if (!passwordValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(passwordValidation.errorMessage ?: "Invalid password"))
                return LoginResult.Error(passwordValidation.errorMessage ?: "Invalid password")
            }

            // Call API
            when (val result = AuthApiService.login(email, password, rememberMe)) {
                is AuthResult.Success -> {
                    // Save to cache
                    AuthCacheService.getInstance().saveAuth(
                        user = result.user,
                        token = result.token,
                        tokenType = result.tokenType,
                        rememberMe = rememberMe
                    )

                    logger.info("Login successful: ${result.user.email}")
                    onAuthStateChanged?.invoke(AuthState.LoggedIn(result.user))
                    return LoginResult.Success(result.user)
                }

                is AuthResult.Error -> {
                    logger.warn("Login failed: ${result.message}")
                    onAuthStateChanged?.invoke(AuthState.Error(result.message))
                    return LoginResult.Error(result.message, result.fieldErrors)
                }
            }
        } finally {
            isOperating.set(false)
        }
    }

    /**
     * Registers a new user account.
     *
     * @param name User's display name
     * @param email User's email
     * @param password User's password
     * @return RegisterResult indicating success or failure
     */
    fun register(name: String, email: String, password: String): RegisterResult {
        if (!isOperating.compareAndSet(false, true)) {
            return RegisterResult.Error("Operation in progress")
        }

        try {
            onAuthStateChanged?.invoke(AuthState.Loading)

            // Validate inputs
            val nameValidation = validateName(name)
            if (!nameValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(nameValidation.errorMessage ?: "Invalid name"))
                return RegisterResult.Error(nameValidation.errorMessage ?: "Invalid name")
            }

            val emailValidation = validateEmail(email)
            if (!emailValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(emailValidation.errorMessage ?: "Invalid email"))
                return RegisterResult.Error(emailValidation.errorMessage ?: "Invalid email")
            }

            val passwordValidation = validatePassword(password)
            if (!passwordValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(passwordValidation.errorMessage ?: "Invalid password"))
                return RegisterResult.Error(passwordValidation.errorMessage ?: "Invalid password")
            }

            // Call API
            when (val result = AuthApiService.register(name, email, password)) {
                is AuthResult.Success -> {
                    // Save to cache
                    AuthCacheService.getInstance().saveAuth(
                        user = result.user,
                        token = result.token,
                        tokenType = result.tokenType,
                        rememberMe = false
                    )

                    logger.info("Registration successful: ${result.user.email}")
                    onAuthStateChanged?.invoke(AuthState.LoggedIn(result.user))
                    return RegisterResult.Success(result.user)
                }

                is AuthResult.Error -> {
                    logger.warn("Registration failed: ${result.message}")
                    onAuthStateChanged?.invoke(AuthState.Error(result.message))
                    return RegisterResult.Error(result.message, result.fieldErrors)
                }
            }
        } finally {
            isOperating.set(false)
        }
    }

    /**
     * Registers a new user using Firebase Anonymous authentication.
     * Flow: Firebase Anonymous Auth → Get UID → Register with social_id
     *
     * @param name User's display name
     * @return RegisterResult indicating success or failure
     */
    fun registerWithFirebase(name: String): RegisterResult {
        if (!isOperating.compareAndSet(false, true)) {
            return RegisterResult.Error("Operation in progress")
        }

        try {
            onAuthStateChanged?.invoke(AuthState.Loading)

            // Validate name
            val nameValidation = validateName(name)
            if (!nameValidation.isValid) {
                onAuthStateChanged?.invoke(AuthState.Error(nameValidation.errorMessage ?: "Invalid name"))
                return RegisterResult.Error(nameValidation.errorMessage ?: "Invalid name")
            }

            // Step 1: Firebase Anonymous Sign-in to get UID
            logger.debug("Starting Firebase anonymous authentication")
            when (val firebaseResult = FirebaseAuthService.signInAnonymously()) {
                is FirebaseAuthService.FirebaseAuthResult.Success -> {
                    val socialId = firebaseResult.uid
                    logger.debug("Firebase auth successful, got UID: $socialId")

                    // Step 2: Register with backend using social_id
                    when (val result = AuthApiService.registerWithSocialId(name, socialId)) {
                        is AuthResult.Success -> {
                            // Save to cache
                            AuthCacheService.getInstance().saveAuth(
                                user = result.user,
                                token = result.token,
                                tokenType = result.tokenType,
                                rememberMe = true // Auto remember for social login
                            )

                            // Also save Firebase UID for future reference
                            AuthCacheService.getInstance().saveSocialId(socialId)

                            logger.info("Firebase registration successful: ${result.user.name}")
                            onAuthStateChanged?.invoke(AuthState.LoggedIn(result.user))
                            return RegisterResult.Success(result.user)
                        }

                        is AuthResult.Error -> {
                            logger.warn("Backend registration failed: ${result.message}")
                            onAuthStateChanged?.invoke(AuthState.Error(result.message))
                            return RegisterResult.Error(result.message, result.fieldErrors)
                        }
                    }
                }

                is FirebaseAuthService.FirebaseAuthResult.Error -> {
                    logger.warn("Firebase auth failed: ${firebaseResult.message}")
                    onAuthStateChanged?.invoke(AuthState.Error(firebaseResult.message))
                    return RegisterResult.Error("Firebase authentication failed: ${firebaseResult.message}")
                }
            }
        } finally {
            isOperating.set(false)
        }
    }

    /**
     * Logs out the current user.
     *
     * @return true if logout was successful
     */
    fun logout(): Boolean {
        val token = AuthCacheService.getInstance().getAuthToken()

        // Call API logout (if we have a token)
        if (token != null) {
            AuthApiService.logout(token)
        }

        // Clear local cache regardless of API result
        AuthCacheService.getInstance().clearAuth()

        logger.info("Logout completed")
        onAuthStateChanged?.invoke(AuthState.LoggedOut)
        return true
    }

    /**
     * Result of delete account operation
     */
    sealed class DeleteAccountResult {
        object Success : DeleteAccountResult()
        data class Error(val message: String) : DeleteAccountResult()
    }

    /**
     * Deletes the current user's account and clears all local data.
     *
     * @return DeleteAccountResult indicating success or failure
     */
    fun deleteAccount(): DeleteAccountResult {
        if (!isOperating.compareAndSet(false, true)) {
            return DeleteAccountResult.Error("Operation in progress")
        }

        try {
            val token = AuthCacheService.getInstance().getAuthToken()
                ?: return DeleteAccountResult.Error("Not logged in")

            logger.debug("Attempting to delete account")

            when (val result = AuthApiService.deleteAccount(token)) {
                is com.alfayedoficial.astagfirullah.data.model.DeleteAccountResult.Success -> {
                    // Clear all local auth data
                    AuthCacheService.getInstance().clearAuth()

                    logger.info("Account deleted successfully")
                    onAuthStateChanged?.invoke(AuthState.LoggedOut)
                    return DeleteAccountResult.Success
                }

                is com.alfayedoficial.astagfirullah.data.model.DeleteAccountResult.Error -> {
                    logger.warn("Delete account failed: ${result.message}")
                    return DeleteAccountResult.Error(result.message)
                }
            }
        } finally {
            isOperating.set(false)
        }
    }

    /**
     * Refreshes the current user's information from the server.
     *
     * @return The updated user or null if failed
     */
    fun refreshUser(): AuthUser? {
        val token = AuthCacheService.getInstance().getAuthToken() ?: return null

        return when (val result = AuthApiService.getMe(token)) {
            is AuthMeResult.Success -> {
                AuthCacheService.getInstance().updateUser(result.user)
                onAuthStateChanged?.invoke(AuthState.LoggedIn(result.user))
                result.user
            }

            is AuthMeResult.Error -> {
                logger.warn("Failed to refresh user: ${result.message}")
                // If unauthorized, clear auth
                if (result.message.contains("401") || result.message.contains("unauthorized", ignoreCase = true)) {
                    logout()
                }
                null
            }
        }
    }

    /**
     * Gets the currently logged in user.
     */
    fun getCurrentUser(): AuthUser? = AuthCacheService.getInstance().getUser()

    /**
     * Checks if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean = AuthCacheService.getInstance().isLoggedIn()

    /**
     * Gets the current auth token.
     */
    fun getAuthToken(): String? = AuthCacheService.getInstance().getAuthToken()

    /**
     * Gets the current auth state.
     */
    fun getCurrentAuthState(): AuthState {
        val user = getCurrentUser()
        return if (user != null) {
            AuthState.LoggedIn(user)
        } else {
            AuthState.LoggedOut
        }
    }

    // ==================== Validation Methods ====================

    /**
     * Validates an email address.
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email is required")
        }
        val emailRegex = Constants.EMAIL_REGEX.toRegex()
        if (!emailRegex.matches(email)) {
            return ValidationResult(false, "Invalid email format")
        }
        return ValidationResult(true)
    }

    /**
     * Validates a password (just checks not empty, for login).
     */
    fun validatePasswordNotEmpty(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(false, "Password is required")
        }
        return ValidationResult(true)
    }

    /**
     * Validates a password (for registration).
     * Only requires minimum length of 8 characters.
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(false, "Password is required")
        }
        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            return ValidationResult(false, "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters")
        }
        return ValidationResult(true)
    }

    /**
     * Validates a user name.
     */
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Name is required")
        }
        if (name.length > Constants.MAX_NAME_LENGTH) {
            return ValidationResult(false, "Name must be less than ${Constants.MAX_NAME_LENGTH} characters")
        }
        return ValidationResult(true)
    }

    /**
     * Validates password confirmation matches.
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        if (password != confirmPassword) {
            return ValidationResult(false, "Passwords do not match")
        }
        return ValidationResult(true)
    }

    /**
     * Calculates password strength.
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            return PasswordStrength.WEAK
        }

        var score = 0

        // Length bonus
        if (password.length >= 12) score++
        if (password.length >= 16) score++

        // Character variety
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}

/**
 * Password strength levels
 */
enum class PasswordStrength(val displayName: String, val level: Int) {
    WEAK("Weak", 1),
    MEDIUM("Medium", 2),
    STRONG("Strong", 3)
}