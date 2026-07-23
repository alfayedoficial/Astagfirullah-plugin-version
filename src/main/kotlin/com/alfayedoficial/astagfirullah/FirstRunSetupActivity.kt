package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Runs when a project is opened. Handles two mutually-exclusive first-impression flows:
 *
 *  - A brand-new install gets the setup wizard.
 *  - An existing install that has just been upgraded to a new version gets the "What's New"
 *    dialog, once for that version.
 *
 * A fresh install never sees both: after the wizard, the current version is recorded as
 * already-seen, so the What's New for the same version does not also fire.
 */
class FirstRunSetupActivity : ProjectActivity {

    private val logger = Logger.getInstance(FirstRunSetupActivity::class.java)

    override suspend fun execute(project: Project) {
        val settings = AstagfirullahSettings.getInstance()

        if (settings.isFirstRun()) {
            logger.debug("First run detected, showing setup wizard")
            // A new user does not need a What's New for the version they are installing.
            settings.lastWhatsNewVersion = Constants.PLUGIN_VERSION
            showSetupWizard(project)
            return
        }

        if (settings.shouldShowWhatsNew(Constants.PLUGIN_VERSION)) {
            logger.debug("Upgrade detected, showing What's New for ${Constants.PLUGIN_VERSION}")
            // Record before showing so two projects opening near-simultaneously cannot both
            // pass the check and open two dialogs.
            settings.lastWhatsNewVersion = Constants.PLUGIN_VERSION
            showWhatsNew(project)
        }
    }

    private fun showSetupWizard(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            FirstRunSetupDialog(project).show()
        }
    }

    private fun showWhatsNew(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            try {
                if (project.isDisposed) return@invokeLater
                WhatsNewDialog(project).show()
            } catch (e: Exception) {
                // A What's New popup must never take project startup down with it.
                logger.warn("Failed to show What's New dialog", e)
            }
        }
    }
}
