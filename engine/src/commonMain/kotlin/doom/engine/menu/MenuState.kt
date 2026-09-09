
package doom.engine.menu

import doom.engine.audio.SFX_BOSCUB
import doom.engine.audio.SFX_BSPACT
import doom.engine.audio.SFX_DMPAIN
import doom.engine.audio.SFX_GETPOW
import doom.engine.audio.SFX_KNTDTH
import doom.engine.audio.SFX_PLDETH
import doom.engine.audio.SFX_POPAIN
import doom.engine.audio.SFX_POSIT1
import doom.engine.audio.SFX_POSIT3
import doom.engine.audio.SFX_SGTATK
import doom.engine.audio.SFX_SKESWG
import doom.engine.audio.SFX_SLOP
import doom.engine.audio.SFX_TELEPT
import doom.engine.audio.SFX_VILACT
import doom.engine.resources.GAMMALVL0
import doom.engine.resources.GAMMALVL1
import doom.engine.resources.GAMMALVL2
import doom.engine.resources.GAMMALVL3
import doom.engine.resources.GAMMALVL4

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

    lateinit var currentMenu: MenuDefinition

    val mainMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "M_NGAME", { argument0 -> commands.execute(MenuCommand.NEW_GAME, argument0) }, 'n'.code),
        MenuItem(1, "M_OPTION", { argument0 -> commands.execute(MenuCommand.OPTIONS, argument0) }, 'o'.code),
        MenuItem(1, "M_LOADG", { argument0 -> commands.execute(MenuCommand.LOAD_GAME, argument0) }, 'l'.code),
        MenuItem(1, "M_SAVEG", { argument0 -> commands.execute(MenuCommand.SAVE_GAME, argument0) }, 's'.code),
        MenuItem(1, "M_RDTHIS", { argument0 -> commands.execute(MenuCommand.HELP, argument0) }, 'r'.code),
        MenuItem(1, "M_QUITG", { argument0 -> commands.execute(MenuCommand.QUIT, argument0) }, 'q'.code),
    ) }

    val mainDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        MAIN_END,
        null,
        mainMenu,
        { renderer.draw(MenuPage.MAIN) },
        97, 64,
        0,
    ) }

    val episodeMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "M_EPI1", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 'k'.code),
        MenuItem(1, "M_EPI2", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 't'.code),
        MenuItem(1, "M_EPI3", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 'i'.code),
        MenuItem(1, "M_EPI4", { argument0 -> commands.execute(MenuCommand.SELECT_EPISODE, argument0) }, 't'.code),
    ) }

    val epiDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        EP_END,
        mainDef,
        episodeMenu,
        { renderer.draw(MenuPage.EPISODES) },
        48, 63,
        EP1,
    ) }

    val newGameMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "M_JKILL", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'i'.code),
        MenuItem(1, "M_ROUGH", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'h'.code),
        MenuItem(1, "M_HURT", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'h'.code),
        MenuItem(1, "M_ULTRA", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'u'.code),
        MenuItem(1, "M_NMARE", { argument0 -> commands.execute(MenuCommand.SELECT_SKILL, argument0) }, 'n'.code),
    ) }

    val newDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        NEWG_END,
        epiDef,
        newGameMenu,
        { renderer.draw(MenuPage.SKILL) },
        48, 63,
        HURTME,
    ) }

    val optionsMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "M_ENDGAM", { argument0 -> commands.execute(MenuCommand.END_GAME, argument0) }, 'e'.code),
        MenuItem(1, "M_MESSG", { argument0 -> commands.execute(MenuCommand.TOGGLE_MESSAGES, argument0) }, 'm'.code),
        MenuItem(1, "M_DETAIL", { argument0 -> commands.execute(MenuCommand.TOGGLE_DETAIL, argument0) }, 'g'.code),
        MenuItem(2, "M_SCRNSZ", { argument0 -> commands.execute(MenuCommand.RESIZE_VIEW, argument0) }, 's'.code),
        MenuItem(-1, "", null),
        MenuItem(2, "M_MSENS", { argument0 -> commands.execute(MenuCommand.ADJUST_SENSITIVITY, argument0) }, 'm'.code),
        MenuItem(-1, "", null),
        MenuItem(1, "M_SVOL", { argument0 -> commands.execute(MenuCommand.SOUND, argument0) }, 's'.code),
    ) }

    val optionsDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        OPT_END,
        mainDef,
        optionsMenu,
        { renderer.draw(MenuPage.OPTIONS) },
        60, 37,
        0,
    ) }

    val readMenu1: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.HELP_NEXT, argument0) }, 0),
    ) }

    val readDef1: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        READ1_END,
        mainDef,
        readMenu1,
        { renderer.draw(MenuPage.HELP_FIRST) },
        280, 185,
        0,
    ) }

    val readMenu2: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.HELP_CLOSE, argument0) }, 0),
    ) }

    val readDef2: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        READ2_END,
        readDef1,
        readMenu2,
        { renderer.draw(MenuPage.HELP_SECOND) },
        330, 175,
        0,
    ) }

    val soundMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(2, "M_SFXVOL", { argument0 -> commands.execute(MenuCommand.ADJUST_SFX_VOLUME, argument0) }, 's'.code),
        MenuItem(-1, "", null),
        MenuItem(2, "M_MUSVOL", { argument0 -> commands.execute(MenuCommand.ADJUST_MUSIC_VOLUME, argument0) }, 'm'.code),
        MenuItem(-1, "", null),
    ) }

    val soundDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        SOUND_END,
        optionsDef,
        soundMenu,
        { renderer.draw(MenuPage.SOUND) },
        80, 64,
        0,
    ) }

    val loadMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '1'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '2'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '3'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '4'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '5'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_LOAD_SLOT, argument0) }, '6'.code),
    ) }

    val loadDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        LOAD_END,
        mainDef,
        loadMenu,
        { renderer.draw(MenuPage.LOAD) },
        80, 54,
        0,
    ) }

    val saveMenu: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '1'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '2'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '3'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '4'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '5'.code),
        MenuItem(1, "", { argument0 -> commands.execute(MenuCommand.SELECT_SAVE_SLOT, argument0) }, '6'.code),
    ) }

    val saveDef: MenuDefinition by lazy(LazyThreadSafetyMode.NONE) { MenuDefinition(
        LOAD_END,
        mainDef,
        saveMenu,
        { renderer.draw(MenuPage.SAVE) },
        80, 54,
        0,
    ) }

    var tempstring = ""

    var epi = 0

    val detailNames: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf("M_GDHIGH", "M_GDLOW") }

    val msgNames: Array<String> by lazy(LazyThreadSafetyMode.NONE) { arrayOf("M_MSGOFF", "M_MSGON") }

    val quitsounds: IntArray by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        SFX_PLDETH,
        SFX_DMPAIN,
        SFX_POPAIN,
        SFX_SLOP,
        SFX_TELEPT,
        SFX_POSIT1,
        SFX_POSIT3,
        SFX_SGTATK,
    ) }

    val quitsounds2: IntArray by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        SFX_VILACT,
        SFX_GETPOW,
        SFX_BOSCUB,
        SFX_SLOP,
        SFX_SKESWG,
        SFX_KNTDTH,
        SFX_BSPACT,
        SFX_SGTATK,
    ) }

    var joywait = 0

    var mousewait = 0

    var menuMousey = 0

    var lasty = 0

    var menuMousex = 0

    var lastx = 0
}
