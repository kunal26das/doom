package doom.engine.simulation

internal interface WorldSimulation {
    val paused: Boolean
    val pausedByMenu: Boolean
    fun updatePlayers()
    fun updateSpecials()
    fun respawnSpecials()
}
