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
private fun NG_STATSX(): Int = 32 + patchWidth(star) / 2 + 32 * (if (dofrags != 0) 0 else 1)

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
const val NoState = -1
const val StatCount = 0
const val ShowNextLoc = 1

// animenum_t
private const val ANIM_ALWAYS = 0
private const val ANIM_RANDOM = 1
private const val ANIM_LEVEL = 2

private class point_t(var x: Int, var y: Int)

//
// Animation.
// There is another anim_t used in p_spec.
// (Named wianim_t here since p_spec.kt owns the public anim_t.)
//
private class wianim_t(
    val type: Int,

    // period in tics between animations
    val period: Int,

    // number of animation frames
    val nanims: Int,

    // location of animation
    val loc: point_t,

    // ALWAYS: n/a,
    // RANDOM: period deviation (<256),
    // LEVEL: level
    val data1: Int = 0,

    // ALWAYS: n/a,
    // RANDOM: random base period,
    // LEVEL: n/a
    val data2: Int = 0,
) {
    // actual graphics for frames of animations
    val p = Array(3) { ByteArray(0) }

    // following must be initialized to zero before use!

    // next value of bcnt (used in conjunction with period)
    var nexttic = 0

    // last drawn animation frame
    var lastdrawn = 0

    // next frame number to animate
    var ctr = 0

    // used by RANDOM and LEVEL when animating
    var state = 0
}

private val lnodes: Array<Array<point_t>> = arrayOf(
    // Episode 0 World Map
    arrayOf(
        point_t(185, 164),  // location of level 0 (CJ)
        point_t(148, 143),  // location of level 1 (CJ)
        point_t(69, 122),   // location of level 2 (CJ)
        point_t(209, 102),  // location of level 3 (CJ)
        point_t(116, 89),   // location of level 4 (CJ)
        point_t(166, 55),   // location of level 5 (CJ)
        point_t(71, 56),    // location of level 6 (CJ)
        point_t(135, 29),   // location of level 7 (CJ)
        point_t(71, 24),    // location of level 8 (CJ)
    ),

    // Episode 1 World Map should go here
    arrayOf(
        point_t(254, 25),   // location of level 0 (CJ)
        point_t(97, 50),    // location of level 1 (CJ)
        point_t(188, 64),   // location of level 2 (CJ)
        point_t(128, 78),   // location of level 3 (CJ)
        point_t(214, 92),   // location of level 4 (CJ)
        point_t(133, 130),  // location of level 5 (CJ)
        point_t(208, 136),  // location of level 6 (CJ)
        point_t(148, 140),  // location of level 7 (CJ)
        point_t(235, 158),  // location of level 8 (CJ)
    ),

    // Episode 2 World Map should go here
    arrayOf(
        point_t(156, 168),  // location of level 0 (CJ)
        point_t(48, 154),   // location of level 1 (CJ)
        point_t(174, 95),   // location of level 2 (CJ)
        point_t(265, 75),   // location of level 3 (CJ)
        point_t(130, 48),   // location of level 4 (CJ)
        point_t(279, 23),   // location of level 5 (CJ)
        point_t(198, 48),   // location of level 6 (CJ)
        point_t(140, 25),   // location of level 7 (CJ)
        point_t(281, 136),  // location of level 8 (CJ)
    ),
)

//
// Animation locations for episode 0 (1).
// Using patches saves a lot of space,
//  as they replace 320x200 full screen frames.
//
private val epsd0animinfo = arrayOf(
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(224, 104)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(184, 160)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(112, 136)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(72, 112)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(88, 96)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(64, 48)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(192, 40)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(136, 16)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(80, 16)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(64, 24)),
)

private val epsd1animinfo = arrayOf(
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 1),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 2),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 3),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 4),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 5),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 6),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 7),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 3, point_t(192, 144), 8),
    wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 8),
)

