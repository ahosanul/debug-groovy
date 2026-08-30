/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

/**
 * Utility class for locating GSP views associated with controller actions.
 * 
 * Uses Grails convention-over-configuration to map:
 * - Controller name → View directory
 * - Action name → View file
 * 
 * Example:
 * - UserController.show() → /grails-app/views/user/show.gsp
 * - BookController.list() → /grails-app/views/book/list.gsp
 */
class GspViewLocator {
    
    companion object {
        // Common GSP view locations for different Grails versions
        private val VIEW_PATHS = listOf(
            "grails-app/views",           // Grails 3+ standard
            "src/main/groovy/templates",  // Alternative location
            "views"                       // Legacy/simplified
        )
        
        // File extensions for GSP views
        private val GSP_EXTENSIONS = listOf("gsp", "html")
    }
    
    /**
     * Find the GSP view for a given controller and action
     * 
     * @param project The IntelliJ project
     * @param controllerName The controller name (e.g., "User" from UserController)
     * @param actionName The action name (e.g., "show", "list", "index")
     * @return The VirtualFile of the GSP view, or null if not found
     */
    fun findGspView(project: Project, controllerName: String, actionName: String): VirtualFile? {
        // Convert controller name to view directory (lowercase first letter)
        val viewDirName = controllerName.decapitalize()
        
        // Try each possible view path
        for (basePath in VIEW_PATHS) {
            for (extension in GSP_EXTENSIONS) {
                val viewPath = "$basePath/$viewDirName/$actionName.$extension"
                val viewFile = findFileByRelativePath(project, viewPath)
                if (viewFile != null) {
                    return viewFile
                }
            }
        }
        
        // Try without subdirectory (for single-level views)
        for (basePath in VIEW_PATHS) {
            for (extension in GSP_EXTENSIONS) {
                val viewPath = "$basePath/${controllerName.decapitalize()}.$extension"
                val viewFile = findFileByRelativePath(project, viewPath)
                if (viewFile != null) {
                    return viewFile
                }
            }
        }
        
        return null
    }
    
    /**
     * Find all GSP views for a given controller
     * 
     * @param project The IntelliJ project
     * @param controllerName The controller name
     * @return List of all GSP view files for this controller
     */
    fun findAllViewsForController(project: Project, controllerName: String): List<VirtualFile> {
        val views = mutableListOf<VirtualFile>()
        val viewDirName = controllerName.decapitalize()
        
        for (basePath in VIEW_PATHS) {
            val viewDir = findDirectoryByRelativePath(project, "$basePath/$viewDirName")
            if (viewDir != null && viewDir.isDirectory) {
                viewDir.children.forEach { file ->
                    if (file.extension in GSP_EXTENSIONS) {
                        views.add(file)
                    }
                }
            }
        }
        
        return views
    }
    
    /**
     * Find the controller associated with a GSP view
     * 
     * @param gspFile The GSP view file
     * @return The controller name, or null if cannot be determined
     */
    fun findAssociatedController(gspFile: VirtualFile): String? {
        val parent = gspFile.parent ?: return null
        val parentName = parent.name
        
        // Check if parent directory name matches a controller pattern
        if (isLikelyControllerName(parentName)) {
            return parentName.capitalize() + "Controller"
        }
        
        return null
    }
    
    /**
     * Find taglib files used in a GSP view
     * 
     * @param project The IntelliJ project
     * @param gspFile The GSP view file
     * @return List of taglib files referenced in the view
     */
    fun findTaglibsForView(project: Project, gspFile: VirtualFile): List<VirtualFile> {
        val taglibs = mutableListOf<VirtualFile>()
        
        // Parse GSP content to find taglib references
        val content = try {
            String(gspFile.contentsToByteArray())
        } catch (e: Exception) {
            return emptyList()
        }
        
        // Look for <g:*, <sec:*, etc. patterns
        val taglibPattern = Regex("""<(\w+):""")
        val matches = taglibPattern.findAll(content)
        
        val taglibPrefixes = matches.map { it.groupValues[1] }.toSet()
        
        // Search for matching taglib files
        for (prefix in taglibPrefixes) {
            val taglibFile = findTaglibByPrefix(project, prefix)
            if (taglibFile != null) {
                taglibs.add(taglibFile)
            }
        }
        
        return taglibs
    }
    
    /**
     * Find a taglib file by its prefix
     */
    private fun findTaglibByPrefix(project: Project, prefix: String): VirtualFile? {
        // Common taglib naming conventions
        val possibleNames = listOf(
            "${prefix.capitalize()}TagLib.groovy",
            "${prefix}TagLib.groovy",
            "${prefix}.taglib.groovy"
        )
        
        val taglibDir = findDirectoryByRelativePath(project, "grails-app/taglib")
        if (taglibDir == null || !taglibDir.isDirectory) {
            return null
        }
        
        for (name in possibleNames) {
            val file = taglibDir.findChild(name)
            if (file != null && file.isValid) {
                return file
            }
        }
        
        return null
    }
    
    /**
     * Navigate from controller action to its GSP view
     * This is a convenience method that combines finding and opening the view
     */
    fun navigateToView(project: Project, controllerName: String, actionName: String): Boolean {
        val viewFile = findGspView(project, controllerName, actionName)
        
        if (viewFile != null) {
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            fileEditorManager.openFile(viewFile, true)
            return true
        }
        
        return false
    }
    
    /**
     * Check if a string looks like a controller name
     */
    private fun isLikelyControllerName(name: String): Boolean {
        // Simple heuristic: lowercase, alphanumeric
        return name.matches(Regex("[a-z][a-zA-Z0-9]*"))
    }
    
    /**
     * Find a file by relative path from any content root
     */
    private fun findFileByRelativePath(project: Project, relativePath: String): VirtualFile? {
        val contentRoots = com.intellij.openapi.roots.ProjectRootManager.getInstance(project).contentRoots
        
        for (root in contentRoots) {
            val file = root.findFileByRelativePath(relativePath.trimStart('/'))
            if (file != null && file.isValid) {
                return file
            }
        }
        
        return null
    }
    
    /**
     * Find a directory by relative path from any content root
     */
    private fun findDirectoryByRelativePath(project: Project, relativePath: String): VirtualFile? {
        val contentRoots = com.intellij.openapi.roots.ProjectRootManager.getInstance(project).contentRoots
        
        for (root in contentRoots) {
            val dir = root.findFileByRelativePath(relativePath.trimStart('/'))
            if (dir != null && dir.isValid && dir.isDirectory) {
                return dir
            }
        }
        
        return null
    }
}

/**
 * Extension function to capitalize first letter (Kotlin stdlib compatibility)
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/**
 * Extension function to decapitalize first letter (Kotlin stdlib compatibility)
 */
private fun String.decapitalize(): String {
    return this.replaceFirstChar { it.lowercase() }
}
