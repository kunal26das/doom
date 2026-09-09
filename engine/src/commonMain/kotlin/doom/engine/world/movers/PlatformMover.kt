
package doom.engine.world.movers

import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class PlatformMover : Thinker() {
    var sector: Sector? = null
    var speed: FixedPoint = 0
    var low: FixedPoint = 0
    var high: FixedPoint = 0
    var wait = 0
    var count = 0
    var status = 0
    var oldstatus = 0
    var crush = false
    var tag = 0
    var type = 0
}
