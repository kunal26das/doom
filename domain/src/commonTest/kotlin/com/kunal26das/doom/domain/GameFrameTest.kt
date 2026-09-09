package com.kunal26das.doom.domain

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameFrameTest {
    @Test
    fun frameOwnsItsPixelsWhenTheEngineReusesItsBuffer() {
        val enginePixels = intArrayOf(1, 2)
        val frame = GameFrame(2, 1, enginePixels)

        enginePixels.fill(0)

        assertEquals(2, frame.width)
        assertEquals(1, frame.height)
        assertContentEquals(intArrayOf(1, 2), frame.pixels)
    }

    @Test
    fun invalidDimensionsAndOverflowAreRejected() {
        assertFailsWith<IllegalArgumentException> { GameFrame(0, 1, intArrayOf()) }
        assertFailsWith<IllegalArgumentException> { GameFrame(1, -1, intArrayOf()) }
        assertFailsWith<IllegalArgumentException> { GameFrame(2, 2, intArrayOf(1)) }
        assertFailsWith<IllegalArgumentException> { GameFrame(Int.MAX_VALUE, 2, intArrayOf()) }
    }
}
