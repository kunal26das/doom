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

//
//      source animation definition
//
internal class animdef_t {
    var istexture = 0   // C boolean; if false, it is a flat. -1 terminates the list.
    var endname = ""
    var startname = ""
    var speed = 0

    constructor(istexture: Boolean, endname: String, startname: String, speed: Int) {
        this.istexture = if (istexture) 1 else 0
        this.endname = endname
        this.startname = startname
        this.speed = speed
    }

    // the {-1} table terminator
    constructor(istexture: Int) {
        this.istexture = istexture
    }
}
