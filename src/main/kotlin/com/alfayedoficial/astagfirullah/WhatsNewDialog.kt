package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.ui.components.CrossPlatformBanner
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.Action
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * "What's New in 3.0.0" — shown once per version when an existing user upgrades.
 *
 * Non-modal, like the daily dhikr dialog: it appears at IDE startup and must never block the
 * editor. Its content is static; the only interactive elements are the cross-platform links
 * in [CrossPlatformBanner].
 */
class WhatsNewDialog(project: Project?) : DialogWrapper(project, false) {

    init {
        title = "What's New in Astagfirullah ${Constants.PLUGIN_VERSION}"
        isModal = false
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(20, 24)
            preferredSize = Dimension(520, 460)
        }

        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        content.add(JBLabel("Astagfirullah 3.0.0").apply {
            font = font.deriveFont(Font.BOLD, 22f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        content.add(Box.createVerticalStrut(4))
        content.add(JBLabel("Now on the latest IDEs, with a daily moment of remembrance").apply {
            font = font.deriveFont(14f)
            foreground = com.intellij.ui.JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
        })
        content.add(Box.createVerticalStrut(18))

        content.add(
            highlights(
                "Runs on IntelliJ 2024.2 through 2026.2 — including the newest release",
                "New: a daily dhikr window when you open your IDE, closing itself after 5 seconds",
                "Turn the daily window off any time in Settings → Tools → Astagfirullah",
                "Right-to-left rendering for Arabic, Urdu and Farsi",
                "Faster, lighter sign-in — one less network round-trip",
                "Under-the-hood: modern build, continuous integration, and a verified compatibility range",
            )
        )

        content.add(Box.createVerticalStrut(20))
        content.add(CrossPlatformBanner.create().apply { alignmentX = Component.LEFT_ALIGNMENT })
        content.add(Box.createVerticalStrut(16))
        content.add(JBLabel("<html><i>Mention us in your good deeds 🤲</i></html>").apply {
            foreground = com.intellij.ui.JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
        })

        root.add(content, BorderLayout.CENTER)
        return root
    }

    private fun highlights(vararg lines: String): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
        }
        lines.forEach { line ->
            panel.add(JBLabel("<html>&bull;&nbsp;&nbsp;$line</html>").apply {
                alignmentX = Component.LEFT_ALIGNMENT
            })
            panel.add(Box.createVerticalStrut(8))
        }
        return panel
    }

    /** Single dismiss button, relabelled from the inherited "OK". */
    override fun createActions(): Array<Action> =
        arrayOf(okAction.apply { putValue(Action.NAME, "Got it") })
}
