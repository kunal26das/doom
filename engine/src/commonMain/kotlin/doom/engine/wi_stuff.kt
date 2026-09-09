// Port of linuxdoom-1.10 wi_stuff.c/wi_stuff.h -- intermission screens.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// Data needed to add patches to full screen intermission pics.
// Patches are statistics messages, and animations.
// Loads of by-pixel layout and placement, offsets etc.
//

//
// Different vetween registered DOOM (1994) and
//  Ultimate DOOM - Final edition (retail, 1995?).
// This is supposedly ignored for commercial
//  release (aka DOOM II), which had 34 maps
//  in one episode. So there.
private const val NUMEPISODES = 4
private const val NUMMAPS = 9

// in tics
//U #define PAUSELEN		(TICRATE*2)
//U #define SCORESTEP		100
//U #define ANIMPERIOD		32
// pixel distance from "(YOU)" to "PLAYER N"
//U #define STARDIST		10
//U #define WK 1

// GLOBAL LOCATIONS
private const val WI_TITLEY = 2
private const val WI_SPACINGY = 33

// SINGPLE-PLAYER STUFF
private const val SP_STATSX = 50
private const val SP_STATSY = 50

private const val SP_TIMEX = 16
private const val SP_TIMEY = SCREENHEIGHT - 32

// NET GAME STUFF
private const val NG_STATSY = 50
// C: #define NG_STATSX (32 + SHORT(star->width)/2 + 32*!dofrags)
private fun DoomEngineCore.NG_STATSX(): Int = 32 + patchWidth(star) / 2 + 32 * (if (dofrags != 0) 0 else 1)

private const val NG_SPACINGX = 64

// DEATHMATCH STUFF
private const val DM_MATRIXX = 42
private const val DM_MATRIXY = 68

private const val DM_SPACINGX = 40

private const val DM_TOTALSX = 269

private const val DM_KILLERSX = 10
private const val DM_KILLERSY = 100
private const val DM_VICTIMSX = 5
private const val DM_VICTIMSY = 50

// States for the intermission (stateenum_t, wi_stuff.h)
internal const val NoState = -1
internal const val StatCount = 0
internal const val ShowNextLoc = 1

// animenum_t
internal const val ANIM_ALWAYS = 0
private const val ANIM_RANDOM = 1
internal const val ANIM_LEVEL = 2


private val DoomEngineCore.lnodes: Array<Array<point_t>>
    get() = stateIntermission.lnodes

private val DoomEngineCore.epsd0animinfo
    get() = stateIntermission.epsd0animinfo

private val DoomEngineCore.epsd1animinfo
    get() = stateIntermission.epsd1animinfo

private val DoomEngineCore.epsd2animinfo
    get() = stateIntermission.epsd2animinfo

private val DoomEngineCore.NUMANIMS
    get() = stateIntermission.NUMANIMS

private val DoomEngineCore.wi_anims
    get() = stateIntermission.wi_anims

//
// GENERAL DATA
//

//
// Locally used stuff.
//
private const val FB = 0

// States for single-player
private const val SP_KILLS = 0
private const val SP_ITEMS = 2
private const val SP_SECRET = 4
private const val SP_FRAGS = 6
private const val SP_TIME = 8
// C: #define SP_PAR ST_TIME -- ST_TIME is defined nowhere; the macro is never expanded.

private const val SP_PAUSE = 1

// in seconds
private const val SHOWNEXTLOCDELAY = 4
//#define SHOWLASTLOCDELAY	SHOWNEXTLOCDELAY

private var DoomEngineCore.acceleratestage
    get() = stateIntermission.acceleratestage
    set(value) { stateIntermission.acceleratestage = value }

private var DoomEngineCore.me
    get() = stateIntermission.me
    set(value) { stateIntermission.me = value }

private var DoomEngineCore.state
    get() = stateIntermission.state
    set(value) { stateIntermission.state = value }

private var DoomEngineCore.wbs: wbstartstruct_t
    get() = stateIntermission.wbs
    set(value) { stateIntermission.wbs = value }

private var DoomEngineCore.plrs: Array<wbplayerstruct_t>
    get() = stateIntermission.plrs
    set(value) { stateIntermission.plrs = value }

private var DoomEngineCore.cnt
    get() = stateIntermission.cnt
    set(value) { stateIntermission.cnt = value }

private var DoomEngineCore.bcnt
    get() = stateIntermission.bcnt
    set(value) { stateIntermission.bcnt = value }

private var DoomEngineCore.firstrefresh
    get() = stateIntermission.firstrefresh
    set(value) { stateIntermission.firstrefresh = value }

private val DoomEngineCore.cnt_kills
    get() = stateIntermission.cnt_kills
private val DoomEngineCore.cnt_items
    get() = stateIntermission.cnt_items
private val DoomEngineCore.cnt_secret
    get() = stateIntermission.cnt_secret
private var DoomEngineCore.cnt_time
    get() = stateIntermission.cnt_time
    set(value) { stateIntermission.cnt_time = value }
