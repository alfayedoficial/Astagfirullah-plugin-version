package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.ui.quran.QuranToolWindowPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Registers the "Quran" tool window that hosts the audio player.
 * DumbAware so it is usable while the IDE is indexing.
 */
class QuranToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = QuranToolWindowPanel()
        val content = ContentFactory.getInstance().createContent(panel.component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
