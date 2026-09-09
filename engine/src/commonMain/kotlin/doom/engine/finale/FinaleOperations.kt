
package doom.engine.finale

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.audio.sChangeMusic
import doom.engine.audio.sStartMusic
import doom.engine.audio.sStartSound
import doom.engine.audio.MUS_BUNNY
import doom.engine.audio.MUS_EVIL
import doom.engine.audio.MUS_READ_M
import doom.engine.audio.MUS_VICTOR
import doom.engine.audio.SFX_CLAW
import doom.engine.audio.SFX_DSHTGN
import doom.engine.audio.SFX_FIRSHT
import doom.engine.audio.SFX_PISTOL
import doom.engine.audio.SFX_PLASMA
import doom.engine.audio.SFX_RLAUNC
import doom.engine.audio.SFX_SGTATK
import doom.engine.audio.SFX_SHOTGN
import doom.engine.audio.SFX_SKEATK
import doom.engine.audio.SFX_SKEPCH
import doom.engine.audio.SFX_SKESWG
import doom.engine.audio.SFX_SKLATK
import doom.engine.audio.SFX_VILATK
import doom.engine.automap.automapactive
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.core.wipegamestate
import doom.engine.gameplay.GS_FINALE
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.actors.FF_FRAMEMASK
import doom.engine.gameplay.actors.S_BOS2_ATK2
import doom.engine.gameplay.actors.S_BOSS_ATK2
import doom.engine.gameplay.actors.S_BSPI_ATK2
import doom.engine.gameplay.actors.S_CPOS_ATK2
import doom.engine.gameplay.actors.S_CPOS_ATK3
import doom.engine.gameplay.actors.S_CPOS_ATK4
import doom.engine.gameplay.actors.S_CYBER_ATK2
import doom.engine.gameplay.actors.S_CYBER_ATK4
import doom.engine.gameplay.actors.S_CYBER_ATK6
import doom.engine.gameplay.actors.S_FATT_ATK2
import doom.engine.gameplay.actors.S_FATT_ATK5
import doom.engine.gameplay.actors.S_FATT_ATK8
import doom.engine.gameplay.actors.S_HEAD_ATK2
import doom.engine.gameplay.actors.S_NULL
import doom.engine.gameplay.actors.S_PAIN_ATK3
import doom.engine.gameplay.actors.S_PLAY_ATK1
import doom.engine.gameplay.actors.S_POSS_ATK2
import doom.engine.gameplay.actors.S_SARG_ATK2
import doom.engine.gameplay.actors.S_SKEL_FIST2
import doom.engine.gameplay.actors.S_SKEL_FIST4
import doom.engine.gameplay.actors.S_SKEL_MISS2
import doom.engine.gameplay.actors.S_SKULL_ATK2
import doom.engine.gameplay.actors.S_SPID_ATK2
import doom.engine.gameplay.actors.S_SPID_ATK3
import doom.engine.gameplay.actors.S_SPOS_ATK2
import doom.engine.gameplay.actors.S_TROO_ATK3
import doom.engine.gameplay.actors.S_VILE_ATK2
import doom.engine.gameplay.actors.StateDefinition
import doom.engine.gameplay.actors.mobjinfo
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.GA_NOTHING
import doom.engine.gameplay.GA_WORLDDONE
import doom.engine.gameplay.gameaction
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gamestate
import doom.engine.gameplay.players
import doom.engine.gameplay.REGISTERED
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.viewactive
import doom.engine.hud.HU_FONTSIZE
import doom.engine.hud.HU_FONTSTART
import doom.engine.hud.huFont
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.vMarkRect
import doom.engine.rendering.patchColumnOfs
import doom.engine.rendering.patchHeight
import doom.engine.rendering.patchLeftOffset
import doom.engine.rendering.patchTopOffset
import doom.engine.rendering.patchWidth
import doom.engine.rendering.resources.firstspritelump
import doom.engine.rendering.screens
import doom.engine.rendering.sprites
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.u8

internal var DoomEngineCore.finalestage
    get() = stateFinale.finalestage
    set(value) { stateFinale.finalestage = value }

internal var DoomEngineCore.finalecount
    get() = stateFinale.finalecount
    set(value) { stateFinale.finalecount = value }

private const val TEXTSPEED = 3
private const val TEXTWAIT = 250

internal var DoomEngineCore.e1text
    get() = stateFinale.e1text
    set(value) { stateFinale.e1text = value }
