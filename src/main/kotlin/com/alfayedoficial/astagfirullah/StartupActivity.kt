package com.alfayedoficial.astagfirullah

import com.intellij.build.BuildViewManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer

class StartupActivity : StartupActivity {
   override fun runActivity(project: Project) {
      val buildViewManager = project.service<BuildViewManager>()
      val buildProgressService = project.service<BuildProgressService>()
      val disposable = Disposer.newDisposable()

      buildViewManager.addListener(buildProgressService, disposable)
   }
}

