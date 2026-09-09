// Port of linuxdoom-1.10 p_user.c -- player related stuff.
// Bobbing POV/weapon, movement. Pending weapon.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class PUserState {
    var onground = false

    val ANG5: angle_t by lazy(LazyThreadSafetyMode.NONE) { ANG90 / 18u }
}
