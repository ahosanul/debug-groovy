package cool.request.integration

import grails.test.mixin.integration.Integration
import cool.request.discovery.ControllerDiscoveryService
import cool.request.discovery.UrlMappingDiscoveryService
import cool.request.discovery.ParameterDiscoveryService
import cool.request.export.CurlExporter
import cool.request.export.OpenApiExporter

/**
 * Integration tests for Cool Request plugin discovery and export services.
 * These tests verify that the services work correctly with a real Grails application context.
 */
@Integration
class DiscoveryIntegrationTests {

    ControllerDiscoveryService controllerDiscoveryService
    UrlMappingDiscoveryService urlMappingDiscoveryService
    ParameterDiscoveryService parameterDiscoveryService
    CurlExporter curlExporter
    OpenApiExporter openApiExporter

    def setup() {
        controllerDiscoveryService = new ControllerDiscoveryService(grailsApplication: grailsApplication)
        urlMappingDiscoveryService = new UrlMappingDiscoveryService(grailsApplication: grailsApplication)
        parameterDiscoveryService = new ParameterDiscoveryService()
        curlExporter = new CurlExporter()
        openApiExporter = new OpenApiExporter()
    }

    def "should discover controllers from real Grails application"() {
        when:
        def controllers = controllerDiscoveryService.discoverControllers()

        then:
        controllers != null
        controllers instanceof List
        // Should find at least CoolRequestController
        controllers.size() >= 1
        controllers.any { it.name == 'coolRequest' }
    }

    def "should discover URL mappings from real Grails application"() {
        when:
        def mappings = urlMappingDiscoveryService.discoverMappings()

        then:
        mappings != null
        mappings instanceof List
        // Should find at least the /cool-request mapping
        mappings.size() >= 1
    }

    def "should extract action metadata from discovered controllers"() {
        given:
        def controllers = controllerDiscoveryService.discoverControllers()
        def coolRequestController = controllers.find { it.name == 'coolRequest' }

        expect:
        coolRequestController != null
        coolRequestController.actions != null
        coolRequestController.actions.size() > 0
    }

    def "should generate valid cURL command for discovered endpoint"() {
        given:
        def requestData = [
            method: 'GET',
            url: 'http://localhost:8080/cool-request/api/controllers',
            headers: ['Accept': 'application/json']
        ]

        when:
        def curlCommand = curlExporter.generateCurl(requestData as cool.request.model.RequestData)

        then:
        curlCommand != null
        curlCommand.contains('curl')
        curlCommand.contains('-X GET')
        curlCommand.contains('/cool-request/api/controllers')
    }

    def "should generate valid OpenAPI spec from discovered endpoints"() {
        given:
        def controllers = controllerDiscoveryService.discoverControllers()
        def mappings = urlMappingDiscoveryService.discoverMappings()

        when:
        def openapi = openApiExporter.generateOpenApi('Cool Request Test', '1.0.0')
        
        // Add discovered endpoints to the spec
        mappings.each { mapping ->
            openApiExporter.addEndpoint(openapi, mapping)
        }

        then:
        openapi != null
        openapi.openapi == '3.0.0'
        openapi.info.title == 'Cool Request Test'
        openapi.paths != null
    }

    def "should handle parameter discovery for common types"() {
        expect:
        parameterDiscoveryService.isPrimitiveType(String)
        parameterDiscoveryService.isPrimitiveType(Integer)
        parameterDiscoveryService.isPrimitiveType(Long)
        parameterDiscoveryService.isPrimitiveType(Boolean)
        parameterDiscoveryService.isPrimitiveType(Double)
    }

    def "should generate example values for different types"() {
        expect:
        parameterDiscoveryService.generateExampleValue('String') == ''
        parameterDiscoveryService.generateExampleValue('Integer') == 1
        parameterDiscoveryService.generateExampleValue('Long') == 1L
        parameterDiscoveryService.generateExampleValue('Boolean') == true
    }
}
