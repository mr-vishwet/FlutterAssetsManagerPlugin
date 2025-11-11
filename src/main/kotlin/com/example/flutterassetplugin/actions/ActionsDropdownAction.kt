package com.example.flutterassetplugin.actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ActionsDropdownAction : AnAction("Actions") {
    override fun actionPerformed(e: AnActionEvent) {
        // Implement dropdown logic if needed
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}