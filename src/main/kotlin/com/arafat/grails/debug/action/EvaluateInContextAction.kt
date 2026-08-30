/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * Action to evaluate a Groovy expression in the current Grails debug context.
 * 
 * Opens the Grails Debug tool window and focuses the Evaluate tab.
 */
class EvaluateInContextAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // Show the Grails Debug tool window
        showGrailsDebugWindow(project)
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.project
        
        if (project == null) {
            presentation.isEnabled = false
            return
        }
        
        // Check if we're in an active debug session
        val debuggerManager = com.intellij.xdebugger.XDebuggerManager.getInstance(project)
        val isInDebugSession = debuggerManager.currentSession != null
        
        presentation.isEnabled = isInDebugSession
        presentation.text = if (isInDebugSession) "Evaluate in Grails Context" else "Start Debugging First"
    }
    
    private fun showGrailsDebugWindow(project: Project) {
        val toolWindowManager = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Grails Debug")
        
        if (toolWindow != null) {
            toolWindow.show()
        } else {
            Messages.showInfoMessage(
                project,
                "The Grails Debug tool window is not available.\n" +
                "Make sure you're debugging a Grails application.",
                "Grails Debug Assistant"
            )
        }
    }
}
