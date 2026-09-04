package cool.request

import grails.test.mixin.TestFor
import cool.request.export.CurlExporter
import cool.request.model.RequestData

@TestFor(CurlExporter)
class CurlExporterTests {

    def "should generate basic GET curl command"() {
        given:
        def exporter = new CurlExporter()
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/api/users'
        )

        when:
        def curl = exporter.generateCurl(requestData)

        then:
        curl.contains('curl -X GET')
        curl.contains('http://localhost:8080/api/users')
    }

    def "should generate POST curl command with JSON body"() {
        given:
        def exporter = new CurlExporter()
        def requestData = new RequestData(
            method: 'POST',
            url: 'http://localhost:8080/api/users',
            headers: ['Content-Type': 'application/json'],
            body: '{"name":"John"}'
        )

        when:
        def curl = exporter.generateCurl(requestData)

        then:
        curl.contains('curl -X POST')
        curl.contains('-H \'Content-Type: application/json\'')
        curl.contains('-d')
    }

    def "should escape special characters in URL"() {
        given:
        def exporter = new CurlExporter()
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/api/users?name=John Doe'
        )

        when:
        def curl = exporter.generateCurl(requestData)

        then:
        curl.contains("'") // URL should be quoted
    }

    def "should include multiple headers"() {
        given:
        def exporter = new CurlExporter()
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/api/users',
            headers: [
                'Authorization': 'Bearer token123',
                'Accept': 'application/json'
            ]
        )

        when:
        def curl = exporter.generateCurl(requestData)

        then:
        curl.contains('-H \'Authorization: Bearer token123\'')
        curl.contains('-H \'Accept: application/json\'')
    }
}
