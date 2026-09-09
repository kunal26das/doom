// Port of linuxdoom-1.10 p_maputl.c -- movement/collision utility functions,
// as used by function in p_map.c. BLOCKMAP Iterator functions, and some
// PIT_* functions to use for iteration. (Also owns divline_t/intercept_t
// and the traverser machinery from p_local.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.geometry.BspQueries

import kotlin.math.abs


internal const val MAXINTERCEPTS = 128


internal const val PT_ADDLINES = 1
internal const val PT_ADDTHINGS = 2
internal const val PT_EARLYOUT = 4

//
// P_AproxDistance
// Gives an estimation of distance (not exact)
//
internal fun DoomEngineCore.P_AproxDistance(dx: fixed_t, dy: fixed_t): fixed_t {
    val dx = abs(dx)
    val dy = abs(dy)
    if (dx < dy)
        return dx + dy - (dx shr 1)
    return dx + dy - (dy shr 1)
}

//
// P_PointOnLineSide
// Returns 0 or 1
//
internal fun DoomEngineCore.P_PointOnLineSide(x: fixed_t, y: fixed_t, line: line_t): Int {
    if (line.dx == 0) {
        if (x <= line.v1.x)
            return if (line.dy > 0) 1 else 0

        return if (line.dy < 0) 1 else 0
    }
    if (line.dy == 0) {
        if (y <= line.v1.y)
            return if (line.dx < 0) 1 else 0

        return if (line.dx > 0) 1 else 0
    }

    val dx = x - line.v1.x
    val dy = y - line.v1.y

    val left = FixedMul(line.dy shr FRACBITS, dx)
    val right = FixedMul(dy, line.dx shr FRACBITS)

    if (right < left)
        return 0    // front side
    return 1        // back side
}

//
// P_BoxOnLineSide
// Considers the line to be infinite
// Returns side 0 or 1, -1 if box crosses the line.
//
internal fun DoomEngineCore.P_BoxOnLineSide(tmbox: IntArray, ld: line_t): Int {
    var p1 = 0
    var p2 = 0

    when (ld.slopetype) {
        ST_HORIZONTAL -> {
            p1 = if (tmbox[BOXTOP] > ld.v1.y) 1 else 0
            p2 = if (tmbox[BOXBOTTOM] > ld.v1.y) 1 else 0
            if (ld.dx < 0) {
                p1 = p1 xor 1
                p2 = p2 xor 1
            }
        }

        ST_VERTICAL -> {
            p1 = if (tmbox[BOXRIGHT] < ld.v1.x) 1 else 0
            p2 = if (tmbox[BOXLEFT] < ld.v1.x) 1 else 0
            if (ld.dy < 0) {
                p1 = p1 xor 1
                p2 = p2 xor 1
            }
        }

        ST_POSITIVE -> {
            p1 = P_PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXTOP], ld)
            p2 = P_PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXBOTTOM], ld)
        }

        ST_NEGATIVE -> {
            p1 = P_PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXTOP], ld)
            p2 = P_PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXBOTTOM], ld)
        }
    }

    if (p1 == p2)
        return p1
    return -1
}

//
// P_PointOnDivlineSide
// Returns 0 or 1.
//
internal fun DoomEngineCore.P_PointOnDivlineSide(x: fixed_t, y: fixed_t, line: divline_t): Int {
    if (line.dx == 0) {
        if (x <= line.x)
            return if (line.dy > 0) 1 else 0

        return if (line.dy < 0) 1 else 0
    }
    if (line.dy == 0) {
        if (y <= line.y)
            return if (line.dx < 0) 1 else 0

        return if (line.dx > 0) 1 else 0
    }

    val dx = x - line.x
    val dy = y - line.y

    // try to quickly decide by looking at sign bits
    if ((line.dy xor line.dx xor dx xor dy) and (1 shl 31) != 0) {
        if ((line.dy xor dx) and (1 shl 31) != 0)
            return 1    // (left is negative)
        return 0
    }

    val left = FixedMul(line.dy shr 8, dx shr 8)
    val right = FixedMul(dy shr 8, line.dx shr 8)

    if (right < left)
        return 0    // front side
    return 1        // back side
}

//
// P_MakeDivline
//
internal fun DoomEngineCore.P_MakeDivline(li: line_t, dl: divline_t) {
    dl.x = li.v1.x
    dl.y = li.v1.y
    dl.dx = li.dx
    dl.dy = li.dy
}

//
// P_InterceptVector
// Returns the fractional intercept point
// along the first divline.
// This is only called by the addthings
// and addlines traversers.
//
internal fun DoomEngineCore.P_InterceptVector(v2: divline_t, v1: divline_t): fixed_t {
    val den = FixedMul(v1.dy shr 8, v2.dx) - FixedMul(v1.dx shr 8, v2.dy)

    if (den == 0)
        return 0
    //	I_Error ("P_InterceptVector: parallel");

    val num =
        FixedMul((v1.x - v2.x) shr 8, v1.dy) +
        FixedMul((v2.y - v1.y) shr 8, v1.dx)

    val frac = FixedDiv(num, den)

    return frac
}

