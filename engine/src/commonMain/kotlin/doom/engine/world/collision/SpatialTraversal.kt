
package doom.engine.world.collision

import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MF_NOBLOCKMAP
import doom.engine.gameplay.actors.MF_NOSECTOR
import doom.engine.geometry.BOXBOTTOM
import doom.engine.geometry.BOXLEFT
import doom.engine.geometry.BOXRIGHT
import doom.engine.geometry.BOXTOP
import doom.engine.geometry.BspQueries
import doom.engine.geometry.DividingLine
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.ST_HORIZONTAL
import doom.engine.rendering.ST_NEGATIVE
import doom.engine.rendering.ST_POSITIVE
import doom.engine.rendering.ST_VERTICAL
import doom.engine.rendering.validcount
import doom.engine.resources.MAXINT
import doom.engine.world.MAPBLOCKSHIFT
import doom.engine.world.MAPBLOCKSIZE
import doom.engine.world.MAPBTOFRAC
import doom.engine.world.MapLine
import doom.engine.world.Subsector
import doom.engine.world.blocklinks
import doom.engine.world.blockmap
import doom.engine.world.blockmaplump
import doom.engine.world.bmapheight
import doom.engine.world.bmaporgx
import doom.engine.world.bmaporgy
import doom.engine.world.bmapwidth
import doom.engine.world.lines
import doom.engine.world.nodes
import doom.engine.world.numnodes
import doom.engine.world.subsectors

import kotlin.math.abs

internal const val MAXINTERCEPTS = 128

internal const val PT_ADDLINES = 1
internal const val PT_ADDTHINGS = 2
internal const val PT_EARLYOUT = 4

internal fun DoomEngineCore.pAproxDistance(xDistance: FixedPoint, yDistance: FixedPoint): FixedPoint {
    val dx = abs(xDistance)
    val dy = abs(yDistance)
    if (dx < dy)
        return dx + dy - (dx shr 1)
    return dx + dy - (dy shr 1)
}

internal fun DoomEngineCore.pPointOnLineSide(x: FixedPoint, y: FixedPoint, line: MapLine): Int {
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

    val left = fixedMul(line.dy shr FRACBITS, dx)
    val right = fixedMul(dy, line.dx shr FRACBITS)

    if (right < left)
        return 0
    return 1
}

internal fun DoomEngineCore.pBoxOnLineSide(tmbox: IntArray, ld: MapLine): Int {
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
            p1 = pPointOnLineSide(tmbox[BOXLEFT], tmbox[BOXTOP], ld)
            p2 = pPointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXBOTTOM], ld)
        }

        ST_NEGATIVE -> {
            p1 = pPointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXTOP], ld)
            p2 = pPointOnLineSide(tmbox[BOXLEFT], tmbox[BOXBOTTOM], ld)
        }
    }

    if (p1 == p2)
        return p1
    return -1
}

internal fun DoomEngineCore.pPointOnDivlineSide(x: FixedPoint, y: FixedPoint, line: DividingLine): Int {
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

    if ((line.dy xor line.dx xor dx xor dy) and (1 shl 31) != 0) {
        if ((line.dy xor dx) and (1 shl 31) != 0)
            return 1
        return 0
    }

    val left = fixedMul(line.dy shr 8, dx shr 8)
    val right = fixedMul(dy shr 8, line.dx shr 8)

    if (right < left)
        return 0
    return 1
}

internal fun DoomEngineCore.pMakeDivline(li: MapLine, dl: DividingLine) {
    dl.x = li.v1.x
    dl.y = li.v1.y
    dl.dx = li.dx
    dl.dy = li.dy
}

internal fun DoomEngineCore.pInterceptVector(v2: DividingLine, v1: DividingLine): FixedPoint {
    val den = fixedMul(v1.dy shr 8, v2.dx) - fixedMul(v1.dx shr 8, v2.dy)

    if (den == 0)
        return 0

    val num =
        fixedMul((v1.x - v2.x) shr 8, v1.dy) +
        fixedMul((v2.y - v1.y) shr 8, v1.dx)

    val frac = fixedDiv(num, den)

    return frac
}

