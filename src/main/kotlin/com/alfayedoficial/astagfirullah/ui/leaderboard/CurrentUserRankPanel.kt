package com.alfayedoficial.astagfirullah.ui.leaderboard

import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.*
import java.text.NumberFormat
import java.util.*
import javax.swing.*

/**
 * Panel highlighting the current user's rank in the leaderboard.
 * Displayed at the bottom of the leaderboard when the user is logged in.
 */
class CurrentUserRankPanel : JPanel(BorderLayout()) {

    private val rankLabel = JBLabel()
    private val titleLabel = JBLabel("Your Rank")
    private val countLabel = JBLabel()
    private val rankChangeLabel = JBLabel()

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US)
    }

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
            JBUI.Borders.empty(12)
        )
        background = JBColor(Color(232, 245, 233), Color(30, 60, 35))

        // Left: Your Rank badge
        val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            isOpaque = false
        }

        val rankBadge = JPanel(BorderLayout()).apply {
            background = JBColor(Color(76, 175, 80), Color(56, 142, 60))
            border = JBUI.Borders.empty(4, 12)
            add(JBLabel().apply {
                text = "Your Rank"
                foreground = Color.WHITE
                font = font.deriveFont(Font.BOLD, 11f)
            })
        }

        rankLabel.apply {
            font = font.deriveFont(Font.BOLD, 18f)
            foreground = JBColor(Color(76, 175, 80), Color(129, 199, 132))
        }

        leftPanel.add(rankBadge)
        leftPanel.add(rankLabel)

        // Center: Rank change
        rankChangeLabel.apply {
            font = font.deriveFont(12f)
            horizontalAlignment = SwingConstants.CENTER
        }

        // Right: Count
        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0)).apply {
            isOpaque = false
        }

        countLabel.apply {
            font = font.deriveFont(Font.BOLD, 14f)
            foreground = JBColor(Color(76, 175, 80), Color(129, 199, 132))
        }

        val phrasesLabel = JBLabel("phrases").apply {
            foreground = JBColor.GRAY
            font = font.deriveFont(11f)
        }

        rightPanel.add(countLabel)
        rightPanel.add(phrasesLabel)

        add(leftPanel, BorderLayout.WEST)
        add(rankChangeLabel, BorderLayout.CENTER)
        add(rightPanel, BorderLayout.EAST)

        isVisible = false
    }

    /**
     * Sets the current user's rank information.
     */
    fun setRank(entry: LeaderboardEntry?) {
        if (entry == null) {
            isVisible = false
            return
        }

        rankLabel.text = " #${entry.rank}"
        countLabel.text = formatCount(entry.totalCount)
        rankChangeLabel.text = ""

        isVisible = true
    }

    /**
     * Formats a count number with abbreviations for large numbers.
     */
    private fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> NUMBER_FORMAT.format(count)
        }
    }

    /**
     * Clears the rank information.
     */
    fun clear() {
        rankLabel.text = ""
        countLabel.text = ""
        rankChangeLabel.text = ""
        isVisible = false
    }
}