private val epsd2animinfo = arrayOf(
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(104, 168)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(40, 136)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(160, 96)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(104, 80)),
    wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(120, 32)),
    wianim_t(ANIM_ALWAYS, TICRATE / 4, 3, point_t(40, 0)),
)

private val NUMANIMS = intArrayOf(
    epsd0animinfo.size,
    epsd1animinfo.size,
    epsd2animinfo.size,
)

// C: static anim_t *anims[NUMEPISODES] (4th entry NULL, never touched: epsd > 2 always bails)
private val wi_anims = arrayOf(
    epsd0animinfo,
    epsd1animinfo,
    epsd2animinfo,
)

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

// used to accelerate or skip a stage
private var acceleratestage = 0

// wbs->pnum
private var me = 0

// specifies current state
private var state = StatCount

// contains information passed into intermission
private lateinit var wbs: wbstartstruct_t

private lateinit var plrs: Array<wbplayerstruct_t>  // wbs->plyr[]

// used for general timing
private var cnt = 0

// used for timing of background animation
private var bcnt = 0

// signals to refresh everything for one frame
private var firstrefresh = 0

private val cnt_kills = IntArray(MAXPLAYERS)
private val cnt_items = IntArray(MAXPLAYERS)
private val cnt_secret = IntArray(MAXPLAYERS)
private var cnt_time = 0
private var cnt_par = 0
private var cnt_pause = 0

// # of commercial levels
private var NUMCMAPS = 0

//
//	GRAPHICS
//

// background (map of levels).
private var bg: ByteArray = ByteArray(0)

// You Are Here graphic
private val yah = Array(2) { ByteArray(0) }

// splat
private var splat: ByteArray = ByteArray(0)

// %, : graphics
private var percent: ByteArray = ByteArray(0)
private var colon: ByteArray = ByteArray(0)

// 0-9 graphic
private val num = Array(10) { ByteArray(0) }

// minus sign
private var wiminus: ByteArray = ByteArray(0)

// "Finished!" graphics
private var finished: ByteArray = ByteArray(0)

// "Entering" graphic
private var entering: ByteArray = ByteArray(0)

// "secret"
private var sp_secret: ByteArray = ByteArray(0)

// "Kills", "Scrt", "Items", "Frags"
private var kills: ByteArray = ByteArray(0)
private var secret: ByteArray = ByteArray(0)
private var items: ByteArray = ByteArray(0)
private var frags: ByteArray = ByteArray(0)

// Time sucks.
private var time: ByteArray = ByteArray(0)
private var par: ByteArray = ByteArray(0)
private var sucks: ByteArray = ByteArray(0)

// "killers", "victims"
private var killers: ByteArray = ByteArray(0)
private var victims: ByteArray = ByteArray(0)

// "Total", your face, your dead face
private var total: ByteArray = ByteArray(0)
private var star: ByteArray = ByteArray(0)
private var bstar: ByteArray = ByteArray(0)

// "red P[1..MAXPLAYERS]"
private val p = Array(MAXPLAYERS) { ByteArray(0) }

// "gray P[1..MAXPLAYERS]"
private val bp = Array(MAXPLAYERS) { ByteArray(0) }

// Name graphics of each level (centered)
private var lnames: Array<ByteArray> = emptyArray()

//
// CODE
//

// slam background
// UNUSED static unsigned char *background=0;

fun WI_slamBackground() {
    // memcpy(screens[0], screens[1], SCREENWIDTH * SCREENHEIGHT)
    screens[1].copyInto(screens[0], 0, 0, SCREENWIDTH * SCREENHEIGHT)
    V_MarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)
}

// The ticker is used to detect keys
//  because of timing issues in netgames.
fun WI_Responder(ev: event_t): Boolean {
    return false
}