private var DoomEngineCore.cnt_par
    get() = stateIntermission.cnt_par
    set(value) { stateIntermission.cnt_par = value }
private var DoomEngineCore.cnt_pause
    get() = stateIntermission.cnt_pause
    set(value) { stateIntermission.cnt_pause = value }

private var DoomEngineCore.NUMCMAPS
    get() = stateIntermission.NUMCMAPS
    set(value) { stateIntermission.NUMCMAPS = value }

//
//	GRAPHICS
//

private var DoomEngineCore.bg: ByteArray
    get() = stateIntermission.bg
    set(value) { stateIntermission.bg = value }

private val DoomEngineCore.yah
    get() = stateIntermission.yah

private var DoomEngineCore.splat: ByteArray
    get() = stateIntermission.splat
    set(value) { stateIntermission.splat = value }

private var DoomEngineCore.percent: ByteArray
    get() = stateIntermission.percent
    set(value) { stateIntermission.percent = value }
private var DoomEngineCore.colon: ByteArray
    get() = stateIntermission.colon
    set(value) { stateIntermission.colon = value }

private val DoomEngineCore.num
    get() = stateIntermission.num

private var DoomEngineCore.wiminus: ByteArray
    get() = stateIntermission.wiminus
    set(value) { stateIntermission.wiminus = value }

private var DoomEngineCore.finished: ByteArray
    get() = stateIntermission.finished
    set(value) { stateIntermission.finished = value }

private var DoomEngineCore.entering: ByteArray
    get() = stateIntermission.entering
    set(value) { stateIntermission.entering = value }

private var DoomEngineCore.sp_secret: ByteArray
    get() = stateIntermission.sp_secret
    set(value) { stateIntermission.sp_secret = value }

private var DoomEngineCore.kills: ByteArray
    get() = stateIntermission.kills
    set(value) { stateIntermission.kills = value }
private var DoomEngineCore.secret: ByteArray
    get() = stateIntermission.secret
    set(value) { stateIntermission.secret = value }
private var DoomEngineCore.items: ByteArray
    get() = stateIntermission.items
    set(value) { stateIntermission.items = value }
private var DoomEngineCore.frags: ByteArray
    get() = stateIntermission.frags
    set(value) { stateIntermission.frags = value }

private var DoomEngineCore.time: ByteArray
    get() = stateIntermission.time
    set(value) { stateIntermission.time = value }
private var DoomEngineCore.par: ByteArray
    get() = stateIntermission.par
    set(value) { stateIntermission.par = value }
private var DoomEngineCore.sucks: ByteArray
    get() = stateIntermission.sucks
    set(value) { stateIntermission.sucks = value }

private var DoomEngineCore.killers: ByteArray
    get() = stateIntermission.killers
    set(value) { stateIntermission.killers = value }
private var DoomEngineCore.victims: ByteArray
    get() = stateIntermission.victims
    set(value) { stateIntermission.victims = value }

private var DoomEngineCore.total: ByteArray
    get() = stateIntermission.total
    set(value) { stateIntermission.total = value }
private var DoomEngineCore.star: ByteArray
    get() = stateIntermission.star
    set(value) { stateIntermission.star = value }
private var DoomEngineCore.bstar: ByteArray
    get() = stateIntermission.bstar
    set(value) { stateIntermission.bstar = value }

private val DoomEngineCore.p
    get() = stateIntermission.p

private val DoomEngineCore.bp
    get() = stateIntermission.bp

private var DoomEngineCore.lnames: Array<ByteArray>
    get() = stateIntermission.lnames
    set(value) { stateIntermission.lnames = value }

//
// CODE
//

// slam background
// UNUSED static unsigned char *background=0;

internal fun DoomEngineCore.WI_slamBackground() {
    // memcpy(screens[0], screens[1], SCREENWIDTH * SCREENHEIGHT)
    screens[1].copyInto(screens[0], 0, 0, SCREENWIDTH * SCREENHEIGHT)
    V_MarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)
}

// The ticker is used to detect keys
//  because of timing issues in netgames.
internal fun DoomEngineCore.WI_Responder(ev: event_t): Boolean {
    return false
}

// Draws "<Levelname> Finished!"
internal fun DoomEngineCore.WI_drawLF() {
    var y = WI_TITLEY

    // draw <LevelName>
    V_DrawPatch((SCREENWIDTH - patchWidth(lnames[wbs.last])) / 2,
        y, FB, lnames[wbs.last])

    // draw "Finished!"
    y += (5 * patchHeight(lnames[wbs.last])) / 4

    V_DrawPatch((SCREENWIDTH - patchWidth(finished)) / 2,
        y, FB, finished)
}

// Draws "Entering <LevelName>"
internal fun DoomEngineCore.WI_drawEL() {
    var y = WI_TITLEY

    // draw "Entering"
    V_DrawPatch((SCREENWIDTH - patchWidth(entering)) / 2,
        y, FB, entering)

    // draw level
    y += (5 * patchHeight(lnames[wbs.next])) / 4

    V_DrawPatch((SCREENWIDTH - patchWidth(lnames[wbs.next])) / 2,
        y, FB, lnames[wbs.next])
}

