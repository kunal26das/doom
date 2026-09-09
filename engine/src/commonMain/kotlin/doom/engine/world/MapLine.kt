
package doom.engine.world

import doom.engine.geometry.FixedPoint
import doom.engine.rendering.ST_HORIZONTAL
import doom.engine.simulation.Thinker

internal class MapLine {
    var v1: MapVertex = MapVertex()
    var v2: MapVertex = MapVertex()

    var dx: FixedPoint = 0
    var dy: FixedPoint = 0

    var flags = 0
    var special = 0
    var tag = 0

    val sidenum = IntArray(2)

    val bbox = IntArray(4)

    var slopetype = ST_HORIZONTAL

    var frontsector: Sector? = null
    var backsector: Sector? = null

    var validcount = 0

    var specialdata: Thinker? = null

    var index = 0
}
