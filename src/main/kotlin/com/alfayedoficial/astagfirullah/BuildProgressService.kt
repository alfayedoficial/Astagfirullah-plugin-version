package com.alfayedoficial.astagfirullah

import com.intellij.build.BuildProgressListener
import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.panel.ProgressPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.WindowManager
import kotlinx.coroutines.*


@Service(Service.Level.PROJECT)
class BuildProgressService(val project: Project) : BuildProgressListener {

   companion object {
      val arabicPhrases = listOf(
         "اللهم صل وسلم على نبينا محمد",
         "سبحان الله",
         "الحمدلله",
         "لا إله إلا الله",
         "الله أكبر",
         "أستغفر الله",
         "سبحان الله وبحمده",
         "سبحان الله العظيم",
         "لا حول ولا قوة إلا بالله",
         "اللهم صل وسلم على نبينا محمد",
         "لا إله إلا أنت سبحانك إني كنت من الظالمين"
      )

      @Volatile
      private var isTaskRunning = false
   }

   private fun isAnyTaskRunning(): Boolean {
      return  ProgressManager.getInstance().hasProgressIndicator()
   }

   @Synchronized
   override fun onEvent(buildId: Any, event: BuildEvent) {
      if (!isTaskRunning && !isAnyTaskRunning()) {
         isTaskRunning = true
         ProgressManager.getInstance().run(object : Task.Backgroundable(project, "اذكر الله", false) {
            override fun run(indicator: ProgressIndicator) {
               try {
                  runBlocking {
                     withContext(Dispatchers.Default) {
                        for (i in arabicPhrases.indices) {
                           indicator.text = arabicPhrases[i]
                           indicator.fraction = (i + 1) / arabicPhrases.size.toDouble()
                           delay(1000) // Adjust the delay as needed
                        }
                     }
                  }
               } finally {
                  isTaskRunning = false
               }
            }
         })
      }
   }

}
