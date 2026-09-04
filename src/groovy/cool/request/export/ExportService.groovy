package cool.request

import cool.request.discovery.*
import cool.request.model.*
import groovy.json.JsonBuilder

/**
 * Exports data in various formats (cURL, OpenAPI, etc.)
 */
class ExportService {
    
    static transactional = false
    
    CoolRequestDiscoveryService coolRequestDiscoveryService
    
    /**
     * Generate cURL command from request context
     */
    String generateCurl(RequestContext context) {
        def curl = ['curl']
        
        // Add HTTP method
        if (context.method && context.method.toUpperCase() != 'GET') {
            curl << "-X ${context.method.toUpperCase()}"
        }
        
        // Build URL
        def url = buildUrl(context)
        curl << "'${escapeSingleQuotes(url)}'"
        
        // Add headers
        context.headers?.each { key, value ->
            curl << "-H '${escapeSingleQuotes("${key}: ${value}")}'"
        }
        
        // Add content type if not already in headers
        if (context.contentType && !context.headers?.find { k, v -> k.equalsIgnoreCase('Content-Type') }) {
            curl << "-H 'Content-Type: ${context.contentType}'"
        }
        
        // Add cookies
        if (context.cookies) {
            def cookieHeader = context.cookies.collect { k, v -> "${k}=${v}" }.join('; ')
            if (cookieHeader) {
                curl << "-H 'Cookie: ${escapeSingleQuotes(cookieHeader)}'"
            }
        }
        
        // Add body for POST/PUT/PATCH
        if (context.body && ['POST', 'PUT', 'PATCH'].contains(context.method?.toUpperCase())) {
            // Escape single quotes in body
            def escapedBody = escapeSingleQuotes(context.body)
            curl << "-d '${escapedBody}'"
        }
        
        return curl.join(' \\\n  ')
    }
    
    /**
     * Generate OpenAPI specification from discovered endpoints
     */
    Map<String, Object> generateOpenApiSpec() {
        def metadata = coolRequestDiscoveryService.getApplicationMetadata()
        
        def spec = [
            openapi: '3.0.0',
            info: [
                title: metadata.applicationName ?: 'Grails Application API',
                version: '1.0.0',
                description: "Auto-generated OpenAPI specification for ${metadata.applicationName ?: 'Grails Application'}"
            ],
            servers: [
                [
                    url: 'http://localhost:8080',
                    description: 'Development server'
                ]
            ],
            paths: [:],
            components: [
                schemas: [:]
            ]
        ]
        
        // Process endpoints
        def endpoints = metadata.endpoints ?: []
        endpoints.each { endpoint ->
            def path = endpoint.path
            
            if (!spec.paths.containsKey(path)) {
                spec.paths[path] = [:]
            }
            
            def method = (endpoint.httpMethod ?: 'get').toLowerCase()
            
            spec.paths[path][method] = [
                summary: "${endpoint.action ?: 'Action'} - ${endpoint.controller ?: 'Controller'}",
                operationId: "${endpoint.controller ?: 'api'}_${endpoint.action ?: 'action'}",
                tags: [endpoint.controller ? endpoint.controller.capitalize() : 'API'],
                parameters: buildOpenApiParameters(endpoint),
                responses: [
                    '200': [
                        description: 'Successful response'
                    ],
                    '400': [
                        description: 'Bad request'
                    ],
                    '404': [
                        description: 'Resource not found'
                    ],
                    '500': [
                        description: 'Internal server error'
                    ]
                ]
            ]
        }
        
        // Add schema definitions from domain classes if available
        // This would require DomainMetadata discovery
        
        return spec
    }
    
