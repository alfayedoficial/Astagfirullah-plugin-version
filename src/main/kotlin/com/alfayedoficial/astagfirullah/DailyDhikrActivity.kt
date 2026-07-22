package com.alfayedoficial.astagfirullah

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Shows [DailyDhikrDialog] when the IDE opens — at most once per calendar day.
 *
 * IntelliJ runs a startup activity **once per opened project**, not once per IDE launch.
 * Opening three projects in a morning would therefore mean three popups, which is exactly
 * the behaviour that earns one-star reviews, so the once-a-day cap is enforced here
 * rather than left to the dialog.
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

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            if (!settings.shouldShowDailyDhikr(today)) {
                logger.debug("Skipping daily dhikr: disabled, or already shown on $today")
                return
            }

            val phrase = TranslatePhrases.selectedTranslatePhrases().firstOrNull()
            if (phrase.isNullOrBlank()) {
                // No cached or static phrase available — show nothing rather than an
                // empty dialog, and leave the date unstamped so it can retry later.
                logger.warn("Skipping daily dhikr: no phrase available")
                return
            }

            // Stamp the date BEFORE showing. Two projects can finish starting up almost
            // simultaneously; stamping first means the second one reads the new date and
            // bails, instead of both passing the check and opening two dialogs.
            settings.lastDailyDhikrDate = today

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
