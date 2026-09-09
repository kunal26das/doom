// Port of linuxdoom-1.10 m_menu.c -- DOOM selection menu, options, episode etc.
// Sliders and icons. Kinda widget stuff.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.menu.MenuCommand
import doom.engine.menu.MenuCommandHandler
import doom.engine.menu.MenuPage
import doom.engine.menu.MenuPageRenderer

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

// C-locale toupper (ASCII a-z only, exactly like the DOS/linux original).
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

internal var DoomEngineCore.currentMenu: menu_t
    get() = stateMenu.currentMenu
    set(value) { stateMenu.currentMenu = value }

//
// DOOM MENU
//
// main_e
internal const val newgame = 0
internal const val options = 1
internal const val loadgame = 2
internal const val savegame = 3
internal const val readthis = 4
internal const val quitdoom = 5
internal const val main_end = 6

internal val DoomEngineCore.MainMenu: Array<menuitem_t>
    get() = stateMenu.MainMenu

internal val DoomEngineCore.MainDef: menu_t
    get() = stateMenu.MainDef

//
// EPISODE SELECT
//
// episodes_e
internal const val ep1 = 0
internal const val ep2 = 1
internal const val ep3 = 2
internal const val ep4 = 3
internal const val ep_end = 4

internal val DoomEngineCore.EpisodeMenu: Array<menuitem_t>
    get() = stateMenu.EpisodeMenu

internal val DoomEngineCore.EpiDef: menu_t
    get() = stateMenu.EpiDef

//
// NEW GAME
//
// newgame_e
internal const val killthings = 0
internal const val toorough = 1
internal const val hurtme = 2
internal const val violence = 3
internal const val nightmare = 4
internal const val newg_end = 5

internal val DoomEngineCore.NewGameMenu: Array<menuitem_t>
    get() = stateMenu.NewGameMenu

internal val DoomEngineCore.NewDef: menu_t
    get() = stateMenu.NewDef

//
// OPTIONS MENU
//
// options_e
internal const val endgame = 0
internal const val messages = 1
internal const val detail = 2
internal const val scrnsize = 3
internal const val option_empty1 = 4
internal const val mousesens = 5
internal const val option_empty2 = 6
internal const val soundvol = 7
internal const val opt_end = 8

internal val DoomEngineCore.OptionsMenu: Array<menuitem_t>
    get() = stateMenu.OptionsMenu

internal val DoomEngineCore.OptionsDef: menu_t
    get() = stateMenu.OptionsDef

//
// Read This! MENU 1 & 2
//
// read_e
internal const val rdthsempty1 = 0
internal const val read1_end = 1

internal val DoomEngineCore.ReadMenu1: Array<menuitem_t>
    get() = stateMenu.ReadMenu1

internal val DoomEngineCore.ReadDef1: menu_t
    get() = stateMenu.ReadDef1

// read_e2
internal const val rdthsempty2 = 0
internal const val read2_end = 1

internal val DoomEngineCore.ReadMenu2: Array<menuitem_t>
    get() = stateMenu.ReadMenu2

internal val DoomEngineCore.ReadDef2: menu_t
    get() = stateMenu.ReadDef2

//
// SOUND VOLUME MENU
//
// sound_e
internal const val sfx_vol = 0
internal const val sfx_empty1 = 1
internal const val music_vol = 2
internal const val sfx_empty2 = 3
internal const val sound_end = 4

internal val DoomEngineCore.SoundMenu: Array<menuitem_t>
    get() = stateMenu.SoundMenu

internal val DoomEngineCore.SoundDef: menu_t
    get() = stateMenu.SoundDef

//
// LOAD GAME MENU
//
// load_e
internal const val load1 = 0
internal const val load2 = 1
internal const val load3 = 2
internal const val load4 = 3
internal const val load5 = 4
internal const val load6 = 5
internal const val load_end = 6

internal val DoomEngineCore.LoadMenu: Array<menuitem_t>
    get() = stateMenu.LoadMenu

internal val DoomEngineCore.LoadDef: menu_t
    get() = stateMenu.LoadDef

internal val DoomEngineCore.SaveMenu: Array<menuitem_t>
    get() = stateMenu.SaveMenu

internal val DoomEngineCore.SaveDef: menu_t
    get() = stateMenu.SaveDef

//
// M_ReadSaveStrings
//  read the strings from the savegame files
//
internal fun DoomEngineCore.M_ReadSaveStrings() {
    for (i in 0 until load_end) {
        // (the DOS -cdrom "c:\doomdata\" path is not applicable here)
        val name = "$SAVEGAMENAME$i.dsg"

        val handle = M_ReadFile(name)
        if (handle == null) {
            savegamestrings[i] = EMPTYSTRING
            LoadMenu[i].status = 0
            continue
        }
        // (C read() reads at most SAVESTRINGSIZE bytes; short files read less)
        savegamestrings[i] = handle.str(0, minOf(SAVESTRINGSIZE, handle.size))
        LoadMenu[i].status = 1
    }
}

