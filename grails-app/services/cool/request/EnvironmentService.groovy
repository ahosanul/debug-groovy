package cool.request

import grails.util.GrailsUtil
import grails.util.Holders

/**
 * Provides environment information and management
 */
class EnvironmentService {
    
    static transactional = false
    
    // Available environments
    private static final ENVIRONMENTS = [
        'development',
        'test',
        'staging',
        'production',
        'custom'
    ]
    
    /**
     * Get current Grails environment
     */
    String getCurrentEnvironment() {
        try {
            return GrailsUtil.environment ?: 'development'
        } catch (Exception e) {
            return 'development'
        }
    }
    
    /**
     * Check if running in production
     */
    boolean isProduction() {
        def env = getCurrentEnvironment()
        return env.equalsIgnoreCase('production') || 
               env.equalsIgnoreCase('prod')
    }
    
    /**
     * Check if running in development
     */
    boolean isDevelopment() {
        def env = getCurrentEnvironment()
        return env.equalsIgnoreCase('development') || 
               env.equalsIgnoreCase('dev')
    }
    
    /**
     * Get available environments
     */
    List<String> getAvailableEnvironments() {
        return ENVIRONMENTS
    }
    
    /**
     * Get environment variables/configuration
     */
    Map<String, Object> getEnvironmentConfig(String environment) {
        def config = [:]
        
        try {
            def grailsApp = Holders.grailsApplication
            def appConfig = grailsApp?.config
            
            if (appConfig) {
                // Try to get environment-specific config
                def envConfig = appConfig."environments"?."${environment}"
                if (envConfig instanceof Map) {
                    config.putAll(envConfig)
                }
            }
            
        } catch (Exception e) {
            log.warn "Error getting environment config", e
        }
        
        return config
    }
    
    /**
     * Get all environment configurations
     */
    Map<String, Map<String, Object>> getAllEnvironmentConfigs() {
        def configs = [:]
        
        ENVIRONMENTS.each { env ->
            configs[env] = getEnvironmentConfig(env)
        }
        
        return configs
    }
    
    /**
     * Check if Cool Request should be enabled for current environment
     */
    boolean isEnabled() {
        try {
            def grailsApp = Holders.grailsApplication
            def config = grailsApp?.config
            
            // Check explicit enable/disable
            def coolRequestConfig = config?.coolRequest
            if (coolRequestConfig instanceof Map) {
                def enabled = coolRequestConfig.enabled
                if (enabled != null) {
                    return enabled as Boolean
                }
                
                // Check production allowance
                def allowProduction = coolRequestConfig.allowProduction
                if (isProduction() && !allowProduction) {
                    return false
                }
            }
            
            // Default: enabled in development, disabled in production
            return !isProduction()
            
        } catch (Exception e) {
            log.error "Error checking Cool Request enabled status", e
            return !isProduction()
        }
    }
    
    /**
     * Get the configured path for Cool Request
     */
    String getConfiguredPath() {
        try {
            def grailsApp = Holders.grailsApplication
            def config = grailsApp?.config?.coolRequest
            
            return config?.path ?: '/cool-request'
            
        } catch (Exception e) {
            return '/cool-request'
        }
    }
}
