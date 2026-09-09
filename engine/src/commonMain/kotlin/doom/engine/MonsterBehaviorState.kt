// Port of linuxdoom-1.10 p_enemy.c -- enemy thinking, AI.
// Action Pointer Functions that are associated with states/frames.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class MonsterBehaviorState {
    val opposite by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        DI_WEST, DI_SOUTHWEST, DI_SOUTH, DI_SOUTHEAST,
        DI_EAST, DI_NORTHEAST, DI_NORTH, DI_NORTHWEST, DI_NODIR
    ) }

    val diags by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        DI_NORTHWEST, DI_NORTHEAST, DI_SOUTHWEST, DI_SOUTHEAST
    ) }

    var soundtarget: mobj_t? = null

    val xspeed by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000, 0, 47000) }

    val yspeed by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0, 47000, FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000) }

    var TRACEANGLE = 0xc000000

    var corpsehit: mobj_t? = null

    var vileobj: mobj_t? = null

    var viletryx: fixed_t = 0

    var viletryy: fixed_t = 0

    val FATSPREAD: angle_t by lazy(LazyThreadSafetyMode.NONE) { ANG90 / 8u }

    val braintargets by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<mobj_t>(32) }

    var numbraintargets = 0

    var braintargeton = 0

    var easy = 0
}
