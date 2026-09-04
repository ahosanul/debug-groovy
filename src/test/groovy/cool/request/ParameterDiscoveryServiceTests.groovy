package cool.request

import grails.test.mixin.TestFor
import cool.request.discovery.ParameterDiscoveryService

@TestFor(ParameterDiscoveryService)
class ParameterDiscoveryServiceTests {

    def "should identify primitive types"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def isPrimitive = service.isPrimitiveType(String)

        then:
        isPrimitive == true
    }

    def "should handle Integer type"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def isPrimitive = service.isPrimitiveType(Integer)

        then:
        isPrimitive == true
    }

    def "should handle Long type"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def isPrimitive = service.isPrimitiveType(Long)

        then:
        isPrimitive == true
    }

    def "should handle Boolean type"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def isPrimitive = service.isPrimitiveType(Boolean)

        then:
        isPrimitive == true
    }

    def "should generate example value for String"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def example = service.generateExampleValue('String')

        then:
        example == ''
    }

    def "should generate example value for Integer"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def example = service.generateExampleValue('Integer')

        then:
        example == 1
    }

    def "should generate example value for Long"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def example = service.generateExampleValue('Long')

        then:
        example == 1L
    }

    def "should generate example value for Boolean"() {
        given:
        def service = new ParameterDiscoveryService()

        when:
        def example = service.generateExampleValue('Boolean')

        then:
        example == true
    }
}
