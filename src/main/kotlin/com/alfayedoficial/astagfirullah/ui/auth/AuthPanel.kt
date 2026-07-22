package com.alfayedoficial.astagfirullah.ui.auth

import com.alfayedoficial.astagfirullah.data.cache.AuthCacheService
import com.alfayedoficial.astagfirullah.data.model.AuthUser
import com.alfayedoficial.astagfirullah.data.sync.AuthSyncService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Main authentication panel that manages login, register, and profile views.
 * Uses CardLayout to switch between different auth states.
 * Includes a loading overlay for async operations.
 */
class AuthPanel : JPanel(BorderLayout()) {

    companion object {
        private const val CARD_LOGIN = "login"
        private const val CARD_REGISTER = "register"
        private const val CARD_PROFILE = "profile"
    }

    private val contentPanel = JPanel(CardLayout())
    private val cardLayout: CardLayout
        get() = contentPanel.layout as CardLayout

    private lateinit var loginPanel: LoginFormPanel
    private lateinit var registerPanel: RegisterFormPanel
    private lateinit var profilePanel: UserProfilePanel

    // Loading overlay components
    private val loadingOverlay = LoadingOverlayPanel()
    private val layeredPane = JLayeredPane()

    private var onAuthSuccess: ((AuthUser) -> Unit)? = null

    init {
        createPanels()
        setupLayeredPane()
        setupAuthStateListener()

        // Show appropriate initial panel
        if (AuthCacheService.getInstance().isLoggedIn()) {
            val user = AuthCacheService.getInstance().getUser()
            if (user != null) {
                showProfile(user)
            } else {
                showLogin()
            }
        } else {
            showLogin()
        }
    }

    private fun setupLayeredPane() {
        // Use a custom layout to make components fill the layered pane
        layeredPane.layout = object : LayoutManager {
            override fun addLayoutComponent(name: String?, comp: Component?) {}
            override fun removeLayoutComponent(comp: Component?) {}
            override fun preferredLayoutSize(parent: Container?): Dimension = contentPanel.preferredSize
            override fun minimumLayoutSize(parent: Container?): Dimension = contentPanel.minimumSize
            override fun layoutContainer(parent: Container?) {
                val bounds = layeredPane.bounds
                contentPanel.setBounds(0, 0, bounds.width, bounds.height)
                loadingOverlay.setBounds(0, 0, bounds.width, bounds.height)
            }
        }

        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER)
        layeredPane.add(loadingOverlay, JLayeredPane.POPUP_LAYER)

