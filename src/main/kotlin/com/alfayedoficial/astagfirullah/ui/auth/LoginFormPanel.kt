package com.alfayedoficial.astagfirullah.ui.auth

import com.alfayedoficial.astagfirullah.core.ValidationUtils
import com.alfayedoficial.astagfirullah.ui.components.PasswordFieldWithToggle
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Login form panel with email and password fields.
 */
class LoginFormPanel(
    private val onLoginClicked: (email: String, password: String, rememberMe: Boolean) -> Unit,
    private val onRegisterClicked: () -> Unit
) : JPanel() {

    private val emailField = JBTextField(20)
    private val passwordField = PasswordFieldWithToggle()
    private val rememberMeCheckBox = JBCheckBox("Remember me")
    private val loginButton = JButton("Login")
    private val registerLink = JBLabel("<html><a href='#'>Don't have an account? Register</a></html>")

    private val emailErrorLabel = JBLabel()
    private val passwordErrorLabel = JBLabel()
    private val generalErrorLabel = JBLabel()

    private var isLoading = false

    init {
        layout = GridBagLayout()
        border = JBUI.Borders.empty(20)

        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(4)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
        }

        // Title
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        val titleLabel = JBLabel("Login to Your Account").apply {
            font = font.deriveFont(Font.BOLD, 18f)
        }
        add(titleLabel, gbc)

        // General error
        gbc.gridy++
        generalErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            isVisible = false
        }
        add(generalErrorLabel, gbc)

        // Email label
        gbc.gridy++
        gbc.gridwidth = 1
        add(JBLabel("Email:"), gbc)

        // Email field
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        emailField.apply {
            columns = 25
            addCaretListener { validateEmailField() }
        }
        add(emailField, gbc)

        // Email error
        gbc.gridy++
        emailErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(emailErrorLabel, gbc)

        // Password label
        gbc.gridy++
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        add(JBLabel("Password:"), gbc)

        // Password field
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        passwordField.columns = 25
        add(passwordField, gbc)

        // Password error
        gbc.gridy++
        passwordErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(passwordErrorLabel, gbc)

        // Remember me checkbox
        gbc.gridy++
        gbc.insets = JBUI.insets(8, 4, 4, 4)
        add(rememberMeCheckBox, gbc)

        // Login button
        gbc.gridy++
        gbc.insets = JBUI.insets(12, 4, 4, 4)
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.CENTER
        loginButton.apply {
            preferredSize = Dimension(120, 32)
            addActionListener { performLogin() }
        }
        add(loginButton, gbc)

        // Register link
        gbc.gridy++
        gbc.insets = JBUI.insets(16, 4, 4, 4)
        registerLink.apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                    onRegisterClicked()
                }
            })
        }
        add(registerLink, gbc)

        // Spacer
        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(JPanel(), gbc)

        // Enter key to login
        passwordField.addActionListener { performLogin() }
    }

    private fun validateEmailField() {
        val email = emailField.text
        if (email.isNotEmpty()) {
            val error = ValidationUtils.getEmailError(email)
            if (error != null) {
                showFieldError("email", error)
            } else {
                clearFieldError("email")
            }
        }
    }

    private fun performLogin() {
        if (isLoading) return

        clearErrors()

        val email = emailField.text.trim()
        val password = passwordField.text

        // Validate
        var hasError = false

        val emailError = ValidationUtils.getEmailError(email)
        if (emailError != null) {
            showFieldError("email", emailError)
            hasError = true
        }

        if (password.isEmpty()) {
            showFieldError("password", "Password is required")
            hasError = true
        }

        if (!hasError) {
            onLoginClicked(email, password, rememberMeCheckBox.isSelected)
        }
    }

    /**
     * Sets the loading state.
     */
    fun setLoading(loading: Boolean) {
        isLoading = loading
        loginButton.isEnabled = !loading
        loginButton.text = if (loading) "Logging in..." else "Login"
        emailField.isEnabled = !loading
        passwordField.isEnabled = !loading
        rememberMeCheckBox.isEnabled = !loading
    }

    /**
     * Shows a general error message.
     */
    fun showError(message: String) {
        generalErrorLabel.text = message
        generalErrorLabel.isVisible = true
    }

    /**
     * Shows a field-specific error.
     */
    fun showFieldError(field: String, message: String) {
        when (field.lowercase()) {
            "email" -> {
                emailErrorLabel.text = message
                emailErrorLabel.isVisible = true
            }
            "password" -> {
                passwordErrorLabel.text = message
                passwordErrorLabel.isVisible = true
            }
            else -> showError(message)
        }
    }

    /**
     * Clears a field-specific error.
     */
    private fun clearFieldError(field: String) {
        when (field.lowercase()) {
            "email" -> emailErrorLabel.isVisible = false
            "password" -> passwordErrorLabel.isVisible = false
        }
    }

    /**
     * Clears all error messages.
     */
    fun clearErrors() {
        generalErrorLabel.isVisible = false
        emailErrorLabel.isVisible = false
        passwordErrorLabel.isVisible = false
    }

    /**
     * Resets the form to initial state.
     */
    fun reset() {
        emailField.text = ""
        passwordField.clear()
        rememberMeCheckBox.isSelected = false
        clearErrors()
        setLoading(false)
    }

    /**
     * Pre-fills the email field.
     */
    fun setEmail(email: String) {
        emailField.text = email
    }

    /**
     * Requests focus on the email field.
     */
    override fun requestFocusInWindow(): Boolean {
        return emailField.requestFocusInWindow()
    }
}