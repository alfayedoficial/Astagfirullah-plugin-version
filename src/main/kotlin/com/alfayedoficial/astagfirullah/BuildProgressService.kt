package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.TranslatePhrases.selectTranslateTitle
import com.alfayedoficial.astagfirullah.TranslatePhrases.selectedTranslatePhrases
import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service that displays Islamic phrases during build progress events.
 * Integrates with IntelliJ's build system to show remembrance phrases.
 */
@Service(Service.Level.PROJECT)
class BuildProgressService(private val project: Project) : BuildProgressListener {

    private val logger = Logger.getInstance(BuildProgressService::class.java)

    companion object {
        private val isTaskRunning = AtomicBoolean(false)
    }

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

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, selectTranslateTitle(), false) {
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

                            // Display phrases with progress
                            for (i in phrases.indices) {
                                indicator.text = phrases[i]
                                indicator.fraction = (i + 1) / phrases.size.toDouble()
                                delay(delayMs)
                            }

                            // Record statistics
                            StatisticsService.getInstance().recordPhrasesDisplayed(
                                count = phrases.size,
                                language = settings.language
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error displaying phrases", e)
                } finally {
                    isTaskRunning.set(false)
                }
            }
        })
    }
}
