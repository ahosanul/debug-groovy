package cool.request

import cool.request.discovery.*
import cool.request.model.*
import grails.converters.JSON
import grails.util.Holders

/**
 * Main controller for Cool Request for Grails
 * Provides API inspection, testing, and debugging functionality
 */
class CoolRequestController {
    
    static allowedMethods = [
        saveRequest: 'POST',
        executeRequest: 'POST',
        executeAction: 'POST',
        executeJob: 'POST',
        refreshMetadata: 'POST',
        deleteHistory: 'DELETE'
    ]
    
    CoolRequestDiscoveryService coolRequestDiscoveryService
    RequestExecutionService requestExecutionService
    JobDiscoveryService jobDiscoveryService
    EnvironmentService environmentService
    HistoryService historyService
    ScriptExecutionService scriptExecutionService
    ExportService exportService
    
    def beforeInterceptor = [except: '(index|assets)', action: 'checkAccess']
    
    /**
     * Check if access is allowed (security check)
     */
    private boolean checkAccess() {
        // Check if plugin is enabled
        if (!environmentService.isEnabled()) {
            log.warn "Cool Request access denied - plugin is disabled"
            render status: 403, text: 'Cool Request is disabled for this environment'
            return false
        }
        
        // Check for production warning
        if (environmentService.isProduction()) {
            log.warn "Cool Request accessed in production environment"
        }
        
        return true
    }
    
    /**
     * Main UI page
     */
    def index() {
        log.debug "Rendering Cool Request UI"
    }
    
    /**
     * Get application metadata (controllers, endpoints, jobs)
     */
    def api() {
        def forceRefresh = params.boolean('refresh', false)
        def metadata = coolRequestDiscoveryService.getApplicationMetadata(forceRefresh)
        
        render contentType: 'application/json'
        render metadata as JSON
    }
    
    /**
     * Get controllers list
     */
    def controllers() {
        def controllers = coolRequestDiscoveryService.controllerDiscoveryService.discoverControllers()
        
        render contentType: 'application/json'
        render controllers as JSON
    }
    
    /**
     * Get specific controller details
     */
    def controller() {
        def name = params.name
        
        if (!name) {
            render status: 400, text: 'Controller name required'
            return
        }
        
        def controller = coolRequestDiscoveryService.controllerDiscoveryService.getControllerByName(name)
        
        if (!controller) {
            render status: 404, text: "Controller not found: ${name}"
            return
        }
        
        render contentType: 'application/json'
        render controller as JSON
    }
    
    /**
     * Get URL mappings/endpoints
     */
    def mappings() {
        def endpoints = coolRequestDiscoveryService.urlMappingDiscoveryService.discoverUrlMappings()
        
        render contentType: 'application/json'
        render endpoints as JSON
    }
    
    /**
     * Get jobs list
     */
    def jobs() {
        def jobs = jobDiscoveryService.discoverJobs()
        
        render contentType: 'application/json'
        render jobs as JSON
    }
    
    /**
     * Execute a job manually
     */
    def executeJob() {
        def name = params.name
        
        if (!name) {
            render status: 400, text: 'Job name required'
            return
        }
        
        try {
            def result = jobDiscoveryService.executeJob(name)
            
            render contentType: 'application/json'
            render result as JSON
            
        } catch (Exception e) {
            log.error "Error executing job: ${name}", e
            render status: 500, text: "Job execution failed: ${e.message}"
        }
    }
    
    /**
     * Execute HTTP request
     */
    def executeRequest() {
        try {
            def requestContext = parseRequestContext()
            
            // Execute pre-request script if provided
            if (requestContext.preRequestScript) {
                scriptExecutionService.executePreRequestScript(
                    requestContext.preRequestScript, 
                    requestContext
                )
            }
            
            // Execute the request
            long startTime = System.currentTimeMillis()
            def result = requestExecutionService.execute(requestContext)
            long endTime = System.currentTimeMillis()
            
            result.executionTimeMs = endTime - startTime
            
            // Execute post-request script if provided
            if (requestContext.postRequestScript) {
                scriptExecutionService.executePostRequestScript(
                    requestContext.postRequestScript,
                    requestContext,
                    result
                )
            }
            
            // Save to history
            historyService.saveToHistory(requestContext, result)
            
            render contentType: 'application/json'
            render result as JSON
            
        } catch (Exception e) {
            log.error "Error executing request", e
            render status: 500, text: "Request execution failed: ${e.message}"
        }
    }
    
