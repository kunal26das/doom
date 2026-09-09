package com.kunal26das.doom

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmFilePersistenceTest {
    @Test
    fun savedBytesSurviveProcessExitAndPlatformStartupInANewProcess() {
        val home = Files.createTempDirectory("doom-persistence-test-").toFile()
        try {
            verifyProcess(home, "write")
            verifyProcess(home, "read")
        } finally {
            home.deleteRecursively()
        }
    }

    private fun verifyProcess(home: File, mode: String) {
        val executable = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val java = File(System.getProperty("java.home"), "bin/$executable")
        val classpath = checkNotNull(System.getProperty("doom.test.classpath"))
        val output = File(home, "$mode.log")
        val process = ProcessBuilder(
            java.absolutePath,
            "-Duser.home=${home.absolutePath}",
            "-cp", classpath,
            JvmFilePersistenceProcess::class.java.name,
            mode,
        ).redirectErrorStream(true).redirectOutput(output).start()
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "$mode storage verification exceeded 30 seconds")
            assertEquals(0, process.exitValue(), output.readText())
        } finally {
            if (process.isAlive) {
                process.destroyForcibly()
                process.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
}
