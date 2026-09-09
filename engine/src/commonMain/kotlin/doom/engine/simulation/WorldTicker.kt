package doom.engine.simulation


internal class WorldTicker(
    private val thinkers: ThinkerScheduler,
    private val world: WorldSimulation,
) {
    var levelTime: Int = 0
        private set

    fun restoreLevelTime(tics: Int) { levelTime = tics }

    fun tick() {
        if (world.paused || world.pausedByMenu) return
        world.updatePlayers()
        thinkers.run()
        world.updateSpecials()
        world.respawnSpecials()
        levelTime++
    }
}
