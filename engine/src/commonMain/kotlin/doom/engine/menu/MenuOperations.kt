
package doom.engine.menu

import doom.engine.KEY_BACKSPACE
import doom.engine.KEY_DOWNARROW
import doom.engine.KEY_ENTER
import doom.engine.KEY_EQUALS
import doom.engine.KEY_ESCAPE
import doom.engine.KEY_F1
import doom.engine.KEY_F10
import doom.engine.KEY_F11
import doom.engine.KEY_F2
import doom.engine.KEY_F3
import doom.engine.KEY_F4
import doom.engine.KEY_F5
import doom.engine.KEY_F6
import doom.engine.KEY_F7
import doom.engine.KEY_F8
import doom.engine.KEY_F9
import doom.engine.KEY_LEFTARROW
import doom.engine.KEY_MINUS
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_UPARROW
import doom.engine.SCREENWIDTH
import doom.engine.audio.sSetMusicVolume
import doom.engine.audio.sSetSfxVolume
import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_OOF
import doom.engine.audio.SFX_PISTOL
import doom.engine.audio.SFX_PSTOP
import doom.engine.audio.SFX_STNMOV
import doom.engine.audio.SFX_SWTCHN
import doom.engine.audio.SFX_SWTCHX
import doom.engine.audio.sndMusicVolume
import doom.engine.audio.sndSfxVolume
import doom.engine.automap.automapactive
import doom.engine.core.dStartTitle
import doom.engine.core.DoomEngineCore
import doom.engine.core.iGetTime
import doom.engine.core.iQuit
import doom.engine.core.mReadFile
import doom.engine.core.devparm
import doom.engine.gameplay.GS_LEVEL
import doom.engine.gameplay.gDeferedInitNew
import doom.engine.gameplay.gLoadGame
import doom.engine.gameplay.gSaveGame
import doom.engine.gameplay.gScreenShot
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.demoplayback
import doom.engine.gameplay.ENGLISH
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gamestate
import doom.engine.gameplay.gametic
import doom.engine.gameplay.language
import doom.engine.gameplay.netgame
import doom.engine.gameplay.players
import doom.engine.gameplay.REGISTERED
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.usergame
import doom.engine.hud.HU_FONTSIZE
import doom.engine.hud.HU_FONTSTART
import doom.engine.hud.chatOn
import doom.engine.hud.huFont
import doom.engine.hud.messageDontfuckwithme
import doom.engine.input.EngineEvent
import doom.engine.input.EV_JOYSTICK
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_MOUSE
import doom.engine.rendering.iSetPalette
import doom.engine.rendering.rSetViewSize
import doom.engine.rendering.vDrawPatchDirect
import doom.engine.rendering.patchHeight
import doom.engine.rendering.patchWidth
import doom.engine.rendering.usegamma
import doom.engine.resources.DOSY
import doom.engine.resources.EMPTYSTRING
import doom.engine.resources.ENDGAME
import doom.engine.resources.LOADNET
import doom.engine.resources.MSGOFF
import doom.engine.resources.MSGON
import doom.engine.resources.NETEND
import doom.engine.resources.NEWGAME
import doom.engine.resources.NIGHTMARE
import doom.engine.resources.NUM_QUITMESSAGES
import doom.engine.resources.QLOADNET
import doom.engine.resources.QLPROMPT
import doom.engine.resources.QSAVESPOT
import doom.engine.resources.QSPROMPT
import doom.engine.resources.SAVEDEAD
import doom.engine.resources.SAVEGAMENAME
import doom.engine.resources.SWSTRING
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.endmsg
import doom.engine.resources.str

internal var DoomEngineCore.mouseSensitivity
    get() = stateMenu.mouseSensitivity
    set(value) { stateMenu.mouseSensitivity = value }

internal var DoomEngineCore.showMessages
    get() = stateMenu.showMessages
    set(value) { stateMenu.showMessages = value }

internal var DoomEngineCore.detailLevel
    get() = stateMenu.detailLevel
    set(value) { stateMenu.detailLevel = value }
internal var DoomEngineCore.screenblocks
    get() = stateMenu.screenblocks
    set(value) { stateMenu.screenblocks = value }

internal var DoomEngineCore.screenSize
    get() = stateMenu.screenSize
    set(value) { stateMenu.screenSize = value }

internal var DoomEngineCore.quickSaveSlot
    get() = stateMenu.quickSaveSlot
    set(value) { stateMenu.quickSaveSlot = value }

internal var DoomEngineCore.messageToPrint
    get() = stateMenu.messageToPrint
    set(value) { stateMenu.messageToPrint = value }
internal var DoomEngineCore.messageString: String?
    get() = stateMenu.messageString
    set(value) { stateMenu.messageString = value }

internal var DoomEngineCore.messx
    get() = stateMenu.messx
    set(value) { stateMenu.messx = value }
internal var DoomEngineCore.messy
    get() = stateMenu.messy
    set(value) { stateMenu.messy = value }
internal var DoomEngineCore.messageLastMenuActive
    get() = stateMenu.messageLastMenuActive
    set(value) { stateMenu.messageLastMenuActive = value }

internal var DoomEngineCore.messageNeedsInput
    get() = stateMenu.messageNeedsInput
    set(value) { stateMenu.messageNeedsInput = value }

