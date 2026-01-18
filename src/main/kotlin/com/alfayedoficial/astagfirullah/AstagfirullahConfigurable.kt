package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.BrowserUtil
import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.cache.PraiseCacheService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.swing.*

/**
 * Configurable for Astagfirullah plugin settings.
 * Accessible from Settings → Tools → Astagfirullah
 */
class AstagfirullahConfigurable : Configurable {

    private val settings = AstagfirullahSettings.getInstance()
    private val statistics = StatisticsService.getInstance()
    private val cacheService = PraiseCacheService.getInstance()

    private var mainPanel: JPanel? = null
    private lateinit var languageComboBox: ComboBox<String>
    private lateinit var delayComboBox: ComboBox<String>
    private lateinit var soundCheckBox: JCheckBox
    private lateinit var startupCheckBox: JCheckBox

    override fun getDisplayName(): String = Constants.PLUGIN_NAME

    override fun createComponent(): JComponent {
        val tabbedPane = JBTabbedPane()

        tabbedPane.addTab("Settings", createSettingsTab())
        tabbedPane.addTab("Statistics", createStatisticsTab())
        tabbedPane.addTab("About", createAboutTab())

        mainPanel = JPanel(BorderLayout()).apply {
            add(tabbedPane, BorderLayout.CENTER)
        }

        return mainPanel!!
    }

    private fun createSettingsTab(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.empty(20)

        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(8, 0)
            anchor = GridBagConstraints.WEST
        }

        var row = 0

        // Header
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 2
        panel.add(createSectionHeader("Display Settings"), gbc)

        // Language
        gbc.gridy = row
        gbc.gridwidth = 1
        gbc.gridx = 0
        gbc.weightx = 0.3
        panel.add(JBLabel("Language:"), gbc)

        languageComboBox = ComboBox(DefaultComboBoxModel(AstagfirullahSettings.SUPPORTED_LANGUAGES))
        languageComboBox.selectedItem = settings.language
        gbc.gridx = 1
        gbc.weightx = 0.7
        panel.add(languageComboBox, gbc)
        row++

        // Delay
        gbc.gridy = row
        gbc.gridx = 0
        gbc.weightx = 0.3
        panel.add(JBLabel("Phrase display duration:"), gbc)

