
package doom.engine.intermission

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.TICRATE
import doom.engine.audio.sChangeMusic
import doom.engine.audio.sStartSound
import doom.engine.audio.MUS_DM2INT
import doom.engine.audio.MUS_INTER
import doom.engine.audio.SFX_BAREXP
import doom.engine.audio.SFX_PISTOL
import doom.engine.audio.SFX_PLDETH
import doom.engine.audio.SFX_SGCOCK
import doom.engine.audio.SFX_SLOP
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.gWorldDone
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.FRENCH
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.netgame
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.RETAIL
import doom.engine.input.BT_ATTACK
import doom.engine.input.BT_USE
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.vMarkRect
import doom.engine.rendering.patchHeight
import doom.engine.rendering.patchLeftOffset
import doom.engine.rendering.patchTopOffset
import doom.engine.rendering.patchWidth
import doom.engine.rendering.screens
import doom.engine.resources.wCacheLumpName
import doom.engine.simulation.mRandom


private const val NUMEPISODES = 4
private const val NUMMAPS = 9


private const val WI_TITLEY = 2
private const val WI_SPACINGY = 33

private const val SP_STATSX = 50
private const val SP_STATSY = 50

private const val SP_TIMEX = 16
private const val SP_TIMEY = SCREENHEIGHT - 32

private const val NG_STATSY = 50
private fun DoomEngineCore.ngSTATSX(): Int = 32 + patchWidth(star) / 2 + 32 * (if (dofrags != 0) 0 else 1)

private const val NG_SPACINGX = 64

private const val DM_MATRIXX = 42
private const val DM_MATRIXY = 68

private const val DM_SPACINGX = 40

private const val DM_TOTALSX = 269

private const val DM_KILLERSX = 10
private const val DM_KILLERSY = 100
private const val DM_VICTIMSX = 5
private const val DM_VICTIMSY = 50

internal const val NO_STATE = -1
internal const val STAT_COUNT = 0
internal const val SHOW_NEXT_LOC = 1

internal const val ANIM_ALWAYS = 0
private const val ANIM_RANDOM = 1
internal const val ANIM_LEVEL = 2

private val DoomEngineCore.lnodes: Array<Array<IntermissionPoint>>
    get() = stateIntermission.lnodes

private val DoomEngineCore.epsd0animinfo
    get() = stateIntermission.epsd0animinfo

private val DoomEngineCore.epsd1animinfo
    get() = stateIntermission.epsd1animinfo

private val DoomEngineCore.epsd2animinfo
    get() = stateIntermission.epsd2animinfo

private val DoomEngineCore.numanims
    get() = stateIntermission.numanims

private val DoomEngineCore.wiAnims
    get() = stateIntermission.wiAnims


private const val FB = 0

private const val SP_KILLS = 0
private const val SP_ITEMS = 2
private const val SP_SECRET = 4
private const val SP_FRAGS = 6
private const val SP_TIME = 8

private const val SP_PAUSE = 1

private const val SHOWNEXTLOCDELAY = 4

private var DoomEngineCore.acceleratestage
    get() = stateIntermission.acceleratestage
    set(value) { stateIntermission.acceleratestage = value }

private var DoomEngineCore.me
    get() = stateIntermission.me
    set(value) { stateIntermission.me = value }

private var DoomEngineCore.state
    get() = stateIntermission.state
    set(value) { stateIntermission.state = value }

private var DoomEngineCore.wbs: IntermissionSummary
    get() = stateIntermission.wbs
    set(value) { stateIntermission.wbs = value }

private var DoomEngineCore.plrs: Array<IntermissionPlayerStats>
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

private val DoomEngineCore.cntKills
    get() = stateIntermission.cntKills
private val DoomEngineCore.cntItems
    get() = stateIntermission.cntItems
private val DoomEngineCore.cntSecret
    get() = stateIntermission.cntSecret
private var DoomEngineCore.cntTime
    get() = stateIntermission.cntTime
    set(value) { stateIntermission.cntTime = value }
private var DoomEngineCore.cntPar
    get() = stateIntermission.cntPar
    set(value) { stateIntermission.cntPar = value }
private var DoomEngineCore.cntPause
    get() = stateIntermission.cntPause
    set(value) { stateIntermission.cntPause = value }