//
// M_LoadGame & Cie.
//
internal fun DoomEngineCore.M_DrawLoad() {
    V_DrawPatchDirect(72, 28, 0, W_CacheLumpName("M_LOADG"))
    for (i in 0 until load_end) {
        M_DrawSaveLoadBorder(LoadDef.x, LoadDef.y + LINEHEIGHT * i)
        M_WriteText(LoadDef.x, LoadDef.y + LINEHEIGHT * i, savegamestrings[i])
    }
}

//
// Draw border for the savegame description
//
internal fun DoomEngineCore.M_DrawSaveLoadBorder(x: Int, y: Int) {
    var x = x

    V_DrawPatchDirect(x - 8, y + 7, 0, W_CacheLumpName("M_LSLEFT"))

    for (i in 0 until 24) {
        V_DrawPatchDirect(x, y + 7, 0, W_CacheLumpName("M_LSCNTR"))
        x += 8
    }

    V_DrawPatchDirect(x, y + 7, 0, W_CacheLumpName("M_LSRGHT"))
}

//
// User wants to load this game
//
internal fun DoomEngineCore.M_LoadSelect(choice: Int) {
    // (the DOS -cdrom "c:\doomdata\" path is not applicable here)
    val name = "$SAVEGAMENAME$choice.dsg"
    G_LoadGame(name)
    M_ClearMenus()
}

//
// Selected from DOOM menu
//
internal fun DoomEngineCore.M_LoadGame(choice: Int) {
    if (netgame) {
        M_StartMessage(LOADNET, null, false)
        return
    }

    M_SetupNextMenu(LoadDef)
    M_ReadSaveStrings()
}

//
//  M_SaveGame & Cie.
//
internal fun DoomEngineCore.M_DrawSave() {
    V_DrawPatchDirect(72, 28, 0, W_CacheLumpName("M_SAVEG"))
    for (i in 0 until load_end) {
        M_DrawSaveLoadBorder(LoadDef.x, LoadDef.y + LINEHEIGHT * i)
        M_WriteText(LoadDef.x, LoadDef.y + LINEHEIGHT * i, savegamestrings[i])
    }

    if (saveStringEnter != 0) {
        val i = M_StringWidth(savegamestrings[saveSlot])
        M_WriteText(LoadDef.x + i, LoadDef.y + LINEHEIGHT * saveSlot, "_")
    }
}

//
// M_Responder calls this when user is finished
//
internal fun DoomEngineCore.M_DoSave(slot: Int) {
    G_SaveGame(slot, savegamestrings[slot])
    M_ClearMenus()

    // PICK QUICKSAVE SLOT YET?
    if (quickSaveSlot == -2)
        quickSaveSlot = slot
}

//
// User wants to save. Start string input for M_Responder
//
internal fun DoomEngineCore.M_SaveSelect(choice: Int) {
    // we are going to be intercepting all chars
    saveStringEnter = 1

    saveSlot = choice
    saveOldString = savegamestrings[choice]
    if (savegamestrings[choice] == EMPTYSTRING)
        savegamestrings[choice] = ""
    saveCharIndex = savegamestrings[choice].length
}

//
// Selected from DOOM menu
//
internal fun DoomEngineCore.M_SaveGame(choice: Int) {
    if (!usergame) {
        M_StartMessage(SAVEDEAD, null, false)
        return
    }

    if (gamestate != GS_LEVEL)
        return

    M_SetupNextMenu(SaveDef)
    M_ReadSaveStrings()
}

internal var DoomEngineCore.tempstring
    get() = stateMenu.tempstring
    set(value) { stateMenu.tempstring = value }

internal fun DoomEngineCore.M_QuickSaveResponse(ch: Int) {
    if (ch == 'y'.code) {
        M_DoSave(quickSaveSlot)
        S_StartSound(null, sfx_swtchx)
    }
}

internal fun DoomEngineCore.M_QuickSave() {
    if (!usergame) {
        S_StartSound(null, sfx_oof)
        return
    }

    if (gamestate != GS_LEVEL)
        return

    if (quickSaveSlot < 0) {
        M_StartControlPanel()
        M_ReadSaveStrings()
        M_SetupNextMenu(SaveDef)
        quickSaveSlot = -2 // means to pick a slot now
        return
    }
    tempstring = QSPROMPT.replace("%s", savegamestrings[quickSaveSlot])
    M_StartMessage(tempstring, { argument0 -> M_QuickSaveResponse(argument0) }, true)
}

