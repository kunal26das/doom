// Port of linuxdoom-1.10 r_bsp.c -- BSP traversal, handling of LineSegs
// for rendering.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

var curline: seg_t? = null
var sidedef: side_t? = null
var linedef: line_t? = null
var frontsector: sector_t? = null
var backsector: sector_t? = null

val drawsegs = Array(MAXDRAWSEGS) { drawseg_t() }
var ds_p = 0  // drawseg_t* --> index into drawsegs

// typedef void (*drawfunc_t) (int start, int stop); (r_bsp.h -- unused)

//
// R_ClearDrawSegs
//
fun R_ClearDrawSegs() {
    ds_p = 0  // ds_p = drawsegs
}

//
// ClipWallSegment
// Clips the given range of columns
// and includes it in the new clip list.
//
class cliprange_t {
    var first = 0
    var last = 0
}

private const val MAXSEGS = 32

// newend is one past the last valid seg
var newend = 0  // cliprange_t* --> index into solidsegs
val solidsegs = Array(MAXSEGS) { cliprange_t() }

//
// R_ClipSolidWallSegment
// Does handle solid walls,
//  e.g. single sided LineDefs (middle texture)
//  that entirely block the view.
//
fun R_ClipSolidWallSegment(first: Int, last: Int) {
    var next: Int
    var start: Int

    // Find the first range that touches the range
    //  (adjacent pixels are touching).
    start = 0  // start = solidsegs
    while (solidsegs[start].last < first - 1)
        start++

    if (first < solidsegs[start].first) {
        if (last < solidsegs[start].first - 1) {
            // Post is entirely visible (above start),
            //  so insert a new clippost.
            R_StoreWallRange(first, last)
            next = newend
            newend++

            while (next != start) {
                // *next = *(next-1);
                solidsegs[next].first = solidsegs[next - 1].first
                solidsegs[next].last = solidsegs[next - 1].last
                next--
            }
            solidsegs[next].first = first
            solidsegs[next].last = last
            return
        }

        // There is a fragment above *start.
        R_StoreWallRange(first, solidsegs[start].first - 1)
        // Now adjust the clip size.
        solidsegs[start].first = first
    }

    // Bottom contained in start?
    if (last <= solidsegs[start].last)
        return

    next = start
    var crunch = false
    while (last >= solidsegs[next + 1].first - 1) {
        // There is a fragment between two posts.
        R_StoreWallRange(solidsegs[next].last + 1, solidsegs[next + 1].first - 1)
        next++

        if (last <= solidsegs[next].last) {
            // Bottom is contained in next.
            // Adjust the clip size.
            solidsegs[start].last = solidsegs[next].last
            crunch = true  // goto crunch
            break
        }
    }

    if (!crunch) {
        // There is a fragment after *next.
        R_StoreWallRange(solidsegs[next].last + 1, last)
        // Adjust the clip size.
        solidsegs[start].last = last
    }

    // Remove start+1 to next from the clip list,
    // because start now covers their area.
    // crunch:
    if (next == start) {
        // Post just extended past the bottom of one post.
        return
    }

    // while (next++ != newend)
    //     *++start = *next;
    // (note vanilla's final iteration copies the one-past-end element too)
    while (next != newend) {
        next++
        // Remove a post.
        start++
        solidsegs[start].first = solidsegs[next].first
        solidsegs[start].last = solidsegs[next].last
    }

    newend = start + 1
}

//
// R_ClipPassWallSegment
// Clips the given range of columns,
//  but does not includes it in the clip list.
// Does handle windows,
//  e.g. LineDefs with upper and lower texture.
//
fun R_ClipPassWallSegment(first: Int, last: Int) {
    var start: Int

    // Find the first range that touches the range
    //  (adjacent pixels are touching).
    start = 0  // start = solidsegs
    while (solidsegs[start].last < first - 1)
        start++

    if (first < solidsegs[start].first) {
        if (last < solidsegs[start].first - 1) {
            // Post is entirely visible (above start).
            R_StoreWallRange(first, last)
            return
        }

        // There is a fragment above *start.
        R_StoreWallRange(first, solidsegs[start].first - 1)
    }

    // Bottom contained in start?
    if (last <= solidsegs[start].last)
        return

    while (last >= solidsegs[start + 1].first - 1) {
        // There is a fragment between two posts.
        R_StoreWallRange(solidsegs[start].last + 1, solidsegs[start + 1].first - 1)
        start++

        if (last <= solidsegs[start].last)
            return
    }

    // There is a fragment after *next.
    R_StoreWallRange(solidsegs[start].last + 1, last)
}

