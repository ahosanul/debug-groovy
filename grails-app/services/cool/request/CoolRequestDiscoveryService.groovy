package cool.request

import cool.request.model.*
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.commons.*

/**
 * Main discovery service that coordinates all discovery operations
 */
class CoolRequestDiscoveryService {
    
    static transactional = false
    
    ControllerDiscoveryService controllerDiscoveryService
    UrlMappingDiscoveryService urlMappingDiscoveryService
    JobDiscoveryService jobDiscoveryService
    EnvironmentService environmentService
    
    private Map<String, Object> cache = [:]
    private long lastDiscoveryTime = 0
    private boolean isScanning = false
    
    /**
     * Get complete application metadata
     */
    Map<String, Object> getApplicationMetadata(boolean forceRefresh = false) {
        if (!forceRefresh && !isCacheExpired()) {
            return cache
        }
        
        synchronized(this) {
            if (isScanning) {
                // Return cached data while scanning
                return cache ?: buildEmptyMetadata()
            }
            
            isScanning = true
            try {
                cache = performDiscovery()
                lastDiscoveryTime = System.currentTimeMillis()
                return cache
            } finally {
                isScanning = false
            }
        }
    }
    
    /**
     * Perform full discovery scan
     */
    private Map<String, Object> performDiscovery() {
        log.info "Starting Cool Request discovery scan..."
        
        def metadata = buildEmptyMetadata()
        
        try {
            // Discover controllers
            def controllers = controllerDiscoveryService?.discoverControllers() ?: []
            metadata.controllers = controllers
            metadata.controllerCount = controllers.size()
            
            // Discover URL mappings
            def endpoints = urlMappingDiscoveryService?.discoverUrlMappings() ?: []
            metadata.endpoints = endpoints
            metadata.endpointCount = endpoints.size()
            
            // Associate endpoints with controllers/actions
            associateEndpointsWithControllers(controllers, endpoints)
            
            // Discover jobs
            def jobs = jobDiscoveryService?.discoverJobs() ?: []
            metadata.jobs = jobs
            metadata.jobCount = jobs.size()
            
            // Environment info
            metadata.environment = environmentService?.getCurrentEnvironment() ?: GrailsUtil.environment
            metadata.applicationName = getApplicationName()
            metadata.grailsVersion = getGrailsVersion()
            
            log.info "Discovery completed: ${metadata.controllerCount} controllers, ${metadata.endpointCount} endpoints, ${metadata.jobCount} jobs"
            
        } catch (Exception e) {
            log.error "Error during discovery", e
            metadata.errorMessage = "Discovery error: ${e.message}"
        }
        
        metadata.lastScanTime = new Date()
        return metadata
    }
    
    /**
     * Associate URL mapping endpoints with controller actions
     */
    private void associateEndpointsWithControllers(List<ControllerMetadata> controllers, List<EndpointMetadata> endpoints) {
        controllers.each { controller ->
            controller.actions.each { action ->
                def matchingEndpoints = endpoints.findAll { ep ->
                    ep.controller == controller.name.toLowerCase() && 
                    ep.action == action.name
                }
                action.endpoints.addAll(matchingEndpoints)
                
                // Add HTTP methods from endpoints
                matchingEndpoints.each { ep ->
                    if (ep.httpMethod) {
                        action.addHttpMethod(ep.httpMethod)
                    }
                }
            }
        }
    }
    
    /**
     * Check if cache should be refreshed (5 minute expiry)
     */
    private boolean isCacheExpired() {
        if (!cache) return true
        
        def fiveMinutes = 5 * 60 * 1000
        return (System.currentTimeMillis() - lastDiscoveryTime) > fiveMinutes
    }
    
    /**
     * Build empty metadata structure
     */
    private Map<String, Object> buildEmptyMetadata() {
        [
            controllers: [],
            controllerCount: 0,
            endpoints: [],
            endpointCount: 0,
            jobs: [],
            jobCount: 0,
            lastScanTime: null,
            errorMessage: null
        ]
    }
    
    /**
     * Get application name from Grails configuration
     */
    private String getApplicationName() {
        try {
            def config = Holders.grailsAttributes?.applicationInstance?.metadata
            return config?.getApplicationName() ?: "Unknown"
        } catch (Exception e) {
            return "Unknown"
        }
    }
    
    /**
     * Get Grails version
     */
    private String getGrailsVersion() {
        try {
            return GrailsUtil.version ?: "2.5.3"
        } catch (Exception e) {
            return "2.5.3"
        }
    }
    
    /**
     * Clear discovery cache
     */
    void clearCache() {
        cache = [:]
        lastDiscoveryTime = 0
        log.debug "Cool Request discovery cache cleared"
    }
}