internal var DoomEngineCore.messageRoutine: ((Int) -> Unit)?
    get() = stateMenu.messageRoutine
    set(value) { stateMenu.messageRoutine = value }

private const val SAVESTRINGSIZE = 24

internal val DoomEngineCore.gammamsg: Array<String>
    get() = stateMenu.gammamsg

internal var DoomEngineCore.saveStringEnter
    get() = stateMenu.saveStringEnter
    set(value) { stateMenu.saveStringEnter = value }
internal var DoomEngineCore.saveSlot
    get() = stateMenu.saveSlot
    set(value) { stateMenu.saveSlot = value }
internal var DoomEngineCore.saveCharIndex
    get() = stateMenu.saveCharIndex
    set(value) { stateMenu.saveCharIndex = value }
internal var DoomEngineCore.saveOldString
    get() = stateMenu.saveOldString
    set(value) { stateMenu.saveOldString = value }

internal var DoomEngineCore.inhelpscreens
    get() = stateMenu.inhelpscreens
    set(value) { stateMenu.inhelpscreens = value }
internal var DoomEngineCore.menuactive
    get() = stateMenu.menuactive
    set(value) { stateMenu.menuactive = value }

private const val SKULLXOFF = -32
private const val LINEHEIGHT = 16

internal val DoomEngineCore.savegamestrings: Array<String>
    get() = stateMenu.savegamestrings

internal var DoomEngineCore.endstring
    get() = stateMenu.endstring
    set(value) { stateMenu.endstring = value }

private fun DoomEngineCore.toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

internal var DoomEngineCore.itemOn
    get() = stateMenu.itemOn
    set(value) { stateMenu.itemOn = value }
internal var DoomEngineCore.skullAnimCounter
    get() = stateMenu.skullAnimCounter
    set(value) { stateMenu.skullAnimCounter = value }
internal var DoomEngineCore.whichSkull
    get() = stateMenu.whichSkull
    set(value) { stateMenu.whichSkull = value }

internal val DoomEngineCore.skullName: Array<String>
    get() = stateMenu.skullName

internal var DoomEngineCore.currentMenu: MenuDefinition
    get() = stateMenu.currentMenu
    set(value) { stateMenu.currentMenu = value }

internal const val OPTIONS = 1
internal const val LOADGAME = 2
internal const val SAVEGAME = 3
internal const val READTHIS = 4
internal const val QUITDOOM = 5
internal const val MAIN_END = 6

internal val DoomEngineCore.mainMenu: Array<MenuItem>
    get() = stateMenu.mainMenu

internal val DoomEngineCore.mainDef: MenuDefinition
    get() = stateMenu.mainDef

internal const val EP1 = 0
internal const val EP2 = 1
internal const val EP3 = 2
internal const val EP4 = 3
internal const val EP_END = 4

internal val DoomEngineCore.episodeMenu: Array<MenuItem>
    get() = stateMenu.episodeMenu

internal val DoomEngineCore.epiDef: MenuDefinition
    get() = stateMenu.epiDef

internal const val KILLTHINGS = 0
internal const val TOOROUGH = 1
internal const val HURTME = 2
internal const val VIOLENCE = 3
internal const val NIGHTMARE_ITEM = 4
internal const val NEWG_END = 5

internal val DoomEngineCore.newGameMenu: Array<MenuItem>
    get() = stateMenu.newGameMenu

internal val DoomEngineCore.newDef: MenuDefinition
    get() = stateMenu.newDef

internal const val MESSAGES = 1
internal const val DETAIL = 2
internal const val SCRNSIZE = 3
internal const val OPTION_EMPTY1 = 4
internal const val MOUSESENS = 5
internal const val OPTION_EMPTY2 = 6
internal const val SOUNDVOL = 7
internal const val OPT_END = 8

internal val DoomEngineCore.optionsMenu: Array<MenuItem>
    get() = stateMenu.optionsMenu

internal val DoomEngineCore.optionsDef: MenuDefinition
    get() = stateMenu.optionsDef

internal const val RDTHSEMPTY1 = 0
internal const val READ1_END = 1

internal val DoomEngineCore.readMenu1: Array<MenuItem>
    get() = stateMenu.readMenu1

internal val DoomEngineCore.readDef1: MenuDefinition
    get() = stateMenu.readDef1

internal const val RDTHSEMPTY2 = 0
internal const val READ2_END = 1

internal val DoomEngineCore.readMenu2: Array<MenuItem>
    get() = stateMenu.readMenu2

internal val DoomEngineCore.readDef2: MenuDefinition
    get() = stateMenu.readDef2

internal const val SFX_VOL = 0
internal const val SFX_EMPTY1 = 1
internal const val MUSIC_VOL = 2
internal const val SFX_EMPTY2 = 3
internal const val SOUND_END = 4

internal val DoomEngineCore.soundMenu: Array<MenuItem>
    get() = stateMenu.soundMenu

internal val DoomEngineCore.soundDef: MenuDefinition
    get() = stateMenu.soundDef

internal const val LOAD1 = 0
internal const val LOAD2 = 1
internal const val LOAD3 = 2
internal const val LOAD4 = 3
internal const val LOAD5 = 4
internal const val LOAD6 = 5
internal const val LOAD_END = 6

