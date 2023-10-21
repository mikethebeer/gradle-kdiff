package io.github.mikethebeer.kdiff

import java.io.File
import java.util.concurrent.TimeUnit

fun execCmd(
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
