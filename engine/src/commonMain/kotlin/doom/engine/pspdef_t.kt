// Port of linuxdoom-1.10 d_player.h + p_pspr.h player/psprite definitions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal class pspdef_t {
    var state: state_t? = null  // a NULL state means not active
    var tics = 0
    var sx: fixed_t = 0
    var sy: fixed_t = 0
}