internal val DoomEngineCore.loadMenu: Array<MenuItem>
    get() = stateMenu.loadMenu

internal val DoomEngineCore.loadDef: MenuDefinition
    get() = stateMenu.loadDef

internal val DoomEngineCore.saveMenu: Array<MenuItem>
    get() = stateMenu.saveMenu

internal val DoomEngineCore.saveDef: MenuDefinition
    get() = stateMenu.saveDef

internal fun DoomEngineCore.mReadSaveStrings() {
    for (i in 0 until LOAD_END) {
        val name = "$SAVEGAMENAME$i.dsg"

        val handle = mReadFile(name)
        if (handle == null) {
            savegamestrings[i] = EMPTYSTRING
            loadMenu[i].status = 0
            continue
        }
        savegamestrings[i] = handle.str(0, minOf(SAVESTRINGSIZE, handle.size))
        loadMenu[i].status = 1
    }
}

internal fun DoomEngineCore.mDrawLoad() {
    vDrawPatchDirect(72, 28, 0, wCacheLumpName("M_LOADG"))
    for (i in 0 until LOAD_END) {
        mDrawSaveLoadBorder(loadDef.x, loadDef.y + LINEHEIGHT * i)
        mWriteText(loadDef.x, loadDef.y + LINEHEIGHT * i, savegamestrings[i])
    }
}

internal fun DoomEngineCore.mDrawSaveLoadBorder(leftX: Int, y: Int) {
    var x = leftX

    vDrawPatchDirect(x - 8, y + 7, 0, wCacheLumpName("M_LSLEFT"))

    for (i in 0 until 24) {
        vDrawPatchDirect(x, y + 7, 0, wCacheLumpName("M_LSCNTR"))
        x += 8
    }

    vDrawPatchDirect(x, y + 7, 0, wCacheLumpName("M_LSRGHT"))
}

internal fun DoomEngineCore.mLoadSelect(choice: Int) {
    val name = "$SAVEGAMENAME$choice.dsg"
    gLoadGame(name)
    mClearMenus()
}

internal fun DoomEngineCore.mLoadGame() {
    if (netgame) {
        mStartMessage(LOADNET, null, false)
        return
    }

    mSetupNextMenu(loadDef)
    mReadSaveStrings()
}

internal fun DoomEngineCore.mDrawSave() {
    vDrawPatchDirect(72, 28, 0, wCacheLumpName("M_SAVEG"))
    for (i in 0 until LOAD_END) {
        mDrawSaveLoadBorder(loadDef.x, loadDef.y + LINEHEIGHT * i)
        mWriteText(loadDef.x, loadDef.y + LINEHEIGHT * i, savegamestrings[i])
    }

    if (saveStringEnter != 0) {
        val i = mStringWidth(savegamestrings[saveSlot])
        mWriteText(loadDef.x + i, loadDef.y + LINEHEIGHT * saveSlot, "_")
    }
}

internal fun DoomEngineCore.mDoSave(slot: Int) {
    gSaveGame(slot, savegamestrings[slot])
    mClearMenus()

    if (quickSaveSlot == -2)
        quickSaveSlot = slot
}

internal fun DoomEngineCore.mSaveSelect(choice: Int) {
    saveStringEnter = 1

    saveSlot = choice
    saveOldString = savegamestrings[choice]
    if (savegamestrings[choice] == EMPTYSTRING || savegamestrings[choice].isEmpty()) {
        savegamestrings[choice] = if (gamemode == COMMERCIAL) {
            "MAP${gamemap.toString().padStart(2, '0')}"
        } else {
            "E${gameepisode}M$gamemap"
        }
    }
    saveCharIndex = savegamestrings[choice].length
}

internal fun DoomEngineCore.mSaveGame() {
    if (!usergame) {
        mStartMessage(SAVEDEAD, null, false)
        return
    }

    if (gamestate != GS_LEVEL)
        return

    mSetupNextMenu(saveDef)
    mReadSaveStrings()
}

internal var DoomEngineCore.tempstring
    get() = stateMenu.tempstring
    set(value) { stateMenu.tempstring = value }

internal fun DoomEngineCore.mQuickSaveResponse(ch: Int) {
    if (ch == 'y'.code) {
        mDoSave(quickSaveSlot)
        sStartSound(null, SFX_SWTCHX)
    }
}

internal fun DoomEngineCore.mQuickSave() {
    if (!usergame) {
        sStartSound(null, SFX_OOF)
        return
    }

    if (gamestate != GS_LEVEL)
        return

    if (quickSaveSlot < 0) {
        mStartControlPanel()
        mReadSaveStrings()
        mSetupNextMenu(saveDef)
        quickSaveSlot = -2
        return
    }
    tempstring = QSPROMPT.replace("%s", savegamestrings[quickSaveSlot])
    mStartMessage(tempstring, { argument0 -> mQuickSaveResponse(argument0) }, true)
}

internal fun DoomEngineCore.mQuickLoadResponse(ch: Int) {
    if (ch == 'y'.code) {
        mLoadSelect(quickSaveSlot)
        sStartSound(null, SFX_SWTCHX)
    }
}

