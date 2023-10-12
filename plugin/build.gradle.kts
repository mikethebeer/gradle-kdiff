plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.0"
    id("de.undercouch.download") version "5.5.0"
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
    // Define the plugin
    val kDiff by plugins.creating {
        id = "at.mibe.gradle.kdiff"
        implementationClass = "at.mibe.gradle.kdiff.GradleKDiffPlugin"
        version = rootProject.version
        group = "at.mibe"
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
