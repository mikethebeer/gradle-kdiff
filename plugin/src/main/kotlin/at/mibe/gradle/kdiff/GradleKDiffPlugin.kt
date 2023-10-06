package at.mibe.gradle.kdiff

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

@Suppress("unused")
class GradleKDiffPlugin : Plugin<Project> {

    companion object {
        const val KDIFF_GROUP = "kdiff"
    }

    override fun apply(project: Project) {
        val kDiffLocation = project.layout.buildDirectory.dir("kdiff").get()

        // Apply the third-party plugin
        project.apply { action ->
            action.plugin("de.undercouch.download")
        }

        project.tasks.register("kDiffVersion") {
            it.group = KDIFF_GROUP
            it.description = "Prints the kDiff version"

            it.doLast {
                println("GradleKDiffPlugin version: ${project.version}")
            }
        }

        val downloadKustomize = project.tasks.register("downloadKustomize", Download::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Download kustomize CLI"

            it.src("https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh")
            it.dest(kDiffLocation.dir("bin"))
            it.overwrite(false)
        }

        project.tasks.register("installKustomize", Exec::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Install kustomize CLI"

            it.onlyIf { !kDiffLocation.asFile.resolve("bin/kustomize").exists() }
            it.dependsOn(downloadKustomize)

            it.workingDir(kDiffLocation.dir("bin"))
            // only install kustomize if it is not already installed
            it.commandLine("bash", "install_kustomize.sh")
        }

        val downloadKDiff = project.tasks.register("downloadKDiff", Download::class.java) {
            it.group = KDIFF_GROUP
            it.description = "Download kDiff CLI"

            it.src("https://github.com/mikethebeer/gradle-kdiff/releases/download/${project.version}/kdiff-${project.version}.zip")
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
}
