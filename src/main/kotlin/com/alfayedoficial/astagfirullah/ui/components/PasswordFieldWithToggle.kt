package com.alfayedoficial.astagfirullah.ui.components

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

/**
 * A password field with a visibility toggle button.
 * Shows/hides the password when the toggle is clicked.
 */
class PasswordFieldWithToggle : JPanel(BorderLayout()) {

    private val passwordField = JBPasswordField()
    private val toggleButton = JButton()
    private var isPasswordVisible = false

    /**
     * The password text.
     */
    var text: String
        get() = String(passwordField.password)
        set(value) {
            passwordField.text = value
        }

    /**
     * The password field's columns (width hint).
     */
    var columns: Int
        get() = passwordField.columns
        set(value) {
            passwordField.columns = value
        }

    init {
        // Configure password field
        passwordField.echoChar = '\u2022' // Bullet character

        // Configure toggle button
        toggleButton.apply {
            icon = AllIcons.Actions.Show
            toolTipText = "Show password"
            isFocusable = false
            isBorderPainted = false
            isContentAreaFilled = false
            preferredSize = JBUI.size(24, 24)
            addActionListener {
                togglePasswordVisibility()
            }
        }

        // Layout
        add(passwordField, BorderLayout.CENTER)
        add(toggleButton, BorderLayout.EAST)
    }

    /**
     * Toggles the password visibility.
     */
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            passwordField.echoChar = 0.toChar() // Show password
            toggleButton.icon = AllIcons.Actions.ToggleVisibility
            toggleButton.toolTipText = "Hide password"
        } else {
            passwordField.echoChar = '\u2022' // Hide password
            toggleButton.icon = AllIcons.Actions.Show
            toggleButton.toolTipText = "Show password"
        }
    }

    /**
     * Adds an action listener to the password field.
     */
    fun addActionListener(listener: ActionListener) {
        passwordField.addActionListener(listener)
    }

    /**
     * Gets the password field component (for focus requests, etc.).
     */
    fun getPasswordField(): JBPasswordField = passwordField

    /**
     * Sets whether the field is enabled.
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        passwordField.isEnabled = enabled
        toggleButton.isEnabled = enabled
    }

    /**
     * Requests focus on the password field.
     */
    override fun requestFocusInWindow(): Boolean {
        return passwordField.requestFocusInWindow()
    }

    /**
     * Clears the password field.
     */
    fun clear() {
        passwordField.text = ""
    }
}