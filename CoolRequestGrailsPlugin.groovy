// Grails 2.5.3 Plugin Descriptor
class CoolRequestGrailsPlugin {
    // Plugin metadata
    def version = "0.1.0"
    def grailsVersion = "2.5.3 > *"
    def title = "Cool Request for Grails"
    def description = '''A developer-focused API inspection, testing, and debugging tool for Grails 2.5.3 applications.
    
Inspired by the Cool Request IntelliJ plugin, this tool provides a browser-based interface to:
- Discover controllers, actions, and URL mappings automatically
- Test HTTP endpoints with a modern request/response UI
- Execute Grails jobs manually
- Support pre/post-request Groovy scripts
- Export cURL commands and OpenAPI specifications
'''
    def documentation = "https://github.com/arafat/cool-request-grails"
    def license = "APACHE"
    def organization = [name: "Cool Request for Grails", url: "https://github.com/arafat/cool-request-grails"]
    def developers = ["arafat"]
    def issueManagement = [system: "GITHUB", url: "https://github.com/arafat/cool-request-grails/issues"]
    def scm = [url: "https://github.com/arafat/cool-request-grails"]

    // Plugin dependencies
    def dependsOn = [:]

    // Plugin configuration
    def config = {
        coolRequest {
            enabled = true
            path = "/cool-request"
            allowProduction = false
            enableJobExecution = true
            enableScripts = true
            enableDirectInvocation = true
            enableBeanInspection = false
            maxHistory = 100
            responseMaxSize = 10 * 1024 * 1024 // 10MB
        }
    }

    // Callback when plugin is loaded
    def doWithSpring = {
        // Register services
        coolRequestDiscoveryService(cool.request.CoolRequestDiscoveryService)
        controllerDiscoveryService(cool.request.ControllerDiscoveryService)
        urlMappingDiscoveryService(cool.request.UrlMappingDiscoveryService)
        actionMetadataService(cool.request.ActionMetadataService)
        parameterDiscoveryService(cool.request.ParameterDiscoveryService)
        requestExecutionService(cool.request.RequestExecutionService)
        jobDiscoveryService(cool.request.JobDiscoveryService)
        environmentService(cool.request.EnvironmentService)
        historyService(cool.request.HistoryService)
        scriptExecutionService(cool.request.ScriptExecutionService)
        exportService(cool.request.ExportService)
    }

    // Called after Spring context is initialized
    def onApplicationEvent = { evt ->
        if (evt.source instanceof org.springframework.context.event.ContextRefreshedEvent) {
            log.info "Cool Request for Grails plugin initialized"
        }
    }

    // Plugin exclusion patterns
    def excludedPlugins = []

    // Plugin observation callbacks
    def onChange = { event ->
        // Reload discovery cache when application changes
        log.debug "Change detected: ${event}"
    }

    def onConfigChange = { event ->
        // Handle configuration changes
        log.debug "Configuration changed: ${event}"
    }
}