// Draws "<Levelname> Finished!"
fun WI_drawLF() {
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
fun WI_drawEL() {
    var y = WI_TITLEY

    // draw "Entering"
    V_DrawPatch((SCREENWIDTH - patchWidth(entering)) / 2,
        y, FB, entering)

    // draw level
    y += (5 * patchHeight(lnames[wbs.next])) / 4

    V_DrawPatch((SCREENWIDTH - patchWidth(lnames[wbs.next])) / 2,
        y, FB, lnames[wbs.next])
}

fun WI_drawOnLnode(n: Int, c: Array<ByteArray>) {
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

fun WI_initAnimatedBack() {
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

fun WI_updateAnimatedBack() {
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

fun WI_drawAnimatedBack() {
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

fun WI_drawNum(x: Int, y: Int, n: Int, digits: Int): Int {
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

fun WI_drawPercent(x: Int, y: Int, p: Int) {
    if (p < 0)
        return

    V_DrawPatch(x, y, FB, percent)
    WI_drawNum(x, y, p, -1)
}

//
// Display level completion time and par,
//  or "sucks" message if overflow.
//
fun WI_drawTime(x: Int, y: Int, t: Int) {
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

fun WI_End() {
    WI_unloadData()
}

fun WI_initNoState() {
    state = NoState
    acceleratestage = 0
    cnt = 10
}

fun WI_updateNoState() {
    WI_updateAnimatedBack()

    cnt--
    if (cnt == 0) {     // C: if (!--cnt)
        WI_End()
        G_WorldDone()
    }
}

private var snl_pointeron = false

fun WI_initShowNextLoc() {
    state = ShowNextLoc
    acceleratestage = 0
    cnt = SHOWNEXTLOCDELAY * TICRATE

    WI_initAnimatedBack()
}

fun WI_updateShowNextLoc() {
    WI_updateAnimatedBack()

    cnt--
    if (cnt == 0 || acceleratestage != 0)   // C: if (!--cnt || acceleratestage)
        WI_initNoState()
    else
        snl_pointeron = (cnt and 31) < 20
}

fun WI_drawShowNextLoc() {
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

fun WI_drawNoState() {
    snl_pointeron = true
    WI_drawShowNextLoc()
}

fun WI_fragSum(playernum: Int): Int {
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

private var dm_state = 0
private val dm_frags = Array(MAXPLAYERS) { IntArray(MAXPLAYERS) }
private val dm_totals = IntArray(MAXPLAYERS)

fun WI_initDeathmatchStats() {
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

fun WI_updateDeathmatchStats() {
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

fun WI_drawDeathmatchStats() {
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

private val cnt_frags = IntArray(MAXPLAYERS)
private var dofrags = 0
private var ng_state = 0

fun WI_initNetgameStats() {
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

fun WI_updateNetgameStats() {
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

fun WI_drawNetgameStats() {
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

private var sp_state = 0

fun WI_initStats() {
    state = StatCount
    acceleratestage = 0
    sp_state = 1
    cnt_kills[0] = -1; cnt_items[0] = -1; cnt_secret[0] = -1
    cnt_time = -1; cnt_par = -1
    cnt_pause = TICRATE

    WI_initAnimatedBack()
}

fun WI_updateStats() {
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

fun WI_drawStats() {
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

fun WI_checkForAccelerate() {
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
fun WI_Ticker() {
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

fun WI_loadData() {
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

fun WI_unloadData() {
    // All the Z_ChangeTag(..., PU_CACHE) calls on wiminus/num[]/lnames[]/
    // yah[]/splat/anim patches/percent/colon/finished/entering/kills/secret/
    // sp_secret/items/frags/time/sucks/par/victims/killers/total/p[]/bp[]
    // and the Z_Free(lnames) are deleted -- lump caching lives in
    // W_CacheLumpNum, there is nothing to release here.
}

fun WI_Drawer() {
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

fun WI_initVariables(wbstartstruct: wbstartstruct_t) {
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

fun WI_Start(wbstartstruct: wbstartstruct_t) {
    WI_initVariables(wbstartstruct)
    WI_loadData()

    if (deathmatch != 0)
        WI_initDeathmatchStats()
    else if (netgame)
        WI_initNetgameStats()
    else
        WI_initStats()
}
