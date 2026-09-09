
package doom.engine.automap

import doom.engine.geometry.FixedPoint

internal class AutomapWorldLine(ax: FixedPoint = 0, ay: FixedPoint = 0, bx: FixedPoint = 0, by: FixedPoint = 0) {
    val a = AutomapWorldPoint(ax, ay)
    val b = AutomapWorldPoint(bx, by)
}