internal var DoomEngineCore.e2text
    get() = stateFinale.e2text
    set(value) { stateFinale.e2text = value }
internal var DoomEngineCore.e3text
    get() = stateFinale.e3text
    set(value) { stateFinale.e3text = value }
internal var DoomEngineCore.e4text
    get() = stateFinale.e4text
    set(value) { stateFinale.e4text = value }

internal var DoomEngineCore.c1text
    get() = stateFinale.c1text
    set(value) { stateFinale.c1text = value }
internal var DoomEngineCore.c2text
    get() = stateFinale.c2text
    set(value) { stateFinale.c2text = value }
internal var DoomEngineCore.c3text
    get() = stateFinale.c3text
    set(value) { stateFinale.c3text = value }
internal var DoomEngineCore.c4text
    get() = stateFinale.c4text
    set(value) { stateFinale.c4text = value }
internal var DoomEngineCore.c5text
    get() = stateFinale.c5text
    set(value) { stateFinale.c5text = value }
internal var DoomEngineCore.c6text
    get() = stateFinale.c6text
    set(value) { stateFinale.c6text = value }

internal var DoomEngineCore.p1text
    get() = stateFinale.p1text
    set(value) { stateFinale.p1text = value }
internal var DoomEngineCore.p2text
    get() = stateFinale.p2text
    set(value) { stateFinale.p2text = value }
internal var DoomEngineCore.p3text
    get() = stateFinale.p3text
    set(value) { stateFinale.p3text = value }
internal var DoomEngineCore.p4text
    get() = stateFinale.p4text
    set(value) { stateFinale.p4text = value }
internal var DoomEngineCore.p5text
    get() = stateFinale.p5text
    set(value) { stateFinale.p5text = value }
internal var DoomEngineCore.p6text
    get() = stateFinale.p6text
    set(value) { stateFinale.p6text = value }

internal var DoomEngineCore.t1text
    get() = stateFinale.t1text
    set(value) { stateFinale.t1text = value }
internal var DoomEngineCore.t2text
    get() = stateFinale.t2text
    set(value) { stateFinale.t2text = value }
internal var DoomEngineCore.t3text
    get() = stateFinale.t3text
    set(value) { stateFinale.t3text = value }
internal var DoomEngineCore.t4text
    get() = stateFinale.t4text
    set(value) { stateFinale.t4text = value }
internal var DoomEngineCore.t5text
    get() = stateFinale.t5text
    set(value) { stateFinale.t5text = value }
internal var DoomEngineCore.t6text
    get() = stateFinale.t6text
    set(value) { stateFinale.t6text = value }

internal var DoomEngineCore.finaletext
    get() = stateFinale.finaletext
    set(value) { stateFinale.finaletext = value }
internal var DoomEngineCore.finaleflat
    get() = stateFinale.finaleflat
    set(value) { stateFinale.finaleflat = value }

private fun DoomEngineCore.toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

internal fun DoomEngineCore.fStartFinale() {
    gameaction = GA_NOTHING
    gamestate = GS_FINALE
    viewactive = false
    automapactive = false

    when (gamemode) {

        SHAREWARE,
        REGISTERED,
        RETAIL -> {
            sChangeMusic(MUS_VICTOR, 1)

            when (gameepisode) {
                1 -> {
                    finaleflat = "FLOOR4_8"
                    finaletext = e1text
                }
                2 -> {
                    finaleflat = "SFLR6_1"
                    finaletext = e2text
                }
                3 -> {
                    finaleflat = "MFLR8_4"
                    finaletext = e3text
                }
                4 -> {
                    finaleflat = "MFLR8_3"
                    finaletext = e4text
                }
                else -> {
                }
            }
        }

        COMMERCIAL -> {
            sChangeMusic(MUS_READ_M, 1)

            when (gamemap) {
                6 -> {
                    finaleflat = "SLIME16"
                    finaletext = c1text
                }
                11 -> {
                    finaleflat = "RROCK14"
                    finaletext = c2text
                }
                20 -> {
                    finaleflat = "RROCK07"
                    finaletext = c3text
                }
                30 -> {
                    finaleflat = "RROCK17"
                    finaletext = c4text
                }
                15 -> {
                    finaleflat = "RROCK13"
                    finaletext = c5text
                }
                31 -> {
                    finaleflat = "RROCK19"
                    finaletext = c6text
                }
                else -> {
                }
            }
        }

        else -> {
            sChangeMusic(MUS_READ_M, 1)
            finaleflat = "F_SKY1"
            finaletext = c1text
        }
    }

    finalestage = 0
    finalecount = 0
}

