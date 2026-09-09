
package doom.engine.automap

import doom.engine.KEY_DOWNARROW
import doom.engine.KEY_LEFTARROW
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_TAB
import doom.engine.KEY_UPARROW
import doom.engine.cheats.chtCheckCheat
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.PW_ALLMAP
import doom.engine.gameplay.PW_INVISIBILITY
import doom.engine.gameplay.singledemo
import doom.engine.gameplay.viewactive
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_KEYUP
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.vMarkRect
import doom.engine.rendering.screens
import doom.engine.resources.AMSTR_FOLLOWOFF
import doom.engine.resources.AMSTR_FOLLOWON
import doom.engine.resources.AMSTR_GRIDOFF
import doom.engine.resources.AMSTR_GRIDON
import doom.engine.resources.AMSTR_MARKEDSPOT
import doom.engine.resources.AMSTR_MARKSCLEARED
import doom.engine.resources.MAXINT
import doom.engine.resources.wCacheLumpName
import doom.engine.statusbar.stResponder
import doom.engine.world.MAPBLOCKUNITS
import doom.engine.world.ML_DONTDRAW
import doom.engine.world.ML_MAPPED
import doom.engine.world.ML_SECRET
import doom.engine.world.PLAYERRADIUS
import doom.engine.world.bmaporgx
import doom.engine.world.bmaporgy
import doom.engine.world.lines
import doom.engine.world.numlines
import doom.engine.world.numsectors
import doom.engine.world.numvertexes
import doom.engine.world.sectors
import doom.engine.world.vertexes

internal const val AM_MSGHEADER = ('a'.code shl 24) + ('m'.code shl 16)
internal const val AM_MSGENTERED = AM_MSGHEADER or ('e'.code shl 8)
internal const val AM_MSGEXITED = AM_MSGHEADER or ('x'.code shl 8)

internal const val REDS = 256 - 5 * 16
private const val REDRANGE = 16
private const val BLUES = 256 - 4 * 16 + 8
private const val BLUERANGE = 8
internal const val GREENS = 7 * 16
private const val GREENRANGE = 16
internal const val GRAYS = 6 * 16
private const val GRAYSRANGE = 16
internal const val BROWNS = 4 * 16
private const val BROWNRANGE = 16
private const val YELLOWS = 256 - 32 + 7
private const val YELLOWRANGE = 1
private const val BLACK = 0
private const val WHITE = 256 - 47

private const val BACKGROUND = BLACK
private const val YOURCOLORS = WHITE
private const val YOURRANGE = 0
private const val WALLCOLORS = REDS
private const val WALLRANGE = REDRANGE
private const val TSWALLCOLORS = GRAYS
private const val TSWALLRANGE = GRAYSRANGE
private const val FDWALLCOLORS = BROWNS
private const val FDWALLRANGE = BROWNRANGE
private const val CDWALLCOLORS = YELLOWS
private const val CDWALLRANGE = YELLOWRANGE
private const val THINGCOLORS = GREENS
private const val THINGRANGE = GREENRANGE
private const val SECRETWALLCOLORS = WALLCOLORS
private const val SECRETWALLRANGE = WALLRANGE
private const val GRIDCOLORS = GRAYS + GRAYSRANGE / 2
private const val GRIDRANGE = 0
private const val XHAIRCOLORS = GRAYS

private const val FB = 0

private const val AM_PANDOWNKEY = KEY_DOWNARROW
private const val AM_PANUPKEY = KEY_UPARROW
private const val AM_PANRIGHTKEY = KEY_RIGHTARROW
private const val AM_PANLEFTKEY = KEY_LEFTARROW
private const val AM_ZOOMINKEY = '='.code
private const val AM_ZOOMOUTKEY = '-'.code
private const val AM_STARTKEY = KEY_TAB
private const val AM_ENDKEY = KEY_TAB
private const val AM_GOBIGKEY = '0'.code
private const val AM_FOLLOWKEY = 'f'.code
private const val AM_GRIDKEY = 'g'.code
private const val AM_MARKKEY = 'm'.code
private const val AM_CLEARMARKKEY = 'c'.code

internal const val AM_NUMMARKPOINTS = 10

private val DoomEngineCore.initscalemtof
    get() = stateAutomap.initscalemtof
private const val F_PANINC = 4
private val DoomEngineCore.mZOOMIN
    get() = stateAutomap.mZOOMIN
private val DoomEngineCore.mZOOMOUT
    get() = stateAutomap.mZOOMOUT

