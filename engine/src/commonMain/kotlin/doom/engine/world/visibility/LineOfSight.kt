
package doom.engine.world.visibility

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.actors.Actor
import doom.engine.geometry.DividingLine
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.validcount
import doom.engine.resources.u8
import doom.engine.world.BspNode
import doom.engine.world.ML_TWOSIDED
import doom.engine.world.NF_SUBSECTOR
import doom.engine.world.nodes
import doom.engine.world.numnodes
import doom.engine.world.numsectors
import doom.engine.world.numsubsectors
import doom.engine.world.rejectmatrix
import doom.engine.world.segs
import doom.engine.world.subsectors

internal var DoomEngineCore.sightzstart: FixedPoint
    get() = statePSight.sightzstart
    set(value) { statePSight.sightzstart = value }
internal var DoomEngineCore.topslope: FixedPoint
    get() = statePSight.topslope
    set(value) { statePSight.topslope = value }
internal var DoomEngineCore.bottomslope: FixedPoint
    get() = statePSight.bottomslope
    set(value) { statePSight.bottomslope = value }

internal val DoomEngineCore.strace
    get() = statePSight.strace
internal var DoomEngineCore.t2x: FixedPoint
    get() = statePSight.t2x
    set(value) { statePSight.t2x = value }
internal var DoomEngineCore.t2y: FixedPoint
    get() = statePSight.t2y
    set(value) { statePSight.t2y = value }

internal val DoomEngineCore.sightcounts
    get() = statePSight.sightcounts

internal fun DoomEngineCore.pDivlineSide(x: FixedPoint, y: FixedPoint, node: DividingLine): Int {
    if (node.dx == 0) {
        if (x == node.x)
            return 2

        if (x <= node.x)
            return if (node.dy > 0) 1 else 0

        return if (node.dy < 0) 1 else 0
    }

    if (node.dy == 0) {
        if (x == node.y)
            return 2

        if (y <= node.y)
            return if (node.dx < 0) 1 else 0

        return if (node.dx > 0) 1 else 0
    }

    val dx = x - node.x
    val dy = y - node.y

    val left = (node.dy shr FRACBITS) * (dx shr FRACBITS)
    val right = (dy shr FRACBITS) * (node.dx shr FRACBITS)

    if (right < left)
        return 0

    if (left == right)
        return 2
    return 1
}

internal fun DoomEngineCore.pDivlineSide(x: FixedPoint, y: FixedPoint, node: BspNode): Int {
    if (node.dx == 0) {
        if (x == node.x)
            return 2

        if (x <= node.x)
            return if (node.dy > 0) 1 else 0

        return if (node.dy < 0) 1 else 0
    }

    if (node.dy == 0) {
        if (x == node.y)
            return 2

        if (y <= node.y)
            return if (node.dx < 0) 1 else 0

        return if (node.dx > 0) 1 else 0
    }

    val dx = x - node.x
    val dy = y - node.y

    val left = (node.dy shr FRACBITS) * (dx shr FRACBITS)
    val right = (dy shr FRACBITS) * (node.dx shr FRACBITS)

    if (right < left)
        return 0

    if (left == right)
        return 2
    return 1
}

internal fun DoomEngineCore.pInterceptVector2(v2: DividingLine, v1: DividingLine): FixedPoint {
    val den = fixedMul(v1.dy shr 8, v2.dx) - fixedMul(v1.dx shr 8, v2.dy)

    if (den == 0)
        return 0

    val num = fixedMul((v1.x - v2.x) shr 8, v1.dy) +
        fixedMul((v2.y - v1.y) shr 8, v1.dx)
    val frac = fixedDiv(num, den)

    return frac
}

internal fun DoomEngineCore.pCrossSubsector(num: Int): Boolean {
    if (num >= numsubsectors)
        iError("P_CrossSubsector: ss $num with numss = $numsubsectors")

    val sub = subsectors[num]

    var count = sub.numlines
    var seg = sub.firstline

    while (count != 0) {
        val line = segs[seg].linedef!!

        if (line.validcount == validcount) {
            seg++
            count--
            continue
        }

        line.validcount = validcount

        val v1 = line.v1
        val v2 = line.v2
        var s1 = pDivlineSide(v1.x, v1.y, strace)
        var s2 = pDivlineSide(v2.x, v2.y, strace)

        if (s1 == s2) {
            seg++
            count--
            continue
        }

        val divl = DividingLine()
        divl.x = v1.x
        divl.y = v1.y
        divl.dx = v2.x - v1.x
        divl.dy = v2.y - v1.y
        s1 = pDivlineSide(strace.x, strace.y, divl)
        s2 = pDivlineSide(t2x, t2y, divl)

        if (s1 == s2) {
            seg++
            count--
            continue
        }

        if (line.flags and ML_TWOSIDED == 0)
            return false

        val front = segs[seg].frontsector!!
        val back = segs[seg].backsector!!

        if (front.floorheight == back.floorheight
            && front.ceilingheight == back.ceilingheight
        ) {
            seg++
            count--
            continue
        }

        val opentop: FixedPoint
        if (front.ceilingheight < back.ceilingheight)
            opentop = front.ceilingheight
        else
            opentop = back.ceilingheight

        val openbottom: FixedPoint
        if (front.floorheight > back.floorheight)
            openbottom = front.floorheight
        else
            openbottom = back.floorheight

        if (openbottom >= opentop)
            return false

        val frac = pInterceptVector2(strace, divl)

        if (front.floorheight != back.floorheight) {
            val slope = fixedDiv(openbottom - sightzstart, frac)
            if (slope > bottomslope)
                bottomslope = slope
        }

        if (front.ceilingheight != back.ceilingheight) {
            val slope = fixedDiv(opentop - sightzstart, frac)
            if (slope < topslope)
                topslope = slope
        }

        if (topslope <= bottomslope)
            return false

        seg++
        count--
    }
    return true
}

internal fun DoomEngineCore.pCrossBSPNode(bspnum: Int): Boolean {
    if (bspnum and NF_SUBSECTOR != 0) {
        if (bspnum == -1)
            return pCrossSubsector(0)
        else
            return pCrossSubsector(bspnum and NF_SUBSECTOR.inv())
    }

    val bsp = nodes[bspnum]

    var side = pDivlineSide(strace.x, strace.y, bsp)
    if (side == 2)
        side = 0

    if (!pCrossBSPNode(bsp.children[side]))
        return false

    if (side == pDivlineSide(t2x, t2y, bsp)) {
        return true
    }

    return pCrossBSPNode(bsp.children[side xor 1])
}

internal fun DoomEngineCore.pCheckSight(t1: Actor, t2: Actor): Boolean {

    val s1 = t1.subsector!!.sector!!.index
    val s2 = t2.subsector!!.sector!!.index
    val pnum = s1 * numsectors + s2
    val bytenum = pnum shr 3
    val bitnum = 1 shl (pnum and 7)

    if (rejectmatrix.u8(bytenum) and bitnum != 0) {
        sightcounts[0]++

        return false
    }

    sightcounts[1]++

    validcount++

    sightzstart = t1.z + t1.height - (t1.height shr 2)
    topslope = (t2.z + t2.height) - sightzstart
    bottomslope = (t2.z) - sightzstart

    strace.x = t1.x
    strace.y = t1.y
    t2x = t2.x
    t2y = t2.y
    strace.dx = t2.x - t1.x
    strace.dy = t2.y - t1.y

    return pCrossBSPNode(numnodes - 1)
}
