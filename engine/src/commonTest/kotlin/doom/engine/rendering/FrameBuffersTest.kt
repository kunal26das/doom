package doom.engine.rendering

import doom.engine.DoomError

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrameBuffersTest {
    @Test
    fun blockDrawingAndReadingRespectScreenStrideAndLeaveSurroundingPixelsAlone() {
        val buffers = FrameBuffers(width = 6, height = 4)
        buffers[0].fill(9)
        buffers.drawBlock(2, 1, 0, 3, 2, byteArrayOf(1, 2, 3, 4, 5, 6))
        assertContentEquals(
            byteArrayOf(9, 9, 9, 9, 9, 9, 9, 9, 1, 2, 3, 9, 9, 9, 4, 5, 6, 9, 9, 9, 9, 9, 9, 9),
            buffers[0],
        )
        val result = ByteArray(4)
        buffers.getBlock(3, 1, 0, 2, 2, result)
        assertContentEquals(byteArrayOf(2, 3, 5, 6), result)
        val screen = ByteArray(24)
        buffers.readScreen(screen)
        assertContentEquals(buffers[0], screen)
    }

    @Test
    fun copyingBetweenScreensUsesBothOffsetsAndPreservesTheSource() {
        val buffers = FrameBuffers(width = 6, height = 4)
        val source = ByteArray(24) { it.toByte() }
        source.copyInto(buffers[1])
        buffers.copyRect(1, 0, 1, 2, 3, 3, 1, 0)
        val result = ByteArray(6)
        buffers.getBlock(3, 1, 0, 2, 3, result)
        assertContentEquals(byteArrayOf(1, 2, 7, 8, 13, 14), result)
        assertContentEquals(source, buffers[1])
        assertEquals(0, buffers[0][8].toInt())
    }

    @Test
    fun overlappingCopiesPreserveVanillasRowByRowOrder() {
        val buffers = FrameBuffers(width = 4, height = 3)
        byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).copyInto(buffers[0])
        buffers.copyRect(0, 0, 0, 4, 2, 0, 1, 0)
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4), buffers[0])
    }

    @Test
    fun patchesApplyOriginOffsetsAndTransparentColumnPosts() {
        val buffers = FrameBuffers(width = 6, height = 6)
        buffers[0].fill(7)
        buffers.drawPatch(2, 3, 0, twoColumnPatch())
        val expected = ByteArray(36) { 7 }
        expected[1 * 6 + 1] = -128
        expected[2 * 6 + 2] = 30
        expected[3 * 6 + 1] = -1
        expected[3 * 6 + 2] = 40
        expected[4 * 6 + 1] = 20
        assertContentEquals(expected, buffers[0])
        assertContentEquals(intArrayOf(4, 0, 0, 2), buffers.dirtyRectangle())
    }

    @Test
    fun offscreenPatchesDoNotMarkTheVisibleScreenAndClippedPatchesAreIgnored() {
        val buffers = FrameBuffers(width = 6, height = 6)
        buffers.drawPatch(2, 3, 1, twoColumnPatch())
        assertContentEquals(IntArray(4), buffers.dirtyRectangle())
        val before = buffers[1].copyOf()
        buffers.drawPatch(0, 0, 1, twoColumnPatch())
        assertContentEquals(before, buffers[1])
        assertContentEquals(ByteArray(36), buffers[0])
    }

    @Test
    fun buffersAndDirtyBoundsCannotBeChangedThroughAnotherInstancesState() {
        val first = FrameBuffers(width = 6, height = 4)
        val second = FrameBuffers(width = 6, height = 4)
        first.drawBlock(2, 1, 0, 2, 2, byteArrayOf(1, 2, 3, 4))
        val dirty = first.dirtyRectangle()
        dirty.fill(999)
        second[0].fill(44)
        assertContentEquals(intArrayOf(2, 0, 0, 3), first.dirtyRectangle())
        assertEquals(1, first[0][8].toInt())
    }

    @Test
    fun invalidRectanglesFailBeforeChangingPixelsOrDirtyBounds() {
        val buffers = FrameBuffers(width = 6, height = 4)
        assertFailsWith<DoomError> { buffers.drawBlock(5, 0, 0, 2, 1, byteArrayOf(1, 2)) }
        assertFailsWith<DoomError> { buffers.getBlock(0, 0, -1, 1, 1, ByteArray(1)) }
        assertFailsWith<DoomError> { buffers.copyRect(0, 0, 0, 2, 2, 5, 3, 0) }
        assertContentEquals(ByteArray(24), buffers[0])
        assertContentEquals(IntArray(4), buffers.dirtyRectangle())
    }

    private fun twoColumnPatch(): ByteArray = byteArrayOf(
        2, 0, 4, 0, 1, 0, 2, 0,
        16, 0, 0, 0, 28, 0, 0, 0,
        0, 1, 0, -128, 0,
        2, 2, 0, -1, 20, 0, -1,
        1, 2, 0, 30, 40, 0, -1,
    )
}
