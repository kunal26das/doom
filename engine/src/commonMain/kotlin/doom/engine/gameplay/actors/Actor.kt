
package doom.engine.gameplay.actors

import doom.engine.audio.SoundOrigin
import doom.engine.gameplay.player.Player
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker
import doom.engine.world.MapThingSpawn
import doom.engine.world.Subsector

internal class Actor : Thinker(), SoundOrigin {
    override var x: FixedPoint = 0
    override var y: FixedPoint = 0
    var z: FixedPoint = 0

    var snext: Actor? = null
    var sprev: Actor? = null

    var angle: BinaryAngle = 0u
    var sprite = 0
    var frame = 0

    var bnext: Actor? = null
    var bprev: Actor? = null

    var subsector: Subsector? = null

    var floorz: FixedPoint = 0
    var ceilingz: FixedPoint = 0

    var radius: FixedPoint = 0
    var height: FixedPoint = 0

    var momx: FixedPoint = 0
    var momy: FixedPoint = 0
    var momz: FixedPoint = 0

    var validcount = 0

    var type = 0
    var info: ActorDefinition? = null

    var tics = 0
    var state: StateDefinition? = null
    var flags = 0
    var health = 0

    var movedir = 0
    var movecount = 0

    var target: Actor? = null

    var reactiontime = 0

    var threshold = 0

    var player: Player? = null

    var lastlook = 0

    var spawnpoint: MapThingSpawn? = null

    var tracer: Actor? = null
}
