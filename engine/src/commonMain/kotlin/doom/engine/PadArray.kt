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
 * Now what is a visplane, anyway?
 * top/bottom are C byte[SCREENWIDTH] flanked by pad bytes that absorb the
 * deliberate writes at x == minx-1 and x == maxx+1; PadArray keeps that trick.
 */
internal class PadArray {
    private val a = IntArray(SCREENWIDTH + 2)
    operator fun get(i: Int): Int = a[i + 1]
    operator fun set(i: Int, v: Int) { a[i + 1] = v }
    fun fill(v: Int) = a.fill(v)
}
