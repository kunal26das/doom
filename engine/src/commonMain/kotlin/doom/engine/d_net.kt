// Compatibility adapters for the original single-player tic vocabulary.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "ktlint")

package doom.engine

import doom.engine.simulation.TickInput
import doom.engine.simulation.TickScheduler
import doom.engine.simulation.TickSimulation

internal const val MAXNETNODES = 8
internal const val BACKUPTICS = 12

internal val DoomEngineCore.maketic get() = tickScheduler.commandTic
internal var DoomEngineCore.ticdup
    get() = tickScheduler.ticDuplication
    set(value) { tickScheduler.configureTicDuplication(value) }

internal fun DoomEngineCore.NetUpdate() = tickScheduler.pollInputs()
internal fun DoomEngineCore.TryRunTics() = tickScheduler.advanceDueTics()

/** Networking is not implemented; there are no peers to notify on exit. */
internal fun DoomEngineCore.D_QuitNetGame() {}

/** The composition adapter is the only scheduling code allowed to see the complete game. */
internal fun createTickScheduler(core: DoomEngineCore): TickScheduler = TickScheduler(
    clock = DoomClock { core.I_GetTime() },
    input = object : TickInput {
        override fun collectEvents() = with(core) {
            I_StartTic()
            D_ProcessEvents()
        }

        override fun buildCommand(command: ticcmd_t) = core.G_BuildTiccmd(command)
    },
    simulation = object : TickSimulation {
        override val tic: Int get() = core.gametic
        override val playerIndex: Int get() = core.consoleplayer

        override fun advanceTic() {
            with(core) {
                if (advancedemo) D_DoAdvanceDemo()
                M_Ticker()
                G_Ticker()
                gametic++
            }
        }
    },
)
