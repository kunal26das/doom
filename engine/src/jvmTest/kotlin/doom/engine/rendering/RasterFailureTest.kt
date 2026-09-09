package doom.engine.rendering

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.rendering.resources.colormaps
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RasterFailureTest {
    @Test
    fun fuzzFailureKeepsCompletedPixelsAndPositionAtFailingPixel() {
        val engine = DoomEngineCore().apply {
            rInitBuffer(SCREENWIDTH, SCREENHEIGHT)
            colormaps = ByteArray(6 * 256 + 31) { (255 - it).toByte() }
            dcX = 2
            dcYl = 0
            dcYh = 4
            viewheight = 5
            repeat(5) { y -> screens[0][y * SCREENWIDTH + 2] = ((y + 1) * 10).toByte() }
        }

        assertFailsWith<IndexOutOfBoundsException> { engine.rDrawFuzzColumn() }

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        byteArrayOf(10, -31, 30, 40, 50).forEachIndexed { y, pixel -> expected[y * SCREENWIDTH + 2] = pixel }
        assertContentEquals(expected, engine.screens[0])
        assertEquals(1, engine.fuzzpos)
    }
}
