// Port of linuxdoom-1.10 d_player.h + p_pspr.h player/psprite definitions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal class wbstartstruct_t {
    var epsd = 0        // episode # (0-2)
    var didsecret = false  // if true, splash the secret level
    var last = 0        // previous and next levels, origin 0
    var next = 0
    var maxkills = 0
    var maxitems = 0
    var maxsecret = 0
    var maxfrags = 0
    var partime = 0     // the par time
    var pnum = 0        // index of this player in game
    val plyr = Array(MAXPLAYERS) { wbplayerstruct_t() }
}
