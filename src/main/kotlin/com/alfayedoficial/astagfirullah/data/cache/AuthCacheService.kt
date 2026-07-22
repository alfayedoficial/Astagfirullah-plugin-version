package com.alfayedoficial.astagfirullah.data.cache

import com.alfayedoficial.astagfirullah.data.model.AuthUser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Service for caching authentication state.
 * Persists login status, tokens, and user information.
 */
@Service(Service.Level.APP)
@State(
    name = "AstagfirullahAuth",
    storages = [Storage("astagfirullah-auth.xml")]
)
class AuthCacheService : PersistentStateComponent<AuthCacheService.AuthState> {

    private val logger = Logger.getInstance(AuthCacheService::class.java)
    private var myState = AuthState()

    companion object {
        @JvmStatic
        fun getInstance(): AuthCacheService {
            return ApplicationManager.getApplication().getService(AuthCacheService::class.java)
        }
    }

    /**
     * Authentication state persisted to disk
     */
    data class AuthState(
        var isLoggedIn: Boolean = false,
        var accessToken: String = "",
        var tokenType: String = "Bearer",
        var tokenExpiresAt: Long = 0L,
        var userId: Int = 0,
        var userName: String = "",
        var userEmail: String = "",
        var userAvatarUrl: String = "",
        var rememberMe: Boolean = false,
        var lastLoginTime: Long = 0L,
        var socialId: String = "" // Locally generated anonymous identifier
    )

    override fun getState(): AuthState = myState

    override fun loadState(state: AuthState) {
        XmlSerializerUtil.copyBean(state, myState)
        logger.debug("Auth state loaded: isLoggedIn=${myState.isLoggedIn}, user=${myState.userEmail}")
    }

    /**
     * Saves authentication data after successful login/register.
     *
     * @param user The authenticated user
     * @param token The access token
     * @param tokenType The token type (usually "Bearer")
     * @param rememberMe Whether to remember the login
     * @param expiresIn Optional token expiration time in seconds
     */
    fun saveAuth(
        user: AuthUser,
        token: String,
        tokenType: String = "Bearer",
        rememberMe: Boolean = false,
        expiresIn: Long? = null
    ) {
        myState.isLoggedIn = true
        myState.accessToken = token
        myState.tokenType = tokenType
        myState.userId = user.id
        myState.userName = user.name
        myState.userEmail = user.email
        myState.userAvatarUrl = user.avatarUrl ?: ""
        myState.rememberMe = rememberMe
        myState.lastLoginTime = System.currentTimeMillis()

        // Calculate token expiration if provided
        if (expiresIn != null && expiresIn > 0) {
            myState.tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000)
        } else {
            myState.tokenExpiresAt = 0L // No expiration
        }

        logger.info("Auth saved: user=${user.email}, rememberMe=$rememberMe")
    }

    /**
     * Clears all authentication data (logout).
     */
    fun clearAuth() {
        myState.isLoggedIn = false
        myState.accessToken = ""
        myState.tokenType = "Bearer"
        myState.tokenExpiresAt = 0L
        myState.userId = 0
        myState.userName = ""
        myState.userEmail = ""
        myState.userAvatarUrl = ""
        myState.socialId = ""
        // Keep rememberMe setting for next login
        myState.lastLoginTime = 0L

        logger.info("Auth cleared (logged out)")
    }

    /**
     * Checks if user is currently logged in.
     */
    fun isLoggedIn(): Boolean = myState.isLoggedIn && myState.accessToken.isNotEmpty()

    /**
     * Gets the authentication token if logged in.
     *
     * @return The Bearer token or null if not logged in
     */
    fun getAuthToken(): String? {
        if (!isLoggedIn()) return null
        if (isTokenExpired()) {
            logger.warn("Token expired, clearing auth")
            clearAuth()
            return null
        }
        return myState.accessToken
    }

    /**
     * Gets the full Authorization header value.
     *
     * @return "Bearer {token}" or null if not logged in
     */
    fun getAuthHeader(): String? {
        val token = getAuthToken() ?: return null
        return "${myState.tokenType} $token"
    }

    /**
     * Gets the current user if logged in.
     *
     * @return AuthUser or null if not logged in
     */
    fun getUser(): AuthUser? {
        if (!isLoggedIn()) return null
        return AuthUser(
            id = myState.userId,
            name = myState.userName,
            email = myState.userEmail,
            avatarUrl = myState.userAvatarUrl.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Gets the current user ID if logged in.
     */
    fun getUserId(): Int? = if (isLoggedIn()) myState.userId else null

    /**
     * Gets the current user's name if logged in.
     */
    fun getUserName(): String? = if (isLoggedIn()) myState.userName else null

    /**
     * Gets the current user's email if logged in.
     */
    fun getUserEmail(): String? = if (isLoggedIn()) myState.userEmail else null

    /**
     * Checks if the token has expired.
     */
    fun isTokenExpired(): Boolean {
        if (myState.tokenExpiresAt == 0L) return false // No expiration set
        return System.currentTimeMillis() > myState.tokenExpiresAt
    }

    /**
     * Checks if "Remember Me" was selected during login.
     */
    fun isRememberMe(): Boolean = myState.rememberMe

    /**
     * Gets the last login timestamp.
     */
    fun getLastLoginTime(): Long = myState.lastLoginTime

    /**
     * Updates user information (e.g., after profile edit).
     */
    fun updateUser(user: AuthUser) {
        if (!isLoggedIn()) return
        myState.userId = user.id
        myState.userName = user.name
        myState.userEmail = user.email
        myState.userAvatarUrl = user.avatarUrl ?: ""
        logger.debug("User info updated: ${user.email}")
    }

    /**
     * Saves the anonymous social ID.
     */
    fun saveSocialId(socialId: String) {
        myState.socialId = socialId
        logger.debug("Social ID saved: $socialId")
    }

    /**
     * Gets the stored anonymous social ID.
     */
    fun getSocialId(): String? = myState.socialId.takeIf { it.isNotEmpty() }

    /**
     * Checks if user registered via anonymous auth.
     */
    fun isSocialLogin(): Boolean = myState.socialId.isNotEmpty()
}