//
// M_QuickLoad
//
internal fun DoomEngineCore.M_QuickLoadResponse(ch: Int) {
    if (ch == 'y'.code) {
        M_LoadSelect(quickSaveSlot)
        S_StartSound(null, sfx_swtchx)
    }
}

internal fun DoomEngineCore.M_QuickLoad() {
    if (netgame) {
        M_StartMessage(QLOADNET, null, false)
        return
    }

    if (quickSaveSlot < 0) {
        M_StartMessage(QSAVESPOT, null, false)
        return
    }
    tempstring = QLPROMPT.replace("%s", savegamestrings[quickSaveSlot])
    M_StartMessage(tempstring, { argument0 -> M_QuickLoadResponse(argument0) }, true)
}

//
// Read This Menus
// Had a "quick hack to fix romero bug"
//
internal fun DoomEngineCore.M_DrawReadThis1() {
    inhelpscreens = true
    when (gamemode) {
        commercial ->
            V_DrawPatchDirect(0, 0, 0, W_CacheLumpName("HELP"))
        shareware, registered, retail ->
            V_DrawPatchDirect(0, 0, 0, W_CacheLumpName("HELP1"))
        else -> {}
    }
    return
}

//
// Read This Menus - optional second page.
//
internal fun DoomEngineCore.M_DrawReadThis2() {
    inhelpscreens = true
    when (gamemode) {
        retail, commercial ->
            // This hack keeps us from having to change menus.
            V_DrawPatchDirect(0, 0, 0, W_CacheLumpName("CREDIT"))
        shareware, registered ->
            V_DrawPatchDirect(0, 0, 0, W_CacheLumpName("HELP2"))
        else -> {}
    }
    return
}

//
// Change Sfx & Music volumes
//
internal fun DoomEngineCore.M_DrawSound() {
    V_DrawPatchDirect(60, 38, 0, W_CacheLumpName("M_SVOL"))

    M_DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (sfx_vol + 1),
        16, snd_SfxVolume)

    M_DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (music_vol + 1),
        16, snd_MusicVolume)
}

internal fun DoomEngineCore.M_Sound(choice: Int) {
    M_SetupNextMenu(SoundDef)
}

internal fun DoomEngineCore.M_SfxVol(choice: Int) {
    when (choice) {
        0 ->
            if (snd_SfxVolume != 0)
                snd_SfxVolume--
        1 ->
            if (snd_SfxVolume < 15)
                snd_SfxVolume++
    }

    S_SetSfxVolume(snd_SfxVolume /* *8 */)
}

internal fun DoomEngineCore.M_MusicVol(choice: Int) {
    when (choice) {
        0 ->
            if (snd_MusicVolume != 0)
                snd_MusicVolume--
        1 ->
            if (snd_MusicVolume < 15)
                snd_MusicVolume++
    }

    S_SetMusicVolume(snd_MusicVolume /* *8 */)
}

//
// M_DrawMainMenu
//
internal fun DoomEngineCore.M_DrawMainMenu() {
    V_DrawPatchDirect(94, 2, 0, W_CacheLumpName("M_DOOM"))
}

//
// M_NewGame
//
internal fun DoomEngineCore.M_DrawNewGame() {
    V_DrawPatchDirect(96, 14, 0, W_CacheLumpName("M_NEWG"))
    V_DrawPatchDirect(54, 38, 0, W_CacheLumpName("M_SKILL"))
}

internal fun DoomEngineCore.M_NewGame(choice: Int) {
    if (netgame && !demoplayback) {
        M_StartMessage(NEWGAME, null, false)
        return
    }

    if (gamemode == commercial)
        M_SetupNextMenu(NewDef)
    else
        M_SetupNextMenu(EpiDef)
}

internal var DoomEngineCore.epi
    get() = stateMenu.epi
    set(value) { stateMenu.epi = value }

internal fun DoomEngineCore.M_DrawEpisode() {
    V_DrawPatchDirect(54, 38, 0, W_CacheLumpName("M_EPISOD"))
}

internal fun DoomEngineCore.M_VerifyNightmare(ch: Int) {
    if (ch != 'y'.code)
        return

    G_DeferedInitNew(nightmare, epi + 1, 1)
    M_ClearMenus()
}

internal fun DoomEngineCore.M_ChooseSkill(choice: Int) {
    if (choice == nightmare) {
        M_StartMessage(NIGHTMARE, { argument0 -> M_VerifyNightmare(argument0) }, true)
        return
    }

    G_DeferedInitNew(choice, epi + 1, 1)
    M_ClearMenus()
}

