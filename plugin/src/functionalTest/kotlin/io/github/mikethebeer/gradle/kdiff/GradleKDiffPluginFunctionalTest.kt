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
            buildFile.writeText(
                """
                plugins {
                    id("io.github.mikethebeer.gradle.kdiff")
                }
                project.version = "0.1.0"
            """.trimIndent()
            )
        }
    }

    private fun runTask(taskName: String): String {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(taskName)
        runner.withProjectDir(projectDir)
        val result = runner.build()

        return result.output
    }

    @Test
    fun `can run kDiffVersion task`() {
        val output = runTask("kDiffVersion")
        assertTrue(output.contains("GradleKDiffPlugin version: unspecified"))
    }

    @Test
    fun `can download and install Kustomize`() {
        runTask("installKustomize")
        assertTrue(projectDir.resolve("build/bin/install_kustomize.sh").exists())
        assertTrue(projectDir.resolve("build/bin/kustomize").exists())
    }

    @Test
    fun `can download and install KDiff`() {
        runTask("installKdiff")
        assertTrue(projectDir.resolve("build/bin/kdiff").exists())
        assertTrue(projectDir.resolve("build/kdiff-0.1.0.zip").exists())
    }
}
