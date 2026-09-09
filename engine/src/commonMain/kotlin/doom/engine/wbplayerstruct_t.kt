// Port of linuxdoom-1.10 d_player.h + p_pspr.h player/psprite definitions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** INTERMISSION: structs passed as parameters to the widget code. */
internal class wbplayerstruct_t {
    var inGame = false  // C field name `in`; whether the player is in game
    // Player stats, kills, collected items etc.
    var skills = 0
    var sitems = 0
    var ssecret = 0
    var stime = 0
    val frags = IntArray(4)
    var score = 0       // current score on entry, modified on return
}
