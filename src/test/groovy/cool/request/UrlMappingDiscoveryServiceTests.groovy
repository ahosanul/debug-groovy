package cool.request

import grails.test.mixin.TestFor
import cool.request.discovery.UrlMappingDiscoveryService
import cool.request.model.EndpointMetadata

@TestFor(UrlMappingDiscoveryService)
class UrlMappingDiscoveryServiceTests {

    def "should discover URL mappings from grailsApplication"() {
        given:
        def service = new UrlMappingDiscoveryService(grailsApplication: grailsApplication)

        when:
        def mappings = service.discoverMappings()

        then:
        mappings != null
        mappings instanceof List
    }

    def "should extract path variables from URL pattern"() {
        given:
        def service = new UrlMappingDiscoveryService()

        when:
        def variables = service.extractPathVariables('/api/users/$id')

        then:
        variables.size() == 1
        variables[0] == 'id'
    }

    def "should handle multiple path variables"() {
        given:
        def service = new UrlMappingDiscoveryService()

        when:
        def variables = service.extractPathVariables('/api/users/$userId/posts/$postId')

        then:
        variables.size() == 2
        variables[0] == 'userId'
        variables[1] == 'postId'
    }

    def "should return empty list for static paths"() {
        given:
        def service = new UrlMappingDiscoveryService()

        when:
        def variables = service.extractPathVariables('/api/static/path')

        then:
        variables.isEmpty()
    }
}
