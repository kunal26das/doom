
package doom.engine.world.movers

import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class CeilingMover : Thinker() {
    var type = 0
    var sector: Sector? = null
    var bottomheight: FixedPoint = 0
    var topheight: FixedPoint = 0
    var speed: FixedPoint = 0
    var crush = false

    var direction = 0

    var tag = 0
    var olddirection = 0
}
