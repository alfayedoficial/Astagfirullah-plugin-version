package com.alfayedoficial.astagfirullah

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Shows [DailyDhikrDialog] every time a project is opened.
 *
 * IntelliJ runs a startup activity once per opened project, so this greets the user on every
 * project window (owner's request). The dialog is non-modal and self-dismissing, so it never
 * blocks work; users who don't want it can turn it off in Settings.
 */
class DailyDhikrActivity : ProjectActivity {

    private val logger = Logger.getInstance(DailyDhikrActivity::class.java)

    override suspend fun execute(project: Project) {
        try {
            val settings = AstagfirullahSettings.getInstance()

            // Never interrupt the very first run: the setup wizard owns that moment.
            if (settings.isFirstRun()) {
                logger.debug("Skipping daily dhikr: first run, setup wizard takes precedence")
                return
            }

            if (!settings.dailyDhikrEnabled) {
                logger.debug("Skipping daily dhikr: disabled in settings")
                return
            }

            val phrase = TranslatePhrases.selectedTranslatePhrases().firstOrNull()
            if (phrase.isNullOrBlank()) {
                // No cached or static phrase available — show nothing rather than an empty dialog.
                logger.warn("Skipping daily dhikr: no phrase available")
                return
            }

            ApplicationManager.getApplication().invokeLater {
                try {
                    if (project.isDisposed) return@invokeLater
                    DailyDhikrDialog(project, phrase, settings.language).show()

                    // Count it like any other displayed phrase so the local stats and the
                    // leaderboard stay consistent with what the user actually saw.
                    StatisticsService.getInstance().recordPhrasesDisplayed(1, settings.language)
                } catch (e: Exception) {
                    logger.warn("Failed to show daily dhikr dialog", e)
                }
            }
        } catch (e: Exception) {
            // A greeting dialog must never take project startup down with it.
            logger.warn("Daily dhikr activity failed", e)
        }
    }
}