private fun DoomEngineCore.ftom(x: Int): FixedPoint = fixedMul(x shl 16, scaleFtom)
private fun DoomEngineCore.mtof(x: FixedPoint): Int = fixedMul(x, scaleMtof) shr 16
private fun DoomEngineCore.cxmtof(x: FixedPoint): Int = fX + mtof(x - mX)
private fun DoomEngineCore.cymtof(y: FixedPoint): Int = fY + (fH - mtof(y - mY))

private const val LINE_NEVERSEE = ML_DONTDRAW

internal val DoomEngineCore.playerArrow: Array<AutomapWorldLine>
    get() = stateAutomap.playerArrow
private val DoomEngineCore.numplyrlines
    get() = stateAutomap.numplyrlines

internal val DoomEngineCore.cheatPlayerArrow: Array<AutomapWorldLine>
    get() = stateAutomap.cheatPlayerArrow
private val DoomEngineCore.numcheatplyrlines
    get() = stateAutomap.numcheatplyrlines

internal val DoomEngineCore.triangleGuy: Array<AutomapWorldLine>
    get() = stateAutomap.triangleGuy
private val DoomEngineCore.numtriangleguylines
    get() = stateAutomap.numtriangleguylines

internal val DoomEngineCore.thintriangleGuy: Array<AutomapWorldLine>
    get() = stateAutomap.thintriangleGuy
private val DoomEngineCore.numthintriangleguylines
    get() = stateAutomap.numthintriangleguylines

private var DoomEngineCore.cheating
    get() = stateAutomap.cheating
    set(value) { stateAutomap.cheating = value }
private var DoomEngineCore.grid
    get() = stateAutomap.grid
    set(value) { stateAutomap.grid = value }

private var DoomEngineCore.leveljuststarted
    get() = stateAutomap.leveljuststarted
    set(value) { stateAutomap.leveljuststarted = value }

internal var DoomEngineCore.automapactive
    get() = stateAutomap.automapactive
    set(value) { stateAutomap.automapactive = value }
private var DoomEngineCore.finitWidth
    get() = stateAutomap.finitWidth
    set(value) { stateAutomap.finitWidth = value }
private var DoomEngineCore.finitHeight
    get() = stateAutomap.finitHeight
    set(value) { stateAutomap.finitHeight = value }

private var DoomEngineCore.fX
    get() = stateAutomap.fX
    set(value) { stateAutomap.fX = value }
private var DoomEngineCore.fY
    get() = stateAutomap.fY
    set(value) { stateAutomap.fY = value }

private var DoomEngineCore.fW
    get() = stateAutomap.fW
    set(value) { stateAutomap.fW = value }
private var DoomEngineCore.fH
    get() = stateAutomap.fH
    set(value) { stateAutomap.fH = value }

private var DoomEngineCore.lightlev
    get() = stateAutomap.lightlev
    set(value) { stateAutomap.lightlev = value }
private var DoomEngineCore.fb
    get() = stateAutomap.fb
    set(value) { stateAutomap.fb = value }
private var DoomEngineCore.amclock
    get() = stateAutomap.amclock
    set(value) { stateAutomap.amclock = value }

private val DoomEngineCore.mPaninc
    get() = stateAutomap.mPaninc
private var DoomEngineCore.mtofZoommul: FixedPoint
    get() = stateAutomap.mtofZoommul
    set(value) { stateAutomap.mtofZoommul = value }
private var DoomEngineCore.ftomZoommul: FixedPoint
    get() = stateAutomap.ftomZoommul
    set(value) { stateAutomap.ftomZoommul = value }

private var DoomEngineCore.mX: FixedPoint
    get() = stateAutomap.mX
    set(value) { stateAutomap.mX = value }
private var DoomEngineCore.mY: FixedPoint
    get() = stateAutomap.mY
    set(value) { stateAutomap.mY = value }
private var DoomEngineCore.mX2: FixedPoint
    get() = stateAutomap.mX2
    set(value) { stateAutomap.mX2 = value }
private var DoomEngineCore.mY2: FixedPoint
    get() = stateAutomap.mY2
    set(value) { stateAutomap.mY2 = value }

private var DoomEngineCore.mW: FixedPoint
    get() = stateAutomap.mW
    set(value) { stateAutomap.mW = value }
private var DoomEngineCore.mH: FixedPoint
    get() = stateAutomap.mH
    set(value) { stateAutomap.mH = value }

private var DoomEngineCore.minX: FixedPoint
    get() = stateAutomap.minX
    set(value) { stateAutomap.minX = value }
private var DoomEngineCore.minY: FixedPoint
    get() = stateAutomap.minY
    set(value) { stateAutomap.minY = value }
private var DoomEngineCore.maxX: FixedPoint
    get() = stateAutomap.maxX
    set(value) { stateAutomap.maxX = value }
