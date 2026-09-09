
package doom.engine.rendering

import doom.engine.geometry.FixedPoint
import doom.engine.world.MapSegment

internal class DrawSegment {
    var curline: MapSegment? = null
    var x1 = 0
    var x2 = 0

    var scale1: FixedPoint = 0
    var scale2: FixedPoint = 0
    var scalestep: FixedPoint = 0

    var silhouette = 0

    var bsilheight: FixedPoint = 0

    var tsilheight: FixedPoint = 0

    var sprtopclip = -1
    var sprbottomclip = -1
    var maskedtexturecol = -1
}
