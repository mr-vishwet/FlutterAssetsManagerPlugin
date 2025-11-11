package com.example.flutterassetplugin

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger

@Service(Service.Level.PROJECT)
class FlutterAssetsManagerService(private val project: Project) {
    private val LOG = Logger.getInstance(FlutterAssetsManagerService::class.java)
    private var toolbar: PluginToolbar? = null

    fun initializeToolbar() {
        LOG.info("toolbar started")
        toolbar = PluginToolbar(project)
        // Toolbar addition handled by ToolWindowFactory
    }

    fun dispose() {
        toolbar = null
        LOG.info("toolbar disposed")
    }

    fun getToolbar(): PluginToolbar? = toolbar // Added this method to resolve the reference
}