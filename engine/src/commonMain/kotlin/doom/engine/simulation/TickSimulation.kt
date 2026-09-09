package doom.engine.simulation

internal interface TickSimulation {
    val tic: Int
    val playerIndex: Int
    fun advanceTic()
}
