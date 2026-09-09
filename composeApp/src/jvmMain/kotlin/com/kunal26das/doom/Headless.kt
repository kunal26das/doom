package com.kunal26das.doom

import doom.engine.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val options = HeadlessOptions.parse(args.toList())
    val wadFile = options.wad ?: sequenceOf(
        File("composeApp/src/commonMain/composeResources/files/doom1.wad"),
        File("src/commonMain/composeResources/files/doom1.wad"),
    ).firstOrNull { it.isFile }
        ?: error("Bundled WAD not found; pass --wad /path/to/doom1.wad")
    require(wadFile.isFile) { "WAD file does not exist: $wadFile" }
    val output = options.output?.also {
        require((it.isDirectory || it.mkdirs()) && it.canWrite()) { "Cannot write frames to $it" }
    }
    var clock = 0
    var captured = 0
    var nextCapture = 0
    val engine = DoomEngine(DoomHost(clock = DoomClock { clock }))
    val video = if (output == null) null else DoomVideo { pixels ->
        val tic = engine.metrics.gametic
        if (tic >= nextCapture) {
            val image = BufferedImage(SCREENWIDTH, SCREENHEIGHT, BufferedImage.TYPE_INT_RGB)
            image.setRGB(0, 0, SCREENWIDTH, SCREENHEIGHT, pixels, 0, SCREENWIDTH)
            check(ImageIO.write(image, "png", File(output, "tic%05d.png".format(tic))))
            captured++
            nextCapture = tic + TICRATE
        }
    }
    try {
        if (video != null) engine.resume(DoomHost(video = video))
        val engineArgs = options.demo?.let { listOf("-playdemo", it) } ?: emptyList()
        engine.boot(listOf(wadFile.readBytes()), engineArgs)
        while (engine.metrics.gametic < options.ticks && !engine.quitRequested) {
            clock++
            engine.stepSingleTic()
        }
        check(options.demo == null || engine.quitRequested) {
            "${options.demo} did not finish within ${options.ticks} game tics"
        }
        println("HEADLESS OK: gametic=${engine.metrics.gametic}, frames captured=$captured, demoFinished=${engine.quitRequested}")
    } finally {
        engine.close()
    }
}