internal fun DoomEngineCore.mQuickLoad() {
    if (netgame) {
        mStartMessage(QLOADNET, null, false)
        return
    }

    if (quickSaveSlot < 0) {
        mStartMessage(QSAVESPOT, null, false)
        return
    }
    tempstring = QLPROMPT.replace("%s", savegamestrings[quickSaveSlot])
    mStartMessage(tempstring, { argument0 -> mQuickLoadResponse(argument0) }, true)
}

internal fun DoomEngineCore.mDrawReadThis1() {
    inhelpscreens = true
    when (gamemode) {
        COMMERCIAL ->
            vDrawPatchDirect(0, 0, 0, wCacheLumpName("HELP"))
        SHAREWARE, REGISTERED, RETAIL ->
            vDrawPatchDirect(0, 0, 0, wCacheLumpName("HELP1"))
        else -> {}
    }
    return
}

internal fun DoomEngineCore.mDrawReadThis2() {
    inhelpscreens = true
    when (gamemode) {
        RETAIL, COMMERCIAL ->
            vDrawPatchDirect(0, 0, 0, wCacheLumpName("CREDIT"))
        SHAREWARE, REGISTERED ->
            vDrawPatchDirect(0, 0, 0, wCacheLumpName("HELP2"))
        else -> {}
    }
    return
}

internal fun DoomEngineCore.mDrawSound() {
    vDrawPatchDirect(60, 38, 0, wCacheLumpName("M_SVOL"))

    mDrawThermo(soundDef.x, soundDef.y + LINEHEIGHT * (SFX_VOL + 1),
        16, sndSfxVolume)

    mDrawThermo(soundDef.x, soundDef.y + LINEHEIGHT * (MUSIC_VOL + 1),
        16, sndMusicVolume)
}

internal fun DoomEngineCore.mSound() {
    mSetupNextMenu(soundDef)
}

internal fun DoomEngineCore.mSfxVol(choice: Int) {
    when (choice) {
        0 ->
            if (sndSfxVolume != 0)
                sndSfxVolume--
        1 ->
            if (sndSfxVolume < 15)
                sndSfxVolume++
    }

    sSetSfxVolume(sndSfxVolume  )
}

internal fun DoomEngineCore.mMusicVol(choice: Int) {
    when (choice) {
        0 ->
            if (sndMusicVolume != 0)
                sndMusicVolume--
        1 ->
            if (sndMusicVolume < 15)
                sndMusicVolume++
    }

    sSetMusicVolume(sndMusicVolume  )
}

internal fun DoomEngineCore.mDrawMainMenu() {
    vDrawPatchDirect(94, 2, 0, wCacheLumpName("M_DOOM"))
}

internal fun DoomEngineCore.mDrawNewGame() {
    vDrawPatchDirect(96, 14, 0, wCacheLumpName("M_NEWG"))
    vDrawPatchDirect(54, 38, 0, wCacheLumpName("M_SKILL"))
}

internal fun DoomEngineCore.mNewGame() {
    if (netgame && !demoplayback) {
        mStartMessage(NEWGAME, null, false)
        return
    }

    if (gamemode == COMMERCIAL)
        mSetupNextMenu(newDef)
    else
        mSetupNextMenu(epiDef)
}

internal var DoomEngineCore.epi
    get() = stateMenu.epi
    set(value) { stateMenu.epi = value }

internal fun DoomEngineCore.mDrawEpisode() {
    vDrawPatchDirect(54, 38, 0, wCacheLumpName("M_EPISOD"))
}

internal fun DoomEngineCore.mVerifyNightmare(ch: Int) {
    if (ch != 'y'.code)
        return

    gDeferedInitNew(NIGHTMARE_ITEM, epi + 1, 1)
    mClearMenus()
}

internal fun DoomEngineCore.mChooseSkill(choice: Int) {
    if (choice == NIGHTMARE_ITEM) {
        mStartMessage(NIGHTMARE, { argument0 -> mVerifyNightmare(argument0) }, true)
        return
    }

    gDeferedInitNew(choice, epi + 1, 1)
    mClearMenus()
}

internal fun DoomEngineCore.mEpisode(selectedEpisode: Int) {
    var choice = selectedEpisode
    if ((gamemode == SHAREWARE) && choice != 0) {
        mStartMessage(SWSTRING, null, false)
        mSetupNextMenu(readDef1)
        return
    }

    if ((gamemode == REGISTERED) && (choice > 2)) {
        println("M_Episode: 4th episode requires UltimateDOOM")
        choice = 0
    }

    epi = choice
    mSetupNextMenu(newDef)
}

internal val DoomEngineCore.detailNames: Array<String>
    get() = stateMenu.detailNames
internal val DoomEngineCore.msgNames: Array<String>
    get() = stateMenu.msgNames

internal fun DoomEngineCore.mDrawOptions() {
    vDrawPatchDirect(108, 15, 0, wCacheLumpName("M_OPTTTL"))

    vDrawPatchDirect(optionsDef.x + 175, optionsDef.y + LINEHEIGHT * DETAIL, 0,
        wCacheLumpName(detailNames[detailLevel]))

    vDrawPatchDirect(optionsDef.x + 120, optionsDef.y + LINEHEIGHT * MESSAGES, 0,
        wCacheLumpName(msgNames[showMessages]))

    mDrawThermo(optionsDef.x, optionsDef.y + LINEHEIGHT * (MOUSESENS + 1),
        10, mouseSensitivity)

    mDrawThermo(optionsDef.x, optionsDef.y + LINEHEIGHT * (SCRNSIZE + 1),
        9, screenSize)
}