private var DoomEngineCore.numcmaps
    get() = stateIntermission.numcmaps
    set(value) { stateIntermission.numcmaps = value }


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

private var DoomEngineCore.spSecret: ByteArray
    get() = stateIntermission.spSecret
    set(value) { stateIntermission.spSecret = value }

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



internal fun DoomEngineCore.wiSlamBackground() {
    screens[1].copyInto(screens[0], 0, 0, SCREENWIDTH * SCREENHEIGHT)
    vMarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)
}

internal fun DoomEngineCore.wiDrawLF() {
    var y = WI_TITLEY

    vDrawPatch((SCREENWIDTH - patchWidth(lnames[wbs.last])) / 2,
        y, FB, lnames[wbs.last])

    y += (5 * patchHeight(lnames[wbs.last])) / 4

    vDrawPatch((SCREENWIDTH - patchWidth(finished)) / 2,
        y, FB, finished)
}

internal fun DoomEngineCore.wiDrawEL() {
    var y = WI_TITLEY

    vDrawPatch((SCREENWIDTH - patchWidth(entering)) / 2,
        y, FB, entering)

    y += (5 * patchHeight(lnames[wbs.next])) / 4

    vDrawPatch((SCREENWIDTH - patchWidth(lnames[wbs.next])) / 2,
        y, FB, lnames[wbs.next])
}

internal fun DoomEngineCore.wiDrawOnLnode(n: Int, c: Array<ByteArray>) {
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
        vDrawPatch(lnodes[wbs.epsd][n].x, lnodes[wbs.epsd][n].y,
            FB, c[i])
    } else {
        print("Could not place patch on level ${n + 1}")
    }
}

internal fun DoomEngineCore.wiInitAnimatedBack() {
    if (gamemode == COMMERCIAL)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until numanims[wbs.epsd]) {
        val a = wiAnims[wbs.epsd][i]

        a.ctr = -1

        if (a.type == ANIM_ALWAYS)
            a.nexttic = bcnt + 1 + (mRandom() % a.period)
        else if (a.type == ANIM_RANDOM)
            a.nexttic = bcnt + 1 + a.data2 + (mRandom() % a.data1)
        else if (a.type == ANIM_LEVEL)
            a.nexttic = bcnt + 1
    }
}