private var DoomEngineCore.maxY: FixedPoint
    get() = stateAutomap.maxY
    set(value) { stateAutomap.maxY = value }

private var DoomEngineCore.maxW: FixedPoint
    get() = stateAutomap.maxW
    set(value) { stateAutomap.maxW = value }
private var DoomEngineCore.maxH: FixedPoint
    get() = stateAutomap.maxH
    set(value) { stateAutomap.maxH = value }

private var DoomEngineCore.minW: FixedPoint
    get() = stateAutomap.minW
    set(value) { stateAutomap.minW = value }
private var DoomEngineCore.minH: FixedPoint
    get() = stateAutomap.minH
    set(value) { stateAutomap.minH = value }

private var DoomEngineCore.minScaleMtof: FixedPoint
    get() = stateAutomap.minScaleMtof
    set(value) { stateAutomap.minScaleMtof = value }
private var DoomEngineCore.maxScaleMtof: FixedPoint
    get() = stateAutomap.maxScaleMtof
    set(value) { stateAutomap.maxScaleMtof = value }

private var DoomEngineCore.oldMW: FixedPoint
    get() = stateAutomap.oldMW
    set(value) { stateAutomap.oldMW = value }
private var DoomEngineCore.oldMH: FixedPoint
    get() = stateAutomap.oldMH
    set(value) { stateAutomap.oldMH = value }
private var DoomEngineCore.oldMX: FixedPoint
    get() = stateAutomap.oldMX
    set(value) { stateAutomap.oldMX = value }
private var DoomEngineCore.oldMY: FixedPoint
    get() = stateAutomap.oldMY
    set(value) { stateAutomap.oldMY = value }

private val DoomEngineCore.fOldloc
    get() = stateAutomap.fOldloc

private var DoomEngineCore.scaleMtof: FixedPoint
    get() = stateAutomap.scaleMtof
    set(value) { stateAutomap.scaleMtof = value }
private var DoomEngineCore.scaleFtom: FixedPoint
    get() = stateAutomap.scaleFtom
    set(value) { stateAutomap.scaleFtom = value }

private var DoomEngineCore.plr: Player
    get() = stateAutomap.plr
    set(value) { stateAutomap.plr = value }

private val DoomEngineCore.marknums
    get() = stateAutomap.marknums
private val DoomEngineCore.markpoints
    get() = stateAutomap.markpoints
private var DoomEngineCore.markpointnum
    get() = stateAutomap.markpointnum
    set(value) { stateAutomap.markpointnum = value }

private var DoomEngineCore.followplayer
    get() = stateAutomap.followplayer
    set(value) { stateAutomap.followplayer = value }

private val DoomEngineCore.cheatAmapSeq
    get() = stateAutomap.cheatAmapSeq
private val DoomEngineCore.cheatAmap
    get() = stateAutomap.cheatAmap

private var DoomEngineCore.stopped
    get() = stateAutomap.stopped
    set(value) { stateAutomap.stopped = value }


internal fun DoomEngineCore.amGetIslope(ml: AutomapWorldLine, `is`: AutomapSlope) {
    val dy = ml.a.y - ml.b.y
    val dx = ml.b.x - ml.a.x
    if (dy == 0) `is`.islp = if (dx < 0) -MAXINT else MAXINT
    else `is`.islp = fixedDiv(dx, dy)
    if (dx == 0) `is`.slp = if (dy < 0) -MAXINT else MAXINT
    else `is`.slp = fixedDiv(dy, dx)
}

internal fun DoomEngineCore.amActivateNewScale() {
    mX += mW / 2
    mY += mH / 2
    mW = ftom(fW)
    mH = ftom(fH)
    mX -= mW / 2
    mY -= mH / 2
    mX2 = mX + mW
    mY2 = mY + mH
}

internal fun DoomEngineCore.amSaveScaleAndLoc() {
    oldMX = mX
    oldMY = mY
    oldMW = mW
    oldMH = mH
}

internal fun DoomEngineCore.amRestoreScaleAndLoc() {
    mW = oldMW
    mH = oldMH
    if (followplayer == 0) {
        mX = oldMX
        mY = oldMY
    } else {
        mX = plr.mo!!.x - mW / 2
        mY = plr.mo!!.y - mH / 2
    }
    mX2 = mX + mW
    mY2 = mY + mH

    scaleMtof = fixedDiv(fW shl FRACBITS, mW)
    scaleFtom = fixedDiv(FRACUNIT, scaleMtof)
}

internal fun DoomEngineCore.amAddMark() {
    markpoints[markpointnum].x = mX + mW / 2
    markpoints[markpointnum].y = mY + mH / 2
    markpointnum = (markpointnum + 1) % AM_NUMMARKPOINTS
}

