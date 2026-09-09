// Port of linuxdoom-1.10 r_data.c -- Preparation of data for rendering,
// generation of lookups, caching, retrieval by name.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

// A maptexturedef_t describes a rectangular texture,
//  which is composed of one or more mappatch_t structures
//  that arrange graphic patches.
internal class texture_t {
    // Keep name for switch changing, etc.
    var name: String = ""
    var width = 0
    var height = 0

    // All the patches[patchcount]
    //  are drawn back to front into the cached texture.
    var patchcount = 0
    var patches: Array<texpatch_t> = emptyArray()
}
