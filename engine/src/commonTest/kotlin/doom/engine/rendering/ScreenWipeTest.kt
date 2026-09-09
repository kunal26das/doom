package doom.engine.rendering

import doom.engine.wipe_ColorXForm
import doom.engine.wipe_Melt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenWipeTest {
    @Test
    fun colorTransformUsesUnsignedValuesClampsAtTheTargetAndCompletesOnTheFollowingTick() {
        val buffers = FrameBuffers(width = 4, height = 1)
        val wipe = ScreenWipe(buffers) { error("Color transforms must not consume randomness") }
        capture(wipe, buffers, byteArrayOf(0, -128, -1, 4), byteArrayOf(3, 125, -7, 4))

        assertFalse(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 2))
        assertContentEquals(byteArrayOf(2, 126, -3, 4), buffers[0])
        assertFalse(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 2))
        assertContentEquals(byteArrayOf(3, 125, -5, 4), buffers[0])
        assertFalse(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 2))
        assertContentEquals(byteArrayOf(3, 125, -7, 4), buffers[0])
        assertTrue(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 2))
    }

    @Test
    fun meltPreservesPixelPairsColumnDelaysAndVanillaCompletionTiming() {
        val buffers = FrameBuffers(width = 4, height = 4)
        var randomCalls = 0
        val wipe = ScreenWipe(buffers) { randomCalls++; 0 }
        capture(wipe, buffers, start, end)

        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertEquals(4, randomCalls, "Vanilla consumes a random value per pixel column, not per pair")
        assertContentEquals(
            byteArrayOf(101, 102, 3, 4, 1, 2, 13, 14, 11, 12, 23, 24, 21, 22, 33, 34), buffers[0],
        )
        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertContentEquals(
            byteArrayOf(101, 102, 103, 104, 111, 112, 3, 4, 121, 122, 13, 14, 1, 2, 23, 24), buffers[0],
        )
        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertContentEquals(
            byteArrayOf(101, 102, 103, 104, 111, 112, 113, 114, 121, 122, 123, 124, -125, -124, 3, 4), buffers[0],
        )
        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertContentEquals(end, buffers[0])
        assertTrue(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertEquals(4, randomCalls)
    }

    @Test
    fun meltClampsMinusSixteenColumnDelayToMinusFifteen() {
        val buffers = FrameBuffers(width = 4, height = 4)
        val values = intArrayOf(15, 0, 2, 1)
        var cursor = 0
        val wipe = ScreenWipe(buffers) { values[cursor++] }
        capture(wipe, buffers, start, end)
        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 15))
        assertContentEquals(start, buffers[0])
        assertFalse(wipe.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertContentEquals(
            byteArrayOf(101, 102, 103, 104, 1, 2, 3, 4, 11, 12, 13, 14, 21, 22, 23, 24), buffers[0],
        )
        assertEquals(4, cursor)
    }

    @Test
    fun meltAcceleratesForTheFirstSixteenRowsThenAdvancesEightRowsAtATime() {
        val buffers = FrameBuffers(width = 2, height = 40)
        val wipe = ScreenWipe(buffers) { 0 }
        val start = ByteArray(80) { it.toByte() }
        val end = ByteArray(80) { (128 + it).toByte() }
        capture(wipe, buffers, start, end)

        for (revealedRows in intArrayOf(1, 3, 7, 15, 31, 39, 40)) {
            assertFalse(wipe.advance(wipe_Melt, 0, 0, 2, 40, 1))
            val expected = end.copyOfRange(0, revealedRows * 2) + start.copyOfRange(0, (40 - revealedRows) * 2)
            assertContentEquals(expected, buffers[0])
        }
        assertTrue(wipe.advance(wipe_Melt, 0, 0, 2, 40, 1))
    }

    @Test
    fun batchedMeltTicksMatchIndividualTicksWithoutSharingAnotherTransitionsState() {
        val firstBuffers = FrameBuffers(width = 4, height = 4)
        val secondBuffers = FrameBuffers(width = 4, height = 4)
        val first = ScreenWipe(firstBuffers) { 0 }
        val second = ScreenWipe(secondBuffers) { 0 }
        capture(first, firstBuffers, start, end)
        capture(second, secondBuffers, start, end)
        repeat(4) { assertFalse(first.advance(wipe_Melt, 0, 0, 4, 4, 1)) }
        assertContentEquals(start, secondBuffers[0])
        assertFalse(second.advance(wipe_Melt, 0, 0, 4, 4, 4))
        assertContentEquals(firstBuffers[0], secondBuffers[0])
        assertTrue(first.advance(wipe_Melt, 0, 0, 4, 4, 1))
        assertTrue(second.advance(wipe_Melt, 0, 0, 4, 4, 1))
    }

    @Test
    fun aCompletedTransitionCanCaptureAndWipeTheNextPairOfScreens() {
        val buffers = FrameBuffers(width = 4, height = 1)
        val wipe = ScreenWipe(buffers) { 0 }
        capture(wipe, buffers, byteArrayOf(1, 2, 3, 4), byteArrayOf(4, 3, 2, 1))
        assertFalse(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 10))
        assertTrue(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 10))
        capture(wipe, buffers, byteArrayOf(8, 9, 10, 11), byteArrayOf(20, 21, 22, 23))
        assertFalse(wipe.advance(wipe_ColorXForm, 0, 0, 4, 1, 10))
        assertContentEquals(byteArrayOf(18, 19, 20, 21), buffers[0])
    }

    private fun capture(wipe: ScreenWipe, buffers: FrameBuffers, start: ByteArray, end: ByteArray) {
        start.copyInto(buffers[0])
        wipe.captureStart(0, 0, buffers.width, buffers.height)
        end.copyInto(buffers[0])
        wipe.captureEnd(0, 0, buffers.width, buffers.height)
        assertContentEquals(start, buffers[0], "Capturing the destination must restore the start screen")
    }

    private val start = byteArrayOf(1, 2, 3, 4, 11, 12, 13, 14, 21, 22, 23, 24, 31, 32, 33, 34)
    private val end = byteArrayOf(101, 102, 103, 104, 111, 112, 113, 114, 121, 122, 123, 124, -125, -124, -123, -122)
}
