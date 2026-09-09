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
// Animating textures and planes
// There is another anim_t used in wi_stuff, unrelated.
//
internal class anim_t {
    var istexture = false
    var picnum = 0
    var basepic = 0
    var numpics = 0
    var speed = 0
}
