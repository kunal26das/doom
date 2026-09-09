package doom.engine

import doom.engine.menu.MenuCommand
import doom.engine.menu.MenuCommandHandler
import doom.engine.menu.MenuPage
import doom.engine.menu.MenuPageRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class MenuStateTest {
    @Test
    fun menuItemsSendTypedChoicesWithoutInvokingTheDrawingCapability() {
        val commands = mutableListOf<Pair<MenuCommand, Int>>()
        val pages = mutableListOf<MenuPage>()
        val menu = MenuState(
            MenuCommandHandler { command, choice -> commands.add(command to choice) },
            MenuPageRenderer { pages.add(it) },
        )
        menu.MainMenu[0].routine!!.invoke(0)
        menu.EpisodeMenu[2].routine!!.invoke(2)
        menu.NewGameMenu[4].routine!!.invoke(4)
        menu.OptionsMenu[3].routine!!.invoke(0)
        menu.OptionsMenu[3].routine!!.invoke(1)
        menu.LoadMenu[5].routine!!.invoke(5)
        menu.SaveMenu[4].routine!!.invoke(4)

        assertEquals(
            listOf(
                MenuCommand.NEW_GAME to 0,
                MenuCommand.SELECT_EPISODE to 2,
                MenuCommand.SELECT_SKILL to 4,
                MenuCommand.RESIZE_VIEW to 0,
                MenuCommand.RESIZE_VIEW to 1,
                MenuCommand.SELECT_LOAD_SLOT to 5,
                MenuCommand.SELECT_SAVE_SLOT to 4,
            ),
            commands,
        )
        assertEquals(emptyList(), pages)
    }

    @Test
    fun pageDescriptorsRequestDrawingWithoutExecutingGameCommands() {
        val pages = mutableListOf<MenuPage>()
        var commandCount = 0
        val menu = MenuState(
            MenuCommandHandler { _, _ -> commandCount++ },
            MenuPageRenderer { pages.add(it) },
        )
        val definitions = listOf(
            menu.MainDef, menu.EpiDef, menu.NewDef, menu.OptionsDef,
            menu.ReadDef1, menu.ReadDef2, menu.SoundDef, menu.LoadDef, menu.SaveDef,
        )
        assertEquals(emptyList(), pages, "Constructing the lazy graph must not draw anything")
        definitions.forEach { it.routine!!.invoke() }
        assertEquals(MenuPage.entries.toList(), pages)
        assertEquals(0, commandCount)
    }

    @Test
    fun menuLinksAndMutableItemStatusBelongToTheirOwnMenuInstance() {
        val commands = MenuCommandHandler { _, _ -> }
        val renderer = MenuPageRenderer { }
        val first = MenuState(commands, renderer)
        val second = MenuState(commands, renderer)
        assertSame(first.MainMenu, first.MainDef.menuitems)
        assertSame(first.MainDef, first.EpiDef.prevMenu)
        assertSame(first.EpiDef, first.NewDef.prevMenu)
        assertSame(first.OptionsDef, first.SoundDef.prevMenu)
        assertSame(first.ReadDef1, first.ReadDef2.prevMenu)
        assertNotSame(first.MainMenu, second.MainMenu)
        assertNotSame(first.MainMenu[0], second.MainMenu[0])
        first.MainMenu[0].status = 0
        assertEquals(1, second.MainMenu[0].status)
    }
}
