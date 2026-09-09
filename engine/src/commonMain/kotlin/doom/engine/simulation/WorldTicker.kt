// World update order adapted from linuxdoom-1.10 p_tick.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

/** Owns level elapsed time and the deterministic ordering of world updates. */
internal class WorldTicker(
    private val thinkers: ThinkerScheduler,
    private val world: WorldSimulation,
) {
    var levelTime: Int = 0
        private set

    /** Called when loading a level or restoring its savegame clock. */
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
