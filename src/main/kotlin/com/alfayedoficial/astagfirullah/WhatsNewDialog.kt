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

        content.add(JBLabel("Astagfirullah ${Constants.PLUGIN_VERSION}").apply {
            font = font.deriveFont(Font.BOLD, 22f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        content.add(Box.createVerticalStrut(4))
        content.add(JBLabel(WhatsNew.TAGLINE).apply {
            font = font.deriveFont(14f)
            foreground = com.intellij.ui.JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
        })
        content.add(Box.createVerticalStrut(18))

        // Highlights come from the single shared WhatsNew source, so the dialog, the About
        // tab and the first-run wizard can never drift apart.
        content.add(highlights(*WhatsNew.HIGHLIGHTS.toTypedArray()))

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