internal fun DoomEngineCore.fResponder(event: EngineEvent): Boolean {
    if (finalestage == 2)
        return fCastResponder(event)

    return false
}

internal fun DoomEngineCore.fTicker() {
    var i: Int

    if ((gamemode == COMMERCIAL) && (finalecount > 50)) {
        i = 0
        while (i < MAXPLAYERS) {
            if (players[i].cmd.buttons != 0)
                break
            i++
        }

        if (i < MAXPLAYERS) {
            if (gamemap == 30)
                fStartCast()
            else
                gameaction = GA_WORLDDONE
        }
    }

    finalecount++

    if (finalestage == 2) {
        fCastTicker()
        return
    }

    if (gamemode == COMMERCIAL)
        return

    if (finalestage == 0 && finalecount > finaletext.length * TEXTSPEED + TEXTWAIT) {
        finalecount = 0
        finalestage = 1
        wipegamestate = -1
        if (gameepisode == 3)
            sStartMusic(MUS_BUNNY)
    }
}

internal fun DoomEngineCore.fTextWrite() {
    val src = wCacheLumpName(finaleflat)
    val dest = screens[0]
    var destofs = 0

    for (y in 0 until SCREENHEIGHT) {
        for (x in 0 until SCREENWIDTH / 64) {
            src.copyInto(dest, destofs, (y and 63) shl 6, ((y and 63) shl 6) + 64)
            destofs += 64
        }
        if (SCREENWIDTH and 63 != 0) {
            src.copyInto(dest, destofs, (y and 63) shl 6, ((y and 63) shl 6) + (SCREENWIDTH and 63))
            destofs += SCREENWIDTH and 63
        }
    }

    vMarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)

    var cx = 10
    var cy = 10
    var ch = 0

    var count = (finalecount - 10) / TEXTSPEED
    if (count < 0)
        count = 0
    while (count != 0) {
        if (ch >= finaletext.length)
            break
        var c = finaletext[ch].code
        ch++
        if (c == '\n'.code) {
            cx = 10
            cy += 11
            count--
            continue
        }

        c = toupper(c) - HU_FONTSTART
        if (c < 0 || c > HU_FONTSIZE) {
            cx += 4
            count--
            continue
        }

        val w = patchWidth(huFont[c])
        if (cx + w > SCREENWIDTH)
            break
        vDrawPatch(cx, cy, 0, huFont[c])
        cx += w
        count--
    }
}

internal val DoomEngineCore.castorder
    get() = stateFinale.castorder

internal var DoomEngineCore.castnum
    get() = stateFinale.castnum
    set(value) { stateFinale.castnum = value }
internal var DoomEngineCore.casttics
    get() = stateFinale.casttics
    set(value) { stateFinale.casttics = value }
internal var DoomEngineCore.caststate: StateDefinition
    get() = stateFinale.caststate
    set(value) { stateFinale.caststate = value }
internal var DoomEngineCore.castdeath
    get() = stateFinale.castdeath
    set(value) { stateFinale.castdeath = value }
internal var DoomEngineCore.castframes
    get() = stateFinale.castframes
    set(value) { stateFinale.castframes = value }
internal var DoomEngineCore.castonmelee
    get() = stateFinale.castonmelee
    set(value) { stateFinale.castonmelee = value }
internal var DoomEngineCore.castattacking
    get() = stateFinale.castattacking
    set(value) { stateFinale.castattacking = value }

internal fun DoomEngineCore.fStartCast() {
    wipegamestate = -1
    castnum = 0
    caststate = states[mobjinfo[castorder[castnum].type].seestate]
    casttics = caststate.tics
    castdeath = false
    finalestage = 2
    castframes = 0
    castonmelee = 0
    castattacking = false
    sChangeMusic(MUS_EVIL, 1)
}

