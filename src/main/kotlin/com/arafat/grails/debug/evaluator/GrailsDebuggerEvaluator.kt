/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.evaluator

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Grails-specific debugger evaluator that understands Grails artifacts and context.
 * 
 * This class extends IntelliJ's XDebuggerEvaluator to provide:
 * - Auto-import of common Grails packages (grails.gorm.*, grails.web.*, etc.)
 * - Special handling for Grails domain classes, services, and controllers
 * - Evaluation of Groovy expressions in the current debug frame context
 * 
 * Usage:
 * ```kotlin
 * val evaluator = GrailsDebuggerEvaluator(project)
 * evaluator.evaluateExpression("User.list()", session) { result ->
 *     println("Result: $result")
 * }
 * ```
 */
class GrailsDebuggerEvaluator(private val project: Project) {
    
    companion object {
        // Common Grails imports to auto-inject during evaluation
        private val GRAILS_AUTO_IMPORTS = listOf(
            "import grails.gorm.*",
            "import grails.gorm.services.*",
            "import grails.web.mapping.*",
            "import grails.web.servlet.*",
            "import grails.artefact.*",
            "import org.springframework.web.context.request.RequestContextHolder",
            "import static grails.gorm.DetachedCriteria.build"
        )
        
        // Grails-specific utility methods available in evaluation context
        private val GRAILS_CONTEXT_METHODS = listOf(
            "def getGrailsApplication() { grailsApplication }",
            "def getRequest() { RequestContextHolder.currentRequestAttributes() }",
            "def getSession() { RequestContextHolder.currentRequestAttributes().session }"
        )
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Evaluate a Groovy expression in the current debug session context.
     * 
     * @param expression The Groovy expression to evaluate
     * @param session The current XDebugSession (can be null if not debugging)
     * @param callback Called with the evaluation result
     */
    fun evaluateExpression(
        expression: String,
        session: Any?,
        callback: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = evaluateExpressionInternal(expression, session)
                withContext(Dispatchers.Main) {
                    callback(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("Error: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Internal evaluation logic with Grails context enhancement.
     */
    private suspend fun evaluateExpressionInternal(
        expression: String,
        session: Any?
    ): String = suspendCoroutine { continuation ->
        
        // If we have an active debug session, use the debugger's evaluator
        val xDebugSession = session as? XDebugSession
        
        if (xDebugSession != null) {
            // Get the current stack frame
            val stackFrame = xDebugSession.currentStackFrame
            
            if (stackFrame != null) {
                // Prepare expression with Grails auto-imports
                val enhancedExpression = prepareGrailsExpression(expression)
                
                // Use the debugger's built-in evaluator
                stackFrame.computeValue(
                    enhancedExpression,
                    object : XDebuggerEvaluator.XEvaluationCallback {
                        override fun evaluated(value: XValue) {
                            // Convert XValue to string representation
                            value.computePresentation(
                                object : com.intellij.xdebugger.frame.XValuePresenter() {
                                    override fun presentationTextReady(text: String) {
                                        continuation.resume(text)
                                    }
                                },
                                com.intellij.xdebugger.frame.XValuePlace.TREE
                            )
                        }
                        
                        override fun errorOccurred(errorMessage: String) {
                            continuation.resume("Evaluation Error: $errorMessage")
                        }
                    }
                )
                return@suspendCoroutine
            }
        }
        
        // No active debug session - return informational message
        continuation.resume(
            "No active debug session. Start debugging a Grails application to evaluate expressions.\n\n" +
            "Available Grails context:\n${GRAILS_AUTO_IMPORTS.joinToString("\n")}"
        )
    }
    
    /**
     * Prepare a Groovy expression by adding Grails-specific imports and context.
     */
    private fun prepareGrailsExpression(expression: String): String {
        val imports = GRAILS_AUTO_IMPORTS.joinToString("\n")
        val contextMethods = GRAILS_CONTEXT_METHODS.joinToString("\n")
        
        return buildString {
            appendLine("// Grails Debug Assistant Context")
            appendLine(imports)
            appendLine()
            appendLine("// Available context methods:")
            appendLine(contextMethods)
            appendLine()
            appendLine("// User expression:")
            append(expression)
        }
    }
    
    /**
     * Evaluate a Groovy script file against the current debug context.
     * 
     * @param scriptContent The full script content to execute
     * @param session The current debug session
     * @return Result of script execution
     */
    suspend fun evaluateScript(scriptContent: String, session: XDebugSession?): String {
        return withContext(Dispatchers.IO) {
            if (session == null) {
                return@withContext "No active debug session"
            }
            
            val stackFrame = session.currentStackFrame ?: return@withContext "No stack frame available"
            
            // Wrap script in Grails context
            val wrappedScript = prepareGrailsExpression(scriptContent)
            
            // Execute via debugger
            try {
                suspendCoroutine<String> { continuation ->
                    stackFrame.computeValue(
                        wrappedScript,
                        object : XDebuggerEvaluator.XEvaluationCallback {
                            override fun evaluated(value: XValue) {
                                value.computePresentation(
                                    object : com.intellij.xdebugger.frame.XValuePresenter() {
                                        override fun presentationTextReady(text: String) {
                                            continuation.resume(text)
                                        }
                                    },
                                    com.intellij.xdebugger.frame.XValuePlace.TREE
                                )
                            }
                            
                            override fun errorOccurred(errorMessage: String) {
                                continuation.resume("Script Error: $errorMessage")
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                "Script execution failed: ${e.message}"
            }
        }
    }
    
    /**
     * Get available Grails artifacts in the current context.
     * This can be used to populate the artifact tree.
     */
    fun getGrailsArtifacts(session: XDebugSession?): GrailsContextInfo {
        if (session == null) {
            return GrailsContextInfo(null, emptyList(), emptyList(), emptyList())
        }
        
        // In a real implementation, this would inspect the debug session
        // to find actual Grails artifacts in scope
        val stackFrame = session.currentStackFrame
        
        // Placeholder - would need actual PSI/debugger integration
        return GrailsContextInfo(
            controllerName = extractControllerName(stackFrame),
            domainClasses = emptyList(),
            services = emptyList(),
            taglibs = emptyList()
        )
    }
    
    private fun extractControllerName(stackFrame: com.intellij.xdebugger.frame.XStackFrame?): String? {
        // Extract controller name from stack frame
        // This is a simplified implementation
        return stackFrame?.toString()?.substringBeforeLast('.')?.substringAfterLast('/')
    }
    
    /**
     * Data class holding Grails context information
     */
    data class GrailsContextInfo(
        val controllerName: String?,
        val domainClasses: List<String>,
        val services: List<String>,
        val taglibs: List<String>
    )
    
    /**
     * Dispose resources when plugin is unloaded
     */
    fun dispose() {
        coroutineScope.cancel()
    }
}