internal fun DoomEngineCore.amFindMinMaxBoundaries() {
    minX = MAXINT
    minY = MAXINT
    maxX = -MAXINT
    maxY = -MAXINT

    for (i in 0 until numvertexes) {
        if (vertexes[i].x < minX)
            minX = vertexes[i].x
        else if (vertexes[i].x > maxX)
            maxX = vertexes[i].x

        if (vertexes[i].y < minY)
            minY = vertexes[i].y
        else if (vertexes[i].y > maxY)
            maxY = vertexes[i].y
    }

    maxW = maxX - minX
    maxH = maxY - minY

    minW = 2 * PLAYERRADIUS
    minH = 2 * PLAYERRADIUS

    val a = fixedDiv(fW shl FRACBITS, maxW)
    val b = fixedDiv(fH shl FRACBITS, maxH)

    minScaleMtof = if (a < b) a else b
    maxScaleMtof = fixedDiv(fH shl FRACBITS, 2 * PLAYERRADIUS)
}

internal fun DoomEngineCore.amChangeWindowLoc() {
    if (mPaninc.x != 0 || mPaninc.y != 0) {
        followplayer = 0
        fOldloc.x = MAXINT
    }

    mX += mPaninc.x
    mY += mPaninc.y

    if (mX + mW / 2 > maxX)
        mX = maxX - mW / 2
    else if (mX + mW / 2 < minX)
        mX = minX - mW / 2

    if (mY + mH / 2 > maxY)
        mY = maxY - mH / 2
    else if (mY + mH / 2 < minY)
        mY = minY - mH / 2

    mX2 = mX + mW
    mY2 = mY + mH
}

internal fun DoomEngineCore.amInitVariables() {
    val stNotify = EngineEvent(EV_KEYUP, AM_MSGENTERED)

    automapactive = true
    fb = screens[0]

    fOldloc.x = MAXINT
    amclock = 0
    lightlev = 0

    mPaninc.x = 0
    mPaninc.y = 0
    ftomZoommul = FRACUNIT
    mtofZoommul = FRACUNIT

    mW = ftom(fW)
    mH = ftom(fH)

    var pnum = consoleplayer
    if (!playeringame[pnum]) {
        pnum = 0
        while (pnum < MAXPLAYERS) {
            if (playeringame[pnum])
                break
            pnum++
        }
    }

    plr = players[pnum]
    mX = plr.mo!!.x - mW / 2
    mY = plr.mo!!.y - mH / 2
    amChangeWindowLoc()

    oldMX = mX
    oldMY = mY
    oldMW = mW
    oldMH = mH

    stResponder(stNotify)
}

internal fun DoomEngineCore.amLoadPics() {
    for (i in 0 until 10) {
        val namebuf = "AMMNUM$i"
        marknums[i] = wCacheLumpName(namebuf)
    }
}

internal fun DoomEngineCore.amUnloadPics() {
}

internal fun DoomEngineCore.amClearMarks() {
    for (i in 0 until AM_NUMMARKPOINTS)
        markpoints[i].x = -1
    markpointnum = 0
}

internal fun DoomEngineCore.amLevelInit() {
    leveljuststarted = 0

    fX = 0
    fY = 0
    fW = finitWidth
    fH = finitHeight

    amClearMarks()

    amFindMinMaxBoundaries()
    scaleMtof = fixedDiv(minScaleMtof, (0.7 * FRACUNIT).toInt())
    if (scaleMtof > maxScaleMtof)
        scaleMtof = minScaleMtof
    scaleFtom = fixedDiv(FRACUNIT, scaleMtof)
}

internal fun DoomEngineCore.amStop() {
    val stNotify = EngineEvent(0, EV_KEYUP, AM_MSGEXITED)

    amUnloadPics()
    automapactive = false
    stResponder(stNotify)
    stopped = true
}

private var DoomEngineCore.lastlevel
    get() = stateAutomap.lastlevel
    set(value) { stateAutomap.lastlevel = value }
private var DoomEngineCore.lastepisode
    get() = stateAutomap.lastepisode
    set(value) { stateAutomap.lastepisode = value }

internal fun DoomEngineCore.amStart() {
    if (!stopped) amStop()
    stopped = false
    if (lastlevel != gamemap || lastepisode != gameepisode) {
        amLevelInit()
        lastlevel = gamemap
        lastepisode = gameepisode
    }
    amInitVariables()
    amLoadPics()
}

internal fun DoomEngineCore.amMinOutWindowScale() {
    scaleMtof = minScaleMtof
    scaleFtom = fixedDiv(FRACUNIT, scaleMtof)
    amActivateNewScale()
}

