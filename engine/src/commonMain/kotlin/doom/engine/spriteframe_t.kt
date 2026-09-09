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
 * Sprites are patches with a special naming convention so they can be
 * recognized by R_InitSprites. Rotation zero means all views look the same.
 * (`rotate` is memset to -1 during init, hence Int not Boolean.)
 */
internal class spriteframe_t {
    // If false use 0 for any position.
    // Note: as eight entries are available, we might as well insert the same name eight times.
    var rotate = -1

    // Lump to use for view angles 0-7.
    val lump = IntArray(8)

    // Flip bit (1 = flip) to use for view angles 0-7.
    val flip = IntArray(8)
}
