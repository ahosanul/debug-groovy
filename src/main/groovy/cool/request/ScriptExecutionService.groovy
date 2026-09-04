package cool.request

import cool.request.model.*
import groovy.lang.Binding
import groovy.lang.GroovyShell

/**
 * Executes Groovy scripts for pre/post request processing
 */
class ScriptExecutionService {
    
    static transactional = false
    
    /**
     * Execute a pre-request script
     */
    void executePreRequestScript(String script, RequestContext context) {
        try {
            def binding = createBinding(context, null)
            def shell = new GroovyShell(binding)
            
            // Execute the script
            shell.evaluate(script)
            
            log.debug "Pre-request script executed successfully"
            
        } catch (Exception e) {
            log.error "Pre-request script execution failed", e
            throw new ScriptExecutionException("Pre-request script failed: ${e.message}", e)
        }
    }
    
    /**
     * Execute a post-request script
     */
    void executePostRequestScript(String script, RequestContext context, RequestResult result) {
        try {
            def binding = createBinding(context, result)
            def shell = new GroovyShell(binding)
            
            // Execute the script
            shell.evaluate(script)
            
            log.debug "Post-request script executed successfully"
            
        } catch (Exception e) {
            log.error "Post-request script execution failed", e
            throw new ScriptExecutionException("Post-request script failed: ${e.message}", e)
        }
    }
    
    /**
     * Create binding with available variables
     */
    private Binding createBinding(RequestContext context, RequestResult result) {
        def binding = new Binding()
        
        // Expose request context
        binding.setVariable('request', context)
        binding.setVariable('method', context.method)
        binding.setVariable('url', context.url)
        binding.setVariable('path', context.path)
        binding.setVariable('params', context.queryParams)
        binding.setVariable('pathParams', context.pathParams)
        binding.setVariable('headers', context.headers)
        binding.setVariable('body', context.body)
        
        // Expose environment
        binding.setVariable('environment', context.environment)
        
        // Expose variables map for template substitution
        binding.setVariable('variables', context.queryParams ?: [:])
        
        // Expose response if available
        if (result) {
            binding.setVariable('response', result)
            binding.setVariable('status', result.status)
            binding.setVariable('responseBody', result.body)
            binding.setVariable('responseHeaders', result.headers)
        }
        
        // Expose utility functions
        binding.setVariable('log', org.slf4j.LoggerFactory.getLogger('cool.request.script'))
        
        return binding
    }
    
    /**
     * Validate a script without executing it
     */
    boolean validateScript(String script) {
        try {
            def shell = new GroovyShell()
            shell.parse(script)
            return true
        } catch (Exception e) {
            log.warn "Script validation failed: ${e.message}"
            return false
        }
    }
    
    /**
     * Execute arbitrary script in safe context
     */
    Object executeScript(String script, Map<String, Object> variables = [:]) {
        try {
            def binding = new Binding(variables)
            def shell = new GroovyShell(binding)
            return shell.evaluate(script)
        } catch (Exception e) {
            log.error "Script execution failed", e
            throw new ScriptExecutionException("Script failed: ${e.message}", e)
        }
    }
}

/**
 * Exception for script execution errors
 */
class ScriptExecutionException extends RuntimeException {
    ScriptExecutionException(String message, Throwable cause) {
        super(message, cause)
    }
}
