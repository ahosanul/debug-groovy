/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.ui

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element
import java.awt.Component
import java.beans.PropertyChangeListener
import javax.swing.JPanel

/**
 * File editor provider for Grails script files.
 * 
 * Provides a specialized editor for executing Groovy scripts in the context
 * of a Grails debug session (Hot-Script Execution feature).
 */
class GrailsScriptEditorProvider : FileEditorProvider {
    
    companion object {
        const val EDITOR_TYPE_ID = "grails-debug-script-editor"
    }
    
    override fun getEditorTypeId(): String = EDITOR_TYPE_ID
    
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
    
    override fun accept(project: Project, file: VirtualFile): Boolean {
        // Accept .groovy files that are scripts (not classes)
        return file.extension == "groovy" && 
               (file.name.endsWith(".groovy") || isGrailsScript(file, project))
    }
    
    private fun isGrailsScript(file: VirtualFile, project: Project): Boolean {
        // Check if file is in a Grails script directory
        val path = file.path
        
        return path.contains("/scripts/") ||
               path.contains("/grails-app/scripts/") ||
               path.contains("src/main/groovy/scripts")
    }
    
    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        // Return a custom editor for Grails scripts
        // This would be a full implementation with execution capabilities
        return GrailsScriptEditor(project, file)
    }
}

/**
 * Custom file editor for Grails scripts with debug session integration.
 */
class GrailsScriptEditor(
    private val project: Project,
    private val file: VirtualFile
) : FileEditor {
    
    private var modified = false
    private val listeners = mutableListOf<(FileEditorStateLevel) -> Unit>()
    private val userData = mutableMapOf<String, Any?>()
    
    override fun getComponent(): JPanel {
        // Return the main editor component
        // Would contain EditorTextField with Groovy highlighting
        return JPanel()
    }
    
    override fun getPreferredFocusedComponent(): JPanel? {
        return component
    }
    
    override fun getName(): String = "Grails Script"
    
    override fun setState(state: FileEditorState) {
        // Restore editor state
    }
    
    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return FileEditorState.INSTANCE
    }
    
    override fun isModified(): Boolean = modified
    
    override fun isValid(): Boolean = file.isValid
    
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        // Add property change listener
    }
    
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        // Remove property change listener
    }
    
    override fun selectNotify() {
        // Called when editor is selected
    }
    
    override fun deselectNotify() {
        // Called when editor is deselected
    }
    
    override fun dispose() {
        // Clean up resources
        listeners.clear()
    }
    
    override fun <T> getUserData(key: com.intellij.openapi.util.Key<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return userData[key.toString()] as T?
    }
    
    override fun <T> putUserData(key: com.intellij.openapi.util.Key<T>, value: T?) {
        userData[key.toString()] = value
    }
    
    /**
     * Execute the script in the current debug session context
     */
    fun executeScriptInDebugContext() {
        // Get current debug session
        val debuggerManager = com.intellij.xdebugger.XDebuggerManager.getInstance(project)
        val session = debuggerManager.currentSession
        
        if (session == null) {
            javax.swing.JOptionPane.showMessageDialog(
                component,
                "No active debug session. Start debugging your Grails application first.",
                "No Debug Session",
                javax.swing.JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        // Read script content
        val scriptContent = String(file.contentsToByteArray())
        
        // Execute using GrailsDebuggerEvaluator
        // val evaluator = GrailsDebuggerEvaluator(project)
        // evaluator.evaluateScript(scriptContent, session) { result -> ... }
        
        println("Executing script: $scriptContent")
    }
}
