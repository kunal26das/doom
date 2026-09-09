package doom.engine.simulation

import doom.engine.input.TicCommand

import kotlin.test.Test
import kotlin.test.assertEquals

class TickSchedulerTest {
    @Test
    fun catchUpCapsWorldUpdatesAndDoesNotReplayDiscardedElapsedTime() {
        val game = TickSchedulerFixture()
        game.now = 20
        game.scheduler.advanceDueTics()
        assertEquals(5, game.scheduler.commandTic, "Only five commands fit ahead of simulation")
        assertEquals(4, game.simulationTic, "One frame may advance at most four tics")
        assertEquals(6, game.collections, "The full-backlog iteration must still drain input")

        game.scheduler.advanceDueTics()
        assertEquals(5, game.simulationTic, "The buffered fifth command remains available")
        assertEquals(6, game.collections, "An unchanged clock does not poll again")

        game.now++
        game.scheduler.advanceDueTics()
        assertEquals(6, game.simulationTic, "Only newly elapsed time produces a command")
        assertEquals((1..6).toList(), game.executedCommands)
    }

    @Test
    fun pollingAFullCommandBacklogStillCollectsKeyReleasesWithoutAdvancingTheWorld() {
        val game = TickSchedulerFixture()
        game.now = 5
        game.scheduler.pollInputs()
        assertEquals(5, game.collections)
        assertEquals(5, game.scheduler.commandTic)
        assertEquals(0, game.simulationTic)

        game.now = 6
        game.scheduler.pollInputs()
        assertEquals(6, game.collections)
        assertEquals(5, game.scheduler.commandTic)
        assertEquals(0, game.simulationTic)
    }

    @Test
    fun changingASimulationCommandDoesNotCorruptTheBufferedInput() {
        val game = TickSchedulerFixture()
        game.now = 1
        game.scheduler.pollInputs()
        val command = TicCommand()
        game.scheduler.copyCommand(0, 0, command)
        command.forwardmove = 999
        game.scheduler.copyCommand(0, 0, command)
        assertEquals(1, command.forwardmove)
    }

    @Test
    fun singleTicModeWrapsTheCommandRingAndAdvancesAfterBuildingInput() {
        val game = TickSchedulerFixture()
        repeat(BACKUPTICS * 3 + 1) { game.scheduler.advanceSingleTic() }
        assertEquals((1..37).toList(), game.executedCommands)
        assertEquals(37, game.collections)
        assertEquals(37, game.scheduler.commandTic)
        assertEquals(37, game.simulationTic)
        assertEquals(0, game.clockSamples, "Single-tic mode must ignore elapsed wall time")
        assertEquals((0..36).toList(), game.commandTicsDuringSimulation)
    }

    @Test
    fun timedFramesKeepTheEntryAndInputClockSamplesInTheirOriginalOrder() {
        val samples = ArrayDeque(listOf(1, 2))
        val game = TickSchedulerFixture { samples.removeFirst() }
        game.scheduler.advanceDueTics()
        assertEquals(2, game.clockSamples)
        assertEquals(2, game.simulationTic, "Commands use the later input clock sample")
    }

    @Test
    fun repeatedAndBackwardClockValuesDoNotCreateSpuriousCommands() {
        val game = TickSchedulerFixture()
        game.scheduler.configureTicDuplication(2)
        game.now = 1
        game.scheduler.pollInputs()
        assertEquals(0, game.collections)
        game.now = 2
        game.scheduler.pollInputs()
        game.scheduler.pollInputs()
        game.now = 0
        game.scheduler.pollInputs()
        assertEquals(1, game.collections)
        assertEquals(1, game.scheduler.commandTic)
    }

}
