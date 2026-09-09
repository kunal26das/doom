// Port of linuxdoom-1.10 p_setup.c -- do all the WAD I/O, get map description,
// set up initial state and misc. LUTs.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class WorldGeometryState {
    var numvertexes = 0

    var vertexes: Array<vertex_t> = emptyArray()

    var numsegs = 0

    var segs: Array<seg_t> = emptyArray()

    var numsectors = 0

    var sectors: Array<sector_t> = emptyArray()

    var numsubsectors = 0

    var subsectors: Array<subsector_t> = emptyArray()

    var numnodes = 0

    var nodes: Array<node_t> = emptyArray()

    var numlines = 0

    var lines: Array<line_t> = emptyArray()

    var numsides = 0

    var sides: Array<side_t> = emptyArray()

    var bmapwidth = 0

    var bmapheight = 0

    var blockmap = 0

    var blockmaplump: IntArray = IntArray(0)

    var bmaporgx: fixed_t = 0

    var bmaporgy: fixed_t = 0

    var blocklinks: Array<mobj_t?> = emptyArray()

    var rejectmatrix: ByteArray = ByteArray(0)

    val deathmatchstarts by lazy(LazyThreadSafetyMode.NONE) { Array(MAX_DEATHMATCH_STARTS) { mapthing_t() } }

    var deathmatch_p = 0

    val playerstarts by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { mapthing_t() } }
}
