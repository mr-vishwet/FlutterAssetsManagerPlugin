package com.example.flutterassetsmanager

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class PluginToolbar(private val project: Project) : JBPanel<PluginToolbar>(BorderLayout()) {
    init {
        background = com.intellij.util.ui.UIUtil.getPanelBackground()
        layout = BorderLayout()

        val actionGroup = DefaultActionGroup()
        actionGroup.add(RegenerateAssetsAction())
        actionGroup.add(ConfigureFolderAction())
        actionGroup.add(ScanForChangesAction())
        actionGroup.add(MigrateAssetsAction())
        actionGroup.add(ActionsDropdownAction())

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "FlutterAssetsManagerToolbar",
            actionGroup,
            true
        )
        toolbar.setTargetComponent(this)
        toolbar.layoutPolicy = ActionToolbar.NOWRAP_LAYOUT_POLICY
        add(toolbar.component, BorderLayout.CENTER)

        // Description Panel
        val descriptionPanel = JPanel(BorderLayout()).apply {
            background = com.intellij.ui.JBColor.namedColor("ToolTip.background", java.awt.Color.decode("#3c3f41"))
            border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
            add(JLabel("<html><body style='color: #e0e0e0;'>This tool simplifies asset management in Flutter projects by generating references (e.g., FlutterAssets.img.imageName) and organizing assets into folders. Use the actions above to configure, regenerate, or migrate assets.</body></html>"), BorderLayout.CENTER)
        }
        add(descriptionPanel, BorderLayout.SOUTH)
    }
}