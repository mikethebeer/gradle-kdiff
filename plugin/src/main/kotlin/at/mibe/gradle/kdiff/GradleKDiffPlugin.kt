package at.mibe.gradle.kdiff

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec

class GradleKDiffPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        // Apply the third-party plugin
        project.apply { action ->
            action.plugin("de.undercouch.download")
        }

        val diffBranch = project.properties["kdiff_branch"] as? String ?: "origin/master"

        project.tasks.register("kDiffVersion") {
            it.doLast {
                println("GradleKDiffPlugin version: ${project.version}")
            }
        }

        project.tasks.register("downloadKustomize", Download::class.java) {
            it.src("https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh")
            it.dest(project.layout.buildDirectory)
            it.overwrite(false)
        }
        project.tasks.register("installKustomize", Exec::class.java) {
            it.dependsOn("downloadKustomize")
            it.workingDir(project.layout.buildDirectory)
            it.commandLine("bash", "install_kustomize.sh")
        }

        val executable = project.layout.buildDirectory.file("kustomize").get().asFile.absolutePath ?: "kustomize"
        project.tasks.register("kDiff", KDiffTask::class.java, executable, diffBranch)
    }
}
