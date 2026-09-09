// Port of linuxdoom-1.10 d_items.c -- items: key cards, artifacts, weapon, ammunition.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Weapon info: sprite frames, ammunition use.
internal class weaponinfo_t(
    val ammo: Int,        // ammotype_t
    val upstate: Int,
    val downstate: Int,
    val readystate: Int,
    val atkstate: Int,
    val flashstate: Int,
)
