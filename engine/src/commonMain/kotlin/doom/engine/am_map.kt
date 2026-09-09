// Port of linuxdoom-1.10 am_map.c/am_map.h -- the automap code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// Used by ST StatusBar stuff. (am_map.h)
internal const val AM_MSGHEADER = ('a'.code shl 24) + ('m'.code shl 16)
internal const val AM_MSGENTERED = AM_MSGHEADER or ('e'.code shl 8)
internal const val AM_MSGEXITED = AM_MSGHEADER or ('x'.code shl 8)

// For use if I do walls with outsides/insides
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

// Automap colors
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

// drawing stuff
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

private val DoomEngineCore.INITSCALEMTOF
    get() = stateAutomap.INITSCALEMTOF
// how much the automap moves window per tic in frame-buffer coordinates
// moves 140 pixels in 1 second
private const val F_PANINC = 4
private val DoomEngineCore.M_ZOOMIN
    get() = stateAutomap.M_ZOOMIN
private val DoomEngineCore.M_ZOOMOUT
    get() = stateAutomap.M_ZOOMOUT

// translates between frame-buffer and map distances
private fun DoomEngineCore.FTOM(x: Int): fixed_t = FixedMul(x shl 16, scale_ftom)
private fun DoomEngineCore.MTOF(x: fixed_t): Int = FixedMul(x, scale_mtof) shr 16
// translates between frame-buffer and map coordinates
private fun DoomEngineCore.CXMTOF(x: fixed_t): Int = f_x + MTOF(x - m_x)
private fun DoomEngineCore.CYMTOF(y: fixed_t): Int = f_y + (f_h - MTOF(y - m_y))

// the following is crap
private const val LINE_NEVERSEE = ML_DONTDRAW


internal val DoomEngineCore.player_arrow: Array<mline_t>
    get() = stateAutomap.player_arrow
private val DoomEngineCore.NUMPLYRLINES
    get() = stateAutomap.NUMPLYRLINES

internal val DoomEngineCore.cheat_player_arrow: Array<mline_t>
    get() = stateAutomap.cheat_player_arrow
private val DoomEngineCore.NUMCHEATPLYRLINES
    get() = stateAutomap.NUMCHEATPLYRLINES

internal val DoomEngineCore.triangle_guy: Array<mline_t>
    get() = stateAutomap.triangle_guy
private val DoomEngineCore.NUMTRIANGLEGUYLINES
    get() = stateAutomap.NUMTRIANGLEGUYLINES

internal val DoomEngineCore.thintriangle_guy: Array<mline_t>
    get() = stateAutomap.thintriangle_guy
private val DoomEngineCore.NUMTHINTRIANGLEGUYLINES
    get() = stateAutomap.NUMTHINTRIANGLEGUYLINES

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
private var DoomEngineCore.finit_width
    get() = stateAutomap.finit_width
    set(value) { stateAutomap.finit_width = value }
private var DoomEngineCore.finit_height
    get() = stateAutomap.finit_height
    set(value) { stateAutomap.finit_height = value }

private var DoomEngineCore.f_x
    get() = stateAutomap.f_x
    set(value) { stateAutomap.f_x = value }
private var DoomEngineCore.f_y
    get() = stateAutomap.f_y
    set(value) { stateAutomap.f_y = value }

private var DoomEngineCore.f_w
    get() = stateAutomap.f_w
    set(value) { stateAutomap.f_w = value }
private var DoomEngineCore.f_h
    get() = stateAutomap.f_h
    set(value) { stateAutomap.f_h = value }

private var DoomEngineCore.lightlev
    get() = stateAutomap.lightlev
    set(value) { stateAutomap.lightlev = value }
private var DoomEngineCore.fb
    get() = stateAutomap.fb
    set(value) { stateAutomap.fb = value }
private var DoomEngineCore.amclock
    get() = stateAutomap.amclock
    set(value) { stateAutomap.amclock = value }

private val DoomEngineCore.m_paninc
    get() = stateAutomap.m_paninc
private var DoomEngineCore.mtof_zoommul: fixed_t
    get() = stateAutomap.mtof_zoommul
    set(value) { stateAutomap.mtof_zoommul = value }
private var DoomEngineCore.ftom_zoommul: fixed_t
    get() = stateAutomap.ftom_zoommul
    set(value) { stateAutomap.ftom_zoommul = value }

