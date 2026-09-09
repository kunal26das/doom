// Single-player scheduling adapted from linuxdoom-1.10 d_net.c and d_main.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

/** The scheduler controls when to advance; the simulation controls what a tic does. */
internal interface TickSimulation {
    val tic: Int
    val playerIndex: Int
    fun advanceTic()
}