internal fun DoomEngineCore.WI_drawOnLnode(n: Int, c: Array<ByteArray>) {
    var i: Int
    var left: Int
    var top: Int
    var right: Int
    var bottom: Int
    var fits = false

    i = 0
    do {
        left = lnodes[wbs.epsd][n].x - patchLeftOffset(c[i])
        top = lnodes[wbs.epsd][n].y - patchTopOffset(c[i])
        right = left + patchWidth(c[i])
        bottom = top + patchHeight(c[i])

        if (left >= 0
            && right < SCREENWIDTH
            && top >= 0
            && bottom < SCREENHEIGHT
        ) {
            fits = true
        } else {
            i++
        }
    } while (!fits && i != 2)

    if (fits && i < 2) {
        V_DrawPatch(lnodes[wbs.epsd][n].x, lnodes[wbs.epsd][n].y,
            FB, c[i])
    } else {
        // DEBUG
        print("Could not place patch on level ${n + 1}")
    }
}

internal fun DoomEngineCore.WI_initAnimatedBack() {
    if (gamemode == commercial)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until NUMANIMS[wbs.epsd]) {
        val a = wi_anims[wbs.epsd][i]

        // init variables
        a.ctr = -1

        // specify the next time to draw it
        if (a.type == ANIM_ALWAYS)
            a.nexttic = bcnt + 1 + (M_Random() % a.period)
        else if (a.type == ANIM_RANDOM)
            a.nexttic = bcnt + 1 + a.data2 + (M_Random() % a.data1)
        else if (a.type == ANIM_LEVEL)
            a.nexttic = bcnt + 1
    }
}

internal fun DoomEngineCore.WI_updateAnimatedBack() {
    if (gamemode == commercial)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until NUMANIMS[wbs.epsd]) {
        val a = wi_anims[wbs.epsd][i]

        if (bcnt == a.nexttic) {
            when (a.type) {
                ANIM_ALWAYS -> {
                    a.ctr++
                    if (a.ctr >= a.nanims) a.ctr = 0
                    a.nexttic = bcnt + a.period
                }

                ANIM_RANDOM -> {
                    a.ctr++
                    if (a.ctr == a.nanims) {
                        a.ctr = -1
                        a.nexttic = bcnt + a.data2 + (M_Random() % a.data1)
                    } else a.nexttic = bcnt + a.period
                }

                ANIM_LEVEL -> {
                    // gawd-awful hack for level anims
                    if (!(state == StatCount && i == 7)
                        && wbs.next == a.data1
                    ) {
                        a.ctr++
                        if (a.ctr == a.nanims) a.ctr--
                        a.nexttic = bcnt + a.period
                    }
                }
            }
        }
    }
}

internal fun DoomEngineCore.WI_drawAnimatedBack() {
    // (linuxdoom-1.10 literally wrote "if (commercial)" here -- the bare enum
    // constant, always true, so animations never drew. DOS vanilla checked
    // gamemode; chocolate-doom confirms.)
    if (gamemode == commercial)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until NUMANIMS[wbs.epsd]) {
        val a = wi_anims[wbs.epsd][i]

        if (a.ctr >= 0)
            V_DrawPatch(a.loc.x, a.loc.y, FB, a.p[a.ctr])
    }
}

//
// Draws a number.
// If digits > 0, then use that many digits minimum,
//  otherwise only use as many as necessary.
// Returns new x position.
//

internal fun DoomEngineCore.WI_drawNum(x: Int, y: Int, n: Int, digits: Int): Int {
    var x = x
    var n = n
    var digits = digits

    val fontwidth = patchWidth(num[0])
    val neg: Boolean
    var temp: Int

    if (digits < 0) {
        if (n == 0) {
            // make variable-length zeros 1 digit long
            digits = 1
        } else {
            // figure out # of digits in #
            digits = 0
            temp = n

            while (temp != 0) {
                temp /= 10
                digits++
            }
        }
    }

    neg = n < 0
    if (neg)
        n = -n

    // if non-number, do not draw it
    if (n == 1994)
        return 0

    // draw the new number
    while (digits != 0) {   // C: while (digits--)
        digits--
        x -= fontwidth
        V_DrawPatch(x, y, FB, num[n % 10])
        n /= 10
    }

    // draw a minus sign if necessary
    if (neg) {
        x -= 8
        V_DrawPatch(x, y, FB, wiminus)
    }

    return x
}

internal fun DoomEngineCore.WI_drawPercent(x: Int, y: Int, p: Int) {
    if (p < 0)
        return

    V_DrawPatch(x, y, FB, percent)
    WI_drawNum(x, y, p, -1)
}