private var DoomEngineCore.m_x: fixed_t
    get() = stateAutomap.m_x
    set(value) { stateAutomap.m_x = value }
private var DoomEngineCore.m_y: fixed_t
    get() = stateAutomap.m_y
    set(value) { stateAutomap.m_y = value }
private var DoomEngineCore.m_x2: fixed_t
    get() = stateAutomap.m_x2
    set(value) { stateAutomap.m_x2 = value }
private var DoomEngineCore.m_y2: fixed_t
    get() = stateAutomap.m_y2
    set(value) { stateAutomap.m_y2 = value }

private var DoomEngineCore.m_w: fixed_t
    get() = stateAutomap.m_w
    set(value) { stateAutomap.m_w = value }
private var DoomEngineCore.m_h: fixed_t
    get() = stateAutomap.m_h
    set(value) { stateAutomap.m_h = value }

private var DoomEngineCore.min_x: fixed_t
    get() = stateAutomap.min_x
    set(value) { stateAutomap.min_x = value }
private var DoomEngineCore.min_y: fixed_t
    get() = stateAutomap.min_y
    set(value) { stateAutomap.min_y = value }
private var DoomEngineCore.max_x: fixed_t
    get() = stateAutomap.max_x
    set(value) { stateAutomap.max_x = value }
private var DoomEngineCore.max_y: fixed_t
    get() = stateAutomap.max_y
    set(value) { stateAutomap.max_y = value }

private var DoomEngineCore.max_w: fixed_t
    get() = stateAutomap.max_w
    set(value) { stateAutomap.max_w = value }
private var DoomEngineCore.max_h: fixed_t
    get() = stateAutomap.max_h
    set(value) { stateAutomap.max_h = value }

private var DoomEngineCore.min_w: fixed_t
    get() = stateAutomap.min_w
    set(value) { stateAutomap.min_w = value }
private var DoomEngineCore.min_h: fixed_t
    get() = stateAutomap.min_h
    set(value) { stateAutomap.min_h = value }

private var DoomEngineCore.min_scale_mtof: fixed_t
    get() = stateAutomap.min_scale_mtof
    set(value) { stateAutomap.min_scale_mtof = value }
private var DoomEngineCore.max_scale_mtof: fixed_t
    get() = stateAutomap.max_scale_mtof
    set(value) { stateAutomap.max_scale_mtof = value }

private var DoomEngineCore.old_m_w: fixed_t
    get() = stateAutomap.old_m_w
    set(value) { stateAutomap.old_m_w = value }
private var DoomEngineCore.old_m_h: fixed_t
    get() = stateAutomap.old_m_h
    set(value) { stateAutomap.old_m_h = value }
private var DoomEngineCore.old_m_x: fixed_t
    get() = stateAutomap.old_m_x
    set(value) { stateAutomap.old_m_x = value }
private var DoomEngineCore.old_m_y: fixed_t
    get() = stateAutomap.old_m_y
    set(value) { stateAutomap.old_m_y = value }

private val DoomEngineCore.f_oldloc
    get() = stateAutomap.f_oldloc

private var DoomEngineCore.scale_mtof: fixed_t
    get() = stateAutomap.scale_mtof
    set(value) { stateAutomap.scale_mtof = value }
private var DoomEngineCore.scale_ftom: fixed_t
    get() = stateAutomap.scale_ftom
    set(value) { stateAutomap.scale_ftom = value }

private var DoomEngineCore.plr: player_t
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

private val DoomEngineCore.cheat_amap_seq
    get() = stateAutomap.cheat_amap_seq
private val DoomEngineCore.cheat_amap
    get() = stateAutomap.cheat_amap

private var DoomEngineCore.stopped
    get() = stateAutomap.stopped
    set(value) { stateAutomap.stopped = value }

// Calculates the slope and slope according to the x-axis of a line
// segment in map coordinates (with the upright y-axis n' all) so
// that it can be used with the brain-dead drawing stuff.

internal fun DoomEngineCore.AM_getIslope(ml: mline_t, `is`: islope_t) {
    val dy = ml.a.y - ml.b.y
    val dx = ml.b.x - ml.a.x
    if (dy == 0) `is`.islp = if (dx < 0) -MAXINT else MAXINT
    else `is`.islp = FixedDiv(dx, dy)
    if (dx == 0) `is`.slp = if (dy < 0) -MAXINT else MAXINT
    else `is`.slp = FixedDiv(dy, dx)
}

