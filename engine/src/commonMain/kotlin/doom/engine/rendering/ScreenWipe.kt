
package doom.engine.rendering

internal class ScreenWipe(
    private val frameBuffers: FrameBuffers,
    private val nextPresentationRandom: () -> Int,
) {
    private var active = false
    private var startPixels = ByteArray(0)
    private var endPixels = ByteArray(0)
    private var outputPixels = ByteArray(0)
    private var y = IntArray(0)
    private val width = frameBuffers.width
    private val height = frameBuffers.height
    private val initializers = arrayOf<() -> Unit>(
        ::initializeColorTransform, ::initializeMelt,
    )
    private val advances = arrayOf<(Int) -> Int>(
        ::advanceColorTransform, ::advanceMelt,
    )

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

        for (i in 0 until width * height)
            writePixelPair(array, i, dest[i])
    }

    private fun initializeColorTransform() {
        startPixels.copyInto(outputPixels, 0, 0, width * height)
    }

    private fun advanceColorTransform(ticks: Int): Int {
        var changed = false
        var w = 0
        var e = 0

        while (w != width * height) {
            val wv = outputPixels[w].toInt() and 0xff
            val ev = endPixels[e].toInt() and 0xff
            if (wv != ev) {
                if (wv > ev) {
                    outputPixels[w] = (wv - minOf(ticks, wv - ev)).toByte()
                    changed = true
                } else if (wv < ev) {
                    outputPixels[w] = (wv + minOf(ticks, ev - wv)).toByte()
                    changed = true
                }
            }
            w++
            e++
        }

        return if (!changed) 1 else 0
    }

    private fun initializeMelt() {
        startPixels.copyInto(outputPixels, 0, 0, width * height)

        transposePixelPairs(startPixels, width / 2, height)
        transposePixelPairs(endPixels, width / 2, height)

        y = IntArray(width)
        y[0] = -(nextPresentationRandom() % 16)
        for (i in 1 until width) {
            val r = (nextPresentationRandom() % 3) - 1
            y[i] = y[i - 1] + r
            if (y[i] > 0) y[i] = 0
            else if (y[i] == -16) y[i] = -15
        }
    }

    private fun advanceMelt(elapsedTicks: Int): Int {
        val pairWidth = width / 2
        var ticks = elapsedTicks
        var dy: Int
        var idx: Int
        var s: Int
        var d: Int
        var done = true

        while (ticks-- > 0) {
            var advancedColumn = false
            for (i in 0 until pairWidth) {
                if (y[i] < 0) {
                    y[i]++
                    done = false
                    advancedColumn = true
                } else if (y[i] < height) {
                    dy = if (y[i] < 16) y[i] + 1 else 8
                    if (y[i] + dy >= height) dy = height - y[i]
                    s = i * height + y[i]
                    d = y[i] * pairWidth + i
                    idx = 0
                    var j = dy
                    while (j != 0) {
                        writePixelPair(outputPixels, d + idx, readPixelPair(endPixels, s))
                        s++
                        idx += pairWidth
                        j--
                    }
                    y[i] += dy
                    s = i * height
                    d = y[i] * pairWidth + i
                    idx = 0
                    j = height - y[i]
                    while (j != 0) {
                        writePixelPair(outputPixels, d + idx, readPixelPair(startPixels, s))
                        s++
                        idx += pairWidth
                        j--
                    }
                    done = false
                    advancedColumn = true
                }
            }
            if (!advancedColumn) break
        }

        return if (done) 1 else 0
    }

    fun captureStart() {
        startPixels = frameBuffers[2]
        frameBuffers.readScreen(startPixels)
    }

    fun captureEnd() {
        endPixels = frameBuffers[3]
        frameBuffers.readScreen(endPixels)
        frameBuffers.drawBlock(0, 0, 0, width, height, startPixels)
    }

    fun advance(wipeno: Int, ticks: Int): Boolean {
        if (ticks <= 0) return false
        val rc: Int

        if (!active) {
            active = true
            outputPixels = frameBuffers[0]
            initializers[wipeno]()
        }

        frameBuffers.markRect(0, 0, width, height)
        rc = advances[wipeno](ticks)

        if (rc != 0) {
            active = false
        }

        return !active
    }

}
