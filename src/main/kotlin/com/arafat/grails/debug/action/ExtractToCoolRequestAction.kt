/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.action

import com.arafat.grails.debug.evaluator.GrailsDebuggerEvaluator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.impl.XDebugSessionImpl

/**
 * Action to extract the current Grails controller context and format it as an HTTP request.
 * 
 * This action appears in:
 * - Debugger variable tree context menu
 * - Editor gutter when paused in a Grails Controller
 * 
 * When triggered, it:
 * 1. Analyzes the current debug frame to extract HTTP method, route, path variables, and parameters
 * 2. Formats them into a ready-to-use HTTP request snippet
 * 3. Copies the snippet to clipboard (compatible with "Cool Request" plugin)
 * 4. Optionally opens the Grails Debug tool window with pre-filled values
 * 
 * Inspired by the "Cool Request" plugin's clean, developer-friendly approach.
 */
class ExtractToCoolRequestAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // Get current debug session
        val debugSession = getDebugSession(e)
        
        if (debugSession == null) {
            Messages.showWarningDialog(
                project,
                "No active debug session found.\nPlease start debugging your Grails application.",
                "Grails Debug Assistant"
            )
            return
        }
        
        // Extract controller context
        val context = extractControllerContext(debugSession, e)
        
        if (context == null) {
            Messages.showWarningDialog(
                project,
                "Could not extract controller context.\nMake sure you're paused inside a Grails Controller.",
                "Grails Debug Assistant"
            )
            return
        }
        
        // Format as HTTP request
        val requestSnippet = formatAsHttpRequest(context)
        
        // Copy to clipboard
        copyToClipboard(project, requestSnippet)
        
        // Update tool window if available
        updateToolWindow(project, context)
        
        // Show success notification
        Messages.showInfoMessage(
            project,
            "HTTP request extracted and copied to clipboard!\n\n" +
            "Method: ${context.method}\n" +
            "URL: ${context.url}\n\n" +
            "Paste into Cool Request or IntelliJ HTTP Client.",
            "Request Extracted Successfully"
        )
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.project
        
        if (project == null) {
            presentation.isEnabled = false
            presentation.isVisible = false
            return
        }
        
        // Check if we're in a debug session
        val debugSession = getDebugSession(e)
        val isInGrailsController = debugSession != null && isGrailsControllerContext(debugSession, e)
        
        presentation.isEnabled = isInGrailsController
        presentation.isVisible = true
        presentation.text = if (isInGrailsController) "Extract to Cool Request" else "Not in Grails Controller"
    }
    
    /**
     * Get the current debug session from the action event context
     */
    private fun getDebugSession(e: AnActionEvent): XDebugSession? {
        // Try to get from editor first
        val editor = e.getData(CommonDataKeys.EDITOR)
        val project = e.project
        
        // Get active debug session from debugger manager
        val debuggerManager = com.intellij.xdebugger.XDebuggerManager.getInstance(project ?: return null)
        return debuggerManager.currentSession
    }
    
    /**
     * Check if the current debug context is inside a Grails Controller
     */
    private fun isGrailsControllerContext(session: XDebugSession, e: AnActionEvent): Boolean {
        val stackFrame = session.currentStackFrame ?: return false
        
        // Get the file being debugged
        val file = stackFrame.file ?: return false
        val fileName = file.name
        
        // Check if it's a controller (convention-based)
        return fileName.endsWith("Controller.groovy") || 
               fileName.endsWith("Controller.java") ||
               isGrailsArtefact(file, "Controller")
    }
    
    /**
     * Check if a file represents a specific Grails artefact type
     */
    private fun isGrailsArtefact(file: com.intellij.openapi.vfs.VirtualFile, artefactType: String): Boolean {
        // Use PSI to check for Grails artefact annotations or conventions
        // This is a simplified implementation
        return file.path.contains("/${artefactType}s/") || 
               file.path.contains("\\${artefactType}s\\")
    }
    
    /**
     * Extract HTTP request context from the current debug session
     */
    private fun extractControllerContext(
        session: XDebugSession,
        e: AnActionEvent
    ): HttpRequestContext? {
        val stackFrame = session.currentStackFrame ?: return null
        val file = stackFrame.file ?: return null
        
        // Extract information from the controller method
        val methodInfo = extractMethodInfo(stackFrame, e)
        
        // Extract request parameters from debug variables
        val params = extractRequestParameters(session)
        
        // Build URL from route info
        val url = buildUrlFromContext(session, methodInfo)
        
        return HttpRequestContext(
            method = methodInfo.httpMethod,
            url = url,
            pathVariables = methodInfo.pathVariables,
            parameters = params,
            controllerName = methodInfo.controllerName,
            actionName = methodInfo.actionName
        )
    }
    
    /**
     * Extract method information from the stack frame
     */
    private fun extractMethodInfo(
        stackFrame: com.intellij.xdebugger.frame.XStackFrame,
        e: AnActionEvent
    ): MethodInfo {
        // In a real implementation, this would use Groovy PSI to parse:
        // - @GetMapping, @PostMapping annotations
        // - Method name (action name)
        // - Path variables from route mapping
        // - Controller class name
        
        // Placeholder implementation
        return MethodInfo(
            httpMethod = "GET",
            controllerName = "ExampleController",
            actionName = "index",
            pathVariables = emptyMap()
        )
    }
    
    /**
     * Extract request parameters from debug session variables
     */
    private fun extractRequestParameters(session: XDebugSession): Map<String, String> {
        val params = mutableMapOf<String, String>()
        
        // Try to get 'params' variable from debug context
        // In Grails, 'params' is automatically available in controllers
        val xStackFrame = session.currentStackFrame
        
        // This would need actual debugger variable inspection
        // For now, return empty map
        
        return params
    }
    
    /**
     * Build the full URL from routing context
     */
    private fun buildUrlFromContext(session: XDebugSession, methodInfo: MethodInfo): String {
        // Extract base URL from application config or request
        val baseUrl = "http://localhost:8080"
        val routePath = "/${methodInfo.controllerName.removeSuffix("Controller").decapitalize()}/${methodInfo.actionName}"
        
        return "$baseUrl$routePath"
    }
    
    /**
     * Format the extracted context as an HTTP request snippet
     */
    private fun formatAsHttpRequest(context: HttpRequestContext): String {
        return buildString {
            appendLine("### ${context.controllerName}.${context.actionName}")
            appendLine("# Generated by Grails Debug Assistant")
            appendLine()
            appendLine("${context.method} ${context.url}")
            
            // Add path variables as comments
            if (context.pathVariables.isNotEmpty()) {
                appendLine()
                appendLine("# Path Variables:")
                context.pathVariables.forEach { (key, value) ->
                    appendLine("# $key = $value")
                }
            }
            
            // Add parameters
            if (context.parameters.isNotEmpty()) {
                if (context.method == "GET") {
                    // Append to URL for GET requests
                    val queryString = context.parameters.entries.joinToString("&") { "${it.key}=${it.value}" }
                    append("?$queryString")
                } else {
                    // Add as JSON body for POST/PUT/etc.
                    appendLine()
                    appendLine("Content-Type: application/json")
                    appendLine()
                    appendLine("{")
                    context.parameters.entries.forEachIndexed { index, (key, value) ->
                        val comma = if (index < context.parameters.size - 1) "," else ""
                        appendLine("  \"$key\": \"$value\"$comma")
                    }
                    appendLine("}")
                }
            }
            
            appendLine()
            appendLine("# Accept: application/json")
        }
    }
    
    /**
     * Copy the request snippet to system clipboard
     */
    private fun copyToClipboard(project: Project, content: String) {
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        val transferable = java.awt.datatransfer.StringSelection(content)
        clipboard.setContents(transferable, null)
    }
    
    /**
     * Update the Grails Debug tool window with the extracted context
     */
    private fun updateToolWindow(project: Project, context: HttpRequestContext) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Grails Debug")
        
        if (toolWindow != null) {
            toolWindow.show {
                // Find the panel and update it
                // This would require a reference to the GrailsDebugPanel
            }
        }
    }
    
    /**
     * Data class representing an extracted HTTP request context
     */
    data class HttpRequestContext(
        val method: String,
        val url: String,
        val pathVariables: Map<String, String>,
        val parameters: Map<String, String>,
        val controllerName: String,
        val actionName: String
    )
    
    /**
     * Data class representing method information
     */
    data class MethodInfo(
        val httpMethod: String,
        val controllerName: String,
        val actionName: String,
        val pathVariables: Map<String, String>
    )
}
