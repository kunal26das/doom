// Port of linuxdoom-1.10 p_plats.c -- Plats (i.e. elevator platforms) code,
// raising/lowering.
// (Also owns the P_PLATS thinker struct + plat_e/plattype_e consts and
//  PLATWAIT/PLATSPEED/MAXPLATS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: plat_t
internal class plat_t : thinker_t() {
    var sector: sector_t? = null
    var speed: fixed_t = 0
    var low: fixed_t = 0
    var high: fixed_t = 0
    var wait = 0
    var count = 0
    var status = 0  // plat_e
    var oldstatus = 0  // plat_e
    var crush = false
    var tag = 0
    var type = 0  // plattype_e
}
