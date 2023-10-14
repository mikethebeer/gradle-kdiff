plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.0"
    id("de.undercouch.download") version "5.5.0"
    id("com.gradle.plugin-publish") version "1.2.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("de.undercouch:gradle-download-task:5.5.0")
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

gradlePlugin {
    website = "https://github.com/mikethebeer/gradle-kdiff"
    vcsUrl = "https://github.com/mikethebeer/gradle-kdiff"
    // Define the plugin
    val kDiff by plugins.creating {
        id = "io.github.mikethebeer.gradle.kdiff"
        implementationClass = "io.github.mikethebeer.gradle.kdiff.GradleKDiffPlugin"
        version = rootProject.version
        group = "io.github.mikethebeer"
        displayName = "Gradle KDiff Plugin"
        description = "A plugin to download and install KDiff"
        tags = listOf("kdiff", "kustomize", "diff")
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

/**
 * Task gets called during the build lifecycle that involves compiling and packaging the plugin.
 * e.g. like running tasks like `build` or `assemble`. `processResources` will be automatically
 * triggered as a dependency of these tasks.
 */
tasks.named<Copy>("processResources") {
    expand("version" to version)
}

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
