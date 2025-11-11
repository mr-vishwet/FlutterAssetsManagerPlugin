package com.example.flutterassetplugin.ui


import com.example.flutterassetplugin.config.PluginSettings
import com.example.flutterassetplugin.utils.AssetFile
import com.example.flutterassetplugin.utils.AssetScanner
import com.example.flutterassetplugin.utils.DartCodeGenerator
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel


@Service(Service.Level.PROJECT)
class AssetScannerPanel(private val project: Project) : JBPanel<AssetScannerPanel>(BorderLayout()) {


    companion object {
        /** Helper for other classes (e.g. AssetWatcher) */
        fun getInstance(project: Project): AssetScannerPanel =
            project.service<AssetScannerPanel>()
    }

    private val settings = PluginSettings.getInstance(project)
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("FlutterAssetsManager.Notification")

    private val statusLabel = JBLabel("No scan performed").apply {
        foreground = JBColor.namedColor("Label.infoForeground", JBColor.gray)
    }

    private val namingGroup = ButtonGroup()
    private val camelCaseButton = JRadioButton("camelCase").apply {
        isSelected = settings.namingConvention == "camelCase"
        addActionListener { settings.namingConvention = "camelCase" }
    }
    private val snakeCaseButton = JRadioButton("snake_case").apply {
        isSelected = settings.namingConvention == "snake_case"
        addActionListener { settings.namingConvention = "snake_case" }
    }

    private val regenerateButton = JButton("Regenerate flutter_assets.dart").apply {
        background = JBColor.namedColor("Button.default.startBackground", JBColor.blue)
        foreground = JBColor.namedColor("Button.default.foreground", JBColor.white)
        isOpaque = true
        addActionListener { regenerate() }
    }

    private val uploadButton = JButton("Upload Assets").apply {
        background = JBColor.namedColor("Button.default.startBackground", JBColor(0x0D7FF2, 0x0D7FF2))
        foreground = JBColor.white
        isOpaque = true
        addActionListener { openUploadChooser() }
    }

    private val refreshButton = JButton(IconLoader.getIcon("/actions/refresh.svg", javaClass)).apply {
        toolTipText = "Rescan assets"
        addActionListener { scanAndUpdateUI() }
    }

    private val tree = Tree().apply {
        isRootVisible = false
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        cellRenderer = CustomTreeRenderer()
    }


    init {
        namingGroup.add(camelCaseButton)
        namingGroup.add(snakeCaseButton)

        val headerPanel = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            border = JBUI.Borders.empty(8)
        }
        val c = GridBagConstraints().apply { insets = JBUI.insets(4) }

        // --- Row 0: Title + Status ---
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL
        headerPanel.add(JBLabel("Asset Scanner").apply {
            font = font.deriveFont(Font.BOLD, font.size + 4f)
        }, c)

        c.gridx = 1; c.weightx = 0.0
        headerPanel.add(statusLabel, c)

