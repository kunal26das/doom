// Port of linuxdoom-1.10 am_map.c/am_map.h -- the automap code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

internal class mline_t(ax: fixed_t = 0, ay: fixed_t = 0, bx: fixed_t = 0, by: fixed_t = 0) {
    val a = mpoint_t(ax, ay)
    val b = mpoint_t(bx, by)
}
