package doom.engine.geometry

import doom.engine.core.DoomEngineCore
import doom.engine.rendering.rPointToAngle2
import doom.engine.rendering.viewx
import doom.engine.rendering.viewy
import doom.engine.world.BspNode
import doom.engine.world.NF_SUBSECTOR
import doom.engine.world.Subsector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class FixedGeometryTest {
    @Test
    fun anglesRetainVanillaOctantAndAxisRounding() {
        val scale = 10 * FRACUNIT
        val points = listOf(
            (scale to 0) to 0u,
            (scale to scale) to (ANG45 - 1u),
            (0 to scale) to (ANG90 - 1u),
            (-scale to scale) to ANG90 + ANG45,
            (-scale to 0) to (ANG180 - 1u),
            (-scale to -scale) to (ANG180 + ANG45 - 1u),
            (0 to -scale) to ANG270,
            (scale to -scale) to (ANG270 + ANG45),
        )
        for ((point, expected) in points) {
            assertEquals(expected, FixedGeometry.angleBetween(0, 0, point.first, point.second))
            assertEquals(expected, FixedGeometry.angleBetween(21, -17, point.first + 21, point.second - 17))
        }
        assertEquals(0u, FixedGeometry.angleBetween(scale, scale, scale, scale))
    }

    @Test
    fun gameplayAngleQueryDoesNotMoveTheRenderCamera() {
        val core = DoomEngineCore()
        core.viewx = 7 * FRACUNIT
        core.viewy = -13 * FRACUNIT
        assertEquals(0u, core.rPointToAngle2(2 * FRACUNIT, 3 * FRACUNIT, 4 * FRACUNIT, 3 * FRACUNIT))
        assertEquals(7 * FRACUNIT, core.viewx)
        assertEquals(-13 * FRACUNIT, core.viewy)
    }

    @Test
    fun bspQueryUsesItsSuppliedWorldAndHandlesMapsWithoutNodes() {
        val west = Subsector()
        val east = Subsector()
        val partition = BspNode().apply {
            x = 0; y = 0; dx = 0; dy = FRACUNIT
            children[0] = NF_SUBSECTOR or 1
            children[1] = NF_SUBSECTOR
        }
        val leaves = arrayOf(west, east)
        assertSame(west, BspQueries.subsectorAt(-FRACUNIT, 0, arrayOf(partition), leaves))
        assertSame(east, BspQueries.subsectorAt(FRACUNIT, 0, arrayOf(partition), leaves))
        assertSame(west, BspQueries.subsectorAt(FRACUNIT, 0, emptyArray(), arrayOf(west)))
    }
}
