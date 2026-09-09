
package doom.engine.simulation

import doom.engine.DoomClock
import doom.engine.core.dDoAdvanceDemo
import doom.engine.core.dProcessEvents
import doom.engine.core.DoomEngineCore
import doom.engine.core.iGetTime
import doom.engine.core.advancedemo
import doom.engine.gameplay.gBuildTiccmd
import doom.engine.gameplay.gTicker
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.gametic
import doom.engine.input.TicCommand
import doom.engine.menu.mTicker
import doom.engine.rendering.iStartTic

internal const val MAXNETNODES = 8
internal const val BACKUPTICS = 12

internal val DoomEngineCore.maketic get() = tickScheduler.commandTic
internal var DoomEngineCore.ticdup
    get() = tickScheduler.ticDuplication
    set(value) { tickScheduler.configureTicDuplication(value) }

internal fun DoomEngineCore.netUpdate() = tickScheduler.pollInputs()
internal fun DoomEngineCore.tryRunTics() = tickScheduler.advanceDueTics()

internal fun DoomEngineCore.dQuitNetGame() {}

internal fun createTickScheduler(core: DoomEngineCore): TickScheduler = TickScheduler(
    clock = DoomClock { core.iGetTime() },
    input = object : TickInput {
        override fun collectEvents() = with(core) {
            iStartTic()
            dProcessEvents()
        }

        override fun buildCommand(command: TicCommand) = core.gBuildTiccmd(command)
    },
    simulation = object : TickSimulation {
        override val tic: Int get() = core.gametic
        override val playerIndex: Int get() = core.consoleplayer

        override fun advanceTic() {
            with(core) {
                if (advancedemo) dDoAdvanceDemo()
                mTicker()
                gTicker()
                gametic++
            }
        }
    },
)
