package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.TranslatePhrases.selectTranslateTitle
import com.alfayedoficial.astagfirullah.TranslatePhrases.selectedTranslatePhrases
import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.api.StatisticApiService
import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service that displays Islamic phrases during build progress events.
 * Integrates with IntelliJ's build system to show remembrance phrases.
 * Enhanced to show progress prominently in the status bar.
 */
@Service(Service.Level.PROJECT)
class BuildProgressService(private val project: Project) : BuildProgressListener {

    private val logger = Logger.getInstance(BuildProgressService::class.java)

    /**
     * Tracks whether a phrase display task is currently running for this project.
     * Instance-level to ensure each project has independent state.
     */
    private val isTaskRunning = AtomicBoolean(false)

    private fun isAnyTaskRunning(): Boolean {
        return ProgressManager.getInstance().hasProgressIndicator()
    }

    override fun onEvent(buildId: Any, event: BuildEvent) {
        handleTask()
    }

    fun onSyncEvent() {
        handleTask()
    }

    /**
     * Display random phrases when the IDE opens.
     * Uses a fixed duration for startup display.
     */
    fun displayPhrasesOnStartup() {
        handleTask(Constants.STARTUP_DISPLAY_SECONDS)
    }

    private fun handleTask(maxDurationSeconds: Int = -1) {
        // Use compareAndSet for thread-safe check-and-set operation
        if (!isTaskRunning.compareAndSet(false, true)) {
            return // Already running
        }

        if (isAnyTaskRunning()) {
            isTaskRunning.set(false)
            return
        }

        val task = object : Task.Backgroundable(project, selectTranslateTitle(), true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    runBlocking {
                        withContext(Dispatchers.Default) {
                            val settings = AstagfirullahSettings.getInstance()
                            val phrases = selectedTranslatePhrases()

                            val delayMs = if (maxDurationSeconds > 0) {
                                (maxDurationSeconds * 1000L) / phrases.size
                            } else {
                                settings.getDelayMillis()
                            }

                            // Play sound at the start
                            AudioService.getInstance().playBlessingSound()

                            // Display phrases with progress - enhanced visibility
                            for (i in phrases.indices) {
                                // Main text shows the phrase prominently
                                indicator.text = phrases[i]
                                // Secondary text shows progress count
                                indicator.text2 = "${i + 1} / ${phrases.size}"
                                indicator.fraction = (i + 1) / phrases.size.toDouble()

                                // Update status bar info text
                                updateStatusBarText(phrases[i])

                                delay(delayMs)
                            }

                            // Clear status bar text when done
                            updateStatusBarText(null)

                            // Record statistics locally
                            StatisticsService.getInstance().recordPhrasesDisplayed(
                                count = phrases.size,
                                language = settings.language
                            )

                            // Send statistics to API (in background)
                            ApplicationManager.getApplication().executeOnPooledThread {
                                StatisticApiService.sendStatistic(phrases.size)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error displaying phrases", e)
                } finally {
                    isTaskRunning.set(false)
                    updateStatusBarText(null)
                }
            }

            override fun shouldStartInBackground(): Boolean = false // Start in foreground for visibility

            override fun isConditionalModal(): Boolean = false
        }

        // Use BackgroundableProcessIndicator for better status bar integration
        val indicator = BackgroundableProcessIndicator(task)
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
    }

    /**
     * Updates the status bar info text to show the current phrase.
     */
    private fun updateStatusBarText(text: String?) {
        ApplicationManager.getApplication().invokeLater {
            try {
                val statusBar = WindowManager.getInstance().getStatusBar(project)
                statusBar?.info = text ?: ""
            } catch (e: Exception) {
                logger.debug("Could not update status bar: ${e.message}")
            }
        }
    }
}
