// File: src/main/kotlin/com/example/flutterassetplugin/utils/PubspecParser.kt
package com.example.flutterassetplugin.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.yaml.snakeyaml.Yaml

object PubspecParser {

    private val yaml = Yaml()

    /**
     * Returns a list of declared asset paths (strings) from pubspec.yaml
     * Supports both:
     *   assets:
     *     - assets/
     *     - assets/img/logo.png
     */
    fun getDeclaredAssetPaths(project: Project): List<String> {
        val pubspec = project.baseDir.findChild("pubspec.yaml") ?: return emptyList()
        val input = pubspec.inputStream
        val data = yaml.load<Map<String, Any>>(input) ?: return emptyList()

        val flutter = data["flutter"] as? Map<*, *> ?: return emptyList()
        val assets = flutter["assets"] ?: return emptyList()

        return when (assets) {
            is String -> listOf(assets.trimEnd('/'))
            is List<*> -> assets.filterIsInstance<String>().map { it.trimEnd('/') }
            else -> emptyList()
        }
    }

    /**
     * Returns VirtualFile for each declared path that actually exists
     */
    fun getDeclaredAssetFiles(project: Project): List<VirtualFile> {
        val baseDir = project.baseDir
        return getDeclaredAssetPaths(project)
            .mapNotNull { path -> baseDir.findFileByRelativePath(path) }
            .filter { it.exists() }
    }
}