internal fun DoomEngineCore.M_Episode(choice: Int) {
    var choice = choice
    if ((gamemode == shareware) && choice != 0) {
        M_StartMessage(SWSTRING, null, false)
        M_SetupNextMenu(ReadDef1)
        return
    }

    // Yet another hack...
    if ((gamemode == registered) && (choice > 2)) {
        println("M_Episode: 4th episode requires UltimateDOOM")
        choice = 0
    }

    epi = choice
    M_SetupNextMenu(NewDef)
}

internal val DoomEngineCore.detailNames: Array<String>
    get() = stateMenu.detailNames
internal val DoomEngineCore.msgNames: Array<String>
    get() = stateMenu.msgNames

internal fun DoomEngineCore.M_DrawOptions() {
    V_DrawPatchDirect(108, 15, 0, W_CacheLumpName("M_OPTTTL"))

    V_DrawPatchDirect(OptionsDef.x + 175, OptionsDef.y + LINEHEIGHT * detail, 0,
        W_CacheLumpName(detailNames[detailLevel]))

    V_DrawPatchDirect(OptionsDef.x + 120, OptionsDef.y + LINEHEIGHT * messages, 0,
        W_CacheLumpName(msgNames[showMessages]))

    M_DrawThermo(OptionsDef.x, OptionsDef.y + LINEHEIGHT * (mousesens + 1),
        10, mouseSensitivity)

    M_DrawThermo(OptionsDef.x, OptionsDef.y + LINEHEIGHT * (scrnsize + 1),
        9, screenSize)
}

internal fun DoomEngineCore.M_Options(choice: Int) {
    M_SetupNextMenu(OptionsDef)
}

//
//      Toggle messages on/off
//
internal fun DoomEngineCore.M_ChangeMessages(choice: Int) {
    // warning: unused parameter `int choice'
    showMessages = 1 - showMessages

    if (showMessages == 0)
        players[consoleplayer].message = MSGOFF
    else
        players[consoleplayer].message = MSGON

    message_dontfuckwithme = true
}

//
// M_EndGame
//
internal fun DoomEngineCore.M_EndGameResponse(ch: Int) {
    if (ch != 'y'.code)
        return

    currentMenu.lastOn = itemOn
    M_ClearMenus()
    D_StartTitle()
}

internal fun DoomEngineCore.M_EndGame(choice: Int) {
    if (!usergame) {
        S_StartSound(null, sfx_oof)
        return
    }

    if (netgame) {
        M_StartMessage(NETEND, null, false)
        return
    }

    M_StartMessage(ENDGAME, { argument0 -> M_EndGameResponse(argument0) }, true)
}

//
// M_ReadThis
//
internal fun DoomEngineCore.M_ReadThis(choice: Int) {
    M_SetupNextMenu(ReadDef1)
}

internal fun DoomEngineCore.M_ReadThis2(choice: Int) {
    M_SetupNextMenu(ReadDef2)
}

internal fun DoomEngineCore.M_FinishReadThis(choice: Int) {
    M_SetupNextMenu(MainDef)
}

internal val DoomEngineCore.quitsounds: IntArray
    get() = stateMenu.quitsounds

internal val DoomEngineCore.quitsounds2: IntArray
    get() = stateMenu.quitsounds2

internal fun DoomEngineCore.M_QuitResponse(ch: Int) {
    if (ch != 'y'.code)
        return
    if (!netgame) {
        if (gamemode == commercial)
            S_StartSound(null, quitsounds2[(gametic shr 2) and 7])
        else
            S_StartSound(null, quitsounds[(gametic shr 2) and 7])
        I_WaitVBL(105)
    }
    I_Quit()
}

internal fun DoomEngineCore.M_QuitDOOM(choice: Int) {
    // We pick index 0 which is language sensitive,
    //  or one at random, between 1 and maximum number.
    endstring = if (language != english)
        "${endmsg[0]}\n\n$DOSY"
    else
        "${endmsg[(gametic % (NUM_QUITMESSAGES - 2)) + 1]}\n\n$DOSY"

    M_StartMessage(endstring, { argument0 -> M_QuitResponse(argument0) }, true)
}

internal fun DoomEngineCore.M_ChangeSensitivity(choice: Int) {
    when (choice) {
        0 ->
            if (mouseSensitivity != 0)
                mouseSensitivity--
        1 ->
            if (mouseSensitivity < 9)
                mouseSensitivity++
    }
}

internal fun DoomEngineCore.M_ChangeDetail(choice: Int) {
    detailLevel = 1 - detailLevel

    // FIXME - does not work. Remove anyway?
    println("M_ChangeDetail: low detail mode n.a.")

    return

    /*R_SetViewSize (screenblocks, detailLevel);

    if (!detailLevel)
        players[consoleplayer].message = DETAILHI;
    else
        players[consoleplayer].message = DETAILLO;*/
}

