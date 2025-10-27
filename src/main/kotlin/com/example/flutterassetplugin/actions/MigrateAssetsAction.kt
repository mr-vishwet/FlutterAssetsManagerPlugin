package com.example.flutterassetplugin.actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class MigrateAssetsAction : AnAction("Migrate Assets") {
    private val LOG = Logger.getInstance(MigrateAssetsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Migrate Assets action performed")
        // Add logic to migrate asset structure here
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true // Enable the action by default
    }
}