internal var DoomEngineCore.opentop: fixed_t
    get() = stateTraversal.opentop
    set(value) { stateTraversal.opentop = value }
internal var DoomEngineCore.openbottom: fixed_t
    get() = stateTraversal.openbottom
    set(value) { stateTraversal.openbottom = value }
internal var DoomEngineCore.openrange: fixed_t
    get() = stateTraversal.openrange
    set(value) { stateTraversal.openrange = value }
internal var DoomEngineCore.lowfloor: fixed_t
    get() = stateTraversal.lowfloor
    set(value) { stateTraversal.lowfloor = value }

internal fun DoomEngineCore.P_LineOpening(linedef: line_t) {
    if (linedef.sidenum[1] == -1) {
        // single sided line
        openrange = 0
        return
    }

    val front = linedef.frontsector!!
    val back = linedef.backsector!!

    if (front.ceilingheight < back.ceilingheight)
        opentop = front.ceilingheight
    else
        opentop = back.ceilingheight

    if (front.floorheight > back.floorheight) {
        openbottom = front.floorheight
        lowfloor = back.floorheight
    } else {
        openbottom = back.floorheight
        lowfloor = front.floorheight
    }

    openrange = opentop - openbottom
}

//
// THING POSITION SETTING
//

//
// P_UnsetThingPosition
// Unlinks a thing from block map and sectors.
// On each position change, BLOCKMAP and other
// lookups maintaining lists ot things inside
// these structures need to be updated.
//
internal fun DoomEngineCore.P_UnsetThingPosition(thing: mobj_t) {
    if (thing.flags and MF_NOSECTOR == 0) {
        // inert things don't need to be in blockmap?
        // unlink from subsector
        if (thing.snext != null)
            thing.snext!!.sprev = thing.sprev

        if (thing.sprev != null)
            thing.sprev!!.snext = thing.snext
        else
            thing.subsector!!.sector!!.thinglist = thing.snext
    }

    if (thing.flags and MF_NOBLOCKMAP == 0) {
        // inert things don't need to be in blockmap
        // unlink from block map
        if (thing.bnext != null)
            thing.bnext!!.bprev = thing.bprev

        if (thing.bprev != null)
            thing.bprev!!.bnext = thing.bnext
        else {
            val blockx = (thing.x - bmaporgx) shr MAPBLOCKSHIFT
            val blocky = (thing.y - bmaporgy) shr MAPBLOCKSHIFT

            if (blockx >= 0 && blockx < bmapwidth
                && blocky >= 0 && blocky < bmapheight
            ) {
                blocklinks[blocky * bmapwidth + blockx] = thing.bnext
            }
        }
    }
}

//
// P_SetThingPosition
// Links a thing into both a block and a subsector
// based on it's x y.
// Sets thing->subsector properly
//
internal fun DoomEngineCore.P_SetThingPosition(thing: mobj_t) {
    // link into subsector
    val ss = findSubsector(thing.x, thing.y)
    thing.subsector = ss

    if (thing.flags and MF_NOSECTOR == 0) {
        // invisible things don't go into the sector links
        val sec = ss.sector!!

        thing.sprev = null
        thing.snext = sec.thinglist

        if (sec.thinglist != null)
            sec.thinglist!!.sprev = thing

        sec.thinglist = thing
    }

    // link into blockmap
    if (thing.flags and MF_NOBLOCKMAP == 0) {
        // inert things don't need to be in blockmap
        val blockx = (thing.x - bmaporgx) shr MAPBLOCKSHIFT
        val blocky = (thing.y - bmaporgy) shr MAPBLOCKSHIFT

        if (blockx >= 0
            && blockx < bmapwidth
            && blocky >= 0
            && blocky < bmapheight
        ) {
            val link = blocky * bmapwidth + blockx
            thing.bprev = null
            thing.bnext = blocklinks[link]
            if (blocklinks[link] != null)
                blocklinks[link]!!.bprev = thing

            blocklinks[link] = thing
        } else {
            // thing is off the map
            thing.bnext = null
            thing.bprev = null
        }
    }
}

//
// BLOCK MAP ITERATORS
// For each line/thing in the given mapblock,
// call the passed PIT_* function.
// If the function returns false,
// exit with false without checking anything else.
//

