/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

/**
 * Utility class for resolving Grails artifacts (controllers, services, domains, taglibs).
 * 
 * Uses IntelliJ's PSI infrastructure to statically analyze Grails projects and identify:
 * - Controller classes and their actions
 * - Service classes
 * - Domain classes
 * - Taglib classes
 * 
 * This information is used to populate the artifact tree in the Grails Debug tool window.
 */
class GrailsArtifactResolver {
    
    companion object {
        // Grails artefact type markers
        private val CONTROLLER_SUFFIXES = listOf("Controller")
        private val SERVICE_SUFFIXES = listOf("Service")
        private val TAGLIB_SUFFIXES = listOf("TagLib")
        
        // Common Grails directory patterns
        private val GRAILS_APP_DIRS = listOf(
            "grails-app/controllers",
            "grails-app/services",
            "grails-app/domain",
            "grails-app/taglib",
            "src/main/groovy"
        )
    }
    
    /**
     * Find all Grails controllers in the project
     */
    fun findControllers(project: Project): List<GrailsArtifact> {
        return findArtefactsByType(project, ArtefactType.CONTROLLER)
    }
    
    /**
     * Find all Grails services in the project
     */
    fun findServices(project: Project): List<GrailsArtifact> {
        return findArtefactsByType(project, ArtefactType.SERVICE)
    }
    
    /**
     * Find all Grails domain classes in the project
     */
    fun findDomainClasses(project: Project): List<GrailsArtifact> {
        return findArtefactsByType(project, ArtefactType.DOMAIN)
    }
    
    /**
     * Find all Grails taglibs in the project
     */
    fun findTaglibs(project: Project): List<GrailsArtifact> {
        return findArtefactsByType(project, ArtefactType.TAGLIB)
    }
    
    /**
     * Find Grails artefacts by type
     */
    private fun findArtefactsByType(project: Project, type: ArtefactType): List<GrailsArtifact> {
        val artefacts = mutableListOf<GrailsArtifact>()
        val psiManager = PsiManager.getInstance(project)
        
        // Search through content roots
        val contentRoots = com.intellij.openapi.roots.ProjectRootManager.getInstance(project).contentRoots
        
        for (root in contentRoots) {
            val typeDir = root.findFileByRelativePath(type.directoryPath)
            if (typeDir != null && typeDir.isDirectory) {
                collectArtefactsFromDirectory(psiManager, typeDir, type, artefacts)
            }
        }
        
        return artefacts
    }
    
    /**
     * Recursively collect artefacts from a directory
     */
    private fun collectArtefactsFromDirectory(
        psiManager: PsiManager,
        directory: VirtualFile,
        type: ArtefactType,
        artefacts: MutableList<GrailsArtifact>
    ) {
        directory.children.forEach { file ->
            if (file.isDirectory) {
                collectArtefactsFromDirectory(psiManager, file, type, artefacts)
            } else if (isArtefactFile(file, type)) {
                val psiFile = psiManager.findFile(file)
                if (psiFile != null) {
                    artefacts.add(GrailsArtifact(
                        name = file.nameWithoutExtension,
                        type = type,
                        virtualFile = file,
                        psiElement = psiFile
                    ))
                }
            }
        }
    }
    
    /**
     * Check if a file matches the artefact type
     */
    private fun isArtefactFile(file: VirtualFile, type: ArtefactType): Boolean {
        if (file.extension != "groovy" && file.extension != "java") {
            return false
        }
        
        val name = file.nameWithoutExtension
        
        return when (type) {
            ArtefactType.CONTROLLER -> CONTROLLER_SUFFIXES.any { name.endsWith(it) }
            ArtefactType.SERVICE -> SERVICE_SUFFIXES.any { name.endsWith(it) }
            ArtefactType.TAGLIB -> TAGLIB_SUFFIXES.any { name.endsWith(it) }
            ArtefactType.DOMAIN -> true // All .groovy files in domain directory are domains
        }
    }
    
    /**
     * Get actions (methods) from a controller
     */
    fun getControllerActions(controllerFile: VirtualFile): List<String> {
        val actions = mutableListOf<String>()
        
        // Parse the controller file to extract public methods
        // This would need Groovy PSI parsing
        val content = try {
            String(controllerFile.contentsToByteArray())
        } catch (e: Exception) {
            return emptyList()
        }
        
        // Simple regex-based extraction (placeholder for proper PSI analysis)
        val methodPattern = Regex("""\b(def|public|protected)\s+\w+\s+(\w+)\s*\(""")
        val matches = methodPattern.findAll(content)
        
        matches.forEach { match ->
            val methodName = match.groupValues[2]
            // Filter out common non-action methods
            if (!isNonActionMethod(methodName)) {
                actions.add(methodName)
            }
        }
        
        return actions
    }
    
    private fun isNonActionMethod(methodName: String): Boolean {
        return methodName in listOf(
            "index", "show", "create", "edit", "update", "delete",
            "equals", "hashCode", "toString", "getClass"
        ) || methodName.startsWith("_")
    }
    
    /**
     * Find a specific artefact by name and type
     */
    fun findArtefactByName(project: Project, name: String, type: ArtefactType): GrailsArtifact? {
        return when (type) {
            ArtefactType.CONTROLLER -> findControllers(project).find { it.name == name }
            ArtefactType.SERVICE -> findServices(project).find { it.name == name }
            ArtefactType.DOMAIN -> findDomainClasses(project).find { it.name == name }
            ArtefactType.TAGLIB -> findTaglibs(project).find { it.name == name }
        }
    }
    
    /**
     * Data class representing a Grails artefact
     */
    data class GrailsArtifact(
        val name: String,
        val type: ArtefactType,
        val virtualFile: VirtualFile,
        val psiElement: com.intellij.psi.PsiElement
    )
    
    /**
     * Enum representing Grails artefact types
     */
    enum class ArtefactType(val directoryPath: String) {
        CONTROLLER("grails-app/controllers"),
        SERVICE("grails-app/services"),
        DOMAIN("grails-app/domain"),
        TAGLIB("grails-app/taglib")
    }
}