internal fun DoomEngineCore.M_SizeDisplay(choice: Int) {
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

    R_SetViewSize(screenblocks, detailLevel)
}

//
//      Menu Functions
//
internal fun DoomEngineCore.M_DrawThermo(x: Int, y: Int, thermWidth: Int, thermDot: Int) {
    var xx = x
    V_DrawPatchDirect(xx, y, 0, W_CacheLumpName("M_THERML"))
    xx += 8
    for (i in 0 until thermWidth) {
        V_DrawPatchDirect(xx, y, 0, W_CacheLumpName("M_THERMM"))
        xx += 8
    }
    V_DrawPatchDirect(xx, y, 0, W_CacheLumpName("M_THERMR"))

    V_DrawPatchDirect((x + 8) + thermDot * 8, y,
        0, W_CacheLumpName("M_THERMO"))
}

internal fun DoomEngineCore.M_DrawEmptyCell(menu: menu_t, item: Int) {
    V_DrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        W_CacheLumpName("M_CELL1"))
}

internal fun DoomEngineCore.M_DrawSelCell(menu: menu_t, item: Int) {
    V_DrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        W_CacheLumpName("M_CELL2"))
}

internal fun DoomEngineCore.M_StartMessage(string: String, routine: ((Int) -> Unit)?, input: Boolean) {
    messageLastMenuActive = menuactive
    messageToPrint = 1
    messageString = string
    messageRoutine = routine
    messageNeedsInput = input
    menuactive = true
    return
}

internal fun DoomEngineCore.M_StopMessage() {
    menuactive = messageLastMenuActive
    messageToPrint = 0
}

//
// Find string width from hu_font chars
//
internal fun DoomEngineCore.M_StringWidth(string: String): Int {
    var w = 0

    for (i in 0 until string.length) {
        val c = toupper(string[i].code) - HU_FONTSTART
        if (c < 0 || c >= HU_FONTSIZE)
            w += 4
        else
            w += patchWidth(hu_font[c])
    }

    return w
}

//
//      Find string height from hu_font chars
//
internal fun DoomEngineCore.M_StringHeight(string: String): Int {
    val height = patchHeight(hu_font[0])

    var h = height
    for (i in 0 until string.length)
        if (string[i] == '\n')
            h += height

    return h
}

//
//      Write a string using the hu_font
//
internal fun DoomEngineCore.M_WriteText(x: Int, y: Int, string: String) {
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

        val w = patchWidth(hu_font[c])
        if (cx + w > SCREENWIDTH)
            break
        V_DrawPatchDirect(cx, cy, 0, hu_font[c])
        cx += w
    }
}

//
// CONTROL PANEL
//

private var DoomEngineCore.joywait
    get() = stateMenu.joywait
    set(value) { stateMenu.joywait = value }
private var DoomEngineCore.mousewait
    get() = stateMenu.mousewait
    set(value) { stateMenu.mousewait = value }
private var DoomEngineCore.menu_mousey
    get() = stateMenu.menu_mousey
    set(value) { stateMenu.menu_mousey = value }
private var DoomEngineCore.lasty
    get() = stateMenu.lasty
    set(value) { stateMenu.lasty = value }
private var DoomEngineCore.menu_mousex
    get() = stateMenu.menu_mousex
    set(value) { stateMenu.menu_mousex = value }
private var DoomEngineCore.lastx
    get() = stateMenu.lastx
    set(value) { stateMenu.lastx = value }

