package cool.request

import cool.request.model.*
import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.core.io.Resource

/**
 * Discovers Grails controllers and their actions
 */
class ControllerDiscoveryService {
    
    static transactional = false
    
    GrailsApplication grailsApplication
    ActionMetadataService actionMetadataService
    ParameterDiscoveryService parameterDiscoveryService
    
    /**
     * Discover all controllers in the application
     */
    List<ControllerMetadata> discoverControllers() {
        def controllers = []
        
        try {
            // Get all controller classes from GrailsApplication
            def controllerClasses = grailsApplication?.allClasses?.findAll { clazz ->
                isControllerClass(clazz)
            }
            
            controllerClasses?.each { clazz ->
                try {
                    def metadata = extractControllerMetadata(clazz)
                    if (metadata) {
                        controllers << metadata
                    }
                } catch (Exception e) {
                    log.warn "Error processing controller: ${clazz?.name}", e
                }
            }
            
            // Sort by name
            controllers.sort { it.name }
            
        } catch (Exception e) {
            log.error "Error discovering controllers", e
        }
        
        return controllers
    }
    
    /**
     * Check if a class is a Grails controller
     */
    private boolean isControllerClass(Class clazz) {
        if (!clazz) return false
        
        // Check for GrailsController annotation or naming convention
        def className = clazz.simpleName
        
        // Must end with "Controller"
        if (!className.endsWith('Controller')) {
            return false
        }
        
        // Exclude abstract classes
        if (java.lang.reflect.Modifier.isAbstract(clazz.modifiers)) {
            return false
        }
        
        // Check for common Grails controller traits
        // In Grails 2.5.3, controllers typically have 'allowedMethods' or respond to web requests
        return true
    }
    
    /**
     * Extract metadata from a controller class
     */
    private ControllerMetadata extractControllerMetadata(Class clazz) {
        def metadata = new ControllerMetadata(
            clazz.simpleName.replaceAll('Controller$', ''),
            clazz.name
        )
        metadata.packageName = clazz.package?.name
        
        // Extract actions from public methods
        def methods = clazz.declaredMethods.findAll { method ->
            isActionMethod(method)
        }
        
        methods.each { method ->
            try {
                def actionMetadata = actionMetadataService.extractActionMetadata(method)
                
                // Discover parameters
                def params = parameterDiscoveryService.discoverParameters(method)
                actionMetadata.parameters.addAll(params)
                
                metadata.actions << actionMetadata
            } catch (Exception e) {
                log.warn "Error processing action: ${method.name}", e
            }
        }
        
        // Sort actions by name
        metadata.actions.sort { it.name }
        
        return metadata
    }
    
    /**
     * Check if a method is a controller action
     */
    private boolean isActionMethod(java.lang.reflect.Method method) {
        // Must be public
        if (!java.lang.reflect.Modifier.isPublic(method.modifiers)) {
            return false
        }
        
        // Exclude special methods
        def methodName = method.name
        if (methodName == '<init>' || methodName == '<clinit>') {
            return false
        }
        
        // Exclude methods that start with underscore (internal)
        if (methodName.startsWith('_')) {
            return false
        }
        
        // Exclude inherited Object methods
        if (['toString', 'equals', 'hashCode', 'getClass', 'notify', 'notifyAll', 'wait', 'clone', 'finalize'].contains(methodName)) {
            return false
        }
        
        // Exclude Grails internal methods
        if (['setServletContext', 'getServletContext', 'setGrailsApplication', 'getGrailsApplication'].contains(methodName)) {
            return false
        }
        
        return true
    }
    
    /**
     * Get a specific controller by name
     */
    ControllerMetadata getControllerByName(String name) {
        def controllers = discoverControllers()
        return controllers.find { it.name.equalsIgnoreCase(name) }
    }
}
