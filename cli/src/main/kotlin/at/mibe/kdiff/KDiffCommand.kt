package at.mibe.kdiff

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString

class KDiffCommand : CliktCommand(name = "kdiff") {

    private val kpath: Path by argument("path").path(mustExist = true).help("The path to the Kustomization file")
    private val remoteBranch: String by option("-b", "--branch").help("Remote branch to diff against").default("master")
    private val remoteDirOverride: String by option("-r", "--remote-dir").help("Root directory of the remote branch to diff against")
        .default("")
    private val kustomize: File by option("--kustomize").file(mustExist = true).help("Path to the kustomize executable")
        .default(File("kustomize"))

    override fun run() {
        // clone the git repo
        val remoteRepoDir = cloneGitRepo(gitOriginUrl(), Path("/tmp/kdiff"))
        checkoutBranch(remoteRepoDir, remoteBranch)

        val remoteRepoExecDir = if (remoteDirOverride.isNotEmpty()) {
            remoteRepoDir / remoteDirOverride
        } else {
            remoteRepoDir
        }

        // run kustomize on the cloned remote branch
        // suppress error output because we don't care about the exit code
        val branch1Output = execCmd(kustomize.path, "build", (remoteRepoExecDir / kpath).pathString) { "" }

        // run kustomize on the local branch
        // suppress error output because we don't care about the exit code
        val branch2Output = execCmd(kustomize.path, "build", kpath.pathString) { "" }

        val diffs = findTextDifferences(branch1Output, branch2Output)
        if (diffs.deltas.isNotEmpty()) {
            val inlineDiff = generateInlineDiff(branch1Output, diffs)
            println(inlineDiff)
        } else {
            println("No differences found.")
        }
    }

    private fun cloneGitRepo(originUrl: String, dest: Path): Path {
        // Store target directory into a variable to avoid project reference in the configuration cache
        val repoPath = dest / Path(originUrl)
        Files.createDirectories(repoPath)

        execCmd("git", "clone", originUrl, ".", dir = repoPath.toFile()) { "Git clone failed" }
        return repoPath
    }

    private fun checkoutBranch(repoPath: Path, branch: String) {
        execCmd("git", "checkout", branch, dir = repoPath.toFile())
        execCmd("git", "pull", "origin", branch, dir = repoPath.toFile())
    }

    private fun gitOriginUrl(): String = execCmd("git", "remote", "get-url", "origin")

    private fun execCmd(
        vararg args: String,
        dir: File? = null,
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
            return if (proc.exitValue() == 0) {
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
