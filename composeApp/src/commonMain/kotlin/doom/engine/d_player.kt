// Port of linuxdoom-1.10 d_player.h + p_pspr.h player/psprite definitions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// psprnum_t: overlay psprites are scaled shapes drawn directly on the view screen,
// coordinates are given for a 320*200 view screen.
const val ps_weapon = 0
const val ps_flash = 1
const val NUMPSPRITES = 2

class pspdef_t {
    var state: state_t? = null  // a NULL state means not active
    var tics = 0
    var sx: fixed_t = 0
    var sy: fixed_t = 0
}

// playerstate_t
const val PST_LIVE = 0    // Playing or camping.
const val PST_DEAD = 1    // Dead on the ground, view follows killer.
const val PST_REBORN = 2  // Ready to restart/respawn???

// cheat_t: player internal flags, for cheats and debug.
const val CF_NOCLIP = 1      // No clipping, walk through barriers.
const val CF_GODMODE = 2     // No damage, no health loss.
const val CF_NOMOMENTUM = 4  // Not really a cheat, just a debug aid.

/** Extended player object info. */
class player_t {
    var mo: mobj_t? = null
    var playerstate = PST_LIVE
    val cmd = ticcmd_t()

    // Determine POV, including viewpoint bobbing during movement.
    var viewz: fixed_t = 0            // Focal origin above r.z
    var viewheight: fixed_t = 0       // Base height above floor for viewz.
    var deltaviewheight: fixed_t = 0  // Bob/squat speed.
    var bob: fixed_t = 0              // bounded/scaled total momentum.

    // This is only used between levels, mo->health is used during levels.
    var health = 0
    var armorpoints = 0
    var armortype = 0                 // Armor type is 0-2.

    // Power ups. invinc and invis are tic counters.
    val powers = IntArray(NUMPOWERS)
    val cards = BooleanArray(NUMCARDS)
    var backpack = false

    // Frags, kills of other players.
    val frags = IntArray(MAXPLAYERS)
    var readyweapon = wp_fist

    // Is wp_nochange if not changing.
    var pendingweapon = wp_nochange

    val weaponowned = BooleanArray(NUMWEAPONS)
    val ammo = IntArray(NUMAMMO)
    val maxammo = IntArray(NUMAMMO)

    // True if button down last tic.
    var attackdown = false
    var usedown = false

    // Bit flags, for cheats and debug. See cheat_t, above.
    var cheats = 0

    // Refired shots are less accurate.
    var refire = 0

    // For intermission stats.
    var killcount = 0
    var itemcount = 0
    var secretcount = 0

    // Hint messages.
    var message: String? = null

    // For screen flashing (red or bright).
    var damagecount = 0
    var bonuscount = 0

    // Who did damage (NULL for floors/ceilings).
    var attacker: mobj_t? = null

    // So gun flashes light up areas.
    var extralight = 0

    // Current PLAYPAL, ??? can be set to REDCOLORMAP for pain, etc.
    var fixedcolormap = 0

    // Player skin colorshift, 0-3 for which color to draw player.
    var colormap = 0

    // Overlay view sprites (gun, etc).
    val psprites = Array(NUMPSPRITES) { pspdef_t() }

    // True if secret level has been done.
    var didsecret = false
}

/** INTERMISSION: structs passed as parameters to the widget code. */
class wbplayerstruct_t {
    var inGame = false  // C field name `in`; whether the player is in game
    // Player stats, kills, collected items etc.
    var skills = 0
    var sitems = 0
    var ssecret = 0
    var stime = 0
    val frags = IntArray(4)
    var score = 0       // current score on entry, modified on return
}

class wbstartstruct_t {
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