//
// P_BlockLinesIterator
// The validcount flags are used to avoid checking lines
// that are marked in multiple mapblocks,
// so increment validcount before the first call
// to P_BlockLinesIterator, then make one or more calls
// to it.
//
internal fun DoomEngineCore.P_BlockLinesIterator(x: Int, y: Int, func: (line_t) -> Boolean): Boolean {
    if (x < 0
        || y < 0
        || x >= bmapwidth
        || y >= bmapheight
    ) {
        return true
    }

    var offset = y * bmapwidth + x

    offset = blockmaplump[blockmap + offset].toInt()

    var list = offset
    while (blockmaplump[list].toInt() != -1) {
        val ld = lines[blockmaplump[list].toInt()]

        if (ld.validcount == validcount) {
            list++
            continue    // line has already been checked
        }

        ld.validcount = validcount

        if (!func(ld))
            return false
        list++
    }
    return true    // everything was checked
}

//
// P_BlockThingsIterator
//
internal fun DoomEngineCore.P_BlockThingsIterator(x: Int, y: Int, func: (mobj_t) -> Boolean): Boolean {
    if (x < 0
        || y < 0
        || x >= bmapwidth
        || y >= bmapheight
    ) {
        return true
    }

    var mobj = blocklinks[y * bmapwidth + x]
    while (mobj != null) {
        if (!func(mobj))
            return false
        mobj = mobj.bnext
    }
    return true
}

internal val DoomEngineCore.intercepts
    get() = stateTraversal.intercepts
internal var DoomEngineCore.intercept_p
    get() = stateTraversal.intercept_p
    set(value) { stateTraversal.intercept_p = value }

internal val DoomEngineCore.trace
    get() = stateTraversal.trace
internal var DoomEngineCore.earlyout
    get() = stateTraversal.earlyout
    set(value) { stateTraversal.earlyout = value }
internal var DoomEngineCore.ptflags
    get() = stateTraversal.ptflags
    set(value) { stateTraversal.ptflags = value }

//
// PIT_AddLineIntercepts.
// Looks for lines in the given block
// that intercept the given trace
// to add to the intercepts list.
//
// A line is crossed if its endpoints
// are on opposite sides of the trace.
// Returns true if earlyout and a solid line hit.
//
internal fun DoomEngineCore.PIT_AddLineIntercepts(ld: line_t): Boolean {
    val s1: Int
    val s2: Int

    // avoid precision problems with two routines
    if (trace.dx > FRACUNIT * 16
        || trace.dy > FRACUNIT * 16
        || trace.dx < -FRACUNIT * 16
        || trace.dy < -FRACUNIT * 16
    ) {
        s1 = P_PointOnDivlineSide(ld.v1.x, ld.v1.y, trace)
        s2 = P_PointOnDivlineSide(ld.v2.x, ld.v2.y, trace)
    } else {
        s1 = P_PointOnLineSide(trace.x, trace.y, ld)
        s2 = P_PointOnLineSide(trace.x + trace.dx, trace.y + trace.dy, ld)
    }

    if (s1 == s2)
        return true    // line isn't crossed

    // hit the line
    val dl = divline_t()
    P_MakeDivline(ld, dl)
    val frac = P_InterceptVector(trace, dl)

    if (frac < 0)
        return true    // behind source

    // try to early out the check
    if (earlyout
        && frac < FRACUNIT
        && ld.backsector == null
    ) {
        return false    // stop checking
    }

    intercepts[intercept_p].frac = frac
    intercepts[intercept_p].isaline = true
    intercepts[intercept_p].line = ld
    intercept_p++

    return true    // continue
}

//
// PIT_AddThingIntercepts
//
internal fun DoomEngineCore.PIT_AddThingIntercepts(thing: mobj_t): Boolean {
    val x1: fixed_t
    val y1: fixed_t
    val x2: fixed_t
    val y2: fixed_t

    val tracepositive = (trace.dx xor trace.dy) > 0

    // check a corner to corner crossection for hit
    if (tracepositive) {
        x1 = thing.x - thing.radius
        y1 = thing.y + thing.radius

        x2 = thing.x + thing.radius
        y2 = thing.y - thing.radius
    } else {
        x1 = thing.x - thing.radius
        y1 = thing.y - thing.radius

        x2 = thing.x + thing.radius
        y2 = thing.y + thing.radius
    }

    val s1 = P_PointOnDivlineSide(x1, y1, trace)
    val s2 = P_PointOnDivlineSide(x2, y2, trace)

    if (s1 == s2)
        return true    // line isn't crossed

    val dl = divline_t()
    dl.x = x1
    dl.y = y1
    dl.dx = x2 - x1
    dl.dy = y2 - y1

    val frac = P_InterceptVector(trace, dl)

    if (frac < 0)
        return true    // behind source

    intercepts[intercept_p].frac = frac
    intercepts[intercept_p].isaline = false
    intercepts[intercept_p].thing = thing
    intercept_p++

    return true    // keep going
}

