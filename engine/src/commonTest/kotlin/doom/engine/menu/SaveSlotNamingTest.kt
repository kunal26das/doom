package doom.engine.menu

import doom.engine.KEY_BACKSPACE
import doom.engine.KEY_ENTER
import doom.engine.KEY_ESCAPE
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.savedescription
import doom.engine.gameplay.savegameslot
import doom.engine.gameplay.sendsave
import doom.engine.gameplay.SHAREWARE
import doom.engine.hud.huFont
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.resources.EMPTYSTRING
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveSlotNamingTest {
    @Test
    fun emptyEpisodeSlotCanBeConfirmedWithoutTyping() {
        val engine = DoomEngineCore().apply {
            gamemode = SHAREWARE
            gameepisode = 1
            gamemap = 1
            savegamestrings[2] = EMPTYSTRING
            mSaveSelect(2)
        }

        assertEquals("E1M1", engine.savegamestrings[2])
        assertEquals(4, engine.saveCharIndex)
        assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, KEY_ENTER)))
        assertTrue(engine.sendsave)
        assertEquals(2, engine.savegameslot)
        assertEquals("E1M1", engine.savedescription)
        assertEquals(0, engine.saveStringEnter)
    }

    @Test
    fun emptyCommercialSlotsUsePaddedMapNumbers() {
        for ((map, name) in listOf(1 to "MAP01", 32 to "MAP32")) {
            val engine = DoomEngineCore().apply {
                gamemode = COMMERCIAL
                gamemap = map
                mSaveSelect(0)
            }

            assertEquals(name, engine.savegamestrings[0])
            assertEquals(name.length, engine.saveCharIndex)
            assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, KEY_ENTER)))
            assertTrue(engine.sendsave)
            assertEquals(name, engine.savedescription)
        }
    }

    @Test
    fun existingSlotNameIsPreservedAndCancelRestoresItsEditedText() {
        val engine = DoomEngineCore().apply {
            savegamestrings[1] = "WEST CORRIDOR"
            mSaveSelect(1)
        }

        assertEquals("WEST CORRIDOR", engine.savegamestrings[1])
        assertEquals(13, engine.saveCharIndex)
        assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, KEY_BACKSPACE)))
        assertEquals("WEST CORRIDO", engine.savegamestrings[1])

        assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, KEY_ESCAPE)))

        assertEquals("WEST CORRIDOR", engine.savegamestrings[1])
        assertEquals(0, engine.saveStringEnter)
        assertFalse(engine.sendsave)
    }

    @Test
    fun generatedNamesRemainEditableAndCancelRestoresTheOriginalEmptyValue() {
        for (original in listOf(EMPTYSTRING, "")) {
            val engine = DoomEngineCore().apply {
                gamemode = RETAIL
                gameepisode = 4
                gamemap = 7
                for (index in huFont.indices) huFont[index] = byteArrayOf(8, 0, 8, 0, 0, 0, 0, 0)
                savegamestrings[0] = original
                mSaveSelect(0)
            }

            assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, 'a'.code)))
            assertEquals("E4M7A", engine.savegamestrings[0])
            assertEquals(5, engine.saveCharIndex)

            assertTrue(engine.mResponder(EngineEvent(EV_KEYDOWN, KEY_ESCAPE)))

            assertEquals(original, engine.savegamestrings[0])
            assertEquals(0, engine.saveStringEnter)
            assertFalse(engine.sendsave)
        }
    }
}
