package com.alfayedoficial.astagfirullah.ui.components

import com.alfayedoficial.astagfirullah.core.BrowserUtil
import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * An informational "Astagfirullah is everywhere" banner.
 *
 * This is cross-promotion of the developer's OWN apps — the Android and iOS builds and the
 * browser extensions — pointing at the developer's own websites. It is not a third-party ad
 * and serves no remote ad content: the two links are compile-time constants
 * ([Constants.WEBSITE_ASTAGHFIRULLAH], [Constants.WEBSITE_AFAPPS]). We deliberately do NOT
 * fabricate individual store URLs we were not given; the astaghfirullah site is the hub that
 * links out to every platform.
 */
object CrossPlatformBanner {

    /** Builds a fresh banner component. Not cached: Swing components cannot be reused across containers. */
    fun create(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                JBUI.Borders.empty(14, 18),
            )
        }

        panel.add(JBLabel("Astagfirullah is everywhere").apply {
            font = font.deriveFont(Font.BOLD, 14f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(4))
        panel.add(JBLabel("Keep your dhikr going beyond the IDE:").apply {
            foreground = JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(12))

        panel.add(platformRow("📱  Android & iOS apps", Constants.WEBSITE_ASTAGHFIRULLAH))
        panel.add(Box.createVerticalStrut(8))
        panel.add(platformRow("🧩  Chrome, Edge & Firefox extensions", Constants.WEBSITE_ASTAGHFIRULLAH))
        panel.add(Box.createVerticalStrut(8))
        panel.add(platformRow("🌐  More apps from AFApps", Constants.WEBSITE_AFAPPS))

        return panel
    }

    /** The pool of one-line product promotions used in the daily dhikr window. */
    private val PROMOS: List<Pair<String, String>> = listOf(
        "📱  Also on Android & iOS" to Constants.WEBSITE_ASTAGHFIRULLAH,
        "🧩  Get the browser extension" to Constants.WEBSITE_ASTAGHFIRULLAH,
        "🕌  Listen to the Quran anywhere" to Constants.WEBSITE_ASTAGHFIRULLAH,
        "🌐  More apps from AFApps" to Constants.WEBSITE_AFAPPS,
    )

    /**
     * A single, randomly chosen product promotion line with a clickable link — used under the
     * phrase in the daily dhikr window so a different product is surfaced each time.
     */
    fun randomPromoLine(): JComponent {
        val (label, url) = PROMOS[kotlin.random.Random.nextInt(PROMOS.size)]
        val row = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 0)).apply { isOpaque = false }
        row.add(JBLabel(label).apply { foreground = JBColor.GRAY })
        row.add(HyperlinkLabel(prettyHost(url)).apply {
            addHyperlinkListener { BrowserUtil.openUrl(url) }
        })
        return row
    }

    private fun platformRow(label: String, url: String): JComponent {
        val row = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentX = Component.LEFT_ALIGNMENT
        }
        row.add(JBLabel(label))
        row.add(Box.createHorizontalStrut(10))
        row.add(HyperlinkLabel(prettyHost(url)).apply {
            // Route through the plugin's own BrowserUtil (validates + logs) rather than
            // letting HyperlinkLabel open the URL itself, so link handling stays in one place.
            addHyperlinkListener { BrowserUtil.openUrl(url) }
        })
        return row
    }

    /** "https://astaghfirullah.4fdev.com/" -> "astaghfirullah.4fdev.com" for a tidier link label. */
    private fun prettyHost(url: String): String =
        url.removePrefix("https://").removePrefix("http://").trimEnd('/')
}
