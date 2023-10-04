package at.mibe.gradle.kdiff

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.div

abstract class KDiffTask @Inject constructor(
    private val executable: String,
    private val diffBranch: String,
    private val remoteDirOverride: String
) :
    DefaultTask() {

    @TaskAction
    fun kDiff() {
        val kpath = project.properties["kpath"] as? String ?: "."
        val userDir = File(System.getProperty("user.dir"))

        // clone the git repo
        val remoteRepoDir = cloneGitRepo(gitOriginUrl())
        checkoutBranch(remoteRepoDir, diffBranch)

        val remoteRepoExecDir = if (remoteDirOverride.isNotEmpty()) {
            File(remoteRepoDir, remoteDirOverride)
        } else {
            remoteRepoDir
        }

        val branch1Output = execCmd(remoteRepoExecDir, executable, "build", kpath) { "" }
        val branch2Output = execCmd(userDir, executable, "build", kpath) { "" }

        val diffs = findTextDifferences(branch1Output, branch2Output)
        val inlineDiff = generateInlineDiff(branch1Output, diffs, 5)

        println(inlineDiff)
    }

    private fun cloneGitRepo(originUrl: String): File {
        // Store target directory into a variable to avoid project reference in the configuration cache
        val dir = project.layout.buildDirectory.asFile.get()

        val repoPath = dir.toPath() / Path(originUrl)
        Files.createDirectories(repoPath)

        execCmd(repoPath.toFile(), "git", "clone", originUrl, ".") { "Git clone failed" }
        return repoPath.toFile()
    }

    private fun checkoutBranch(repoPath: File, branch: String) {
        execCmd(repoPath, "git", "checkout", branch)
        execCmd(repoPath, "git", "pull", "origin", branch)
    }

    private fun gitOriginUrl(): String = execCmd(project.rootDir, "git", "remote", "get-url", "origin")

    private fun execCmd(
        dir: File,
        vararg args: String,
        ignoreExit: Boolean = false,
        onError: ((String) -> String)? = null
    ): String {
        val cmd = args.toList()
        val stdoutFile = kotlin.io.path.createTempFile().toFile()
        val stderrFile = kotlin.io.path.createTempFile().toFile()

        try {
            val proc = ProcessBuilder(cmd)
                .directory(dir)
                .redirectOutput(stdoutFile)
                .redirectError(stderrFile)
                .start()
            proc.waitFor(10, TimeUnit.SECONDS)
            val stdout = stdoutFile.readText().trim()
            val stderr = stderrFile.readText().trim()
            return if (proc.exitValue() == 0 || ignoreExit) {
                stdout
            } else {
                onError?.invoke(stderr) ?: throw RuntimeException(
                    "command failed: ${cmd.joinToString(" ")}\n$stderr"
                )
            }
        } finally {
            stderrFile.delete()
            stdoutFile.delete()
        }
    }
}