//
// M_Responder
//
internal fun DoomEngineCore.M_Responder(ev: event_t): Boolean {
    var ch = -1

    if (ev.type == ev_joystick && joywait < I_GetTime()) {
        if (ev.data3 == -1) {
            ch = KEY_UPARROW
            joywait = I_GetTime() + 5
        } else if (ev.data3 == 1) {
            ch = KEY_DOWNARROW
            joywait = I_GetTime() + 5
        }

        if (ev.data2 == -1) {
            ch = KEY_LEFTARROW
            joywait = I_GetTime() + 2
        } else if (ev.data2 == 1) {
            ch = KEY_RIGHTARROW
            joywait = I_GetTime() + 2
        }

        if (ev.data1 and 1 != 0) {
            ch = KEY_ENTER
            joywait = I_GetTime() + 5
        }
        if (ev.data1 and 2 != 0) {
            ch = KEY_BACKSPACE
            joywait = I_GetTime() + 5
        }
    } else {
        if (ev.type == ev_mouse && mousewait < I_GetTime()) {
            menu_mousey += ev.data3
            if (menu_mousey < lasty - 30) {
                ch = KEY_DOWNARROW
                mousewait = I_GetTime() + 5
                lasty -= 30
                menu_mousey = lasty
            } else if (menu_mousey > lasty + 30) {
                ch = KEY_UPARROW
                mousewait = I_GetTime() + 5
                lasty += 30
                menu_mousey = lasty
            }

            menu_mousex += ev.data2
            if (menu_mousex < lastx - 30) {
                ch = KEY_LEFTARROW
                mousewait = I_GetTime() + 5
                lastx -= 30
                menu_mousex = lastx
            } else if (menu_mousex > lastx + 30) {
                ch = KEY_RIGHTARROW
                mousewait = I_GetTime() + 5
                lastx += 30
                menu_mousex = lastx
            }

            if (ev.data1 and 1 != 0) {
                ch = KEY_ENTER
                mousewait = I_GetTime() + 15
            }

            if (ev.data1 and 2 != 0) {
                ch = KEY_BACKSPACE
                mousewait = I_GetTime() + 15
            }
        } else if (ev.type == ev_keydown) {
            ch = ev.data1
        }
    }

    if (ch == -1)
        return false

    // Save Game string input
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
                    M_DoSave(saveSlot)
            }

            else -> {
                val c = toupper(ch)
                var usable = true
                if (c != 32)
                    if (c - HU_FONTSTART < 0 || c - HU_FONTSTART >= HU_FONTSIZE)
                        usable = false
                if (usable && c >= 32 && c <= 127 &&
                    saveCharIndex < SAVESTRINGSIZE - 1 &&
                    M_StringWidth(savegamestrings[saveSlot]) <
                    (SAVESTRINGSIZE - 2) * 8
                ) {
                    savegamestrings[saveSlot] = savegamestrings[saveSlot] + c.toChar()
                    saveCharIndex++
                }
            }
        }
        return true
    }

    // Take care of any messages that need input
    if (messageToPrint != 0) {
        if (messageNeedsInput &&
            !(ch == ' '.code || ch == 'n'.code || ch == 'y'.code || ch == KEY_ESCAPE)
        )
            return false

        menuactive = messageLastMenuActive
        messageToPrint = 0
        messageRoutine?.invoke(ch)

        menuactive = false
        S_StartSound(null, sfx_swtchx)
        return true
    }

    if (devparm && ch == KEY_F1) {
        G_ScreenShot()
        return true
    }

    // F-Keys
    if (!menuactive)
        when (ch) {
            KEY_MINUS -> {          // Screen size down
                if (automapactive || chat_on)
                    return false
                M_SizeDisplay(0)
                S_StartSound(null, sfx_stnmov)
                return true
            }

            KEY_EQUALS -> {         // Screen size up
                if (automapactive || chat_on)
                    return false
                M_SizeDisplay(1)
                S_StartSound(null, sfx_stnmov)
                return true
            }

            KEY_F1 -> {             // Help key
                M_StartControlPanel()

                if (gamemode == retail)
                    currentMenu = ReadDef2
                else
                    currentMenu = ReadDef1

                itemOn = 0
                S_StartSound(null, sfx_swtchn)
                return true
            }

            KEY_F2 -> {             // Save
                M_StartControlPanel()
                S_StartSound(null, sfx_swtchn)
                M_SaveGame(0)
                return true
            }

            KEY_F3 -> {             // Load
                M_StartControlPanel()
                S_StartSound(null, sfx_swtchn)
                M_LoadGame(0)
                return true
            }

            KEY_F4 -> {             // Sound Volume
                M_StartControlPanel()
                currentMenu = SoundDef
                itemOn = sfx_vol
                S_StartSound(null, sfx_swtchn)
                return true
            }

            KEY_F5 -> {             // Detail toggle
                M_ChangeDetail(0)
                S_StartSound(null, sfx_swtchn)
                return true
            }

            KEY_F6 -> {             // Quicksave
                S_StartSound(null, sfx_swtchn)
                M_QuickSave()
                return true
            }

            KEY_F7 -> {             // End game
                S_StartSound(null, sfx_swtchn)
                M_EndGame(0)
                return true
            }

            KEY_F8 -> {             // Toggle messages
                M_ChangeMessages(0)
                S_StartSound(null, sfx_swtchn)
                return true
            }

            KEY_F9 -> {             // Quickload
                S_StartSound(null, sfx_swtchn)
                M_QuickLoad()
                return true
            }

            KEY_F10 -> {            // Quit DOOM
                S_StartSound(null, sfx_swtchn)
                M_QuitDOOM(0)
                return true
            }

            KEY_F11 -> {            // gamma toggle
                usegamma++
                if (usegamma > 4)
                    usegamma = 0
                players[consoleplayer].message = gammamsg[usegamma]
                I_SetPalette(W_CacheLumpName("PLAYPAL"))
                return true
            }
        }

    // Pop-up menu?
    if (!menuactive) {
        if (ch == KEY_ESCAPE) {
            M_StartControlPanel()
            S_StartSound(null, sfx_swtchn)
            return true
        }
        return false
    }

    // Keys usable within menu
    when (ch) {
        KEY_DOWNARROW -> {
            do {
                if (itemOn + 1 > currentMenu.numitems - 1)
                    itemOn = 0
                else itemOn++
                S_StartSound(null, sfx_pstop)
            } while (currentMenu.menuitems[itemOn].status == -1)
            return true
        }

        KEY_UPARROW -> {
            do {
                if (itemOn == 0)
                    itemOn = currentMenu.numitems - 1
                else itemOn--
                S_StartSound(null, sfx_pstop)
            } while (currentMenu.menuitems[itemOn].status == -1)
            return true
        }

        KEY_LEFTARROW -> {
            if (currentMenu.menuitems[itemOn].routine != null &&
                currentMenu.menuitems[itemOn].status == 2
            ) {
                S_StartSound(null, sfx_stnmov)
                currentMenu.menuitems[itemOn].routine!!(0)
            }
            return true
        }

        KEY_RIGHTARROW -> {
            if (currentMenu.menuitems[itemOn].routine != null &&
                currentMenu.menuitems[itemOn].status == 2
            ) {
                S_StartSound(null, sfx_stnmov)
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
                    currentMenu.menuitems[itemOn].routine!!(1)  // right arrow
                    S_StartSound(null, sfx_stnmov)
                } else {
                    currentMenu.menuitems[itemOn].routine!!(itemOn)
                    S_StartSound(null, sfx_pistol)
                }
            }
            return true
        }

        KEY_ESCAPE -> {
            currentMenu.lastOn = itemOn
            M_ClearMenus()
            S_StartSound(null, sfx_swtchx)
            return true
        }

        KEY_BACKSPACE -> {
            currentMenu.lastOn = itemOn
            if (currentMenu.prevMenu != null) {
                currentMenu = currentMenu.prevMenu!!
                itemOn = currentMenu.lastOn
                S_StartSound(null, sfx_swtchn)
            }
            return true
        }

        else -> {
            for (i in itemOn + 1 until currentMenu.numitems)
                if (currentMenu.menuitems[i].alphaKey == ch) {
                    itemOn = i
                    S_StartSound(null, sfx_pstop)
                    return true
                }
            for (i in 0..itemOn)
                if (currentMenu.menuitems[i].alphaKey == ch) {
                    itemOn = i
                    S_StartSound(null, sfx_pstop)
                    return true
                }
        }
    }

    return false
}

