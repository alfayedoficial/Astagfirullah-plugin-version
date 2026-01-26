package com.alfayedoficial.astagfirullah.ui.auth

import com.alfayedoficial.astagfirullah.data.model.AuthUser
import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Panel displaying the logged-in user's profile information.
 */
class UserProfilePanel(
    private val onLogoutClicked: () -> Unit,
    private val onDeleteAccountClicked: (() -> Unit)? = null
) : JPanel() {

    private val avatarLabel = JBLabel()
    private val nameLabel = JBLabel()
    private val emailLabel = JBLabel()
    private val logoutButton = JButton("Logout")
    private val deleteAccountButton = JButton("Delete Account")

    private var isLoading = false

    init {
        layout = GridBagLayout()
        border = JBUI.Borders.empty(20)

        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(4)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.CENTER
        }

        // Avatar
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        avatarLabel.apply {
            icon = AllIcons.General.User
            horizontalAlignment = SwingConstants.CENTER
            preferredSize = JBUI.size(64, 64)
        }
        add(avatarLabel, gbc)

        // Welcome text
        gbc.gridy++
        gbc.insets = JBUI.insets(16, 4, 4, 4)
        val welcomeLabel = JBLabel("Welcome back!").apply {
            font = font.deriveFont(Font.BOLD, 16f)
            horizontalAlignment = SwingConstants.CENTER
        }
        add(welcomeLabel, gbc)

        // Name
        gbc.gridy++
        gbc.insets = JBUI.insets(8, 4, 2, 4)
        nameLabel.apply {
            font = font.deriveFont(Font.BOLD, 14f)
            horizontalAlignment = SwingConstants.CENTER
        }
        add(nameLabel, gbc)

        // Email
        gbc.gridy++
        gbc.insets = JBUI.insets(2, 4, 4, 4)
        emailLabel.apply {
            foreground = JBColor.GRAY
            horizontalAlignment = SwingConstants.CENTER
        }
        add(emailLabel, gbc)

        // Divider
        gbc.gridy++
        gbc.insets = JBUI.insets(16, 4, 16, 4)
        add(JSeparator(), gbc)

        // Account info section
        gbc.gridy++
        gbc.insets = JBUI.insets(4)
        val infoPanel = createInfoPanel()
        add(infoPanel, gbc)

        // Buttons panel
        gbc.gridy++
        gbc.insets = JBUI.insets(20, 4, 4, 4)
        gbc.fill = GridBagConstraints.NONE

        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))

        logoutButton.apply {
            preferredSize = Dimension(100, 32)
            addActionListener { performLogout() }
        }
        buttonsPanel.add(logoutButton)

        add(buttonsPanel, gbc)

        // Delete account section (danger zone)
        if (onDeleteAccountClicked != null) {
            gbc.gridy++
            gbc.insets = JBUI.insets(30, 4, 4, 4)
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(createDangerZonePanel(), gbc)
        }

        // Spacer
        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(JPanel(), gbc)
    }

    private fun createDangerZonePanel(): JPanel {
        return JPanel(GridBagLayout()).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor(Color(244, 67, 54), Color(244, 67, 54))),
                JBUI.Borders.empty(12)
            )
            background = JBColor(Color(255, 235, 238), Color(75, 45, 45))

            val gbc = GridBagConstraints().apply {
                insets = JBUI.insets(4)
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.WEST
            }

            gbc.gridx = 0
            gbc.gridy = 0
            add(JBLabel("Danger Zone").apply {
                font = font.deriveFont(Font.BOLD)
                foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(8, 4, 12, 4)
            add(JBLabel("<html><small>Deleting your account will permanently remove all your data<br>and statistics. This action cannot be undone.</small></html>").apply {
                foreground = JBColor(Color(183, 28, 28), Color(239, 154, 154))
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(4)
            gbc.fill = GridBagConstraints.NONE
            gbc.anchor = GridBagConstraints.CENTER

            deleteAccountButton.apply {
                preferredSize = Dimension(140, 32)
                foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
                addActionListener { performDeleteAccount() }
            }
            add(deleteAccountButton, gbc)
        }
    }

    private fun createInfoPanel(): JPanel {
        return JPanel(GridBagLayout()).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(12)
            )
            background = JBColor(Color(245, 245, 245), Color(60, 63, 65))

            val gbc = GridBagConstraints().apply {
                insets = JBUI.insets(4)
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.WEST
            }

            gbc.gridx = 0
            gbc.gridy = 0
            add(JBLabel("Your account is active and synchronized.").apply {
                foreground = JBColor(Color(76, 175, 80), Color(76, 175, 80))
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(8, 4, 4, 4)
            add(JBLabel("<html><small>Your progress is being tracked and synced to the leaderboard.</small></html>").apply {
                foreground = JBColor.GRAY
            }, gbc)
        }
    }

    private fun performLogout() {
        if (isLoading) return

        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        if (confirm == JOptionPane.YES_OPTION) {
            onLogoutClicked()
        }
    }

    private fun performDeleteAccount() {
        if (isLoading) return

        // First confirmation
        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete your account?\n\nThis will permanently delete:\n" +
                    "- Your profile and login credentials\n" +
                    "- All your statistics and progress\n" +
                    "- Your leaderboard ranking\n\n" +
                    "This action CANNOT be undone!",
            "Delete Account",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )

        if (confirm == JOptionPane.YES_OPTION) {
            // Second confirmation with typing
            val input = JOptionPane.showInputDialog(
                this,
                "To confirm deletion, type 'DELETE' below:",
                "Confirm Account Deletion",
                JOptionPane.WARNING_MESSAGE
            )

            if (input?.uppercase() == "DELETE") {
                onDeleteAccountClicked?.invoke()
            } else if (input != null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Account deletion cancelled. You typed: $input",
                    "Deletion Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }
    }

    /**
     * Sets the user information to display.
     */
    fun setUser(user: AuthUser) {
        nameLabel.text = user.name
        emailLabel.text = user.email

        // Update avatar if URL provided (placeholder for future implementation)
        avatarLabel.icon = AllIcons.General.User
    }

    /**
     * Sets the loading state.
     */
    fun setLoading(loading: Boolean, isDeleting: Boolean = false) {
        isLoading = loading
        logoutButton.isEnabled = !loading
        deleteAccountButton.isEnabled = !loading

        if (isDeleting) {
            deleteAccountButton.text = if (loading) "Deleting..." else "Delete Account"
        } else {
            logoutButton.text = if (loading) "Logging out..." else "Logout"
        }
    }

    /**
     * Clears the displayed user information.
     */
    fun clear() {
        nameLabel.text = ""
        emailLabel.text = ""
        avatarLabel.icon = AllIcons.General.User
    }
}