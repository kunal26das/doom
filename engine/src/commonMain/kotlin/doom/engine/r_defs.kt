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

/** Silhouette, needed for clipping Segs (mainly) and sprites representing things. */
internal const val SIL_NONE = 0
internal const val SIL_BOTTOM = 1
internal const val SIL_TOP = 2
internal const val SIL_BOTH = 3

internal const val MAXDRAWSEGS = 256


// slopetype_t: To aid move clipping.
internal const val ST_HORIZONTAL = 0
internal const val ST_VERTICAL = 1
internal const val ST_POSITIVE = 2
internal const val ST_NEGATIVE = 3


// posts are runs of non masked source pixels; columns are lists of posts.
// Patches and columns stay raw ByteArrays; see v_video.kt patch helpers.
