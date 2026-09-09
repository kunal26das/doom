package doom.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals

class DeterministicPrimitivesTest {
    private val core = DoomEngineCore()
    @Test
    fun gameplayRandomStreamIsIndependentAndWrapsExactly() = with(core) {
        M_ClearRandom()
        assertContentEquals(intArrayOf(8, 109, 220, 222, 241), IntArray(5) { P_Random() })
        repeat(91) { M_Random() }
        assertEquals(149, P_Random())
        repeat(250) { P_Random() }
        assertEquals(0, prndindex)
        assertEquals(8, P_Random())
        M_ClearRandom()
    }

    @Test
    fun fixedPointArithmeticPreservesSignsTruncationAndSaturation() = with(core) {
        assertEquals(6 * FRACUNIT, FixedMul(2 * FRACUNIT, 3 * FRACUNIT))
        assertEquals(-FRACUNIT / 2, FixedMul(-FRACUNIT, FRACUNIT / 2))
        assertEquals(-1, FixedMul(-1, 1))
        assertEquals(FRACUNIT / 2, FixedDiv(FRACUNIT, 2 * FRACUNIT))
        assertEquals(-FRACUNIT / 3, FixedDiv(-FRACUNIT, 3 * FRACUNIT))
        assertEquals(MAXINT, FixedDiv(FRACUNIT, 0))
        assertEquals(MININT, FixedDiv(-FRACUNIT, 0))
    }
}
