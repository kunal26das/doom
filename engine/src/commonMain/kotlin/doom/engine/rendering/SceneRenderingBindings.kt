
package doom.engine.rendering

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.player.Player
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BOXBOTTOM
import doom.engine.geometry.BOXLEFT
import doom.engine.geometry.BOXRIGHT
import doom.engine.geometry.BOXTOP
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.BspQueries
import doom.engine.geometry.FINEANGLES
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.geometry.finetangent
import doom.engine.menu.detailLevel
import doom.engine.menu.screenblocks
import doom.engine.rendering.resources.rInitData
import doom.engine.world.BspNode
import doom.engine.world.MapSegment
import doom.engine.world.Subsector
import doom.engine.world.nodes
import doom.engine.world.numnodes
import doom.engine.world.subsectors

import kotlin.math.abs

private const val FIELDOFVIEW = 2048

internal const val LIGHTLEVELS = 16
internal const val LIGHTSEGSHIFT = 4

internal const val MAXLIGHTSCALE = 48
internal const val LIGHTSCALESHIFT = 12
internal const val MAXLIGHTZ = 128
internal const val LIGHTZSHIFT = 20

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

internal var DoomEngineCore.centerxfrac: FixedPoint
    get() = stateRendererView.centerxfrac
    set(value) { stateRendererView.centerxfrac = value }
internal var DoomEngineCore.centeryfrac: FixedPoint
    get() = stateRendererView.centeryfrac
    set(value) { stateRendererView.centeryfrac = value }
internal var DoomEngineCore.projection: FixedPoint
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

internal var DoomEngineCore.viewx: FixedPoint
    get() = stateRendererView.viewx
    set(value) { stateRendererView.viewx = value }
internal var DoomEngineCore.viewy: FixedPoint
    get() = stateRendererView.viewy
    set(value) { stateRendererView.viewy = value }
internal var DoomEngineCore.viewz: FixedPoint
    get() = stateRendererView.viewz
    set(value) { stateRendererView.viewz = value }

internal var DoomEngineCore.viewangle: BinaryAngle
    get() = stateRendererView.viewangle
    set(value) { stateRendererView.viewangle = value }

internal var DoomEngineCore.viewcos: FixedPoint
    get() = stateRendererView.viewcos
    set(value) { stateRendererView.viewcos = value }
internal var DoomEngineCore.viewsin: FixedPoint
    get() = stateRendererView.viewsin
    set(value) { stateRendererView.viewsin = value }

internal var DoomEngineCore.viewplayer: Player?
    get() = stateRendererView.viewplayer
    set(value) { stateRendererView.viewplayer = value }

internal var DoomEngineCore.detailshift
    get() = stateRendererView.detailshift
    set(value) { stateRendererView.detailshift = value }

internal var DoomEngineCore.clipangle: BinaryAngle
    get() = stateRendererView.clipangle
    set(value) { stateRendererView.clipangle = value }

internal val DoomEngineCore.viewangletox
    get() = stateRendererView.viewangletox

internal val DoomEngineCore.xtoviewangle
    get() = stateRendererView.xtoviewangle


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

internal fun DoomEngineCore.rAddPointToBox(x: Int, y: Int, box: IntArray) {
    if (x < box[BOXLEFT])
        box[BOXLEFT] = x
    if (x > box[BOXRIGHT])
        box[BOXRIGHT] = x
    if (y < box[BOXBOTTOM])
        box[BOXBOTTOM] = y
    if (y > box[BOXTOP])
        box[BOXTOP] = y
}

internal fun DoomEngineCore.rPointOnSide(x: FixedPoint, y: FixedPoint, node: BspNode): Int =
    FixedGeometry.sideOfNode(x, y, node)

internal fun DoomEngineCore.rPointOnSegSide(x: FixedPoint, y: FixedPoint, line: MapSegment): Int =
    FixedGeometry.sideOfSegment(x, y, line)

internal fun DoomEngineCore.rPointToAngle(x: FixedPoint, y: FixedPoint): BinaryAngle =
    FixedGeometry.angleBetween(viewx, viewy, x, y)