internal fun DoomEngineCore.wiUpdateAnimatedBack() {
    if (gamemode == COMMERCIAL)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until numanims[wbs.epsd]) {
        val a = wiAnims[wbs.epsd][i]

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
                        a.nexttic = bcnt + a.data2 + (mRandom() % a.data1)
                    } else a.nexttic = bcnt + a.period
                }

                ANIM_LEVEL -> {
                    if (!(state == STAT_COUNT && i == 7)
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

internal fun DoomEngineCore.wiDrawAnimatedBack() {
    if (gamemode == COMMERCIAL)
        return

    if (wbs.epsd > 2)
        return

    for (i in 0 until numanims[wbs.epsd]) {
        val a = wiAnims[wbs.epsd][i]

        if (a.ctr >= 0)
            vDrawPatch(a.loc.x, a.loc.y, FB, a.p[a.ctr])
    }
}


internal fun DoomEngineCore.wiDrawNum(rightX: Int, y: Int, number: Int, requestedDigits: Int): Int {
    var x = rightX
    var n = number
    var digits = requestedDigits

    val fontwidth = patchWidth(num[0])
    val neg: Boolean
    var temp: Int

    if (digits < 0) {
        if (n == 0) {
            digits = 1
        } else {
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

    if (n == 1994)
        return 0

    while (digits != 0) {
        digits--
        x -= fontwidth
        vDrawPatch(x, y, FB, num[n % 10])
        n /= 10
    }

    if (neg) {
        x -= 8
        vDrawPatch(x, y, FB, wiminus)
    }

    return x
}

internal fun DoomEngineCore.wiDrawPercent(x: Int, y: Int, p: Int) {
    if (p < 0)
        return

    vDrawPatch(x, y, FB, percent)
    wiDrawNum(x, y, p, -1)
}

internal fun DoomEngineCore.wiDrawTime(rightX: Int, y: Int, t: Int) {
    var x = rightX

    var div: Int
    var n: Int

    if (t < 0)
        return

    if (t <= 61 * 59) {
        div = 1

        do {
            n = (t / div) % 60
            x = wiDrawNum(x, y, n, 2) - patchWidth(colon)
            div *= 60

            if (div == 60 || t / div != 0)
                vDrawPatch(x, y, FB, colon)
        } while (t / div != 0)
    } else {
        vDrawPatch(x - patchWidth(sucks), y, FB, sucks)
    }
}

internal fun DoomEngineCore.wiEnd() {
    wiUnloadData()
}

internal fun DoomEngineCore.wiInitNoState() {
    state = NO_STATE
    acceleratestage = 0
    cnt = 10
}

internal fun DoomEngineCore.wiUpdateNoState() {
    wiUpdateAnimatedBack()

    cnt--
    if (cnt == 0) {
        wiEnd()
        gWorldDone()
    }
}

private var DoomEngineCore.snlPointeron
    get() = stateIntermission.snlPointeron
    set(value) { stateIntermission.snlPointeron = value }

internal fun DoomEngineCore.wiInitShowNextLoc() {
    state = SHOW_NEXT_LOC
    acceleratestage = 0
    cnt = SHOWNEXTLOCDELAY * TICRATE

    wiInitAnimatedBack()
}

internal fun DoomEngineCore.wiUpdateShowNextLoc() {
    wiUpdateAnimatedBack()

    cnt--
    if (cnt == 0 || acceleratestage != 0)
        wiInitNoState()
    else
        snlPointeron = (cnt and 31) < 20
}

internal fun DoomEngineCore.wiDrawShowNextLoc() {
    val last: Int

    wiSlamBackground()

    wiDrawAnimatedBack()

    if (gamemode != COMMERCIAL) {
        if (wbs.epsd > 2) {
            wiDrawEL()
            return
        }

        last = if (wbs.last == 8) wbs.next - 1 else wbs.last

        for (i in 0..last)
            wiDrawOnLnode(i, arrayOf(splat))

        if (wbs.didsecret)
            wiDrawOnLnode(8, arrayOf(splat))

        if (snlPointeron)
            wiDrawOnLnode(wbs.next, yah)
    }

    if ((gamemode != COMMERCIAL)
        || wbs.next != 30
    )
        wiDrawEL()
}

internal fun DoomEngineCore.wiDrawNoState() {
    snlPointeron = true
    wiDrawShowNextLoc()
}

internal fun DoomEngineCore.wiFragSum(playernum: Int): Int {
    var frags = 0

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]
            && i != playernum
        ) {
            frags += plrs[playernum].frags[i]
        }
    }

    frags -= plrs[playernum].frags[playernum]

    return frags
}

private var DoomEngineCore.dmState
    get() = stateIntermission.dmState
    set(value) { stateIntermission.dmState = value }
private val DoomEngineCore.dmFrags
    get() = stateIntermission.dmFrags
private val DoomEngineCore.dmTotals
    get() = stateIntermission.dmTotals

internal fun DoomEngineCore.wiInitDeathmatchStats() {
    state = STAT_COUNT
    acceleratestage = 0
    dmState = 1

    cntPause = TICRATE

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            for (j in 0 until MAXPLAYERS)
                if (playeringame[j])
                    dmFrags[i][j] = 0

            dmTotals[i] = 0
        }
    }

    wiInitAnimatedBack()
}

