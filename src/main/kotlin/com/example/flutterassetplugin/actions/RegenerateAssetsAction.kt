package com.example.flutterassetplugin.actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class RegenerateAssetsAction : AnAction("Regenerate Assets") {
    private val LOG = Logger.getInstance(RegenerateAssetsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Regenerate Assets action performed")
        // Add logic to regenerate assets here
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true // Enable the action by default
    }
}