//
// Display level completion time and par,
//  or "sucks" message if overflow.
//
internal fun DoomEngineCore.WI_drawTime(x: Int, y: Int, t: Int) {
    var x = x

    var div: Int
    var n: Int

    if (t < 0)
        return

    if (t <= 61 * 59) {
        div = 1

        do {
            n = (t / div) % 60
            x = WI_drawNum(x, y, n, 2) - patchWidth(colon)
            div *= 60

            // draw
            if (div == 60 || t / div != 0)
                V_DrawPatch(x, y, FB, colon)
        } while (t / div != 0)
    } else {
        // "sucks"
        V_DrawPatch(x - patchWidth(sucks), y, FB, sucks)
    }
}

internal fun DoomEngineCore.WI_End() {
    WI_unloadData()
}

internal fun DoomEngineCore.WI_initNoState() {
    state = NoState
    acceleratestage = 0
    cnt = 10
}

internal fun DoomEngineCore.WI_updateNoState() {
    WI_updateAnimatedBack()

    cnt--
    if (cnt == 0) {     // C: if (!--cnt)
        WI_End()
        G_WorldDone()
    }
}

private var DoomEngineCore.snl_pointeron
    get() = stateIntermission.snl_pointeron
    set(value) { stateIntermission.snl_pointeron = value }

internal fun DoomEngineCore.WI_initShowNextLoc() {
    state = ShowNextLoc
    acceleratestage = 0
    cnt = SHOWNEXTLOCDELAY * TICRATE

    WI_initAnimatedBack()
}

internal fun DoomEngineCore.WI_updateShowNextLoc() {
    WI_updateAnimatedBack()

    cnt--
    if (cnt == 0 || acceleratestage != 0)   // C: if (!--cnt || acceleratestage)
        WI_initNoState()
    else
        snl_pointeron = (cnt and 31) < 20
}

internal fun DoomEngineCore.WI_drawShowNextLoc() {
    val last: Int

    WI_slamBackground()

    // draw animated background
    WI_drawAnimatedBack()

    if (gamemode != commercial) {
        if (wbs.epsd > 2) {
            WI_drawEL()
            return
        }

        last = if (wbs.last == 8) wbs.next - 1 else wbs.last

        // draw a splat on taken cities.
        for (i in 0..last)
            WI_drawOnLnode(i, arrayOf(splat))

        // splat the secret level?
        if (wbs.didsecret)
            WI_drawOnLnode(8, arrayOf(splat))

        // draw flashing ptr
        if (snl_pointeron)
            WI_drawOnLnode(wbs.next, yah)
    }

    // draws which level you are entering..
    if ((gamemode != commercial)
        || wbs.next != 30
    )
        WI_drawEL()
}

internal fun DoomEngineCore.WI_drawNoState() {
    snl_pointeron = true
    WI_drawShowNextLoc()
}

internal fun DoomEngineCore.WI_fragSum(playernum: Int): Int {
    var frags = 0

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]
            && i != playernum
        ) {
            frags += plrs[playernum].frags[i]
        }
    }

    // JDC hack - negative frags.
    frags -= plrs[playernum].frags[playernum]
    // UNUSED if (frags < 0)
    // 	frags = 0;

    return frags
}

private var DoomEngineCore.dm_state
    get() = stateIntermission.dm_state
    set(value) { stateIntermission.dm_state = value }
private val DoomEngineCore.dm_frags
    get() = stateIntermission.dm_frags
private val DoomEngineCore.dm_totals
    get() = stateIntermission.dm_totals

internal fun DoomEngineCore.WI_initDeathmatchStats() {
    state = StatCount
    acceleratestage = 0
    dm_state = 1

    cnt_pause = TICRATE

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            for (j in 0 until MAXPLAYERS)
                if (playeringame[j])
                    dm_frags[i][j] = 0

            dm_totals[i] = 0
        }
    }

    WI_initAnimatedBack()
}

internal fun DoomEngineCore.WI_updateDeathmatchStats() {
    var stillticking: Boolean

    WI_updateAnimatedBack()

    if (acceleratestage != 0 && dm_state != 4) {
        acceleratestage = 0

        for (i in 0 until MAXPLAYERS) {
            if (playeringame[i]) {
                for (j in 0 until MAXPLAYERS)
                    if (playeringame[j])
                        dm_frags[i][j] = plrs[i].frags[j]

                dm_totals[i] = WI_fragSum(i)
            }
        }

        S_StartSound(null, sfx_barexp)
        dm_state = 4
    }

    if (dm_state == 2) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (playeringame[i]) {
                for (j in 0 until MAXPLAYERS) {
                    if (playeringame[j]
                        && dm_frags[i][j] != plrs[i].frags[j]
                    ) {
                        if (plrs[i].frags[j] < 0)
                            dm_frags[i][j]--
                        else
                            dm_frags[i][j]++

                        if (dm_frags[i][j] > 99)
                            dm_frags[i][j] = 99

                        if (dm_frags[i][j] < -99)
                            dm_frags[i][j] = -99

                        stillticking = true
                    }
                }
                dm_totals[i] = WI_fragSum(i)

                if (dm_totals[i] > 99)
                    dm_totals[i] = 99

                if (dm_totals[i] < -99)
                    dm_totals[i] = -99
            }
        }
        if (!stillticking) {
            S_StartSound(null, sfx_barexp)
            dm_state++
        }
    } else if (dm_state == 4) {
        if (acceleratestage != 0) {
            S_StartSound(null, sfx_slop)

            if (gamemode == commercial)
                WI_initNoState()
            else
                WI_initShowNextLoc()
        }
    } else if ((dm_state and 1) != 0) {
        cnt_pause--
        if (cnt_pause == 0) {   // C: if (!--cnt_pause)
            dm_state++
            cnt_pause = TICRATE
        }
    }
}

