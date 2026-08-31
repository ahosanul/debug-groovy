/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.ui

import com.arafat.grails.debug.evaluator.GrailsDebuggerEvaluator
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.treeStructure.SimpleTree
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Main UI panel for the Grails Debug Assistant tool window.
 * 
 * Layout:
 * - Horizontal splitter (30% left, 70% right)
 *   - Left: SimpleTree showing Grails artifacts in context
 *   - Right: JBTabs with two tabs:
 *     - Tab 1 "Evaluate": EditorTextField + Execute button + output console
 *     - Tab 2 "Request Builder": Method dropdown, URL field, params editor, Copy button
 * 
 * Uses modern IntelliJ UI components compatible with ide.experimental.ui=true
 */
class GrailsDebugPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {
    
    private val evaluator = GrailsDebuggerEvaluator(project)
    private var currentSession: Any? = null // XDebuggerSession reference
    
    init {
        setupUI()
    }
    
    private fun setupUI() {
        // Create main splitter
        val splitter = Splitter(false, 0.3f).apply {
            minimumSize = java.awt.Dimension(400, 300)
        }
        
        // Left panel - Grails Artifacts Tree
        val leftPanel = createLeftPanel()
        
        // Right panel - Tabs
        val rightPanel = createRightPanel()
        
        splitter.firstComponent = JBScrollPane(leftPanel).apply {
            border = EmptyBorder(5, 5, 5, 5)
        }
        splitter.secondComponent = rightPanel
        
        setContent(splitter)
    }
    