//
//
//
internal fun DoomEngineCore.AM_activateNewScale() {
    m_x += m_w / 2
    m_y += m_h / 2
    m_w = FTOM(f_w)
    m_h = FTOM(f_h)
    m_x -= m_w / 2
    m_y -= m_h / 2
    m_x2 = m_x + m_w
    m_y2 = m_y + m_h
}

//
//
//
internal fun DoomEngineCore.AM_saveScaleAndLoc() {
    old_m_x = m_x
    old_m_y = m_y
    old_m_w = m_w
    old_m_h = m_h
}

//
//
//
internal fun DoomEngineCore.AM_restoreScaleAndLoc() {
    m_w = old_m_w
    m_h = old_m_h
    if (followplayer == 0) {
        m_x = old_m_x
        m_y = old_m_y
    } else {
        m_x = plr.mo!!.x - m_w / 2
        m_y = plr.mo!!.y - m_h / 2
    }
    m_x2 = m_x + m_w
    m_y2 = m_y + m_h

    // Change the scaling multipliers
    scale_mtof = FixedDiv(f_w shl FRACBITS, m_w)
    scale_ftom = FixedDiv(FRACUNIT, scale_mtof)
}

//
// adds a marker at the current location
//
internal fun DoomEngineCore.AM_addMark() {
    markpoints[markpointnum].x = m_x + m_w / 2
    markpoints[markpointnum].y = m_y + m_h / 2
    markpointnum = (markpointnum + 1) % AM_NUMMARKPOINTS
}

//
// Determines bounding box of all vertices,
// sets global variables controlling zoom range.
//
internal fun DoomEngineCore.AM_findMinMaxBoundaries() {
    min_x = MAXINT
    min_y = MAXINT
    max_x = -MAXINT
    max_y = -MAXINT

    for (i in 0 until numvertexes) {
        if (vertexes[i].x < min_x)
            min_x = vertexes[i].x
        else if (vertexes[i].x > max_x)
            max_x = vertexes[i].x

        if (vertexes[i].y < min_y)
            min_y = vertexes[i].y
        else if (vertexes[i].y > max_y)
            max_y = vertexes[i].y
    }

    max_w = max_x - min_x
    max_h = max_y - min_y

    min_w = 2 * PLAYERRADIUS // const? never changed?
    min_h = 2 * PLAYERRADIUS

    val a = FixedDiv(f_w shl FRACBITS, max_w)
    val b = FixedDiv(f_h shl FRACBITS, max_h)

    min_scale_mtof = if (a < b) a else b
    max_scale_mtof = FixedDiv(f_h shl FRACBITS, 2 * PLAYERRADIUS)
}

//
//
//
internal fun DoomEngineCore.AM_changeWindowLoc() {
    if (m_paninc.x != 0 || m_paninc.y != 0) {
        followplayer = 0
        f_oldloc.x = MAXINT
    }

    m_x += m_paninc.x
    m_y += m_paninc.y

    if (m_x + m_w / 2 > max_x)
        m_x = max_x - m_w / 2
    else if (m_x + m_w / 2 < min_x)
        m_x = min_x - m_w / 2

    if (m_y + m_h / 2 > max_y)
        m_y = max_y - m_h / 2
    else if (m_y + m_h / 2 < min_y)
        m_y = min_y - m_h / 2

    m_x2 = m_x + m_w
    m_y2 = m_y + m_h
}

//
//
//
internal fun DoomEngineCore.AM_initVariables() {
    val st_notify = event_t(ev_keyup, AM_MSGENTERED)

    automapactive = true
    fb = screens[0]

    f_oldloc.x = MAXINT
    amclock = 0
    lightlev = 0

    m_paninc.x = 0
    m_paninc.y = 0
    ftom_zoommul = FRACUNIT
    mtof_zoommul = FRACUNIT

    m_w = FTOM(f_w)
    m_h = FTOM(f_h)

    // find player to center on initially
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
    m_x = plr.mo!!.x - m_w / 2
    m_y = plr.mo!!.y - m_h / 2
    AM_changeWindowLoc()

    // for saving & restoring
    old_m_x = m_x
    old_m_y = m_y
    old_m_w = m_w
    old_m_h = m_h

    // inform the status bar of the change
    ST_Responder(st_notify)
}

//
//
//
internal fun DoomEngineCore.AM_loadPics() {
    for (i in 0 until 10) {
        val namebuf = "AMMNUM$i"
        marknums[i] = W_CacheLumpName(namebuf)
    }
}