internal fun DoomEngineCore.WI_drawDeathmatchStats() {
    var x: Int
    var y: Int
    val w: Int

    val lh: Int     // line height

    lh = WI_SPACINGY

    WI_slamBackground()

    // draw animated background
    WI_drawAnimatedBack()
    WI_drawLF()

    // draw stat titles (top line)
    V_DrawPatch(DM_TOTALSX - patchWidth(total) / 2,
        DM_MATRIXY - WI_SPACINGY + 10,
        FB,
        total)

    V_DrawPatch(DM_KILLERSX, DM_KILLERSY, FB, killers)
    V_DrawPatch(DM_VICTIMSX, DM_VICTIMSY, FB, victims)

    // draw P?
    x = DM_MATRIXX + DM_SPACINGX
    y = DM_MATRIXY

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            V_DrawPatch(x - patchWidth(p[i]) / 2,
                DM_MATRIXY - WI_SPACINGY,
                FB,
                p[i])

            V_DrawPatch(DM_MATRIXX - patchWidth(p[i]) / 2,
                y,
                FB,
                p[i])

            if (i == me) {
                V_DrawPatch(x - patchWidth(p[i]) / 2,
                    DM_MATRIXY - WI_SPACINGY,
                    FB,
                    bstar)

                V_DrawPatch(DM_MATRIXX - patchWidth(p[i]) / 2,
                    y,
                    FB,
                    star)
            }
        } else {
            // V_DrawPatch(x-SHORT(bp[i]->width)/2,
            //   DM_MATRIXY - WI_SPACINGY, FB, bp[i]);
            // V_DrawPatch(DM_MATRIXX-SHORT(bp[i]->width)/2,
            //   y, FB, bp[i]);
        }
        x += DM_SPACINGX
        y += WI_SPACINGY
    }

    // draw stats
    y = DM_MATRIXY + 10
    w = patchWidth(num[0])

    for (i in 0 until MAXPLAYERS) {
        x = DM_MATRIXX + DM_SPACINGX

        if (playeringame[i]) {
            for (j in 0 until MAXPLAYERS) {
                if (playeringame[j])
                    WI_drawNum(x + w, y, dm_frags[i][j], 2)

                x += DM_SPACINGX
            }
            WI_drawNum(DM_TOTALSX + w, y, dm_totals[i], 2)
        }
        y += WI_SPACINGY
    }
}

private val DoomEngineCore.cnt_frags
    get() = stateIntermission.cnt_frags
private var DoomEngineCore.dofrags
    get() = stateIntermission.dofrags
    set(value) { stateIntermission.dofrags = value }
private var DoomEngineCore.ng_state
    get() = stateIntermission.ng_state
    set(value) { stateIntermission.ng_state = value }

internal fun DoomEngineCore.WI_initNetgameStats() {
    state = StatCount
    acceleratestage = 0
    ng_state = 1

    cnt_pause = TICRATE

    for (i in 0 until MAXPLAYERS) {
        if (!playeringame[i])
            continue

        cnt_kills[i] = 0; cnt_items[i] = 0; cnt_secret[i] = 0; cnt_frags[i] = 0

        dofrags += WI_fragSum(i)
    }

    dofrags = if (dofrags != 0) 1 else 0    // C: dofrags = !!dofrags

    WI_initAnimatedBack()
}

