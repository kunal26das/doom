
package doom.engine.rendering

import doom.engine.world.MapLine
import doom.engine.world.MapSegment
import doom.engine.world.MapSide
import doom.engine.world.Sector

internal class BspRendererState {
    var curline: MapSegment? = null

    var sidedef: MapSide? = null

    var linedef: MapLine? = null

    var frontsector: Sector? = null

    var backsector: Sector? = null

    val drawsegs by lazy(LazyThreadSafetyMode.NONE) { Array(MAXDRAWSEGS) { DrawSegment() } }

    var dsP = 0

    var newend = 0

    val solidsegs by lazy(LazyThreadSafetyMode.NONE) { Array(MAXSEGS) { ClipRange() } }

    val checkcoord by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(3, 0, 2, 1),
        intArrayOf(3, 0, 2, 0),
        intArrayOf(3, 1, 2, 0),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(2, 0, 2, 1),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(3, 1, 3, 0),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(2, 0, 3, 1),
        intArrayOf(2, 1, 3, 1),
        intArrayOf(2, 1, 3, 0),
        intArrayOf(0, 0, 0, 0),
    ) }
}