internal fun DoomEngineCore.fCastTicker() {
    casttics--
    if (casttics > 0)
        return

    var stopattack = false

    if (caststate.tics == -1 || caststate.nextstate == S_NULL) {
        castnum++
        castdeath = false
        if (castorder[castnum].name == null)
            castnum = 0
        if (mobjinfo[castorder[castnum].type].seesound != 0)
            sStartSound(null, mobjinfo[castorder[castnum].type].seesound)
        caststate = states[mobjinfo[castorder[castnum].type].seestate]
        castframes = 0
    } else {
        if (caststate === states[S_PLAY_ATK1]) {
            stopattack = true
        } else {
            val st = caststate.nextstate
            caststate = states[st]
            castframes++

            val sfx = when (st) {
                S_PLAY_ATK1 -> SFX_DSHTGN
                S_POSS_ATK2 -> SFX_PISTOL
                S_SPOS_ATK2 -> SFX_SHOTGN
                S_VILE_ATK2 -> SFX_VILATK
                S_SKEL_FIST2 -> SFX_SKESWG
                S_SKEL_FIST4 -> SFX_SKEPCH
                S_SKEL_MISS2 -> SFX_SKEATK
                S_FATT_ATK8,
                S_FATT_ATK5,
                S_FATT_ATK2 -> SFX_FIRSHT
                S_CPOS_ATK2,
                S_CPOS_ATK3,
                S_CPOS_ATK4 -> SFX_SHOTGN
                S_TROO_ATK3 -> SFX_CLAW
                S_SARG_ATK2 -> SFX_SGTATK
                S_BOSS_ATK2,
                S_BOS2_ATK2,
                S_HEAD_ATK2 -> SFX_FIRSHT
                S_SKULL_ATK2 -> SFX_SKLATK
                S_SPID_ATK2,
                S_SPID_ATK3 -> SFX_SHOTGN
                S_BSPI_ATK2 -> SFX_PLASMA
                S_CYBER_ATK2,
                S_CYBER_ATK4,
                S_CYBER_ATK6 -> SFX_RLAUNC
                S_PAIN_ATK3 -> SFX_SKLATK
                else -> 0
            }

            if (sfx != 0)
                sStartSound(null, sfx)
        }
    }

    if (!stopattack) {
        if (castframes == 12) {
            castattacking = true
            if (castonmelee != 0)
                caststate = states[mobjinfo[castorder[castnum].type].meleestate]
            else
                caststate = states[mobjinfo[castorder[castnum].type].missilestate]
            castonmelee = castonmelee xor 1
            if (caststate === states[S_NULL]) {
                if (castonmelee != 0)
                    caststate = states[mobjinfo[castorder[castnum].type].meleestate]
                else
                    caststate = states[mobjinfo[castorder[castnum].type].missilestate]
            }
        }
    }

    if (stopattack ||
        (castattacking &&
            (castframes == 24 ||
                caststate === states[mobjinfo[castorder[castnum].type].seestate]))
    ) {
        castattacking = false
        castframes = 0
        caststate = states[mobjinfo[castorder[castnum].type].seestate]
    }

    casttics = caststate.tics
    if (casttics == -1)
        casttics = 15
}

internal fun DoomEngineCore.fCastResponder(ev: EngineEvent): Boolean {
    if (ev.type != EV_KEYDOWN)
        return false

    if (castdeath)
        return true

    castdeath = true
    caststate = states[mobjinfo[castorder[castnum].type].deathstate]
    casttics = caststate.tics
    castframes = 0
    castattacking = false
    if (mobjinfo[castorder[castnum].type].deathsound != 0)
        sStartSound(null, mobjinfo[castorder[castnum].type].deathsound)

    return true
}

internal fun DoomEngineCore.fCastPrint(text: String) {
    var ch = 0
    var width = 0

    while (true) {
        if (ch >= text.length)
            break
        var c = text[ch].code
        ch++
        c = toupper(c) - HU_FONTSTART
        if (c < 0 || c > HU_FONTSIZE) {
            width += 4
            continue
        }

        val w = patchWidth(huFont[c])
        width += w
    }

    var cx = 160 - width / 2
    ch = 0
    while (true) {
        if (ch >= text.length)
            break
        var c = text[ch].code
        ch++
        c = toupper(c) - HU_FONTSTART
        if (c < 0 || c > HU_FONTSIZE) {
            cx += 4
            continue
        }

        val w = patchWidth(huFont[c])
        vDrawPatch(cx, 180, 0, huFont[c])
        cx += w
    }
}