internal fun DoomEngineCore.WI_updateNetgameStats() {
    var fsum: Int

    var stillticking: Boolean

    WI_updateAnimatedBack()

    if (acceleratestage != 0 && ng_state != 10) {
        acceleratestage = 0

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills
            cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems
            cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret

            if (dofrags != 0)
                cnt_frags[i] = WI_fragSum(i)
        }
        S_StartSound(null, sfx_barexp)
        ng_state = 10
    }

    if (ng_state == 2) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cnt_kills[i] += 2

            if (cnt_kills[i] >= (plrs[i].skills * 100) / wbs.maxkills)
                cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills
            else
                stillticking = true
        }

        if (!stillticking) {
            S_StartSound(null, sfx_barexp)
            ng_state++
        }
    } else if (ng_state == 4) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cnt_items[i] += 2
            if (cnt_items[i] >= (plrs[i].sitems * 100) / wbs.maxitems)
                cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems
            else
                stillticking = true
        }
        if (!stillticking) {
            S_StartSound(null, sfx_barexp)
            ng_state++
        }
    } else if (ng_state == 6) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cnt_secret[i] += 2

            if (cnt_secret[i] >= (plrs[i].ssecret * 100) / wbs.maxsecret)
                cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret
            else
                stillticking = true
        }

        if (!stillticking) {
            S_StartSound(null, sfx_barexp)
            ng_state += 1 + 2 * (if (dofrags != 0) 0 else 1)    // C: ng_state += 1 + 2*!dofrags
        }
    } else if (ng_state == 8) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cnt_frags[i] += 1

            fsum = WI_fragSum(i)
            if (cnt_frags[i] >= fsum)   // C: if (cnt_frags[i] >= (fsum = WI_fragSum(i)))
                cnt_frags[i] = fsum
            else
                stillticking = true
        }

        if (!stillticking) {
            S_StartSound(null, sfx_pldeth)
            ng_state++
        }
    } else if (ng_state == 10) {
        if (acceleratestage != 0) {
            S_StartSound(null, sfx_sgcock)
            if (gamemode == commercial)
                WI_initNoState()
            else
                WI_initShowNextLoc()
        }
    } else if ((ng_state and 1) != 0) {
        cnt_pause--
        if (cnt_pause == 0) {   // C: if (!--cnt_pause)
            ng_state++
            cnt_pause = TICRATE
        }
    }
}

internal fun DoomEngineCore.WI_drawNetgameStats() {
    var x: Int
    var y: Int
    val pwidth = patchWidth(percent)

    WI_slamBackground()

    // draw animated background
    WI_drawAnimatedBack()

    WI_drawLF()

    // draw stat titles (top line)
    V_DrawPatch(NG_STATSX() + NG_SPACINGX - patchWidth(kills),
        NG_STATSY, FB, kills)

    V_DrawPatch(NG_STATSX() + 2 * NG_SPACINGX - patchWidth(items),
        NG_STATSY, FB, items)

    V_DrawPatch(NG_STATSX() + 3 * NG_SPACINGX - patchWidth(secret),
        NG_STATSY, FB, secret)

    if (dofrags != 0)
        V_DrawPatch(NG_STATSX() + 4 * NG_SPACINGX - patchWidth(frags),
            NG_STATSY, FB, frags)

    // draw stats
    y = NG_STATSY + patchHeight(kills)

    for (i in 0 until MAXPLAYERS) {
        if (!playeringame[i])
            continue

        x = NG_STATSX()
        V_DrawPatch(x - patchWidth(p[i]), y, FB, p[i])

        if (i == me)
            V_DrawPatch(x - patchWidth(p[i]), y, FB, star)

        x += NG_SPACINGX
        WI_drawPercent(x - pwidth, y + 10, cnt_kills[i]); x += NG_SPACINGX
        WI_drawPercent(x - pwidth, y + 10, cnt_items[i]); x += NG_SPACINGX
        WI_drawPercent(x - pwidth, y + 10, cnt_secret[i]); x += NG_SPACINGX

        if (dofrags != 0)
            WI_drawNum(x, y + 10, cnt_frags[i], -1)

        y += WI_SPACINGY
    }
}

private var DoomEngineCore.sp_state
    get() = stateIntermission.sp_state
    set(value) { stateIntermission.sp_state = value }

internal fun DoomEngineCore.WI_initStats() {
    state = StatCount
    acceleratestage = 0
    sp_state = 1
    cnt_kills[0] = -1; cnt_items[0] = -1; cnt_secret[0] = -1
    cnt_time = -1; cnt_par = -1
    cnt_pause = TICRATE

    WI_initAnimatedBack()
}

internal fun DoomEngineCore.WI_updateStats() {
    WI_updateAnimatedBack()

    if (acceleratestage != 0 && sp_state != 10) {
        acceleratestage = 0
        cnt_kills[0] = (plrs[me].skills * 100) / wbs.maxkills
        cnt_items[0] = (plrs[me].sitems * 100) / wbs.maxitems
        cnt_secret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret
        cnt_time = plrs[me].stime / TICRATE
        cnt_par = wbs.partime / TICRATE
        S_StartSound(null, sfx_barexp)
        sp_state = 10
    }

    if (sp_state == 2) {
        cnt_kills[0] += 2

        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        if (cnt_kills[0] >= (plrs[me].skills * 100) / wbs.maxkills) {
            cnt_kills[0] = (plrs[me].skills * 100) / wbs.maxkills
            S_StartSound(null, sfx_barexp)
            sp_state++
        }
    } else if (sp_state == 4) {
        cnt_items[0] += 2

        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        if (cnt_items[0] >= (plrs[me].sitems * 100) / wbs.maxitems) {
            cnt_items[0] = (plrs[me].sitems * 100) / wbs.maxitems
            S_StartSound(null, sfx_barexp)
            sp_state++
        }
    } else if (sp_state == 6) {
        cnt_secret[0] += 2

        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        if (cnt_secret[0] >= (plrs[me].ssecret * 100) / wbs.maxsecret) {
            cnt_secret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret
            S_StartSound(null, sfx_barexp)
            sp_state++
        }
    } else if (sp_state == 8) {
        if ((bcnt and 3) == 0)
            S_StartSound(null, sfx_pistol)

        cnt_time += 3

        if (cnt_time >= plrs[me].stime / TICRATE)
            cnt_time = plrs[me].stime / TICRATE

        cnt_par += 3

        if (cnt_par >= wbs.partime / TICRATE) {
            cnt_par = wbs.partime / TICRATE

            if (cnt_time >= plrs[me].stime / TICRATE) {
                S_StartSound(null, sfx_barexp)
                sp_state++
            }
        }
    } else if (sp_state == 10) {
        if (acceleratestage != 0) {
            S_StartSound(null, sfx_sgcock)

            if (gamemode == commercial)
                WI_initNoState()
            else
                WI_initShowNextLoc()
        }
    } else if ((sp_state and 1) != 0) {
        cnt_pause--
        if (cnt_pause == 0) {   // C: if (!--cnt_pause)
            sp_state++
            cnt_pause = TICRATE
        }
    }
}