//
// R_ClearClipSegs
//
fun R_ClearClipSegs() {
    solidsegs[0].first = -0x7fffffff
    solidsegs[0].last = -1
    solidsegs[1].first = viewwidth
    solidsegs[1].last = 0x7fffffff
    newend = 2  // newend = solidsegs+2
}

//
// R_AddLine
// Clips the given segment
// and adds any visible pieces to the line list.
//
fun R_AddLine(line: seg_t) {
    val x1: Int
    val x2: Int
    var angle1: angle_t
    var angle2: angle_t
    val span: angle_t
    var tspan: angle_t

    curline = line

    // OPTIMIZE: quickly reject orthogonal back sides.
    angle1 = R_PointToAngle(line.v1.x, line.v1.y)
    angle2 = R_PointToAngle(line.v2.x, line.v2.y)

    // Clip to view edges.
    // OPTIMIZE: make constant out of 2*clipangle (FIELDOFVIEW).
    span = angle1 - angle2

    // Back side? I.e. backface culling?
    if (span >= ANG180)
        return

    // Global angle needed by segcalc.
    rw_angle1 = angle1.toInt()
    angle1 -= viewangle
    angle2 -= viewangle

    tspan = angle1 + clipangle
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        // Totally off the left edge?
        if (tspan >= span)
            return

        angle1 = clipangle
    }
    tspan = clipangle - angle2
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        // Totally off the left edge?
        if (tspan >= span)
            return
        angle2 = 0u - clipangle
    }

    // The seg is in the view range,
    // but not necessarily visible.
    angle1 = (angle1 + ANG90) shr ANGLETOFINESHIFT
    angle2 = (angle2 + ANG90) shr ANGLETOFINESHIFT
    x1 = viewangletox[angle1.toInt()]
    x2 = viewangletox[angle2.toInt()]

    // Does not cross a pixel?
    if (x1 == x2)
        return

    backsector = line.backsector

    // Single sided line?
    if (backsector == null) {
        // goto clipsolid
        R_ClipSolidWallSegment(x1, x2 - 1)
        return
    }

    // Closed door.
    if (backsector!!.ceilingheight <= frontsector!!.floorheight
        || backsector!!.floorheight >= frontsector!!.ceilingheight
    ) {
        // goto clipsolid
        R_ClipSolidWallSegment(x1, x2 - 1)
        return
    }

    // Window.
    if (backsector!!.ceilingheight != frontsector!!.ceilingheight
        || backsector!!.floorheight != frontsector!!.floorheight
    ) {
        // goto clippass
        R_ClipPassWallSegment(x1, x2 - 1)
        return
    }

    // Reject empty lines used for triggers
    //  and special events.
    // Identical floor and ceiling on both sides,
    // identical light levels on both sides,
    // and no middle texture.
    if (backsector!!.ceilingpic == frontsector!!.ceilingpic
        && backsector!!.floorpic == frontsector!!.floorpic
        && backsector!!.lightlevel == frontsector!!.lightlevel
        && curline!!.sidedef!!.midtexture == 0
    ) {
        return
    }

    // clippass:
    R_ClipPassWallSegment(x1, x2 - 1)
}

//
// R_CheckBBox
// Checks BSP node/subtree bounding box.
// Returns true
//  if some part of the bbox might be visible.
//
val checkcoord = arrayOf(
    intArrayOf(3, 0, 2, 1),
    intArrayOf(3, 0, 2, 0),
    intArrayOf(3, 1, 2, 0),
    intArrayOf(0, 0, 0, 0),
    intArrayOf(2, 0, 2, 1),
    intArrayOf(0, 0, 0, 0),
    intArrayOf(3, 1, 3, 0),
    intArrayOf(0, 0, 0, 0),
    intArrayOf(2, 0, 3, 1),
    intArrayOf(2, 1, 3, 1),
    intArrayOf(2, 1, 3, 0),
    intArrayOf(0, 0, 0, 0),
)

