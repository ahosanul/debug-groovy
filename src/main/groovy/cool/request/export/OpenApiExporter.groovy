package cool.request.export

import cool.request.model.EndpointMetadata
import groovy.json.JsonBuilder

/**
 * Generates OpenAPI 3.0 specifications from discovered endpoints
 */
class OpenApiExporter {

    /**
     * Generate OpenAPI 3.0 JSON from a list of endpoints
     */
    String generateOpenApi(List<EndpointMetadata> endpoints, String baseUrl = 'http://localhost:8080') {
        def openApi = [
            openapi: '3.0.0',
            info: [
                title: 'Grails Application API',
                version: '1.0.0',
                description: 'Auto-generated OpenAPI specification from Grails application'
            ],
            servers: [
                [url: baseUrl]
            ],
            paths: [:],
            components: [
                schemas: [:]
            ]
        ]

        // Group endpoints by path
        def pathMap = [:].withDefault { [] }
        endpoints.each { endpoint ->
            if (endpoint?.path) {
                def openApiPath = convertToOpenApiPath(endpoint.path)
                pathMap[openApiPath] << endpoint
            }
        }

        // Convert to OpenAPI paths
        pathMap.each { path, endpointList ->
            openApi.paths[path] = [:]
            
            endpointList.each { endpoint ->
                def method = (endpoint.httpMethod ?: 'GET').toLowerCase()
                
                openApi.paths[path][method] = [
                    summary: "${method.toUpperCase()} ${endpoint.path}",
                    description: buildDescription(endpoint),
                    operationId: buildOperationId(endpoint),
                    parameters: buildParameters(endpoint),
                    responses: [
                        '200': [
                            description: 'Successful response',
                            content: [
                                'application/json': [
                                    schema: [type: 'object']
                                ]
                            ]
                        ],
                        '400': [description: 'Bad request'],
                        '404': [description: 'Not found'],
                        '500': [description: 'Internal server error']
                    ]
                ]
                
                // Add request body for POST/PUT/PATCH
                if (method in ['post', 'put', 'patch']) {
                    def requestBody = buildRequestBody(endpoint)
                    if (requestBody) {
                        openApi.paths[path][method].requestBody = requestBody
                    }
                }
            }
        }

        def builder = new JsonBuilder(openApi)
        return builder.toPrettyString()
    }

    /**
     * Convert Grails path syntax to OpenAPI path syntax
     */
    private String convertToOpenApiPath(String grailsPath) {
        if (!grailsPath) return '/'
        
        // Convert $param to {param}
        def openApiPath = grailsPath.replaceAll(/\$([a-zA-Z_][a-zA-Z0-9_]*)/, '{$1}')
        
        // Remove trailing wildcards
        openApiPath = openApiPath.replaceAll('/\*+', '')
        
        return openApiPath.startsWith('/') ? openApiPath : "/${openApiPath}"
    }

    /**
     * Build operation summary/description
     */
    private String buildDescription(EndpointMetadata endpoint) {
        def parts = []
        
        if (endpoint.controller) {
            parts << "Controller: ${endpoint.controller}"
        }
        if (endpoint.action) {
            parts << "Action: ${endpoint.action}"
        }
        
        return parts.join(' | ') ?: "API Endpoint"
    }

    /**
     * Build unique operation ID
     */
    private String buildOperationId(EndpointMetadata endpoint) {
        def method = (endpoint.httpMethod ?: 'GET').toLowerCase()
        def controller = endpoint.controller ?: 'unknown'
        def action = endpoint.action ?: 'action'
        
        return "${method}${controller.capitalize()}${action.capitalize()}"
    }

    /**
     * Build parameters array
     */
    private List buildParameters(EndpointMetadata endpoint) {
        def parameters = []
        
        // Path parameters
        if (endpoint.parameters) {
            endpoint.parameters.findAll { it.isPathParameter }.each { param ->
                parameters << [
                    name: param.name,
                    in: 'path',
                    required: param.required ?: true,
                    schema: [
                        type: mapTypeToOpenApi(param.type)
                    ]
                ]
            }
            
            // Query parameters (if we can detect them)
            endpoint.parameters.findAll { it.isQueryParameter }.each { param ->
                parameters << [
                    name: param.name,
                    in: 'query',
                    required: param.required ?: false,
                    schema: [
                        type: mapTypeToOpenApi(param.type)
                    ]
                ]
            }
        }
        
        return parameters
    }

    /**
     * Build request body for POST/PUT/PATCH
     */
    private Map buildRequestBody(EndpointMetadata endpoint) {
        // If there are command objects or complex parameters, create a schema
        def properties = [:]
        def required = []
        
        if (endpoint.parameters) {
            endpoint.parameters.findAll { !it.isPathParameter && !it.isQueryParameter }.each { param ->
                properties[param.name] = [
                    type: mapTypeToOpenApi(param.type),
                    example: param.example
                ]
                
                if (param.required) {
                    required << param.name
                }
            }
        }
        
        if (properties) {
            def requestBody = [
                description: 'Request body',
                content: [
                    'application/json': [
                        schema: [
                            type: 'object',
                            properties: properties
                        ]
                    ]
                ]
            ]
            
            if (required) {
                requestBody.content['application/json'].schema.required = required
            }
            
            return requestBody
        }
        
        return null
    }

    /**
     * Map Groovy/Grails types to OpenAPI types
     */
    private String mapTypeToOpenApi(String type) {
        if (!type) return 'string'
        
        switch (type.toLowerCase()) {
            case 'string':
            case 'java.lang.string':
            case 'groovy.lang.gstring':
                return 'string'
            case 'integer':
            case 'java.lang.integer':
            case 'int':
                return 'integer'
            case 'long':
            case 'java.lang.long':
                return 'integer'
            case 'boolean':
            case 'java.lang.boolean':
                return 'boolean'
            case 'double':
            case 'java.lang.double':
            case 'float':
            case 'java.lang.float':
            case 'bigdecimal':
            case 'java.math.bigdecimal':
                return 'number'
            case 'date':
            case 'java.util.date':
            case 'calendar':
            case 'java.util.calendar':
                return 'string' // ISO 8601 date-time
            default:
                return 'string'
        }
    }
}
