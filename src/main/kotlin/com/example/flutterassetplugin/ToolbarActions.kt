package com.example.flutterassetsmanager

import com.intellij.openapi.actionSystem.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon
import javax.swing.JComponent

class RegenerateAssetsAction : DumbAwareAction("Regenerate Assets", "Regenerate asset references", AllIcons.Actions.Refresh) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            // Placeholder for regeneration logic
        }
    }
}

class ConfigureFolderAction : DumbAwareAction("Configure Folder", "Configure asset folders", AllIcons.General.Settings) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            // Placeholder for folder configuration dialog
        }
    }
}

class ScanForChangesAction : DumbAwareAction("Scan for Changes", "Scan project for asset changes", AllIcons.Actions.Find) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            // Placeholder for scan logic
        }
    }
}

class MigrateAssetsAction : DumbAwareAction("Migrate Assets", "Migrate asset structure", AllIcons.Actions.Commit) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            // Placeholder for migration logic
        }
    }
}

class ActionsDropdownAction : DefaultActionGroup("Actions", true) { // Changed to DefaultActionGroup
    init {
        // Add children in constructor for popup group
        add(RegenerateAssetsAction())
        add(ConfigureFolderAction())
        add(ScanForChangesAction())
        add(MigrateAssetsAction())
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isPopupGroup = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    // Remove override for createPopupMenu and createCustomComponent
    // These are handled by DefaultActionGroup's implementation
}

private fun loadIcon(iconName: String): Icon? {
    return IconLoader.getIcon("/icons/$iconName.svg", RegenerateAssetsAction::class.java)
}