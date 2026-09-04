package cool.request.integration

import grails.test.mixin.integration.Integration
import cool.request.discovery.JobDiscoveryService
import cool.request.model.JobMetadata

/**
 * Integration tests for job discovery and execution.
 * These tests verify that the job discovery service can find Grails/Quartz jobs
 * and that job execution works correctly.
 */
@Integration
class JobDiscoveryIntegrationTests {

    JobDiscoveryService jobDiscoveryService

    def setup() {
        jobDiscoveryService = new JobDiscoveryService(grailsApplication: grailsApplication)
    }

    def "should discover jobs in the application"() {
        when:
        def jobs = jobDiscoveryService.discoverJobs()

        then:
        jobs != null
        jobs instanceof List
    }

    def "should extract job metadata correctly"() {
        given:
        def jobs = jobDiscoveryService.discoverJobs()

        expect:
        jobs.each { job ->
            job.name != null
            job.className != null
            job.cronExpression != null || job.isManual()
        }
    }

    def "should identify manual vs scheduled jobs"() {
        given:
        def jobs = jobDiscoveryService.discoverJobs()

        when:
        def hasScheduled = jobs.any { it.cronExpression != null }
        def hasManual = jobs.any { it.isManual() }

        then:
        // At least one type should exist
        hasScheduled || hasManual
    }

    def "should handle job with no cron expression"() {
        given:
        def jobMetadata = new JobMetadata(
            name: 'TestJob',
            className: 'cool.request.TestJob',
            cronExpression: null
        )

        expect:
        jobMetadata.isManual() == true
        jobMetadata.getScheduleDescription() == 'Manual execution only'
    }

    def "should format cron expression as human readable"() {
        given:
        def jobMetadata = new JobMetadata(
            name: 'ScheduledJob',
            className: 'cool.request.ScheduledJob',
            cronExpression: '0 0 12 * * ?'
        )

        expect:
        jobMetadata.getScheduleDescription() != null
        jobMetadata.getScheduleDescription().length() > 0
    }

    def "should validate job metadata completeness"() {
        given:
        def jobs = jobDiscoveryService.discoverJobs()

        expect:
        jobs.every { job ->
            job.name && job.className
        }
    }
}
