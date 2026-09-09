package doom.engine.simulation

import doom.engine.input.TicCommand

internal interface TickInput {
    fun collectEvents()
    fun buildCommand(command: TicCommand)
}