internal fun DoomEngineCore.mOptions() {
    mSetupNextMenu(optionsDef)
}

internal fun DoomEngineCore.mChangeMessages() {
    showMessages = 1 - showMessages

    if (showMessages == 0)
        players[consoleplayer].message = MSGOFF
    else
        players[consoleplayer].message = MSGON

    messageDontfuckwithme = true
}

internal fun DoomEngineCore.mEndGameResponse(ch: Int) {
    if (ch != 'y'.code)
        return

    currentMenu.lastOn = itemOn
    mClearMenus()
    dStartTitle()
}

internal fun DoomEngineCore.mEndGame() {
    if (!usergame) {
        sStartSound(null, SFX_OOF)
        return
    }

    if (netgame) {
        mStartMessage(NETEND, null, false)
        return
    }

    mStartMessage(ENDGAME, { argument0 -> mEndGameResponse(argument0) }, true)
}

internal fun DoomEngineCore.mReadThis() {
    mSetupNextMenu(readDef1)
}

internal fun DoomEngineCore.mReadThis2() {
    mSetupNextMenu(readDef2)
}

internal fun DoomEngineCore.mFinishReadThis() {
    mSetupNextMenu(mainDef)
}

internal val DoomEngineCore.quitsounds: IntArray
    get() = stateMenu.quitsounds

internal val DoomEngineCore.quitsounds2: IntArray
    get() = stateMenu.quitsounds2

internal fun DoomEngineCore.mQuitResponse(ch: Int) {
    if (ch != 'y'.code)
        return
    if (!netgame) {
        if (gamemode == COMMERCIAL)
            sStartSound(null, quitsounds2[(gametic shr 2) and 7])
        else
            sStartSound(null, quitsounds[(gametic shr 2) and 7])
    }
    iQuit()
}

internal fun DoomEngineCore.mQuitDOOM() {
    endstring = if (language != ENGLISH)
        "${endmsg[0]}\n\n$DOSY"
    else
        "${endmsg[(gametic % (NUM_QUITMESSAGES - 2)) + 1]}\n\n$DOSY"

    mStartMessage(endstring, { argument0 -> mQuitResponse(argument0) }, true)
}

internal fun DoomEngineCore.mChangeSensitivity(choice: Int) {
    when (choice) {
        0 ->
            if (mouseSensitivity != 0)
                mouseSensitivity--
        1 ->
            if (mouseSensitivity < 9)
                mouseSensitivity++
    }
}

internal fun DoomEngineCore.mChangeDetail() {
    detailLevel = 1 - detailLevel

    println("M_ChangeDetail: low detail mode n.a.")

    return


}

internal fun DoomEngineCore.mSizeDisplay(choice: Int) {
    when (choice) {
        0 ->
            if (screenSize > 0) {
                screenblocks--
                screenSize--
            }
        1 ->
            if (screenSize < 8) {
                screenblocks++
                screenSize++
            }
    }

    rSetViewSize(screenblocks, detailLevel)
}

internal fun DoomEngineCore.mDrawThermo(x: Int, y: Int, thermWidth: Int, thermDot: Int) {
    var xx = x
    vDrawPatchDirect(xx, y, 0, wCacheLumpName("M_THERML"))
    xx += 8
    for (i in 0 until thermWidth) {
        vDrawPatchDirect(xx, y, 0, wCacheLumpName("M_THERMM"))
        xx += 8
    }
    vDrawPatchDirect(xx, y, 0, wCacheLumpName("M_THERMR"))

    vDrawPatchDirect((x + 8) + thermDot * 8, y,
        0, wCacheLumpName("M_THERMO"))
}

internal fun DoomEngineCore.mDrawEmptyCell(menu: MenuDefinition, item: Int) {
    vDrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        wCacheLumpName("M_CELL1"))
}

internal fun DoomEngineCore.mDrawSelCell(menu: MenuDefinition, item: Int) {
    vDrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        wCacheLumpName("M_CELL2"))
}

internal fun DoomEngineCore.mStartMessage(string: String, routine: ((Int) -> Unit)?, input: Boolean) {
    messageLastMenuActive = menuactive
    messageToPrint = 1
    messageString = string
    messageRoutine = routine
    messageNeedsInput = input
    menuactive = true
    return
}

internal fun DoomEngineCore.mStopMessage() {
    menuactive = messageLastMenuActive
    messageToPrint = 0
}

internal fun DoomEngineCore.mStringWidth(string: String): Int {
    var w = 0

    for (i in 0 until string.length) {
        val c = toupper(string[i].code) - HU_FONTSTART
        if (c < 0 || c >= HU_FONTSIZE)
            w += 4
        else
            w += patchWidth(huFont[c])
    }

    return w
}

internal fun DoomEngineCore.mStringHeight(string: String): Int {
    val height = patchHeight(huFont[0])

    var h = height
    for (i in 0 until string.length)
        if (string[i] == '\n')
            h += height

    return h
}

