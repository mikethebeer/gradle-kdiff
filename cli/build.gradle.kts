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
    implementation("com.github.sya-ri:kgit:1.0.6")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.8.0.202311291450-r")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
}

application {
    mainClass = "io.github.mikethebeer.kdiff.ApplicationKt"
}
