package cool.request

import grails.test.mixin.TestFor
import cool.request.discovery.ControllerDiscoveryService
import cool.request.model.ControllerMetadata
import cool.request.model.ActionMetadata

@TestFor(ControllerDiscoveryService)
class ControllerDiscoveryServiceTests {

    def "should discover all controllers in the application"() {
        when:
        def service = new ControllerDiscoveryService(grailsApplication: grailsApplication)
        def controllers = service.discoverControllers()

        then:
        controllers != null
        controllers instanceof List
    }

    def "should extract controller name from class name"() {
        given:
        def service = new ControllerDiscoveryService()

        when:
        def name = service.extractControllerName('UserController')

        then:
        name == 'user'
    }

    def "should handle controller name without Controller suffix"() {
        given:
        def service = new ControllerDiscoveryService()

        when:
        def name = service.extractControllerName('MyController')

        then:
        name == 'my'
    }
}
