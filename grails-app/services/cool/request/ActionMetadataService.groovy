package cool.request

import cool.request.model.ActionMetadata

/**
 * Extracts metadata from controller action methods
 */
class ActionMetadataService {
    
    static transactional = false
    
    /**
     * Extract metadata from a method
     */
    ActionMetadata extractActionMetadata(java.lang.reflect.Method method) {
        def metadata = new ActionMetadata(
            method.name,
            method.name
        )
        
        // Determine return type
        metadata.returnType = method.returnType?.name
        
        // Check for RESTful annotations or patterns (Grails 2.5.3 specific)
        metadata.isRestful = isRestfulAction(method.name)
        
        // Infer HTTP methods from action name
        inferHttpMethods(method, metadata)
        
        return metadata
    }
    
    /**
     * Check if an action is typically RESTful
     */
    private boolean isRestfulAction(String actionName) {
        def restfulActions = ['index', 'show', 'create', 'save', 'edit', 'update', 'delete']
        return restfulActions.contains(actionName.toLowerCase())
    }
    
    /**
     * Infer HTTP methods from action name based on Grails conventions
     */
    private void inferHttpMethods(java.lang.reflect.Method method, ActionMetadata metadata) {
        def actionName = method.name.toLowerCase()
        
        // Grails convention-based HTTP method inference
        switch (actionName) {
            case 'index':
            case 'list':
            case 'show':
                metadata.addHttpMethod('GET')
                break
            case 'create':
                metadata.addHttpMethod('GET')
                break
            case 'save':
                metadata.addHttpMethod('POST')
                break
            case 'edit':
                metadata.addHttpMethod('GET')
                break
            case 'update':
                metadata.addHttpMethod('PUT')
                break
            case 'delete':
                metadata.addHttpMethod('DELETE')
                break
            default:
                // Default to GET for unknown actions
                metadata.addHttpMethod('GET')
        }
        
        // Check for allowedMethods in the controller (if accessible)
        checkAllowedMethods(method, metadata)
    }
    
    /**
     * Check for allowedMethods property in the controller
     */
    private void checkAllowedMethods(java.lang.reflect.Method method, ActionMetadata metadata) {
        try {
            def declaringClass = method.declaringClass
            
            // Try to find allowedMethods static property
            def allowedMethodsField = declaringClass?.getDeclaredField('allowedMethods')
            if (allowedMethodsField) {
                allowedMethodsField.setAccessible(true)
                def allowedMethods = allowedMethodsField.get(null)
                
                if (allowedMethods instanceof Map) {
                    def methodsForAction = allowedMethods[method.name]
                    if (methodsForAction) {
                        // Clear inferred methods and use declared ones
                        metadata.httpMethods.clear()
                        
                        if (methodsForAction instanceof String) {
                            metadata.addHttpMethod(methodsForAction)
                        } else if (methodsForAction instanceof List) {
                            methodsForAction.each { m ->
                                metadata.addHttpMethod(m.toString())
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore - allowedMethods not available
            log.debug "Could not read allowedMethods: ${e.message}"
        }
    }
}
