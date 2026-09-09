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

/** The SECTORS record, at runtime. Stores things/mobjs. */
internal class sector_t {
    var floorheight: fixed_t = 0
    var ceilingheight: fixed_t = 0
    var floorpic = 0
    var ceilingpic = 0
    var lightlevel = 0
    var special = 0
    var tag = 0

    // 0 = untraversed, 1,2 = sndlines -1
    var soundtraversed = 0

    // thing that made a sound (or null)
    var soundtarget: mobj_t? = null

    // mapblock bounding box for height changes
    val blockbox = IntArray(4)

    // origin for any sounds played by the sector
    val soundorg = degenmobj_t()

    // if == validcount, already checked
    var validcount = 0

    // list of mobjs in sector
    var thinglist: mobj_t? = null

    // thinker_t for reversable actions (C void*)
    var specialdata: thinker_t? = null

    var linecount = 0
    var lines: Array<line_t?> = emptyArray()  // [linecount] size

    var index = 0  // C pointer arithmetic (sec - sectors)
}
