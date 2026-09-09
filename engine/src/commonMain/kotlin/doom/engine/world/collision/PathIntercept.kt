
package doom.engine.world.collision

import doom.engine.gameplay.actors.Actor
import doom.engine.geometry.FixedPoint
import doom.engine.world.MapLine

internal class PathIntercept {
    var frac: FixedPoint = 0
    var isaline = false
    var thing: Actor? = null
    var line: MapLine? = null
}
