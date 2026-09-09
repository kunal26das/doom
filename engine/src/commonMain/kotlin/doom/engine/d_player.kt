// Port of linuxdoom-1.10 d_player.h + p_pspr.h player/psprite definitions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// psprnum_t: overlay psprites are scaled shapes drawn directly on the view screen,
// coordinates are given for a 320*200 view screen.
internal const val ps_weapon = 0
internal const val ps_flash = 1
internal const val NUMPSPRITES = 2


// playerstate_t
internal const val PST_LIVE = 0    // Playing or camping.
internal const val PST_DEAD = 1    // Dead on the ground, view follows killer.
internal const val PST_REBORN = 2  // Ready to restart/respawn???

// cheat_t: player internal flags, for cheats and debug.
internal const val CF_NOCLIP = 1      // No clipping, walk through barriers.
internal const val CF_GODMODE = 2     // No damage, no health loss.
internal const val CF_NOMOMENTUM = 4  // Not really a cheat, just a debug aid.
