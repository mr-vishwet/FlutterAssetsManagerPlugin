package com.example.flutterassetplugin

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger

class FlutterAssetsManagerComponent(private val project: Project) : ProjectComponent {
    private val LOG = Logger.getInstance(FlutterAssetsManagerComponent::class.java)

    override fun projectOpened() {
        LOG.info("Project opened, initializing service")
        val service = project.getService(FlutterAssetsManagerService::class.java)
        if (service != null) {
            service.initializeToolbar()
            LOG.info("Service initialized, toolbar started")
        } else {
            LOG.error("Failed to get FlutterAssetsManagerService")
        }
    }

    override fun projectClosed() {
        // Cleanup if needed
    }

    override fun getComponentName(): String = "FlutterAssetsManagerComponent"
}