
package doom.engine.rendering

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BOXBOTTOM
import doom.engine.geometry.BOXLEFT
import doom.engine.geometry.BOXRIGHT
import doom.engine.geometry.BOXTOP
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FixedPoint
import doom.engine.world.BspNode
import doom.engine.world.MapLine
import doom.engine.world.MapSegment
import doom.engine.world.MapSide
import doom.engine.world.NF_SUBSECTOR
import doom.engine.world.Sector
import doom.engine.world.Subsector
import doom.engine.world.nodes
import doom.engine.world.numsubsectors
import doom.engine.world.segs
import doom.engine.world.subsectors

internal var DoomEngineCore.curline: MapSegment?
    get() = stateBspRenderer.curline
    set(value) { stateBspRenderer.curline = value }
internal var DoomEngineCore.sidedef: MapSide?
    get() = stateBspRenderer.sidedef
    set(value) { stateBspRenderer.sidedef = value }
internal var DoomEngineCore.linedef: MapLine?
    get() = stateBspRenderer.linedef
    set(value) { stateBspRenderer.linedef = value }
internal var DoomEngineCore.frontsector: Sector?
    get() = stateBspRenderer.frontsector
    set(value) { stateBspRenderer.frontsector = value }
internal var DoomEngineCore.backsector: Sector?
    get() = stateBspRenderer.backsector
    set(value) { stateBspRenderer.backsector = value }

internal val DoomEngineCore.drawsegs
    get() = stateBspRenderer.drawsegs
internal var DoomEngineCore.dsP
    get() = stateBspRenderer.dsP
    set(value) { stateBspRenderer.dsP = value }


internal fun DoomEngineCore.rClearDrawSegs() {
    dsP = 0
}

internal const val MAXSEGS = 32

internal var DoomEngineCore.newend
    get() = stateBspRenderer.newend
    set(value) { stateBspRenderer.newend = value }
internal val DoomEngineCore.solidsegs
    get() = stateBspRenderer.solidsegs

internal fun DoomEngineCore.rClipSolidWallSegment(first: Int, last: Int) {
    var next: Int
    var start: Int

    start = 0
    while (solidsegs[start].last < first - 1)
        start++

    if (first < solidsegs[start].first) {
        if (last < solidsegs[start].first - 1) {
            rStoreWallRange(first, last)
            next = newend
            newend++

            while (next != start) {
                solidsegs[next].first = solidsegs[next - 1].first
                solidsegs[next].last = solidsegs[next - 1].last
                next--
            }
            solidsegs[next].first = first
            solidsegs[next].last = last
            return
        }

        rStoreWallRange(first, solidsegs[start].first - 1)
        solidsegs[start].first = first
    }

    if (last <= solidsegs[start].last)
        return

    next = start
    var crunch = false
    while (last >= solidsegs[next + 1].first - 1) {
        rStoreWallRange(solidsegs[next].last + 1, solidsegs[next + 1].first - 1)
        next++

        if (last <= solidsegs[next].last) {
            solidsegs[start].last = solidsegs[next].last
            crunch = true
            break
        }
    }

    if (!crunch) {
        rStoreWallRange(solidsegs[next].last + 1, last)
        solidsegs[start].last = last
    }

    if (next == start) {
        return
    }

    while (next != newend) {
        next++
        start++
        solidsegs[start].first = solidsegs[next].first
        solidsegs[start].last = solidsegs[next].last
    }

    newend = start + 1
}

internal fun DoomEngineCore.rClipPassWallSegment(first: Int, last: Int) {
    var start: Int

    start = 0
    while (solidsegs[start].last < first - 1)
        start++

    if (first < solidsegs[start].first) {
        if (last < solidsegs[start].first - 1) {
            rStoreWallRange(first, last)
            return
        }

        rStoreWallRange(first, solidsegs[start].first - 1)
    }

    if (last <= solidsegs[start].last)
        return

    while (last >= solidsegs[start + 1].first - 1) {
        rStoreWallRange(solidsegs[start].last + 1, solidsegs[start + 1].first - 1)
        start++

        if (last <= solidsegs[start].last)
            return
    }

    rStoreWallRange(solidsegs[start].last + 1, last)
}

internal fun DoomEngineCore.rClearClipSegs() {
    solidsegs[0].first = -0x7fffffff
    solidsegs[0].last = -1
    solidsegs[1].first = viewwidth
    solidsegs[1].last = 0x7fffffff
    newend = 2
}