internal fun DoomEngineCore.AM_unloadPics() {
    // C: Z_ChangeTag(marknums[i], PU_CACHE) -- lump caching handles this.
}

internal fun DoomEngineCore.AM_clearMarks() {
    for (i in 0 until AM_NUMMARKPOINTS)
        markpoints[i].x = -1 // means empty
    markpointnum = 0
}

//
// should be called at the start of every level
// right now, i figure it out myself
//
internal fun DoomEngineCore.AM_LevelInit() {
    leveljuststarted = 0

    f_x = 0
    f_y = 0
    f_w = finit_width
    f_h = finit_height

    AM_clearMarks()

    AM_findMinMaxBoundaries()
    scale_mtof = FixedDiv(min_scale_mtof, (0.7 * FRACUNIT).toInt())
    if (scale_mtof > max_scale_mtof)
        scale_mtof = min_scale_mtof
    scale_ftom = FixedDiv(FRACUNIT, scale_mtof)
}

//
//
//
internal fun DoomEngineCore.AM_Stop() {
    // (sic) vanilla passes { 0, ev_keyup, AM_MSGEXITED }: type 0, data1 ev_keyup,
    // data2 AM_MSGEXITED -- so ST_Responder never actually sees the exit message.
    val st_notify = event_t(0, ev_keyup, AM_MSGEXITED)

    AM_unloadPics()
    automapactive = false
    ST_Responder(st_notify)
    stopped = true
}

private var DoomEngineCore.lastlevel
    get() = stateAutomap.lastlevel
    set(value) { stateAutomap.lastlevel = value }
private var DoomEngineCore.lastepisode
    get() = stateAutomap.lastepisode
    set(value) { stateAutomap.lastepisode = value }

internal fun DoomEngineCore.AM_Start() {
    if (!stopped) AM_Stop()
    stopped = false
    if (lastlevel != gamemap || lastepisode != gameepisode) {
        AM_LevelInit()
        lastlevel = gamemap
        lastepisode = gameepisode
    }
    AM_initVariables()
    AM_loadPics()
}

//
// set the window scale to the maximum size
//
internal fun DoomEngineCore.AM_minOutWindowScale() {
    scale_mtof = min_scale_mtof
    scale_ftom = FixedDiv(FRACUNIT, scale_mtof)
    AM_activateNewScale()
}

