package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import javax.swing.*

/**
 * Main panel content for the Astagfirullah Tool Window.
 * Displays current phrase, statistics, and quick controls.
 */
class AstagfirullahToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val settings = AstagfirullahSettings.getInstance()
    private val statistics = StatisticsService.getInstance()

    // UI Components
    private val currentPhraseLabel = createPhraseLabel()
    private val phraseListPanel = createPhraseListPanel()
    private val totalPhrasesLabel = JBLabel()
    private val todayPhrasesLabel = JBLabel()
    private val sessionsLabel = JBLabel()
    private val favoriteLanguageLabel = JBLabel()
    private val languageComboBox = ComboBox(DefaultComboBoxModel(AstagfirullahSettings.SUPPORTED_LANGUAGES))

    init {
        border = JBUI.Borders.empty(10)

        add(createHeaderPanel(), BorderLayout.NORTH)
        add(createCenterPanel(), BorderLayout.CENTER)
        add(createFooterPanel(), BorderLayout.SOUTH)

        // Initialize state
        languageComboBox.selectedItem = settings.language
        refreshPhrases()
        updateStatistics()
    }

    private fun createPhraseLabel(): JBLabel = JBLabel().apply {
        font = font.deriveFont(Font.BOLD, 16f)
        horizontalAlignment = SwingConstants.CENTER
        border = JBUI.Borders.empty(20)
    }

    private fun createPhraseListPanel(): JPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(10)
    }

    private fun createHeaderPanel(): JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.emptyBottom(10)

        // Title with icon
        val titleLabel = JBLabel(Constants.PLUGIN_NAME).apply {
            font = font.deriveFont(Font.BOLD, 18f)
            icon = loadPluginIcon()
        }
        add(titleLabel, BorderLayout.WEST)

        // Language selector
        val languagePanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(JBLabel("Language:"))
            add(languageComboBox)
        }
        add(languagePanel, BorderLayout.EAST)

        languageComboBox.addActionListener {
            settings.language = languageComboBox.selectedItem as String
            refreshPhrases()
        }
    }

    private fun createCenterPanel(): JPanel = JPanel(BorderLayout()).apply {
        // Current phrase card
        val phraseCard = JPanel(BorderLayout()).apply {
            background = JBColor.background()
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                JBUI.Borders.empty(15)
            )
            add(currentPhraseLabel, BorderLayout.CENTER)
        }
        add(phraseCard, BorderLayout.NORTH)

        // Phrases list
        val listTitle = JBLabel("Random Phrases:").apply {
            font = font.deriveFont(Font.BOLD, 14f)
            border = JBUI.Borders.empty(10, 0, 5, 0)
        }

        val listPanel = JPanel(BorderLayout()).apply {
            add(listTitle, BorderLayout.NORTH)
            add(JBScrollPane(phraseListPanel).apply {
                preferredSize = Dimension(300, 200)
                border = BorderFactory.createLineBorder(JBColor.border(), 1)
            }, BorderLayout.CENTER)
        }
        add(listPanel, BorderLayout.CENTER)
    }

    private fun createFooterPanel(): JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.emptyTop(10)

        // Statistics panel
        val statsPanel = JPanel(GridLayout(2, 2, 10, 5)).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Statistics"),
                JBUI.Borders.empty(5)
            )
            add(totalPhrasesLabel)
            add(todayPhrasesLabel)
            add(sessionsLabel)
            add(favoriteLanguageLabel)
        }
        add(statsPanel, BorderLayout.CENTER)

        // Action buttons
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 10)).apply {
            add(JButton("Refresh").apply {
                addActionListener { refreshPhrases() }
            })
            add(JButton("Show Now").apply {
                addActionListener { triggerPhrasesDisplay() }
            })
            add(JCheckBox("Sound", settings.soundEnabled).apply {
                addActionListener { settings.soundEnabled = isSelected }
            })
        }
        add(buttonsPanel, BorderLayout.SOUTH)
    }

    private fun refreshPhrases() {
        val phrases = TranslatePhrases.selectedTranslatePhrases()

        // Update current phrase
        if (phrases.isNotEmpty()) {
            currentPhraseLabel.text = "<html><center>${phrases[0]}</center></html>"
        }

        // Update phrases list
        phraseListPanel.removeAll()
        phrases.forEachIndexed { index, phrase ->
            phraseListPanel.add(JBLabel("${index + 1}. $phrase").apply {
                border = JBUI.Borders.emptyBottom(8)
                font = font.deriveFont(13f)
            })
        }
        phraseListPanel.revalidate()
        phraseListPanel.repaint()
    }

    private fun updateStatistics() {
        totalPhrasesLabel.text = "Total: ${statistics.totalPhrasesDisplayed} phrases"
        todayPhrasesLabel.text = "Today: ${statistics.todayPhrasesDisplayed} phrases"
        sessionsLabel.text = "Sessions: ${statistics.totalSessionsCount}"
        favoriteLanguageLabel.text = "Favorite: ${statistics.favoriteLanguage}"
    }

    private fun triggerPhrasesDisplay() {
        val service = project.getService(BuildProgressService::class.java)
        service.displayPhrasesOnStartup()

        // Record and update statistics
        statistics.recordPhrasesDisplayed(Constants.PHRASES_PER_DISPLAY, settings.language)
        updateStatistics()
    }

    private fun loadPluginIcon(): Icon? {
        return try {
            IconLoader.getIcon(Constants.PLUGIN_ICON_PATH, this::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
