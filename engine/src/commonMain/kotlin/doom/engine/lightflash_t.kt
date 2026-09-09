// Port of linuxdoom-1.10 p_lights.c -- handle Sector base lighting effects.
// Muzzle flash?
// (Also owns the P_LIGHTS thinker structs + light constants from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

internal class lightflash_t : thinker_t() {
    var sector: sector_t? = null
    var count = 0
    var maxlight = 0
    var minlight = 0
    var maxtime = 0
    var mintime = 0
}
