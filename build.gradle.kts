plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.arafat.grails.debug"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform dependencies
    intellijPlatform {
        intellijIdeaCommunity("2026.1.4")
        
        // Required bundled plugins
        java()
        groovy()
        grails()
        
        // Plugin development tools
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
    
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-intellij:1.10.1")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    patchPluginXml {
        sinceBuild.set("261")
        untilBuild.set("263.*")
    }
    
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    
    verifyPlugin {
        dependsOn(patchPluginXml)
    }
    
    buildSearchableOptions {
        enabled = false
    }
}

intellijPlatform {
    instrumentCode = true
}
