package com.example.flutterassetplugin.actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class ConfigureFolderAction : AnAction("Configure Folder") {
    private val LOG = Logger.getInstance(ConfigureFolderAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Configure Folder action performed")
        // Add logic to configure asset folders here
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true // Enable the action by default
    }
}