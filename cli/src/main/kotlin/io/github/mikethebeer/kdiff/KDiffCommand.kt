package io.github.mikethebeer.kdiff

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.syari.kgit.KGit
import com.jcraft.jsch.Session
import org.eclipse.jgit.lib.TextProgressMonitor
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.pathString

class KDiffCommand : CliktCommand(name = "kdiff") {

    private val appPath: String by argument("path").help("The path to the Kustomization file")
    private val remoteBranch: String by option("-b", "--branch").help("Remote branch to diff against").default("master")
    private val kustomizeExec: File by option("--kustomize").file(mustExist = true)
        .help("Path to the kustomize executable")
        .default(File("kustomize"))

    private val tempRootPath = Path("/tmp/kdiff")

    override fun run() {
        val thisGit = KGit.open(File("."))
        val thisGitPath = thisGit.repository.directory.toPath().parent

        val remoteGitPath = tempRootPath / Path(gitOriginUrl)
        val remoteGit = ensureGitRepoCheckoutDirExists(remoteGitPath)
        remoteGit.checkout {
            setName(remoteBranch)
            setStartPoint("origin/$remoteBranch")
            setProgressMonitor(TextProgressMonitor())
            setForced(true)
        }

        // run kustomize on the cloned remote branch
        // suppress error output because we don't care about the exit code
        val branch1Output = execCmd(kustomizeExec.path, "build", (remoteGitPath / appPath).pathString) { "" }

        // run kustomize on the local branch
        // suppress error output because we don't care about the exit code
        val branch2Output = execCmd(kustomizeExec.path, "build", (thisGitPath / appPath).pathString) { "" }

        val diffs = findTextDifferences(branch1Output, branch2Output)
        if (diffs.deltas.isNotEmpty()) {
            val inlineDiff = generateInlineDiff(branch1Output, diffs)
            echo(inlineDiff)
        } else {
            echo("No differences found.")
        }
    }

    private fun ensureGitRepoCheckoutDirExists(repoPath: Path): KGit {
        if (repoPath.exists()) {
            return KGit.open(repoPath.toFile())
        }

        Files.createDirectories(repoPath)
        return KGit.cloneRepository {
            setURI(gitOriginUrl)
            setTimeout(60)
            setProgressMonitor(TextProgressMonitor())
            setDirectory(repoPath.toFile())
//            setCredentialsProvider(UsernamePasswordCredentialsProvider("user", "password"))
            setTransportConfigCallback { transport ->
                val sshTransport = transport as? SshTransport
                sshTransport?.sshSessionFactory =
                    object : JschConfigSessionFactory() {
                        override fun configure(host: OpenSshConfig.Host?, session: Session?) {
                            // do nothing
                        }
                    }
            }

        }
    }

    private val gitOriginUrl: String by lazy {
        // get the current directory as File
        val repoPath = File(".")

        val git = KGit.open(repoPath)
        git.repository.config.getString("remote", "origin", "url")
    }
}
