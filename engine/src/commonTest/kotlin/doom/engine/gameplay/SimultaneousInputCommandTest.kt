package doom.engine.gameplay

import doom.engine.DoomKeyInput
import doom.engine.KEY_RCTRL
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_UPARROW
import doom.engine.core.DoomEngineCore
import doom.engine.core.LegacyEngineRuntime
import doom.engine.core.dProcessEvents
import doom.engine.input.BT_ATTACK
import doom.engine.input.TicCommand
import doom.engine.rendering.iStartTic
import kotlin.test.Test
import kotlin.test.assertEquals

class SimultaneousInputCommandTest {
    @Test
    fun forwardTurningAndFireRemainIndependentAcrossReleases() = with(DoomEngineCore()) {
        val runtime = LegacyEngineRuntime(this)
        val command = TicCommand()

        runtime.post(DoomKeyInput(KEY_UPARROW, true))
        runtime.post(DoomKeyInput(KEY_RIGHTARROW, true))
        runtime.post(DoomKeyInput(KEY_RCTRL, true))
        collectCommand(command)

        assertEquals(25, command.forwardmove)
        assertEquals(-320, command.angleturn)
        assertEquals(0, command.sidemove)
        assertEquals(BT_ATTACK, command.buttons)

        runtime.post(DoomKeyInput(KEY_RCTRL, false))
        collectCommand(command)

        assertEquals(25, command.forwardmove)
        assertEquals(-320, command.angleturn)
        assertEquals(0, command.sidemove)
        assertEquals(0, command.buttons)

        runtime.post(DoomKeyInput(KEY_UPARROW, false))
        runtime.post(DoomKeyInput(KEY_RIGHTARROW, false))
        collectCommand(command)

        assertEquals(0, command.forwardmove)
        assertEquals(0, command.angleturn)
        assertEquals(0, command.sidemove)
        assertEquals(0, command.buttons)
        runtime.close()
    }

    private fun DoomEngineCore.collectCommand(command: TicCommand) {
        iStartTic()
        dProcessEvents()
        gBuildTiccmd(command)
    }
}
