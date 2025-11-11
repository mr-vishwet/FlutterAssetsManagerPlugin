// Update File: src/main/kotlin/com/example/flutterassetplugin/FlutterAssetsManagerToolWindowFactory.kt
package com.example.flutterassetplugin

import com.example.flutterassetplugin.ui.AssetScannerPanel
import com.example.flutterassetplugin.utils.AssetWatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.JPanel

class FlutterAssetsManagerToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val service = project.getService(FlutterAssetsManagerService::class.java)
        service.initializeToolbar() // Triggers toolbar + watcher init

        val scannerPanel = AssetScannerPanel(project)
        project.getService(AssetWatcher::class.java) // Auto-init via @Service

        val contentPanel = JPanel().apply {
            layout = BorderLayout()
            add(service.getToolbar(), BorderLayout.NORTH)
            add(scannerPanel, BorderLayout.CENTER)
        }

        val content = contentFactory.createContent(contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}