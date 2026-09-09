// Single-player scheduling adapted from linuxdoom-1.10 d_net.c and d_main.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

import doom.engine.ticcmd_t

/** Owner-thread input operations needed to prepare one simulation command. */
internal interface TickInput {
    fun collectEvents()
    fun buildCommand(command: ticcmd_t)
}
