package com.alfayedoficial.astagfirullah.ui.leaderboard

import com.alfayedoficial.astagfirullah.data.model.LeaderboardConstants
import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.text.NumberFormat
import java.util.*
import javax.swing.*

/**
 * Custom list cell renderer for leaderboard entries.
 * Displays rank (with medals for top 3), username, and count.
 */
class LeaderboardEntryRenderer(
    private val currentUserId: Int? = null
) : ListCellRenderer<LeaderboardEntry> {

    companion object {
        // Medal colors
        private val GOLD_COLOR = Color(255, 215, 0)
        private val SILVER_COLOR = Color(192, 192, 192)
        private val BRONZE_COLOR = Color(205, 127, 50)

        // Medal unicode characters
        private const val GOLD_MEDAL = "\uD83E\uDD47"    // 🥇
        private const val SILVER_MEDAL = "\uD83E\uDD48"  // 🥈
        private const val BRONZE_MEDAL = "\uD83E\uDD49"  // 🥉

        private val NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US)
    }

    private val panel = JPanel(BorderLayout())
    private val rankLabel = JLabel()
    private val nameLabel = JLabel()
    private val countLabel = JLabel()
    private val rankChangeLabel = JLabel()

    init {
        panel.apply {
            border = JBUI.Borders.empty(8, 12)
            isOpaque = true
        }

        // Left: Rank
        rankLabel.apply {
            preferredSize = JBUI.size(50, 24)
            horizontalAlignment = SwingConstants.CENTER
            font = font.deriveFont(Font.BOLD, 14f)
        }

        // Center: Name with rank change
        val centerPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
        }
        nameLabel.apply {
            font = font.deriveFont(13f)
        }
        rankChangeLabel.apply {
            font = font.deriveFont(10f)
        }
        centerPanel.add(nameLabel, BorderLayout.CENTER)
        centerPanel.add(rankChangeLabel, BorderLayout.EAST)

        // Right: Count
        countLabel.apply {
            horizontalAlignment = SwingConstants.RIGHT
            font = font.deriveFont(Font.BOLD, 13f)
        }

        panel.add(rankLabel, BorderLayout.WEST)
        panel.add(centerPanel, BorderLayout.CENTER)
        panel.add(countLabel, BorderLayout.EAST)
    }

    override fun getListCellRendererComponent(
        list: JList<out LeaderboardEntry>,
        value: LeaderboardEntry,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        // Set rank display (with medal for top 3)
        rankLabel.text = when (value.rank) {
            LeaderboardConstants.GOLD_RANK -> GOLD_MEDAL
            LeaderboardConstants.SILVER_RANK -> SILVER_MEDAL
            LeaderboardConstants.BRONZE_RANK -> BRONZE_MEDAL
            else -> "#${value.rank}"
        }

        // Set rank color for top 3
        rankLabel.foreground = when (value.rank) {
            LeaderboardConstants.GOLD_RANK -> GOLD_COLOR
            LeaderboardConstants.SILVER_RANK -> SILVER_COLOR
            LeaderboardConstants.BRONZE_RANK -> BRONZE_COLOR
            else -> if (isSelected) list.selectionForeground else list.foreground
        }

        // Set name
        nameLabel.text = value.userName
        nameLabel.foreground = if (isSelected) list.selectionForeground else list.foreground

        // Clear rank change label (API doesn't provide rank change info)
        rankChangeLabel.text = ""

        // Set count
        countLabel.text = formatCount(value.totalCount)
        countLabel.foreground = if (isSelected) list.selectionForeground else JBColor.GRAY

        // Highlight current user
        val isCurrentUser = currentUserId != null && value.userId == currentUserId
        if (isCurrentUser && !isSelected) {
            panel.background = JBColor(Color(232, 245, 233), Color(30, 60, 35))
            panel.border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, JBColor(Color(76, 175, 80), Color(76, 175, 80))),
                JBUI.Borders.empty(8, 9, 8, 12)
            )
        } else {
            panel.background = if (isSelected) list.selectionBackground else list.background
            panel.border = JBUI.Borders.empty(8, 12)
        }

        return panel
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
}