internal fun DoomEngineCore.rAddLine(line: MapSegment) {
    val x1: Int
    val x2: Int
    var angle1: BinaryAngle
    var angle2: BinaryAngle
    val span: BinaryAngle
    var tspan: BinaryAngle

    curline = line

    angle1 = rPointToAngle(line.v1.x, line.v1.y)
    angle2 = rPointToAngle(line.v2.x, line.v2.y)

    span = angle1 - angle2

    if (span >= ANG180)
        return

    rwAngle1 = angle1.toInt()
    angle1 -= viewangle
    angle2 -= viewangle

    tspan = angle1 + clipangle
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        if (tspan >= span)
            return

        angle1 = clipangle
    }
    tspan = clipangle - angle2
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        if (tspan >= span)
            return
        angle2 = 0u - clipangle
    }

    angle1 = (angle1 + ANG90) shr ANGLETOFINESHIFT
    angle2 = (angle2 + ANG90) shr ANGLETOFINESHIFT
    x1 = viewangletox[angle1.toInt()]
    x2 = viewangletox[angle2.toInt()]

    if (x1 == x2)
        return

    backsector = line.backsector

    if (backsector == null) {
        rClipSolidWallSegment(x1, x2 - 1)
        return
    }

    if (backsector!!.ceilingheight <= frontsector!!.floorheight
        || backsector!!.floorheight >= frontsector!!.ceilingheight
    ) {
        rClipSolidWallSegment(x1, x2 - 1)
        return
    }

    if (backsector!!.ceilingheight != frontsector!!.ceilingheight
        || backsector!!.floorheight != frontsector!!.floorheight
    ) {
        rClipPassWallSegment(x1, x2 - 1)
        return
    }

    if (backsector!!.ceilingpic == frontsector!!.ceilingpic
        && backsector!!.floorpic == frontsector!!.floorpic
        && backsector!!.lightlevel == frontsector!!.lightlevel
        && curline!!.sidedef!!.midtexture == 0
    ) {
        return
    }

    rClipPassWallSegment(x1, x2 - 1)
}

internal val DoomEngineCore.checkcoord
    get() = stateBspRenderer.checkcoord

internal fun DoomEngineCore.rCheckBBox(bspcoord: IntArray): Boolean {
    val boxx: Int
    val boxy: Int
    val boxpos: Int

    val x1: FixedPoint
    val y1: FixedPoint
    val x2: FixedPoint
    val y2: FixedPoint

    var angle1: BinaryAngle
    var angle2: BinaryAngle
    val span: BinaryAngle
    var tspan: BinaryAngle

    var start: Int

    val sx1: Int
    var sx2: Int

    if (viewx <= bspcoord[BOXLEFT])
        boxx = 0
    else if (viewx < bspcoord[BOXRIGHT])
        boxx = 1
    else
        boxx = 2

    if (viewy >= bspcoord[BOXTOP])
        boxy = 0
    else if (viewy > bspcoord[BOXBOTTOM])
        boxy = 1
    else
        boxy = 2

    boxpos = (boxy shl 2) + boxx
    if (boxpos == 5)
        return true

    x1 = bspcoord[checkcoord[boxpos][0]]
    y1 = bspcoord[checkcoord[boxpos][1]]
    x2 = bspcoord[checkcoord[boxpos][2]]
    y2 = bspcoord[checkcoord[boxpos][3]]

    angle1 = rPointToAngle(x1, y1) - viewangle
    angle2 = rPointToAngle(x2, y2) - viewangle

    span = angle1 - angle2

    if (span >= ANG180)
        return true

    tspan = angle1 + clipangle

    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        if (tspan >= span)
            return false

        angle1 = clipangle
    }
    tspan = clipangle - angle2
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        if (tspan >= span)
            return false

        angle2 = 0u - clipangle
    }

    angle1 = (angle1 + ANG90) shr ANGLETOFINESHIFT
    angle2 = (angle2 + ANG90) shr ANGLETOFINESHIFT
    sx1 = viewangletox[angle1.toInt()]
    sx2 = viewangletox[angle2.toInt()]

    if (sx1 == sx2)
        return false
    sx2--

    start = 0
    while (solidsegs[start].last < sx2)
        start++

    if (sx1 >= solidsegs[start].first
        && sx2 <= solidsegs[start].last
    ) {
        return false
    }

    return true
}

internal fun DoomEngineCore.rSubsector(num: Int) {
    var count: Int
    var line: Int
    val sub: Subsector

    if (num >= numsubsectors)
        iError("R_Subsector: ss $num with numss = $numsubsectors")

    sscount++
    sub = subsectors[num]
    frontsector = sub.sector
    count = sub.numlines
    line = sub.firstline

    if (frontsector!!.floorheight < viewz) {
        floorplane = rFindPlane(frontsector!!.floorheight,
            frontsector!!.floorpic,
            frontsector!!.lightlevel)
    } else
        floorplane = -1

    if (frontsector!!.ceilingheight > viewz
        || frontsector!!.ceilingpic == skyflatnum
    ) {
        ceilingplane = rFindPlane(frontsector!!.ceilingheight,
            frontsector!!.ceilingpic,
            frontsector!!.lightlevel)
    } else
        ceilingplane = -1

    rAddSprites(frontsector!!)

    while (count != 0) {
        count--
        rAddLine(segs[line])
        line++
    }
}

internal fun DoomEngineCore.rRenderBSPNode(bspnum: Int) {
    val bsp: BspNode
    val side: Int

    if ((bspnum and NF_SUBSECTOR) != 0) {
        if (bspnum == -1)
            rSubsector(0)
        else
            rSubsector(bspnum and NF_SUBSECTOR.inv())
        return
    }

    bsp = nodes[bspnum]

    side = rPointOnSide(viewx, viewy, bsp)

    rRenderBSPNode(bsp.children[side])

    if (rCheckBBox(bsp.bbox[side xor 1]))
        rRenderBSPNode(bsp.children[side xor 1])
}
