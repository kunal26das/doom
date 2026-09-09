
package doom.engine.world.movers

import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class FloorMover : Thinker() {
    var type = 0
    var crush = false
    var sector: Sector? = null
    var direction = 0
    var newspecial = 0
    var texture = 0
    var floordestheight: FixedPoint = 0
    var speed: FixedPoint = 0
}
