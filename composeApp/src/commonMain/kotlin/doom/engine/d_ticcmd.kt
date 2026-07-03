// Port of linuxdoom-1.10 d_ticcmd.h -- per-tic player command.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "ktlint")

package doom.engine

/**
 * The data sampled per tick (single player) and transmitted to other peers (multiplayer).
 * Mainly movements/button commands per game tick.
 * forwardmove/sidemove are signed char in C; angleturn/consistancy signed short;
 * chatchar/buttons byte. All held as Int with the same value ranges.
 */
class ticcmd_t {
    var forwardmove = 0  // *2048 for move
    var sidemove = 0     // *2048 for move
    var angleturn = 0    // <<16 for angle delta
    var consistancy = 0  // checks for net game
    var chatchar = 0
    var buttons = 0

    fun copyFrom(other: ticcmd_t) {
        forwardmove = other.forwardmove
        sidemove = other.sidemove
        angleturn = other.angleturn
        consistancy = other.consistancy
        chatchar = other.chatchar
        buttons = other.buttons
    }
}