//
// M_StartControlPanel
//
internal fun DoomEngineCore.M_StartControlPanel() {
    // intro might call this repeatedly
    if (menuactive)
        return

    menuactive = true
    currentMenu = MainDef           // JDC
    itemOn = currentMenu.lastOn     // JDC
}

//
// M_Drawer
// Called after the view has been rendered,
// but before it has been blitted.
//
internal fun DoomEngineCore.M_Drawer() {
    var x: Int
    var y: Int

    inhelpscreens = false

    // Horiz. & Vertically center string and print it.
    if (messageToPrint != 0) {
        val ms = messageString!!
        var start = 0
        y = 100 - M_StringHeight(ms) / 2
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

            // (vanilla quirk: strlen is re-evaluated against the already
            //  advanced `start`, so a line whose length equals the length of
            //  the remaining text is consumed twice)
            if (i == ms.length - start) {
                string = ms.substring(start)
                start += i
            }

            x = 160 - M_StringWidth(string) / 2
            M_WriteText(x, y, string)
            y += patchHeight(hu_font[0])
        }
        return
    }

    if (!menuactive)
        return

    currentMenu.routine?.invoke()   // call Draw routine

    // DRAW MENU
    x = currentMenu.x
    y = currentMenu.y
    val max = currentMenu.numitems

    for (i in 0 until max) {
        if (currentMenu.menuitems[i].name.isNotEmpty())
            V_DrawPatchDirect(x, y, 0,
                W_CacheLumpName(currentMenu.menuitems[i].name))
        y += LINEHEIGHT
    }

    // DRAW SKULL
    V_DrawPatchDirect(x + SKULLXOFF, currentMenu.y - 5 + itemOn * LINEHEIGHT, 0,
        W_CacheLumpName(skullName[whichSkull]))
}

