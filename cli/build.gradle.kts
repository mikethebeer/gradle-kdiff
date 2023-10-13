plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    applicationName = "kdiff"
}

tasks.shadowDistZip {
    archiveBaseName.set("kdiff")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("io.github.java-diff-utils:java-diff-utils:4.12")
}

application {
    mainClass = "at.mibe.kdiff.ApplicationKt"
}