internal fun DoomEngineCore.mWriteText(x: Int, y: Int, string: String) {
    var cx = x
    var cy = y
    var pos = 0

    while (true) {
        if (pos >= string.length)
            break
        var c = string[pos].code
        pos++
        if (c == '\n'.code) {
            cx = x
            cy += 12
            continue
        }

        c = toupper(c) - HU_FONTSTART
        if (c < 0 || c >= HU_FONTSIZE) {
            cx += 4
            continue
        }

        val w = patchWidth(huFont[c])
        if (cx + w > SCREENWIDTH)
            break
        vDrawPatchDirect(cx, cy, 0, huFont[c])
        cx += w
    }
}


private var DoomEngineCore.joywait
    get() = stateMenu.joywait
    set(value) { stateMenu.joywait = value }
private var DoomEngineCore.mousewait
    get() = stateMenu.mousewait
    set(value) { stateMenu.mousewait = value }
private var DoomEngineCore.menuMousey
    get() = stateMenu.menuMousey
    set(value) { stateMenu.menuMousey = value }
private var DoomEngineCore.lasty
    get() = stateMenu.lasty
    set(value) { stateMenu.lasty = value }
private var DoomEngineCore.menuMousex
    get() = stateMenu.menuMousex
    set(value) { stateMenu.menuMousex = value }
private var DoomEngineCore.lastx
    get() = stateMenu.lastx
    set(value) { stateMenu.lastx = value }

