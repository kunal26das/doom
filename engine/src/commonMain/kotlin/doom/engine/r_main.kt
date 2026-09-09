// Port of linuxdoom-1.10 r_main.c -- rendering main loop and setup functions,
// utility functions (BSP, geometry, trigonometry). See tables.c, too.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

import doom.engine.rendering.ScenePasses
import doom.engine.rendering.SceneRenderer
import doom.engine.geometry.BspQueries
import doom.engine.geometry.FixedGeometry
import kotlin.math.abs

// Fineangles in the SCREENWIDTH wide window.
private const val FIELDOFVIEW = 2048

// Lighting constants (r_main.h).
// Now why not 32 levels here?
internal const val LIGHTLEVELS = 16
internal const val LIGHTSEGSHIFT = 4

internal const val MAXLIGHTSCALE = 48
internal const val LIGHTSCALESHIFT = 12
internal const val MAXLIGHTZ = 128
internal const val LIGHTZSHIFT = 20

// Number of diminishing brightness levels.
// There a 0-31, i.e. 32 LUT in the COLORMAP lump.
internal const val NUMCOLORMAPS = 32

internal var DoomEngineCore.viewangleoffset
    get() = stateRendererView.viewangleoffset
    set(value) { stateRendererView.viewangleoffset = value }

internal var DoomEngineCore.validcount
    get() = stateRendererView.validcount
    set(value) { stateRendererView.validcount = value }

internal var DoomEngineCore.fixedcolormap
    get() = stateRendererView.fixedcolormap
    set(value) { stateRendererView.fixedcolormap = value }

internal var DoomEngineCore.centerx
    get() = stateRendererView.centerx
    set(value) { stateRendererView.centerx = value }
internal var DoomEngineCore.centery
    get() = stateRendererView.centery
    set(value) { stateRendererView.centery = value }

internal var DoomEngineCore.centerxfrac: fixed_t
    get() = stateRendererView.centerxfrac
    set(value) { stateRendererView.centerxfrac = value }
internal var DoomEngineCore.centeryfrac: fixed_t
    get() = stateRendererView.centeryfrac
    set(value) { stateRendererView.centeryfrac = value }
internal var DoomEngineCore.projection: fixed_t
    get() = stateRendererView.projection
    set(value) { stateRendererView.projection = value }

internal var DoomEngineCore.framecount
    get() = stateRendererView.framecount
    set(value) { stateRendererView.framecount = value }

internal var DoomEngineCore.sscount
    get() = stateRendererView.sscount
    set(value) { stateRendererView.sscount = value }
internal var DoomEngineCore.linecount
    get() = stateRendererView.linecount
    set(value) { stateRendererView.linecount = value }
internal var DoomEngineCore.loopcount
    get() = stateRendererView.loopcount
    set(value) { stateRendererView.loopcount = value }

internal var DoomEngineCore.viewx: fixed_t
    get() = stateRendererView.viewx
    set(value) { stateRendererView.viewx = value }
internal var DoomEngineCore.viewy: fixed_t
    get() = stateRendererView.viewy
    set(value) { stateRendererView.viewy = value }
internal var DoomEngineCore.viewz: fixed_t
    get() = stateRendererView.viewz
    set(value) { stateRendererView.viewz = value }

internal var DoomEngineCore.viewangle: angle_t
    get() = stateRendererView.viewangle
    set(value) { stateRendererView.viewangle = value }

internal var DoomEngineCore.viewcos: fixed_t
    get() = stateRendererView.viewcos
    set(value) { stateRendererView.viewcos = value }
internal var DoomEngineCore.viewsin: fixed_t
    get() = stateRendererView.viewsin
    set(value) { stateRendererView.viewsin = value }

internal var DoomEngineCore.viewplayer: player_t?
    get() = stateRendererView.viewplayer
    set(value) { stateRendererView.viewplayer = value }

internal var DoomEngineCore.detailshift
    get() = stateRendererView.detailshift
    set(value) { stateRendererView.detailshift = value }

internal var DoomEngineCore.clipangle: angle_t
    get() = stateRendererView.clipangle
    set(value) { stateRendererView.clipangle = value }

internal val DoomEngineCore.viewangletox
    get() = stateRendererView.viewangletox

internal val DoomEngineCore.xtoviewangle
    get() = stateRendererView.xtoviewangle

// fixed_t* finecosine = &finesine[FINEANGLES/4];
// -- the alias object lives in gen/TablesGen.kt.

internal val DoomEngineCore.scalelight
    get() = stateRendererView.scalelight
internal val DoomEngineCore.scalelightfixed
    get() = stateRendererView.scalelightfixed
internal val DoomEngineCore.zlight
    get() = stateRendererView.zlight

internal var DoomEngineCore.extralight
    get() = stateRendererView.extralight
    set(value) { stateRendererView.extralight = value }

internal var DoomEngineCore.colfunc: () -> Unit
    get() = stateRendererView.colfunc
    set(value) { stateRendererView.colfunc = value }
internal var DoomEngineCore.basecolfunc: () -> Unit
    get() = stateRendererView.basecolfunc
    set(value) { stateRendererView.basecolfunc = value }