//
// set the window scale to the minimum size
//
internal fun DoomEngineCore.AM_maxOutWindowScale() {
    scale_mtof = max_scale_mtof
    scale_ftom = FixedDiv(FRACUNIT, scale_mtof)
    AM_activateNewScale()
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

internal fun DoomEngineCore.AM_Responder(ev: event_t): Boolean {
    var rc: Boolean

    rc = false

    if (!automapactive) {
        if (ev.type == ev_keydown && ev.data1 == AM_STARTKEY) {
            AM_Start()
            viewactive = false
            rc = true
        }
    } else if (ev.type == ev_keydown) {
        rc = true
        when (ev.data1) {
            AM_PANRIGHTKEY -> { // pan right
                if (followplayer == 0) m_paninc.x = FTOM(F_PANINC)
                else rc = false
            }
            AM_PANLEFTKEY -> { // pan left
                if (followplayer == 0) m_paninc.x = -FTOM(F_PANINC)
                else rc = false
            }
            AM_PANUPKEY -> { // pan up
                if (followplayer == 0) m_paninc.y = FTOM(F_PANINC)
                else rc = false
            }
            AM_PANDOWNKEY -> { // pan down
                if (followplayer == 0) m_paninc.y = -FTOM(F_PANINC)
                else rc = false
            }
            AM_ZOOMOUTKEY -> { // zoom out
                mtof_zoommul = M_ZOOMOUT
                ftom_zoommul = M_ZOOMIN
            }
            AM_ZOOMINKEY -> { // zoom in
                mtof_zoommul = M_ZOOMIN
                ftom_zoommul = M_ZOOMOUT
            }
            AM_ENDKEY -> {
                bigstate = 0
                viewactive = true
                AM_Stop()
            }
            AM_GOBIGKEY -> {
                bigstate = if (bigstate == 0) 1 else 0
                if (bigstate != 0) {
                    AM_saveScaleAndLoc()
                    AM_minOutWindowScale()
                } else AM_restoreScaleAndLoc()
            }
            AM_FOLLOWKEY -> {
                followplayer = if (followplayer == 0) 1 else 0
                f_oldloc.x = MAXINT
                plr.message = if (followplayer != 0) AMSTR_FOLLOWON else AMSTR_FOLLOWOFF
            }
            AM_GRIDKEY -> {
                grid = if (grid == 0) 1 else 0
                plr.message = if (grid != 0) AMSTR_GRIDON else AMSTR_GRIDOFF
            }
            AM_MARKKEY -> {
                buffer = "$AMSTR_MARKEDSPOT $markpointnum"
                plr.message = buffer
                AM_addMark()
            }
            AM_CLEARMARKKEY -> {
                AM_clearMarks()
                plr.message = AMSTR_MARKSCLEARED
            }
            else -> {
                cheatstate = 0
                rc = false
            }
        }
        if (deathmatch == 0 && cht_CheckCheat(cheat_amap, ev.data1) != 0) {
            rc = false
            cheating = (cheating + 1) % 3
        }
    } else if (ev.type == ev_keyup) {
        rc = false
        when (ev.data1) {
            AM_PANRIGHTKEY ->
                if (followplayer == 0) m_paninc.x = 0
            AM_PANLEFTKEY ->
                if (followplayer == 0) m_paninc.x = 0
            AM_PANUPKEY ->
                if (followplayer == 0) m_paninc.y = 0
            AM_PANDOWNKEY ->
                if (followplayer == 0) m_paninc.y = 0
            AM_ZOOMOUTKEY, AM_ZOOMINKEY -> {
                mtof_zoommul = FRACUNIT
                ftom_zoommul = FRACUNIT
            }
        }
    }

    return rc
}

//
// Zooming
//
internal fun DoomEngineCore.AM_changeWindowScale() {
    // Change the scaling multipliers
    scale_mtof = FixedMul(scale_mtof, mtof_zoommul)
    scale_ftom = FixedDiv(FRACUNIT, scale_mtof)

    if (scale_mtof < min_scale_mtof)
        AM_minOutWindowScale()
    else if (scale_mtof > max_scale_mtof)
        AM_maxOutWindowScale()
    else
        AM_activateNewScale()
}

//
//
//
internal fun DoomEngineCore.AM_doFollowPlayer() {
    if (f_oldloc.x != plr.mo!!.x || f_oldloc.y != plr.mo!!.y) {
        m_x = FTOM(MTOF(plr.mo!!.x)) - m_w / 2
        m_y = FTOM(MTOF(plr.mo!!.y)) - m_h / 2
        m_x2 = m_x + m_w
        m_y2 = m_y + m_h
        f_oldloc.x = plr.mo!!.x
        f_oldloc.y = plr.mo!!.y

        //  m_x = FTOM(MTOF(plr->mo->x - m_w/2));
        //  m_y = FTOM(MTOF(plr->mo->y - m_h/2));
        //  m_x = plr->mo->x - m_w/2;
        //  m_y = plr->mo->y - m_h/2;
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

internal fun DoomEngineCore.AM_updateLightLev() {
    // Change light level
    if (amclock > nexttic) {
        lightlev = litelevels[litelevelscnt]
        litelevelscnt++
        if (litelevelscnt == litelevels.size) litelevelscnt = 0
        nexttic = amclock + 6 - (amclock % 6)
    }
}

//
// Updates on Game Tick
//
internal fun DoomEngineCore.AM_Ticker() {
    if (!automapactive)
        return

    amclock++

    if (followplayer != 0)
        AM_doFollowPlayer()

    // Change the zoom if necessary
    if (ftom_zoommul != FRACUNIT)
        AM_changeWindowScale()

    // Change x,y location
    if (m_paninc.x != 0 || m_paninc.y != 0)
        AM_changeWindowLoc()

    // Update light level
    // AM_updateLightLev();
}

//
// Clear automap frame buffer.
//
internal fun DoomEngineCore.AM_clearFB(color: Int) {
    fb.fill(color.toByte(), 0, f_w * f_h)
}

//
// Automap clipping of lines.
//
// Based on Cohen-Sutherland clipping algorithm but with a slightly
// faster reject and precalculated slopes.  If the speed is needed,
// use a hash algorithm to handle  the common cases.
//
private const val LEFT = 1
private const val RIGHT = 2
private const val BOTTOM = 4
private const val TOP = 8

// The C DOOUTCODE macro.
private fun DoomEngineCore.DOOUTCODE(mx: Int, my: Int): Int {
    var oc = 0
    if (my < 0) oc = oc or TOP
    else if (my >= f_h) oc = oc or BOTTOM
    if (mx < 0) oc = oc or LEFT
    else if (mx >= f_w) oc = oc or RIGHT
    return oc
}

internal fun DoomEngineCore.AM_clipMline(ml: mline_t, fl: fline_t): Boolean {
    var outcode1 = 0
    var outcode2 = 0
    var outside: Int

    val tmp = fpoint_t()
    var dx: Int
    var dy: Int

    // do trivial rejects and outcodes
    if (ml.a.y > m_y2)
        outcode1 = TOP
    else if (ml.a.y < m_y)
        outcode1 = BOTTOM

    if (ml.b.y > m_y2)
        outcode2 = TOP
    else if (ml.b.y < m_y)
        outcode2 = BOTTOM

    if ((outcode1 and outcode2) != 0)
        return false // trivially outside

    if (ml.a.x < m_x)
        outcode1 = outcode1 or LEFT
    else if (ml.a.x > m_x2)
        outcode1 = outcode1 or RIGHT

    if (ml.b.x < m_x)
        outcode2 = outcode2 or LEFT
    else if (ml.b.x > m_x2)
        outcode2 = outcode2 or RIGHT

    if ((outcode1 and outcode2) != 0)
        return false // trivially outside

    // transform to frame-buffer coordinates.
    fl.a.x = CXMTOF(ml.a.x)
    fl.a.y = CYMTOF(ml.a.y)
    fl.b.x = CXMTOF(ml.b.x)
    fl.b.y = CYMTOF(ml.b.y)

    outcode1 = DOOUTCODE(fl.a.x, fl.a.y)
    outcode2 = DOOUTCODE(fl.b.x, fl.b.y)

    if ((outcode1 and outcode2) != 0)
        return false

    while ((outcode1 or outcode2) != 0) {
        // may be partially inside box
        // find an outside point
        outside = if (outcode1 != 0) outcode1 else outcode2

        // clip to each side
        if ((outside and TOP) != 0) {
            dy = fl.a.y - fl.b.y
            dx = fl.b.x - fl.a.x
            tmp.x = fl.a.x + (dx * (fl.a.y)) / dy
            tmp.y = 0
        } else if ((outside and BOTTOM) != 0) {
            dy = fl.a.y - fl.b.y
            dx = fl.b.x - fl.a.x
            tmp.x = fl.a.x + (dx * (fl.a.y - f_h)) / dy
            tmp.y = f_h - 1
        } else if ((outside and RIGHT) != 0) {
            dy = fl.b.y - fl.a.y
            dx = fl.b.x - fl.a.x
            tmp.y = fl.a.y + (dy * (f_w - 1 - fl.a.x)) / dx
            tmp.x = f_w - 1
        } else if ((outside and LEFT) != 0) {
            dy = fl.b.y - fl.a.y
            dx = fl.b.x - fl.a.x
            tmp.y = fl.a.y + (dy * (-fl.a.x)) / dx
            tmp.x = 0
        }

        if (outside == outcode1) {
            // C struct copy: fl->a = tmp
            fl.a.x = tmp.x
            fl.a.y = tmp.y
            outcode1 = DOOUTCODE(fl.a.x, fl.a.y)
        } else {
            // C struct copy: fl->b = tmp
            fl.b.x = tmp.x
            fl.b.y = tmp.y
            outcode2 = DOOUTCODE(fl.b.x, fl.b.y)
        }

        if ((outcode1 and outcode2) != 0)
            return false // trivially outside
    }

    return true
}

private var DoomEngineCore.fuck
    get() = stateAutomap.fuck
    set(value) { stateAutomap.fuck = value }

internal fun DoomEngineCore.AM_drawFline(fl: fline_t, color: Int) {
    var x: Int
    var y: Int
    var d: Int

    // For debugging only
    if (fl.a.x < 0 || fl.a.x >= f_w
        || fl.a.y < 0 || fl.a.y >= f_h
        || fl.b.x < 0 || fl.b.x >= f_w
        || fl.b.y < 0 || fl.b.y >= f_h
    ) {
        println("fuck $fuck \r")
        fuck++
        return
    }

    // C: #define PUTDOT(xx,yy,cc) fb[(yy)*f_w+(xx)]=(cc)

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
            fb[y * f_w + x] = color.toByte() // PUTDOT(x,y,color)
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
            fb[y * f_w + x] = color.toByte() // PUTDOT(x,y,color)
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

internal fun DoomEngineCore.AM_drawMline(ml: mline_t, color: Int) {
    if (AM_clipMline(ml, fl))
        AM_drawFline(fl, color) // draws it on frame buffer using fb coords
}

//
// Draws flat (floor/ceiling tile) aligned grid lines.
//
internal fun DoomEngineCore.AM_drawGrid(color: Int) {
    var x: fixed_t
    var y: fixed_t
    var start: fixed_t
    var end: fixed_t
    val ml = mline_t()

    // Figure out start of vertical gridlines
    start = m_x
    if ((start - bmaporgx) % (MAPBLOCKUNITS shl FRACBITS) != 0)
        start += (MAPBLOCKUNITS shl FRACBITS) -
            ((start - bmaporgx) % (MAPBLOCKUNITS shl FRACBITS))
    end = m_x + m_w

    // draw vertical gridlines
    ml.a.y = m_y
    ml.b.y = m_y + m_h
    x = start
    while (x < end) {
        ml.a.x = x
        ml.b.x = x
        AM_drawMline(ml, color)
        x += (MAPBLOCKUNITS shl FRACBITS)
    }

    // Figure out start of horizontal gridlines
    start = m_y
    if ((start - bmaporgy) % (MAPBLOCKUNITS shl FRACBITS) != 0)
        start += (MAPBLOCKUNITS shl FRACBITS) -
            ((start - bmaporgy) % (MAPBLOCKUNITS shl FRACBITS))
    end = m_y + m_h

    // draw horizontal gridlines
    ml.a.x = m_x
    ml.b.x = m_x + m_w
    y = start
    while (y < end) {
        ml.a.y = y
        ml.b.y = y
        AM_drawMline(ml, color)
        y += (MAPBLOCKUNITS shl FRACBITS)
    }
}

private val DoomEngineCore.l
    get() = stateAutomap.l

internal fun DoomEngineCore.AM_drawWalls() {
    for (i in 0 until numlines) {
        l.a.x = lines[i].v1.x
        l.a.y = lines[i].v1.y
        l.b.x = lines[i].v2.x
        l.b.y = lines[i].v2.y
        if (cheating != 0 || (lines[i].flags and ML_MAPPED) != 0) {
            if ((lines[i].flags and LINE_NEVERSEE) != 0 && cheating == 0)
                continue
            if (lines[i].backsector == null) {
                AM_drawMline(l, WALLCOLORS + lightlev)
            } else {
                if (lines[i].special == 39) { // teleporters
                    AM_drawMline(l, WALLCOLORS + WALLRANGE / 2)
                } else if ((lines[i].flags and ML_SECRET) != 0) { // secret door
                    if (cheating != 0) AM_drawMline(l, SECRETWALLCOLORS + lightlev)
                    else AM_drawMline(l, WALLCOLORS + lightlev)
                } else if (lines[i].backsector!!.floorheight
                    != lines[i].frontsector!!.floorheight
                ) {
                    AM_drawMline(l, FDWALLCOLORS + lightlev) // floor level change
                } else if (lines[i].backsector!!.ceilingheight
                    != lines[i].frontsector!!.ceilingheight
                ) {
                    AM_drawMline(l, CDWALLCOLORS + lightlev) // ceiling level change
                } else if (cheating != 0) {
                    AM_drawMline(l, TSWALLCOLORS + lightlev)
                }
            }
        } else if (plr.powers[pw_allmap] != 0) {
            if ((lines[i].flags and LINE_NEVERSEE) == 0) AM_drawMline(l, GRAYS + 3)
        }
    }
}

//
// Rotation in 2D.
// Used to rotate player arrow line character.
//
// C signature: AM_rotate(fixed_t* x, fixed_t* y, angle_t a) -- both call
// sites pass the two fields of an mpoint_t, so it takes the point here.
internal fun DoomEngineCore.AM_rotate(pt: mpoint_t, a: angle_t) {
    val tmpx =
        FixedMul(pt.x, finecosine[(a shr ANGLETOFINESHIFT).toInt()]) -
            FixedMul(pt.y, finesine[(a shr ANGLETOFINESHIFT).toInt()])

    pt.y =
        FixedMul(pt.x, finesine[(a shr ANGLETOFINESHIFT).toInt()]) +
            FixedMul(pt.y, finecosine[(a shr ANGLETOFINESHIFT).toInt()])

    pt.x = tmpx
}

internal fun DoomEngineCore.AM_drawLineCharacter(
    lineguy: Array<mline_t>,
    lineguylines: Int,
    scale: fixed_t,
    angle: angle_t,
    color: Int,
    x: fixed_t,
    y: fixed_t,
) {
    val l = mline_t()

    for (i in 0 until lineguylines) {
        l.a.x = lineguy[i].a.x
        l.a.y = lineguy[i].a.y

        if (scale != 0) {
            l.a.x = FixedMul(scale, l.a.x)
            l.a.y = FixedMul(scale, l.a.y)
        }

        if (angle != 0u)
            AM_rotate(l.a, angle)

        l.a.x += x
        l.a.y += y

        l.b.x = lineguy[i].b.x
        l.b.y = lineguy[i].b.y

        if (scale != 0) {
            l.b.x = FixedMul(scale, l.b.x)
            l.b.y = FixedMul(scale, l.b.y)
        }

        if (angle != 0u)
            AM_rotate(l.b, angle)

        l.b.x += x
        l.b.y += y

        AM_drawMline(l, color)
    }
}

private val DoomEngineCore.their_colors
    get() = stateAutomap.their_colors

internal fun DoomEngineCore.AM_drawPlayers() {
    var p: player_t
    var their_color = -1
    var color: Int

    if (!netgame) {
        if (cheating != 0)
            AM_drawLineCharacter(
                cheat_player_arrow, NUMCHEATPLYRLINES, 0,
                plr.mo!!.angle, WHITE, plr.mo!!.x, plr.mo!!.y
            )
        else
            AM_drawLineCharacter(
                player_arrow, NUMPLYRLINES, 0, plr.mo!!.angle,
                WHITE, plr.mo!!.x, plr.mo!!.y
            )
        return
    }

    for (i in 0 until MAXPLAYERS) {
        their_color++
        p = players[i]

        if ((deathmatch != 0 && !singledemo) && p !== plr)
            continue

        if (!playeringame[i])
            continue

        color = if (p.powers[pw_invisibility] != 0)
            246 // *close* to black
        else
            their_colors[their_color]

        AM_drawLineCharacter(
            player_arrow, NUMPLYRLINES, 0, p.mo!!.angle,
            color, p.mo!!.x, p.mo!!.y
        )
    }
}

internal fun DoomEngineCore.AM_drawThings(colors: Int, colorrange: Int) {
    var t: mobj_t?

    for (i in 0 until numsectors) {
        t = sectors[i].thinglist
        while (t != null) {
            AM_drawLineCharacter(
                thintriangle_guy, NUMTHINTRIANGLEGUYLINES,
                16 shl FRACBITS, t.angle, colors + lightlev, t.x, t.y
            )
            t = t.snext
        }
    }
}

internal fun DoomEngineCore.AM_drawMarks() {
    for (i in 0 until AM_NUMMARKPOINTS) {
        if (markpoints[i].x != -1) {
            //      w = SHORT(marknums[i]->width);
            //      h = SHORT(marknums[i]->height);
            val w = 5 // because something's wrong with the wad, i guess
            val h = 6 // because something's wrong with the wad, i guess
            val fx = CXMTOF(markpoints[i].x)
            val fy = CYMTOF(markpoints[i].y)
            if (fx >= f_x && fx <= f_w - w && fy >= f_y && fy <= f_h - h)
                V_DrawPatch(fx, fy, FB, marknums[i]!!)
        }
    }
}

internal fun DoomEngineCore.AM_drawCrosshair(color: Int) {
    fb[(f_w * (f_h + 1)) / 2] = color.toByte() // single point for now
}

internal fun DoomEngineCore.AM_Drawer() {
    if (!automapactive) return

    AM_clearFB(BACKGROUND)
    if (grid != 0)
        AM_drawGrid(GRIDCOLORS)
    AM_drawWalls()
    AM_drawPlayers()
    if (cheating == 2)
        AM_drawThings(THINGCOLORS, THINGRANGE)
    AM_drawCrosshair(XHAIRCOLORS)

    AM_drawMarks()

    V_MarkRect(f_x, f_y, f_w, f_h)
}