internal fun DoomEngineCore.mResponder(ev: EngineEvent): Boolean {
    var ch = -1

    if (ev.type == EV_JOYSTICK && joywait < iGetTime()) {
        if (ev.data3 == -1) {
            ch = KEY_UPARROW
            joywait = iGetTime() + 5
        } else if (ev.data3 == 1) {
            ch = KEY_DOWNARROW
            joywait = iGetTime() + 5
        }

        if (ev.data2 == -1) {
            ch = KEY_LEFTARROW
            joywait = iGetTime() + 2
        } else if (ev.data2 == 1) {
            ch = KEY_RIGHTARROW
            joywait = iGetTime() + 2
        }

        if (ev.data1 and 1 != 0) {
            ch = KEY_ENTER
            joywait = iGetTime() + 5
        }
        if (ev.data1 and 2 != 0) {
            ch = KEY_BACKSPACE
            joywait = iGetTime() + 5
        }
    } else {
        if (ev.type == EV_MOUSE && mousewait < iGetTime()) {
            menuMousey += ev.data3
            if (menuMousey < lasty - 30) {
                ch = KEY_DOWNARROW
                mousewait = iGetTime() + 5
                lasty -= 30
                menuMousey = lasty
            } else if (menuMousey > lasty + 30) {
                ch = KEY_UPARROW
                mousewait = iGetTime() + 5
                lasty += 30
                menuMousey = lasty
            }

            menuMousex += ev.data2
            if (menuMousex < lastx - 30) {
                ch = KEY_LEFTARROW
                mousewait = iGetTime() + 5
                lastx -= 30
                menuMousex = lastx
            } else if (menuMousex > lastx + 30) {
                ch = KEY_RIGHTARROW
                mousewait = iGetTime() + 5
                lastx += 30
                menuMousex = lastx
            }

            if (ev.data1 and 1 != 0) {
                ch = KEY_ENTER
                mousewait = iGetTime() + 15
            }

            if (ev.data1 and 2 != 0) {
                ch = KEY_BACKSPACE
                mousewait = iGetTime() + 15
            }
        } else if (ev.type == EV_KEYDOWN) {
            ch = ev.data1
        }
    }

    if (ch == -1)
        return false

    if (saveStringEnter != 0) {
        when (ch) {
            KEY_BACKSPACE -> {
                if (saveCharIndex > 0) {
                    saveCharIndex--
                    savegamestrings[saveSlot] =
                        savegamestrings[saveSlot].substring(0, saveCharIndex)
                }
            }

            KEY_ESCAPE -> {
                saveStringEnter = 0
                savegamestrings[saveSlot] = saveOldString
            }

            KEY_ENTER -> {
                saveStringEnter = 0
                if (savegamestrings[saveSlot].isNotEmpty())
                    mDoSave(saveSlot)
            }

            else -> {
                val c = toupper(ch)
                var usable = true
                if (c != 32)
                    if (c - HU_FONTSTART < 0 || c - HU_FONTSTART >= HU_FONTSIZE)
                        usable = false
                if (usable && c >= 32 && c <= 127 &&
                    saveCharIndex < SAVESTRINGSIZE - 1 &&
                    mStringWidth(savegamestrings[saveSlot]) <
                    (SAVESTRINGSIZE - 2) * 8
                ) {
                    savegamestrings[saveSlot] = savegamestrings[saveSlot] + c.toChar()
                    saveCharIndex++
                }
            }
        }
        return true
    }

    if (messageToPrint != 0) {
        if (messageNeedsInput &&
            !(ch == ' '.code || ch == 'n'.code || ch == 'y'.code || ch == KEY_ESCAPE)
        )
            return false

        menuactive = messageLastMenuActive
        messageToPrint = 0
        messageRoutine?.invoke(ch)

        menuactive = false
        sStartSound(null, SFX_SWTCHX)
        return true
    }

    if (devparm && ch == KEY_F1) {
        gScreenShot()
        return true
    }

    if (!menuactive)
        when (ch) {
            KEY_MINUS -> {
                if (automapactive || chatOn)
                    return false
                mSizeDisplay(0)
                sStartSound(null, SFX_STNMOV)
                return true
            }

            KEY_EQUALS -> {
                if (automapactive || chatOn)
                    return false
                mSizeDisplay(1)
                sStartSound(null, SFX_STNMOV)
                return true
            }

            KEY_F1 -> {
                mStartControlPanel()

                if (gamemode == RETAIL)
                    currentMenu = readDef2
                else
                    currentMenu = readDef1

                itemOn = 0
                sStartSound(null, SFX_SWTCHN)
                return true
            }

            KEY_F2 -> {
                mStartControlPanel()
                sStartSound(null, SFX_SWTCHN)
                mSaveGame()
                return true
            }

            KEY_F3 -> {
                mStartControlPanel()
                sStartSound(null, SFX_SWTCHN)
                mLoadGame()
                return true
            }

            KEY_F4 -> {
                mStartControlPanel()
                currentMenu = soundDef
                itemOn = SFX_VOL
                sStartSound(null, SFX_SWTCHN)
                return true
            }

            KEY_F5 -> {
                mChangeDetail()
                sStartSound(null, SFX_SWTCHN)
                return true
            }

            KEY_F6 -> {
                sStartSound(null, SFX_SWTCHN)
                mQuickSave()
                return true
            }

            KEY_F7 -> {
                sStartSound(null, SFX_SWTCHN)
                mEndGame()
                return true
            }

            KEY_F8 -> {
                mChangeMessages()
                sStartSound(null, SFX_SWTCHN)
                return true
            }

            KEY_F9 -> {
                sStartSound(null, SFX_SWTCHN)
                mQuickLoad()
                return true
            }

            KEY_F10 -> {
                sStartSound(null, SFX_SWTCHN)
                mQuitDOOM()
                return true
            }

            KEY_F11 -> {
                usegamma++
                if (usegamma > 4)
                    usegamma = 0
                players[consoleplayer].message = gammamsg[usegamma]
                iSetPalette(wCacheLumpName("PLAYPAL"))
                return true
            }
        }

    if (!menuactive) {
        if (ch == KEY_ESCAPE) {
            mStartControlPanel()
            sStartSound(null, SFX_SWTCHN)
            return true
        }
        return false
    }

    when (ch) {
        KEY_DOWNARROW -> {
            do {
                if (itemOn + 1 > currentMenu.numitems - 1)
                    itemOn = 0
                else itemOn++
                sStartSound(null, SFX_PSTOP)
            } while (currentMenu.menuitems[itemOn].status == -1)
            return true
        }

        KEY_UPARROW -> {
            do {
                if (itemOn == 0)
                    itemOn = currentMenu.numitems - 1
                else itemOn--
                sStartSound(null, SFX_PSTOP)
            } while (currentMenu.menuitems[itemOn].status == -1)
            return true
        }

        KEY_LEFTARROW -> {
            if (currentMenu.menuitems[itemOn].routine != null &&
                currentMenu.menuitems[itemOn].status == 2
            ) {
                sStartSound(null, SFX_STNMOV)
                currentMenu.menuitems[itemOn].routine!!(0)
            }
            return true
        }

        KEY_RIGHTARROW -> {
            if (currentMenu.menuitems[itemOn].routine != null &&
                currentMenu.menuitems[itemOn].status == 2
            ) {
                sStartSound(null, SFX_STNMOV)
                currentMenu.menuitems[itemOn].routine!!(1)
            }
            return true
        }

        KEY_ENTER -> {
            if (currentMenu.menuitems[itemOn].routine != null &&
                currentMenu.menuitems[itemOn].status != 0
            ) {
                currentMenu.lastOn = itemOn
                if (currentMenu.menuitems[itemOn].status == 2) {
                    currentMenu.menuitems[itemOn].routine!!(1)
                    sStartSound(null, SFX_STNMOV)
                } else {
                    currentMenu.menuitems[itemOn].routine!!(itemOn)
                    sStartSound(null, SFX_PISTOL)
                }
            }
            return true
        }

        KEY_ESCAPE -> {
            currentMenu.lastOn = itemOn
            mClearMenus()
            sStartSound(null, SFX_SWTCHX)
            return true
        }

        KEY_BACKSPACE -> {
            currentMenu.lastOn = itemOn
            if (currentMenu.prevMenu != null) {
                currentMenu = currentMenu.prevMenu!!
                itemOn = currentMenu.lastOn
                sStartSound(null, SFX_SWTCHN)
            }
            return true
        }

        else -> {
            for (i in itemOn + 1 until currentMenu.numitems)
                if (currentMenu.menuitems[i].alphaKey == ch) {
                    itemOn = i
                    sStartSound(null, SFX_PSTOP)
                    return true
                }
            for (i in 0..itemOn)
                if (currentMenu.menuitems[i].alphaKey == ch) {
                    itemOn = i
                    sStartSound(null, SFX_PSTOP)
                    return true
                }
        }
    }

    return false
}

internal fun DoomEngineCore.mStartControlPanel() {
    if (menuactive)
        return

    menuactive = true
    currentMenu = mainDef
    itemOn = currentMenu.lastOn
}

