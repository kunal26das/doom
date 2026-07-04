// Port of linuxdoom-1.10 m_menu.c -- DOOM selection menu, options, episode etc.
// Sliders and icons. Kinda widget stuff.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// defaulted values
//
var mouseSensitivity = 0       // has default

// Show messages has default, 0 = off, 1 = on
var showMessages = 0

// Blocky mode, has default, 0 = high, 1 = normal
var detailLevel = 0
var screenblocks = 0           // has default

// temp for screenblocks (0-9)
var screenSize = 0

// -1 = no quicksave slot picked!
var quickSaveSlot = 0

// 1 = message to be printed
var messageToPrint = 0
// ...and here is the message string!
var messageString: String? = null

// message x & y
var messx = 0
var messy = 0
var messageLastMenuActive = false

// timed message = no input from user
var messageNeedsInput = false

var messageRoutine: ((Int) -> Unit)? = null

private const val SAVESTRINGSIZE = 24

val gammamsg: Array<String> = arrayOf(
    GAMMALVL0,
    GAMMALVL1,
    GAMMALVL2,
    GAMMALVL3,
    GAMMALVL4,
)

// we are going to be entering a savegame string
var saveStringEnter = 0
var saveSlot = 0        // which slot to save in
var saveCharIndex = 0   // which char we're editing
// old save description before edit
var saveOldString = ""

var inhelpscreens = false
var menuactive = false

private const val SKULLXOFF = -32
private const val LINEHEIGHT = 16

val savegamestrings: Array<String> = Array(10) { "" }

var endstring = ""

// C-locale toupper (ASCII a-z only, exactly like the DOS/linux original).
private fun toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

//
// MENU TYPEDEFS
//
class menuitem_t(
    // 0 = no cursor here, 1 = ok, 2 = arrows ok
    var status: Int,
    var name: String,
    // choice = menu item #.
    // if status = 2,
    //   choice=0:leftarrow,1:rightarrow
    var routine: ((Int) -> Unit)?,
    // hotkey in menu
    var alphaKey: Int = 0,
)

class menu_t(
    var numitems: Int,              // # of menu items
    var prevMenu: menu_t?,          // previous menu
    var menuitems: Array<menuitem_t>, // menu items
    var routine: (() -> Unit)?,     // draw routine
    var x: Int,
    var y: Int,                     // x,y of menu
    var lastOn: Int,                // last item user was on in menu
)

var itemOn = 0            // menu item skull is on
var skullAnimCounter = 0  // skull animation counter
var whichSkull = 0        // which skull to draw

// graphic name of skulls
// warning: initializer-string for array of chars is too long
val skullName: Array<String> = arrayOf("M_SKULL1", "M_SKULL2")

// current menudef
lateinit var currentMenu: menu_t

//
// DOOM MENU
//
// main_e
const val newgame = 0
const val options = 1
const val loadgame = 2
const val savegame = 3
const val readthis = 4
const val quitdoom = 5
const val main_end = 6

val MainMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "M_NGAME", ::M_NewGame, 'n'.code),
    menuitem_t(1, "M_OPTION", ::M_Options, 'o'.code),
    menuitem_t(1, "M_LOADG", ::M_LoadGame, 'l'.code),
    menuitem_t(1, "M_SAVEG", ::M_SaveGame, 's'.code),
    // Another hickup with Special edition.
    menuitem_t(1, "M_RDTHIS", ::M_ReadThis, 'r'.code),
    menuitem_t(1, "M_QUITG", ::M_QuitDOOM, 'q'.code),
)

val MainDef: menu_t = menu_t(
    main_end,
    null,
    MainMenu,
    ::M_DrawMainMenu,
    97, 64,
    0,
)

//
// EPISODE SELECT
//
// episodes_e
const val ep1 = 0
const val ep2 = 1
const val ep3 = 2
const val ep4 = 3
const val ep_end = 4

val EpisodeMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "M_EPI1", ::M_Episode, 'k'.code),
    menuitem_t(1, "M_EPI2", ::M_Episode, 't'.code),
    menuitem_t(1, "M_EPI3", ::M_Episode, 'i'.code),
    menuitem_t(1, "M_EPI4", ::M_Episode, 't'.code),
)

val EpiDef: menu_t = menu_t(
    ep_end,             // # of menu items
    MainDef,            // previous menu
    EpisodeMenu,        // menuitem_t ->
    ::M_DrawEpisode,    // drawing routine ->
    48, 63,             // x,y
    ep1,                // lastOn
)

//
// NEW GAME
//
// newgame_e
const val killthings = 0
const val toorough = 1
const val hurtme = 2
const val violence = 3
const val nightmare = 4
const val newg_end = 5

val NewGameMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "M_JKILL", ::M_ChooseSkill, 'i'.code),
    menuitem_t(1, "M_ROUGH", ::M_ChooseSkill, 'h'.code),
    menuitem_t(1, "M_HURT", ::M_ChooseSkill, 'h'.code),
    menuitem_t(1, "M_ULTRA", ::M_ChooseSkill, 'u'.code),
    menuitem_t(1, "M_NMARE", ::M_ChooseSkill, 'n'.code),
)

val NewDef: menu_t = menu_t(
    newg_end,           // # of menu items
    EpiDef,             // previous menu
    NewGameMenu,        // menuitem_t ->
    ::M_DrawNewGame,    // drawing routine ->
    48, 63,             // x,y
    hurtme,             // lastOn
)

//
// OPTIONS MENU
//
// options_e
const val endgame = 0
const val messages = 1
const val detail = 2
const val scrnsize = 3
const val option_empty1 = 4
const val mousesens = 5
const val option_empty2 = 6
const val soundvol = 7
const val opt_end = 8

val OptionsMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "M_ENDGAM", ::M_EndGame, 'e'.code),
    menuitem_t(1, "M_MESSG", ::M_ChangeMessages, 'm'.code),
    menuitem_t(1, "M_DETAIL", ::M_ChangeDetail, 'g'.code),
    menuitem_t(2, "M_SCRNSZ", ::M_SizeDisplay, 's'.code),
    menuitem_t(-1, "", null),
    menuitem_t(2, "M_MSENS", ::M_ChangeSensitivity, 'm'.code),
    menuitem_t(-1, "", null),
    menuitem_t(1, "M_SVOL", ::M_Sound, 's'.code),
)

val OptionsDef: menu_t = menu_t(
    opt_end,
    MainDef,
    OptionsMenu,
    ::M_DrawOptions,
    60, 37,
    0,
)

//
// Read This! MENU 1 & 2
//
// read_e
const val rdthsempty1 = 0
const val read1_end = 1

val ReadMenu1: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "", ::M_ReadThis2, 0),
)

val ReadDef1: menu_t = menu_t(
    read1_end,
    MainDef,
    ReadMenu1,
    ::M_DrawReadThis1,
    280, 185,
    0,
)

// read_e2
const val rdthsempty2 = 0
const val read2_end = 1

val ReadMenu2: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "", ::M_FinishReadThis, 0),
)

val ReadDef2: menu_t = menu_t(
    read2_end,
    ReadDef1,
    ReadMenu2,
    ::M_DrawReadThis2,
    330, 175,
    0,
)

//
// SOUND VOLUME MENU
//
// sound_e
const val sfx_vol = 0
const val sfx_empty1 = 1
const val music_vol = 2
const val sfx_empty2 = 3
const val sound_end = 4

val SoundMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(2, "M_SFXVOL", ::M_SfxVol, 's'.code),
    menuitem_t(-1, "", null),
    menuitem_t(2, "M_MUSVOL", ::M_MusicVol, 'm'.code),
    menuitem_t(-1, "", null),
)

val SoundDef: menu_t = menu_t(
    sound_end,
    OptionsDef,
    SoundMenu,
    ::M_DrawSound,
    80, 64,
    0,
)

