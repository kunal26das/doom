package doom.engine.menu

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
        menu.mainMenu[0].routine!!.invoke(0)
        menu.episodeMenu[2].routine!!.invoke(2)
        menu.newGameMenu[4].routine!!.invoke(4)
        menu.optionsMenu[3].routine!!.invoke(0)
        menu.optionsMenu[3].routine!!.invoke(1)
        menu.loadMenu[5].routine!!.invoke(5)
        menu.saveMenu[4].routine!!.invoke(4)

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
            menu.mainDef, menu.epiDef, menu.newDef, menu.optionsDef,
            menu.readDef1, menu.readDef2, menu.soundDef, menu.loadDef, menu.saveDef,
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
        assertSame(first.mainMenu, first.mainDef.menuitems)
        assertSame(first.mainDef, first.epiDef.prevMenu)
        assertSame(first.epiDef, first.newDef.prevMenu)
        assertSame(first.optionsDef, first.soundDef.prevMenu)
        assertSame(first.readDef1, first.readDef2.prevMenu)
        assertNotSame(first.mainMenu, second.mainMenu)
        assertNotSame(first.mainMenu[0], second.mainMenu[0])
        first.mainMenu[0].status = 0
        assertEquals(1, second.mainMenu[0].status)
    }
}
