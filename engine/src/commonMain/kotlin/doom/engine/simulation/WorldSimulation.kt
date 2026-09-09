// World update order adapted from linuxdoom-1.10 p_tick.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

/** Gameplay operations needed for one world tic, separate from timing and list ownership. */
internal interface WorldSimulation {
    val paused: Boolean
    val pausedByMenu: Boolean
    fun updatePlayers()
    fun updateSpecials()
    fun respawnSpecials()
}
