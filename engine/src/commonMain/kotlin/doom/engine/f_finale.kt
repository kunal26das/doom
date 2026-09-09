// Port of linuxdoom-1.10 f_finale.c -- game completion, final screen animation.
// (F_DrawPatchFlipped is the vanilla v_video.c V_DrawPatchFlipped, kept local
// here because the cast drawer is its only caller.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

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

// C-locale toupper (ASCII a-z only, exactly like the DOS/linux original).
private fun DoomEngineCore.toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

//
// F_StartFinale
//
internal fun DoomEngineCore.F_StartFinale() {
    gameaction = ga_nothing
    gamestate = GS_FINALE
    viewactive = false
    automapactive = false

    // Okay - IWAD dependend stuff.
    // This has been changed severly, and
    //  some stuff might have changed in the process.
    when (gamemode) {

        // DOOM 1 - E1, E3 or E4, but each nine missions
        shareware,
        registered,
        retail -> {
            S_ChangeMusic(mus_victor, 1)

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
                    // Ouch.
                }
            }
        }

        // DOOM II and missions packs with E1, M34
        commercial -> {
            S_ChangeMusic(mus_read_m, 1)

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
                    // Ouch.
                }
            }
        }

        // Indeterminate.
        else -> {
            S_ChangeMusic(mus_read_m, 1)
            finaleflat = "F_SKY1" // Not used anywhere else.
            finaletext = c1text // FIXME - other text, music?
        }
    }

    finalestage = 0
    finalecount = 0
}

internal fun DoomEngineCore.F_Responder(event: event_t): Boolean {
    if (finalestage == 2)
        return F_CastResponder(event)

    return false
}

//
// F_Ticker
//
internal fun DoomEngineCore.F_Ticker() {
    var i: Int

    // check for skipping
    if ((gamemode == commercial) && (finalecount > 50)) {
        // go on to the next level
        i = 0
        while (i < MAXPLAYERS) {
            if (players[i].cmd.buttons != 0)
                break
            i++
        }

        if (i < MAXPLAYERS) {
            if (gamemap == 30)
                F_StartCast()
            else
                gameaction = ga_worlddone
        }
    }

    // advance animation
    finalecount++

    if (finalestage == 2) {
        F_CastTicker()
        return
    }

    if (gamemode == commercial)
        return

    if (finalestage == 0 && finalecount > finaletext.length * TEXTSPEED + TEXTWAIT) {
        finalecount = 0
        finalestage = 1
        wipegamestate = -1 // force a wipe
        if (gameepisode == 3)
            S_StartMusic(mus_bunny)
    }
}

