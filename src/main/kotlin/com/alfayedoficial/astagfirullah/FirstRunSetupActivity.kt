package com.alfayedoficial.astagfirullah

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Activity that runs when a project is opened.
 * Shows the first-run setup wizard if this is a new installation.
 */
class FirstRunSetupActivity : ProjectActivity {

    private val logger = Logger.getInstance(FirstRunSetupActivity::class.java)

    override suspend fun execute(project: Project) {
        val settings = AstagfirullahSettings.getInstance()

        // Check if this is the first run
        if (settings.isFirstRun()) {
            logger.debug("First run detected, showing setup wizard")
            showSetupWizard(project)
        }
    }

    private fun showSetupWizard(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            val dialog = FirstRunSetupDialog(project)
            dialog.show()
        }
    }
}
