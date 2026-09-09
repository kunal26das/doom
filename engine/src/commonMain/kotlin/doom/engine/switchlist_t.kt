// Port of linuxdoom-1.10 p_switch.c -- switches, buttons. Two-state animation. Exits.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: P_SWITCH.
internal class switchlist_t(
    val name1: String,
    val name2: String,
    val episode: Int,   // C: short
)