internal fun DoomEngineCore.amMaxOutWindowScale() {
    scaleMtof = maxScaleMtof
    scaleFtom = fixedDiv(FRACUNIT, scaleMtof)
    amActivateNewScale()
}

private var DoomEngineCore.cheatstate
    get() = stateAutomap.cheatstate
    set(value) { stateAutomap.cheatstate = value }
private var DoomEngineCore.bigstate
    get() = stateAutomap.bigstate
    set(value) { stateAutomap.bigstate = value }
private var DoomEngineCore.buffer
    get() = stateAutomap.buffer
    set(value) { stateAutomap.buffer = value }

internal fun DoomEngineCore.amResponder(ev: EngineEvent): Boolean {
    var rc: Boolean

    rc = false

    if (!automapactive) {
        if (ev.type == EV_KEYDOWN && ev.data1 == AM_STARTKEY) {
            amStart()
            viewactive = false
            rc = true
        }
    } else if (ev.type == EV_KEYDOWN) {
        rc = true
        when (ev.data1) {
            AM_PANRIGHTKEY -> {
                if (followplayer == 0) mPaninc.x = ftom(F_PANINC)
                else rc = false
            }
            AM_PANLEFTKEY -> {
                if (followplayer == 0) mPaninc.x = -ftom(F_PANINC)
                else rc = false
            }
            AM_PANUPKEY -> {
                if (followplayer == 0) mPaninc.y = ftom(F_PANINC)
                else rc = false
            }
            AM_PANDOWNKEY -> {
                if (followplayer == 0) mPaninc.y = -ftom(F_PANINC)
                else rc = false
            }
            AM_ZOOMOUTKEY -> {
                mtofZoommul = mZOOMOUT
                ftomZoommul = mZOOMIN
            }
            AM_ZOOMINKEY -> {
                mtofZoommul = mZOOMIN
                ftomZoommul = mZOOMOUT
            }
            AM_ENDKEY -> {
                bigstate = 0
                viewactive = true
                amStop()
            }
            AM_GOBIGKEY -> {
                bigstate = if (bigstate == 0) 1 else 0
                if (bigstate != 0) {
                    amSaveScaleAndLoc()
                    amMinOutWindowScale()
                } else amRestoreScaleAndLoc()
            }
            AM_FOLLOWKEY -> {
                followplayer = if (followplayer == 0) 1 else 0
                fOldloc.x = MAXINT
                plr.message = if (followplayer != 0) AMSTR_FOLLOWON else AMSTR_FOLLOWOFF
            }
            AM_GRIDKEY -> {
                grid = if (grid == 0) 1 else 0
                plr.message = if (grid != 0) AMSTR_GRIDON else AMSTR_GRIDOFF
            }
            AM_MARKKEY -> {
                buffer = "$AMSTR_MARKEDSPOT $markpointnum"
                plr.message = buffer
                amAddMark()
            }
            AM_CLEARMARKKEY -> {
                amClearMarks()
                plr.message = AMSTR_MARKSCLEARED
            }
            else -> {
                cheatstate = 0
                rc = false
            }
        }
        if (deathmatch == 0 && chtCheckCheat(cheatAmap, ev.data1) != 0) {
            rc = false
            cheating = (cheating + 1) % 3
        }
    } else if (ev.type == EV_KEYUP) {
        rc = false
        when (ev.data1) {
            AM_PANRIGHTKEY ->
                if (followplayer == 0) mPaninc.x = 0
            AM_PANLEFTKEY ->
                if (followplayer == 0) mPaninc.x = 0
            AM_PANUPKEY ->
                if (followplayer == 0) mPaninc.y = 0
            AM_PANDOWNKEY ->
                if (followplayer == 0) mPaninc.y = 0
            AM_ZOOMOUTKEY, AM_ZOOMINKEY -> {
                mtofZoommul = FRACUNIT
                ftomZoommul = FRACUNIT
            }
        }
    }

    return rc
}

internal fun DoomEngineCore.amChangeWindowScale() {
    scaleMtof = fixedMul(scaleMtof, mtofZoommul)
    scaleFtom = fixedDiv(FRACUNIT, scaleMtof)

    if (scaleMtof < minScaleMtof)
        amMinOutWindowScale()
    else if (scaleMtof > maxScaleMtof)
        amMaxOutWindowScale()
    else
        amActivateNewScale()
}

internal fun DoomEngineCore.amDoFollowPlayer() {
    if (fOldloc.x != plr.mo!!.x || fOldloc.y != plr.mo!!.y) {
        mX = ftom(mtof(plr.mo!!.x)) - mW / 2
        mY = ftom(mtof(plr.mo!!.y)) - mH / 2
        mX2 = mX + mW
        mY2 = mY + mH
        fOldloc.x = plr.mo!!.x
        fOldloc.y = plr.mo!!.y

    }
}

