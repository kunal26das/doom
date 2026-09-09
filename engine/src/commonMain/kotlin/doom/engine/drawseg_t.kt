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

/**
 * The renderer uses these to (re)draw sprites over segs (masked textures, etc).
 * sprtopclip/sprbottomclip/maskedtexturecol: index into `openings` (-1 = NULL).
 */
internal class drawseg_t {
    var curline: seg_t? = null
    var x1 = 0
    var x2 = 0

    var scale1: fixed_t = 0
    var scale2: fixed_t = 0
    var scalestep: fixed_t = 0

    // 0=none, 1=bottom, 2=top, 3=both
    var silhouette = 0

    // do not clip sprites above this
    var bsilheight: fixed_t = 0

    // do not clip sprites below this
    var tsilheight: fixed_t = 0

    // Pointers to lists for sprite clipping, all three adjusted so [x1] is first value.
    var sprtopclip = -1        // index into openings; -1 = NULL
    var sprbottomclip = -1     // index into openings; -1 = NULL
    var maskedtexturecol = -1  // index into openings; -1 = NULL
}