        add(layeredPane, BorderLayout.CENTER)
    }

    private fun createPanels() {
        // Login panel
        loginPanel = LoginFormPanel(
            onLoginClicked = { email, password, rememberMe ->
                performLogin(email, password, rememberMe)
            },
            onRegisterClicked = { showRegister() }
        )
        contentPanel.add(loginPanel, CARD_LOGIN)

        // Register panel
        registerPanel = RegisterFormPanel(
            onRegisterClicked = { name, email, password ->
                performRegister(name, email, password)
            },
            onQuickRegisterClicked = { name ->
                performQuickRegister(name)
            },
            onLoginClicked = { showLogin() }
        )
        contentPanel.add(registerPanel, CARD_REGISTER)

        // Profile panel
        profilePanel = UserProfilePanel(
            onLogoutClicked = { performLogout() },
            onDeleteAccountClicked = { performDeleteAccount() }
        )
        contentPanel.add(profilePanel, CARD_PROFILE)
    }

    private fun setupAuthStateListener() {
        // Store previous listener if any and chain with our listener
        val previousListener = AuthSyncService.getInstance().onAuthStateChanged

        AuthSyncService.getInstance().onAuthStateChanged = { state ->
            // Call previous listener first
            previousListener?.invoke(state)
            SwingUtilities.invokeLater {
                when (state) {
                    is AuthSyncService.AuthState.LoggedIn -> {
                        showProfile(state.user)
                        onAuthSuccess?.invoke(state.user)
                    }
                    is AuthSyncService.AuthState.LoggedOut -> {
                        showLogin()
                    }
                    is AuthSyncService.AuthState.Loading -> {
                        // Keep current panel but show loading state
                    }
                    is AuthSyncService.AuthState.Error -> {
                        // Error is handled in the form panels
                    }
                }
            }
        }
    }

    private fun performLogin(email: String, password: String, rememberMe: Boolean) {
        loginPanel.setLoading(true)
        loginPanel.clearErrors()
        showLoadingOverlay("Signing in...")

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = AuthSyncService.getInstance().login(email, password, rememberMe)

            SwingUtilities.invokeLater {
                hideLoadingOverlay()
                loginPanel.setLoading(false)

                when (result) {
                    is AuthSyncService.LoginResult.Success -> {
                        // Auth state listener will handle showing profile
                    }
                    is AuthSyncService.LoginResult.Error -> {
                        if (result.fieldErrors != null) {
                            result.fieldErrors.forEach { (field, message) ->
                                loginPanel.showFieldError(field, message)
                            }
                        } else {
                            loginPanel.showError(result.message)
                        }
                    }
                }
            }
        }
    }

    private fun performRegister(name: String, email: String, password: String) {
        registerPanel.setLoading(true)
        registerPanel.clearErrors()
        showLoadingOverlay("Creating account...")

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = AuthSyncService.getInstance().register(name, email, password)

            SwingUtilities.invokeLater {
                hideLoadingOverlay()
                registerPanel.setLoading(false)

                when (result) {
                    is AuthSyncService.RegisterResult.Success -> {
                        // Auth state listener will handle showing profile
                    }
                    is AuthSyncService.RegisterResult.Error -> {
                        if (result.fieldErrors != null) {
                            result.fieldErrors.forEach { (field, message) ->
                                registerPanel.showFieldError(field, message)
                            }
                        } else {
                            registerPanel.showError(result.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs quick anonymous registration (name only).
     */
    private fun performQuickRegister(name: String) {
        registerPanel.setLoading(true)
        registerPanel.clearErrors()
        showLoadingOverlay("Setting up your account...")

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = AuthSyncService.getInstance().registerAnonymously(name)

            SwingUtilities.invokeLater {
                hideLoadingOverlay()
                registerPanel.setLoading(false)

                when (result) {
                    is AuthSyncService.RegisterResult.Success -> {
                        // Auth state listener will handle showing profile
                    }
                    is AuthSyncService.RegisterResult.Error -> {
                        if (result.fieldErrors != null) {
                            result.fieldErrors.forEach { (field, message) ->
                                registerPanel.showFieldError(field, message)
                            }
                        } else {
                            registerPanel.showError(result.message)
                        }
                    }
                }
            }
        }
    }

    private fun performLogout() {
        profilePanel.setLoading(true)
        showLoadingOverlay("Signing out...")

        ApplicationManager.getApplication().executeOnPooledThread {
            AuthSyncService.getInstance().logout()

            SwingUtilities.invokeLater {
                hideLoadingOverlay()
                profilePanel.setLoading(false)
                profilePanel.clear()
                showLogin()
            }
        }
    }

    private fun performDeleteAccount() {
        profilePanel.setLoading(true, isDeleting = true)
        showLoadingOverlay("Deleting account...")

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = AuthSyncService.getInstance().deleteAccount()

            SwingUtilities.invokeLater {
                hideLoadingOverlay()
                profilePanel.setLoading(false, isDeleting = true)

                when (result) {
                    is AuthSyncService.DeleteAccountResult.Success -> {
                        profilePanel.clear()
                        showLogin()
                        // Show success message
                        JOptionPane.showMessageDialog(
                            this,
                            "Your account has been successfully deleted.",
                            "Account Deleted",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                    is AuthSyncService.DeleteAccountResult.Error -> {
                        JOptionPane.showMessageDialog(
                            this,
                            "Failed to delete account: ${result.message}",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }
    }

    /**
     * Shows the loading overlay with a message.
     */
    private fun showLoadingOverlay(message: String) {
        loadingOverlay.setMessage(message)
        loadingOverlay.isVisible = true
        layeredPane.revalidate()
        layeredPane.repaint()
    }

    /**
     * Hides the loading overlay.
     */
    private fun hideLoadingOverlay() {
        loadingOverlay.isVisible = false
        layeredPane.revalidate()
        layeredPane.repaint()
    }

    /**
     * Shows the login panel.
     */
    fun showLogin() {
        loginPanel.reset()
        cardLayout.show(contentPanel, CARD_LOGIN)
        loginPanel.requestFocusInWindow()
    }

    /**
     * Shows the registration panel.
     */
    fun showRegister() {
        registerPanel.reset()
        cardLayout.show(contentPanel, CARD_REGISTER)
        registerPanel.requestFocusInWindow()
    }

    /**
     * Shows the profile panel with user information.
     */
    fun showProfile(user: AuthUser) {
        profilePanel.setUser(user)
        cardLayout.show(contentPanel, CARD_PROFILE)
    }

    /**
     * Sets a callback for successful authentication.
     */
    fun setOnAuthSuccess(callback: (AuthUser) -> Unit) {
        onAuthSuccess = callback
    }

    /**
     * Checks if user is currently logged in and refreshes the view.
     */
    fun refreshState() {
        val user = AuthCacheService.getInstance().getUser()
        if (user != null) {
            showProfile(user)
        } else {
            showLogin()
        }
    }

    /**
     * Returns true if user is currently logged in.
     */
    fun isLoggedIn(): Boolean = AuthCacheService.getInstance().isLoggedIn()
}

/**
 * Semi-transparent loading overlay panel with progress indicator.
 */
private class LoadingOverlayPanel : JPanel() {

    private val messageLabel = JBLabel()
    private val progressBar = JProgressBar()

    init {
        isOpaque = false
        isVisible = false
        layout = GridBagLayout()

        val gbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            insets = JBUI.insets(8)
        }

        // Container panel with background
        val containerPanel = JPanel(GridBagLayout()).apply {
            background = JBColor(Color(255, 255, 255, 240), Color(60, 63, 65, 240))
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                JBUI.Borders.empty(20, 30)
            )
        }

        val innerGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            insets = JBUI.insets(4)
        }

        // Progress bar (indeterminate)
        progressBar.apply {
            isIndeterminate = true
            preferredSize = Dimension(200, 4)
        }
        containerPanel.add(progressBar, innerGbc)

        // Message label
        innerGbc.gridy++
        innerGbc.insets = JBUI.insets(12, 4, 4, 4)
        messageLabel.apply {
            font = font.deriveFont(Font.PLAIN, 14f)
            horizontalAlignment = SwingConstants.CENTER
        }
        containerPanel.add(messageLabel, innerGbc)

        add(containerPanel, gbc)
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }

    override fun paintComponent(g: Graphics) {
        // Draw semi-transparent background
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Semi-transparent overlay
        g2d.color = JBColor(Color(255, 255, 255, 180), Color(0, 0, 0, 150))
        g2d.fillRect(0, 0, width, height)

        super.paintComponent(g)
    }
}