package doom.engine.regression

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledDemoRegressionTest {
    @Test
    fun demo1PreservesOriginalSimulationThroughEndMarker() = verify(
        "DEMO1", 1711, "9cd19bf3230c9a3ad59a819edb8d406d88956f613ca9415eda7e1a395a4c0dc2",
    )

    @Test
    fun demo2PreservesOriginalSimulationThroughEndMarker() = verify(
        "DEMO2", 2348, "c6dd6098e7a5cea5ba4c83cdb92d08f0b87ee31f257ea1012e04024b700d8790",
    )

    @Test
    fun demo3PreservesOriginalSimulationThroughEndMarker() = verify(
        "DEMO3", 3864, "854727b2cd9c6206c4ea6e2d2825bbfede71371f0be3241c940be65e8769f79b",
    )

    private fun verify(demo: String, tics: Int, hash: String) {
        val wad = requireNotNull(System.getProperty("doom.test.wad")) { "Set doom.test.wad to the bundled WAD" }
        val classpath = requireNotNull(System.getProperty("doom.test.classpath")) { "Set doom.test.classpath to the JVM test runtime" }
        assertTrue(File(wad).isFile, "Missing test WAD: $wad")
        val executable = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val java = File(System.getProperty("java.home"), "bin/$executable")
        val output = Files.createTempFile("doom-$demo-", ".log").toFile()
        val process = ProcessBuilder(
            java.absolutePath, "-cp", classpath, DemoVerificationProcess::class.java.name, wad, demo,
        ).redirectErrorStream(true).redirectOutput(output).start()
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "$demo exceeded 30 seconds")
            val text = output.readText()
            assertEquals(0, process.exitValue(), text)
            assertEquals("DEMO_RESULT $demo $tics $hash", text.lineSequence().firstOrNull { it.startsWith("DEMO_RESULT ") }, text)
        } finally {
            if (process.isAlive) process.destroyForcibly()
            output.delete()
        }
    }
}
