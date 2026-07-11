package com.kunal26das.doom

import doom.engine.D_DoomMain
import doom.engine.D_DoomStep
import doom.engine.I_VideoSink
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.gametic
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Headless verification: boots the engine with the bundled shareware WAD and
 * runs the vanilla attract sequence (title -> DEMO1 -> credits -> DEMO2 ...)
 * in real time, dumping one PNG per game-second. If the demos play back
 * correctly the simulation is deterministic-faithful.
 *
 * Usage: -DmainClass=com.kunal26das.doom.HeadlessKt, args: [outDir] [gameSeconds]
 */
fun main(args: Array<String>) {
    val outDir = File(args.getOrNull(0) ?: "build/frames").apply { mkdirs() }
    val seconds = (args.getOrNull(1) ?: "80").toInt()

    val wadFile = sequenceOf(
        "composeApp/src/commonMain/composeResources/files/doom1.wad",
        "src/commonMain/composeResources/files/doom1.wad",
        "/Users/kunal/GitHub/doom/composeApp/src/commonMain/composeResources/files/doom1.wad",
    ).map(::File).first { it.exists() }
    val wad = wadFile.readBytes()

    var nextCapture = 0
    var captured = 0
    I_VideoSink = { px ->
        if (gametic >= nextCapture) {
            val img = BufferedImage(SCREENWIDTH, SCREENHEIGHT, BufferedImage.TYPE_INT_RGB)
            img.setRGB(0, 0, SCREENWIDTH, SCREENHEIGHT, px, 0, SCREENWIDTH)
            ImageIO.write(img, "png", File(outDir, "tic%05d.png".format(gametic)))
            captured++
            nextCapture = gametic + 35
        }
    }

    wireEnginePersistence()

    try {
        D_DoomMain(listOf(wad), emptyList())
        println("HEADLESS: engine booted, entering loop")
        val deadline = System.currentTimeMillis() + seconds * 1000L
        while (System.currentTimeMillis() < deadline) {
            D_DoomStep()
            Thread.sleep(4)
        }
        println("HEADLESS OK: gametic=$gametic, frames captured=$captured -> $outDir")
    } catch (e: Throwable) {
        println("HEADLESS CRASH at gametic=$gametic: $e")
        e.printStackTrace()
    }
}