    /**
     * Build OpenAPI parameters from endpoint metadata
     */
    private List<Map<String, Object>> buildOpenApiParameters(def endpoint) {
        def parameters = []
        
        // Path parameters
        endpoint.parameters?.findAll { it.isPathParameter }?.each { param ->
            parameters << [
                name: param.name,
                in: 'path',
                required: param.required ?: true,
                schema: [
                    type: mapTypeToOpenApi(param.type)
                ]
            ]
        }
        
        // Query parameters
        endpoint.parameters?.findAll { !it.isPathParameter }?.each { param ->
            parameters << [
                name: param.name,
                in: 'query',
                required: param.required ?: false,
                schema: [
                    type: mapTypeToOpenApi(param.type)
                ]
            ]
        }
        
        return parameters
    }
    
    /**
     * Map Groovy/Java types to OpenAPI types
     */
    private String mapTypeToOpenApi(String type) {
        switch (type) {
            case 'String':
            case 'string':
                return 'string'
            case 'Integer':
            case 'int':
            case 'Long':
            case 'long':
            case 'Short':
            case 'short':
            case 'Byte':
            case 'byte':
                return 'integer'
            case 'Boolean':
            case 'boolean':
                return 'boolean'
            case 'Double':
            case 'double':
            case 'Float':
            case 'float':
            case 'BigDecimal':
                return 'number'
            case 'Date':
            case 'Calendar':
                return 'string'
            default:
                return 'string'
        }
    }
    
    /**
     * Convert OpenAPI spec to YAML format
     */
    String convertToYaml(Map<String, Object> spec) {
        // Simple YAML conversion without external dependencies
        // For production use, consider using a proper YAML library
        
        def yaml = new StringBuilder()
        convertToYamlRecursive(spec, yaml, 0)
        return yaml.toString()
    }
    
    private void convertToYamlRecursive(def obj, StringBuilder yaml, int indent) {
        def spaces = '  ' * indent
        
        if (obj instanceof Map) {
            obj.each { key, value ->
                if (value instanceof Map || value instanceof List) {
                    yaml.append("${spaces}${key}:\n")
                    convertToYamlRecursive(value, yaml, indent + 1)
                } else if (value instanceof String) {
                    yaml.append("${spaces}${key}: \"${escapeYamlString(value)}\"\n")
                } else {
                    yaml.append("${spaces}${key}: ${value}\n")
                }
            }
        } else if (obj instanceof List) {
            obj.each { item ->
                if (item instanceof Map) {
                    def first = true
                    item.each { key, value ->
                        if (first) {
                            yaml.append("${spaces}- ${key}:")
                            first = false
                        } else {
                            yaml.append("${spaces}  ${key}:")
                        }
                        
                        if (value instanceof Map || value instanceof List) {
                            yaml.append("\n")
                            convertToYamlRecursive(value, yaml, indent + 2)
                        } else if (value instanceof String) {
                            yaml.append(" \"${escapeYamlString(value)}\"\n")
                        } else {
                            yaml.append(" ${value}\n")
                        }
                    }
                } else {
                    yaml.append("${spaces}- ${item}\n")
                }
            }
        }
    }
    
    private String escapeYamlString(String str) {
        return str.replace('\\', '\\\\')
                  .replace('"', '\\"')
                  .replace('\n', '\\n')
    }
    
    /**
     * Escape single quotes for shell commands
     */
    private String escapeSingleQuotes(String str) {
        if (!str) return ''
        return str.replace("'", "'\"'\"'")
    }
    
    /**
     * Build URL from context
     */
    private String buildUrl(RequestContext context) {
        def baseUrl = "http://localhost:8080"
        def path = context.path ?: '/'
        
        // Apply path params
        context.pathParams?.each { key, value ->
            path = path.replace("{${key}}", value)
                      .replace(":${key}", value)
        }
        
        // Add query params
        if (context.queryParams) {
            def queryString = context.queryParams.collect { k, v ->
                "${URLEncoder.encode(k, 'UTF-8')}=${URLEncoder.encode(v ?: '', 'UTF-8')}"
            }.join('&')
            
            if (queryString) {
                path += "?${queryString}"
            }
        }
        
        return "${baseUrl}${path}"
    }
}
