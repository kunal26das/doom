// Port of linuxdoom-1.10 p_ceilng.c -- Ceiling aninmation (lowering, crushing, raising).
// (Also owns the P_CEILNG thinker struct + ceiling_e consts and
//  CEILSPEED/CEILWAIT/MAXCEILINGS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: ceiling_t
internal class ceiling_t : thinker_t() {
    var type = 0  // ceiling_e
    var sector: sector_t? = null
    var bottomheight: fixed_t = 0
    var topheight: fixed_t = 0
    var speed: fixed_t = 0
    var crush = false

    // 1 = up, 0 = waiting, -1 = down
    var direction = 0

    // ID
    var tag = 0
    var olddirection = 0
}