        // --- Row 1: Naming Convention + Refresh ---
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.weightx = 1.0
        val namingPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(4, 0)
        }
        namingPanel.add(JBLabel("Naming Convention"), BorderLayout.WEST)
        val togglePanel = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(camelCaseButton)
            add(snakeCaseButton)
        }
        namingPanel.add(togglePanel, BorderLayout.CENTER)
        namingPanel.add(refreshButton, BorderLayout.EAST)
        headerPanel.add(namingPanel, c)

        // --- Row 2: Buttons (Regenerate + Upload) ---
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            add(regenerateButton)
            add(uploadButton)
        }
        headerPanel.add(buttonPanel, c)

        // --- ADD HEADER TO MAIN PANEL ---
        add(headerPanel, BorderLayout.NORTH)
        add(JBScrollPane(tree), BorderLayout.CENTER)

        // --- INITIAL SCAN ---
        scanAndUpdateUI()
    }

    fun scanAndUpdateUI() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning Assets", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Reading pubspec.yaml..."

                val assets = AssetScanner.scan(project)
                if (assets.isEmpty()) {
                    SwingUtilities.invokeLater {
                        statusLabel.text = "No assets declared in pubspec.yaml"
                        tree.model = DefaultTreeModel(DefaultMutableTreeNode("Root"))
                    }
                    return
                }

                val totalAssets = assets.values.sumOf { it.size }
                val folders = assets.size

                SwingUtilities.invokeLater {
                    statusLabel.text = "$totalAssets assets • $folders folders • Last scan: ${System.currentTimeMillis() / 1000}"

                    val root = DefaultMutableTreeNode("Root")
                    for (cat in assets.keys.sorted()) {
                        val groupNode = DefaultMutableTreeNode("${cat.replaceFirstChar { it.uppercase() }} (${assets[cat]!!.size})")
                        for (asset in assets[cat]!!.sortedBy { it.file.name }) {
                            groupNode.add(DefaultMutableTreeNode(asset))
                        }
                        root.add(groupNode)
                    }
                    tree.model = DefaultTreeModel(root)
                }
            }
        })
    }

    private fun regenerate() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating flutter_assets.dart", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.fraction = 0.0
                indicator.text = "Scanning declared assets..."

                val assets = AssetScanner.scan(project)
                if (assets.isEmpty()) {
                    SwingUtilities.invokeLater {
                        Messages.showWarningDialog(project, "No assets declared in pubspec.yaml", "Nothing to Generate")
                    }
                    return
                }

                indicator.text = "Generating Dart code..."
                val content = DartCodeGenerator.generate(assets, settings.namingConvention, indicator)

                indicator.text = "Preparing directories..."
                val libDir = project.baseDir.findChild("lib") ?: run {
                    SwingUtilities.invokeLater {
                        Messages.showErrorDialog(project, "lib directory not found", "Error")
                    }
                    return
                }

                // Create dirs on EDT
                SwingUtilities.invokeLater {
                    try {
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

                        // Now safe to write
                        DartCodeGenerator.writeToFile(dartFile, content, indicator)

                        indicator.fraction = 1.0
                        notificationGroup.createNotification(
                            "Success",
                            "flutter_assets.dart generated with ${assets.values.sumOf { it.size }} assets",
                            NotificationType.INFORMATION
                        ).notify(project)

                    } catch (e: Exception) {
                        notificationGroup.createNotification(
                            "Error",
                            "Failed to generate file: ${e.message}",
                            NotificationType.ERROR
                        ).notify(project)
                    }
                }
            }
        })
    }

    private fun openUploadChooser() {
        val descriptor = FileChooserDescriptor(
            true, false,false,false, false,true
        ).apply { title = "Select Asset Files" }

        val chosen = FileChooser.chooseFiles(descriptor, project, null)
        if (chosen.isEmpty()) return

        uploadFiles(chosen.toList())
    }

    /** Copies files → assets/<category>/, resolves conflicts, auto-regenerates */
    private fun uploadFiles(files: List<VirtualFile>) {
        val assetsRoot = settings.assetsFolderPath
            ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            ?: run {
                Messages.showErrorDialog(project, "Assets folder not configured", "Error")
                return
            }

        ProgressManager.getInstance().run(object : Task.Modal(project, "Uploading Assets", true) {
            override fun run(ind: ProgressIndicator) {
                ind.isIndeterminate = false
                var processed = 0
                val total = files.size
                val conflicts = mutableListOf<Pair<VirtualFile, VirtualFile>>()

                // Use a queue to process files sequentially
                val queue = ArrayDeque(files)
                fun processNext() {
                    if (queue.isEmpty() || ind.isCanceled) {
                        if (conflicts.isNotEmpty()) {
                            SwingUtilities.invokeLater { resolveConflicts(conflicts) }
                        }
                        regenerateAfterUpload(ind)
                        return
                    }

                    val src = queue.removeFirst()
                    ind.fraction = (++processed).toDouble() / total
                    ind.text = "Copying ${src.name}..."

                    val cat = AssetScanner.getCategory(src.extension?.lowercase() ?: "")
                    assetsRoot.createChildDirectorySafe(cat) { targetDir ->
                        val existing = targetDir.findChild(src.name)
                        if (existing != null) {
                            conflicts.add(src to existing)
                            processNext()
                        } else {
                            // Copy on EDT
                            ApplicationManager.getApplication().invokeLater {
                                ApplicationManager.getApplication().runWriteAction {
                                    try {
                                        src.copy(this@AssetScannerPanel, targetDir, src.name)
                                    } catch (e: Exception) {
                                        // Log or notify
                                    } finally {
                                        processNext()
                                    }
                                }
                            }
                        }
                    }
                }

                processNext()
            }
        })
    }

    /** Simple per-file conflict dialog (skip / overwrite / rename) */
    private fun resolveConflicts(conflicts: List<Pair<VirtualFile, VirtualFile>>) {
        for ((src, dst) in conflicts) {
            val opts = arrayOf("Skip", "Overwrite", "Rename")
            val choice = Messages.showChooseDialog(
                project,
                "${src.name} already exists in assets/${AssetScanner.getCategory(src.extension?.lowercase() ?: "")}/",
                "File Conflict",
                Messages.getQuestionIcon(),
                opts,
                opts[0]
            )

            when (choice) {
                1 -> ApplicationManager.getApplication().runWriteAction { src.copy(this, dst.parent, dst.name) }          // overwrite
                2 -> {
                    var newName = "${src.nameWithoutExtension}_1.${src.extension}"
                    val parent = dst.parent
                    while (parent.findChild(newName) != null) {
                        val num = newName.substringAfterLast("_").substringBefore(".").toIntOrNull() ?: 1
                        newName = "${src.nameWithoutExtension}_${num + 1}.${src.extension}"
                    }
                    ApplicationManager.getApplication().runWriteAction { src.copy(this, parent, newName) }
                }
                // 0 = Skip → do nothing
            }
        }
    }

    /** Regenerate + UI refresh (called from background task) */
    private fun regenerateAfterUpload(ind: ProgressIndicator) {
        val assets = AssetScanner.scan(project)
        val content = DartCodeGenerator.generate(assets, settings.namingConvention, ind)

        // Write on EDT
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                try {
                    val lib = project.baseDir.findChild("lib") ?: return@runWriteAction
                    lib.createChildDirectorySafe("generated") { genDir ->
                        genDir.createChildDataSafe("flutter_assets.dart") { dart ->
                            DartCodeGenerator.writeToFile(dart, content, ind)
                        }
                    }
                } catch (e: Exception) {
                    // handled in writeToFile or notify
                }
            }
        }

        // UI update
        SwingUtilities.invokeLater {
            updateStatus("${assets.values.sumOf { it.size }} assets • Updated just now")
            refreshTree(highlightNew = true)
            notificationGroup.createNotification(
                "Upload Complete",
                "${assets.values.sumOf { it.size }} assets regenerated",
                NotificationType.INFORMATION
            ).notify(project)
        }
    }

    fun setStatus(text: String) {
        SwingUtilities.invokeLater { statusLabel.text = text }
    }

    /** Alias used by AssetWatcher – keep both for backward compatibility */
    fun updateStatus(text: String) = setStatus(text)
