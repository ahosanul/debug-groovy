/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

/**
 * Action to navigate to the GSP view associated with the current controller action.
 * 
 * When debugging a Grails controller, this action finds and opens the corresponding
 * GSP view file (if it exists).
 */
class NavigateToGspAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // Get current debug session to find controller/action
        val debuggerManager = com.intellij.xdebugger.XDebuggerManager.getInstance(project)
        val session = debuggerManager.currentSession ?: return
        
        val stackFrame = session.currentStackFrame ?: return
        val file = stackFrame.file ?: return
        
        // Extract controller and action names
        val controllerInfo = extractControllerInfo(file)
        
        if (controllerInfo != null) {
            // Find and navigate to GSP view
            navigateToGspView(project, controllerInfo.controllerName, controllerInfo.actionName)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.project
        
        if (project == null) {
            presentation.isEnabled = false
            presentation.isVisible = false
            return
        }
        
        // Check if we're in a Grails controller context
        val debuggerManager = com.intellij.xdebugger.XDebuggerManager.getInstance(project)
        val session = debuggerManager.currentSession
        val isInController = session != null && isGrailsControllerContext(session)
        
        presentation.isEnabled = isInController
        presentation.isVisible = true
    }
    
    private fun isGrailsControllerContext(session: com.intellij.xdebugger.XDebugSession): Boolean {
        val stackFrame = session.currentStackFrame ?: return false
        val file = stackFrame.file ?: return false
        
        return file.name.endsWith("Controller.groovy") || 
               file.name.endsWith("Controller.java")
    }
    
    private fun extractControllerInfo(file: VirtualFile): ControllerInfo? {
        val fileName = file.nameWithoutExtension
        
        if (!fileName.endsWith("Controller")) {
            return null
        }
        
        val controllerName = fileName.removeSuffix("Controller")
        
        // Action name would need to be extracted from the stack frame method
        // For now, use a placeholder
        val actionName = "index" // Would need actual method name extraction
        
        return ControllerInfo(controllerName, actionName)
    }
    
    private fun navigateToGspView(project: Project, controllerName: String, actionName: String) {
        // Convert controller name to view path (convention over configuration)
        // Example: UserController -> /user/index.gsp
        val viewPath = "/grails-app/views/${controllerName.decapitalize()}/$actionName.gsp"
        
        // Search for the GSP file in the project
        val gspFile = findGspFile(project, viewPath)
        
        if (gspFile != null) {
            // Open the file in editor
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            fileEditorManager.openFile(gspFile, true)
        } else {
            // Try alternate location (Grails 3+ structure)
            val altViewPath = "/src/main/groovy/templates/${controllerName.decapitalize()}/$actionName.gsp"
            val altFile = findGspFile(project, altViewPath)
            
            if (altFile != null) {
                val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                fileEditorManager.openFile(altFile, true)
            } else {
                com.intellij.openapi.ui.Messages.showWarningDialog(
                    project,
                    "Could not find GSP view for $controllerName.$actionName\n" +
                    "Searched:\n$viewPath\n$altViewPath",
                    "GSP View Not Found"
                )
            }
        }
    }
    
    private fun findGspFile(project: Project, path: String): VirtualFile? {
        // Search in project roots
        val psiManager = PsiManager.getInstance(project)
        
        // Try to find by relative path from content roots
        com.intellij.openapi.roots.ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
            val file = root.findFileByRelativePath(path.trimStart('/'))
            if (file != null) {
                return file
            }
        }
        
        // Also search in module roots
        com.intellij.openapi.roots.ModuleRootManager.getInstance(
            com.intellij.openapi.roots.ProjectRootManager.getInstance(project).projectFileIndex.contentRoots.firstOrNull() 
                ?: return null
        ).contentRoots.forEach { root ->
            val file = root.findFileByRelativePath(path.trimStart('/'))
            if (file != null) {
                return file
            }
        }
        
        return null
    }
    
    data class ControllerInfo(
        val controllerName: String,
        val actionName: String
    )
}
