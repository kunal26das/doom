// Port of linuxdoom-1.10 p_pspr.c -- weapon sprite animation, weapon objects.
// Action functions for weapons.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class WeaponSpriteState {
    var swingx: fixed_t = 0

    var swingy: fixed_t = 0

    var bulletslope: fixed_t = 0
}