fun R_CheckBBox(bspcoord: IntArray): Boolean {
    val boxx: Int
    val boxy: Int
    val boxpos: Int

    val x1: fixed_t
    val y1: fixed_t
    val x2: fixed_t
    val y2: fixed_t

    var angle1: angle_t
    var angle2: angle_t
    val span: angle_t
    var tspan: angle_t

    var start: Int

    val sx1: Int
    var sx2: Int

    // Find the corners of the box
    // that define the edges from current viewpoint.
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

    // check clip list for an open space
    angle1 = R_PointToAngle(x1, y1) - viewangle
    angle2 = R_PointToAngle(x2, y2) - viewangle

    span = angle1 - angle2

    // Sitting on a line?
    if (span >= ANG180)
        return true

    tspan = angle1 + clipangle

    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        // Totally off the left edge?
        if (tspan >= span)
            return false

        angle1 = clipangle
    }
    tspan = clipangle - angle2
    if (tspan > 2u * clipangle) {
        tspan -= 2u * clipangle

        // Totally off the left edge?
        if (tspan >= span)
            return false

        angle2 = 0u - clipangle
    }

    // Find the first clippost
    //  that touches the source post
    //  (adjacent pixels are touching).
    angle1 = (angle1 + ANG90) shr ANGLETOFINESHIFT
    angle2 = (angle2 + ANG90) shr ANGLETOFINESHIFT
    sx1 = viewangletox[angle1.toInt()]
    sx2 = viewangletox[angle2.toInt()]

    // Does not cross a pixel.
    if (sx1 == sx2)
        return false
    sx2--

    start = 0  // start = solidsegs
    while (solidsegs[start].last < sx2)
        start++

    if (sx1 >= solidsegs[start].first
        && sx2 <= solidsegs[start].last
    ) {
        // The clippost contains the new span.
        return false
    }

    return true
}

//
// R_Subsector
// Determine floor/ceiling planes.
// Add sprites of things in sector.
// Draw one or more line segments.
//
fun R_Subsector(num: Int) {
    var count: Int
    var line: Int  // seg_t* --> index into segs
    val sub: subsector_t

    // #ifdef RANGECHECK
    if (num >= numsubsectors)
        I_Error("R_Subsector: ss $num with numss = $numsubsectors")
    // #endif

    sscount++
    sub = subsectors[num]
    frontsector = sub.sector
    count = sub.numlines
    line = sub.firstline  // line = &segs[sub->firstline]

    if (frontsector!!.floorheight < viewz) {
        floorplane = R_FindPlane(frontsector!!.floorheight,
            frontsector!!.floorpic,
            frontsector!!.lightlevel)
    } else
        floorplane = -1  // NULL

    if (frontsector!!.ceilingheight > viewz
        || frontsector!!.ceilingpic == skyflatnum
    ) {
        ceilingplane = R_FindPlane(frontsector!!.ceilingheight,
            frontsector!!.ceilingpic,
            frontsector!!.lightlevel)
    } else
        ceilingplane = -1  // NULL

    R_AddSprites(frontsector!!)

    while (count != 0) {  // while (count--)
        count--
        R_AddLine(segs[line])
        line++
    }
}

//
// RenderBSPNode
// Renders all subsectors below a given node,
//  traversing subtree recursively.
// Just call with BSP root.
fun R_RenderBSPNode(bspnum: Int) {
    val bsp: node_t
    val side: Int

    // Found a subsector?
    if ((bspnum and NF_SUBSECTOR) != 0) {
        if (bspnum == -1)
            R_Subsector(0)
        else
            R_Subsector(bspnum and NF_SUBSECTOR.inv())
        return
    }

    bsp = nodes[bspnum]

    // Decide which side the view point is on.
    side = R_PointOnSide(viewx, viewy, bsp)

    // Recursively divide front space.
    R_RenderBSPNode(bsp.children[side])

    // Possibly divide back space.
    if (R_CheckBBox(bsp.bbox[side xor 1]))
        R_RenderBSPNode(bsp.children[side xor 1])
}