internal fun DoomEngineCore.rPointToAngle2(x1: FixedPoint, y1: FixedPoint, x2: FixedPoint, y2: FixedPoint): BinaryAngle =
    FixedGeometry.angleBetween(x1, y1, x2, y2)

internal fun DoomEngineCore.rPointToDist(x: FixedPoint, y: FixedPoint): FixedPoint =
    FixedGeometry.distanceBetween(viewx, viewy, x, y)

internal fun DoomEngineCore.rInitPointToAngle() {
}

internal fun DoomEngineCore.rScaleFromGlobalAngle(visangle: BinaryAngle): FixedPoint {
    var scale: FixedPoint
    val anglea: BinaryAngle
    val angleb: BinaryAngle
    val sinea: Int
    val sineb: Int
    val num: FixedPoint
    val den: Int

    anglea = ANG90 + (visangle - viewangle)
    angleb = ANG90 + (visangle - rwNormalangle)

    sinea = finesine[(anglea shr ANGLETOFINESHIFT).toInt()]
    sineb = finesine[(angleb shr ANGLETOFINESHIFT).toInt()]
    num = fixedMul(projection, sineb) shl detailshift
    den = fixedMul(rwDistance, sinea)

    if (den > num shr 16) {
        scale = fixedDiv(num, den)

        if (scale > 64 * FRACUNIT)
            scale = 64 * FRACUNIT
        else if (scale < 256)
            scale = 256
    } else
        scale = 64 * FRACUNIT

    return scale
}

internal fun DoomEngineCore.rInitTables() {
}

internal fun DoomEngineCore.rInitTextureMapping() {
    var i: Int
    var x: Int
    var t: Int
    val focallength: FixedPoint

    focallength = fixedDiv(centerxfrac,
        finetangent[FINEANGLES / 4 + FIELDOFVIEW / 2])

    i = 0
    while (i < FINEANGLES / 2) {
        if (finetangent[i] > FRACUNIT * 2)
            t = -1
        else if (finetangent[i] < -FRACUNIT * 2)
            t = viewwidth + 1
        else {
            t = fixedMul(finetangent[i], focallength)
            t = (centerxfrac - t + FRACUNIT - 1) shr FRACBITS

            if (t < -1)
                t = -1
            else if (t > viewwidth + 1)
                t = viewwidth + 1
        }
        viewangletox[i] = t
        i++
    }

    x = 0
    while (x <= viewwidth) {
        i = 0
        while (viewangletox[i] > x)
            i++
        xtoviewangle[x] = (i shl ANGLETOFINESHIFT) - ANG90.toInt()
        x++
    }

    i = 0
    while (i < FINEANGLES / 2) {
        if (viewangletox[i] == -1)
            viewangletox[i] = 0
        else if (viewangletox[i] == viewwidth + 1)
            viewangletox[i] = viewwidth
        i++
    }

    clipangle = xtoviewangle[0].toUInt()
}

private const val DISTMAP = 2