internal fun DoomEngineCore.wiUpdateDeathmatchStats() {
    var stillticking: Boolean

    wiUpdateAnimatedBack()

    if (acceleratestage != 0 && dmState != 4) {
        acceleratestage = 0

        for (i in 0 until MAXPLAYERS) {
            if (playeringame[i]) {
                for (j in 0 until MAXPLAYERS)
                    if (playeringame[j])
                        dmFrags[i][j] = plrs[i].frags[j]

                dmTotals[i] = wiFragSum(i)
            }
        }

        sStartSound(null, SFX_BAREXP)
        dmState = 4
    }

    if (dmState == 2) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (playeringame[i]) {
                for (j in 0 until MAXPLAYERS) {
                    if (playeringame[j]
                        && dmFrags[i][j] != plrs[i].frags[j]
                    ) {
                        if (plrs[i].frags[j] < 0)
                            dmFrags[i][j]--
                        else
                            dmFrags[i][j]++

                        if (dmFrags[i][j] > 99)
                            dmFrags[i][j] = 99

                        if (dmFrags[i][j] < -99)
                            dmFrags[i][j] = -99

                        stillticking = true
                    }
                }
                dmTotals[i] = wiFragSum(i)

                if (dmTotals[i] > 99)
                    dmTotals[i] = 99

                if (dmTotals[i] < -99)
                    dmTotals[i] = -99
            }
        }
        if (!stillticking) {
            sStartSound(null, SFX_BAREXP)
            dmState++
        }
    } else if (dmState == 4) {
        if (acceleratestage != 0) {
            sStartSound(null, SFX_SLOP)

            if (gamemode == COMMERCIAL)
                wiInitNoState()
            else
                wiInitShowNextLoc()
        }
    } else if ((dmState and 1) != 0) {
        cntPause--
        if (cntPause == 0) {
            dmState++
            cntPause = TICRATE
        }
    }
}

internal fun DoomEngineCore.wiDrawDeathmatchStats() {
    var x: Int
    var y: Int
    val w: Int

    wiSlamBackground()

    wiDrawAnimatedBack()
    wiDrawLF()

    vDrawPatch(DM_TOTALSX - patchWidth(total) / 2,
        DM_MATRIXY - WI_SPACINGY + 10,
        FB,
        total)

    vDrawPatch(DM_KILLERSX, DM_KILLERSY, FB, killers)
    vDrawPatch(DM_VICTIMSX, DM_VICTIMSY, FB, victims)

    x = DM_MATRIXX + DM_SPACINGX
    y = DM_MATRIXY

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            vDrawPatch(x - patchWidth(p[i]) / 2,
                DM_MATRIXY - WI_SPACINGY,
                FB,
                p[i])

            vDrawPatch(DM_MATRIXX - patchWidth(p[i]) / 2,
                y,
                FB,
                p[i])

            if (i == me) {
                vDrawPatch(x - patchWidth(p[i]) / 2,
                    DM_MATRIXY - WI_SPACINGY,
                    FB,
                    bstar)

                vDrawPatch(DM_MATRIXX - patchWidth(p[i]) / 2,
                    y,
                    FB,
                    star)
            }
        } else {
        }
        x += DM_SPACINGX
        y += WI_SPACINGY
    }

    y = DM_MATRIXY + 10
    w = patchWidth(num[0])

    for (i in 0 until MAXPLAYERS) {
        x = DM_MATRIXX + DM_SPACINGX

        if (playeringame[i]) {
            for (j in 0 until MAXPLAYERS) {
                if (playeringame[j])
                    wiDrawNum(x + w, y, dmFrags[i][j], 2)

                x += DM_SPACINGX
            }
            wiDrawNum(DM_TOTALSX + w, y, dmTotals[i], 2)
        }
        y += WI_SPACINGY
    }
}

private val DoomEngineCore.cntFrags
    get() = stateIntermission.cntFrags
private var DoomEngineCore.dofrags
    get() = stateIntermission.dofrags
    set(value) { stateIntermission.dofrags = value }
private var DoomEngineCore.ngState
    get() = stateIntermission.ngState
    set(value) { stateIntermission.ngState = value }

internal fun DoomEngineCore.wiInitNetgameStats() {
    state = STAT_COUNT
    acceleratestage = 0
    ngState = 1

    cntPause = TICRATE

    for (i in 0 until MAXPLAYERS) {
        if (!playeringame[i])
            continue

        cntKills[i] = 0; cntItems[i] = 0; cntSecret[i] = 0; cntFrags[i] = 0

        dofrags += wiFragSum(i)
    }

    dofrags = if (dofrags != 0) 1 else 0

    wiInitAnimatedBack()
}

