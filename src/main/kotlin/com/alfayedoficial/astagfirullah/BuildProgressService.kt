package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.TranslatePhrases.selectTranslateTitle
import com.alfayedoficial.astagfirullah.TranslatePhrases.selectedTranslatePhrases
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
      @Volatile
      private var isTaskRunning = false
   }

   private fun isAnyTaskRunning(): Boolean {
      return ProgressManager.getInstance().hasProgressIndicator()
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
      handleTask()
   }

   fun onSyncEvent() {
      handleTask()
   }

   private fun handleTask() {
      if (!isTaskRunning && !isAnyTaskRunning()) {
         isTaskRunning = true
         ProgressManager.getInstance().run(object : Task.Backgroundable(project, selectTranslateTitle(), false) {
            override fun run(indicator: ProgressIndicator) {
               try {
                  runBlocking {
                     withContext(Dispatchers.Default) {

                        val phrases = selectedTranslatePhrases()
                        val delay = convertSECONDSToMillis(PropertiesManager.getPreferredDelaySeconds())

                        if (PropertiesManager.isSoundEnabled()) playSound() // Play sound at the start

                        for (i in phrases.indices) {
                           indicator.text = phrases[i]
                           indicator.fraction = (i + 1) / phrases.size.toDouble()
                           delay(delay) // Adjust the delay as needed
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

   private fun convertSECONDSToMillis(preferredDelaySECONDS: String): Long {
      return when (preferredDelaySECONDS) {
         "1" -> 1000
         "1.5" -> 1500
         "2" -> 2000
         "2.5" -> 2500
         "3" -> 3000
         "3.5" -> 3500
         "4" -> 4000
         "4.5" -> 4500
         "5" -> 5000
         "5.5" -> 5500
         "6" -> 6000
         "6.5" -> 6500
         "7" -> 7000
         "7.5" -> 7500
         "8" -> 8000
         "8.5" -> 8500
         "9" -> 9000
         "9.5" -> 9500
         "10" -> 10000
         else -> {
            1000
         }
      }
   }
}
