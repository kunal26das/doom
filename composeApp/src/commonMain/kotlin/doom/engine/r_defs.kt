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
const val SIL_NONE = 0
const val SIL_BOTTOM = 1
const val SIL_TOP = 2
const val SIL_BOTH = 3

const val MAXDRAWSEGS = 256

/** Your plain vanilla vertex. */
class vertex_t(var x: fixed_t = 0, var y: fixed_t = 0)

/** Anything with an (x, y) that S_StartSound can be attached to. */
interface soundorigin_t {
    val x: fixed_t
    val y: fixed_t
}

/**
 * Each sector has a degenmobj_t in its center for sound origin purposes.
 * (In C it embeds a thinker "not used for anything"; only the coords matter.)
 */
class degenmobj_t(
    override var x: fixed_t = 0,
    override var y: fixed_t = 0,
    var z: fixed_t = 0,
) : soundorigin_t

/** The SECTORS record, at runtime. Stores things/mobjs. */
class sector_t {
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

/** The SideDef. */
class side_t {
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

// slopetype_t: To aid move clipping.
const val ST_HORIZONTAL = 0
const val ST_VERTICAL = 1
const val ST_POSITIVE = 2
const val ST_NEGATIVE = 3

class line_t {
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

/**
 * A SubSector. References a Sector. Basically, this is a list of LineSegs,
 * indicating the visible walls that define (all or some) sides of a convex BSP leaf.
 */
class subsector_t {
    var sector: sector_t? = null
    var numlines = 0
    var firstline = 0
    var index = 0
}

/** The LineSeg. */
class seg_t {
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

/** BSP node. children: if NF_SUBSECTOR its a subsector index (unsigned short in file). */
class node_t {
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

// posts are runs of non masked source pixels; columns are lists of posts.
// Patches and columns stay raw ByteArrays; see v_video.kt patch helpers.

/**
 * The renderer uses these to (re)draw sprites over segs (masked textures, etc).
 * sprtopclip/sprbottomclip/maskedtexturecol: index into `openings` (-1 = NULL).
 */
class drawseg_t {
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

/** A vissprite_t is a thing that will be drawn during a refresh. */
class vissprite_t {
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

/**
 * Sprites are patches with a special naming convention so they can be
 * recognized by R_InitSprites. Rotation zero means all views look the same.
 * (`rotate` is memset to -1 during init, hence Int not Boolean.)
 */
class spriteframe_t {
    // If false use 0 for any position.
    // Note: as eight entries are available, we might as well insert the same name eight times.
    var rotate = -1

    // Lump to use for view angles 0-7.
    val lump = IntArray(8)

    // Flip bit (1 = flip) to use for view angles 0-7.
    val flip = IntArray(8)
}

/** A sprite definition: a number of animation frames. */
class spritedef_t {
    var numframes = 0
    var spriteframes: Array<spriteframe_t?> = emptyArray()
}

/**
 * Now what is a visplane, anyway?
 * top/bottom are C byte[SCREENWIDTH] flanked by pad bytes that absorb the
 * deliberate writes at x == minx-1 and x == maxx+1; PadArray keeps that trick.
 */
class PadArray {
    private val a = IntArray(SCREENWIDTH + 2)
    operator fun get(i: Int): Int = a[i + 1]
    operator fun set(i: Int, v: Int) { a[i + 1] = v }
    fun fill(v: Int) = a.fill(v)
}

class visplane_t {
    var height: fixed_t = 0
    var picnum = 0
    var lightlevel = 0
    var minx = 0
    var maxx = 0

    // Here lies the rub for all dynamic resize/change of resolution.
    val top = PadArray()
    val bottom = PadArray()
}
