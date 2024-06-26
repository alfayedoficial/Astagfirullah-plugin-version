package com.alfayedoficial.astagfirullah

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.DelicateCoroutinesApi
import java.awt.Desktop
import java.net.URI
import javax.swing.*


class AstagfirullahAppAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        AppSettingsDialog().show()
    }
}

class AppSettingsDialog : DialogWrapper(true) {
    private val languages = arrayOf("العربية", "English", "أوردو", "فارسى")
    private val languageComboBox = ComboBox(DefaultComboBoxModel(languages))

    init {
        title = "Settings"
        init()

        // Load saved language preference
        languageComboBox.selectedItem =  PropertiesManager.getValue("preferredLanguage", "العربية")
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun createCenterPanel(): JComponent {
        val tabbedPane = JBTabbedPane()

        // Settings Tab
        val settingsPanel = panel {
            row("Language:") {
                cell(languageComboBox).align(Align.FILL)
            }
        }
        tabbedPane.addTab("Settings", settingsPanel)

        // About Tab
        val aboutPanel = JPanel()
        aboutPanel.layout = BoxLayout(aboutPanel, BoxLayout.Y_AXIS)

        val descriptionLabel = JLabel("<html>This plugin helps utilize waiting time for remembrance, seeking forgiveness,<br> " +
                "glorification, and sending blessings upon the Prophet Muhammad (peace be upon him). <br><br></html>")

        val developerLabel = JLabel("<html><p><b>Developer:</b> <a href='https://www.linkedin.com/in/alfayedoficial'>Ali Al-Shahat Ali</a></p> <br>" +
                "© 2024" +
                "</html>")

        // Add clickable link functionality
        developerLabel.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                try {
                    val uri = URI("https://www.linkedin.com/in/alfayedoficial")
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(uri)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })

        aboutPanel.add(descriptionLabel)
        aboutPanel.add(developerLabel)

        tabbedPane.addTab("About", aboutPanel)

        return tabbedPane
    }
    override fun doOKAction() {
        super.doOKAction()
        // Save selected language to shared preferences
        val selectedLanguage = languageComboBox.selectedItem as String
        PropertiesManager.setValue("preferredLanguage", selectedLanguage)
    }


}




