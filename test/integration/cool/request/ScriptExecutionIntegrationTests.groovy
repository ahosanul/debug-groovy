package cool.request.integration

import grails.test.mixin.integration.Integration
import cool.request.execution.ScriptExecutionService
import cool.request.model.RequestData
import cool.request.model.ResponseData

/**
 * Integration tests for Groovy script execution (pre-request and post-request).
 * These tests verify that scripts can be safely executed with proper context.
 */
@Integration
class ScriptExecutionIntegrationTests {

    ScriptExecutionService scriptExecutionService

    def setup() {
        scriptExecutionService = new ScriptExecutionService()
    }

    def "should execute simple pre-request script"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/test',
            headers: [:],
            params: [:]
        )
        def script = '''
            request.headers['X-Test'] = 'value'
            request.params['timestamp'] = System.currentTimeMillis().toString()
        '''

        when:
        def result = scriptExecutionService.executePreRequestScript(script, requestData)

        then:
        result != null
        result.headers['X-Test'] == 'value'
        result.params.containsKey('timestamp')
    }

    def "should execute simple post-request script"() {
        given:
        def responseData = new ResponseData(
            status: 200,
            body: '{"name":"test"}',
            headers: ['Content-Type': 'application/json'],
            responseTime: 50
        )
        def script = '''
            if (response.status == 200) {
                return true
            }
            return false
        '''

        when:
        def result = scriptExecutionService.executePostRequestScript(script, responseData)

        then:
        result != null
        result == true
    }

    def "should handle script errors gracefully"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/test'
        )
        def invalidScript = '''
            throw new RuntimeException("Test error")
        '''

        when:
        def result = scriptExecutionService.executePreRequestScript(invalidScript, requestData)

        then:
        result != null
        // Should handle error without crashing
        result instanceof RequestData || result.error != null
    }

    def "should provide environment context to scripts"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/test'
        )
        def script = '''
            request.headers['Environment'] = environment?.name ?: 'default'
        '''

        when:
        def result = scriptExecutionService.executePreRequestScript(script, requestData)

        then:
        result != null
        result.headers.containsKey('Environment')
    }

    def "should allow modifying request body in pre-request script"() {
        given:
        def requestData = new RequestData(
            method: 'POST',
            url: 'http://localhost:8080/test',
            headers: ['Content-Type': 'application/json'],
            body: '{"name":"original"}'
        )
        def script = '''
            def json = new groovy.json.JsonSlurper().parseText(request.body)
            json.name = "modified"
            request.body = new groovy.json.JsonBuilder(json).toPrettyString()
        '''

        when:
        def result = scriptExecutionService.executePreRequestScript(script, requestData)

        then:
        result != null
        result.body.contains('modified')
    }

    def "should extract data from response in post-request script"() {
        given:
        def responseData = new ResponseData(
            status: 200,
            body: '{"id":123,"name":"test","active":true}',
            headers: ['Content-Type': 'application/json'],
            responseTime: 50
        )
        def script = '''
            def json = new groovy.json.JsonSlurper().parseText(response.body)
            return [id: json.id, name: json.name]
        '''

        when:
        def result = scriptExecutionService.executePostRequestScript(script, responseData)

        then:
        result != null
        result.id == 123
        result.name == 'test'
    }

    def "should prevent dangerous operations in scripts"() {
        given:
        def requestData = new RequestData(
            method: 'GET',
            url: 'http://localhost:8080/test'
        )
        def dangerousScript = '''
            // Attempting system access should be handled safely
            System.exit(0)
        '''

        when:
        // This should not actually exit the JVM
        def result = scriptExecutionService.executePreRequestScript(dangerousScript, requestData)

        then:
        // Test passes if we reach here (JVM didn't exit)
        true
    }
}
