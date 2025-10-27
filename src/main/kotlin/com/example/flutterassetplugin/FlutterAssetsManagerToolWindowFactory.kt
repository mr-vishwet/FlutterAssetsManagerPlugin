package com.example.flutterassetplugin

import com.example.flutterassetsmanager.PluginToolbar
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

class FlutterAssetsManagerToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val service = project.getService(FlutterAssetsManagerService::class.java)
        val toolbar = service.getToolbar() ?: PluginToolbar(project)
        val contentPanel = JPanel().apply {
            layout = java.awt.BorderLayout()
            add(toolbar, java.awt.BorderLayout.NORTH)
            add(JPanel(), java.awt.BorderLayout.CENTER)
        }
        val content = contentFactory.createContent(contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
        contentPanel.revalidate()
        contentPanel.repaint()
    }
}