        delayComboBox = ComboBox(DefaultComboBoxModel(AstagfirullahSettings.DELAY_OPTIONS))
        delayComboBox.selectedItem = settings.delaySeconds
        val delayPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0)).apply {
            add(delayComboBox)
            add(JBLabel("seconds"))
        }
        gbc.gridx = 1
        gbc.weightx = 0.7
        panel.add(delayPanel, gbc)
        row++

        // Sound Settings Section
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 2
        gbc.insets = JBUI.insets(20, 0, 8, 0)
        panel.add(createSectionHeader("Sound Settings"), gbc)

        gbc.insets = JBUI.insets(8, 0)
        gbc.gridy = row++
        soundCheckBox = JCheckBox("Enable blessings upon the Prophet sound", settings.soundEnabled)
        panel.add(soundCheckBox, gbc)

        // Behavior Section
        gbc.gridy = row++
        gbc.insets = JBUI.insets(20, 0, 8, 0)
        panel.add(createSectionHeader("Behavior"), gbc)

        gbc.insets = JBUI.insets(8, 0)
        gbc.gridy = row++
        startupCheckBox = JCheckBox("Show phrases on IDE startup", settings.showOnStartup)
        panel.add(startupCheckBox, gbc)

        // Info panel
        gbc.gridy = row++
        gbc.insets = JBUI.insets(20, 0, 8, 0)
        panel.add(createInfoPanel(), gbc)

        // Spacer
        gbc.gridy = row
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)

        return JScrollPane(panel).apply {
            border = null
        }
    }

    private fun createStatisticsTab(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        // Stats cards container
        val cardsPanel = JPanel(GridLayout(2, 2, 15, 15))

        // Total phrases card
        cardsPanel.add(createStatCard(
            title = "Total Phrases",
            value = formatNumber(statistics.totalPhrasesDisplayed),
            subtitle = "All time",
            color = JBColor(Color(76, 175, 80), Color(76, 175, 80))
        ))

        // Today phrases card
        cardsPanel.add(createStatCard(
            title = "Today",
            value = statistics.todayPhrasesDisplayed.toString(),
            subtitle = "Phrases displayed",
            color = JBColor(Color(33, 150, 243), Color(33, 150, 243))
        ))

        // Sessions card
        cardsPanel.add(createStatCard(
            title = "Sessions",
            value = formatNumber(statistics.totalSessionsCount),
            subtitle = "IDE sessions",
            color = JBColor(Color(156, 39, 176), Color(156, 39, 176))
        ))

        // Favorite language card
        cardsPanel.add(createStatCard(
            title = "Favorite",
            value = statistics.favoriteLanguage,
            subtitle = "Most used language",
            color = JBColor(Color(255, 152, 0), Color(255, 152, 0))
        ))

        val centerPanel = JPanel(BorderLayout()).apply {
            add(cardsPanel, BorderLayout.NORTH)
        }

        // Additional info section
        val infoSection = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.emptyTop(20)

            add(TitledSeparator("Cache Information"))
            add(Box.createVerticalStrut(10))

            val cacheInfo = JPanel(GridLayout(3, 2, 10, 5)).apply {
                border = JBUI.Borders.emptyLeft(10)
                add(JBLabel("Cached phrases:"))
                add(JBLabel("${cacheService.getCachedPraises().size} phrases"))
                add(JBLabel("Cache version:"))
                add(JBLabel("v${cacheService.getCurrentVersion()}"))
                add(JBLabel("Last sync:"))
                add(JBLabel(getLastSyncText()))
            }
            add(cacheInfo)

            // Usage streak
            add(Box.createVerticalStrut(20))
            add(TitledSeparator("Usage"))
            add(Box.createVerticalStrut(10))

            val daysUsing = getDaysUsingPlugin()
            val usagePanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                border = JBUI.Borders.emptyLeft(10)
                add(JBLabel("Using plugin for: "))
                add(JBLabel("$daysUsing days").apply {
                    font = font.deriveFont(Font.BOLD)
                    foreground = JBColor(Color(76, 175, 80), Color(76, 175, 80))
                })
            }
            add(usagePanel)
        }

        centerPanel.add(infoSection, BorderLayout.CENTER)
        panel.add(centerPanel, BorderLayout.CENTER)

        // Reset button
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = JBUI.Borders.emptyTop(10)
            add(JButton("Clear Cache").apply {
                addActionListener {
                    val result = JOptionPane.showConfirmDialog(
                        panel,
                        "This will clear all cached phrases. Continue?",
                        "Clear Cache",
                        JOptionPane.YES_NO_OPTION
                    )
                    if (result == JOptionPane.YES_OPTION) {
                        cacheService.clearCache()
                        JOptionPane.showMessageDialog(panel, "Cache cleared successfully!")
                    }
                }
            })
        }
        panel.add(buttonPanel, BorderLayout.SOUTH)

        return JScrollPane(panel).apply {
            border = null
        }
    }

    private fun createAboutTab(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // Logo and title
        val headerPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            val icon = try {
                IconLoader.getIcon(Constants.PLUGIN_ICON_PATH, this::class.java)
            } catch (e: Exception) {
                null
            }
            if (icon != null) {
                add(JBLabel(icon))
            }
            add(JBLabel(Constants.PLUGIN_NAME).apply {
                font = font.deriveFont(Font.BOLD, 24f)
            })
        }
        contentPanel.add(headerPanel)
        contentPanel.add(Box.createVerticalStrut(5))

        // Version
        val versionPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(JBLabel("Version ${Constants.PLUGIN_VERSION}").apply {
                foreground = UIUtil.getContextHelpForeground()
            })
        }
        contentPanel.add(versionPanel)
        contentPanel.add(Box.createVerticalStrut(20))

        // Description
        val descPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(JBLabel("<html><center>This plugin helps utilize waiting time for remembrance,<br>" +
                    "seeking forgiveness, glorification, and sending blessings<br>" +
                    "upon the Prophet Muhammad (peace be upon him).</center></html>"))
        }
        contentPanel.add(descPanel)
        contentPanel.add(Box.createVerticalStrut(30))

        // Features
        contentPanel.add(TitledSeparator("Features"))
        contentPanel.add(Box.createVerticalStrut(10))

        val features = listOf(
            "Display dhikr and supplications during build/sync",
            "7 languages supported",
            "Configurable display duration (1-10 seconds)",
            "Optional sound for blessings upon the Prophet",
            "Usage statistics tracking",
            "Offline caching with auto-sync"
        )

        val featuresPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.emptyLeft(20)
            features.forEach { feature ->
                add(JBLabel("• $feature").apply {
                    border = JBUI.Borders.emptyBottom(5)
                })
            }
        }
        contentPanel.add(featuresPanel)
        contentPanel.add(Box.createVerticalStrut(30))

        // Developer info
        contentPanel.add(TitledSeparator("Developer"))
        contentPanel.add(Box.createVerticalStrut(10))

        val devPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = JBUI.Borders.emptyLeft(20)

            add(JBLabel("Ali Al-Shahat Ali"))
            add(Box.createHorizontalStrut(10))

            val linkedinButton = JButton("LinkedIn").apply {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                addActionListener { BrowserUtil.openDeveloperProfile() }
            }
            add(linkedinButton)

            val rateButton = JButton("Rate Plugin").apply {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                addActionListener { BrowserUtil.openPluginPage() }
            }
            add(rateButton)
        }
        contentPanel.add(devPanel)
        contentPanel.add(Box.createVerticalStrut(20))

        // Copyright
        val copyrightPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(JBLabel("© 2024-2026 Ali Al-Shahat Ali").apply {
                foreground = UIUtil.getContextHelpForeground()
            })
        }
        contentPanel.add(copyrightPanel)

        panel.add(contentPanel, BorderLayout.NORTH)
        return JScrollPane(panel).apply {
            border = null
        }
    }

    private fun createSectionHeader(title: String): JComponent {
        return TitledSeparator(title)
    }

    private fun createInfoPanel(): JComponent {
        val panel = JPanel(BorderLayout()).apply {
            background = JBColor(Color(240, 240, 240), Color(60, 63, 65))
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                JBUI.Borders.empty(10)
            )
        }

        val infoLabel = JBLabel("<html>" +
                "<b>Tip:</b> Phrases are displayed during Gradle build and sync operations.<br>" +
                "You can also trigger them manually from the Tool Window (View → Tool Windows → Astagfirullah)." +
                "</html>")
        infoLabel.foreground = UIUtil.getContextHelpForeground()
        panel.add(infoLabel)

        return panel
    }

    private fun createStatCard(title: String, value: String, subtitle: String, color: Color): JComponent {
        val card = JPanel(BorderLayout()).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                JBUI.Borders.empty(15)
            )
            background = JBColor.background()
        }

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        // Title
        contentPanel.add(JBLabel(title).apply {
            font = font.deriveFont(12f)
            foreground = UIUtil.getContextHelpForeground()
            alignmentX = Component.LEFT_ALIGNMENT
        })

        contentPanel.add(Box.createVerticalStrut(8))

        // Value
        contentPanel.add(JBLabel(value).apply {
            font = font.deriveFont(Font.BOLD, 28f)
            foreground = color
            alignmentX = Component.LEFT_ALIGNMENT
        })

        contentPanel.add(Box.createVerticalStrut(4))

        // Subtitle
        contentPanel.add(JBLabel(subtitle).apply {
            font = font.deriveFont(11f)
            foreground = UIUtil.getContextHelpForeground()
            alignmentX = Component.LEFT_ALIGNMENT
        })

        // Color indicator bar
        val colorBar = JPanel().apply {
            preferredSize = Dimension(4, 0)
            background = color
        }

        card.add(colorBar, BorderLayout.WEST)
        card.add(Box.createHorizontalStrut(10), BorderLayout.CENTER)
        card.add(contentPanel, BorderLayout.CENTER)

        return card
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    private fun getLastSyncText(): String {
        return try {
            val cache = PraiseCacheService.getInstance()
            if (cache.hasCachedData()) "Today" else "Never"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getDaysUsingPlugin(): Long {
        val installTime = settings.installTime
        return if (installTime > 0) {
            val installDate = LocalDate.ofEpochDay(installTime / (24 * 60 * 60 * 1000))
            ChronoUnit.DAYS.between(installDate, LocalDate.now())
        } else {
            0
        }
    }

    override fun isModified(): Boolean {
        return languageComboBox.selectedItem != settings.language ||
                delayComboBox.selectedItem != settings.delaySeconds ||
                soundCheckBox.isSelected != settings.soundEnabled ||
                startupCheckBox.isSelected != settings.showOnStartup
    }

    override fun apply() {
        settings.language = languageComboBox.selectedItem as String
        settings.delaySeconds = delayComboBox.selectedItem as String
        settings.soundEnabled = soundCheckBox.isSelected
        settings.showOnStartup = startupCheckBox.isSelected
    }

    override fun reset() {
        languageComboBox.selectedItem = settings.language
        delayComboBox.selectedItem = settings.delaySeconds
        soundCheckBox.isSelected = settings.soundEnabled
        startupCheckBox.isSelected = settings.showOnStartup
    }
}