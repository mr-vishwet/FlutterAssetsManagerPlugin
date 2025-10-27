package com.example.flutterassetplugin.actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class ScanForChangesAction : AnAction("Scan for Changes") {
    private val LOG = Logger.getInstance(ScanForChangesAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Scan for Changes action performed")
        // Add logic to scan for asset changes here
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true // Enable the action by default
    }
}