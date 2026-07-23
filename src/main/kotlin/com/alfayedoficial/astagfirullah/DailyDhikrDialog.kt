package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.ComponentOrientation
import java.awt.Dimension
import java.awt.Font
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.Timer

/**
 * A small, self-dismissing dialog shown once a day when the IDE opens, displaying a
 * single random remembrance phrase (tasbih) with a visible countdown.
 *
 * Deliberately **non-modal**: it appears during IDE startup, and a modal dialog there
 * would block the user from touching their editor until it closed. It also closes itself
 * after [Constants.DAILY_DHIKR_COUNTDOWN_SECONDS] seconds, so it never demands
 * interaction — the user can keep working and let it disappear.
 *
 * The countdown uses a Swing [Timer], which fires on the EDT, so the label may be updated
 * directly from the tick handler without any thread hand-off.
 */
class DailyDhikrDialog(
    project: Project?,
    private val phrase: String,
    private val language: String,
) : DialogWrapper(project, false) {

    private val logger = Logger.getInstance(DailyDhikrDialog::class.java)

    private var secondsLeft = Constants.DAILY_DHIKR_COUNTDOWN_SECONDS
    private var countdownLabel: JBLabel? = null

    /**
     * Held so it can be stopped in [dispose].
     *
     * A Swing Timer keeps a strong reference to its listener and goes on firing after the
     * window is gone unless it is explicitly stopped, which would leak the dialog and tick
     * against a disposed component.
     */
    private var countdownTimer: Timer? = null

    init {
        title = TranslatePhrases.selectTranslateTitle()
        isModal = false
        init()
        startCountdown()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(24, 32)
            preferredSize = Dimension(460, 190)
        }

        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        // The phrase itself, wrapped in HTML so long supplications wrap instead of
        // being clipped by the fixed dialog width.
        val phraseLabel = JBLabel(
            "<html><div style='text-align: center; width: 380px;'>$phrase</div></html>",
            SwingConstants.CENTER,
        ).apply {
            font = font.deriveFont(Font.BOLD, 20f)
            alignmentX = Component.CENTER_ALIGNMENT
            // Arabic, Urdu and Farsi must lay out right-to-left or the phrase renders
            // with its punctuation and diacritics on the wrong side.
            componentOrientation = orientationFor(language)
        }

        val countdown = JBLabel(countdownText(secondsLeft), SwingConstants.CENTER).apply {
            font = font.deriveFont(11f)
            foreground = JBColor.GRAY
            alignmentX = Component.CENTER_ALIGNMENT
        }
        countdownLabel = countdown

        // A random product promotion under the phrase — a different one each time the window
        // appears (owner's request). Cross-promotion of the developer's own apps.
        val promo = com.alfayedoficial.astagfirullah.ui.components.CrossPlatformBanner.randomPromoLine().apply {
            alignmentX = Component.CENTER_ALIGNMENT
        }

        content.add(Box.createVerticalGlue())
        content.add(phraseLabel)
        content.add(Box.createVerticalStrut(18))
        content.add(promo)
        content.add(Box.createVerticalStrut(16))
        content.add(countdown)
        content.add(Box.createVerticalGlue())

        root.add(content, BorderLayout.CENTER)
        return root
    }

    /**
     * A single dismiss button, relabelled from the inherited "Cancel".
     *
     * There is nothing to cancel here — the dialog only shows a phrase and closes itself —
     * and "Cancel" would imply the user is rejecting something.
     */
    override fun createActions(): Array<javax.swing.Action> =
        arrayOf(cancelAction.apply { putValue(javax.swing.Action.NAME, "Close") })

    private fun startCountdown() {
        val timer = Timer(Constants.DAILY_DHIKR_TICK_MS) {
            secondsLeft--
            if (secondsLeft <= 0) {
                // close() routes through dispose(), which stops the timer.
                close(CANCEL_EXIT_CODE)
            } else {
                countdownLabel?.text = countdownText(secondsLeft)
            }
        }
        timer.isRepeats = true
        countdownTimer = timer
        timer.start()
    }

    override fun dispose() {
        // Stop before super.dispose(): once the window is disposed a pending tick would
        // be operating on dead UI.
        countdownTimer?.stop()
        countdownTimer = null
        logger.debug("Daily dhikr dialog disposed")
        super.dispose()
    }

    private fun countdownText(seconds: Int): String =
        if (seconds == 1) "Closing in 1 second…" else "Closing in $seconds seconds…"

    private fun orientationFor(language: String): ComponentOrientation =
        if (language in RTL_LANGUAGES) {
            ComponentOrientation.RIGHT_TO_LEFT
        } else {
            ComponentOrientation.LEFT_TO_RIGHT
        }

    companion object {
        /** Language labels, as stored in settings, that render right-to-left. */
        private val RTL_LANGUAGES = setOf("العربية", "أردو", "فارسى")
    }
}