internal fun DoomEngineCore.rInitLightTables() {
    var level: Int
    var startmap: Int
    var scale: Int

    for (i in 0 until LIGHTLEVELS) {
        startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS
        for (j in 0 until MAXLIGHTZ) {
            scale = fixedDiv(SCREENWIDTH / 2 * FRACUNIT, (j + 1) shl LIGHTZSHIFT)
            scale = scale shr LIGHTSCALESHIFT
            level = startmap - scale / DISTMAP

            if (level < 0)
                level = 0

            if (level >= NUMCOLORMAPS)
                level = NUMCOLORMAPS - 1

            zlight[i][j] = level * 256
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

internal fun DoomEngineCore.rSetViewSize(blocks: Int, detail: Int) {
    setsizeneeded = true
    setblocks = blocks
    setdetail = detail
}

internal fun DoomEngineCore.rExecuteSetViewSize() {
    var cosadj: FixedPoint
    var dy: FixedPoint
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
        basecolfunc = { rDrawColumn() }
        colfunc = basecolfunc
        fuzzcolfunc = { rDrawFuzzColumn() }
        transcolfunc = { rDrawTranslatedColumn() }
        spanfunc = { rDrawSpan() }
    } else {
        basecolfunc = { rDrawColumnLow() }
        colfunc = basecolfunc
        fuzzcolfunc = { rDrawFuzzColumn() }
        transcolfunc = { rDrawTranslatedColumn() }
        spanfunc = { rDrawSpanLow() }
    }

    rInitBuffer(scaledviewwidth, viewheight)

    rInitTextureMapping()

    pspritescale = FRACUNIT * viewwidth / SCREENWIDTH
    pspriteiscale = FRACUNIT * SCREENWIDTH / viewwidth

    for (i in 0 until viewwidth)
        screenheightarray[i] = viewheight.toShort()

    for (i in 0 until viewheight) {
        dy = ((i - viewheight / 2) shl FRACBITS) + FRACUNIT / 2
        dy = abs(dy)
        yslope[i] = fixedDiv((viewwidth shl detailshift) / 2 * FRACUNIT, dy)
    }

    for (i in 0 until viewwidth) {
        cosadj = abs(FineCosineTable[(xtoviewangle[i].toUInt() shr ANGLETOFINESHIFT).toInt()])
        distscale[i] = fixedDiv(FRACUNIT, cosadj)
    }

    for (i in 0 until LIGHTLEVELS) {
        startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS
        for (j in 0 until MAXLIGHTSCALE) {
            level = startmap - j * SCREENWIDTH / (viewwidth shl detailshift) / DISTMAP

            if (level < 0)
                level = 0

            if (level >= NUMCOLORMAPS)
                level = NUMCOLORMAPS - 1

            scalelight[i][j] = level * 256
        }
    }
}

internal fun DoomEngineCore.rInit() {
    rInitData()
    print("\nR_InitData")
    rInitPointToAngle()
    print("\nR_InitPointToAngle")
    rInitTables()
    print("\nR_InitTables")

    rSetViewSize(screenblocks, detailLevel)
    rInitPlanes()
    print("\nR_InitPlanes")
    rInitLightTables()
    print("\nR_InitLightTables")
    rInitSkyMap()
    print("\nR_InitSkyMap")
    rInitTranslationTables()
    print("\nR_InitTranslationsTables")

    framecount = 0
}

internal fun DoomEngineCore.rPointInSubsector(x: FixedPoint, y: FixedPoint): Subsector =
    BspQueries.subsectorAt(x, y, nodes, subsectors, numnodes)

internal fun DoomEngineCore.rSetupFrame(player: Player) {
    viewplayer = player
    viewx = player.mo!!.x
    viewy = player.mo!!.y
    viewangle = player.mo!!.angle + viewangleoffset.toUInt()
    extralight = player.extralight

    viewz = player.viewz

    viewsin = finesine[(viewangle shr ANGLETOFINESHIFT).toInt()]
    viewcos = FineCosineTable[(viewangle shr ANGLETOFINESHIFT).toInt()]

    sscount = 0

    if (player.fixedcolormap != 0) {
        fixedcolormap = player.fixedcolormap * 256

        walllights = scalelightfixed

        for (i in 0 until MAXLIGHTSCALE)
            scalelightfixed[i] = fixedcolormap
    } else
        fixedcolormap = -1

    framecount++
    validcount++
}

internal fun DoomEngineCore.rRenderPlayerView(player: Player) = sceneRenderer.render(player)

internal fun createSceneRenderer(core: DoomEngineCore): SceneRenderer<Player> = SceneRenderer(
    object : ScenePasses<Player> {
        override fun prepare(player: Player) = with(core) {
            rSetupFrame(player)
            rClearClipSegs()
            rClearDrawSegs()
            rClearPlanes()
            rClearSprites()
        }
        override fun drawWorld() = core.rRenderBSPNode(core.numnodes - 1)
        override fun drawPlanes() = core.rDrawPlanes()
        override fun drawMasked() = core.rDrawMasked()
    },
    core.tickScheduler::pollInputs,
)
