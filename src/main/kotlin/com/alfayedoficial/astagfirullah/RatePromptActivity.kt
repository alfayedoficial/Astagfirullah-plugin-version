package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.BrowserUtil
import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Activity that prompts users to rate the plugin after some usage time.
 * Rating flow:
 * - State 1: First prompt after 2 minutes of install
 * - State 2: Second prompt after 2 days if user clicked "Rate Now" or "Remind Me Later"
 * - State 3 (DONE): No more prompts after user rated or dismissed twice
 */
class RatePromptActivity : ProjectActivity {

    private val logger = Logger.getInstance(RatePromptActivity::class.java)

    companion object {
        // Track if notification was already shown this session (shared across all instances)
        @Volatile
        private var notificationShownThisSession = false
    }

    override suspend fun execute(project: Project) {
        val settings = AstagfirullahSettings.getInstance()

        // Install time is recorded by the setup wizard when it completes or is skipped
        // (FirstRunSetupDialog), never here. Recording it during the first project open would
        // prematurely flip isFirstRun() to false — and since ProjectActivity order is not
        // guaranteed, DailyDhikrActivity could then show its popup on top of the setup wizard
        // (and FirstRunSetupActivity could even skip the wizard). Until the wizard has run and
        // stamped installTime, hold off on the rating flow entirely.
        if (settings.installTime == 0L) {
            logger.debug("Install time not yet recorded (setup wizard pending), skipping rating")
            return
        }

        // Skip if user has completed the rating flow (already rated or dismissed permanently)
        if (settings.ratingPrompted) {
            logger.debug("Rating already completed, skipping")
            return
        }

        // Skip if already shown notification this session
        if (notificationShownThisSession) {
            logger.debug("Rating notification already shown this session")
            return
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceInstall = currentTime - settings.installTime
        val lastPromptTime = settings.lastRatingPromptTime

        // Determine required delay based on rating state
        val requiredDelay = when (settings.firstRatingTime) {
            Constants.RATING_STATE_FIRST -> Constants.FIRST_RATING_DELAY_MS
            Constants.RATING_STATE_SECOND -> Constants.SECOND_RATING_DELAY_MS
            Constants.RATING_STATE_DONE -> {
                settings.ratingPrompted = true
                return
            }
            else -> Constants.FIRST_RATING_DELAY_MS
        }

        // Check if enough time has passed since install (for first prompt) or last prompt (for subsequent)
        val timeSinceLastPrompt = if (lastPromptTime > 0) currentTime - lastPromptTime else timeSinceInstall

        if (timeSinceLastPrompt >= requiredDelay) {
            showRateNotification(project, settings)
        }
    }

    private fun showRateNotification(project: Project, settings: AstagfirullahSettings) {
        // Mark as shown this session
        notificationShownThisSession = true
        settings.lastRatingPromptTime = System.currentTimeMillis()

        try {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(Constants.NOTIFICATION_GROUP_ID)
                .createNotification(
                    "Enjoying ${Constants.PLUGIN_NAME}?",
                    "If you find this plugin useful, please consider rating it on the marketplace.",
                    NotificationType.INFORMATION
                )

            notification.addAction(object : NotificationAction("Rate Now") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    // User rated - mark as done
                    settings.ratingPrompted = true
                    settings.firstRatingTime = Constants.RATING_STATE_DONE
                    BrowserUtil.openPluginPage()
                    notification.expire()
                    logger.debug("User clicked Rate Now")
                }
            })

            notification.addAction(object : NotificationAction("Share on LinkedIn") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    BrowserUtil.shareOnLinkedIn()
                }
            })

            notification.addAction(object : NotificationAction("Remind Me Later") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    // Advance to next state for later reminder
                    advanceRatingState(settings)
                    notification.expire()
                    logger.debug("User clicked Remind Me Later, state: ${settings.firstRatingTime}")
                }
            })

            notification.addAction(object : NotificationAction("Don't Ask Again") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    // User dismissed permanently
                    settings.ratingPrompted = true
                    settings.firstRatingTime = Constants.RATING_STATE_DONE
                    notification.expire()
                    logger.debug("User clicked Don't Ask Again")
                }
            })

            notification.notify(project)
            logger.debug("Rating notification displayed")

        } catch (e: Exception) {
            logger.warn("Failed to show rating notification", e)
        }
    }

    private fun advanceRatingState(settings: AstagfirullahSettings) {
        settings.firstRatingTime = when (settings.firstRatingTime) {
            Constants.RATING_STATE_FIRST -> Constants.RATING_STATE_SECOND
            Constants.RATING_STATE_SECOND -> {
                settings.ratingPrompted = true
                Constants.RATING_STATE_DONE
            }
            else -> {
                settings.ratingPrompted = true
                Constants.RATING_STATE_DONE
            }
        }
    }
}
