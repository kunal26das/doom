// Port of linuxdoom-1.10 p_spec.c -- implements special effects:
// Texture animation, height or lighting changes according to adjacent
// sectors, respective utility functions, etc.
// Line Tag handling. Line and Sector triggers.
// (Also owns from p_spec.h: MO_TELEPORTMAN, button_t/bwhere_e/BUTTONTIME/
//  MAXBUTTONS + buttonlist, and the levelTimer globals.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

internal class button_t {
    var line: line_t? = null
    var where = 0            // bwhere_e
    var btexture = 0
    var btimer = 0
    // C: mobj_t* soundorg -- always assigned (mobj_t*)&sector->soundorg,
    // i.e. really the sector's degenmobj_t sound origin.
    var soundorg: degenmobj_t? = null
}
