
package doom.engine.world

import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FixedPoint

internal class MapSegment {
    var v1: MapVertex = MapVertex()
    var v2: MapVertex = MapVertex()

    var offset: FixedPoint = 0

    var angle: BinaryAngle = 0u

    var sidedef: MapSide? = null
    var linedef: MapLine? = null

    var frontsector: Sector? = null
    var backsector: Sector? = null

    var index = 0
}