internal fun DoomEngineCore.WI_drawStats() {
    // line height
    val lh: Int

    lh = (3 * patchHeight(num[0])) / 2

    WI_slamBackground()

    // draw animated background
    WI_drawAnimatedBack()

    WI_drawLF()

    V_DrawPatch(SP_STATSX, SP_STATSY, FB, kills)
    WI_drawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY, cnt_kills[0])

    V_DrawPatch(SP_STATSX, SP_STATSY + lh, FB, items)
    WI_drawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY + lh, cnt_items[0])

    V_DrawPatch(SP_STATSX, SP_STATSY + 2 * lh, FB, sp_secret)
    WI_drawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY + 2 * lh, cnt_secret[0])

    V_DrawPatch(SP_TIMEX, SP_TIMEY, FB, time)
    WI_drawTime(SCREENWIDTH / 2 - SP_TIMEX, SP_TIMEY, cnt_time)

    if (wbs.epsd < 3) {
        V_DrawPatch(SCREENWIDTH / 2 + SP_TIMEX, SP_TIMEY, FB, par)
        WI_drawTime(SCREENWIDTH - SP_TIMEX, SP_TIMEY, cnt_par)
    }
}

internal fun DoomEngineCore.WI_checkForAccelerate() {
    // check for button presses to skip delays
    for (i in 0 until MAXPLAYERS) {
        val player = players[i]
        if (playeringame[i]) {
            if ((player.cmd.buttons and BT_ATTACK) != 0) {
                if (!player.attackdown)
                    acceleratestage = 1
                player.attackdown = true
            } else
                player.attackdown = false
            if ((player.cmd.buttons and BT_USE) != 0) {
                if (!player.usedown)
                    acceleratestage = 1
                player.usedown = true
            } else
                player.usedown = false
        }
    }
}

// Updates stuff each tick
internal fun DoomEngineCore.WI_Ticker() {
    // counter for general background animation
    bcnt++

    if (bcnt == 1) {
        // intermission music
        if (gamemode == commercial)
            S_ChangeMusic(mus_dm2int, 1)
        else
            S_ChangeMusic(mus_inter, 1)
    }

    WI_checkForAccelerate()

    when (state) {
        StatCount -> {
            if (deathmatch != 0) WI_updateDeathmatchStats()
            else if (netgame) WI_updateNetgameStats()
            else WI_updateStats()
        }

        ShowNextLoc -> WI_updateShowNextLoc()

        NoState -> WI_updateNoState()
    }
}

