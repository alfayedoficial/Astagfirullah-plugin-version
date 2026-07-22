package com.alfayedoficial.astagfirullah.ui.auth

import com.alfayedoficial.astagfirullah.core.PasswordStrength
import com.alfayedoficial.astagfirullah.core.ValidationUtils
import com.alfayedoficial.astagfirullah.ui.components.PasswordFieldWithToggle
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Registration form panel with name, email, password, and confirm password fields.
 * Also supports quick anonymous registration (name only).
 */
class RegisterFormPanel(
    private val onRegisterClicked: (name: String, email: String, password: String) -> Unit,
    private val onQuickRegisterClicked: ((name: String) -> Unit)? = null,
    private val onLoginClicked: () -> Unit
) : JPanel() {

    private val nameField = JBTextField(20)
    private val emailField = JBTextField(20)
    private val passwordField = PasswordFieldWithToggle()
    private val confirmPasswordField = PasswordFieldWithToggle()
    private val registerButton = JButton("Create Account")
    private val quickRegisterButton = JButton("Quick Register (Name Only)")
    private val loginLink = JBLabel("<html><a href='#'>Already have an account? Login</a></html>")

    private val nameErrorLabel = JBLabel()
    private val emailErrorLabel = JBLabel()
    private val passwordErrorLabel = JBLabel()
    private val confirmPasswordErrorLabel = JBLabel()
    private val generalErrorLabel = JBLabel()

    // Password strength indicator
    private val passwordStrengthBar = JProgressBar(0, 100)
    private val passwordStrengthLabel = JBLabel()

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
        val titleLabel = JBLabel("Create Account").apply {
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

        // Name label
        gbc.gridy++
        gbc.gridwidth = 1
        add(JBLabel("Name:"), gbc)

        // Name field
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        nameField.apply {
            columns = 25
            addCaretListener { validateNameField() }
        }
        add(nameField, gbc)

        // Name error
        gbc.gridy++
        nameErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(nameErrorLabel, gbc)

        // Email label
        gbc.gridy++
        gbc.gridwidth = 1
        gbc.weightx = 0.0
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
        passwordField.apply {
            columns = 25
            getPasswordField().document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updatePasswordStrength()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updatePasswordStrength()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updatePasswordStrength()
            })
        }
        add(passwordField, gbc)

        // Password strength bar
        gbc.gridy++
        gbc.insets = JBUI.insets(2, 4, 2, 4)
        passwordStrengthBar.apply {
            preferredSize = Dimension(200, 4)
            isStringPainted = false
            isVisible = false
        }
        add(passwordStrengthBar, gbc)

        // Password strength label
        gbc.gridy++
        passwordStrengthLabel.apply {
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(passwordStrengthLabel, gbc)

        // Password error
        gbc.gridy++
        gbc.insets = JBUI.insets(4)
        passwordErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(passwordErrorLabel, gbc)

        // Confirm password label
        gbc.gridy++
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        add(JBLabel("Confirm Password:"), gbc)

        // Confirm password field
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        confirmPasswordField.apply {
            columns = 25
            getPasswordField().document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = validatePasswordMatch()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = validatePasswordMatch()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = validatePasswordMatch()
            })
        }
        add(confirmPasswordField, gbc)

        // Confirm password error
        gbc.gridy++
        confirmPasswordErrorLabel.apply {
            foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            font = font.deriveFont(11f)
            isVisible = false
        }
        add(confirmPasswordErrorLabel, gbc)

        // Buttons panel
        gbc.gridy++
        gbc.insets = JBUI.insets(12, 4, 4, 4)
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.CENTER

        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))

        registerButton.apply {
            preferredSize = Dimension(140, 32)
            addActionListener { performRegister() }
        }
        buttonsPanel.add(registerButton)

        // Quick register button (only if callback is provided)
        if (onQuickRegisterClicked != null) {
            quickRegisterButton.apply {
                preferredSize = Dimension(180, 32)
                toolTipText = "Register quickly with just your name using anonymous authentication"
                addActionListener { performQuickRegister() }
            }
            buttonsPanel.add(quickRegisterButton)
        }

        add(buttonsPanel, gbc)

        // Or separator for quick register
        if (onQuickRegisterClicked != null) {
            gbc.gridy++
            gbc.insets = JBUI.insets(8, 4, 8, 4)
            add(JBLabel("— or register with full details above —").apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(11f)
            }, gbc)
        }

        // Login link
        gbc.gridy++
        gbc.insets = JBUI.insets(16, 4, 4, 4)
        loginLink.apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                    onLoginClicked()
                }
            })
        }
        add(loginLink, gbc)

        // Spacer
        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(JPanel(), gbc)

        // Enter key to register
        confirmPasswordField.addActionListener { performRegister() }
    }

    private fun validateNameField() {
        val name = nameField.text
        if (name.isNotEmpty()) {
            val error = ValidationUtils.getNameError(name)
            if (error != null) {
                showFieldError("name", error)
            } else {
                clearFieldError("name")
            }
        }
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

    private fun updatePasswordStrength() {
        val password = passwordField.text
        if (password.isEmpty()) {
            passwordStrengthBar.isVisible = false
            passwordStrengthLabel.isVisible = false
            return
        }

        val strength = ValidationUtils.getPasswordStrength(password)

        passwordStrengthBar.apply {
            isVisible = true
            value = strength.progressValue
            foreground = strength.color
        }

        passwordStrengthLabel.apply {
            isVisible = true
            text = "Password strength: ${strength.displayName}"
            foreground = strength.color
        }

        // Also validate password requirements
        val error = ValidationUtils.getPasswordError(password)
        if (error != null) {
            showFieldError("password", error)
        } else {
            clearFieldError("password")
        }
    }

    private fun validatePasswordMatch() {
        val password = passwordField.text
        val confirmPassword = confirmPasswordField.text

        if (confirmPassword.isNotEmpty()) {
            if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
                showFieldError("confirmPassword", "Passwords do not match")
            } else {
                clearFieldError("confirmPassword")
            }
        }
    }

    private fun performRegister() {
        if (isLoading) return

        clearErrors()

        val name = nameField.text.trim()
        val email = emailField.text.trim()
        val password = passwordField.text
        val confirmPassword = confirmPasswordField.text

        // Validate
        var hasError = false

        val nameError = ValidationUtils.getNameError(name)
        if (nameError != null) {
            showFieldError("name", nameError)
            hasError = true
        }

        val emailError = ValidationUtils.getEmailError(email)
        if (emailError != null) {
            showFieldError("email", emailError)
            hasError = true
        }

        val passwordError = ValidationUtils.getPasswordError(password)
        if (passwordError != null) {
            showFieldError("password", passwordError)
            hasError = true
        }

        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            showFieldError("confirmPassword", "Passwords do not match")
            hasError = true
        }

        if (!hasError) {
            onRegisterClicked(name, email, password)
        }
    }

    /**
     * Performs quick anonymous registration (name only).
     */
    private fun performQuickRegister() {
        if (isLoading) return

        clearErrors()

        val name = nameField.text.trim()

        // Only validate name for quick register
        val nameError = ValidationUtils.getNameError(name)
        if (nameError != null) {
            showFieldError("name", nameError)
            return
        }

        // Show warning alert about guest account limitations
        val result = JOptionPane.showConfirmDialog(
            this,
            "<html><b>Guest Account Warning</b><br><br>" +
                    "You are about to create a guest account.<br><br>" +
                    "<font color='#D32F2F'><b>Important:</b></font><br>" +
                    "- Your data may be <b>lost</b> when you logout<br>" +
                    "- Your data may be <b>lost</b> when the session expires<br>" +
                    "- You <b>cannot recover</b> a guest account<br><br>" +
                    "For a permanent account, use the full registration<br>" +
                    "with email and password above.<br><br>" +
                    "Do you want to continue as a guest?</html>",
            "Guest Account",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            onQuickRegisterClicked?.invoke(name)
        }
    }

    /**
     * Sets the loading state.
     */
    fun setLoading(loading: Boolean) {
        isLoading = loading
        registerButton.isEnabled = !loading
        registerButton.text = if (loading) "Creating account..." else "Create Account"
        quickRegisterButton.isEnabled = !loading
        quickRegisterButton.text = if (loading) "Registering..." else "Quick Register (Name Only)"
        nameField.isEnabled = !loading
        emailField.isEnabled = !loading
        passwordField.isEnabled = !loading
        confirmPasswordField.isEnabled = !loading
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
            "name" -> {
                nameErrorLabel.text = message
                nameErrorLabel.isVisible = true
            }
            "email" -> {
                emailErrorLabel.text = message
                emailErrorLabel.isVisible = true
            }
            "password" -> {
                passwordErrorLabel.text = message
                passwordErrorLabel.isVisible = true
            }
            "confirmpassword", "password_confirmation" -> {
                confirmPasswordErrorLabel.text = message
                confirmPasswordErrorLabel.isVisible = true
            }
            else -> showError(message)
        }
    }

    /**
     * Clears a field-specific error.
     */
    private fun clearFieldError(field: String) {
        when (field.lowercase()) {
            "name" -> nameErrorLabel.isVisible = false
            "email" -> emailErrorLabel.isVisible = false
            "password" -> passwordErrorLabel.isVisible = false
            "confirmpassword", "password_confirmation" -> confirmPasswordErrorLabel.isVisible = false
        }
    }

    /**
     * Clears all error messages.
     */
    fun clearErrors() {
        generalErrorLabel.isVisible = false
        nameErrorLabel.isVisible = false
        emailErrorLabel.isVisible = false
        passwordErrorLabel.isVisible = false
        confirmPasswordErrorLabel.isVisible = false
    }

    /**
     * Resets the form to initial state.
     */
    fun reset() {
        nameField.text = ""
        emailField.text = ""
        passwordField.clear()
        confirmPasswordField.clear()
        passwordStrengthBar.isVisible = false
        passwordStrengthLabel.isVisible = false
        clearErrors()
        setLoading(false)
    }

    /**
     * Requests focus on the name field.
     */
    override fun requestFocusInWindow(): Boolean {
        return nameField.requestFocusInWindow()
    }
}