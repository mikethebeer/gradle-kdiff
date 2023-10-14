package io.github.mikethebeer.gradle.kdiff

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import java.util.*

@Suppress("unused")
class GradleKDiffPlugin : Plugin<Project> {

    private val version: String

    init {
        val props = Properties()
        this::class.java.classLoader.getResourceAsStream("plugin.properties")?.use { stream ->
            props.load(stream)
        }
        version = props.getProperty("version")
    }

    override fun apply(project: Project) {
        val kDiffLocation = project.layout.buildDirectory

        // Apply the third-party plugin
        project.apply { action ->
            action.plugin("de.undercouch.download")
        }

        project.tasks.register("kDiffVersion") {
            it.group = KDIFF_GROUP
            it.description = "Prints the kDiff version"

            val version = project.rootProject.version
            it.doLast {
                println("GradleKDiffPlugin version: $version")
            }
        }

        val downloadKustomize = project.tasks.register("downloadKustomize", Download::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Download kustomize CLI"

            it.src("https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh")
            it.dest(kDiffLocation.dir("bin"))
            it.overwrite(false)
        }

        val installKustomize = project.tasks.register("installKustomize", Exec::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Install kustomize CLI"

            it.onlyIf { !kDiffLocation.dir("bin/kustomize").get().asFile.exists() }
            it.dependsOn(downloadKustomize)

            it.workingDir(kDiffLocation.dir("bin"))
            // only install kustomize if it is not already installed
            it.commandLine("bash", "install_kustomize.sh")
        }

        val downloadKDiff = project.tasks.register("downloadKDiff", Download::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Download kDiff CLI"

            val version = project.rootProject.version

            it.src("https://github.com/mikethebeer/gradle-kdiff/releases/download/$version/kdiff-$version.zip")
            it.dest(project.layout.buildDirectory.file("kdiff.zip"))
            it.overwrite(false)
        }

        project.tasks.register("installKDiff", Copy::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Install kDiff CLI"

            it.dependsOn(downloadKDiff)

            it.from(project.zipTree(downloadKDiff.get().dest)) { f ->
                f.include("kdiff-*/**")
                f.eachFile { cd ->
                    cd.relativePath = RelativePath(true, *cd.relativePath.segments.drop(1).toTypedArray())
                }
                f.includeEmptyDirs = false
            }
            it.into(kDiffLocation)
        }
    }

    companion object {
        const val KDIFF_GROUP = "kdiff"
    }
}
