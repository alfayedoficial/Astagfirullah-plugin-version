package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * First-run setup wizard dialog with 4 steps.
 * Guides users through initial plugin configuration.
 */
class FirstRunSetupDialog(private val project: Project?) : DialogWrapper(project) {

    private val logger = Logger.getInstance(FirstRunSetupDialog::class.java)

    private var currentStep = 0
    private val totalSteps = 4

    // Card layout for switching between steps
    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)

    // Navigation buttons
    private lateinit var backButton: JButton
    private lateinit var nextButton: JButton
    private lateinit var skipButton: JButton

    // Step panels
    private lateinit var welcomePanel: JPanel
    private lateinit var languagePanel: JPanel
    private lateinit var settingsPanel: JPanel
    private lateinit var summaryPanel: JPanel

    // Settings controls
    private lateinit var languageComboBox: JComboBox<String>
    private lateinit var languagePreviewLabel: JBLabel
    private lateinit var soundEnabledCheckbox: JCheckBox
    private lateinit var delayComboBox: JComboBox<String>

    // Summary labels
    private lateinit var summaryLanguageLabel: JBLabel
    private lateinit var summarySoundLabel: JBLabel
    private lateinit var summaryDelayLabel: JBLabel

    // Progress label
    private lateinit var progressLabel: JBLabel

    // Sample phrases for preview
    private val samplePhrases = mapOf(
        "العربية" to "سبحان الله وبحمده",
        "English" to "Glory be to Allah and with His praise",
        "أردو" to "اللہ پاک ہے اور اس کی تعریف کے ساتھ",
        "فارسى" to "سبحان الله وبحمده",
        "Türkçe" to "Sübhanallahi ve bihamdihi",
        "Bahasa" to "Subhanallahi wa bihamdihi",
        "বাংলা" to "সুবহানাল্লাহি ওয়া বিহামদিহি"
    )

    init {
        title = "Astagfirullah Setup"
        // Non-modal so it never blocks the EDT / IDE startup. A modal wizard auto-shown on
        // project open stalled the Marketplace IDE-run verifier (10-minute timeout) and kept
        // the IDE Trial widget from initializing. Non-modal also lets users keep working.
        isModal = false
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.preferredSize = Dimension(550, 520)

        // Create all step panels
        welcomePanel = createWelcomePanel()
        languagePanel = createLanguagePanel()
        settingsPanel = createSettingsPanel()
        summaryPanel = createSummaryPanel()

        // Add panels to card layout
        cardPanel.add(welcomePanel, "step0")
        cardPanel.add(languagePanel, "step1")
        cardPanel.add(settingsPanel, "step2")
        cardPanel.add(summaryPanel, "step3")

        mainPanel.add(cardPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun createWelcomePanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        // Header with icon and title
        val headerPanel = JPanel(BorderLayout())
        headerPanel.border = JBUI.Borders.emptyBottom(20)

        val titleLabel = JBLabel("Welcome to Astagfirullah")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 24f)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        headerPanel.add(titleLabel, BorderLayout.CENTER)

        // Description
        val descPanel = JPanel()
        descPanel.layout = BoxLayout(descPanel, BoxLayout.Y_AXIS)

        val descLabel = JBLabel("<html><center>Utilize waiting time for remembrance</center></html>")
        descLabel.font = descLabel.font.deriveFont(16f)
        descLabel.alignmentX = Component.CENTER_ALIGNMENT
        descPanel.add(descLabel)
        descPanel.add(Box.createVerticalStrut(30))

        // Features list \u2014 sourced from the shared WhatsNew highlights so the wizard, the
        // About tab and the What's New dialog stay in sync.
        val featuresHtml = buildString {
            // A fixed width makes the HTML label wrap long highlight lines. Without it the label
            // sizes to its widest line and, centered in the FlowLayout, overflows (and visibly
            // clips) both edges of the 550px wizard panel.
            append("<html><div style='width: 490px; text-align: left;'><b>What's New in ")
            append(Constants.PLUGIN_VERSION)
            append(":</b><br><br>")
            WhatsNew.HIGHLIGHTS.forEach { append("\u2022 ").append(it).append("<br><br>") }
            append("</div></html>")
        }
        val featuresLabel = JBLabel(featuresHtml)
        featuresLabel.font = featuresLabel.font.deriveFont(13f)
        featuresLabel.alignmentX = Component.CENTER_ALIGNMENT

        val featuresWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        featuresWrapper.add(featuresLabel)
        descPanel.add(featuresWrapper)

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(descPanel, BorderLayout.CENTER)

        return panel
    }

    private fun createLanguagePanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        // Header
        val headerPanel = JPanel(BorderLayout())
        headerPanel.border = JBUI.Borders.emptyBottom(20)

        val titleLabel = JBLabel("Choose Your Language")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 20f)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        headerPanel.add(titleLabel, BorderLayout.CENTER)

        // Content
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        val instructionLabel = JBLabel("Select your preferred language for phrases:")
        instructionLabel.alignmentX = Component.CENTER_ALIGNMENT
        contentPanel.add(instructionLabel)
        contentPanel.add(Box.createVerticalStrut(20))

        // Language combo box
        languageComboBox = JComboBox(AstagfirullahSettings.SUPPORTED_LANGUAGES)
        languageComboBox.selectedItem = AstagfirullahSettings.getInstance().language
        languageComboBox.maximumSize = Dimension(250, 35)
        languageComboBox.alignmentX = Component.CENTER_ALIGNMENT
        languageComboBox.addActionListener { updateLanguagePreview() }

        val comboWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        comboWrapper.add(languageComboBox)
        contentPanel.add(comboWrapper)
        contentPanel.add(Box.createVerticalStrut(30))

        // Preview section
        val previewLabel = JBLabel("Preview:")
        previewLabel.font = previewLabel.font.deriveFont(Font.BOLD)
        previewLabel.alignmentX = Component.CENTER_ALIGNMENT
        contentPanel.add(previewLabel)
        contentPanel.add(Box.createVerticalStrut(10))

        // Preview phrase
        languagePreviewLabel = JBLabel()
        languagePreviewLabel.font = languagePreviewLabel.font.deriveFont(18f)
        languagePreviewLabel.horizontalAlignment = SwingConstants.CENTER
        languagePreviewLabel.alignmentX = Component.CENTER_ALIGNMENT

        val previewPanel = JPanel(BorderLayout())
        previewPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            JBUI.Borders.empty(20)
        )
        previewPanel.add(languagePreviewLabel, BorderLayout.CENTER)

        val previewWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        previewWrapper.add(previewPanel)
        contentPanel.add(previewWrapper)

        // Initialize preview
        updateLanguagePreview()

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)

        return panel
    }

    private fun createSettingsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        // Header
        val headerPanel = JPanel(BorderLayout())
        headerPanel.border = JBUI.Borders.emptyBottom(20)

        val titleLabel = JBLabel("Sound & Timing")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 20f)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        headerPanel.add(titleLabel, BorderLayout.CENTER)

        // Content
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        // Sound setting
        val soundPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        soundEnabledCheckbox = JCheckBox("Enable blessing sound")
        soundEnabledCheckbox.isSelected = AstagfirullahSettings.getInstance().soundEnabled
        soundEnabledCheckbox.font = soundEnabledCheckbox.font.deriveFont(14f)
        soundPanel.add(soundEnabledCheckbox)

        val testSoundButton = JButton("Test Sound")
        testSoundButton.addActionListener {
            AudioService.getInstance().playAudio(Constants.BLESSING_AUDIO_PATH)
        }
        soundPanel.add(testSoundButton)
        contentPanel.add(soundPanel)

        val soundDescLabel = JBLabel("<html><i>Plays \"صلى على سيدنا محمد\" (Blessings upon Prophet Muhammad)</i></html>")
        soundDescLabel.foreground = JBColor.GRAY
        val soundDescWrapper = JPanel(FlowLayout(FlowLayout.LEFT))
        soundDescWrapper.add(Box.createHorizontalStrut(25))
        soundDescWrapper.add(soundDescLabel)
        contentPanel.add(soundDescWrapper)

        contentPanel.add(Box.createVerticalStrut(30))

        // Delay setting
        val delayLabel = JBLabel("Delay between phrases:")
        delayLabel.font = delayLabel.font.deriveFont(14f)
        val delayLabelWrapper = JPanel(FlowLayout(FlowLayout.LEFT))
        delayLabelWrapper.add(delayLabel)
        contentPanel.add(delayLabelWrapper)

        contentPanel.add(Box.createVerticalStrut(10))

        val delayPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        delayComboBox = JComboBox(AstagfirullahSettings.DELAY_OPTIONS)
        delayComboBox.selectedItem = AstagfirullahSettings.getInstance().delaySeconds
        delayComboBox.preferredSize = Dimension(100, 30)
        delayPanel.add(delayComboBox)
        delayPanel.add(JBLabel("seconds"))
        contentPanel.add(delayPanel)

        val delayDescLabel = JBLabel("<html><i>Time to display each phrase before showing the next one</i></html>")
        delayDescLabel.foreground = JBColor.GRAY
        val delayDescWrapper = JPanel(FlowLayout(FlowLayout.LEFT))
        delayDescWrapper.add(Box.createHorizontalStrut(5))
        delayDescWrapper.add(delayDescLabel)
        contentPanel.add(delayDescWrapper)

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)

        return panel
    }

    private fun createSummaryPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(20)

        // Header
        val headerPanel = JPanel(BorderLayout())
        headerPanel.border = JBUI.Borders.emptyBottom(20)

        val titleLabel = JBLabel("Setup Complete!")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 20f)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        headerPanel.add(titleLabel, BorderLayout.CENTER)

        // Summary content
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        val reviewLabel = JBLabel("Your settings:")
        reviewLabel.font = reviewLabel.font.deriveFont(Font.BOLD, 14f)
        reviewLabel.alignmentX = Component.CENTER_ALIGNMENT
        contentPanel.add(reviewLabel)
        contentPanel.add(Box.createVerticalStrut(20))

        // Settings summary box
        val summaryBox = JPanel()
        summaryBox.layout = BoxLayout(summaryBox, BoxLayout.Y_AXIS)
        summaryBox.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            JBUI.Borders.empty(20)
        )

        summaryLanguageLabel = JBLabel()
        summaryLanguageLabel.font = summaryLanguageLabel.font.deriveFont(14f)
        summaryBox.add(createSummaryRow("Language:", summaryLanguageLabel))
        summaryBox.add(Box.createVerticalStrut(15))

        summarySoundLabel = JBLabel()
        summarySoundLabel.font = summarySoundLabel.font.deriveFont(14f)
        summaryBox.add(createSummaryRow("Sound:", summarySoundLabel))
        summaryBox.add(Box.createVerticalStrut(15))

        summaryDelayLabel = JBLabel()
        summaryDelayLabel.font = summaryDelayLabel.font.deriveFont(14f)
        summaryBox.add(createSummaryRow("Delay:", summaryDelayLabel))

        val summaryWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        summaryWrapper.add(summaryBox)
        contentPanel.add(summaryWrapper)

        contentPanel.add(Box.createVerticalStrut(30))

        // Additional info
        val infoLabel = JBLabel("<html><center>" +
                "You can change these settings anytime from<br>" +
                "<b>Tools > Astagfirullah</b> or the <b>Tool Window</b>" +
                "</center></html>")
        infoLabel.horizontalAlignment = SwingConstants.CENTER
        infoLabel.alignmentX = Component.CENTER_ALIGNMENT
        val infoWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        infoWrapper.add(infoLabel)
        contentPanel.add(infoWrapper)

        contentPanel.add(Box.createVerticalStrut(20))

        val blessingLabel = JBLabel("<html><center><i>May Allah accept your remembrance</i></center></html>")
        blessingLabel.foreground = JBColor.GRAY
        blessingLabel.horizontalAlignment = SwingConstants.CENTER
        blessingLabel.alignmentX = Component.CENTER_ALIGNMENT
        val blessingWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        blessingWrapper.add(blessingLabel)
        contentPanel.add(blessingWrapper)

        contentPanel.add(Box.createVerticalStrut(20))

        // Informational "also available on Android / iOS / browser extensions" banner.
        // Cross-promotion of the developer's own apps via their own websites; see
        // CrossPlatformBanner for the policy rationale.
        val bannerWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        bannerWrapper.add(com.alfayedoficial.astagfirullah.ui.components.CrossPlatformBanner.create())
        contentPanel.add(bannerWrapper)

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(JBScrollPane(contentPanel).apply { border = JBUI.Borders.empty() }, BorderLayout.CENTER)

        return panel
    }

    private fun createSummaryRow(label: String, valueLabel: JBLabel): JPanel {
        val row = JPanel(FlowLayout(FlowLayout.LEFT))
        val labelComponent = JBLabel(label)
        labelComponent.font = labelComponent.font.deriveFont(Font.BOLD, 14f)
        labelComponent.preferredSize = Dimension(100, 25)
        row.add(labelComponent)
        row.add(valueLabel)
        return row
    }

    override fun createActions(): Array<Action> {
        return emptyArray() // We'll use custom buttons
    }

    override fun createSouthPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10, 20, 20, 20)

        // Progress indicator on the left
        progressLabel = JBLabel("Step ${currentStep + 1} of $totalSteps")
        progressLabel.foreground = JBColor.GRAY
        panel.add(progressLabel, BorderLayout.WEST)

        // Buttons on the right
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 10, 0))

        skipButton = JButton("Skip Setup")
        skipButton.addActionListener { skipSetup() }
        buttonPanel.add(skipButton)

        backButton = JButton("Back")
        backButton.addActionListener { goBack() }
        backButton.isVisible = false
        buttonPanel.add(backButton)

        nextButton = JButton("Next")
        nextButton.addActionListener { goNext() }
        buttonPanel.add(nextButton)

        panel.add(buttonPanel, BorderLayout.EAST)

        return panel
    }

    private fun updateLanguagePreview() {
        val selectedLanguage = languageComboBox.selectedItem as? String ?: Constants.DEFAULT_LANGUAGE
        languagePreviewLabel.text = samplePhrases[selectedLanguage] ?: samplePhrases[Constants.DEFAULT_LANGUAGE]
    }

    private fun updateSummary() {
        val selectedLanguage = languageComboBox.selectedItem as? String ?: Constants.DEFAULT_LANGUAGE
        val soundEnabled = soundEnabledCheckbox.isSelected
        val delay = delayComboBox.selectedItem as? String ?: Constants.DEFAULT_DELAY_SECONDS

        summaryLanguageLabel.text = selectedLanguage
        summarySoundLabel.text = if (soundEnabled) "Enabled" else "Disabled"
        summaryDelayLabel.text = "$delay seconds"
    }

    private fun goNext() {
        if (currentStep < totalSteps - 1) {
            currentStep++
            updateStepUI()
        } else {
            completeSetup()
        }
    }

    private fun goBack() {
        if (currentStep > 0) {
            currentStep--
            updateStepUI()
        }
    }

    private fun updateStepUI() {
        cardLayout.show(cardPanel, "step$currentStep")

        // Update button visibility
        backButton.isVisible = currentStep > 0
        skipButton.isVisible = currentStep < totalSteps - 1

        // Update next button text
        if (currentStep == totalSteps - 1) {
            nextButton.text = "Complete Setup"
            updateSummary()
        } else {
            nextButton.text = "Next"
        }

        // Update progress label
        progressLabel.text = "Step ${currentStep + 1} of $totalSteps"
    }

    private fun skipSetup() {
        // Apply default settings and mark setup as completed
        val settings = AstagfirullahSettings.getInstance()
        settings.firstSetupCompleted = true
        if (settings.installTime == 0L) {
            settings.installTime = System.currentTimeMillis()
        }
        logger.debug("Setup wizard skipped, using default settings")
        close(CANCEL_EXIT_CODE)
    }

    private fun completeSetup() {
        // Apply selected settings
        val settings = AstagfirullahSettings.getInstance()
        settings.language = languageComboBox.selectedItem as? String ?: Constants.DEFAULT_LANGUAGE
        settings.soundEnabled = soundEnabledCheckbox.isSelected
        settings.delaySeconds = delayComboBox.selectedItem as? String ?: Constants.DEFAULT_DELAY_SECONDS
        settings.firstSetupCompleted = true
        if (settings.installTime == 0L) {
            settings.installTime = System.currentTimeMillis()
        }

        logger.debug("Setup completed: language=${settings.language}, sound=${settings.soundEnabled}, delay=${settings.delaySeconds}")
        close(OK_EXIT_CODE)
    }

    override fun doCancelAction() {
        // Treat closing the dialog as skipping setup
        skipSetup()
    }
}
