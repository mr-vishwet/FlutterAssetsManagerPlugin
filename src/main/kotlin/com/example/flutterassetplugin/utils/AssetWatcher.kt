// src/main/kotlin/com/example/flutterassetplugin/utils/AssetWatcher.kt
package com.example.flutterassetplugin.utils

import com.example.flutterassetplugin.config.PluginSettings
import com.example.flutterassetplugin.ui.AssetScannerPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Service(Service.Level.PROJECT)
class AssetWatcher(private val project: Project) : Disposable {
    private val logger = Logger.getInstance(AssetWatcher::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val settings = PluginSettings.getInstance(project)
    private val panel = AssetScannerPanel.getInstance(project)

    // path (relative to assets folder) → generated field name
    private val pathToFieldName = ConcurrentHashMap<String, String>()

    private val changeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    init {
        startWatching()
        setupDebounce()
    }

    private fun startWatching() {
        val connection = project.messageBus.connect(this)
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val assetsRootPath = settings.assetsFolderPath ?: return
                val assetsRoot = LocalFileSystem.getInstance().findFileByPath(assetsRootPath) ?: return

                val hasRelevant = events.any { ev ->
                    val f = ev.file ?: return@any false
                    isUnderAssetsRoot(f, assetsRoot) && isAssetFile(f)
                }

                if (hasRelevant) {
                    ApplicationManager.getApplication().invokeLater {
                        changeFlow.tryEmit(Unit)
                    }
                }
            }
        })
    }

    private fun setupDebounce() {
        coroutineScope.launch {
            changeFlow
                .debounce(500.milliseconds)
                .collectLatest {
                    withContext(Dispatchers.EDT) { triggerRegeneration() }
                }
        }
    }

    private suspend fun triggerRegeneration() {
        panel.setStatus("Scanning changes...")
        val start = System.currentTimeMillis()

        try {
            val assets = AssetScanner.scan(project)
            if (assets.isEmpty()) return

            val content = DartCodeGenerator.generate(assets, settings.namingConvention)

            // ---- update field-name map ----
            pathToFieldName.clear()
            assets.forEach { (_, files) ->
                files.forEach { af ->
                    val rel = af.file.path.removePrefix(settings.assetsFolderPath + "/")
                    val field = extractFieldName(content, rel)
                    pathToFieldName[rel] = field
                }
            }

            // ---- write file (EDT-safe) ----
            val libDir = project.baseDir.findChild("lib") ?: return
            val generatedDir = libDir.findChild("generated") ?: run {
                ApplicationManager.getApplication().runWriteAction<VirtualFile> {
                    libDir.createChildDirectory(this, "generated")
                }
            }
            val dartFile = generatedDir.findChild("flutter_assets.dart") ?: run {
                ApplicationManager.getApplication().runWriteAction<VirtualFile> {
                    generatedDir.createChildData(this, "flutter_assets.dart")
                }
            }
            DartCodeGenerator.writeToFile(dartFile, content)

            val duration = System.currentTimeMillis() - start
            val total = assets.values.sumOf { it.size }
            panel.setStatus("$total assets • Regenerated in ${duration}ms")
            panel.refreshTree()          // existing method in AssetScannerPanel

            logger.info("Auto-regenerated flutter_assets.dart in ${duration}ms")
        } catch (e: Exception) {
            logger.error("Auto-regeneration failed", e)
            panel.setStatus("Error: ${e.message}")
        }
    }

    private fun extractFieldName(generated: String, relPath: String): String {
        val regex = """static const String (\w+) = '$relPath';""".toRegex()
        return regex.find(generated)?.groupValues?.get(1)
            ?: DartCodeGenerator.toCamelCase(relPath.substringAfterLast("/").substringBeforeLast("."))
    }

    private fun isUnderAssetsRoot(file: VirtualFile, root: VirtualFile): Boolean =
        VfsUtil.isAncestor(root, file, false)

    private fun isAssetFile(file: VirtualFile): Boolean =
        !file.isDirectory && AssetScanner.isAssetExtension(file.extension)

    override fun dispose() {
        coroutineScope.cancel()
        pathToFieldName.clear()
    }
}