    private fun createLeftPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(8, 8, 8, 8)
        }
        
        // Title label
        val titleLabel = JLabel("Grails Artifacts in Context").apply {
            font = font.deriveFont(java.awt.Font.BOLD, 12f)
            border = EmptyBorder(0, 0, 8, 0)
        }
        
        // Create tree model and tree
        val treeModel = GrailsArtifactTreeModel()
        val tree = SimpleTree(treeModel).apply {
            rootVisible = false
            showsRootHandles = true
            cellRenderer = GrailsArtifactTreeCellRenderer()
        }
        
        // Expand root nodes by default
        for (i in 0 until tree.rowCount) {
            tree.expandRow(i)
        }
        
        panel.add(titleLabel, BorderLayout.NORTH)
        panel.add(JBScrollPane(tree), BorderLayout.CENTER)
        
        return panel
    }
    
    private fun createRightPanel(): JComponent {
        // Create tabs using JBTabsFactory (modern API)
        val tabs = JBTabsFactory.createEditorTabs(project, null)
        
        // Tab 1: Evaluate
        val evaluateTab = createEvaluateTab()
        tabs.addTab(evaluateTab)
        
        // Tab 2: Request Builder
        val requestBuilderTab = createRequestBuilderTab()
        tabs.addTab(requestBuilderTab)
        
        return tabs.component
    }
    
    private fun createEvaluateTab(): com.intellij.ui.tabs.TabInfo {
        val panel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(8, 8, 8, 8)
        }
        
        // Code editor field with Groovy highlighting
        val editorField = EditorTextField("", project, org.jetbrains.kotlin.idea.KotlinLanguage.INSTANCE).apply {
            setOneLineMode(false)
            isViewer = false
            preferredSize = java.awt.Dimension(400, 150)
            border = BorderFactory.createTitledBorder("Groovy Expression")
        }
        
        // Execute button
        val executeButton = JButton("Execute", AllIcons.Actions.Execute).apply {
            toolTipText = "Execute Groovy expression in current debug context"
            addActionListener {
                executeExpression(editorField.text)
            }
        }
        
        // Output console (read-only)
        val outputArea = JBTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = java.awt.Font("JetBrains Mono", java.awt.Font.PLAIN, 12)
            background = JBColor.background()
            border = EmptyBorder(5, 5, 5, 5)
        }
        
        // Button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(executeButton)
            add(JButton("Clear", AllIcons.Actions.GC).apply {
                addActionListener { outputArea.text = "" }
            })
        }
        
        // Layout
        val centerPanel = JPanel(BorderLayout()).apply {
            add(editorField, BorderLayout.NORTH)
            add(buttonPanel, BorderLayout.CENTER)
        }
        
        panel.add(centerPanel, BorderLayout.NORTH)
        panel.add(JBScrollPane(outputArea), BorderLayout.CENTER)
        
        val tabInfo = com.intellij.ui.tabs.TabInfo(panel).apply {
            setText("Evaluate")
            setIcon(AllIcons.Debugger.EvalExpression)
            setTooltipText("Evaluate Groovy expressions in Grails context")
        }
        
        return tabInfo
    }
    
    private fun createRequestBuilderTab(): com.intellij.ui.tabs.TabInfo {
        val panel = JPanel(GridBagLayout()).apply {
            border = EmptyBorder(8, 8, 8, 8)
        }
        
        val gbc = java.awt.GridBagConstraints()
        gbc.insets = java.awt.Insets(5, 5, 5, 5)
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL
        
        // Method dropdown
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JLabel("Method:"), gbc)
        
        val methodCombo = JComboBox<String>(arrayOf("GET", "POST", "PUT", "DELETE", "PATCH")).apply {
            selectedIndex = 0
            preferredSize = java.awt.Dimension(100, 28)
        }
        gbc.gridx = 1
        gbc.weightx = 0.3
        panel.add(methodCombo, gbc)
        
        // URL field
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        panel.add(JLabel("URL:"), gbc)
        
        val urlField = JTextField().apply {
            preferredSize = java.awt.Dimension(300, 28)
            toolTipText = "Auto-filled from debug context when available"
        }
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(urlField, gbc)
        
        // Params/Body editor
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = java.awt.GridBagConstraints.BOTH
        
        val paramsEditor = EditorTextField("", project, org.jetbrains.kotlin.idea.KotlinLanguage.INSTANCE).apply {
            setOneLineMode(false)
            isViewer = false
            preferredSize = java.awt.Dimension(400, 200)
            border = BorderFactory.createTitledBorder("Parameters / JSON Body")
        }
        panel.add(paramsEditor, gbc)
        
        // Action buttons
        gbc.gridy = 3
        gbc.weighty = 0.0
        gbc.fill = java.awt.GridBagConstraints.NONE
        gbc.anchor = java.awt.GridBagConstraints.EAST
        
        val buttonPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.RIGHT)).apply {
            add(JButton("Copy to Cool Request", AllIcons.Webreferences.Server).apply {
                toolTipText = "Copy formatted request to clipboard for Cool Request plugin"
                addActionListener {
                    copyToCoolRequest(methodCombo.selectedItem as String, urlField.text, paramsEditor.text)
                }
            })
            add(JButton("Generate cURL", AllIcons.Debugger.Db_mute_breakpoint).apply {
                addActionListener {
                    generateCurl(methodCombo.selectedItem as String, urlField.text, paramsEditor.text)
                }
            })
        }
        panel.add(buttonPanel, gbc)
        
        val tabInfo = com.intellij.ui.tabs.TabInfo(panel).apply {
            setText("Request Builder")
            setIcon(AllIcons.Webreferences.Server)
            setTooltipText("Build HTTP requests from controller context")
        }
        
        return tabInfo
    }
    
    private fun executeExpression(expression: String) {
        // Execute in background to prevent UI freezing
        evaluator.evaluateExpression(expression, currentSession) { result ->
            SwingUtilities.invokeLater {
                // Find output area and update it
                // This is a simplified implementation
                println("Evaluation result: $result")
            }
        }
    }
    
    private fun copyToCoolRequest(method: String, url: String, params: String) {
        val requestSnippet = buildString {
            appendLine("### $method Request")
            appendLine("$method $url")
            if (params.isNotBlank()) {
                appendLine("Content-Type: application/json")
                appendLine()
                appendLine(params)
            }
        }
        
        // Copy to clipboard
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable = java.awt.datatransfer.StringSelection(requestSnippet)
        clipboard.setContents(transferable, null)
        
        JOptionPane.showMessageDialog(
            this,
            "Request copied to clipboard!\nPaste into Cool Request or HTTP Client.",
            "Copied Successfully",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
    
    private fun generateCurl(method: String, url: String, params: String) {
        val curlCommand = buildString {
            append("curl -X $method")
            if (params.isNotBlank()) {
                append(" -H \"Content-Type: application/json\"")
                append(" -d '$params'")
            }
            append(" \"$url\"")
        }
        
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable = java.awt.datatransfer.StringSelection(curlCommand)
        clipboard.setContents(transferable, null)
        
        JOptionPane.showMessageDialog(
            this,
            "cURL command copied to clipboard!",
            "cURL Generated",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
    
    /**
     * Update the UI with current debug session information
     */
    fun updateFromDebugSession(session: Any?) {
        currentSession = session
        // Refresh tree and pre-fill request builder fields
        // Implementation depends on session type
    }
}
