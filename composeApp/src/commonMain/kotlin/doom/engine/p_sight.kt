// Port of linuxdoom-1.10 p_sight.c -- LineOfSight/Visibility checks,
// uses REJECT Lookup Table.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// P_CheckSight
//
var sightzstart: fixed_t = 0        // eye z of looker
var topslope: fixed_t = 0
var bottomslope: fixed_t = 0        // slopes to top and bottom of target

val strace = divline_t()            // from t1 to t2
var t2x: fixed_t = 0
var t2y: fixed_t = 0

val sightcounts = IntArray(2)

//
// P_DivlineSide
// Returns side 0 (front), 1 (back), or 2 (on).
//
fun P_DivlineSide(x: fixed_t, y: fixed_t, node: divline_t): Int {
    if (node.dx == 0) {
        if (x == node.x)
            return 2

        if (x <= node.x)
            return if (node.dy > 0) 1 else 0

        return if (node.dy < 0) 1 else 0
    }

    if (node.dy == 0) {
        if (x == node.y)    // (vanilla bug: tests x, not y)
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
        return 0    // front side

    if (left == right)
        return 2
    return 1        // back side
}

/** C calls P_DivlineSide with `(divline_t *)bsp` -- the node_t starts with the
 *  same four fixed_t fields, so this overload replicates the cast. */
fun P_DivlineSide(x: fixed_t, y: fixed_t, node: node_t): Int {
    if (node.dx == 0) {
        if (x == node.x)
            return 2

        if (x <= node.x)
            return if (node.dy > 0) 1 else 0

        return if (node.dy < 0) 1 else 0
    }

    if (node.dy == 0) {
        if (x == node.y)    // (vanilla bug: tests x, not y)
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
        return 0    // front side

    if (left == right)
        return 2
    return 1        // back side
}

//
// P_InterceptVector2
// Returns the fractional intercept point
// along the first divline.
// This is only called by the addthings and addlines traversers.
//
fun P_InterceptVector2(v2: divline_t, v1: divline_t): fixed_t {
    val den = FixedMul(v1.dy shr 8, v2.dx) - FixedMul(v1.dx shr 8, v2.dy)

    if (den == 0)
        return 0
    //	I_Error ("P_InterceptVector: parallel");

    val num = FixedMul((v1.x - v2.x) shr 8, v1.dy) +
        FixedMul((v2.y - v1.y) shr 8, v1.dx)
    val frac = FixedDiv(num, den)

    return frac
}

//
// P_CrossSubsector
// Returns true
//  if strace crosses the given subsector successfully.
//
fun P_CrossSubsector(num: Int): Boolean {
    // #ifdef RANGECHECK
    if (num >= numsubsectors)
        I_Error("P_CrossSubsector: ss $num with numss = $numsubsectors")
    // #endif

    val sub = subsectors[num]

    // check lines
    var count = sub.numlines
    var seg = sub.firstline    // seg_t* --> index into segs

    while (count != 0) {
        val line = segs[seg].linedef!!

        // allready checked other side?
        if (line.validcount == validcount) {
            seg++
            count--
            continue
        }

        line.validcount = validcount

        val v1 = line.v1
        val v2 = line.v2
        var s1 = P_DivlineSide(v1.x, v1.y, strace)
        var s2 = P_DivlineSide(v2.x, v2.y, strace)

        // line isn't crossed?
        if (s1 == s2) {
            seg++
            count--
            continue
        }

        val divl = divline_t()
        divl.x = v1.x
        divl.y = v1.y
        divl.dx = v2.x - v1.x
        divl.dy = v2.y - v1.y
        s1 = P_DivlineSide(strace.x, strace.y, divl)
        s2 = P_DivlineSide(t2x, t2y, divl)

        // line isn't crossed?
        if (s1 == s2) {
            seg++
            count--
            continue
        }

        // stop because it is not two sided anyway
        // might do this after updating validcount?
        if (line.flags and ML_TWOSIDED == 0)
            return false

        // crosses a two sided line
        val front = segs[seg].frontsector!!
        val back = segs[seg].backsector!!

        // no wall to block sight with?
        if (front.floorheight == back.floorheight
            && front.ceilingheight == back.ceilingheight
        ) {
            seg++
            count--
            continue
        }

        // possible occluder
        // because of ceiling height differences
        val opentop: fixed_t
        if (front.ceilingheight < back.ceilingheight)
            opentop = front.ceilingheight
        else
            opentop = back.ceilingheight

        // because of ceiling height differences
        val openbottom: fixed_t
        if (front.floorheight > back.floorheight)
            openbottom = front.floorheight
        else
            openbottom = back.floorheight

        // quick test for totally closed doors
        if (openbottom >= opentop)
            return false    // stop

        val frac = P_InterceptVector2(strace, divl)

        if (front.floorheight != back.floorheight) {
            val slope = FixedDiv(openbottom - sightzstart, frac)
            if (slope > bottomslope)
                bottomslope = slope
        }

        if (front.ceilingheight != back.ceilingheight) {
            val slope = FixedDiv(opentop - sightzstart, frac)
            if (slope < topslope)
                topslope = slope
        }

        if (topslope <= bottomslope)
            return false    // stop

        seg++
        count--
    }
    // passed the subsector ok
    return true
}

//
// P_CrossBSPNode
// Returns true
//  if strace crosses the given node successfully.
//
fun P_CrossBSPNode(bspnum: Int): Boolean {
    if (bspnum and NF_SUBSECTOR != 0) {
        if (bspnum == -1)
            return P_CrossSubsector(0)
        else
            return P_CrossSubsector(bspnum and NF_SUBSECTOR.inv())
    }

    val bsp = nodes[bspnum]

    // decide which side the start point is on
    var side = P_DivlineSide(strace.x, strace.y, bsp)
    if (side == 2)
        side = 0    // an "on" should cross both sides

    // cross the starting side
    if (!P_CrossBSPNode(bsp.children[side]))
        return false

    // the partition plane is crossed here
    if (side == P_DivlineSide(t2x, t2y, bsp)) {
        // the line doesn't touch the other side
        return true
    }

    // cross the ending side
    return P_CrossBSPNode(bsp.children[side xor 1])
}

//
// P_CheckSight
// Returns true
//  if a straight line between t1 and t2 is unobstructed.
// Uses REJECT.
//
fun P_CheckSight(t1: mobj_t, t2: mobj_t): Boolean {
    // First check for trivial rejection.

    // Determine subsector entries in REJECT table.
    val s1 = t1.subsector!!.sector!!.index    // (t1->subsector->sector - sectors)
    val s2 = t2.subsector!!.sector!!.index    // (t2->subsector->sector - sectors)
    val pnum = s1 * numsectors + s2
    val bytenum = pnum shr 3
    val bitnum = 1 shl (pnum and 7)

    // Check in REJECT table.
    if (rejectmatrix.u8(bytenum) and bitnum != 0) {
        sightcounts[0]++

        // can't possibly be connected
        return false
    }

    // An unobstructed LOS is possible.
    // Now look from eyes of t1 to any part of t2.
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

    // the head node is the last node output
    return P_CrossBSPNode(numnodes - 1)
}
