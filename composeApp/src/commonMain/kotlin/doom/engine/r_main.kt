// Port of linuxdoom-1.10 r_main.c -- rendering main loop and setup functions,
// utility functions (BSP, geometry, trigonometry). See tables.c, too.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

import kotlin.math.abs

// Fineangles in the SCREENWIDTH wide window.
private const val FIELDOFVIEW = 2048

// Lighting constants (r_main.h).
// Now why not 32 levels here?
const val LIGHTLEVELS = 16
const val LIGHTSEGSHIFT = 4

const val MAXLIGHTSCALE = 48
const val LIGHTSCALESHIFT = 12
const val MAXLIGHTZ = 128
const val LIGHTZSHIFT = 20

// Number of diminishing brightness levels.
// There a 0-31, i.e. 32 LUT in the COLORMAP lump.
const val NUMCOLORMAPS = 32

var viewangleoffset = 0

// increment every time a check is made
var validcount = 1

// lighttable_t*: byte offset into colormaps; -1 == NULL (C uses the 0 pointer).
var fixedcolormap = -1

var centerx = 0
var centery = 0

var centerxfrac: fixed_t = 0
var centeryfrac: fixed_t = 0
var projection: fixed_t = 0

// just for profiling purposes
var framecount = 0

var sscount = 0
var linecount = 0
var loopcount = 0

var viewx: fixed_t = 0
var viewy: fixed_t = 0
var viewz: fixed_t = 0

var viewangle: angle_t = 0u

var viewcos: fixed_t = 0
var viewsin: fixed_t = 0

var viewplayer: player_t? = null

// 0 = high, 1 = low
var detailshift = 0

//
// precalculated math tables
//
var clipangle: angle_t = 0u

// The viewangletox[viewangle + FINEANGLES/4] lookup
// maps the visible view angles to screen X coordinates,
// flattening the arc to a flat projection plane.
// There will be many angles mapped to the same X.
val viewangletox = IntArray(FINEANGLES / 2)

// The xtoviewangleangle[] table maps a screen pixel
// to the lowest viewangle that maps back to x ranges
// from clipangle to -clipangle.
// (angle_t values stored as Int bit patterns; use .toUInt() at reads.)
val xtoviewangle = IntArray(SCREENWIDTH + 1)

// fixed_t* finecosine = &finesine[FINEANGLES/4];
// -- the alias object lives in gen/TablesGen.kt.

// lighttable_t* tables --> colormap byte offsets into colormaps.
val scalelight = Array(LIGHTLEVELS) { IntArray(MAXLIGHTSCALE) }
val scalelightfixed = IntArray(MAXLIGHTSCALE)
val zlight = Array(LIGHTLEVELS) { IntArray(MAXLIGHTZ) }

// bumped light from gun blasts
var extralight = 0

lateinit var colfunc: () -> Unit
lateinit var basecolfunc: () -> Unit
lateinit var fuzzcolfunc: () -> Unit
lateinit var transcolfunc: () -> Unit
lateinit var spanfunc: () -> Unit

//
// R_AddPointToBox
// Expand a given bbox
// so that it encloses a given point.
//
fun R_AddPointToBox(x: Int, y: Int, box: IntArray) {
    if (x < box[BOXLEFT])
        box[BOXLEFT] = x
    if (x > box[BOXRIGHT])
        box[BOXRIGHT] = x
    if (y < box[BOXBOTTOM])
        box[BOXBOTTOM] = y
    if (y > box[BOXTOP])
        box[BOXTOP] = y
}

//
// R_PointOnSide
// Traverse BSP (sub) tree,
//  check point against partition plane.
// Returns side 0 (front) or 1 (back).
//
fun R_PointOnSide(x: fixed_t, y: fixed_t, node: node_t): Int {
    val dx: fixed_t
    val dy: fixed_t
    val left: fixed_t
    val right: fixed_t

    if (node.dx == 0) {
        if (x <= node.x)
            return if (node.dy > 0) 1 else 0

        return if (node.dy < 0) 1 else 0
    }
    if (node.dy == 0) {
        if (y <= node.y)
            return if (node.dx < 0) 1 else 0

        return if (node.dx > 0) 1 else 0
    }

    dx = (x - node.x)
    dy = (y - node.y)

    // Try to quickly decide by looking at sign bits.
    if (((node.dy xor node.dx xor dx xor dy) and 0x80000000.toInt()) != 0) {
        if (((node.dy xor dx) and 0x80000000.toInt()) != 0) {
            // (left is negative)
            return 1
        }
        return 0
    }

    left = FixedMul(node.dy shr FRACBITS, dx)
    right = FixedMul(dy, node.dx shr FRACBITS)

    if (right < left) {
        // front side
        return 0
    }
    // back side
    return 1
}

