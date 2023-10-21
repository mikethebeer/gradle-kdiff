package io.github.mikethebeer.kdiff

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.syari.kgit.KGit
import org.eclipse.jgit.lib.TextProgressMonitor
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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
        val repoPath = ensureGitRepoCheckoutDirExists()
        // clone the git repo
        val git = KGit.cloneRepository {
            setURI(gitOriginUrl)
            setTimeout(60)
            setProgressMonitor(TextProgressMonitor())
            setDirectory(repoPath.toFile())
        }
        git.checkout {
            setName(remoteBranch)
            setProgressMonitor(TextProgressMonitor())
            setForced(true)
        }


        val remoteRepoExecDir = if (remoteDirOverride.isNotEmpty()) {
            repoPath / remoteDirOverride
        } else {
            repoPath
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
            echo(inlineDiff)
        } else {
            echo("No differences found.")
        }
    }

    private fun ensureGitRepoCheckoutDirExists(): Path {
        val tempPath = Path("/tmp/kdiff")
        val repoPath = tempPath / Path(gitOriginUrl)
        Files.createDirectories(repoPath)
        return repoPath
    }

    private val gitOriginUrl: String by lazy {
        execCmd("git", "remote", "get-url", "origin")
    }
}
