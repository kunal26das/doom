// Port of linuxdoom-1.10 r_bsp.c -- BSP traversal, handling of LineSegs
// for rendering.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

//
// ClipWallSegment
// Clips the given range of columns
// and includes it in the new clip list.
//
internal class cliprange_t {
    var first = 0
    var last = 0
}
