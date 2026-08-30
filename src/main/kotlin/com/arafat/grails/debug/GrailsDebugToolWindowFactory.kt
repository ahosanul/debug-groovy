/*
 * Copyright 2024 Arafat Hossain. All rights reserved.
 */
package com.arafat.grails.debug

import com.arafat.grails.debug.ui.GrailsDebugPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Tool Window Factory for the Grails Debug Assistant.
 * 
 * Creates and initializes the main tool window panel with:
 * - Left panel: Grails artifacts tree (30%)
 * - Right panel: Tabs for Evaluate and Request Builder (70%)
 * 
 * This factory is registered in plugin.xml and is called when the tool window is first opened.
 */
class GrailsDebugToolWindowFactory : ToolWindowFactory, DumbAware {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Create the main panel with split layout
        val grailsDebugPanel = GrailsDebugPanel(project)
        
        // Create content using the modern ContentFactory API
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(grailsDebugPanel, "", false)
        
        // Add content to the tool window
        toolWindow.contentManager.addContent(content)
        
        // Set a preferred size for the tool window
        toolWindow.setTitleActions(listOf())
    }
    
    override fun shouldBeAvailable(project: Project): Boolean {
        // Always available, but content will be enabled only during debug sessions
        return true
    }
}