//
// F_TextWrite
//
internal fun DoomEngineCore.F_TextWrite() {
    // erase the entire screen to a tiled background
    val src = W_CacheLumpName(finaleflat)
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

    V_MarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)

    // draw some of the text onto the screen
    var cx = 10
    var cy = 10
    var ch = 0 // index into finaletext (C: char* ch)

    var count = (finalecount - 10) / TEXTSPEED
    if (count < 0)
        count = 0
    while (count != 0) {
        if (ch >= finaletext.length) // c = *ch++; if (!c) break;
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

        val w = patchWidth(hu_font[c])
        if (cx + w > SCREENWIDTH)
            break
        V_DrawPatch(cx, cy, 0, hu_font[c])
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
internal var DoomEngineCore.caststate: state_t
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

//
// F_StartCast
//
internal fun DoomEngineCore.F_StartCast() {
    wipegamestate = -1 // force a screen wipe
    castnum = 0
    caststate = states[mobjinfo[castorder[castnum].type].seestate]
    casttics = caststate.tics
    castdeath = false
    finalestage = 2
    castframes = 0
    castonmelee = 0
    castattacking = false
    S_ChangeMusic(mus_evil, 1)
}

//
// F_CastTicker
//
internal fun DoomEngineCore.F_CastTicker() {
    casttics--
    if (casttics > 0)
        return // not time to change state yet

    // (C: goto stopattack from the S_PLAY_ATK1 hack below)
    var stopattack = false

    if (caststate.tics == -1 || caststate.nextstate == S_NULL) {
        // switch from deathstate to next monster
        castnum++
        castdeath = false
        if (castorder[castnum].name == null)
            castnum = 0
        if (mobjinfo[castorder[castnum].type].seesound != 0)
            S_StartSound(null, mobjinfo[castorder[castnum].type].seesound)
        caststate = states[mobjinfo[castorder[castnum].type].seestate]
        castframes = 0
    } else {
        // just advance to next state in animation
        if (caststate === states[S_PLAY_ATK1]) {
            stopattack = true // Oh, gross hack!
        } else {
            val st = caststate.nextstate
            caststate = states[st]
            castframes++

            // sound hacks....
            val sfx = when (st) {
                S_PLAY_ATK1 -> sfx_dshtgn
                S_POSS_ATK2 -> sfx_pistol
                S_SPOS_ATK2 -> sfx_shotgn
                S_VILE_ATK2 -> sfx_vilatk
                S_SKEL_FIST2 -> sfx_skeswg
                S_SKEL_FIST4 -> sfx_skepch
                S_SKEL_MISS2 -> sfx_skeatk
                S_FATT_ATK8,
                S_FATT_ATK5,
                S_FATT_ATK2 -> sfx_firsht
                S_CPOS_ATK2,
                S_CPOS_ATK3,
                S_CPOS_ATK4 -> sfx_shotgn
                S_TROO_ATK3 -> sfx_claw
                S_SARG_ATK2 -> sfx_sgtatk
                S_BOSS_ATK2,
                S_BOS2_ATK2,
                S_HEAD_ATK2 -> sfx_firsht
                S_SKULL_ATK2 -> sfx_sklatk
                S_SPID_ATK2,
                S_SPID_ATK3 -> sfx_shotgn
                S_BSPI_ATK2 -> sfx_plasma
                S_CYBER_ATK2,
                S_CYBER_ATK4,
                S_CYBER_ATK6 -> sfx_rlaunc
                S_PAIN_ATK3 -> sfx_sklatk
                else -> 0
            }

            if (sfx != 0)
                S_StartSound(null, sfx)
        }
    }

    if (!stopattack) {
        if (castframes == 12) {
            // go into attack frame
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
        // stopattack:
        castattacking = false
        castframes = 0
        caststate = states[mobjinfo[castorder[castnum].type].seestate]
    }

    casttics = caststate.tics
    if (casttics == -1)
        casttics = 15
}

//
// F_CastResponder
//
internal fun DoomEngineCore.F_CastResponder(ev: event_t): Boolean {
    if (ev.type != ev_keydown)
        return false

    if (castdeath)
        return true // already in dying frames

    // go into death frame
    castdeath = true
    caststate = states[mobjinfo[castorder[castnum].type].deathstate]
    casttics = caststate.tics
    castframes = 0
    castattacking = false
    if (mobjinfo[castorder[castnum].type].deathsound != 0)
        S_StartSound(null, mobjinfo[castorder[castnum].type].deathsound)

    return true
}

internal fun DoomEngineCore.F_CastPrint(text: String) {
    // find width
    var ch = 0 // index into text (C: char* ch)
    var width = 0

    while (true) {
        if (ch >= text.length) // c = *ch++; if (!c) break;
            break
        var c = text[ch].code
        ch++
        c = toupper(c) - HU_FONTSTART
        if (c < 0 || c > HU_FONTSIZE) {
            width += 4
            continue
        }

        val w = patchWidth(hu_font[c])
        width += w
    }

    // draw it
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

        val w = patchWidth(hu_font[c])
        V_DrawPatch(cx, 180, 0, hu_font[c])
        cx += w
    }
}

//
// F_DrawPatchFlipped
// (vanilla v_video.c V_DrawPatchFlipped; local because the cast drawer is the
//  only caller. Masks a column based masked pic to the screen.
//  Flips horizontally, e.g. to mirror face.)
//
private fun DoomEngineCore.F_DrawPatchFlipped(x0: Int, y0: Int, scrn: Int, patch: ByteArray) {
    val y = y0 - patchTopOffset(patch)
    val x = x0 - patchLeftOffset(patch)
    val w = patchWidth(patch)

    if (x < 0 || x + w > SCREENWIDTH || y < 0 ||
        y + patchHeight(patch) > SCREENHEIGHT || scrn > 4
    ) {
        println("Patch origin $x,$y exceeds LFB")
        I_Error("Bad V_DrawPatch in V_DrawPatchFlipped")
    }

    if (scrn == 0)
        V_MarkRect(x, y, w, patchHeight(patch))

    val dest = screens[scrn]
    val desttop = y * SCREENWIDTH + x

    for (col in 0 until w) {
        var ofs = patchColumnOfs(patch, w - 1 - col)

        // step through the posts in a column
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

//
// F_CastDrawer
//
internal fun DoomEngineCore.F_CastDrawer() {
    // erase the entire screen to a background
    V_DrawPatch(0, 0, 0, W_CacheLumpName("BOSSBACK"))

    F_CastPrint(castorder[castnum].name!!)

    // draw the current frame in the middle of the screen
    val sprdef = sprites[caststate.sprite]
    val sprframe = sprdef.spriteframes[caststate.frame and FF_FRAMEMASK]!!
    val lump = sprframe.lump[0]
    val flip = sprframe.flip[0] != 0

    val patch = W_CacheLumpNum(lump + firstspritelump)
    if (flip)
        F_DrawPatchFlipped(160, 170, 0, patch)
    else
        V_DrawPatch(160, 170, 0, patch)
}

//
// F_DrawPatchCol
//
internal fun DoomEngineCore.F_DrawPatchCol(x: Int, patch: ByteArray, col: Int) {
    var ofs = patchColumnOfs(patch, col) // column = patch + columnofs[col]
    val dest = screens[0]
    val desttop = x

    // step through the posts in a column
    while (patch.u8(ofs) != 0xff) { // column->topdelta != 0xff
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

internal fun DoomEngineCore.F_BunnyScroll() {
    val p1 = W_CacheLumpName("PFUB2")
    val p2 = W_CacheLumpName("PFUB1")

    V_MarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT)

    var scrolled = 320 - (finalecount - 230) / 2
    if (scrolled > 320)
        scrolled = 320
    if (scrolled < 0)
        scrolled = 0

    for (x in 0 until SCREENWIDTH) {
        if (x + scrolled < 320)
            F_DrawPatchCol(x, p1, x + scrolled)
        else
            F_DrawPatchCol(x, p2, x + scrolled - 320)
    }

    if (finalecount < 1130)
        return
    if (finalecount < 1180) {
        V_DrawPatch(
            (SCREENWIDTH - 13 * 8) / 2,
            (SCREENHEIGHT - 8 * 8) / 2, 0, W_CacheLumpName("END0")
        )
        laststage = 0
        return
    }

    var stage = (finalecount - 1180) / 5
    if (stage > 6)
        stage = 6
    if (stage > laststage) {
        S_StartSound(null, sfx_pistol)
        laststage = stage
    }

    val name = "END$stage"
    V_DrawPatch((SCREENWIDTH - 13 * 8) / 2, (SCREENHEIGHT - 8 * 8) / 2, 0, W_CacheLumpName(name))
}

//
// F_Drawer
//
internal fun DoomEngineCore.F_Drawer() {
    if (finalestage == 2) {
        F_CastDrawer()
        return
    }

    if (finalestage == 0)
        F_TextWrite()
    else {
        when (gameepisode) {
            1 ->
                if (gamemode == retail)
                    V_DrawPatch(0, 0, 0, W_CacheLumpName("CREDIT"))
                else
                    V_DrawPatch(0, 0, 0, W_CacheLumpName("HELP2"))
            2 ->
                V_DrawPatch(0, 0, 0, W_CacheLumpName("VICTORY2"))
            3 ->
                F_BunnyScroll()
            4 ->
                V_DrawPatch(0, 0, 0, W_CacheLumpName("ENDPIC"))
        }
    }
}