internal fun DoomEngineCore.wiUpdateNetgameStats() {
    var fsum: Int

    var stillticking: Boolean

    wiUpdateAnimatedBack()

    if (acceleratestage != 0 && ngState != 10) {
        acceleratestage = 0

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cntKills[i] = (plrs[i].skills * 100) / wbs.maxkills
            cntItems[i] = (plrs[i].sitems * 100) / wbs.maxitems
            cntSecret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret

            if (dofrags != 0)
                cntFrags[i] = wiFragSum(i)
        }
        sStartSound(null, SFX_BAREXP)
        ngState = 10
    }

    if (ngState == 2) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cntKills[i] += 2

            if (cntKills[i] >= (plrs[i].skills * 100) / wbs.maxkills)
                cntKills[i] = (plrs[i].skills * 100) / wbs.maxkills
            else
                stillticking = true
        }

        if (!stillticking) {
            sStartSound(null, SFX_BAREXP)
            ngState++
        }
    } else if (ngState == 4) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cntItems[i] += 2
            if (cntItems[i] >= (plrs[i].sitems * 100) / wbs.maxitems)
                cntItems[i] = (plrs[i].sitems * 100) / wbs.maxitems
            else
                stillticking = true
        }
        if (!stillticking) {
            sStartSound(null, SFX_BAREXP)
            ngState++
        }
    } else if (ngState == 6) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cntSecret[i] += 2

            if (cntSecret[i] >= (plrs[i].ssecret * 100) / wbs.maxsecret)
                cntSecret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret
            else
                stillticking = true
        }

        if (!stillticking) {
            sStartSound(null, SFX_BAREXP)
            ngState += 1 + 2 * (if (dofrags != 0) 0 else 1)
        }
    } else if (ngState == 8) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        stillticking = false

        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue

            cntFrags[i] += 1

            fsum = wiFragSum(i)
            if (cntFrags[i] >= fsum)
                cntFrags[i] = fsum
            else
                stillticking = true
        }

        if (!stillticking) {
            sStartSound(null, SFX_PLDETH)
            ngState++
        }
    } else if (ngState == 10) {
        if (acceleratestage != 0) {
            sStartSound(null, SFX_SGCOCK)
            if (gamemode == COMMERCIAL)
                wiInitNoState()
            else
                wiInitShowNextLoc()
        }
    } else if ((ngState and 1) != 0) {
        cntPause--
        if (cntPause == 0) {
            ngState++
            cntPause = TICRATE
        }
    }
}

internal fun DoomEngineCore.wiDrawNetgameStats() {
    var x: Int
    var y: Int
    val pwidth = patchWidth(percent)

    wiSlamBackground()

    wiDrawAnimatedBack()

    wiDrawLF()

    vDrawPatch(ngSTATSX() + NG_SPACINGX - patchWidth(kills),
        NG_STATSY, FB, kills)

    vDrawPatch(ngSTATSX() + 2 * NG_SPACINGX - patchWidth(items),
        NG_STATSY, FB, items)

    vDrawPatch(ngSTATSX() + 3 * NG_SPACINGX - patchWidth(secret),
        NG_STATSY, FB, secret)

    if (dofrags != 0)
        vDrawPatch(ngSTATSX() + 4 * NG_SPACINGX - patchWidth(frags),
            NG_STATSY, FB, frags)

    y = NG_STATSY + patchHeight(kills)

    for (i in 0 until MAXPLAYERS) {
        if (!playeringame[i])
            continue

        x = ngSTATSX()
        vDrawPatch(x - patchWidth(p[i]), y, FB, p[i])

        if (i == me)
            vDrawPatch(x - patchWidth(p[i]), y, FB, star)

        x += NG_SPACINGX
        wiDrawPercent(x - pwidth, y + 10, cntKills[i]); x += NG_SPACINGX
        wiDrawPercent(x - pwidth, y + 10, cntItems[i]); x += NG_SPACINGX
        wiDrawPercent(x - pwidth, y + 10, cntSecret[i]); x += NG_SPACINGX

        if (dofrags != 0)
            wiDrawNum(x, y + 10, cntFrags[i], -1)

        y += WI_SPACINGY
    }
}

private var DoomEngineCore.spState
    get() = stateIntermission.spState
    set(value) { stateIntermission.spState = value }

internal fun DoomEngineCore.wiInitStats() {
    state = STAT_COUNT
    acceleratestage = 0
    spState = 1
    cntKills[0] = -1; cntItems[0] = -1; cntSecret[0] = -1
    cntTime = -1; cntPar = -1
    cntPause = TICRATE

    wiInitAnimatedBack()
}