//
// M_ClearMenus
//
internal fun DoomEngineCore.M_ClearMenus() {
    menuactive = false
    // if (!netgame && usergame && paused)
    //       sendpause = true;
}

//
// M_SetupNextMenu
//
internal fun DoomEngineCore.M_SetupNextMenu(menudef: menu_t) {
    currentMenu = menudef
    itemOn = currentMenu.lastOn
}

//
// M_Ticker
//
internal fun DoomEngineCore.M_Ticker() {
    if (--skullAnimCounter <= 0) {
        whichSkull = whichSkull xor 1
        skullAnimCounter = 8
    }
}

//
// M_Init
//
internal fun DoomEngineCore.M_Init() {
    currentMenu = MainDef
    menuactive = false
    itemOn = currentMenu.lastOn
    whichSkull = 0
    skullAnimCounter = 10
    screenSize = screenblocks - 3
    messageToPrint = 0
    messageString = null
    messageLastMenuActive = menuactive
    quickSaveSlot = -1

    // Here we could catch other version dependencies,
    //  like HELP1/2, and four episodes.

    when (gamemode) {
        commercial -> {
            // This is used because DOOM 2 had only one HELP
            //  page. I use CREDIT as second page now, but
            //  kept this hack for educational purposes.
            // (struct copy in C)
            MainMenu[readthis] = menuitem_t(
                MainMenu[quitdoom].status,
                MainMenu[quitdoom].name,
                MainMenu[quitdoom].routine,
                MainMenu[quitdoom].alphaKey,
            )
            MainDef.numitems--
            MainDef.y += 8
            NewDef.prevMenu = MainDef
            ReadDef1.routine = { M_DrawReadThis1() }
            ReadDef1.x = 330
            ReadDef1.y = 165
            ReadMenu1[0].routine = { argument0 -> M_FinishReadThis(argument0) }
        }
        shareware,
            // Episode 2 and 3 are handled,
            //  branching to an ad screen.
        registered -> {
            // We need to remove the fourth episode.
            EpiDef.numitems--
        }
        retail -> {
            // We are fine.
        }
        else -> {}
    }
}


/** Bind menu intent and rendering capabilities at the legacy composition boundary. */
internal fun createMenuState(core: DoomEngineCore): MenuState = MenuState(
    commands = MenuCommandHandler { command, choice ->
        with(core) {
            when (command) {
                MenuCommand.NEW_GAME -> M_NewGame(choice)
                MenuCommand.OPTIONS -> M_Options(choice)
                MenuCommand.LOAD_GAME -> M_LoadGame(choice)
                MenuCommand.SAVE_GAME -> M_SaveGame(choice)
                MenuCommand.HELP -> M_ReadThis(choice)
                MenuCommand.QUIT -> M_QuitDOOM(choice)
                MenuCommand.SELECT_EPISODE -> M_Episode(choice)
                MenuCommand.SELECT_SKILL -> M_ChooseSkill(choice)
                MenuCommand.END_GAME -> M_EndGame(choice)
                MenuCommand.TOGGLE_MESSAGES -> M_ChangeMessages(choice)
                MenuCommand.TOGGLE_DETAIL -> M_ChangeDetail(choice)
                MenuCommand.RESIZE_VIEW -> M_SizeDisplay(choice)
                MenuCommand.ADJUST_SENSITIVITY -> M_ChangeSensitivity(choice)
                MenuCommand.SOUND -> M_Sound(choice)
                MenuCommand.HELP_NEXT -> M_ReadThis2(choice)
                MenuCommand.HELP_CLOSE -> M_FinishReadThis(choice)
                MenuCommand.ADJUST_SFX_VOLUME -> M_SfxVol(choice)
                MenuCommand.ADJUST_MUSIC_VOLUME -> M_MusicVol(choice)
                MenuCommand.SELECT_LOAD_SLOT -> M_LoadSelect(choice)
                MenuCommand.SELECT_SAVE_SLOT -> M_SaveSelect(choice)
            }
        }
    },
    renderer = MenuPageRenderer { page ->
        with(core) {
            when (page) {
                MenuPage.MAIN -> M_DrawMainMenu()
                MenuPage.EPISODES -> M_DrawEpisode()
                MenuPage.SKILL -> M_DrawNewGame()
                MenuPage.OPTIONS -> M_DrawOptions()
                MenuPage.HELP_FIRST -> M_DrawReadThis1()
                MenuPage.HELP_SECOND -> M_DrawReadThis2()
                MenuPage.SOUND -> M_DrawSound()
                MenuPage.LOAD -> M_DrawLoad()
                MenuPage.SAVE -> M_DrawSave()
            }
        }
    },
)
