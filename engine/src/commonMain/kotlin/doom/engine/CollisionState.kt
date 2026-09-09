// Port of linuxdoom-1.10 p_map.c -- movement, collision handling.
// Shooting and aiming.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class CollisionState {
    val tmbbox by lazy(LazyThreadSafetyMode.NONE) { IntArray(4) }

    var tmthing: mobj_t? = null

    var tmflags = 0

    var tmx: fixed_t = 0

    var tmy: fixed_t = 0

    var floatok = false

    var tmfloorz: fixed_t = 0

    var tmceilingz: fixed_t = 0

    var tmdropoffz: fixed_t = 0

    var ceilingline: line_t? = null

    val spechit by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<line_t>(MAXSPECIALCROSS) }

    var numspechit = 0

    var bestslidefrac: fixed_t = 0

    var secondslidefrac: fixed_t = 0

    var bestslideline: line_t? = null

    var secondslideline: line_t? = null

    var slidemo: mobj_t? = null

    var tmxmove: fixed_t = 0

    var tmymove: fixed_t = 0

    var linetarget: mobj_t? = null

    var shootthing: mobj_t? = null

    var shootz: fixed_t = 0

    var la_damage = 0

    var attackrange: fixed_t = 0

    var aimslope: fixed_t = 0

    var usething: mobj_t? = null

    var bombsource: mobj_t? = null

    var bombspot: mobj_t? = null

    var bombdamage = 0

    var crushchange = false

    var nofit = false
}