internal fun DoomEngineCore.wiUpdateStats() {
    wiUpdateAnimatedBack()

    if (acceleratestage != 0 && spState != 10) {
        acceleratestage = 0
        cntKills[0] = (plrs[me].skills * 100) / wbs.maxkills
        cntItems[0] = (plrs[me].sitems * 100) / wbs.maxitems
        cntSecret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret
        cntTime = plrs[me].stime / TICRATE
        cntPar = wbs.partime / TICRATE
        sStartSound(null, SFX_BAREXP)
        spState = 10
    }

    if (spState == 2) {
        cntKills[0] += 2

        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        if (cntKills[0] >= (plrs[me].skills * 100) / wbs.maxkills) {
            cntKills[0] = (plrs[me].skills * 100) / wbs.maxkills
            sStartSound(null, SFX_BAREXP)
            spState++
        }
    } else if (spState == 4) {
        cntItems[0] += 2

        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        if (cntItems[0] >= (plrs[me].sitems * 100) / wbs.maxitems) {
            cntItems[0] = (plrs[me].sitems * 100) / wbs.maxitems
            sStartSound(null, SFX_BAREXP)
            spState++
        }
    } else if (spState == 6) {
        cntSecret[0] += 2

        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        if (cntSecret[0] >= (plrs[me].ssecret * 100) / wbs.maxsecret) {
            cntSecret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret
            sStartSound(null, SFX_BAREXP)
            spState++
        }
    } else if (spState == 8) {
        if ((bcnt and 3) == 0)
            sStartSound(null, SFX_PISTOL)

        cntTime += 3

        if (cntTime >= plrs[me].stime / TICRATE)
            cntTime = plrs[me].stime / TICRATE

        cntPar += 3

        if (cntPar >= wbs.partime / TICRATE) {
            cntPar = wbs.partime / TICRATE

            if (cntTime >= plrs[me].stime / TICRATE) {
                sStartSound(null, SFX_BAREXP)
                spState++
            }
        }
    } else if (spState == 10) {
        if (acceleratestage != 0) {
            sStartSound(null, SFX_SGCOCK)

            if (gamemode == COMMERCIAL)
                wiInitNoState()
            else
                wiInitShowNextLoc()
        }
    } else if ((spState and 1) != 0) {
        cntPause--
        if (cntPause == 0) {
            spState++
            cntPause = TICRATE
        }
    }
}

internal fun DoomEngineCore.wiDrawStats() {
    val lh: Int

    lh = (3 * patchHeight(num[0])) / 2

    wiSlamBackground()

    wiDrawAnimatedBack()

    wiDrawLF()

    vDrawPatch(SP_STATSX, SP_STATSY, FB, kills)
    wiDrawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY, cntKills[0])

    vDrawPatch(SP_STATSX, SP_STATSY + lh, FB, items)
    wiDrawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY + lh, cntItems[0])

    vDrawPatch(SP_STATSX, SP_STATSY + 2 * lh, FB, spSecret)
    wiDrawPercent(SCREENWIDTH - SP_STATSX, SP_STATSY + 2 * lh, cntSecret[0])

    vDrawPatch(SP_TIMEX, SP_TIMEY, FB, time)
    wiDrawTime(SCREENWIDTH / 2 - SP_TIMEX, SP_TIMEY, cntTime)

    if (wbs.epsd < 3) {
        vDrawPatch(SCREENWIDTH / 2 + SP_TIMEX, SP_TIMEY, FB, par)
        wiDrawTime(SCREENWIDTH - SP_TIMEX, SP_TIMEY, cntPar)
    }
}

