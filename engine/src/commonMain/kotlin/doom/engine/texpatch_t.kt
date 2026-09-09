// Port of linuxdoom-1.10 r_data.c -- Preparation of data for rendering,
// generation of lookups, caching, retrieval by name.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

// A single patch from a texture definition,
//  basically a rectangular area within
//  the texture rectangle.
internal class texpatch_t {
    // Block origin (allways UL),
    // which has allready accounted
    // for the internal origin of the patch.
    var originx = 0
    var originy = 0
    var patch = 0
}
