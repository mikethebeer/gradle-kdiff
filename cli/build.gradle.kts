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
    implementation("com.github.sya-ri:kgit:1.0.5")
}

application {
    mainClass = "io.github.mikethebeer.kdiff.ApplicationKt"
}