internal fun DoomEngineCore.wiCheckForAccelerate() {
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

internal fun DoomEngineCore.wiTicker() {
    bcnt++

    if (bcnt == 1) {
        if (gamemode == COMMERCIAL)
            sChangeMusic(MUS_DM2INT, 1)
        else
            sChangeMusic(MUS_INTER, 1)
    }

    wiCheckForAccelerate()

    when (state) {
        STAT_COUNT -> {
            if (deathmatch != 0) wiUpdateDeathmatchStats()
            else if (netgame) wiUpdateNetgameStats()
            else wiUpdateStats()
        }

        SHOW_NEXT_LOC -> wiUpdateShowNextLoc()

        NO_STATE -> wiUpdateNoState()
    }
}

internal fun DoomEngineCore.wiLoadData() {
    var name: String

    if (gamemode == COMMERCIAL)
        name = "INTERPIC"
    else
        name = "WIMAP${wbs.epsd}"

    if (gamemode == RETAIL) {
        if (wbs.epsd == 3)
            name = "INTERPIC"
    }

    bg = wCacheLumpName(name)
    vDrawPatch(0, 0, 1, bg)


    if (gamemode == COMMERCIAL) {
        numcmaps = 32
        lnames = Array(numcmaps) { ByteArray(0) }
        for (i in 0 until numcmaps) {
            name = "CWILV" + i.toString().padStart(2, '0')
            lnames[i] = wCacheLumpName(name)
        }
    } else {
        lnames = Array(NUMMAPS) { ByteArray(0) }
        for (i in 0 until NUMMAPS) {
            name = "WILV${wbs.epsd}$i"
            lnames[i] = wCacheLumpName(name)
        }

        yah[0] = wCacheLumpName("WIURH0")

        yah[1] = wCacheLumpName("WIURH1")

        splat = wCacheLumpName("WISPLAT")

        if (wbs.epsd < 3) {
            for (j in 0 until numanims[wbs.epsd]) {
                val a = wiAnims[wbs.epsd][j]
                for (i in 0 until a.nanims) {
                    if (wbs.epsd != 1 || j != 8) {
                        name = "WIA${wbs.epsd}" +
                            j.toString().padStart(2, '0') +
                            i.toString().padStart(2, '0')
                        a.p[i] = wCacheLumpName(name)
                    } else {
                        a.p[i] = wiAnims[1][4].p[i]
                    }
                }
            }
        }
    }

    wiminus = wCacheLumpName("WIMINUS")

    for (i in 0 until 10) {
        name = "WINUM$i"
        num[i] = wCacheLumpName(name)
    }

    percent = wCacheLumpName("WIPCNT")

    finished = wCacheLumpName("WIF")

    entering = wCacheLumpName("WIENTER")

    kills = wCacheLumpName("WIOSTK")

    secret = wCacheLumpName("WIOSTS")

    spSecret = wCacheLumpName("WISCRT2")

    if (FRENCH != 0) {
        if (netgame && deathmatch == 0)
            items = wCacheLumpName("WIOBJ")
        else
            items = wCacheLumpName("WIOSTI")
    } else
        items = wCacheLumpName("WIOSTI")

    frags = wCacheLumpName("WIFRGS")

    colon = wCacheLumpName("WICOLON")

    time = wCacheLumpName("WITIME")

    sucks = wCacheLumpName("WISUCKS")

    par = wCacheLumpName("WIPAR")

    killers = wCacheLumpName("WIKILRS")

    victims = wCacheLumpName("WIVCTMS")

    total = wCacheLumpName("WIMSTT")

    star = wCacheLumpName("STFST01")

    bstar = wCacheLumpName("STFDEAD0")

    for (i in 0 until MAXPLAYERS) {
        name = "STPB$i"
        p[i] = wCacheLumpName(name)

        name = "WIBP${i + 1}"
        bp[i] = wCacheLumpName(name)
    }
}

internal fun DoomEngineCore.wiUnloadData() {
}

internal fun DoomEngineCore.wiDrawer() {
    when (state) {
        STAT_COUNT -> {
            if (deathmatch != 0)
                wiDrawDeathmatchStats()
            else if (netgame)
                wiDrawNetgameStats()
            else
                wiDrawStats()
        }

        SHOW_NEXT_LOC -> wiDrawShowNextLoc()

        NO_STATE -> wiDrawNoState()
    }
}

internal fun DoomEngineCore.wiInitVariables(wbstartstruct: IntermissionSummary) {
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

    if (gamemode != RETAIL)
        if (wbs.epsd > 2)
            wbs.epsd -= 3
}

internal fun DoomEngineCore.wiStart(wbstartstruct: IntermissionSummary) {
    wiInitVariables(wbstartstruct)
    wiLoadData()

    if (deathmatch != 0)
        wiInitDeathmatchStats()
    else if (netgame)
        wiInitNetgameStats()
    else
        wiInitStats()
}
