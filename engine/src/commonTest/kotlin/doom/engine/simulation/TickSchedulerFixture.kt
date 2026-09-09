package doom.engine.simulation

import doom.engine.BACKUPTICS
import doom.engine.DoomClock
import doom.engine.ticcmd_t

internal class TickSchedulerFixture(private val sample: (() -> Int)? = null) {
    var now = 0
    var clockSamples = 0
    var collections = 0
    var simulationTic = 0
    private var builtCommands = 0
    private val command = ticcmd_t()
    val executedCommands = mutableListOf<Int>()
    val commandTicsDuringSimulation = mutableListOf<Int>()

    val scheduler: TickScheduler = TickScheduler(
        clock = DoomClock {
            clockSamples++
            sample?.invoke() ?: now
        },
        input = object : TickInput {
            override fun collectEvents() { collections++ }
            override fun buildCommand(command: ticcmd_t) { command.forwardmove = ++builtCommands }
        },
        simulation = object : TickSimulation {
            override val tic: Int get() = simulationTic
            override val playerIndex: Int get() = 0
            override fun advanceTic() {
                scheduler.copyCommand(0, simulationTic % BACKUPTICS, command)
                executedCommands.add(command.forwardmove)
                commandTicsDuringSimulation.add(scheduler.commandTic)
                simulationTic++
            }
        },
    )
}
