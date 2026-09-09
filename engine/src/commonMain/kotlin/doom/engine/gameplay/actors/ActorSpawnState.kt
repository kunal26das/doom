
package doom.engine.gameplay.actors

import doom.engine.world.ITEMQUESIZE
import doom.engine.world.MapThingSpawn

internal class ActorSpawnState {
    var test = 0

    val itemrespawnque by lazy(LazyThreadSafetyMode.NONE) { Array(ITEMQUESIZE) { MapThingSpawn() } }

    val itemrespawntime by lazy(LazyThreadSafetyMode.NONE) { IntArray(ITEMQUESIZE) }

    var iquehead = 0

    var iquetail = 0
}
