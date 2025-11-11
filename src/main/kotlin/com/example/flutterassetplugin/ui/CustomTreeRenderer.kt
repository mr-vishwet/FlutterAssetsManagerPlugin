// File: src/main/kotlin/com/example/flutterassetplugin/ui/CustomTreeRenderer.kt
package com.example.flutterassetplugin.ui

import com.example.flutterassetplugin.utils.AssetFile
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class CustomTreeRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree, value: Any, selected: Boolean, expanded: Boolean,
        leaf: Boolean, row: Int, hasFocus: Boolean
    ) {
        val node = value as DefaultMutableTreeNode
        val userObject = node.userObject

        if (userObject is String) { // Group header
            append(userObject, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            icon = if (expanded) AllIcons.General.ArrowDown else AllIcons.General.ArrowRight
        } else if (userObject is AssetFile) {
            val af = userObject
            append(af.file.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            append(" (${af.file.path})", SimpleTextAttributes.GRAY_ATTRIBUTES)
            if (af.isNew) {
                append(" New", SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, Color.GREEN))
            }

            // Use correct IntelliJ icons
            when (af.category) {
                "img" -> {
                    try {
                        val img: BufferedImage = ImageIO.read(af.file.inputStream)
                        val scaled = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH)
                        icon = ImageIcon(scaled)
                    } catch (e: IOException) {
                        icon = AllIcons.FileTypes.Image
                    }
                }
                "video" -> icon = AllIcons.FileTypes.Unknown
                "audio" -> icon = AllIcons.FileTypes.Any_type
                "fonts" -> icon = AllIcons.FileTypes.Text
                else -> icon = AllIcons.FileTypes.Unknown
            }
        }
    }
}