private var DoomEngineCore.nexttic
    get() = stateAutomap.nexttic
    set(value) { stateAutomap.nexttic = value }
private val DoomEngineCore.litelevels
    get() = stateAutomap.litelevels
private var DoomEngineCore.litelevelscnt
    get() = stateAutomap.litelevelscnt
    set(value) { stateAutomap.litelevelscnt = value }

internal fun DoomEngineCore.amUpdateLightLev() {
    if (amclock > nexttic) {
        lightlev = litelevels[litelevelscnt]
        litelevelscnt++
        if (litelevelscnt == litelevels.size) litelevelscnt = 0
        nexttic = amclock + 6 - (amclock % 6)
    }
}

internal fun DoomEngineCore.amTicker() {
    if (!automapactive)
        return

    amclock++

    if (followplayer != 0)
        amDoFollowPlayer()

    if (ftomZoommul != FRACUNIT)
        amChangeWindowScale()

    if (mPaninc.x != 0 || mPaninc.y != 0)
        amChangeWindowLoc()

}

internal fun DoomEngineCore.amClearFB(color: Int) {
    fb.fill(color.toByte(), 0, fW * fH)
}

private const val LEFT = 1
private const val RIGHT = 2
private const val BOTTOM = 4
private const val TOP = 8

private fun DoomEngineCore.dooutcode(mx: Int, my: Int): Int {
    var oc = 0
    if (my < 0) oc = oc or TOP
    else if (my >= fH) oc = oc or BOTTOM
    if (mx < 0) oc = oc or LEFT
    else if (mx >= fW) oc = oc or RIGHT
    return oc
}

internal fun DoomEngineCore.amClipMline(ml: AutomapWorldLine, fl: AutomapScreenLine): Boolean {
    var outcode1 = 0
    var outcode2 = 0
    var outside: Int

    val tmp = AutomapScreenPoint()
    var dx: Int
    var dy: Int

    if (ml.a.y > mY2)
        outcode1 = TOP
    else if (ml.a.y < mY)
        outcode1 = BOTTOM

    if (ml.b.y > mY2)
        outcode2 = TOP
    else if (ml.b.y < mY)
        outcode2 = BOTTOM

    if ((outcode1 and outcode2) != 0)
        return false

    if (ml.a.x < mX)
        outcode1 = outcode1 or LEFT
    else if (ml.a.x > mX2)
        outcode1 = outcode1 or RIGHT

    if (ml.b.x < mX)
        outcode2 = outcode2 or LEFT
    else if (ml.b.x > mX2)
        outcode2 = outcode2 or RIGHT

    if ((outcode1 and outcode2) != 0)
        return false

    fl.a.x = cxmtof(ml.a.x)
    fl.a.y = cymtof(ml.a.y)
    fl.b.x = cxmtof(ml.b.x)
    fl.b.y = cymtof(ml.b.y)

    outcode1 = dooutcode(fl.a.x, fl.a.y)
    outcode2 = dooutcode(fl.b.x, fl.b.y)

    if ((outcode1 and outcode2) != 0)
        return false

    while ((outcode1 or outcode2) != 0) {
        outside = if (outcode1 != 0) outcode1 else outcode2

        if ((outside and TOP) != 0) {
            dy = fl.a.y - fl.b.y
            dx = fl.b.x - fl.a.x
            tmp.x = fl.a.x + (dx * (fl.a.y)) / dy
            tmp.y = 0
        } else if ((outside and BOTTOM) != 0) {
            dy = fl.a.y - fl.b.y
            dx = fl.b.x - fl.a.x
            tmp.x = fl.a.x + (dx * (fl.a.y - fH)) / dy
            tmp.y = fH - 1
        } else if ((outside and RIGHT) != 0) {
            dy = fl.b.y - fl.a.y
            dx = fl.b.x - fl.a.x
            tmp.y = fl.a.y + (dy * (fW - 1 - fl.a.x)) / dx
            tmp.x = fW - 1
        } else if ((outside and LEFT) != 0) {
            dy = fl.b.y - fl.a.y
            dx = fl.b.x - fl.a.x
            tmp.y = fl.a.y + (dy * (-fl.a.x)) / dx
            tmp.x = 0
        }

        if (outside == outcode1) {
            fl.a.x = tmp.x
            fl.a.y = tmp.y
            outcode1 = dooutcode(fl.a.x, fl.a.y)
        } else {
            fl.b.x = tmp.x
            fl.b.y = tmp.y
            outcode2 = dooutcode(fl.b.x, fl.b.y)
        }

        if ((outcode1 and outcode2) != 0)
            return false
    }

    return true
}

