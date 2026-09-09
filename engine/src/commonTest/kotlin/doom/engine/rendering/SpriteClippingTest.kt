package doom.engine.rendering

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.geometry.FRACUNIT
import doom.engine.rendering.resources.colormaps
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SpriteClippingTest {
    @Test
    fun successiveLowDetailSpritesKeepOutsideRangePostsClippedAfterHorizontalDoubling() {
        val engine = DoomEngineCore().apply {
            wadArchive.load(listOf(spriteArchive()))
            rInitBuffer(SCREENWIDTH, SCREENHEIGHT)
            viewheight = SCREENHEIGHT
            detailshift = 1
            colormaps = ByteArray(256) { it.toByte() }
            basecolfunc = { rDrawColumnLow() }
            colfunc = basecolfunc
        }

        engine.rDrawSprite(sprite(4))
        assertEquals(11, engine.screens[0][8].toInt())
        assertEquals(11, engine.screens[0][9].toInt())
        engine.screens[0].fill(0)

        engine.rDrawSprite(sprite(2))

        val expected = ByteArray(SCREENWIDTH * SCREENHEIGHT)
        expected[4] = 11
        expected[5] = 11
        assertContentEquals(expected, engine.screens[0])
        assertEquals(5, engine.dcX)
    }

    private fun sprite(x: Int): VisibleSprite = VisibleSprite().apply {
        x1 = x
        x2 = x
        scale = FRACUNIT
        xiscale = 2 * FRACUNIT
        colormap = 0
    }

    private fun spriteArchive(): ByteArray = byteArrayOf(
        80, 87, 65, 68,
        1, 0, 0, 0,
        12, 0, 0, 0,
        28, 0, 0, 0,
        23, 0, 0, 0,
        83, 80, 82, 73, 84, 69, 0, 0,
        1, 0, 3, 0, 0, 0, 0, 0,
        12, 0, 0, 0,
        0, 1, 0, 11, 0,
        2, 1, 0, 22, 0,
        -1,
    )
}
