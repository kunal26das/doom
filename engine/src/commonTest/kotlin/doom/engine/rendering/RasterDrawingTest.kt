package doom.engine.rendering

import doom.engine.DoomError
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.geometry.FRACUNIT
import doom.engine.rendering.resources.colormaps
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RasterDrawingTest {
    @Test
    fun columnsWrapTextureCoordinatesClampSourceAndKeepDetailCoordinates() {
        for (lowDetail in listOf(false, true)) {
            val engine = raster().apply {
                dcX = 2
                dcYl = 0
                dcYh = 3
                dcIscale = FRACUNIT
                dcTexturemid = 126 * FRACUNIT
                dcSource = byteArrayOf(5, 9, 13)
                dcSourceOfs = -1
            }

            if (lowDetail) engine.rDrawColumnLow() else engine.rDrawColumn()

            val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
            val column = byteArrayOf(-14, -14, -6, -6)
            val x = if (lowDetail) 4 else 2
            column.forEachIndexed { y, pixel ->
                expected[y * SCREENWIDTH + x] = pixel
                if (lowDetail) expected[y * SCREENWIDTH + x + 1] = pixel
            }
            assertContentEquals(expected, engine.screens[0])
            assertEquals(x, engine.dcX)
            assertEquals(126 * FRACUNIT, engine.dcTexturemid)
        }
    }

    @Test
    fun translatedColumnsClampBeforeApplyingUnsignedTranslationAndColorMap() {
        val engine = raster().apply {
            dcX = 3
            dcYl = 0
            dcYh = 3
            dcIscale = FRACUNIT
            dcTexturemid = -FRACUNIT
            dcSource = byteArrayOf(-128, -1)
            dcTranslation = 256
            translationtables = ByteArray(512) { (it + 1).toByte() }
        }

        engine.rDrawTranslatedColumn()

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        byteArrayOf(126, 126, -1, -1).forEachIndexed { y, pixel -> expected[y * SCREENWIDTH + 3] = pixel }
        assertContentEquals(expected, engine.screens[0])
        assertEquals(-FRACUNIT, engine.dcTexturemid)
    }

    @Test
    fun spansWrapBothFixedPointCoordinatesAndPreserveSourceOffsetClamping() {
        val engine = raster().apply {
            dsY = 2
            dsX1 = 3
            dsX2 = 6
            dsXfrac = -FRACUNIT
            dsYfrac = -FRACUNIT
            dsXstep = FRACUNIT
            dsYstep = FRACUNIT
            dsSource = ByteArray(4096) { (it * 7 + it / 64).toByte() }
            dsSourceOfs = -1
        }

        engine.rDrawSpan()

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        byteArrayOf(-50, -1, 62, 118).copyInto(expected, 2 * SCREENWIDTH + 3)
        assertContentEquals(expected, engine.screens[0])
        assertEquals(-FRACUNIT, engine.dsXfrac)
        assertEquals(-FRACUNIT, engine.dsYfrac)
    }

    @Test
    fun lowDetailSpansKeepTheirDoubledExtentAndCoordinateUpdates() {
        val engine = raster().apply {
            dsY = 1
            dsX1 = 1
            dsX2 = 2
            dsXstep = FRACUNIT
            dsSource = byteArrayOf(4, 8, 12)
        }

        engine.rDrawSpanLow()

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        byteArrayOf(-5, -5, -9, -9, -13, -13).copyInto(expected, SCREENWIDTH + 2)
        assertContentEquals(expected, engine.screens[0])
        assertEquals(2, engine.dsX1)
        assertEquals(4, engine.dsX2)
        assertEquals(0, engine.dsXfrac)
    }

    @Test
    fun lowDetailSpansReadLiveSourceBytesBetweenAdjacentWrites() {
        val engine = raster().apply {
            dsX1 = 1
            dsX2 = 1
            dsColormap = 0
            colormaps = ByteArray(256) { (it + 1).toByte() }
            dsSource = screens[0]
            dsSourceOfs = 2
            screens[0][2] = 1
        }

        engine.rDrawSpanLow()

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        expected[2] = 2
        expected[3] = 3
        assertContentEquals(expected, engine.screens[0])
    }

    @Test
    fun fuzzClipsBorderRowsWrapsPositionAndReadsEarlierWrites() {
        val engine = fuzzRaster().apply { fuzzpos = FUZZTABLE - 1 }

        engine.rDrawFuzzColumn()

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        byteArrayOf(10, -31, -41, 40, 50).forEachIndexed { y, pixel -> expected[y * SCREENWIDTH + 2] = pixel }
        assertContentEquals(expected, engine.screens[0])
        assertEquals(1, engine.dcYl)
        assertEquals(3, engine.dcYh)
        assertEquals(2, engine.fuzzpos)
    }

    @Test
    fun invalidColumnRangesFailBeforeChangingPixelsOrHorizontalCoordinates() {
        val drawers = listOf<DoomEngineCore.() -> Unit>(
            { rDrawColumn() }, { rDrawColumnLow() }, { rDrawTranslatedColumn() }, { rDrawFuzzColumn() },
        )
        for (draw in drawers) {
            val engine = raster().apply {
                dcX = SCREENWIDTH
                dcYl = 1
                dcYh = 1
                viewheight = SCREENHEIGHT
            }

            assertFailsWith<DoomError> { engine.draw() }

            assertContentEquals(ByteArray(SCREENWIDTH * SCREENHEIGHT), engine.screens[0])
            assertEquals(SCREENWIDTH, engine.dcX)
        }
    }

    private fun raster(): DoomEngineCore = DoomEngineCore().apply {
        rInitBuffer(SCREENWIDTH, SCREENHEIGHT)
        colormaps = ByteArray(7 * 256) { (255 - it).toByte() }
        dcColormap = 256
        dsColormap = 256
    }

    private fun fuzzRaster(): DoomEngineCore = raster().apply {
        dcX = 2
        dcYl = 0
        dcYh = 4
        viewheight = 5
        repeat(5) { y -> screens[0][y * SCREENWIDTH + 2] = ((y + 1) * 10).toByte() }
    }
}
