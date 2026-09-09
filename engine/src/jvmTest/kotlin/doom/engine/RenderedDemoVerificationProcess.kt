package doom.engine

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Renders a complete recorded demo through the public engine API in a fresh JVM. */
object RenderedDemoVerificationProcess {
    @JvmStatic
    fun main(args: Array<String>) {
        val wad = File(args[0]).readBytes()
        val demo = args[1]
        val mode = args[2]
        require(mode == "single" || mode == "timed") { "Unknown stepping mode: $mode" }
        var clock = 0
        var frames = 0
        lateinit var engine: DoomEngine
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = ByteBuffer.allocate(4 + SCREENWIDTH * SCREENHEIGHT * 4).order(ByteOrder.BIG_ENDIAN)
        val video = DoomVideo { pixels ->
            frames++
            val tic = engine.metrics.gametic
            if (tic % 35 == 0 || frames <= 5) {
                bytes.clear()
                bytes.putInt(tic)
                for (pixel in pixels) bytes.putInt(pixel)
                digest.update(bytes.array())
            }
        }
        engine = DoomEngine(DoomHost(clock = DoomClock { clock }, video = video))
        try {
            engine.boot(listOf(wad), listOf("-playdemo", demo))
            while (!engine.quitRequested && clock < 20_000) {
                clock++
                if (mode == "single") engine.stepSingleTic() else engine.step()
            }
            check(engine.quitRequested) { "$mode $demo did not reach its end marker after $clock clock tics" }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            println("RENDER_RESULT $mode $demo ${engine.metrics.gametic} $frames $hash")
        } finally {
            engine.close()
        }
    }
}
