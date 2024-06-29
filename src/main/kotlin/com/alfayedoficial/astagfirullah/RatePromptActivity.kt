package com.alfayedoficial.astagfirullah

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RatePromptActivity : ProjectActivity {


   private var timeHandler : Long = 0L

   override suspend fun execute(project: Project) {

      if (PropertiesManager.getInstallTime() == 0L) {
         PropertiesManager.setInstallTime(System.currentTimeMillis())
      }

      if (PropertiesManager.isFirstTime() == "3" && PropertiesManager.isRatingPrompted()) return

      timeHandler = if (PropertiesManager.isFirstTime() == "1") {
         generateTime(minute = 2)
      } else {
         generateTime(day = 2, hour = 24, minute = 60)
      }


      val installTime = PropertiesManager.getInstallTime()
      val currentTime = System.currentTimeMillis()

      if (currentTime - installTime >= timeHandler) {
         showRateNotification(project)
      } else {
         scheduleNotification(project, timeHandler - (currentTime - installTime))
      }
   }

   private fun showRateNotification(project: Project) {
      val notification = Notification(
         "RatePluginGroup",
         "Enjoying Astagfirullah Plugin?",
         "If you find this plugin useful, please consider rating it.",
         NotificationType.INFORMATION
      )

      notification.addAction(object : AnAction("Rate Now") {
         override fun actionPerformed(e: AnActionEvent) {
            when(PropertiesManager.isFirstTime()){
                "1" -> PropertiesManager.setFirstTime("2")
                "2" -> PropertiesManager.setFirstTime("3")
                "3" -> PropertiesManager.setRatingPrompted()
            }
            val url = "https://plugins.jetbrains.com/plugin/24628-astagfirullah"
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
               Desktop.getDesktop().browse(URI(url))
            }
         }
      })

      notification.addAction(object : AnAction("Share to linkedin") {
         override fun actionPerformed(e: AnActionEvent) {
            val url = "https://www.linkedin.com/shareArticle?mini=true&url=https://plugins.jetbrains.com/plugin/24628-astagfirullah"
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
               Desktop.getDesktop().browse(URI(url))
            }
         }
      })

      notification.addAction(object : AnAction("Remind me later") {
         override fun actionPerformed(e: AnActionEvent) {
            scheduleNotification(project, 10 * 60 * 1000)
         }
      })

      Notifications.Bus.notify(notification, project)
   }

   private fun scheduleNotification(project: Project, delayMillis: Long) {
      val scheduler = Executors.newScheduledThreadPool(1)
      scheduler.schedule({
         ApplicationManager.getApplication().invokeLater {
            if (!PropertiesManager.isRatingPrompted()) {
               showRateNotification(project)
            }
         }
      }, delayMillis, TimeUnit.MILLISECONDS)
   }

   private fun generateTime(day: Int = 1, hour: Int = 1, minute: Int = 1, second: Int = 60, milliSecond: Int = 1000): Long {
      return (day * hour * minute * second * milliSecond).toLong()
   }
}

