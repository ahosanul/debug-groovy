package cool.request.discovery

import cool.request.model.JobMetadata
import grails.util.Holders

/**
 * Discovers Grails jobs (scheduled tasks)
 */
class JobDiscoveryService {
    
    static transactional = false
    
    /**
     * Discover all jobs in the application
     */
    List<JobMetadata> discoverJobs() {
        def jobs = []
        
        try {
            // Get all job classes from GrailsApplication
            def grailsApp = Holders.grailsApplication
            
            if (grailsApp) {
                def jobClasses = grailsApp.allClasses?.findAll { clazz ->
                    isJobClass(clazz)
                }
                
                jobClasses?.each { clazz ->
                    try {
                        def metadata = extractJobMetadata(clazz)
                        if (metadata) {
                            jobs << metadata
                        }
                    } catch (Exception e) {
                        log.warn "Error processing job: ${clazz?.name}", e
                    }
                }
            }
            
            // Sort by name
            jobs.sort { it.name }
            
        } catch (Exception e) {
            log.error "Error discovering jobs", e
        }
        
        return jobs
    }
    
    /**
     * Check if a class is a Grails job
     */
    private boolean isJobClass(Class clazz) {
        if (!clazz) return false
        
        def className = clazz.simpleName
        
        // Jobs typically end with "Job"
        if (!className.endsWith('Job')) {
            return false
        }
        
        // Exclude abstract classes
        if (java.lang.reflect.Modifier.isAbstract(clazz.modifiers)) {
            return false
        }
        
        return true
    }
    
    /**
     * Extract metadata from a job class
     */
    private JobMetadata extractJobMetadata(Class clazz) {
        def metadata = new JobMetadata(
            clazz.simpleName.replaceAll('Job$', ''),
            clazz.name
        )
        metadata.packageName = clazz.package?.name
        
        try {
            // Try to find cron expression
            def cronField = clazz.getDeclaredField('cronExpression')
            if (cronField) {
                cronField.setAccessible(true)
                metadata.cronExpression = cronField.get(null)?.toString()
                metadata.triggerType = 'cron'
                metadata.isScheduled = !!metadata.cronExpression
            }
            
            // Try to find triggerType
            def triggerTypeField = clazz.getDeclaredField('triggerType')
            if (triggerTypeField) {
                triggerTypeField.setAccessible(true)
                def triggerType = triggerTypeField.get(null)?.toString()
                if (triggerType) {
                    metadata.triggerType = triggerType
                    metadata.isScheduled = true
                }
            }
            
            // Try to find interval
            def intervalField = clazz.getDeclaredField('interval')
            if (intervalField) {
                intervalField.setAccessible(true)
                def interval = intervalField.get(null)
                if (interval instanceof Number) {
                    metadata.intervalMs = interval.longValue()
                    metadata.triggerType = 'interval'
                    metadata.isScheduled = true
                }
            }
            
            // Try to find description
            def descField = clazz.getDeclaredField('description')
            if (descField) {
                descField.setAccessible(true)
                metadata.description = descField.get(null)?.toString()
            }
            
        } catch (NoSuchFieldException e) {
            // No scheduling fields found - manual job only
            log.debug "Job ${clazz.simpleName} has no scheduling configuration"
        } catch (Exception e) {
            log.warn "Error extracting job metadata for ${clazz.simpleName}", e
        }
        
        return metadata
    }
    
    /**
     * Execute a job manually
     */
    Map<String, Object> executeJob(String jobName) {
        def result = [success: false, message: '', error: null]
        
        try {
            def jobs = discoverJobs()
            def job = jobs.find { it.name.equalsIgnoreCase(jobName) }
            
            if (!job) {
                result.message = "Job not found: ${jobName}"
                return result
            }
            
            // Load and execute the job
            def jobClass = Class.forName(job.className)
            def jobInstance = jobClass.newInstance()
            
            // Find execute method
            def executeMethod = jobClass.methods.find { m -> 
                m.name == 'execute' && m.parameterTypes.length == 0 
            }
            
            if (executeMethod) {
                long startTime = System.currentTimeMillis()
                executeMethod.invoke(jobInstance)
                long endTime = System.currentTimeMillis()
                
                result.success = true
                result.message = "Job executed successfully in ${endTime - startTime}ms"
                result.executionTimeMs = endTime - startTime
            } else {
                result.message = "No execute() method found in job"
            }
            
        } catch (Exception e) {
            result.success = false
            result.message = "Job execution failed"
            result.error = e.message
            log.error "Error executing job: ${jobName}", e
        }
        
        return result
    }
}
