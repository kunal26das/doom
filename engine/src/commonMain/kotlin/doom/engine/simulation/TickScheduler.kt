package doom.engine.simulation

import doom.engine.DoomClock
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.input.TicCommand

internal class TickScheduler(
    private val clock: DoomClock,
    private val input: TickInput,
    private val simulation: TickSimulation,
) {
    private val commands: Array<Array<TicCommand>> = Array(MAXPLAYERS) { Array(BACKUPTICS) { TicCommand() } }

    var commandTic: Int = 0
        private set
    var ticDuplication: Int = 1
        private set
    private var sampledTime: Int = 0

    fun configureTicDuplication(value: Int) {
        require(value > 0) { "Tic duplication must be positive" }
        ticDuplication = value
    }

    fun copyCommand(playerIndex: Int, slot: Int, destination: TicCommand) {
        destination.copyFrom(commands[playerIndex][slot])
    }

    fun pollInputs() {
        val now = clock.ticks() / ticDuplication
        var remaining = now - sampledTime
        sampledTime = now
        while (remaining > 0) {
            input.collectEvents()
            if (commandTic - simulation.tic / ticDuplication >= BACKUPTICS / 2 - 1) break
            buildCommand()
            commandTic++
            remaining--
        }
    }

    fun advanceDueTics() {
        clock.ticks()
        pollInputs()
        var remaining = minOf(commandTic - simulation.tic, 4)
        while (remaining > 0) {
            simulation.advanceTic()
            remaining--
        }
    }

    fun advanceSingleTic() {
        input.collectEvents()
        buildCommand()
        simulation.advanceTic()
        commandTic++
    }

    private fun buildCommand() {
        input.buildCommand(commands[simulation.playerIndex][commandTic % BACKUPTICS])
    }
}
