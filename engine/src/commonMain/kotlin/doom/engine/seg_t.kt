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

/** The LineSeg. */
internal class seg_t {
    var v1: vertex_t = vertex_t()
    var v2: vertex_t = vertex_t()

    var offset: fixed_t = 0

    var angle: angle_t = 0u

    var sidedef: side_t? = null
    var linedef: line_t? = null

    // Sector references. Could be retrieved from linedef, too.
    // backsector is NULL for one sided lines.
    var frontsector: sector_t? = null
    var backsector: sector_t? = null

    var index = 0
}