internal fun DoomEngineCore.WI_loadData() {
    var name: String

    if (gamemode == commercial)
        name = "INTERPIC"
    else
        name = "WIMAP${wbs.epsd}"

    if (gamemode == retail) {
        if (wbs.epsd == 3)
            name = "INTERPIC"
    }

    // background
    bg = W_CacheLumpName(name)
    V_DrawPatch(0, 0, 1, bg)

    // UNUSED unsigned char *pic = screens[1];
    // if (gamemode == commercial)
    // {
    // darken the background image
    // while (pic != screens[1] + SCREENHEIGHT*SCREENWIDTH)
    // {
    //   *pic = colormaps[256*25 + *pic];
    //   pic++;
    // }
    //}

    if (gamemode == commercial) {
        NUMCMAPS = 32
        lnames = Array(NUMCMAPS) { ByteArray(0) }
        for (i in 0 until NUMCMAPS) {
            name = "CWILV" + i.toString().padStart(2, '0')  // sprintf(name, "CWILV%2.2d", i)
            lnames[i] = W_CacheLumpName(name)
        }
    } else {
        lnames = Array(NUMMAPS) { ByteArray(0) }
        for (i in 0 until NUMMAPS) {
            name = "WILV${wbs.epsd}$i"  // sprintf(name, "WILV%d%d", wbs->epsd, i)
            lnames[i] = W_CacheLumpName(name)
        }

        // you are here
        yah[0] = W_CacheLumpName("WIURH0")

        // you are here (alt.)
        yah[1] = W_CacheLumpName("WIURH1")

        // splat
        splat = W_CacheLumpName("WISPLAT")

        if (wbs.epsd < 3) {
            for (j in 0 until NUMANIMS[wbs.epsd]) {
                val a = wi_anims[wbs.epsd][j]
                for (i in 0 until a.nanims) {
                    // MONDO HACK!
                    if (wbs.epsd != 1 || j != 8) {
                        // animations
                        // sprintf(name, "WIA%d%.2d%.2d", wbs->epsd, j, i)
                        name = "WIA${wbs.epsd}" +
                            j.toString().padStart(2, '0') +
                            i.toString().padStart(2, '0')
                        a.p[i] = W_CacheLumpName(name)
                    } else {
                        // HACK ALERT!
                        a.p[i] = wi_anims[1][4].p[i]
                    }
                }
            }
        }
    }

    // More hacks on minus sign.
    wiminus = W_CacheLumpName("WIMINUS")

    for (i in 0 until 10) {
        // numbers 0-9
        name = "WINUM$i"    // sprintf(name, "WINUM%d", i)
        num[i] = W_CacheLumpName(name)
    }

    // percent sign
    percent = W_CacheLumpName("WIPCNT")

    // "finished"
    finished = W_CacheLumpName("WIF")

    // "entering"
    entering = W_CacheLumpName("WIENTER")

    // "kills"
    kills = W_CacheLumpName("WIOSTK")

    // "scrt"
    secret = W_CacheLumpName("WIOSTS")

    // "secret"
    sp_secret = W_CacheLumpName("WISCRT2")

    // Yuck.
    // (vanilla bug: C wrote "if (french)", testing the Language_t enum
    // constant french == 1, i.e. always true. Preserved literally.)
    if (french != 0) {
        // "items"
        if (netgame && deathmatch == 0)
            items = W_CacheLumpName("WIOBJ")
        else
            items = W_CacheLumpName("WIOSTI")
    } else
        items = W_CacheLumpName("WIOSTI")

    // "frgs"
    frags = W_CacheLumpName("WIFRGS")

    // ":"
    colon = W_CacheLumpName("WICOLON")

    // "time"
    time = W_CacheLumpName("WITIME")

    // "sucks"
    sucks = W_CacheLumpName("WISUCKS")

    // "par"
    par = W_CacheLumpName("WIPAR")

    // "killers" (vertical)
    killers = W_CacheLumpName("WIKILRS")

    // "victims" (horiz)
    victims = W_CacheLumpName("WIVCTMS")

    // "total"
    total = W_CacheLumpName("WIMSTT")

    // your face
    star = W_CacheLumpName("STFST01")

    // dead face
    bstar = W_CacheLumpName("STFDEAD0")

    for (i in 0 until MAXPLAYERS) {
        // "1,2,3,4"
        name = "STPB$i"     // sprintf(name, "STPB%d", i)
        p[i] = W_CacheLumpName(name)

        // "1,2,3,4"
        name = "WIBP${i + 1}"   // sprintf(name, "WIBP%d", i+1)
        bp[i] = W_CacheLumpName(name)
    }
}

internal fun DoomEngineCore.WI_unloadData() {
    // All the Z_ChangeTag(..., PU_CACHE) calls on wiminus/num[]/lnames[]/
    // yah[]/splat/anim patches/percent/colon/finished/entering/kills/secret/
    // sp_secret/items/frags/time/sucks/par/victims/killers/total/p[]/bp[]
    // and the Z_Free(lnames) are deleted -- lump caching lives in
    // W_CacheLumpNum, there is nothing to release here.
}

internal fun DoomEngineCore.WI_Drawer() {
    when (state) {
        StatCount -> {
            if (deathmatch != 0)
                WI_drawDeathmatchStats()
            else if (netgame)
                WI_drawNetgameStats()
            else
                WI_drawStats()
        }

        ShowNextLoc -> WI_drawShowNextLoc()

        NoState -> WI_drawNoState()
    }
}

internal fun DoomEngineCore.WI_initVariables(wbstartstruct: wbstartstruct_t) {
    wbs = wbstartstruct

    acceleratestage = 0
    cnt = 0; bcnt = 0
    firstrefresh = 1
    me = wbs.pnum
    plrs = wbs.plyr

    if (wbs.maxkills == 0)
        wbs.maxkills = 1

    if (wbs.maxitems == 0)
        wbs.maxitems = 1

    if (wbs.maxsecret == 0)
        wbs.maxsecret = 1

    if (gamemode != retail)
        if (wbs.epsd > 2)
            wbs.epsd -= 3
}

internal fun DoomEngineCore.WI_Start(wbstartstruct: wbstartstruct_t) {
    WI_initVariables(wbstartstruct)
    WI_loadData()

    if (deathmatch != 0)
        WI_initDeathmatchStats()
    else if (netgame)
        WI_initNetgameStats()
    else
        WI_initStats()
}
