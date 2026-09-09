// Port of linuxdoom-1.10 p_floor.c -- Floor animation: raising stairs.
// (Also owns the P_FLOOR thinker struct + floor_e/stair_e/result_e consts
//  and FLOORSPEED from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: floormove_t
internal class floormove_t : thinker_t() {
    var type = 0  // floor_e
    var crush = false
    var sector: sector_t? = null
    var direction = 0
    var newspecial = 0
    var texture = 0  // C: short
    var floordestheight: fixed_t = 0
    var speed: fixed_t = 0
}