internal fun DoomEngineCore.mDrawer() {
    var x: Int
    var y: Int

    inhelpscreens = false

    if (messageToPrint != 0) {
        val ms = messageString!!
        var start = 0
        y = 100 - mStringHeight(ms) / 2
        while (start < ms.length) {
            var i = 0
            var string = ""
            val len = ms.length - start
            while (i < len) {
                if (ms[start + i] == '\n') {
                    string = ms.substring(start, start + i)
                    start += i + 1
                    break
                }
                i++
            }

            if (i == ms.length - start) {
                string = ms.substring(start)
                start += i
            }

            x = 160 - mStringWidth(string) / 2
            mWriteText(x, y, string)
            y += patchHeight(huFont[0])
        }
        return
    }

    if (!menuactive)
        return

    currentMenu.routine?.invoke()

    x = currentMenu.x
    y = currentMenu.y
    val max = currentMenu.numitems

    for (i in 0 until max) {
        if (currentMenu.menuitems[i].name.isNotEmpty())
            vDrawPatchDirect(x, y, 0,
                wCacheLumpName(currentMenu.menuitems[i].name))
        y += LINEHEIGHT
    }

    vDrawPatchDirect(x + SKULLXOFF, currentMenu.y - 5 + itemOn * LINEHEIGHT, 0,
        wCacheLumpName(skullName[whichSkull]))
}

internal fun DoomEngineCore.mClearMenus() {
    menuactive = false
}

internal fun DoomEngineCore.mSetupNextMenu(menudef: MenuDefinition) {
    currentMenu = menudef
    itemOn = currentMenu.lastOn
}

internal fun DoomEngineCore.mTicker() {
    if (--skullAnimCounter <= 0) {
        whichSkull = whichSkull xor 1
        skullAnimCounter = 8
    }
}

internal fun DoomEngineCore.mInit() {
    currentMenu = mainDef
    menuactive = false
    itemOn = currentMenu.lastOn
    whichSkull = 0
    skullAnimCounter = 10
    screenSize = screenblocks - 3
    messageToPrint = 0
    messageString = null
    messageLastMenuActive = menuactive
    quickSaveSlot = -1


    when (gamemode) {
        COMMERCIAL -> {
            mainMenu[READTHIS] = MenuItem(
                mainMenu[QUITDOOM].status,
                mainMenu[QUITDOOM].name,
                mainMenu[QUITDOOM].routine,
                mainMenu[QUITDOOM].alphaKey,
            )
            mainDef.numitems--
            mainDef.y += 8
            newDef.prevMenu = mainDef
            readDef1.routine = { mDrawReadThis1() }
            readDef1.x = 330
            readDef1.y = 165
            readMenu1[0].routine = { mFinishReadThis() }
        }
        SHAREWARE,
        REGISTERED -> {
            epiDef.numitems--
        }
        RETAIL -> {
        }
        else -> {}
    }
}

internal fun createMenuState(core: DoomEngineCore): MenuState = MenuState(
    commands = MenuCommandHandler { command, choice ->
        with(core) {
            when (command) {
                MenuCommand.NEW_GAME -> mNewGame()
                MenuCommand.OPTIONS -> mOptions()
                MenuCommand.LOAD_GAME -> mLoadGame()
                MenuCommand.SAVE_GAME -> mSaveGame()
                MenuCommand.HELP -> mReadThis()
                MenuCommand.QUIT -> mQuitDOOM()
                MenuCommand.SELECT_EPISODE -> mEpisode(choice)
                MenuCommand.SELECT_SKILL -> mChooseSkill(choice)
                MenuCommand.END_GAME -> mEndGame()
                MenuCommand.TOGGLE_MESSAGES -> mChangeMessages()
                MenuCommand.TOGGLE_DETAIL -> mChangeDetail()
                MenuCommand.RESIZE_VIEW -> mSizeDisplay(choice)
                MenuCommand.ADJUST_SENSITIVITY -> mChangeSensitivity(choice)
                MenuCommand.SOUND -> mSound()
                MenuCommand.HELP_NEXT -> mReadThis2()
                MenuCommand.HELP_CLOSE -> mFinishReadThis()
                MenuCommand.ADJUST_SFX_VOLUME -> mSfxVol(choice)
                MenuCommand.ADJUST_MUSIC_VOLUME -> mMusicVol(choice)
                MenuCommand.SELECT_LOAD_SLOT -> mLoadSelect(choice)
                MenuCommand.SELECT_SAVE_SLOT -> mSaveSelect(choice)
            }
        }
    },
    renderer = MenuPageRenderer { page ->
        with(core) {
            when (page) {
                MenuPage.MAIN -> mDrawMainMenu()
                MenuPage.EPISODES -> mDrawEpisode()
                MenuPage.SKILL -> mDrawNewGame()
                MenuPage.OPTIONS -> mDrawOptions()
                MenuPage.HELP_FIRST -> mDrawReadThis1()
                MenuPage.HELP_SECOND -> mDrawReadThis2()
                MenuPage.SOUND -> mDrawSound()
                MenuPage.LOAD -> mDrawLoad()
                MenuPage.SAVE -> mDrawSave()
            }
        }
    },
)