fun R_PointOnSegSide(x: fixed_t, y: fixed_t, line: seg_t): Int {
    val lx: fixed_t
    val ly: fixed_t
    val ldx: fixed_t
    val ldy: fixed_t
    val dx: fixed_t
    val dy: fixed_t
    val left: fixed_t
    val right: fixed_t

    lx = line.v1.x
    ly = line.v1.y

    ldx = line.v2.x - lx
    ldy = line.v2.y - ly

    if (ldx == 0) {
        if (x <= lx)
            return if (ldy > 0) 1 else 0

        return if (ldy < 0) 1 else 0
    }
    if (ldy == 0) {
        if (y <= ly)
            return if (ldx < 0) 1 else 0

        return if (ldx > 0) 1 else 0
    }

    dx = (x - lx)
    dy = (y - ly)

    // Try to quickly decide by looking at sign bits.
    if (((ldy xor ldx xor dx xor dy) and 0x80000000.toInt()) != 0) {
        if (((ldy xor dx) and 0x80000000.toInt()) != 0) {
            // (left is negative)
            return 1
        }
        return 0
    }

    left = FixedMul(ldy shr FRACBITS, dx)
    right = FixedMul(dy, ldx shr FRACBITS)

    if (right < left) {
        // front side
        return 0
    }
    // back side
    return 1
}

//
// R_PointToAngle
// To get a global angle from cartesian coordinates,
//  the coordinates are flipped until they are in
//  the first octant of the coordinate system, then
//  the y (<=x) is scaled and divided by x to get a
//  tangent (slope) value which is looked up in the
//  tantoangle[] table.
//
fun R_PointToAngle(x: fixed_t, y: fixed_t): angle_t {
    var x = x
    var y = y

    x -= viewx
    y -= viewy

    if ((x == 0) && (y == 0))
        return 0u

    if (x >= 0) {
        // x >=0
        if (y >= 0) {
            // y>= 0

            if (x > y) {
                // octant 0
                return tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
            } else {
                // octant 1
                return ANG90 - 1u - tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
            }
        } else {
            // y<0
            y = -y

            if (x > y) {
                // octant 8
                return 0u - tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
            } else {
                // octant 7
                return ANG270 + tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
            }
        }
    } else {
        // x<0
        x = -x

        if (y >= 0) {
            // y>= 0
            if (x > y) {
                // octant 3
                return ANG180 - 1u - tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
            } else {
                // octant 2
                return ANG90 + tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
            }
        } else {
            // y<0
            y = -y

            if (x > y) {
                // octant 4
                return ANG180 + tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
            } else {
                // octant 5
                return ANG270 - 1u - tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
            }
        }
    }
}

fun R_PointToAngle2(x1: fixed_t, y1: fixed_t, x2: fixed_t, y2: fixed_t): angle_t {
    viewx = x1
    viewy = y1

    return R_PointToAngle(x2, y2)
}

fun R_PointToDist(x: fixed_t, y: fixed_t): fixed_t {
    val angle: Int
    var dx: fixed_t
    var dy: fixed_t
    val temp: fixed_t
    val dist: fixed_t

    dx = abs(x - viewx)
    dy = abs(y - viewy)

    if (dy > dx) {
        temp = dx
        dx = dy
        dy = temp
    }

    angle = ((tantoangle[FixedDiv(dy, dx) shr DBITS].toUInt() + ANG90) shr ANGLETOFINESHIFT).toInt()

    // use as cosine
    dist = FixedDiv(dx, finesine[angle])

    return dist
}

//
// R_InitPointToAngle
//
fun R_InitPointToAngle() {
    // UNUSED - now getting from tables.c
}

//
// R_ScaleFromGlobalAngle
// Returns the texture mapping scale
//  for the current line (horizontal span)
//  at the given angle.
// rw_distance must be calculated first.
//
fun R_ScaleFromGlobalAngle(visangle: angle_t): fixed_t {
    var scale: fixed_t
    // anglea/angleb: vanilla declares these int, whose signed >> reads out of
    // bounds when angleb >= ANG180; angle_t (unsigned) typing follows
    // chocolate-doom and keeps the finesine index in range.
    val anglea: angle_t
    val angleb: angle_t
    val sinea: Int
    val sineb: Int
    val num: fixed_t
    val den: Int

    anglea = ANG90 + (visangle - viewangle)
    angleb = ANG90 + (visangle - rw_normalangle)

    // both sines are allways positive
    sinea = finesine[(anglea shr ANGLETOFINESHIFT).toInt()]
    sineb = finesine[(angleb shr ANGLETOFINESHIFT).toInt()]
    num = FixedMul(projection, sineb) shl detailshift
    den = FixedMul(rw_distance, sinea)

    if (den > num shr 16) {
        scale = FixedDiv(num, den)

        if (scale > 64 * FRACUNIT)
            scale = 64 * FRACUNIT
        else if (scale < 256)
            scale = 256
    } else
        scale = 64 * FRACUNIT

    return scale
}