internal var DoomEngineCore.fuzzcolfunc: () -> Unit
    get() = stateRendererView.fuzzcolfunc
    set(value) { stateRendererView.fuzzcolfunc = value }
internal var DoomEngineCore.transcolfunc: () -> Unit
    get() = stateRendererView.transcolfunc
    set(value) { stateRendererView.transcolfunc = value }
internal var DoomEngineCore.spanfunc: () -> Unit
    get() = stateRendererView.spanfunc
    set(value) { stateRendererView.spanfunc = value }

//
// R_AddPointToBox
// Expand a given bbox
// so that it encloses a given point.
//
internal fun DoomEngineCore.R_AddPointToBox(x: Int, y: Int, box: IntArray) {
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
internal fun DoomEngineCore.R_PointOnSide(x: fixed_t, y: fixed_t, node: node_t): Int =
    FixedGeometry.sideOfNode(x, y, node)

internal fun DoomEngineCore.R_PointOnSegSide(x: fixed_t, y: fixed_t, line: seg_t): Int =
    FixedGeometry.sideOfSegment(x, y, line)

//
// R_PointToAngle
// To get a global angle from cartesian coordinates,
//  the coordinates are flipped until they are in
//  the first octant of the coordinate system, then
//  the y (<=x) is scaled and divided by x to get a
//  tangent (slope) value which is looked up in the
//  tantoangle[] table.
//
internal fun DoomEngineCore.R_PointToAngle(x: fixed_t, y: fixed_t): angle_t =
    FixedGeometry.angleBetween(viewx, viewy, x, y)

internal fun DoomEngineCore.R_PointToAngle2(x1: fixed_t, y1: fixed_t, x2: fixed_t, y2: fixed_t): angle_t =
    FixedGeometry.angleBetween(x1, y1, x2, y2)

internal fun DoomEngineCore.R_PointToDist(x: fixed_t, y: fixed_t): fixed_t =
    FixedGeometry.distanceBetween(viewx, viewy, x, y)

//
// R_InitPointToAngle
//
internal fun DoomEngineCore.R_InitPointToAngle() {
    // UNUSED - now getting from tables.c
}

//
// R_ScaleFromGlobalAngle
// Returns the texture mapping scale
//  for the current line (horizontal span)
//  at the given angle.
// rw_distance must be calculated first.
//
internal fun DoomEngineCore.R_ScaleFromGlobalAngle(visangle: angle_t): fixed_t {
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
internal fun DoomEngineCore.R_InitTables() {
    // UNUSED: now getting from tables.c
}

//
// R_InitTextureMapping
//
internal fun DoomEngineCore.R_InitTextureMapping() {
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

internal fun DoomEngineCore.R_InitLightTables() {
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

internal var DoomEngineCore.setsizeneeded
    get() = stateRendererView.setsizeneeded
    set(value) { stateRendererView.setsizeneeded = value }
internal var DoomEngineCore.setblocks
    get() = stateRendererView.setblocks
    set(value) { stateRendererView.setblocks = value }
internal var DoomEngineCore.setdetail
    get() = stateRendererView.setdetail
    set(value) { stateRendererView.setdetail = value }

internal fun DoomEngineCore.R_SetViewSize(blocks: Int, detail: Int) {
    setsizeneeded = true
    setblocks = blocks
    setdetail = detail
}

//
// R_ExecuteSetViewSize
//
internal fun DoomEngineCore.R_ExecuteSetViewSize() {
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
        basecolfunc = { R_DrawColumn() }
        colfunc = basecolfunc
        fuzzcolfunc = { R_DrawFuzzColumn() }
        transcolfunc = { R_DrawTranslatedColumn() }
        spanfunc = { R_DrawSpan() }
    } else {
        basecolfunc = { R_DrawColumnLow() }
        colfunc = basecolfunc
        fuzzcolfunc = { R_DrawFuzzColumn() }
        transcolfunc = { R_DrawTranslatedColumn() }
        spanfunc = { R_DrawSpanLow() }
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
internal fun DoomEngineCore.R_Init() {
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
internal fun DoomEngineCore.R_PointInSubsector(x: fixed_t, y: fixed_t): subsector_t =
    BspQueries.subsectorAt(x, y, nodes, subsectors, numnodes)

//
// R_SetupFrame
//
internal fun DoomEngineCore.R_SetupFrame(player: player_t) {
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
internal fun DoomEngineCore.R_RenderPlayerView(player: player_t) = sceneRenderer.render(player)

internal fun createSceneRenderer(core: DoomEngineCore): SceneRenderer<player_t> = SceneRenderer(
    object : ScenePasses<player_t> {
        override fun prepare(player: player_t) = with(core) {
            R_SetupFrame(player)
            R_ClearClipSegs()
            R_ClearDrawSegs()
            R_ClearPlanes()
            R_ClearSprites()
        }
        override fun drawWorld() = core.R_RenderBSPNode(core.numnodes - 1)
        override fun drawPlanes() = core.R_DrawPlanes()
        override fun drawMasked() = core.R_DrawMasked()
    },
    core.tickScheduler::pollInputs,
)
