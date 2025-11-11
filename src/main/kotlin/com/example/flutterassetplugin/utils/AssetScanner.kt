// File: src/main/kotlin/com/example/flutterassetplugin/utils/AssetScanner.kt
package com.example.flutterassetplugin.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Represents a single asset file with its category.
 */
data class AssetFile(
    val file: VirtualFile,
    val category: String,
    var isNew: Boolean = false
)

object AssetScanner {


    /** Mapping of category → file extensions */
    internal val categoryExtensions = mapOf(          // ← changed to **internal**
        "img"   to setOf("png", "jpg", "jpeg", "svg", "gif", "webp"),
        "video" to setOf("mp4", "mov", "avi", "mkv"),
        "audio" to setOf("mp3", "wav", "ogg", "m4a"),
        "fonts" to setOf("ttf", "otf", "woff", "woff2")
    )

    /** Public helper used by AssetWatcher */
    fun isAssetExtension(ext: String?): Boolean =
        ext?.lowercase()?.let { categoryExtensions.values.flatten().contains(it) } == true

    @JvmStatic
    fun getCategory(ext: String): String {
        categoryExtensions.entries.forEach { (cat, exts) -> if (ext in exts) return cat }
        return "others"
    }

    /**
     * Scans **only** the assets that are declared in `pubspec.yaml` and actually exist.
     *
     * @param project current IntelliJ project
     * @return map of category → list of AssetFile
     */
    fun scan(project: Project): Map<String, List<AssetFile>> {
        val declaredFiles = PubspecParser.getDeclaredAssetFiles(project)
        if (declaredFiles.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, MutableList<AssetFile>>()

        // ----- Classic loop (compatible with Kotlin 1.9) -----
        for (virtualFile in declaredFiles) {
            when {
                virtualFile.isDirectory -> {
                    // Folder declared → include *all* files inside (but not sub-folders)
                    val children = virtualFile.children
                    if (children != null) {
                        for (child in children) {
                            if (child.isDirectory) continue   // skip sub-folders
                            categorizeAndAdd(child, result)
                        }
                    }
                }
                else -> {
                    // Single file declared
                    categorizeAndAdd(virtualFile, result)
                }
            }
        }
        // -----------------------------------------------------

        return result
    }

    /** Adds a file to the correct category bucket */
    private fun categorizeAndAdd(
        file: VirtualFile,
        map: MutableMap<String, MutableList<AssetFile>>
    ) {
        val ext = file.extension?.lowercase() ?: ""
        var category = "other"
        for ((cat, exts) in categoryExtensions) {
            if (ext in exts) {
                category = cat
                break
            }
        }
        map.getOrPut(category) { mutableListOf() }
            .add(AssetFile(file, category))
    }


}