//
// R_InitTables
//
fun R_InitTables() {
    // UNUSED: now getting from tables.c
}

//
// R_InitTextureMapping
//
fun R_InitTextureMapping() {
    var i: Int
    var x: Int
    var t: Int
    val focallength: fixed_t

    // Use tangent table to generate viewangletox:
    //  viewangletox will give the next greatest x
    //  after the view angle.
    //
    // Calc focallength
    //  so FIELDOFVIEW angles covers SCREENWIDTH.
    focallength = FixedDiv(centerxfrac,
        finetangent[FINEANGLES / 4 + FIELDOFVIEW / 2])

    i = 0
    while (i < FINEANGLES / 2) {
        if (finetangent[i] > FRACUNIT * 2)
            t = -1
        else if (finetangent[i] < -FRACUNIT * 2)
            t = viewwidth + 1
        else {
            t = FixedMul(finetangent[i], focallength)
            t = (centerxfrac - t + FRACUNIT - 1) shr FRACBITS

            if (t < -1)
                t = -1
            else if (t > viewwidth + 1)
                t = viewwidth + 1
        }
        viewangletox[i] = t
        i++
    }

    // Scan viewangletox[] to generate xtoviewangle[]:
    //  xtoviewangle will give the smallest view angle
    //  that maps to x.
    x = 0
    while (x <= viewwidth) {
        i = 0
        while (viewangletox[i] > x)
            i++
        xtoviewangle[x] = (i shl ANGLETOFINESHIFT) - ANG90.toInt()
        x++
    }

    // Take out the fencepost cases from viewangletox.
    i = 0
    while (i < FINEANGLES / 2) {
        t = FixedMul(finetangent[i], focallength)
        t = centerx - t

        if (viewangletox[i] == -1)
            viewangletox[i] = 0
        else if (viewangletox[i] == viewwidth + 1)
            viewangletox[i] = viewwidth
        i++
    }

    clipangle = xtoviewangle[0].toUInt()
}

//
// R_InitLightTables
// Only inits the zlight table,
//  because the scalelight table changes with view size.
//
private const val DISTMAP = 2

fun R_InitLightTables() {
    var level: Int
    var startmap: Int
    var scale: Int

    // Calculate the light levels to use
    //  for each level / distance combination.
    for (i in 0 until LIGHTLEVELS) {
        startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS
        for (j in 0 until MAXLIGHTZ) {
            scale = FixedDiv(SCREENWIDTH / 2 * FRACUNIT, (j + 1) shl LIGHTZSHIFT)
            scale = scale shr LIGHTSCALESHIFT
            level = startmap - scale / DISTMAP

            if (level < 0)
                level = 0

            if (level >= NUMCOLORMAPS)
                level = NUMCOLORMAPS - 1

            zlight[i][j] = level * 256  // colormaps + level*256
        }
    }
}

//
// R_SetViewSize
// Do not really change anything here,
//  because it might be in the middle of a refresh.
// The change will take effect next refresh.
//
var setsizeneeded = false
var setblocks = 0
var setdetail = 0

fun R_SetViewSize(blocks: Int, detail: Int) {
    setsizeneeded = true
    setblocks = blocks
    setdetail = detail
}

