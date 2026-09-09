package doom.engine.geometry

import doom.engine.core.DoomEngineCore
import doom.engine.rendering.rPointToDist
import doom.engine.rendering.viewx
import doom.engine.rendering.viewy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RendererDistanceTest {
    @Test
    fun viewExactlyOnWallVertexHasZeroDistance() {
        val core = DoomEngineCore()
        core.viewx = 42 * FRACUNIT
        core.viewy = -17 * FRACUNIT
        assertEquals(0, core.rPointToDist(core.viewx, core.viewy))
    }

    @Test
    fun nonzeroDistancesKeepTheOriginalFixedPointCalculation() {
        val core = DoomEngineCore()
        assertTrue(core.rPointToDist(3 * FRACUNIT, 0) in 3 * FRACUNIT..3 * FRACUNIT + 8)
        assertTrue(core.rPointToDist(0, -3 * FRACUNIT) in 3 * FRACUNIT..3 * FRACUNIT + 8)
        assertTrue(core.rPointToDist(3 * FRACUNIT, 4 * FRACUNIT) in 5 * FRACUNIT - 256..5 * FRACUNIT + 256)
    }
}