    /**
     * Execute action directly (bypassing HTTP)
     */
    def executeAction() {
        try {
            def requestContext = parseRequestContext()
            
            // Execute pre-request script if provided
            if (requestContext.preRequestScript) {
                scriptExecutionService.executePreRequestScript(
                    requestContext.preRequestScript,
                    requestContext
                )
            }
            
            // Execute via direct invocation
            long startTime = System.currentTimeMillis()
            def result = requestExecutionService.executeDirect(requestContext)
            long endTime = System.currentTimeMillis()
            
            result.executionTimeMs = endTime - startTime
            
            // Save to history
            historyService.saveToHistory(requestContext, result)
            
            render contentType: 'application/json'
            render result as JSON
            
        } catch (Exception e) {
            log.error "Error executing action directly", e
            render status: 500, text: "Direct action execution failed: ${e.message}"
        }
    }
    
    /**
     * Parse request context from JSON body
     */
    private RequestContext parseRequestContext() {
        def json = request.JSON
        
        if (!json) {
            throw new IllegalArgumentException("Invalid request body")
        }
        
        def context = new RequestContext()
        context.method = json.method ?: 'GET'
        context.url = json.url
        context.path = json.path
        context.pathParams = json.pathParams ?: [:]
        context.queryParams = json.queryParams ?: [:]
        context.headers = json.headers ?: [:]
        context.cookies = json.cookies ?: [:]
        context.body = json.body
        context.contentType = json.contentType ?: 'application/json'
        context.environment = json.environment ?: environmentService.getCurrentEnvironment()
        context.useDirectInvocation = json.useDirectInvocation ?: false
        context.preRequestScript = json.preRequestScript
        context.postRequestScript = json.postRequestScript
        context.controllerName = json.controllerName
        context.actionName = json.actionName
        
        return context
    }
    
    /**
     * Refresh discovery metadata
     */
    def refreshMetadata() {
        coolRequestDiscoveryService.clearCache()
        def metadata = coolRequestDiscoveryService.getApplicationMetadata(true)
        
        render contentType: 'application/json'
        render metadata as JSON
    }
    
    /**
     * Get request history
     */
    def history() {
        def limit = params.int('limit', 50)
        def history = historyService.getHistory(limit)
        
        render contentType: 'application/json'
        render history as JSON
    }
    
    /**
     * Save request to history/favorites
     */
    def saveRequest() {
        try {
            def requestContext = parseRequestContext()
            def name = params.name
            def collection = params.collection
            
            historyService.saveRequest(requestContext, name, collection)
            
            render contentType: 'application/json'
            render [success: true, message: 'Request saved'] as JSON
            
        } catch (Exception e) {
            log.error "Error saving request", e
            render status: 500, text: "Failed to save request: ${e.message}"
        }
    }
    
    /**
     * Delete history entry
     */
    def deleteHistory() {
        def id = params.id
        
        if (!id) {
            render status: 400, text: 'History ID required'
            return
        }
        
        historyService.deleteHistory(id)
        
        render contentType: 'application/json'
        render [success: true, message: 'History entry deleted'] as JSON
    }
    
    /**
     * Generate OpenAPI specification
     */
    def openapi() {
        try {
            def format = params.format ?: 'json'
            def openApiSpec = exportService.generateOpenApiSpec()
            
            if (format == 'yaml') {
                render contentType: 'application/yaml'
                render exportService.convertToYaml(openApiSpec)
            } else {
                render contentType: 'application/json'
                render openApiSpec as JSON
            }
            
        } catch (Exception e) {
            log.error "Error generating OpenAPI spec", e
            render status: 500, text: "OpenAPI generation failed: ${e.message}"
        }
    }
    
    /**
     * Generate cURL command for a request
     */
    def curl() {
        try {
            def requestContext = parseRequestContext()
            def curlCommand = exportService.generateCurl(requestContext)
            
            render contentType: 'text/plain'
            render curlCommand
            
        } catch (Exception e) {
            log.error "Error generating cURL", e
            render status: 500, text: "cURL generation failed: ${e.message}"
        }
    }
}