//
// LOAD GAME MENU
//
// load_e
const val load1 = 0
const val load2 = 1
const val load3 = 2
const val load4 = 3
const val load5 = 4
const val load6 = 5
const val load_end = 6

val LoadMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "", ::M_LoadSelect, '1'.code),
    menuitem_t(1, "", ::M_LoadSelect, '2'.code),
    menuitem_t(1, "", ::M_LoadSelect, '3'.code),
    menuitem_t(1, "", ::M_LoadSelect, '4'.code),
    menuitem_t(1, "", ::M_LoadSelect, '5'.code),
    menuitem_t(1, "", ::M_LoadSelect, '6'.code),
)

val LoadDef: menu_t = menu_t(
    load_end,
    MainDef,
    LoadMenu,
    ::M_DrawLoad,
    80, 54,
    0,
)

//
// SAVE GAME MENU
//
val SaveMenu: Array<menuitem_t> = arrayOf(
    menuitem_t(1, "", ::M_SaveSelect, '1'.code),
    menuitem_t(1, "", ::M_SaveSelect, '2'.code),
    menuitem_t(1, "", ::M_SaveSelect, '3'.code),
    menuitem_t(1, "", ::M_SaveSelect, '4'.code),
    menuitem_t(1, "", ::M_SaveSelect, '5'.code),
    menuitem_t(1, "", ::M_SaveSelect, '6'.code),
)

val SaveDef: menu_t = menu_t(
    load_end,
    MainDef,
    SaveMenu,
    ::M_DrawSave,
    80, 54,
    0,
)

//
// M_ReadSaveStrings
//  read the strings from the savegame files
//
fun M_ReadSaveStrings() {
    for (i in 0 until load_end) {
        // (the DOS -cdrom "c:\doomdata\" path is not applicable here)
        val name = "$SAVEGAMENAME$i.dsg"

        val handle = M_ReadFile(name)
        if (handle == null) {
            savegamestrings[i] = EMPTYSTRING
            LoadMenu[i].status = 0
            continue
        }
        savegamestrings[i] = handle.str(0, SAVESTRINGSIZE)
        LoadMenu[i].status = 1
    }
}

//
// M_LoadGame & Cie.
//
fun M_DrawLoad() {
    V_DrawPatchDirect(72, 28, 0, W_CacheLumpName("M_LOADG"))
    for (i in 0 until load_end) {
        M_DrawSaveLoadBorder(LoadDef.x, LoadDef.y + LINEHEIGHT * i)
        M_WriteText(LoadDef.x, LoadDef.y + LINEHEIGHT * i, savegamestrings[i])
    }
}