private var DoomEngineCore.fuck
    get() = stateAutomap.fuck
    set(value) { stateAutomap.fuck = value }

internal fun DoomEngineCore.amDrawFline(fl: AutomapScreenLine, color: Int) {
    var x: Int
    var y: Int
    var d: Int

    if (fl.a.x < 0 || fl.a.x >= fW
        || fl.a.y < 0 || fl.a.y >= fH
        || fl.b.x < 0 || fl.b.x >= fW
        || fl.b.y < 0 || fl.b.y >= fH
    ) {
        println("fuck $fuck \r")
        fuck++
        return
    }


    val dx = fl.b.x - fl.a.x
    val ax = 2 * (if (dx < 0) -dx else dx)
    val sx = if (dx < 0) -1 else 1

    val dy = fl.b.y - fl.a.y
    val ay = 2 * (if (dy < 0) -dy else dy)
    val sy = if (dy < 0) -1 else 1

    x = fl.a.x
    y = fl.a.y

    if (ax > ay) {
        d = ay - ax / 2
        while (true) {
            fb[y * fW + x] = color.toByte()
            if (x == fl.b.x) return
            if (d >= 0) {
                y += sy
                d -= ax
            }
            x += sx
            d += ay
        }
    } else {
        d = ax - ay / 2
        while (true) {
            fb[y * fW + x] = color.toByte()
            if (y == fl.b.y) return
            if (d >= 0) {
                x += sx
                d -= ay
            }
            y += sy
            d += ax
        }
    }
}

private val DoomEngineCore.fl
    get() = stateAutomap.fl

internal fun DoomEngineCore.amDrawMline(ml: AutomapWorldLine, color: Int) {
    if (amClipMline(ml, fl))
        amDrawFline(fl, color)
}

internal fun DoomEngineCore.amDrawGrid(color: Int) {
    var x: FixedPoint
    var y: FixedPoint
    var start: FixedPoint
    var end: FixedPoint
    val ml = AutomapWorldLine()

    start = mX
    if ((start - bmaporgx) % (MAPBLOCKUNITS shl FRACBITS) != 0)
        start += (MAPBLOCKUNITS shl FRACBITS) -
            ((start - bmaporgx) % (MAPBLOCKUNITS shl FRACBITS))
    end = mX + mW

    ml.a.y = mY
    ml.b.y = mY + mH
    x = start
    while (x < end) {
        ml.a.x = x
        ml.b.x = x
        amDrawMline(ml, color)
        x += (MAPBLOCKUNITS shl FRACBITS)
    }

    start = mY
    if ((start - bmaporgy) % (MAPBLOCKUNITS shl FRACBITS) != 0)
        start += (MAPBLOCKUNITS shl FRACBITS) -
            ((start - bmaporgy) % (MAPBLOCKUNITS shl FRACBITS))
    end = mY + mH

    ml.a.x = mX
    ml.b.x = mX + mW
    y = start
    while (y < end) {
        ml.a.y = y
        ml.b.y = y
        amDrawMline(ml, color)
        y += (MAPBLOCKUNITS shl FRACBITS)
    }
}

private val DoomEngineCore.l
    get() = stateAutomap.l

internal fun DoomEngineCore.amDrawWalls() {
    for (i in 0 until numlines) {
        l.a.x = lines[i].v1.x
        l.a.y = lines[i].v1.y
        l.b.x = lines[i].v2.x
        l.b.y = lines[i].v2.y
        if (cheating != 0 || (lines[i].flags and ML_MAPPED) != 0) {
            if ((lines[i].flags and LINE_NEVERSEE) != 0 && cheating == 0)
                continue
            if (lines[i].backsector == null) {
                amDrawMline(l, WALLCOLORS + lightlev)
            } else {
                if (lines[i].special == 39) {
                    amDrawMline(l, WALLCOLORS + WALLRANGE / 2)
                } else if ((lines[i].flags and ML_SECRET) != 0) {
                    if (cheating != 0) amDrawMline(l, SECRETWALLCOLORS + lightlev)
                    else amDrawMline(l, WALLCOLORS + lightlev)
                } else if (lines[i].backsector!!.floorheight
                    != lines[i].frontsector!!.floorheight
                ) {
                    amDrawMline(l, FDWALLCOLORS + lightlev)
                } else if (lines[i].backsector!!.ceilingheight
                    != lines[i].frontsector!!.ceilingheight
                ) {
                    amDrawMline(l, CDWALLCOLORS + lightlev)
                } else if (cheating != 0) {
                    amDrawMline(l, TSWALLCOLORS + lightlev)
                }
            }
        } else if (plr.powers[PW_ALLMAP] != 0) {
            if ((lines[i].flags and LINE_NEVERSEE) == 0) amDrawMline(l, GRAYS + 3)
        }
    }
}

