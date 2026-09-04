package cool.request.integration

import grails.test.mixin.integration.Integration
import cool.request.execution.RequestExecutionService
import cool.request.model.RequestData

/**
 * Integration tests for HTTP request execution.
 * These tests verify that the request execution service can properly execute requests
 * against the running Grails application.
 */
@Integration
class RequestExecutionIntegrationTests {

    RequestExecutionService requestExecutionService

    def setup() {
        requestExecutionService = new RequestExecutionService()
    }

    def "should execute GET request to internal API"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/cool-request/api/controllers'
        )

        when:
        // Note: This test requires the application to be running
        // In a real integration test environment, this would execute the request
        def result = requestExecutionService.execute(requestData)

        then:
        result != null
        result.status >= 200
        result.status < 500
        result.body != null
    }

    def "should handle POST request with JSON body"() {
        given:
        def requestData = new RequestData(
            method: 'POST',
            url: 'http://localhost:8080/cool-request/api/history',
            headers: ['Content-Type': 'application/json'],
            body: '{"method":"GET","url":"/test"}'
        )

        when:
        def result = requestExecutionService.execute(requestData)

        then:
        result != null
        // Should either succeed or fail gracefully
        result.status > 0
    }

    def "should preserve headers in request execution"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/cool-request/api/controllers',
            headers: [
                'Accept': 'application/json',
                'X-Custom-Header': 'test-value'
            ]
        )

        when:
        def result = requestExecutionService.execute(requestData)

        then:
        result != null
        // Headers should be preserved in execution
        result.requestHeaders?.get('Accept') == 'application/json'
    }

    def "should capture response metadata"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/cool-request/api/controllers'
        )

        when:
        def result = requestExecutionService.execute(requestData)

        then:
        result.status != null
        result.responseTime != null
        result.responseTime > 0
        result.headers != null
    }

    def "should handle query parameters"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/cool-request/api/controllers',
            params: [name: 'user', limit: '10']
        )

        when:
        def result = requestExecutionService.execute(requestData)

        then:
        result != null
        result.status >= 200
    }

    def "should handle errors gracefully"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/cool-request/nonexistent-endpoint'
        )

        when:
        def result = requestExecutionService.execute(requestData)

        then:
        result != null
        result.status >= 400 // Should return error status
    }
}
