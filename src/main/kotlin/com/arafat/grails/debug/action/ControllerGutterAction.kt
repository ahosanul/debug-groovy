/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

/**
 * Editor gutter action for building Cool Requests from Grails controller methods.
 * 
 * This action appears in the editor gutter when editing a Grails controller file,
 * allowing quick extraction of HTTP request information without starting a debug session.
 */
class ControllerGutterAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE) ?: return
        
        // Check if this is a Grails controller file
        if (!isGrailsControllerFile(virtualFile)) {
            return
        }
        
        // Extract method information at caret position
        val methodInfo = extractMethodInfoAtCaret(editor, virtualFile)
        
        if (methodInfo != null) {
            // Build and copy the request
            val requestSnippet = buildRequestSnippet(project, methodInfo)
            copyToClipboard(project, requestSnippet)
            
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                "HTTP request template copied to clipboard!\n\n" +
                "Method: ${methodInfo.httpMethod}\n" +
                "URL Pattern: ${methodInfo.urlPattern}",
                "Cool Request Generated"
            )
        }
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.project
        val virtualFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)
        
        if (project == null || virtualFile == null) {
            presentation.isEnabled = false
            presentation.isVisible = false
            return
        }
        
        val isController = isGrailsControllerFile(virtualFile)
        presentation.isEnabled = isController
        presentation.isVisible = isController
    }
    
    private fun isGrailsControllerFile(file: com.intellij.openapi.vfs.VirtualFile): Boolean {
        return file.name.endsWith("Controller.groovy") || 
               file.name.endsWith("Controller.java") ||
               file.path.contains("/controllers/")
    }
    
    private fun extractMethodInfoAtCaret(
        editor: Editor,
        file: com.intellij.openapi.vfs.VirtualFile
    ): MethodInfo? {
        // Use PSI to extract method information at caret position
        val psiManager = com.intellij.psi.PsiManager.getInstance(editor.project)
        val psiFile = psiManager.findFile(file) ?: return null
        
        // Find the method element at caret
        val caretOffset = editor.caretModel.offset
        val element = psiFile.findElementAt(caretOffset)
        
        // Navigate up to find the method declaration
        var methodElement = element
        while (methodElement != null && !isMethodDeclaration(methodElement)) {
            methodElement = methodElement.parent
        }
        
        if (methodElement == null) {
            return null
        }
        
        // Extract method details
        val methodName = methodElement.text.substringBefore('(').substringAfterLast(' ').trim()
        val httpMethod = detectHttpMethod(methodElement)
        val urlPattern = buildUrlPattern(file, methodName, httpMethod)
        
        return MethodInfo(
            httpMethod = httpMethod,
            methodName = methodName,
            urlPattern = urlPattern
        )
    }
    
    private fun isMethodDeclaration(element: com.intellij.psi.PsiElement): Boolean {
        // Check if element represents a method declaration
        // This would need Groovy/Java PSI inspection
        val text = element.text
        return text.contains("def ") || 
               text.matches(Regex(".*\\s+\\w+\\s*\\([^)]*\\).*"))
    }
    
    private fun detectHttpMethod(methodElement: com.intellij.psi.PsiElement): String {
        // Look for annotations like @GetMapping, @PostMapping, etc.
        val text = methodElement.text
        
        return when {
            text.contains("@GetMapping") || text.contains("@RESTful") -> "GET"
            text.contains("@PostMapping") -> "POST"
            text.contains("@PutMapping") -> "PUT"
            text.contains("@DeleteMapping") -> "DELETE"
            text.contains("@PatchMapping") -> "PATCH"
            else -> "GET" // Default for Grails actions
        }
    }
    
    private fun buildUrlPattern(
        file: com.intellij.openapi.vfs.VirtualFile,
        methodName: String,
        httpMethod: String
    ): String {
        val fileName = file.nameWithoutExtension
        val controllerName = fileName.removeSuffix("Controller").decapitalize()
        
        // Grails convention: /controllerName/actionName
        return "/$controllerName/$methodName"
    }
    
    private fun buildRequestSnippet(project: Project, methodInfo: MethodInfo): String {
        return buildString {
            appendLine("### ${methodInfo.methodName}")
            appendLine("# Generated by Grails Debug Assistant - Cool Request Integration")
            appendLine()
            appendLine("${methodInfo.httpMethod} http://localhost:8080${methodInfo.urlPattern}")
            appendLine()
            appendLine("# Headers")
            appendLine("Accept: application/json")
            appendLine()
            appendLine("# Parameters (add as needed)")
            appendLine("# ?param1=value1&param2=value2")
        }
    }
    
    private fun copyToClipboard(project: Project, content: String) {
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        val transferable = java.awt.datatransfer.StringSelection(content)
        clipboard.setContents(transferable, null)
    }
    
    data class MethodInfo(
        val httpMethod: String,
        val methodName: String,
        val urlPattern: String
    )
}
