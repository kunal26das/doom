
package doom.engine.world.lighting

import doom.engine.simulation.Thinker
import doom.engine.world.Sector

internal class GlowingLight : Thinker() {
    var sector: Sector? = null
    var minlight = 0
    var maxlight = 0
    var direction = 0
}
