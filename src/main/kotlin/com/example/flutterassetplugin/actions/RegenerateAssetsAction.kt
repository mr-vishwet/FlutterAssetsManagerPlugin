// Update File: src/main/kotlin/com/example/flutterassetplugin/actions/RegenerateAssetsAction.kt
package com.example.flutterassetplugin.actions

import com.example.flutterassetplugin.config.PluginSettings
import com.example.flutterassetplugin.utils.AssetScanner
import com.example.flutterassetplugin.utils.DartCodeGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem

class RegenerateAssetsAction : AnAction("Regenerate Assets") {
    private val LOG = Logger.getInstance(RegenerateAssetsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = PluginSettings.getInstance(project)

        val assets = AssetScanner.scan(project)
        val content = DartCodeGenerator.generate(assets, settings.namingConvention)

        val libDir = project.baseDir.findChild("lib")
        if (libDir == null) {
            Messages.showErrorDialog(project, "lib directory not found", "Error")
            return
        }
        val generatedDir = libDir.findChild("generated") ?: libDir.createChildDirectory(this, "generated")
        val dartFile = generatedDir.findChild("flutter_assets.dart") ?: generatedDir.createChildData(this, "flutter_assets.dart")

        DartCodeGenerator.writeToFile(dartFile, content)

        LOG.info("flutter_assets.dart regenerated")
        Messages.showInfoMessage(project, "flutter_assets.dart regenerated successfully", "Success")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}