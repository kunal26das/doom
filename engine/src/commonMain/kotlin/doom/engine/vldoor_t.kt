// Port of linuxdoom-1.10 p_doors.c -- Door animation code (opening/closing).
// (Also owns the P_DOORS thinker struct + vldoor_e consts from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING",
    "SENSELESS_COMPARISON", "ktlint")

package doom.engine

// From p_spec.h: vldoor_t
internal class vldoor_t : thinker_t() {
    var type = 0  // vldoor_e
    var sector: sector_t? = null
    var topheight: fixed_t = 0
    var speed: fixed_t = 0

    // 1 = up, 0 = waiting at top, -1 = down
    var direction = 0

    // tics to wait at the top
    var topwait = 0

    // (keep in case a door going down is reset)
    // when it reaches 0, start going down
    var topcountdown = 0
}