//
//    fun updateStatus(text: String) {
//        SwingUtilities.invokeLater { statusLabel.text = text }
//    }

    fun refreshTree(highlightNew: Boolean = false) {
        SwingUtilities.invokeLater {
            scanAndUpdateUI()
            if (highlightNew) {
                // Mark newly-added files as "NEW" for the renderer
                markNewFilesTemporarily()
            }
        }
    }

    private fun markNewFilesTemporarily() {
        // Store the current set of file paths (before upload)
        val before = mutableSetOf<String>()
        (tree.model.root as? DefaultMutableTreeNode)?.breadthFirstEnumeration()?.asSequence()
            ?.filterIsInstance<DefaultMutableTreeNode>()
            ?.mapNotNull { it.userObject as? AssetFile }
            ?.forEach { before.add(it.file.path) }

        // After a short delay (copy finished) compare with the new scan
        Timer(800) { _ ->
            val after = AssetScanner.scan(project).values.flatten().map { it.file.path }.toSet()
            val added = after - before

            // Walk the tree again and tag added nodes
            (tree.model as? DefaultTreeModel)?.let { model ->
                fun tag(node: DefaultMutableTreeNode) {
                    val obj = node.userObject
                    if (obj is AssetFile && obj.file.path in added) {
                        obj.isNew = true
                        // Auto-clear after 5 seconds
                        Timer(5000) { _ -> obj.isNew = false; model.reload(node) }.start()
                    }
                    node.children().asSequence().filterIsInstance<DefaultMutableTreeNode>().forEach(::tag)
                }
                tag(model.root as DefaultMutableTreeNode)
            }
        }.run { start() }
    }

    private fun VirtualFile.createChildDirectorySafe(name: String, onDone: (VirtualFile) -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            val result = ApplicationManager.getApplication().runWriteAction<VirtualFile> {
                findChild(name) ?: createChildDirectory(this@AssetScannerPanel, name)
            }
            onDone(result)
        }
    }

    private fun VirtualFile.createChildDataSafe(name: String, onDone: (VirtualFile) -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            val result = ApplicationManager.getApplication().runWriteAction<VirtualFile> {
                findChild(name) ?: createChildData(this@AssetScannerPanel, name)
            }
            onDone(result)
        }
    }

    private fun VirtualFile.copySafe(targetParent: VirtualFile, newName: String, onDone: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                try {
                    copy(this@AssetScannerPanel, targetParent, newName)
                } finally {
                    onDone()
                }
            }
        }
    }

}
