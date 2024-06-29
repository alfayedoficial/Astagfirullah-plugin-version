package com.alfayedoficial.astagfirullah

import com.intellij.ProjectTopics
import com.intellij.build.BuildViewManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBusConnection

class AppProjectActivity : ProjectActivity {

   override suspend fun execute(project: Project) {
      val buildViewManager = project.service<BuildViewManager>()
      val buildProgressService = project.service<BuildProgressService>()
      val disposable = Disposer.newDisposable()

      buildViewManager.addListener(buildProgressService, disposable)

      // Add listener for project synchronization
      val connection: MessageBusConnection = project.messageBus.connect(disposable)
      connection.subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
         override fun rootsChanged(event: ModuleRootEvent) {
            buildProgressService.onSyncEvent()
         }
      })
   }

}

