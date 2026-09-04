grails.project.class.level = "1.7"
grails.project.web.class.level = "1.7"

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions if required
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on dependencies

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // Maven Central
        mavenRepo "https://repo.grails.org/grails/core"
        mavenCentral()
        
        // Uncomment these to enable remote Maven repositories instead of local ones
        // mavenRepo "http://repository.jboss.com/maven2/"
        // mavenRepo "https://plugins.gradle.org/m2/"
    }

    plugins {
        // Plugins for development only
        build ":tomcat:7.0.55.3"
        
        // No additional dependencies needed - using Grails core
    }

    dependencies {
        // Explicitly declare aspectjweaver and cglib-nodep with correct versions for Grails 2.5.3
        compile 'org.aspectj:aspectjweaver:1.8.4'
        compile 'cglib:cglib-nodep:2.2.2'
    }
}
