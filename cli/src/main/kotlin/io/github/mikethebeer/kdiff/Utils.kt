package io.github.mikethebeer.kdiff

import java.nio.file.Path
import kotlin.io.path.div

fun remoteRepoKustomizationPath(clonedRepoPath: Path, remoteDirRelative: String?, kustomizationFile: Path): Path {
    val remoteRepoExecDir = if (!remoteDirRelative.isNullOrEmpty()) {
        clonedRepoPath / remoteDirRelative
    } else {
        clonedRepoPath
    }
    return remoteRepoExecDir / kustomizationFile
}