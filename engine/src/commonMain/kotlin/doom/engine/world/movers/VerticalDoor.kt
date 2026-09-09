
package doom.engine.world.movers

import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class VerticalDoor : Thinker() {
    var type = 0
    var sector: Sector? = null
    var topheight: FixedPoint = 0
    var speed: FixedPoint = 0

    var direction = 0

    var topwait = 0

    var topcountdown = 0
}
