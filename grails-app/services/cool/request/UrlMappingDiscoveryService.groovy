package cool.request

import cool.request.model.EndpointMetadata
import cool.request.model.ParameterMetadata
import grails.web.mapping.UrlMappingsHolder
import grails.util.Holders

/**
 * Discovers URL mappings from Grails application
 */
class UrlMappingDiscoveryService {
    
    static transactional = false
    
    /**
     * Discover all URL mappings in the application
     */
    List<EndpointMetadata> discoverUrlMappings() {
        def endpoints = []
        
        try {
            // Get the URL mappings holder from Grails
            def mappingContext = getMappingContext()
            
            if (mappingContext) {
                // Iterate through URL mappings
                def mappings = extractMappings(mappingContext)
                
                mappings.each { mapping ->
                    try {
                        def endpoint = convertToEndpointMetadata(mapping)
                        if (endpoint) {
                            endpoints << endpoint
                        }
                    } catch (Exception e) {
                        log.warn "Error processing URL mapping", e
                    }
                }
            }
            
        } catch (Exception e) {
            log.error "Error discovering URL mappings", e
        }
        
        return endpoints
    }
    
    /**
     * Get the Grails mapping context
     */
    private def getMappingContext() {
        try {
            // Grails 2.5.3 way to access URL mappings
            def ctx = Holders.applicationContext
            if (ctx?.containsBean('urlMappings')) {
                return ctx.getBean('urlMappings')
            }
            
            // Alternative: try to access via GrailsApplication
            def grailsApp = Holders.grailsApplication
            if (grailsApp?.mainContext?.containsBean('urlMappings')) {
                return grailsApp.mainContext.getBean('urlMappings')
            }
            
        } catch (Exception e) {
            log.debug "Could not access URL mappings: ${e.message}"
        }
        
        return null
    }
    
    /**
     * Extract mappings from the mapping context
     */
    private List extractMappings(def mappingContext) {
        def mappings = []
        
        try {
            // Try to get the mappings array/list
            if (mappingContext.respondsTo('getUrlMappings')) {
                def urlMappings = mappingContext.getUrlMappings()
                if (urlMappings instanceof Collection) {
                    mappings.addAll(urlMappings)
                } else if (urlMappings.isArray()) {
                    mappings.addAll(urlMappings as List)
                }
            } else if (mappingContext instanceof Collection) {
                mappings.addAll(mappingContext)
            } else if (mappingContext.isArray()) {
                mappings.addAll(mappingContext as List)
            }
            
        } catch (Exception e) {
            log.debug "Error extracting mappings: ${e.message}"
        }
        
        return mappings
    }
    
    /**
     * Convert a Grails URL mapping to EndpointMetadata
     */
    private EndpointMetadata convertToEndpointMetadata(def mapping) {
        def endpoint = new EndpointMetadata()
        
        try {
            // Extract URL pattern
            endpoint.path = extractPath(mapping)
            
            // Extract HTTP method
            endpoint.httpMethod = extractHttpMethod(mapping)
            
            // Extract controller and action
            def controllerAction = extractControllerAndAction(mapping)
            endpoint.controller = controllerAction.controller
            endpoint.action = controllerAction.action
            
            // Extract parameters from path
            endpoint.parameters = extractPathParameters(endpoint.path)
            
            // Check if restful
            endpoint.isRestful = isRestfulMapping(endpoint)
            
            return endpoint
            
        } catch (Exception e) {
            log.warn "Error converting mapping to endpoint", e
            return null
        }
    }
    
    /**
     * Extract the URL path from a mapping
     */
    private String extractPath(def mapping) {
        try {
            // Try different property names based on Grails version
            if (mapping.respondsTo('getURL')) {
                return mapping.getURL()
            } else if (mapping.respondsTo('getUrl')) {
                return mapping.getUrl()
            } else if (mapping.respondsTo('getPath')) {
                return mapping.getPath()
            } else if (mapping.hasProperty('url')) {
                return mapping.url?.toString()
            } else if (mapping.hasProperty('path')) {
                return mapping.path?.toString()
            }
        } catch (Exception e) {
            log.debug "Error extracting path: ${e.message}"
        }
        
        return "/unknown"
    }
    
    /**
     * Extract HTTP method from a mapping
     */
    private String extractHttpMethod(def mapping) {
        try {
            if (mapping.respondsTo('getHttpMethod')) {
                return mapping.getHttpMethod()?.toUpperCase()
            } else if (mapping.hasProperty('httpMethod')) {
                return mapping.httpMethod?.toString()?.toUpperCase()
            } else if (mapping.respondsTo('getMethod')) {
                return mapping.getMethod()?.toUpperCase()
            } else if (mapping.hasProperty('method')) {
                return mapping.method?.toString()?.toUpperCase()
            }
        } catch (Exception e) {
            log.debug "Error extracting HTTP method: ${e.message}"
        }
        
        return null // No specific method means all methods allowed
    }
    
    /**
     * Extract controller and action from a mapping
     */
    private Map<String, String> extractControllerAndAction(def mapping) {
        def result = [controller: null, action: null]
        
        try {
            // Get defaults/params map
            def defaults = null
            if (mapping.respondsTo('getDefaults')) {
                defaults = mapping.getDefaults()
            } else if (mapping.hasProperty('defaults')) {
                defaults = mapping.defaults
            } else if (mapping.respondsTo('getParams')) {
                defaults = mapping.getParams()
            }
            
            if (defaults instanceof Map) {
                result.controller = defaults.controller?.toString()
                result.action = defaults.action?.toString()
            }
            
        } catch (Exception e) {
            log.debug "Error extracting controller/action: ${e.message}"
        }
        
        return result
    }
    
    /**
     * Extract path parameters from URL pattern
     */
    private List<ParameterMetadata> extractPathParameters(String path) {
        def parameters = []
        
        if (!path) return parameters
        
        // Match $param patterns like /users/$id
        def matcher = path =~ /\$([a-zA-Z_][a-zA-Z0-9_]*)/
        matcher.each { match ->
            def paramName = match[1]
            parameters << new ParameterMetadata(
                name: paramName,
                type: 'String',
                required: true,
                isPathParameter: true,
                isQueryParameter: false
            )
        }
        
        // Match * wildcards
        if (path.contains('*')) {
            parameters << new ParameterMetadata(
                name: 'wildcard',
                type: 'String',
                required: false,
                isPathParameter: true,
                isQueryParameter: false
            )
        }
        
        return parameters
    }
    
    /**
     * Check if a mapping appears to be RESTful
     */
    private boolean isRestfulMapping(EndpointMetadata endpoint) {
        if (!endpoint.controller || !endpoint.action) return false
        
        def restfulActions = ['index', 'show', 'create', 'save', 'edit', 'update', 'delete']
        return restfulActions.contains(endpoint.action.toLowerCase())
    }
}
