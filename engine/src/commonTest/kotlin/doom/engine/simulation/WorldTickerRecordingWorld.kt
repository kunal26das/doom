package doom.engine.simulation

internal class WorldTickerRecordingWorld(private val order: MutableList<String>) : WorldSimulation {
    override var paused = false
    override var pausedByMenu = false
    override fun updatePlayers() { order.add("players") }
    override fun updateSpecials() { order.add("specials") }
    override fun respawnSpecials() { order.add("respawn") }
}
