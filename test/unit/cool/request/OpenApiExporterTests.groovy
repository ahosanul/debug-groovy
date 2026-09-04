package cool.request

import grails.test.mixin.TestFor
import cool.request.export.OpenApiExporter
import cool.request.model.EndpointMetadata
import cool.request.model.ControllerMetadata
import cool.request.model.ActionMetadata

@TestFor(OpenApiExporter)
class OpenApiExporterTests {

    def "should generate basic OpenAPI structure"() {
        given:
        def exporter = new OpenApiExporter()

        when:
        def openapi = exporter.generateOpenApi('Test App', '1.0.0')

        then:
        openapi.openapi == '3.0.0'
        openapi.info.title == 'Test App'
        openapi.info.version == '1.0.0'
        openapi.paths != null
    }

    def "should add endpoint to OpenAPI spec"() {
        given:
        def exporter = new OpenApiExporter()
        def endpoint = new EndpointMetadata(
            path: '/api/users/{id}',
            httpMethod: 'GET',
            controller: 'user',
            action: 'show'
        )

        when:
        def openapi = exporter.generateOpenApi('Test App', '1.0.0')
        exporter.addEndpoint(openapi, endpoint)

        then:
        openapi.paths.'/api/users/{id}' != null
        openapi.paths.'/api/users/{id}'.get != null
    }

    def "should handle different HTTP methods"() {
        given:
        def exporter = new OpenApiExporter()
        def getEndpoint = new EndpointMetadata(
            path: '/api/users',
            httpMethod: 'GET',
            controller: 'user',
            action: 'index'
        )
        def postEndpoint = new EndpointMetadata(
            path: '/api/users',
            httpMethod: 'POST',
            controller: 'user',
            action: 'save'
        )

        when:
        def openapi = exporter.generateOpenApi('Test App', '1.0.0')
        exporter.addEndpoint(openapi, getEndpoint)
        exporter.addEndpoint(openapi, postEndpoint)

        then:
        openapi.paths.'/api/users'.get != null
        openapi.paths.'/api/users'.post != null
    }

    def "should add path parameters"() {
        given:
        def exporter = new OpenApiExporter()
        def endpoint = new EndpointMetadata(
            path: '/api/users/{id}',
            httpMethod: 'GET',
            controller: 'user',
            action: 'show'
        )

        when:
        def openapi = exporter.generateOpenApi('Test App', '1.0.0')
        exporter.addEndpoint(openapi, endpoint)
        def pathItem = openapi.paths.'/api/users/{id}'.get

        then:
        pathItem.parameters != null
        pathItem.parameters.size() > 0
    }
}
