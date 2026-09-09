
package doom.engine.world.lighting

import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class LightFlash : Thinker() {
    var sector: Sector? = null
    var count = 0
    var maxlight = 0
    var minlight = 0
    var maxtime = 0
    var mintime = 0
}
