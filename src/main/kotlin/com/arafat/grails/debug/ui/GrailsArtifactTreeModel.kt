/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTreeStructure
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Tree model for displaying Grails artifacts in the current debug context.
 * 
 * Shows:
 * - Current Controller
 * - Associated Domain Classes
 * - Active Services
 * - Taglibs in scope
 */
class GrailsArtifactTreeModel : SimpleTreeStructure() {
    
    private val root = SimpleNode.Root()
    private val nodes = mutableListOf<SimpleNode>()
    
    init {
        buildTree()
    }
    
    private fun buildTree() {
        // Controller node
        val controllerNode = SimpleNode.Leaf(root, "Current Controller", AllIcons.Webreferences.Server).apply {
            tooltip = "The controller currently being debugged"
        }
        nodes.add(controllerNode)
        
        // Domain Classes node
        val domainClassesNode = SimpleNode.Leaf(root, "Domain Classes", AllIcons.FileTypes.Any_type).apply {
            tooltip = "Domain classes referenced in current context"
        }
        nodes.add(domainClassesNode)
        
        // Services node
        val servicesNode = SimpleNode.Leaf(root, "Active Services", AllIcons.Webreferences.Server).apply {
            tooltip = "Services injected or used in current scope"
        }
        nodes.add(servicesNode)
        
        // Taglibs node
        val taglibsNode = SimpleNode.Leaf(root, "Taglibs", AllIcons.FileTypes.Any_type).apply {
            tooltip = "Taglibs available in current GSP context"
        }
        nodes.add(taglibsNode)
        
        // GSP Views node
        val gspNode = SimpleNode.Leaf(root, "GSP Views", AllIcons.FileTypes.Any_type).apply {
            tooltip = "Associated GSP views"
        }
        nodes.add(gspNode)
    }
    
    override fun getRoot(): Any = root
    
    override fun getChildren(element: Any): Array<out Any> {
        return when (element) {
            is SimpleNode.Root -> nodes.toTypedArray()
            else -> emptyArray()
        }
    }
    
    override fun getParent(element: Any): Any? {
        return when (element) {
            is SimpleNode -> element.parent
            else -> null
        }
    }
    
    /**
     * Refresh the tree with new debug session data
     */
    fun refresh(controllerName: String?, domainClasses: List<String>, services: List<String>) {
        nodes.clear()
        
        // Rebuild nodes with actual data
        if (controllerName != null) {
            nodes.add(SimpleNode.Leaf(root, controllerName, AllIcons.Webreferences.Server))
        }
        
        domainClasses.forEach { className ->
            nodes.add(SimpleNode.Leaf(root, className, AllIcons.FileTypes.Any_type))
        }
        
        services.forEach { serviceName ->
            nodes.add(SimpleNode.Leaf(root, serviceName, AllIcons.Webreferences.Server))
        }
        
        fireModified()
    }
}

/**
 * Custom cell renderer for Grails artifact tree nodes.
 * Uses standard IntelliJ icons for a native look and feel.
 */
class GrailsArtifactTreeCellRenderer : DefaultTreeCellRenderer() {
    
    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
        
        when (val node = value as? SimpleNode) {
            is SimpleNode.Leaf -> {
                icon = node.icon ?: AllIcons.FileTypes.Any_type
                toolTipText = node.tooltip
            }
            is SimpleNode.Root -> {
                icon = AllIcons.Debugger.Db_db_view
            }
        }
        
        return this
    }
}
