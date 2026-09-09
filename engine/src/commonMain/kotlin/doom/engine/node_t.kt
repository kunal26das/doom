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

/** BSP node. children: if NF_SUBSECTOR its a subsector index (unsigned short in file). */
internal class node_t {
    // Partition line.
    var x: fixed_t = 0
    var y: fixed_t = 0
    var dx: fixed_t = 0
    var dy: fixed_t = 0

    // Bounding box for each child.
    val bbox = Array(2) { IntArray(4) }

    // If NF_SUBSECTOR its a subsector.
    val children = IntArray(2)
}
