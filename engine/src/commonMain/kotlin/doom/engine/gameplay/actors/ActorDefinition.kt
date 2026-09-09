
package doom.engine.gameplay.actors

import doom.engine.geometry.FixedPoint

internal class ActorDefinition(
    val doomednum: Int,
    val spawnstate: Int,
    val spawnhealth: Int,
    val seestate: Int,
    val seesound: Int,
    val reactiontime: Int,
    val attacksound: Int,
    val painstate: Int,
    val painchance: Int,
    val painsound: Int,
    val meleestate: Int,
    val missilestate: Int,
    val deathstate: Int,
    val xdeathstate: Int,
    val deathsound: Int,
    var speed: Int,
    val radius: FixedPoint,
    val height: FixedPoint,
    val mass: Int,
    val damage: Int,
    val activesound: Int,
    val flags: Int,
    val raisestate: Int,
)
