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
        intellijIdeaCommunity("2024.1")
        
        // Required bundled plugins
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.groovy")
        
        // Plugin development tools
        pluginVerifier()
        zipSigner()
    }
    
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
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