//
// Draw border for the savegame description
//
fun M_DrawSaveLoadBorder(x: Int, y: Int) {
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
fun M_LoadSelect(choice: Int) {
    // (the DOS -cdrom "c:\doomdata\" path is not applicable here)
    val name = "$SAVEGAMENAME$choice.dsg"
    G_LoadGame(name)
    M_ClearMenus()
}

//
// Selected from DOOM menu
//
fun M_LoadGame(choice: Int) {
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
fun M_DrawSave() {
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
fun M_DoSave(slot: Int) {
    G_SaveGame(slot, savegamestrings[slot])
    M_ClearMenus()

    // PICK QUICKSAVE SLOT YET?
    if (quickSaveSlot == -2)
        quickSaveSlot = slot
}

//
// User wants to save. Start string input for M_Responder
//
fun M_SaveSelect(choice: Int) {
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
fun M_SaveGame(choice: Int) {
    if (!usergame) {
        M_StartMessage(SAVEDEAD, null, false)
        return
    }

    if (gamestate != GS_LEVEL)
        return

    M_SetupNextMenu(SaveDef)
    M_ReadSaveStrings()
}

//
//      M_QuickSave
//
var tempstring = ""

fun M_QuickSaveResponse(ch: Int) {
    if (ch == 'y'.code) {
        M_DoSave(quickSaveSlot)
        S_StartSound(null, sfx_swtchx)
    }
}

fun M_QuickSave() {
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
    M_StartMessage(tempstring, ::M_QuickSaveResponse, true)
}

//
// M_QuickLoad
//
fun M_QuickLoadResponse(ch: Int) {
    if (ch == 'y'.code) {
        M_LoadSelect(quickSaveSlot)
        S_StartSound(null, sfx_swtchx)
    }
}

fun M_QuickLoad() {
    if (netgame) {
        M_StartMessage(QLOADNET, null, false)
        return
    }

    if (quickSaveSlot < 0) {
        M_StartMessage(QSAVESPOT, null, false)
        return
    }
    tempstring = QLPROMPT.replace("%s", savegamestrings[quickSaveSlot])
    M_StartMessage(tempstring, ::M_QuickLoadResponse, true)
}

//
// Read This Menus
// Had a "quick hack to fix romero bug"
//
fun M_DrawReadThis1() {
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
fun M_DrawReadThis2() {
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
fun M_DrawSound() {
    V_DrawPatchDirect(60, 38, 0, W_CacheLumpName("M_SVOL"))

    M_DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (sfx_vol + 1),
        16, snd_SfxVolume)

    M_DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (music_vol + 1),
        16, snd_MusicVolume)
}

fun M_Sound(choice: Int) {
    M_SetupNextMenu(SoundDef)
}

fun M_SfxVol(choice: Int) {
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

fun M_MusicVol(choice: Int) {
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
fun M_DrawMainMenu() {
    V_DrawPatchDirect(94, 2, 0, W_CacheLumpName("M_DOOM"))
}

//
// M_NewGame
//
fun M_DrawNewGame() {
    V_DrawPatchDirect(96, 14, 0, W_CacheLumpName("M_NEWG"))
    V_DrawPatchDirect(54, 38, 0, W_CacheLumpName("M_SKILL"))
}

fun M_NewGame(choice: Int) {
    if (netgame && !demoplayback) {
        M_StartMessage(NEWGAME, null, false)
        return
    }

    if (gamemode == commercial)
        M_SetupNextMenu(NewDef)
    else
        M_SetupNextMenu(EpiDef)
}

//
//      M_Episode
//
var epi = 0

fun M_DrawEpisode() {
    V_DrawPatchDirect(54, 38, 0, W_CacheLumpName("M_EPISOD"))
}

fun M_VerifyNightmare(ch: Int) {
    if (ch != 'y'.code)
        return

    G_DeferedInitNew(nightmare, epi + 1, 1)
    M_ClearMenus()
}

fun M_ChooseSkill(choice: Int) {
    if (choice == nightmare) {
        M_StartMessage(NIGHTMARE, ::M_VerifyNightmare, true)
        return
    }

    G_DeferedInitNew(choice, epi + 1, 1)
    M_ClearMenus()
}

fun M_Episode(choice: Int) {
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

//
// M_Options
//
val detailNames: Array<String> = arrayOf("M_GDHIGH", "M_GDLOW")
val msgNames: Array<String> = arrayOf("M_MSGOFF", "M_MSGON")

fun M_DrawOptions() {
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

fun M_Options(choice: Int) {
    M_SetupNextMenu(OptionsDef)
}

//
//      Toggle messages on/off
//
fun M_ChangeMessages(choice: Int) {
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
fun M_EndGameResponse(ch: Int) {
    if (ch != 'y'.code)
        return

    currentMenu.lastOn = itemOn
    M_ClearMenus()
    D_StartTitle()
}

fun M_EndGame(choice: Int) {
    if (!usergame) {
        S_StartSound(null, sfx_oof)
        return
    }

    if (netgame) {
        M_StartMessage(NETEND, null, false)
        return
    }

    M_StartMessage(ENDGAME, ::M_EndGameResponse, true)
}

//
// M_ReadThis
//
fun M_ReadThis(choice: Int) {
    M_SetupNextMenu(ReadDef1)
}

fun M_ReadThis2(choice: Int) {
    M_SetupNextMenu(ReadDef2)
}

fun M_FinishReadThis(choice: Int) {
    M_SetupNextMenu(MainDef)
}

//
// M_QuitDOOM
//
val quitsounds: IntArray = intArrayOf(
    sfx_pldeth,
    sfx_dmpain,
    sfx_popain,
    sfx_slop,
    sfx_telept,
    sfx_posit1,
    sfx_posit3,
    sfx_sgtatk,
)

val quitsounds2: IntArray = intArrayOf(
    sfx_vilact,
    sfx_getpow,
    sfx_boscub,
    sfx_slop,
    sfx_skeswg,
    sfx_kntdth,
    sfx_bspact,
    sfx_sgtatk,
)

fun M_QuitResponse(ch: Int) {
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

fun M_QuitDOOM(choice: Int) {
    // We pick index 0 which is language sensitive,
    //  or one at random, between 1 and maximum number.
    endstring = if (language != english)
        "${endmsg[0]}\n\n$DOSY"
    else
        "${endmsg[(gametic % (NUM_QUITMESSAGES - 2)) + 1]}\n\n$DOSY"

    M_StartMessage(endstring, ::M_QuitResponse, true)
}

fun M_ChangeSensitivity(choice: Int) {
    when (choice) {
        0 ->
            if (mouseSensitivity != 0)
                mouseSensitivity--
        1 ->
            if (mouseSensitivity < 9)
                mouseSensitivity++
    }
}

fun M_ChangeDetail(choice: Int) {
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

fun M_SizeDisplay(choice: Int) {
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
fun M_DrawThermo(x: Int, y: Int, thermWidth: Int, thermDot: Int) {
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

fun M_DrawEmptyCell(menu: menu_t, item: Int) {
    V_DrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        W_CacheLumpName("M_CELL1"))
}

fun M_DrawSelCell(menu: menu_t, item: Int) {
    V_DrawPatchDirect(menu.x - 10, menu.y + item * LINEHEIGHT - 1, 0,
        W_CacheLumpName("M_CELL2"))
}

fun M_StartMessage(string: String, routine: ((Int) -> Unit)?, input: Boolean) {
    messageLastMenuActive = menuactive
    messageToPrint = 1
    messageString = string
    messageRoutine = routine
    messageNeedsInput = input
    menuactive = true
    return
}

fun M_StopMessage() {
    menuactive = messageLastMenuActive
    messageToPrint = 0
}

//
// Find string width from hu_font chars
//
fun M_StringWidth(string: String): Int {
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
fun M_StringHeight(string: String): Int {
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
fun M_WriteText(x: Int, y: Int, string: String) {
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

// statics from C M_Responder (mousex/mousey renamed: g_game owns globals of
// the same name; the C ones here were function-local statics).
private var joywait = 0
private var mousewait = 0
private var menu_mousey = 0
private var lasty = 0
private var menu_mousex = 0
private var lastx = 0

//
// M_Responder
//
fun M_Responder(ev: event_t): Boolean {
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
fun M_StartControlPanel() {
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
fun M_Drawer() {
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
fun M_ClearMenus() {
    menuactive = false
    // if (!netgame && usergame && paused)
    //       sendpause = true;
}

//
// M_SetupNextMenu
//
fun M_SetupNextMenu(menudef: menu_t) {
    currentMenu = menudef
    itemOn = currentMenu.lastOn
}

//
// M_Ticker
//
fun M_Ticker() {
    if (--skullAnimCounter <= 0) {
        whichSkull = whichSkull xor 1
        skullAnimCounter = 8
    }
}

//
// M_Init
//
fun M_Init() {
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
            ReadDef1.routine = ::M_DrawReadThis1
            ReadDef1.x = 330
            ReadDef1.y = 165
            ReadMenu1[0].routine = ::M_FinishReadThis
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
