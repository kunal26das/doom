package doom.engine.geometry

import doom.engine.core.DoomEngineCore
import doom.engine.resources.MAXINT
import doom.engine.resources.MININT
import doom.engine.simulation.mClearRandom
import doom.engine.simulation.mRandom
import doom.engine.simulation.pRandom
import doom.engine.simulation.prndindex

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals

class DeterministicPrimitivesTest {
    private val core = DoomEngineCore()
    @Test
    fun gameplayRandomStreamIsIndependentAndWrapsExactly() = with(core) {
        mClearRandom()
        assertContentEquals(intArrayOf(8, 109, 220, 222, 241), IntArray(5) { pRandom() })
        repeat(91) { mRandom() }
        assertEquals(149, pRandom())
        repeat(250) { pRandom() }
        assertEquals(0, prndindex)
        assertEquals(8, pRandom())
        mClearRandom()
    }

    @Test
    fun fixedPointArithmeticPreservesSignsTruncationAndSaturation() = with(core) {
        assertEquals(6 * FRACUNIT, fixedMul(2 * FRACUNIT, 3 * FRACUNIT))
        assertEquals(-FRACUNIT / 2, fixedMul(-FRACUNIT, FRACUNIT / 2))
        assertEquals(-1, fixedMul(-1, 1))
        assertEquals(FRACUNIT / 2, fixedDiv(FRACUNIT, 2 * FRACUNIT))
        assertEquals(-FRACUNIT / 3, fixedDiv(-FRACUNIT, 3 * FRACUNIT))
        assertEquals(MAXINT, fixedDiv(FRACUNIT, 0))
        assertEquals(MININT, fixedDiv(-FRACUNIT, 0))
    }
}
