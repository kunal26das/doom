// Screen wipe algorithms from linuxdoom-1.10 f_wipe.c.
// Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
@file:Suppress("UNUSED_PARAMETER")

package doom.engine.rendering

/** Owns one transition; randomness is limited to the presentation stream. */
internal class ScreenWipe(
    private val frameBuffers: FrameBuffers,
    private val nextPresentationRandom: () -> Int,
) {
    private var active = false
    private var startPixels = ByteArray(0)
    private var endPixels = ByteArray(0)
    private var outputPixels = ByteArray(0)
    private var y = IntArray(0)
    private val wipes = arrayOf<(Int, Int, Int) -> Int>(
        ::initializeColorTransform, ::advanceColorTransform, ::finishColorTransform,
        ::initializeMelt, ::advanceMelt, ::finishMelt,
    )

    // Emulate the C "(short*)screen" cast: short i covers screen bytes 2i/2i+1
    // (little-endian pixel pairs, exactly the DOS/x86 layout).
    private fun readPixelPair(b: ByteArray, i: Int): Short =
        ((b[2 * i].toInt() and 0xff) or ((b[2 * i + 1].toInt() and 0xff) shl 8)).toShort()

    private fun writePixelPair(b: ByteArray, i: Int, v: Short) {
        b[2 * i] = v.toByte()
        b[2 * i + 1] = (v.toInt() shr 8).toByte()
    }

    private fun transposePixelPairs(array: ByteArray, width: Int, height: Int) {
        val dest = ShortArray(width * height)

        for (y in 0 until height)
            for (x in 0 until width)
                dest[x * height + y] = readPixelPair(array, y * width + x)

        // memcpy(array, dest, width*height*2)
        for (i in 0 until width * height)
            writePixelPair(array, i, dest[i])
    }

    private fun initializeColorTransform(width: Int, height: Int, ticks: Int): Int {
        startPixels.copyInto(outputPixels, 0, 0, width * height)
        return 0
    }

    private fun advanceColorTransform(width: Int, height: Int, ticks: Int): Int {
        var changed = false
        var w = 0 // index into outputPixels
        var e = 0 // index into endPixels

        while (w != width * height) {
            val wv = outputPixels[w].toInt() and 0xff // *w (byte is unsigned char)
            val ev = endPixels[e].toInt() and 0xff // *e
            if (wv != ev) {
                if (wv > ev) {
                    val newval = wv - ticks
                    if (newval < ev)
                        outputPixels[w] = endPixels[e]
                    else
                        outputPixels[w] = newval.toByte()
                    changed = true
                } else if (wv < ev) {
                    val newval = wv + ticks
                    if (newval > ev)
                        outputPixels[w] = endPixels[e]
                    else
                        outputPixels[w] = newval.toByte()
                    changed = true
                }
            }
            w++
            e++
        }

        return if (!changed) 1 else 0
    }

    private fun finishColorTransform(width: Int, height: Int, ticks: Int): Int {
        return 0
    }

    private fun initializeMelt(width: Int, height: Int, ticks: Int): Int {
        // copy start screen to main screen
        startPixels.copyInto(outputPixels, 0, 0, width * height)

        // makes this wipe faster (in theory)
        // to have stuff in column-major format
        transposePixelPairs(startPixels, width / 2, height)
        transposePixelPairs(endPixels, width / 2, height)

        // setup initial column positions
        // (y<0 => not ready to scroll yet)
        y = IntArray(width)
        y[0] = -(nextPresentationRandom() % 16)
        for (i in 1 until width) {
            val r = (nextPresentationRandom() % 3) - 1
            y[i] = y[i - 1] + r
            if (y[i] > 0) y[i] = 0
            else if (y[i] == -16) y[i] = -15
        }

        return 0
    }

    private fun advanceMelt(width0: Int, height: Int, ticks0: Int): Int {
        var width = width0
        var ticks = ticks0
        var dy: Int
        var idx: Int
        var s: Int // index into (short*)endPixels / startPixels
        var d: Int // index into (short*)outputPixels
        var done = true

        width /= 2

        while (ticks-- != 0) {
            for (i in 0 until width) {
                if (y[i] < 0) {
                    y[i]++
                    done = false
                } else if (y[i] < height) {
                    dy = if (y[i] < 16) y[i] + 1 else 8
                    if (y[i] + dy >= height) dy = height - y[i]
                    s = i * height + y[i] // &((short *)endPixels)[i*height+y[i]]
                    d = y[i] * width + i // &((short *)outputPixels)[y[i]*width+i]
                    idx = 0
                    var j = dy
                    while (j != 0) {
                        writePixelPair(outputPixels, d + idx, readPixelPair(endPixels, s)) // d[idx] = *(s++)
                        s++
                        idx += width
                        j--
                    }
                    y[i] += dy
                    s = i * height // &((short *)startPixels)[i*height]
                    d = y[i] * width + i // &((short *)outputPixels)[y[i]*width+i]
                    idx = 0
                    j = height - y[i]
                    while (j != 0) {
                        writePixelPair(outputPixels, d + idx, readPixelPair(startPixels, s)) // d[idx] = *(s++)
                        s++
                        idx += width
                        j--
                    }
                    done = false
                }
            }
        }

        return if (done) 1 else 0
    }

    private fun finishMelt(width: Int, height: Int, ticks: Int): Int {
        // Z_Free(y) -- Kotlin GC
        return 0
    }

    fun captureStart(x: Int, y: Int, width: Int, height: Int): Int {
        startPixels = frameBuffers[2]
        frameBuffers.readScreen(startPixels)
        return 0
    }

    fun captureEnd(x: Int, y: Int, width: Int, height: Int): Int {
        endPixels = frameBuffers[3]
        frameBuffers.readScreen(endPixels)
        frameBuffers.drawBlock(x, y, 0, width, height, startPixels) // restore start scr.
        return 0
    }

    fun advance(wipeno: Int, x: Int, y: Int, width: Int, height: Int, ticks: Int): Boolean {
        val rc: Int

        // initial stuff
        if (!active) {
            active = true
            // outputPixels = (byte *) Z_Malloc(width*height, PU_STATIC, 0); // DEBUG
            outputPixels = frameBuffers[0]
            wipes[wipeno * 3](width, height, ticks)
        }

        // do a piece of wipe-in
        frameBuffers.markRect(0, 0, width, height)
        rc = wipes[wipeno * 3 + 1](width, height, ticks)
        //  frameBuffers.drawBlock(x, y, 0, width, height, outputPixels); // DEBUG

        // final stuff
        if (rc != 0) {
            active = false
            wipes[wipeno * 3 + 2](width, height, ticks)
        }

        return !active
    }

}
