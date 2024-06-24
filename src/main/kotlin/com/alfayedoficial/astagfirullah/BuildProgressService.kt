package com.alfayedoficial.astagfirullah

import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import javax.sound.sampled.AudioSystem


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

   private fun playSound() {
      try {
         val inputStream: InputStream? = this::class.java.getResourceAsStream("/raw/mohmmed.wav")
         if (inputStream != null) {
            val bufferedInputStream = BufferedInputStream(inputStream)
            val audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
         } else {
            throw FileNotFoundException("Resource not found: /raw/mohmmed.wav")
         }
      } catch (e: Exception) {
         e.printStackTrace()
      }
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
                        playSound() // Play sound at the start
                        for (i in arabicPhrases.indices) {
                           indicator.text = arabicPhrases[i]
                           indicator.fraction = (i + 1) / arabicPhrases.size.toDouble()
                           delay(1200) // Adjust the delay as needed
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
