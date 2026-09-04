package cool.request

import cool.request.model.*
import grails.util.Holders
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.net.URLEncoder

/**
 * Executes HTTP requests against the application
 */
class RequestExecutionService {
    
    static transactional = false
    
    private static final RestTemplate restTemplate = new RestTemplate()
    
    /**
     * Execute an HTTP request
     */
    RequestResult execute(RequestContext context) {
        def result = new RequestResult()
        
        try {
            // Build the full URL
            def url = buildUrl(context)
            
            // Create HTTP headers
            def headers = createHttpHeaders(context)
            
            // Create request body if applicable
            def requestBody = createRequestBody(context)
            
            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers)
            
            // Execute request
            long startTime = System.currentTimeMillis()
            
            ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.valueOf(context.method?.toUpperCase() ?: 'GET'),
                entity,
                String.class
            )
            
            long endTime = System.currentTimeMillis()
            
            // Populate result
            result.status = response.statusCode.value()
            result.statusText = response.statusCode.reasonPhrase
            result.executionTimeMs = endTime - startTime
            result.headers = convertHeaders(response.headers)
            result.body = response.body
            result.contentType = response.headers.getContentType()?.toString()
            result.contentLength = response.body?.length() ?: 0
            result.cookies = response.headers.get('Set-Cookie') ?: []
            result.success = response.statusCode.is2xxSuccessful()
            
        } catch (Exception e) {
            log.error "Error executing HTTP request", e
            result.success = false
            result.errorMessage = e.message
            result.errorType = e.class.simpleName
            
            // Handle specific HTTP client errors
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                result.status = e.rawStatusCode
                result.body = e.responseBodyAsString
            } else if (e instanceof org.springframework.web.client.HttpServerErrorException) {
                result.status = e.rawStatusCode
                result.body = e.responseBodyAsString
            }
        }
        
        return result
    }
    
    /**
     * Execute action directly via Grails controller invocation
     */
    RequestResult executeDirect(RequestContext context) {
        def result = new RequestResult()
        
        try {
            if (!context.controllerName || !context.actionName) {
                throw new IllegalArgumentException("Controller name and action name required for direct invocation")
            }
            
            // Get controller from Spring context
            def controllerName = context.controllerName.endsWith('Controller') 
                ? context.controllerName 
                : "${context.controllerName}Controller"
                
            def beanName = controllerName[0].toLowerCase() + controllerName[1..-1]
            
            def ctx = Holders.applicationContext
            
            if (!ctx.containsBean(beanName)) {
                throw new IllegalArgumentException("Controller bean not found: ${beanName}")
            }
            
            def controller = ctx.getBean(beanName)
            
            // Prepare parameters
            def params = [:]
            params.putAll(context.queryParams)
            params.putAll(context.pathParams)
            
            // Set up mock web request context if needed
            setupWebRequestContext(params, context)
            
            // Find and invoke action method
            def actionMethod = findActionMethod(controller, context.actionName)
            
            if (!actionMethod) {
                throw new IllegalArgumentException("Action method not found: ${context.actionName}")
            }
            
            long startTime = System.currentTimeMillis()
            
            // Invoke the action
            def actionResult = invokeAction(actionMethod, controller, params, context)
            
            long endTime = System.currentTimeMillis()
            
            // Populate result
            result.status = 200
            result.statusText = 'OK'
            result.executionTimeMs = endTime - startTime
            result.body = formatActionResult(actionResult)
            result.contentType = 'application/json'
            result.contentLength = result.body?.length() ?: 0
            result.success = true
            
        } catch (Exception e) {
            log.error "Error executing direct action", e
            result.success = false
            result.errorMessage = e.message
            result.errorType = e.class.simpleName
        }
        
        return result
    }
    
    /**
     * Build the full URL from context
     */
    private String buildUrl(RequestContext context) {
        def baseUrl = getBaseUrl(context.environment)
        def path = applyPathParams(context.path, context.pathParams)
        
        StringBuilder url = new StringBuilder(baseUrl)
        url.append(path)
        
        // Add query parameters
        if (context.queryParams) {
            def queryString = context.queryParams.collect { key, value ->
                "${URLEncoder.encode(key, 'UTF-8')}=${URLEncoder.encode(value ?: '', 'UTF-8')}"
            }.join('&')
            
            if (queryString) {
                url.append('?').append(queryString)
            }
        }
        
        return url.toString()
    }
    
    /**
     * Get base URL for environment
     */
    private String getBaseUrl(String environment) {
        // Default to localhost for development
        return "http://localhost:${System.getProperty('server.port', '8080')}${Holders.grailsApplication?.metadata?.getApplicationContextPath() ?: ''}"
    }
    
    /**
     * Apply path parameters to URL template
     */
    private String applyPathParams(String path, Map<String, String> params) {
        if (!path || !params) return path
        
        def result = path
        params.each { key, value ->
            result = result.replace("\${key}", value)
            result = result.replace("{${key}}", value)
            result = result.replace(":${key}", value)
        }
        return result
    }
    
    /**
     * Create HTTP headers from context
     */
    private HttpHeaders createHttpHeaders(RequestContext context) {
        def headers = new HttpHeaders()
        
        // Add Content-Type
        if (context.contentType) {
            headers.setContentType(MediaType.parseMediaType(context.contentType))
        }
        
        // Add custom headers
        context.headers?.each { key, value ->
            headers.add(key, value)
        }
        
        // Add cookies as header
        if (context.cookies) {
            def cookieHeader = context.cookies.collect { key, value ->
                "${key}=${value}"
            }.join('; ')
            
            if (cookieHeader) {
                headers.add('Cookie', cookieHeader)
            }
        }
        
        return headers
    }
    
    /**
     * Create request body
     */
    private String createRequestBody(RequestContext context) {
        if (!context.body) return null
        
        // For form data, convert to appropriate format
        if (context.contentType?.contains('application/x-www-form-urlencoded')) {
            return context.body
        }
        
        return context.body
    }
    
    /**
     * Convert Spring headers to map
     */
    private Map<String, List<String>> convertHeaders(HttpHeaders headers) {
        def result = [:]
        headers?.each { key, values ->
            result[key] = values
        }
        return result
    }
    
    /**
     * Setup web request context for direct invocation
     */
    private void setupWebRequestContext(Map params, RequestContext context) {
        // In Grails 2.5.3, we may need to set up a mock request
        // This is a simplified version - full implementation would use MockHttpServletRequest
    }
    
    /**
     * Find action method on controller
     */
    private java.lang.reflect.Method findActionMethod(def controller, String actionName) {
        def clazz = controller.class
        
        // Try to find exact match first
        def method = clazz.methods.find { m -> 
            m.name == actionName && m.parameterTypes.length == 0 
        }
        
        if (method) return method
        
        // Try with params parameter
        method = clazz.methods.find { m -> 
            m.name == actionName && m.parameterTypes.length == 1 &&
            m.parameterTypes[0].simpleName in ['Map', 'HttpServletRequest']
        }
        
        return method
    }
    
    /**
     * Invoke action method
     */
    private def invokeAction(java.lang.reflect.Method method, def controller, Map params, RequestContext context) {
        if (method.parameterTypes.length == 0) {
            return method.invoke(controller)
        }
        
        if (method.parameterTypes.length == 1) {
            if (method.parameterTypes[0] == Map) {
                return method.invoke(controller, params)
            }
        }
        
        return method.invoke(controller)
    }
    
    /**
     * Format action result for response
     */
    private String formatActionResult(def result) {
        if (result == null) {
            return '{}'
        }
        
        if (result instanceof String) {
            return result
        }
        
        // Try to convert to JSON
        try {
            return grails.converters.JSON.render(result).toString()
        } catch (Exception e) {
            return result.toString()
        }
    }
}
