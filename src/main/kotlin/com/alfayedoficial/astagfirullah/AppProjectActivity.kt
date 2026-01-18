package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.data.sync.PraiseSyncService
import com.intellij.ProjectTopics
import com.intellij.build.BuildViewManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity that runs when a project is opened.
 * Initializes build listeners, syncs phrases from API, and displays startup phrases.
 */
class AppProjectActivity : ProjectActivity {

    private val logger = Logger.getInstance(AppProjectActivity::class.java)

    override suspend fun execute(project: Project) {
        val buildViewManager = project.service<BuildViewManager>()
        val buildProgressService = project.service<BuildProgressService>()
        val disposable = Disposer.newDisposable()

        // Register build listener
        buildViewManager.addListener(buildProgressService, disposable)

        // Sync phrases from API in background (daily sync with version check)
        syncPhrasesInBackground()

        // Display phrases on startup
        buildProgressService.displayPhrasesOnStartup()

        // Add listener for project synchronization events
        val connection: MessageBusConnection = project.messageBus.connect(disposable)
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
            override fun rootsChanged(event: ModuleRootEvent) {
                buildProgressService.onSyncEvent()
            }
        })

        logger.debug("Astagfirullah plugin initialized for project: ${project.name}")
    }

    /**
     * Syncs phrases from API in background.
     * Uses version-based sync to minimize network requests.
     */
    private fun syncPhrasesInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val syncService = PraiseSyncService.getInstance()
                val result = syncService.syncIfNeeded()

                when (result) {
                    is PraiseSyncService.SyncResult.Success -> {
                        logger.debug("Phrases synced: ${result.phraseCount} phrases, version ${result.version}")
                    }
                    is PraiseSyncService.SyncResult.AlreadyUpToDate -> {
                        logger.debug("Phrases already up to date (version ${result.version})")
                    }
                    is PraiseSyncService.SyncResult.Error -> {
                        logger.warn("Failed to sync phrases: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error during phrase sync", e)
            }
        }
    }
}