private fun DoomEngineCore.fDrawPatchFlipped(x0: Int, y0: Int, scrn: Int, patch: ByteArray) {
    val y = y0 - patchTopOffset(patch)
    val x = x0 - patchLeftOffset(patch)
    val w = patchWidth(patch)

    if (x < 0 || x + w > SCREENWIDTH || y < 0 ||
        y + patchHeight(patch) > SCREENHEIGHT || scrn > 4
    ) {
        println("Patch origin $x,$y exceeds LFB")
        iError("Bad V_DrawPatch in V_DrawPatchFlipped")
    }

    if (scrn == 0)
        vMarkRect(x, y, w, patchHeight(patch))

    val dest = screens[scrn]
    val desttop = y * SCREENWIDTH + x

    for (col in 0 until w) {
        var ofs = patchColumnOfs(patch, w - 1 - col)

        while (patch.u8(ofs) != 0xff) {
            val topdelta = patch.u8(ofs)
            val count = patch.u8(ofs + 1)
            var source = ofs + 3
            var destofs = desttop + col + topdelta * SCREENWIDTH
            for (i in 0 until count) {
                dest[destofs] = patch[source]
                source++
                destofs += SCREENWIDTH
            }
            ofs += 4 + count
        }
    }
}

internal fun DoomEngineCore.fCastDrawer() {
    vDrawPatch(0, 0, 0, wCacheLumpName("BOSSBACK"))

    fCastPrint(castorder[castnum].name!!)

    val sprdef = sprites[caststate.sprite]
    val sprframe = sprdef.spriteframes[caststate.frame and FF_FRAMEMASK]!!
    val lump = sprframe.lump[0]
    val flip = sprframe.flip[0] != 0

    val patch = wCacheLumpNum(lump + firstspritelump)
    if (flip)
        fDrawPatchFlipped(160, 170, 0, patch)
    else
        vDrawPatch(160, 170, 0, patch)
}

internal fun DoomEngineCore.fDrawPatchCol(x: Int, patch: ByteArray, col: Int) {
    var ofs = patchColumnOfs(patch, col)
    val dest = screens[0]
    val desttop = x

    while (patch.u8(ofs) != 0xff) {
        val topdelta = patch.u8(ofs)
        val length = patch.u8(ofs + 1)
        var source = ofs + 3
        var destofs = desttop + topdelta * SCREENWIDTH
        var count = length

        while (count-- != 0) {
            dest[destofs] = patch[source]
            source++
            destofs += SCREENWIDTH
        }
        ofs += length + 4
    }
}

private var DoomEngineCore.laststage
    get() = stateFinale.laststage
    set(value) { stateFinale.laststage = value }

internal fun DoomEngineCore.fBunnyScroll() {
    val p1 = wCacheLumpName("PFUB2")
    val p2 = wCacheLumpName("PFUB1")

    vMarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)

    var scrolled = 320 - (finalecount - 230) / 2
    if (scrolled > 320)
        scrolled = 320
    if (scrolled < 0)
        scrolled = 0

    for (x in 0 until SCREENWIDTH) {
        if (x + scrolled < 320)
            fDrawPatchCol(x, p1, x + scrolled)
        else
            fDrawPatchCol(x, p2, x + scrolled - 320)
    }

    if (finalecount < 1130)
        return
    if (finalecount < 1180) {
        vDrawPatch(
            (SCREENWIDTH - 13 * 8) / 2,
            (SCREENHEIGHT - 8 * 8) / 2, 0, wCacheLumpName("END0")
        )
        laststage = 0
        return
    }

    var stage = (finalecount - 1180) / 5
    if (stage > 6)
        stage = 6
    if (stage > laststage) {
        sStartSound(null, SFX_PISTOL)
        laststage = stage
    }

    val name = "END$stage"
    vDrawPatch((SCREENWIDTH - 13 * 8) / 2, (SCREENHEIGHT - 8 * 8) / 2, 0, wCacheLumpName(name))
}

internal fun DoomEngineCore.fDrawer() {
    if (finalestage == 2) {
        fCastDrawer()
        return
    }

    if (finalestage == 0)
        fTextWrite()
    else {
        when (gameepisode) {
            1 ->
                if (gamemode == RETAIL)
                    vDrawPatch(0, 0, 0, wCacheLumpName("CREDIT"))
                else
                    vDrawPatch(0, 0, 0, wCacheLumpName("HELP2"))
            2 ->
                vDrawPatch(0, 0, 0, wCacheLumpName("VICTORY2"))
            3 ->
                fBunnyScroll()
            4 ->
                vDrawPatch(0, 0, 0, wCacheLumpName("ENDPIC"))
        }
    }
}
