// Framebuffer and patch algorithms from linuxdoom-1.10 v_video.c.
// Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
package doom.engine.rendering

import doom.engine.DoomError
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.u8

/** Owns indexed screen storage and the operations that mutate it. */
internal class FrameBuffers(
    val width: Int = SCREENWIDTH,
    val height: Int = SCREENHEIGHT,
) {
    private val buffers = Array(5) { ByteArray(width * height) }
    // Preserve vanilla's zero-initialized dirty box and its integer update order.
    private val dirtyBounds = IntArray(4)

    /** A borrowed screen for the legacy rasterizer; callers cannot replace screen storage. */
    operator fun get(index: Int): ByteArray = buffers[index]

    fun dirtyRectangle(): IntArray = dirtyBounds.copyOf()

    fun markRect(x: Int, y: Int, width: Int, height: Int) {
        addDirtyPoint(x, y)
        addDirtyPoint(x + width - 1, y + height - 1)
    }

    private fun addDirtyPoint(x: Int, y: Int) {
        if (x < dirtyBounds[2]) dirtyBounds[2] = x
        else if (x > dirtyBounds[3]) dirtyBounds[3] = x
        if (y < dirtyBounds[1]) dirtyBounds[1] = y
        else if (y > dirtyBounds[0]) dirtyBounds[0] = y
    }

    private fun contains(x: Int, y: Int, screen: Int, width: Int, height: Int): Boolean =
        x >= 0 && y >= 0 && width >= 0 && height >= 0 &&
            width <= this.width && height <= this.height &&
            x <= this.width - width && y <= this.height - height && screen in buffers.indices

    fun copyRect(
        srcx: Int, srcy: Int, srcScreen: Int, width: Int, height: Int,
        destx: Int, desty: Int, destScreen: Int,
    ) {
        if (!contains(srcx, srcy, srcScreen, width, height) ||
            !contains(destx, desty, destScreen, width, height)
        ) throw DoomError("Bad V_CopyRect")
        markRect(destx, desty, width, height)
        val source = buffers[srcScreen]
        val destination = buffers[destScreen]
        var sourceOffset = this.width * srcy + srcx
        var destinationOffset = this.width * desty + destx
        repeat(height) {
            source.copyInto(destination, destinationOffset, sourceOffset, sourceOffset + width)
            sourceOffset += this.width
            destinationOffset += this.width
        }
    }

    /** A masked patch keeps transparent gaps between its column posts untouched. */
    fun drawPatch(x0: Int, y0: Int, screen: Int, patch: ByteArray) {
        val x = x0 - PatchFormat.leftOffset(patch)
        val y = y0 - PatchFormat.topOffset(patch)
        val patchWidth = PatchFormat.width(patch)
        val patchHeight = PatchFormat.height(patch)
        if (!contains(x, y, screen, patchWidth, patchHeight)) {
            println("Patch at $x,$y exceeds LFB")
            println("V_DrawPatch: bad patch (ignored)")
            return
        }
        if (screen == 0) markRect(x, y, patchWidth, patchHeight)
        val destination = buffers[screen]
        val destinationTop = y * width + x
        for (column in 0 until patchWidth) {
            var offset = PatchFormat.columnOffset(patch, column)
            while (patch.u8(offset) != 0xff) {
                val topDelta = patch.u8(offset)
                val count = patch.u8(offset + 1)
                var source = offset + 3
                var destinationOffset = destinationTop + column + topDelta * width
                repeat(count) {
                    destination[destinationOffset] = patch[source++]
                    destinationOffset += width
                }
                offset += 4 + count
            }
        }
    }

    fun drawBlock(x: Int, y: Int, screen: Int, width: Int, height: Int, source: ByteArray) {
        if (!contains(x, y, screen, width, height)) throw DoomError("Bad V_DrawBlock")
        markRect(x, y, width, height)
        val destination = buffers[screen]
        var sourceOffset = 0
        var destinationOffset = y * this.width + x
        repeat(height) {
            source.copyInto(destination, destinationOffset, sourceOffset, sourceOffset + width)
            sourceOffset += width
            destinationOffset += this.width
        }
    }

    fun getBlock(x: Int, y: Int, screen: Int, width: Int, height: Int, destination: ByteArray) {
        if (!contains(x, y, screen, width, height)) throw DoomError("Bad V_GetBlock")
        val source = buffers[screen]
        var sourceOffset = y * this.width + x
        var destinationOffset = 0
        repeat(height) {
            source.copyInto(destination, destinationOffset, sourceOffset, sourceOffset + width)
            sourceOffset += this.width
            destinationOffset += width
        }
    }

    fun readScreen(destination: ByteArray) { buffers[0].copyInto(destination) }
}
