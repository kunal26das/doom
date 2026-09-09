
package doom.engine.gameplay.actors

import doom.engine.geometry.ANG90
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FixedPoint

internal class MonsterBehaviorState {
    val opposite by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        DI_WEST, DI_SOUTHWEST, DI_SOUTH, DI_SOUTHEAST,
        DI_EAST, DI_NORTHEAST, DI_NORTH, DI_NORTHWEST, DI_NODIR
    ) }

    val diags by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        DI_NORTHWEST, DI_NORTHEAST, DI_SOUTHWEST, DI_SOUTHEAST
    ) }

    var soundtarget: Actor? = null

    val xspeed by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000, 0, 47000) }

    val yspeed by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0, 47000, FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000) }

    var traceangle = 0xc000000

    var corpsehit: Actor? = null

    var vileobj: Actor? = null

    var viletryx: FixedPoint = 0

    var viletryy: FixedPoint = 0

    val fatspread: BinaryAngle by lazy(LazyThreadSafetyMode.NONE) { ANG90 / 8u }

    val braintargets by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<Actor>(32) }

    var numbraintargets = 0

    var braintargeton = 0

    var easy = 0
}
