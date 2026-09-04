package cool.request.model

/**
 * Metadata for a Grails job
 */
class JobMetadata implements Serializable {
    String name
    String className
    String packageName
    String cronExpression
    String triggerType // 'cron', 'interval', 'simple'
    Long intervalMs
    boolean isScheduled = false
    String description
    
    JobMetadata() {}
    
    JobMetadata(String name, String className) {
        this.name = name
        this.className = className
    }
    
    @Override
    String toString() {
        return "JobMetadata{name='$name', className='$className', isScheduled=$isScheduled}"
    }
}