//
// R_ExecuteSetViewSize
//
fun R_ExecuteSetViewSize() {
    var cosadj: fixed_t
    var dy: fixed_t
    var level: Int
    var startmap: Int

    setsizeneeded = false

    if (setblocks == 11) {
        scaledviewwidth = SCREENWIDTH
        viewheight = SCREENHEIGHT
    } else {
        scaledviewwidth = setblocks * 32
        viewheight = (setblocks * 168 / 10) and 7.inv()
    }

    detailshift = setdetail
    viewwidth = scaledviewwidth shr detailshift

    centery = viewheight / 2
    centerx = viewwidth / 2
    centerxfrac = centerx shl FRACBITS
    centeryfrac = centery shl FRACBITS
    projection = centerxfrac

    if (detailshift == 0) {
        basecolfunc = ::R_DrawColumn
        colfunc = basecolfunc
        fuzzcolfunc = ::R_DrawFuzzColumn
        transcolfunc = ::R_DrawTranslatedColumn
        spanfunc = ::R_DrawSpan
    } else {
        basecolfunc = ::R_DrawColumnLow
        colfunc = basecolfunc
        fuzzcolfunc = ::R_DrawFuzzColumn
        transcolfunc = ::R_DrawTranslatedColumn
        spanfunc = ::R_DrawSpanLow
    }

    R_InitBuffer(scaledviewwidth, viewheight)

    R_InitTextureMapping()

    // psprite scales
    pspritescale = FRACUNIT * viewwidth / SCREENWIDTH
    pspriteiscale = FRACUNIT * SCREENWIDTH / viewwidth

    // thing clipping
    for (i in 0 until viewwidth)
        screenheightarray[i] = viewheight.toShort()

    // planes
    for (i in 0 until viewheight) {
        dy = ((i - viewheight / 2) shl FRACBITS) + FRACUNIT / 2
        dy = abs(dy)
        yslope[i] = FixedDiv((viewwidth shl detailshift) / 2 * FRACUNIT, dy)
    }

    for (i in 0 until viewwidth) {
        cosadj = abs(finecosine[(xtoviewangle[i].toUInt() shr ANGLETOFINESHIFT).toInt()])
        distscale[i] = FixedDiv(FRACUNIT, cosadj)
    }

    // Calculate the light levels to use
    //  for each level / scale combination.
    for (i in 0 until LIGHTLEVELS) {
        startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS
        for (j in 0 until MAXLIGHTSCALE) {
            level = startmap - j * SCREENWIDTH / (viewwidth shl detailshift) / DISTMAP

            if (level < 0)
                level = 0

            if (level >= NUMCOLORMAPS)
                level = NUMCOLORMAPS - 1

            scalelight[i][j] = level * 256  // colormaps + level*256
        }
    }
}

//
// R_Init
//
fun R_Init() {
    R_InitData()
    print("\nR_InitData")
    R_InitPointToAngle()
    print("\nR_InitPointToAngle")
    R_InitTables()
    // viewwidth / viewheight / detailLevel are set by the defaults
    print("\nR_InitTables")

    R_SetViewSize(screenblocks, detailLevel)
    R_InitPlanes()
    print("\nR_InitPlanes")
    R_InitLightTables()
    print("\nR_InitLightTables")
    R_InitSkyMap()
    print("\nR_InitSkyMap")
    R_InitTranslationTables()
    print("\nR_InitTranslationsTables")

    framecount = 0
}

//
// R_PointInSubsector
//
fun R_PointInSubsector(x: fixed_t, y: fixed_t): subsector_t {
    var node: node_t
    var side: Int
    var nodenum: Int

    // single subsector is a special case
    if (numnodes == 0)
        return subsectors[0]

    nodenum = numnodes - 1

    while ((nodenum and NF_SUBSECTOR) == 0) {
        node = nodes[nodenum]
        side = R_PointOnSide(x, y, node)
        nodenum = node.children[side]
    }

    return subsectors[nodenum and NF_SUBSECTOR.inv()]
}

//
// R_SetupFrame
//
fun R_SetupFrame(player: player_t) {
    viewplayer = player
    viewx = player.mo!!.x
    viewy = player.mo!!.y
    viewangle = player.mo!!.angle + viewangleoffset.toUInt()
    extralight = player.extralight

    viewz = player.viewz

    viewsin = finesine[(viewangle shr ANGLETOFINESHIFT).toInt()]
    viewcos = finecosine[(viewangle shr ANGLETOFINESHIFT).toInt()]

    sscount = 0

    if (player.fixedcolormap != 0) {
        // colormaps + player->fixedcolormap*256*sizeof(lighttable_t)
        fixedcolormap = player.fixedcolormap * 256

        walllights = scalelightfixed

        for (i in 0 until MAXLIGHTSCALE)
            scalelightfixed[i] = fixedcolormap
    } else
        fixedcolormap = -1  // NULL (see r_defs.kt pointer conventions)

    framecount++
    validcount++
}

//
// R_RenderView
//
fun R_RenderPlayerView(player: player_t) {
    R_SetupFrame(player)

    // Clear buffers.
    R_ClearClipSegs()
    R_ClearDrawSegs()
    R_ClearPlanes()
    R_ClearSprites()

    // check for new console commands.
    NetUpdate()

    // The head node is the last node output.
    R_RenderBSPNode(numnodes - 1)

    // Check for new console commands.
    NetUpdate()

    R_DrawPlanes()

    // Check for new console commands.
    NetUpdate()

    R_DrawMasked()

    // Check for new console commands.
    NetUpdate()
}
