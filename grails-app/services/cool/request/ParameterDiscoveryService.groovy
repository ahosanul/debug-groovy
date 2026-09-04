package cool.request

import cool.request.model.ParameterMetadata

/**
 * Discovers parameters from action methods
 */
class ParameterDiscoveryService {
    
    static transactional = false
    
    // Common Grails/web parameter names that are implicit
    private static final IMPLICIT_PARAMS = ['params', 'request', 'response', 'session', 'servletContext']
    
    /**
     * Discover parameters from a method signature
     */
    List<ParameterMetadata> discoverParameters(java.lang.reflect.Method method) {
        def parameters = []
        
        try {
            def methodParams = method.parameterTypes
            def paramNames = getParameterNames(method)
            
            methodParams.eachWithIndex { paramType, index ->
                def paramName = paramNames[index] ?: "param${index}"
                
                // Skip implicit Grails parameters
                if (IMPLICIT_PARAMS.contains(paramName.toLowerCase())) {
                    return
                }
                
                def metadata = new ParameterMetadata(
                    paramName,
                    paramType.simpleName
                )
                
                // Determine if required
                metadata.required = isRequired(paramType)
                
                // Set example value
                metadata.example = getExampleValue(paramType)
                
                // Check if it's a command object
                if (isCommandObject(paramType)) {
                    // Command objects will be handled separately
                    metadata.type = "${paramType.simpleName} (Command Object)"
                }
                
                parameters << metadata
            }
            
        } catch (Exception e) {
            log.warn "Error discovering parameters for ${method.name}", e
        }
        
        return parameters
    }
    
    /**
     * Try to get parameter names using reflection
     * Note: This requires debug symbols or Groovy metadata
     */
    private List<String> getParameterNames(java.lang.reflect.Method method) {
        def paramNames = []
        
        try {
            // In Groovy, we can sometimes get parameter names via MetaClass
            def metaMethod = method.declaringClass.metaClass.getMetaMethod(method.name, method.parameterTypes)
            if (metaMethod && metaMethod.respondsTo('getParameters')) {
                def groovyParams = metaMethod.parameters
                paramNames = groovyParams.collect { it?.name }
            }
        } catch (Exception e) {
            log.debug "Could not get parameter names via MetaClass"
        }
        
        // Fallback to generic names
        if (!paramNames || paramNames.any { !it }) {
            paramNames = (0..<method.parameterTypes.length).collect { "arg${it}" }
        }
        
        return paramNames
    }
    
    /**
     * Determine if a parameter type is required
     */
    private boolean isRequired(Class type) {
        // Primitive types are required
        if (type.isPrimitive()) {
            return true
        }
        
        // Optional types
        if (['Optional'].contains(type.simpleName)) {
            return false
        }
        
        // Default to optional for reference types
        return false
    }
    
    /**
     * Get an example value for a parameter type
     */
    private Object getExampleValue(Class type) {
        switch (type.simpleName) {
            case 'String':
                return "example"
            case 'long':
            case 'Long':
                return 1L
            case 'int':
            case 'Integer':
                return 1
            case 'boolean':
            case 'Boolean':
                return true
            case 'double':
            case 'Double':
                return 1.0
            case 'float':
            case 'Float':
                return 1.0f
            case 'short':
            case 'Short':
                return (short)1
            case 'byte':
            case 'Byte':
                return (byte)1
            case 'BigDecimal':
                return new BigDecimal('1.00')
            case 'Date':
                return new Date()
            case 'Calendar':
                return Calendar.instance
            default:
                return null
        }
    }
    
    /**
     * Check if a type is likely a Grails command object
     */
    private boolean isCommandObject(Class type) {
        // Simple heuristic: not a primitive, not a common type, ends with "Command"
        if (type.isPrimitive()) {
            return false
        }
        
        def commonTypes = [
            String, Integer, Long, Boolean, Double, Float, Short, Byte,
            BigDecimal, Date, Calendar, List, Map, Collection
        ]
        
        if (commonTypes.contains(type)) {
            return false
        }
        
        // Check naming convention
        return type.simpleName.endsWith('Command') || type.simpleName.endsWith('Form')
    }
}
