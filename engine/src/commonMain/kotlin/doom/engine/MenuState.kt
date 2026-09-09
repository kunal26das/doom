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

/** State owned by one engine; no mutable process-wide storage. */
internal class MenuState(
    private val commands: MenuCommandHandler,
    private val renderer: MenuPageRenderer,
) {
    var mouseSensitivity = 0

    var showMessages = 0

    var detailLevel = 0

    var screenblocks = 0

    var screenSize = 0

    var quickSaveSlot = 0

    var messageToPrint = 0

    var messageString: String? = null

    var messx = 0

    var messy = 0

    var messageLastMenuActive = false

    var messageNeedsInput = false

    var messageRoutine: ((Int) -> Unit)? = null

    val gammamsg: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        GAMMALVL0,
        GAMMALVL1,
        GAMMALVL2,
        GAMMALVL3,
        GAMMALVL4,
    ) }

    var saveStringEnter = 0

    var saveSlot = 0

    var saveCharIndex = 0

    var saveOldString = ""

    var inhelpscreens = false

    var menuactive = false

    val savegamestrings: Array<String> by lazy(LazyThreadSafetyMode.NONE) { Array(10) { "" } }

    var endstring = ""

    var itemOn = 0

    var skullAnimCounter = 0

    var whichSkull = 0

    val skullName: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf("M_SKULL1", "M_SKULL2") }

    lateinit var currentMenu: menu_t

    val MainMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "M_NGAME", { argument0 -> commands.execute(MenuCommand.NEW_GAME, argument0) }, 'n'.code),
        menuitem_t(1, "M_OPTION", { argument0 -> commands.execute(MenuCommand.OPTIONS, argument0) }, 'o'.code),
        menuitem_t(1, "M_LOADG", { argument0 -> commands.execute(MenuCommand.LOAD_GAME, argument0) }, 'l'.code),
        menuitem_t(1, "M_SAVEG", { argument0 -> commands.execute(MenuCommand.SAVE_GAME, argument0) }, 's'.code),
        // Another hickup with Special edition.
        menuitem_t(1, "M_RDTHIS", { argument0 -> commands.execute(MenuCommand.HELP, argument0) }, 'r'.code),
        menuitem_t(1, "M_QUITG", { argument0 -> commands.execute(MenuCommand.QUIT, argument0) }, 'q'.code),
    ) }

    val MainDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        main_end,
        null,
        MainMenu,
        { renderer.draw(MenuPage.MAIN) },
        97, 64,
        0,
    ) }

    val EpisodeMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "M_EPI1", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 'k'.code),
        menuitem_t(1, "M_EPI2", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 't'.code),
        menuitem_t(1, "M_EPI3", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 'i'.code),
        menuitem_t(1, "M_EPI4", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 't'.code),
    ) }

    val EpiDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        ep_end,             // # of menu items
        MainDef,            // previous menu
        EpisodeMenu,        // menuitem_t ->
        { renderer.draw(MenuPage.EPISODES) },    // drawing routine ->
        48, 63,             // x,y
        ep1,                // lastOn
    ) }

    val NewGameMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "M_JKILL", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'i'.code),
        menuitem_t(1, "M_ROUGH", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'h'.code),
        menuitem_t(1, "M_HURT", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'h'.code),
        menuitem_t(1, "M_ULTRA", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'u'.code),
        menuitem_t(1, "M_NMARE", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'n'.code),
    ) }

    val NewDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        newg_end,           // # of menu items
        EpiDef,             // previous menu
        NewGameMenu,        // menuitem_t ->
        { renderer.draw(MenuPage.SKILL) },    // drawing routine ->
        48, 63,             // x,y
        hurtme,             // lastOn
    ) }

    val OptionsMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "M_ENDGAM", { argument0 -> commands.execute(MenuCommand.END_GAME, argument0) }, 'e'.code),
        menuitem_t(1, "M_MESSG", { argument0 -> commands.execute(MenuCommand.TOGGLE_MESSAGES, argument0) }, 'm'.code),
        menuitem_t(1, "M_DETAIL", { argument0 -> commands.execute(MenuCommand.TOGGLE_DETAIL, argument0) }, 'g'.code),
        menuitem_t(2, "M_SCRNSZ", { argument0 -> commands.execute(MenuCommand.RESIZE_VIEW, argument0) }, 's'.code),
        menuitem_t(-1, "", null),
        menuitem_t(2, "M_MSENS", { argument0 -> commands.execute(MenuCommand.ADJUST_SENSITIVITY, argument0) }, 'm'.code),
        menuitem_t(-1, "", null),
        menuitem_t(1, "M_SVOL", { argument0 -> commands.execute(MenuCommand.SOUND, argument0) }, 's'.code),
    ) }

    val OptionsDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        opt_end,
        MainDef,
        OptionsMenu,
        { renderer.draw(MenuPage.OPTIONS) },
        60, 37,
        0,
    ) }

    val ReadMenu1: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.HELP_NEXT, argument0) }, 0),
    ) }

    val ReadDef1: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        read1_end,
        MainDef,
        ReadMenu1,
        { renderer.draw(MenuPage.HELP_FIRST) },
        280, 185,
        0,
    ) }

    val ReadMenu2: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.HELP_CLOSE, argument0) }, 0),
    ) }

    val ReadDef2: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        read2_end,
        ReadDef1,
        ReadMenu2,
        { renderer.draw(MenuPage.HELP_SECOND) },
        330, 175,
        0,
    ) }

    val SoundMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(2, "M_SFXVOL", { argument0 -> commands.execute(MenuCommand.ADJUST_SFX_VOLUME, argument0) }, 's'.code),
        menuitem_t(-1, "", null),
        menuitem_t(2, "M_MUSVOL", { argument0 -> commands.execute(MenuCommand.ADJUST_MUSIC_VOLUME, argument0) }, 'm'.code),
        menuitem_t(-1, "", null),
    ) }

    val SoundDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        sound_end,
        OptionsDef,
        SoundMenu,
        { renderer.draw(MenuPage.SOUND) },
        80, 64,
        0,
    ) }

    val LoadMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '1'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '2'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '3'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '4'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '5'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '6'.code),
    ) }

    val LoadDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        load_end,
        MainDef,
        LoadMenu,
        { renderer.draw(MenuPage.LOAD) },
        80, 54,
        0,
    ) }

    val SaveMenu: Array<menuitem_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '1'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '2'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '3'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '4'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '5'.code),
        menuitem_t(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '6'.code),
    ) }

    val SaveDef: menu_t by lazy(LazyThreadSafetyMode.NONE) { menu_t(
        load_end,
        MainDef,
        SaveMenu,
        { renderer.draw(MenuPage.SAVE) },
        80, 54,
        0,
    ) }

    var tempstring = ""

    var epi = 0

    val detailNames: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf("M_GDHIGH", "M_GDLOW") }

    val msgNames: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf("M_MSGOFF", "M_MSGON") }

    val quitsounds: IntArray by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        sfx_pldeth,
        sfx_dmpain,
        sfx_popain,
        sfx_slop,
        sfx_telept,
        sfx_posit1,
        sfx_posit3,
        sfx_sgtatk,
    ) }

    val quitsounds2: IntArray by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        sfx_vilact,
        sfx_getpow,
        sfx_boscub,
        sfx_slop,
        sfx_skeswg,
        sfx_kntdth,
        sfx_bspact,
        sfx_sgtatk,
    ) }

    var joywait = 0

    var mousewait = 0

    var menu_mousey = 0

    var lasty = 0

    var menu_mousex = 0

    var lastx = 0
}
