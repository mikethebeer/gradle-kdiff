plugins {
    base
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.0"
    id("io.github.mikethebeer.gradle.kdiff")
    id("com.lovelysystems.gradle") version ("1.12.0")
}

lovely {
    gitProject()
}
