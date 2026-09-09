package com.kunal26das.doom.presentation.input

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Protects the independently defined application protocol at the legacy boundary. */
class GameKeyProtocolTest {
    @Test
    fun everyLogicalSpecialKeyMatchesTheLegacyEngineProtocol() {
        val keys = listOf(
            com.kunal26das.doom.domain.KEY_RIGHTARROW to doom.engine.KEY_RIGHTARROW,
            com.kunal26das.doom.domain.KEY_LEFTARROW to doom.engine.KEY_LEFTARROW,
            com.kunal26das.doom.domain.KEY_UPARROW to doom.engine.KEY_UPARROW,
            com.kunal26das.doom.domain.KEY_DOWNARROW to doom.engine.KEY_DOWNARROW,
            com.kunal26das.doom.domain.KEY_ESCAPE to doom.engine.KEY_ESCAPE,
            com.kunal26das.doom.domain.KEY_ENTER to doom.engine.KEY_ENTER,
            com.kunal26das.doom.domain.KEY_TAB to doom.engine.KEY_TAB,
            com.kunal26das.doom.domain.KEY_F1 to doom.engine.KEY_F1,
            com.kunal26das.doom.domain.KEY_F2 to doom.engine.KEY_F2,
            com.kunal26das.doom.domain.KEY_F3 to doom.engine.KEY_F3,
            com.kunal26das.doom.domain.KEY_F4 to doom.engine.KEY_F4,
            com.kunal26das.doom.domain.KEY_F5 to doom.engine.KEY_F5,
            com.kunal26das.doom.domain.KEY_F6 to doom.engine.KEY_F6,
            com.kunal26das.doom.domain.KEY_F7 to doom.engine.KEY_F7,
            com.kunal26das.doom.domain.KEY_F8 to doom.engine.KEY_F8,
            com.kunal26das.doom.domain.KEY_F9 to doom.engine.KEY_F9,
            com.kunal26das.doom.domain.KEY_F10 to doom.engine.KEY_F10,
            com.kunal26das.doom.domain.KEY_F11 to doom.engine.KEY_F11,
            com.kunal26das.doom.domain.KEY_F12 to doom.engine.KEY_F12,
            com.kunal26das.doom.domain.KEY_BACKSPACE to doom.engine.KEY_BACKSPACE,
            com.kunal26das.doom.domain.KEY_PAUSE to doom.engine.KEY_PAUSE,
            com.kunal26das.doom.domain.KEY_EQUALS to doom.engine.KEY_EQUALS,
            com.kunal26das.doom.domain.KEY_MINUS to doom.engine.KEY_MINUS,
            com.kunal26das.doom.domain.KEY_RSHIFT to doom.engine.KEY_RSHIFT,
            com.kunal26das.doom.domain.KEY_RCTRL to doom.engine.KEY_RCTRL,
            com.kunal26das.doom.domain.KEY_RALT to doom.engine.KEY_RALT,
            com.kunal26das.doom.domain.KEY_LALT to doom.engine.KEY_LALT,
        )

        keys.forEach { (application, engine) ->
            assertEquals(engine, application, "The application key must reach the same engine action")
            assertTrue(application in 1..255, "Keys must fit the engine's input table")
        }
    }
}
