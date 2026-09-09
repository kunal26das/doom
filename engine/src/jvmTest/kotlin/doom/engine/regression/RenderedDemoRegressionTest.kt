package doom.engine.regression

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RenderedDemoRegressionTest {
    @Test
    fun demo1PreservesSingleTicRenderingThroughEndMarker() = verify(
        "single", "DEMO1", 1711, 1751,
        "bfd0123c976b62da87d1231969d008f059ded6834b12efce09533580597cf385",
    )

    @Test
    fun demo2PreservesSingleTicRenderingThroughEndMarker() = verify(
        "single", "DEMO2", 2348, 2388,
        "356220e38d51de222f5cf75b64bca7cb110d40747e6de5bc4bd34bfeff3e08b0",
    )

    @Test
    fun demo3PreservesSingleTicRenderingThroughEndMarker() = verify(
        "single", "DEMO3", 3864, 3904,
        "e121a103cfc54e43aa09fff211c43a494678363b587a37a8625935c6b6d25639",
    )

    @Test
    fun demo1PreservesTimedRenderingThroughEndMarker() = verify(
        "timed", "DEMO1", 1711, 1747,
        "8c9610d74fe33ebb2113cf71b4145bd71843f99149a2d04e2afa5a75001d73cb",
    )

    @Test
    fun demo2PreservesTimedRenderingThroughEndMarker() = verify(
        "timed", "DEMO2", 2348, 2384,
        "356220e38d51de222f5cf75b64bca7cb110d40747e6de5bc4bd34bfeff3e08b0",
    )

    @Test
    fun demo3PreservesTimedRenderingThroughEndMarker() = verify(
        "timed", "DEMO3", 3864, 3900,
        "e121a103cfc54e43aa09fff211c43a494678363b587a37a8625935c6b6d25639",
    )

    private fun verify(mode: String, demo: String, tics: Int, frames: Int, hash: String) {
        val wad = requireNotNull(System.getProperty("doom.test.wad")) { "Set doom.test.wad to the bundled WAD" }
        val classpath = requireNotNull(System.getProperty("doom.test.classpath")) { "Set doom.test.classpath to the JVM test runtime" }
        assertTrue(File(wad).isFile, "Missing test WAD: $wad")
        val executable = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val java = File(System.getProperty("java.home"), "bin/$executable")
        val output = Files.createTempFile("doom-render-$mode-$demo-", ".log").toFile()
        val process = ProcessBuilder(
            java.absolutePath, "-cp", classpath, RenderedDemoVerificationProcess::class.java.name, wad, demo, mode,
        ).redirectErrorStream(true).redirectOutput(output).start()
        try {
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "$mode $demo rendering exceeded 60 seconds")
            val text = output.readText()
            assertEquals(0, process.exitValue(), text)
            assertEquals(
                "RENDER_RESULT $mode $demo $tics $frames $hash",
                text.lineSequence().firstOrNull { it.startsWith("RENDER_RESULT ") },
                text,
            )
        } finally {
            if (process.isAlive) process.destroyForcibly()
            output.delete()
        }
    }
}
