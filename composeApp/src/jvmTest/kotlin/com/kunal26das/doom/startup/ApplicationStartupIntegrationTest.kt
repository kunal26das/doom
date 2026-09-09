package com.kunal26das.doom.startup

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationStartupIntegrationTest {
    @Test
    fun repeatedInstallationCachesStorageWithoutOpeningFilesOrStartingAGame() {
        val home = Files.createTempDirectory("doom-startup-test-").toFile()
        val output = File(home, "result.log")
        val executable = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val java = File(System.getProperty("java.home"), "bin/$executable")
        val classpath = checkNotNull(System.getProperty("doom.test.classpath"))
        val process = ProcessBuilder(
            java.absolutePath,
            "-Duser.home=${home.absolutePath}",
            "-cp", classpath,
            ApplicationStartupVerificationProcess::class.java.name,
        ).redirectErrorStream(true).redirectOutput(output).start()
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Application startup exceeded 30 seconds")
            assertEquals(0, process.exitValue(), output.readText())
            assertFalse(File(home, ".doom-kmp").exists(), "Installing adapters must not access game storage")
        } finally {
            if (process.isAlive) {
                process.destroyForcibly()
                process.waitFor(5, TimeUnit.SECONDS)
            }
            home.deleteRecursively()
        }
    }
}