internal fun DoomEngineCore.amRotate(pt: AutomapWorldPoint, a: BinaryAngle) {
    val tmpx =
        fixedMul(pt.x, FineCosineTable[(a shr ANGLETOFINESHIFT).toInt()]) -
            fixedMul(pt.y, finesine[(a shr ANGLETOFINESHIFT).toInt()])

    pt.y =
        fixedMul(pt.x, finesine[(a shr ANGLETOFINESHIFT).toInt()]) +
            fixedMul(pt.y, FineCosineTable[(a shr ANGLETOFINESHIFT).toInt()])

    pt.x = tmpx
}

internal fun DoomEngineCore.amDrawLineCharacter(
    lineguy: Array<AutomapWorldLine>,
    lineguylines: Int,
    scale: FixedPoint,
    angle: BinaryAngle,
    color: Int,
    x: FixedPoint,
    y: FixedPoint,
) {
    val l = AutomapWorldLine()

    for (i in 0 until lineguylines) {
        l.a.x = lineguy[i].a.x
        l.a.y = lineguy[i].a.y

        if (scale != 0) {
            l.a.x = fixedMul(scale, l.a.x)
            l.a.y = fixedMul(scale, l.a.y)
        }

        if (angle != 0u)
            amRotate(l.a, angle)

        l.a.x += x
        l.a.y += y

        l.b.x = lineguy[i].b.x
        l.b.y = lineguy[i].b.y

        if (scale != 0) {
            l.b.x = fixedMul(scale, l.b.x)
            l.b.y = fixedMul(scale, l.b.y)
        }

        if (angle != 0u)
            amRotate(l.b, angle)

        l.b.x += x
        l.b.y += y

        amDrawMline(l, color)
    }
}

private val DoomEngineCore.theirColors
    get() = stateAutomap.theirColors

internal fun DoomEngineCore.amDrawPlayers() {
    var p: Player
    var theirColor = -1
    var color: Int

    if (!netgame) {
        if (cheating != 0)
            amDrawLineCharacter(
                cheatPlayerArrow, numcheatplyrlines, 0,
                plr.mo!!.angle, WHITE, plr.mo!!.x, plr.mo!!.y
            )
        else
            amDrawLineCharacter(
                playerArrow, numplyrlines, 0, plr.mo!!.angle,
                WHITE, plr.mo!!.x, plr.mo!!.y
            )
        return
    }

    for (i in 0 until MAXPLAYERS) {
        theirColor++
        p = players[i]

        if ((deathmatch != 0 && !singledemo) && p !== plr)
            continue

        if (!playeringame[i])
            continue

        color = if (p.powers[PW_INVISIBILITY] != 0)
            246
        else
            theirColors[theirColor]

        amDrawLineCharacter(
            playerArrow, numplyrlines, 0, p.mo!!.angle,
            color, p.mo!!.x, p.mo!!.y
        )
    }
}

internal fun DoomEngineCore.amDrawThings(colors: Int) {
    var t: Actor?

    for (i in 0 until numsectors) {
        t = sectors[i].thinglist
        while (t != null) {
            amDrawLineCharacter(
                thintriangleGuy, numthintriangleguylines,
                16 shl FRACBITS, t.angle, colors + lightlev, t.x, t.y
            )
            t = t.snext
        }
    }
}

internal fun DoomEngineCore.amDrawMarks() {
    for (i in 0 until AM_NUMMARKPOINTS) {
        if (markpoints[i].x != -1) {
            val w = 5
            val h = 6
            val fx = cxmtof(markpoints[i].x)
            val fy = cymtof(markpoints[i].y)
            if (fx >= fX && fx <= fW - w && fy >= fY && fy <= fH - h)
                vDrawPatch(fx, fy, FB, marknums[i]!!)
        }
    }
}

internal fun DoomEngineCore.amDrawCrosshair(color: Int) {
    fb[(fW * (fH + 1)) / 2] = color.toByte()
}

internal fun DoomEngineCore.amDrawer() {
    if (!automapactive) return

    amClearFB(BACKGROUND)
    if (grid != 0)
        amDrawGrid(GRIDCOLORS)
    amDrawWalls()
    amDrawPlayers()
    if (cheating == 2)
        amDrawThings(THINGCOLORS)
    amDrawCrosshair(XHAIRCOLORS)

    amDrawMarks()

    vMarkRect(fX, fY, fW, fH)
}
