package io.github.mikethebeer.gradle.kdiff

import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir

class GradleKDiffPluginFunctionalTest {
    companion object {
        @field:TempDir
        lateinit var projectDir: File

        private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
        private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

        @BeforeAll
        @JvmStatic
        fun setupBuildFiles() {
            // Set up the default content for the test build
            settingsFile.writeText("")
            buildFile.writeText("""
                plugins {
                    id("io.github.mikethebeer.gradle.kdiff")
                }
            """.trimIndent())
        }
    }

    @Test
    fun `can run kDiffVersion task`() {
        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("kDiffVersion")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("GradleKDiffPlugin version: unspecified"))
    }

    @Test
    fun `can download and install Kustomize`() {
        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("installKustomize")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertTrue(projectDir.resolve("build/bin/install_kustomize.sh").exists())
        assertTrue(projectDir.resolve("build/bin/kustomize").exists())
    }
}