//
// P_TraverseIntercepts
// Returns true if the traverser function returns true
// for all lines.
//
internal fun DoomEngineCore.P_TraverseIntercepts(func: traverser_t, maxfrac: fixed_t): Boolean {
    var count = intercept_p

    var `in`: intercept_t? = null    // shut up compiler warning

    while (count-- > 0) {
        var dist = MAXINT
        for (i in 0 until intercept_p) {
            val scan = intercepts[i]
            if (scan.frac < dist) {
                dist = scan.frac
                `in` = scan
            }
        }

        if (dist > maxfrac)
            return true    // checked everything in range

        if (!func(`in`!!))
            return false    // don't bother going farther

        `in`.frac = MAXINT
    }

    return true    // everything was traversed
}

//
// P_PathTraverse
// Traces a line from x1,y1 to x2,y2,
// calling the traverser function for each.
// Returns true if the traverser function returns true
// for all lines.
//
internal fun DoomEngineCore.P_PathTraverse(
    x1: fixed_t,
    y1: fixed_t,
    x2: fixed_t,
    y2: fixed_t,
    flags: Int,
    trav: traverser_t,
): Boolean {
    var x1 = x1
    var y1 = y1
    var x2 = x2
    var y2 = y2

    val xstep: fixed_t
    val ystep: fixed_t

    var partial: fixed_t

    val mapxstep: Int
    val mapystep: Int

    earlyout = (flags and PT_EARLYOUT) != 0

    validcount++
    intercept_p = 0

    if ((x1 - bmaporgx) and (MAPBLOCKSIZE - 1) == 0)
        x1 += FRACUNIT    // don't side exactly on a line

    if ((y1 - bmaporgy) and (MAPBLOCKSIZE - 1) == 0)
        y1 += FRACUNIT    // don't side exactly on a line

    trace.x = x1
    trace.y = y1
    trace.dx = x2 - x1
    trace.dy = y2 - y1

    x1 -= bmaporgx
    y1 -= bmaporgy
    val xt1 = x1 shr MAPBLOCKSHIFT
    val yt1 = y1 shr MAPBLOCKSHIFT

    x2 -= bmaporgx
    y2 -= bmaporgy
    val xt2 = x2 shr MAPBLOCKSHIFT
    val yt2 = y2 shr MAPBLOCKSHIFT

    if (xt2 > xt1) {
        mapxstep = 1
        partial = FRACUNIT - ((x1 shr MAPBTOFRAC) and (FRACUNIT - 1))
        ystep = FixedDiv(y2 - y1, abs(x2 - x1))
    } else if (xt2 < xt1) {
        mapxstep = -1
        partial = (x1 shr MAPBTOFRAC) and (FRACUNIT - 1)
        ystep = FixedDiv(y2 - y1, abs(x2 - x1))
    } else {
        mapxstep = 0
        partial = FRACUNIT
        ystep = 256 * FRACUNIT
    }

    var yintercept = (y1 shr MAPBTOFRAC) + FixedMul(partial, ystep)

    if (yt2 > yt1) {
        mapystep = 1
        partial = FRACUNIT - ((y1 shr MAPBTOFRAC) and (FRACUNIT - 1))
        xstep = FixedDiv(x2 - x1, abs(y2 - y1))
    } else if (yt2 < yt1) {
        mapystep = -1
        partial = (y1 shr MAPBTOFRAC) and (FRACUNIT - 1)
        xstep = FixedDiv(x2 - x1, abs(y2 - y1))
    } else {
        mapystep = 0
        partial = FRACUNIT
        xstep = 256 * FRACUNIT
    }
    var xintercept = (x1 shr MAPBTOFRAC) + FixedMul(partial, xstep)

    // Step through map blocks.
    // Count is present to prevent a round off error
    // from skipping the break.
    var mapx = xt1
    var mapy = yt1

    for (count in 0 until 64) {
        if (flags and PT_ADDLINES != 0) {
            if (!P_BlockLinesIterator(mapx, mapy, { argument0 -> PIT_AddLineIntercepts(argument0) }))
                return false    // early out
        }

        if (flags and PT_ADDTHINGS != 0) {
            if (!P_BlockThingsIterator(mapx, mapy, { argument0 -> PIT_AddThingIntercepts(argument0) }))
                return false    // early out
        }

        if (mapx == xt2
            && mapy == yt2
        ) {
            break
        }

        if ((yintercept shr FRACBITS) == mapy) {
            yintercept += ystep
            mapx += mapxstep
        } else if ((xintercept shr FRACBITS) == mapx) {
            xintercept += xstep
            mapy += mapystep
        }
    }
    // go through the sorted list
    return P_TraverseIntercepts(trav, FRACUNIT)
}


internal fun DoomEngineCore.findSubsector(x: fixed_t, y: fixed_t): subsector_t =
    BspQueries.subsectorAt(x, y, nodes, subsectors, numnodes)
