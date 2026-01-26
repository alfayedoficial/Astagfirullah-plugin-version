package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.BrowserUtil
import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.core.VersionUtils
import com.alfayedoficial.astagfirullah.data.cache.PluginUpdateCacheService
import com.alfayedoficial.astagfirullah.data.model.UpdateType
import com.alfayedoficial.astagfirullah.data.sync.PraiseSyncService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * Service for displaying plugin update notifications to the user.
 * Handles both NORMAL and EMERGENCY update types with appropriate UI.
 */
@Service(Service.Level.APP)
class UpdateNotificationService {

    private val logger = Logger.getInstance(UpdateNotificationService::class.java)

    companion object {
        @JvmStatic
        fun getInstance(): UpdateNotificationService {
            return ApplicationManager.getApplication().getService(UpdateNotificationService::class.java)
        }
    }

    /**
     * Shows an update notification to the user.
     *
     * @param updateInfo Information about the available update
     * @param project Optional project to show notification in
     */
    fun showUpdateNotification(
        updateInfo: PraiseSyncService.UpdateInfo,
        project: Project? = null
    ) {
        val isEmergency = updateInfo.updateType == UpdateType.EMERGENCY

        val notificationType = if (isEmergency) {
            NotificationType.WARNING
        } else {
            NotificationType.INFORMATION
        }

        val title = if (isEmergency) {
            "Astagfirullah - Important Update Available!"
        } else {
            "Astagfirullah - Update Available"
        }

        val content = buildString {
            append("A new version of Astagfirullah is available.<br/>")
            append("Current: <b>${updateInfo.currentVersion}</b> → New: <b>${updateInfo.newVersion}</b>")
            if (isEmergency) {
                append("<br/><br/><i>This is an important update. Please update as soon as possible.</i>")
            }
        }

        try {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(Constants.NOTIFICATION_GROUP_ID)
                .createNotification(title, content, notificationType)

            // Add Update action
            notification.addAction(object : NotificationAction("Update Now") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    BrowserUtil.openUrl(updateInfo.updateUrl)
                    notification.expire()
                }
            })

            // Add Remind Later action (only for non-emergency)
            if (!isEmergency) {
                notification.addAction(object : NotificationAction("Remind Me Later") {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        // Just dismiss the notification, will show again next session
                        notification.expire()
                    }
                })

                // Add Don't Ask Again action
                notification.addAction(object : NotificationAction("Don't Ask Again") {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        // Dismiss this version permanently
                        try {
                            PluginUpdateCacheService.getInstance()
                                .dismissVersion(updateInfo.newVersion)
                        } catch (ex: Exception) {
                            logger.warn("Failed to dismiss version", ex)
                        }
                        notification.expire()
                    }
                })
            }

            notification.notify(project)
            logger.debug("Update notification shown for version ${updateInfo.newVersion}")

        } catch (e: Exception) {
            logger.warn("Failed to show update notification", e)
        }
    }

    /**
     * Checks for updates and shows notification if available.
     * This can be called manually to check for updates.
     *
     * @param project Optional project to show notification in
     */
    fun checkAndShowNotification(project: Project? = null) {
        try {
            val updateCacheService = PluginUpdateCacheService.getInstance()
            val latestVersion = updateCacheService.getLatestVersionName()

            if (latestVersion.isEmpty()) {
                logger.debug("No cached version info available")
                return
            }

            val currentVersion = Constants.PLUGIN_VERSION

            // Check if update is available using shared version comparison utility
            val isUpdateAvailable = VersionUtils.isUpdateAvailable(currentVersion, latestVersion)

            if (isUpdateAvailable &&
                !updateCacheService.wasNotificationShown() &&
                !updateCacheService.isVersionDismissed(latestVersion)
            ) {
                val updateInfo = PraiseSyncService.UpdateInfo(
                    currentVersion = currentVersion,
                    newVersion = latestVersion,
                    updateType = updateCacheService.getUpdateType(),
                    updateUrl = updateCacheService.getUpdateUrl().ifEmpty { Constants.PLUGIN_MARKETPLACE_URL }
                )

                showUpdateNotification(updateInfo, project)

                // Mark notification as shown (except for emergency)
                if (updateCacheService.getUpdateType() != UpdateType.EMERGENCY) {
                    updateCacheService.markNotificationShown()
                }
            }

        } catch (e: Exception) {
            logger.warn("Error checking for updates", e)
        }
    }
}