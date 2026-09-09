// Single-player scheduling adapted from linuxdoom-1.10 d_net.c and d_main.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

import doom.engine.BACKUPTICS
import doom.engine.DoomClock
import doom.engine.MAXPLAYERS
import doom.engine.ticcmd_t

/** Owns command buffering, elapsed-time polling, and the bounded 35 Hz catch-up policy. */
internal class TickScheduler(
    private val clock: DoomClock,
    private val input: TickInput,
    private val simulation: TickSimulation,
) {
    private val commands: Array<Array<ticcmd_t>> = Array(MAXPLAYERS) { Array(BACKUPTICS) { ticcmd_t() } }

    var commandTic: Int = 0
        private set
    var ticDuplication: Int = 1
        private set
    private var sampledTime: Int = 0

    fun configureTicDuplication(value: Int) {
        require(value > 0) { "Tic duplication must be positive" }
        ticDuplication = value
    }

    /** Simulation receives its own mutable command, without access to the buffered instance. */
    fun copyCommand(playerIndex: Int, slot: Int, destination: ticcmd_t) {
        destination.copyFrom(commands[playerIndex][slot])
    }

    /** Polling between render passes keeps input responsive without advancing the world. */
    fun pollInputs() {
        val now = clock.ticks() / ticDuplication
        var remaining = now - sampledTime
        sampledTime = now
        while (remaining > 0) {
            // The original loop drains input even when the command backlog is already full.
            input.collectEvents()
            if (commandTic - simulation.tic / ticDuplication >= BACKUPTICS / 2 - 1) break
            buildCommand()
            commandTic++
            remaining--
        }
    }

    fun advanceDueTics() {
        // Preserve the original frame-entry sample before the separate input-poll sample.
        // Its historical elapsed-time diagnostic did not influence the catch-up count.
        clock.ticks()
        pollInputs()
        var remaining = minOf(commandTic - simulation.tic, 4)
        while (remaining > 0) {
            simulation.advanceTic()
            remaining--
        }
    }

    /** Deterministic tooling mode advances exactly once regardless of the wall clock. */
    fun advanceSingleTic() {
        input.collectEvents()
        buildCommand()
        simulation.advanceTic()
        // Single-tic mode historically increments maketic after advancing the simulation.
        commandTic++
    }

    private fun buildCommand() {
        input.buildCommand(commands[simulation.playerIndex][commandTic % BACKUPTICS])
    }
}
