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

internal class line_t {
    // Vertices, from v1 to v2.
    var v1: vertex_t = vertex_t()
    var v2: vertex_t = vertex_t()

    // Precalculated v2 - v1 for side checking.
    var dx: fixed_t = 0
    var dy: fixed_t = 0

    // Animation related.
    var flags = 0
    var special = 0
    var tag = 0

    // Visual appearance: SideDefs. sidenum[1] will be -1 if one sided.
    val sidenum = IntArray(2)

    // Neat. Another bounding box, for the extent of the LineDef.
    val bbox = IntArray(4)

    // To aid move clipping.
    var slopetype = ST_HORIZONTAL

    // Front and back sector.
    // Note: redundant? Can be retrieved from SideDefs.
    var frontsector: sector_t? = null
    var backsector: sector_t? = null

    // if == validcount, already checked
    var validcount = 0

    // thinker_t for reversable actions (C void*)
    var specialdata: thinker_t? = null

    var index = 0
}
