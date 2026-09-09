// Port of linuxdoom-1.10 d_items.c -- items: key cards, artifacts, weapon, ammunition.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine


internal val DoomEngineCore.weaponinfo: Array<weaponinfo_t>
    get() = stateDItems.weaponinfo
