
package doom.engine.rendering

import doom.engine.geometry.FixedPoint

internal class VisiblePlane {
    var height: FixedPoint = 0
    var picnum = 0
    var lightlevel = 0
    var minx = 0
    var maxx = 0

    val top = PaddedColumnBuffer()
    val bottom = PaddedColumnBuffer()
}
