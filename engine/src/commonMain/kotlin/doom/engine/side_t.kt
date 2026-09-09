// Port of linuxdoom-1.10 r_defs.h -- shared rendering/map runtime structs.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
//
// Pointer conventions used across the whole port (see PORTING.md):
//  * `lighttable_t*` (a pointer into the COLORMAP lump) --> `Int` byte offset
//    into the global `colormaps` ByteArray; -1 stands for NULL.
//  * `short*` pointers into the shared `openings` ShortArray (r_plane/r_segs)
//    --> `Int` index into `openings`; -1 stands for NULL.
//  * struct pointer subtraction (`sec - sectors`) --> `index` field set at load.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** The SideDef. */
internal class side_t {
    // add this to the calculated texture column
    var textureoffset: fixed_t = 0

    // add this to the calculated texture top
    var rowoffset: fixed_t = 0

    // Texture indices. We do not maintain names here.
    var toptexture = 0
    var bottomtexture = 0
    var midtexture = 0

    // Sector the SideDef is facing.
    var sector: sector_t? = null

    var index = 0
}
