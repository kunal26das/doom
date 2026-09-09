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

/** A vissprite_t is a thing that will be drawn during a refresh. */
internal class vissprite_t {
    // Doubly linked list.
    var prev: vissprite_t? = null
    var next: vissprite_t? = null

    var x1 = 0
    var x2 = 0

    // for line side calculation
    var gx: fixed_t = 0
    var gy: fixed_t = 0

    // global bottom / top for silhouette clipping
    var gz: fixed_t = 0
    var gzt: fixed_t = 0

    // horizontal position of x1
    var startfrac: fixed_t = 0

    var scale: fixed_t = 0

    // negative if flipped
    var xiscale: fixed_t = 0

    var texturemid: fixed_t = 0
    var patch = 0

    // for color translation and shadow draw, maxbright frames as well
    var colormap = -1  // lighttable offset into colormaps; -1 = NULL

    var mobjflags = 0
}