internal var DoomEngineCore.opentop: FixedPoint
    get() = stateTraversal.opentop
    set(value) { stateTraversal.opentop = value }
internal var DoomEngineCore.openbottom: FixedPoint
    get() = stateTraversal.openbottom
    set(value) { stateTraversal.openbottom = value }
internal var DoomEngineCore.openrange: FixedPoint
    get() = stateTraversal.openrange
    set(value) { stateTraversal.openrange = value }
internal var DoomEngineCore.lowfloor: FixedPoint
    get() = stateTraversal.lowfloor
    set(value) { stateTraversal.lowfloor = value }

internal fun DoomEngineCore.pLineOpening(linedef: MapLine) {
    if (linedef.sidenum[1] == -1) {
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


internal fun DoomEngineCore.pUnsetThingPosition(thing: Actor) {
    if (thing.flags and MF_NOSECTOR == 0) {
        if (thing.snext != null)
            thing.snext!!.sprev = thing.sprev

        if (thing.sprev != null)
            thing.sprev!!.snext = thing.snext
        else
            thing.subsector!!.sector!!.thinglist = thing.snext
    }

    if (thing.flags and MF_NOBLOCKMAP == 0) {
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

internal fun DoomEngineCore.pSetThingPosition(thing: Actor) {
    val ss = findSubsector(thing.x, thing.y)
    thing.subsector = ss

    if (thing.flags and MF_NOSECTOR == 0) {
        val sec = ss.sector!!

        thing.sprev = null
        thing.snext = sec.thinglist

        if (sec.thinglist != null)
            sec.thinglist!!.sprev = thing

        sec.thinglist = thing
    }

    if (thing.flags and MF_NOBLOCKMAP == 0) {
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
            thing.bnext = null
            thing.bprev = null
        }
    }
}


internal fun DoomEngineCore.pBlockLinesIterator(x: Int, y: Int, func: (MapLine) -> Boolean): Boolean {
    if (x < 0
        || y < 0
        || x >= bmapwidth
        || y >= bmapheight
    ) {
        return true
    }

    var offset = y * bmapwidth + x

    offset = blockmaplump[blockmap + offset]

    var list = offset
    while (blockmaplump[list] != -1) {
        val ld = lines[blockmaplump[list]]

        if (ld.validcount == validcount) {
            list++
            continue
        }

        ld.validcount = validcount

        if (!func(ld))
            return false
        list++
    }
    return true
}

internal fun DoomEngineCore.pBlockThingsIterator(x: Int, y: Int, func: (Actor) -> Boolean): Boolean {
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
internal var DoomEngineCore.interceptP
    get() = stateTraversal.interceptP
    set(value) { stateTraversal.interceptP = value }

internal val DoomEngineCore.trace
    get() = stateTraversal.trace
internal var DoomEngineCore.earlyout
    get() = stateTraversal.earlyout
    set(value) { stateTraversal.earlyout = value }
internal var DoomEngineCore.ptflags
    get() = stateTraversal.ptflags
    set(value) { stateTraversal.ptflags = value }

internal fun DoomEngineCore.pitAddLineIntercepts(ld: MapLine): Boolean {
    val s1: Int
    val s2: Int

    if (trace.dx > FRACUNIT * 16
        || trace.dy > FRACUNIT * 16
        || trace.dx < -FRACUNIT * 16
        || trace.dy < -FRACUNIT * 16
    ) {
        s1 = pPointOnDivlineSide(ld.v1.x, ld.v1.y, trace)
        s2 = pPointOnDivlineSide(ld.v2.x, ld.v2.y, trace)
    } else {
        s1 = pPointOnLineSide(trace.x, trace.y, ld)
        s2 = pPointOnLineSide(trace.x + trace.dx, trace.y + trace.dy, ld)
    }

    if (s1 == s2)
        return true

    val dl = DividingLine()
    pMakeDivline(ld, dl)
    val frac = pInterceptVector(trace, dl)

    if (frac < 0)
        return true

    if (earlyout
        && frac < FRACUNIT
        && ld.backsector == null
    ) {
        return false
    }

    intercepts[interceptP].frac = frac
    intercepts[interceptP].isaline = true
    intercepts[interceptP].line = ld
    interceptP++

    return true
}

internal fun DoomEngineCore.pitAddThingIntercepts(thing: Actor): Boolean {
    val x1: FixedPoint
    val y1: FixedPoint
    val x2: FixedPoint
    val y2: FixedPoint

    val tracepositive = (trace.dx xor trace.dy) > 0

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

    val s1 = pPointOnDivlineSide(x1, y1, trace)
    val s2 = pPointOnDivlineSide(x2, y2, trace)

    if (s1 == s2)
        return true

    val dl = DividingLine()
    dl.x = x1
    dl.y = y1
    dl.dx = x2 - x1
    dl.dy = y2 - y1

    val frac = pInterceptVector(trace, dl)

    if (frac < 0)
        return true

    intercepts[interceptP].frac = frac
    intercepts[interceptP].isaline = false
    intercepts[interceptP].thing = thing
    interceptP++

    return true
}

internal fun DoomEngineCore.pTraverseIntercepts(func: InterceptVisitor, maxfrac: FixedPoint): Boolean {
    var count = interceptP

    var `in`: PathIntercept? = null

    while (count-- > 0) {
        var dist = MAXINT
        for (i in 0 until interceptP) {
            val scan = intercepts[i]
            if (scan.frac < dist) {
                dist = scan.frac
                `in` = scan
            }
        }

        if (dist > maxfrac)
            return true

        if (!func(`in`!!))
            return false

        `in`.frac = MAXINT
    }

    return true
}

internal fun DoomEngineCore.pPathTraverse(
    startX: FixedPoint,
    startY: FixedPoint,
    endX: FixedPoint,
    endY: FixedPoint,
    flags: Int,
    trav: InterceptVisitor,
): Boolean {
    var x1 = startX
    var y1 = startY
    var x2 = endX
    var y2 = endY

    val xstep: FixedPoint
    val ystep: FixedPoint

    var partial: FixedPoint

    val mapxstep: Int
    val mapystep: Int

    earlyout = (flags and PT_EARLYOUT) != 0

    validcount++
    interceptP = 0

    if ((x1 - bmaporgx) and (MAPBLOCKSIZE - 1) == 0)
        x1 += FRACUNIT

    if ((y1 - bmaporgy) and (MAPBLOCKSIZE - 1) == 0)
        y1 += FRACUNIT

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
        ystep = fixedDiv(y2 - y1, abs(x2 - x1))
    } else if (xt2 < xt1) {
        mapxstep = -1
        partial = (x1 shr MAPBTOFRAC) and (FRACUNIT - 1)
        ystep = fixedDiv(y2 - y1, abs(x2 - x1))
    } else {
        mapxstep = 0
        partial = FRACUNIT
        ystep = 256 * FRACUNIT
    }

    var yintercept = (y1 shr MAPBTOFRAC) + fixedMul(partial, ystep)

    if (yt2 > yt1) {
        mapystep = 1
        partial = FRACUNIT - ((y1 shr MAPBTOFRAC) and (FRACUNIT - 1))
        xstep = fixedDiv(x2 - x1, abs(y2 - y1))
    } else if (yt2 < yt1) {
        mapystep = -1
        partial = (y1 shr MAPBTOFRAC) and (FRACUNIT - 1)
        xstep = fixedDiv(x2 - x1, abs(y2 - y1))
    } else {
        mapystep = 0
        partial = FRACUNIT
        xstep = 256 * FRACUNIT
    }
    var xintercept = (x1 shr MAPBTOFRAC) + fixedMul(partial, xstep)

    var mapx = xt1
    var mapy = yt1

    for (count in 0 until 64) {
        if (flags and PT_ADDLINES != 0) {
            if (!pBlockLinesIterator(mapx, mapy, { argument0 -> pitAddLineIntercepts(argument0) }))
                return false
        }

        if (flags and PT_ADDTHINGS != 0) {
            if (!pBlockThingsIterator(mapx, mapy, { argument0 -> pitAddThingIntercepts(argument0) }))
                return false
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
    return pTraverseIntercepts(trav, FRACUNIT)
}

internal fun DoomEngineCore.findSubsector(x: FixedPoint, y: FixedPoint): Subsector =
    BspQueries.subsectorAt(x, y, nodes, subsectors, numnodes)
