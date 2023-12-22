package io.github.mikethebeer.kdiff

import kotlin.test.Test

class KDiffCommandTest {

    @Test
    fun `test kdiff command`() {
        val args = arrayOf("kdiff", "src/test/resources/one.txt", "src/test/resources/two.txt")
        val output = captureOutput {
            main(args)
        }
    }
}