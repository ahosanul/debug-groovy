/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug

import com.arafat.grails.debug.ui.GrailsDebugPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener

/**
 * Listener for debugger session events.
 * 
 * This class monitors debug session lifecycle and updates the Grails Debug tool window
 * when sessions start, stop, or pause at breakpoints.
 * 
 * Key responsibilities:
 * - Detect when debugging a Grails application
 * - Update the artifact tree with current context
 * - Pre-fill the Request Builder with controller information
 * - Enable/disable UI components based on session state
 */
class GrailsDebuggerSessionListener : XDebugSessionListener {
    
    override fun sessionCreated(session: XDebugSession) {
        // Debug session started
        println("Grails Debug Assistant: Debug session created")
        
        // Check if this is a Groovy/Grails debug session
        if (isGrailsDebugSession(session)) {
            updateToolWindowForSession(session)
        }
    }
    
    override fun sessionPaused(session: XDebugSession) {
        // Debugger paused at breakpoint
        println("Grails Debug Assistant: Session paused")
        
        if (isGrailsDebugSession(session)) {
            // Update UI with current stack frame information
            updateToolWindowForPausedSession(session)
        }
    }
    
    override fun sessionResumed(session: XDebugSession) {
        // Debugger resumed execution
        println("Grails Debug Assistant: Session resumed")
    }
    
    override fun sessionStopped(session: XDebugSession) {
        // Debug session ended
        println("Grails Debug Assistant: Session stopped")
        
        // Disable tool window controls
        disableToolWindowControls(session.project)
    }
    
    /**
     * Check if the current debug session is for a Grails application
     */
    private fun isGrailsDebugSession(session: XDebugSession): Boolean {
        val stackFrame = session.currentStackFrame ?: return false
        val file = stackFrame.file ?: return false
        
        // Check for Grails-specific indicators
        return file.name.endsWith(".groovy") ||
               file.path.contains("/grails-app/") ||
               file.path.contains("/src/main/groovy/") ||
               hasGrailsClassesInProject(session.project)
    }
    
    /**
     * Check if the project contains Grails classes
     */
    private fun hasGrailsClassesInProject(project: Project): Boolean {
        // Search for Grails artefact markers
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)
        val contentRoots = com.intellij.openapi.roots.ProjectRootManager.getInstance(project).contentRoots
        
        for (root in contentRoots) {
            val grailsAppDir = root.findFileByRelativePath("grails-app")
            if (grailsAppDir != null && grailsAppDir.isDirectory) {
                return true
            }
            
            // Also check for build.gradle with grails plugin
            val buildGradle = root.findFileByRelativePath("build.gradle")
            if (buildGradle != null) {
                val content = buildGradle.inputStream?.bufferedReader()?.readText() ?: ""
                if (content.contains("grails") || content.contains("org.grails")) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Update the tool window when a session is active
     */
    private fun updateToolWindowForSession(session: XDebugSession) {
        val project = session.project
        
        // Show tool window
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Grails Debug")
        
        if (toolWindow != null) {
            toolWindow.show()
            
            // Get the panel and update it
            updatePanelWithSession(toolWindow, session)
        }
    }
    
    /**
     * Update the tool window when paused at a breakpoint
     */
    private fun updateToolWindowForPausedSession(session: XDebugSession) {
        val project = session.project
        
        // Extract controller/action information from stack frame
        val contextInfo = extractGrailsContextFromSession(session)
        
        // Update tool window
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Grails Debug")
        
        if (toolWindow != null) {
            updatePanelWithContext(toolWindow, contextInfo)
        }
    }
    
    /**
     * Extract Grails context information from the debug session
     */
    private fun extractGrailsContextFromSession(session: XDebugSession): GrailsContextInfo {
        val stackFrame = session.currentStackFrame ?: return GrailsContextInfo()
        val file = stackFrame.file ?: return GrailsContextInfo()
        
        val fileName = file.nameWithoutExtension
        val controllerName = if (fileName.endsWith("Controller")) {
            fileName.removeSuffix("Controller")
        } else {
            null
        }
        
        // Extract action name from method (would need PSI analysis)
        val actionName = "index" // Placeholder
        
        // Extract request parameters from debug variables
        val params = extractParamsFromDebugVariables(session)
        
        return GrailsContextInfo(
            controllerName = controllerName,
            actionName = actionName,
            httpMethod = params["method"] ?: "GET",
            pathVariables = params.filterKeys { it.startsWith("path:") }.mapValues { it.value },
            requestParams = params.filterKeys { !it.startsWith("path:") && it != "method" }
        )
    }
    
    /**
     * Extract request parameters from debug session variables
     */
    private fun extractParamsFromDebugVariables(session: XDebugSession): Map<String, String> {
        val params = mutableMapOf<String, String>()
        
        // Try to get 'params' variable from the current frame
        // In Grails controllers, 'params' contains all request parameters
        
        // This would need actual debugger variable inspection
        // For now, return empty map as placeholder
        
        return params
    }
    
    /**
     * Update the tool window panel with session information
     */
    private fun updatePanelWithSession(toolWindow: com.intellij.openapi.wm.ToolWindow, session: XDebugSession) {
        // Get content and find the GrailsDebugPanel
        val contentManager = toolWindow.contentManager
        if (contentManager.contentCount > 0) {
            val content = contentManager.getContent(0)
            val component = content?.component
            
            if (component is GrailsDebugPanel) {
                component.updateFromDebugSession(session)
            }
        }
    }
    
    /**
     * Update the tool window panel with Grails context information
     */
    private fun updatePanelWithContext(toolWindow: com.intellij.openapi.wm.ToolWindow, context: GrailsContextInfo) {
        // Get content and find the GrailsDebugPanel
        val contentManager = toolWindow.contentManager
        if (contentManager.contentCount > 0) {
            val content = contentManager.getContent(0)
            val component = content?.component
            
            if (component is GrailsDebugPanel) {
                // Would need a more specific update method
                component.updateFromDebugSession(null)
            }
        }
    }
    
    /**
     * Disable tool window controls when no session is active
     */
    private fun disableToolWindowControls(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Grails Debug")
        
        if (toolWindow != null) {
            // Could disable controls here, but better to leave them accessible
            // with a message indicating no active session
        }
    }
    
    /**
     * Data class holding Grails context information extracted from debug session
     */
    data class GrailsContextInfo(
        val controllerName: String? = null,
        val actionName: String? = null,
        val httpMethod: String = "GET",
        val pathVariables: Map<String, String> = emptyMap(),
        val requestParams: Map<String, String> = emptyMap()
    )
}
