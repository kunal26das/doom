// Port of linuxdoom-1.10 doomdef.h -- global constants and enums.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// GameMode_t
internal const val shareware = 0     // DOOM 1 shareware, E1, M9
internal const val registered = 1    // DOOM 1 registered, E3, M27
internal const val commercial = 2    // DOOM 2 retail, E1 M34
internal const val retail = 3        // DOOM 1 retail, E4, M36
internal const val indetermined = 4  // Well, no IWAD found.

// GameMission_t
internal const val doom = 0          // DOOM 1
internal const val doom2 = 1         // DOOM 2
internal const val pack_tnt = 2      // TNT mission pack
internal const val pack_plut = 3     // Plutonia pack
internal const val none = 4

// Language_t
internal const val english = 0
internal const val french = 1
internal const val german = 2
internal const val unknown = 3


internal const val MAXPLAYERS = 4

// gamestate_t: The current state of the game: whether we are
// playing, gazing at the intermission screen, the game final animation,
// or a demo.
internal const val GS_LEVEL = 0
internal const val GS_INTERMISSION = 1
internal const val GS_FINALE = 2
internal const val GS_DEMOSCREEN = 3
internal const val GS_WIPE = -1  // wipegamestate sentinel

// Difficulty/skill settings/filters.
internal const val MTF_EASY = 1
internal const val MTF_NORMAL = 2
internal const val MTF_HARD = 4
internal const val MTF_AMBUSH = 8  // Deaf monsters/do not react to sound.

// skill_t
internal const val sk_baby = 0
internal const val sk_easy = 1
internal const val sk_medium = 2
internal const val sk_hard = 3
internal const val sk_nightmare = 4

// card_t: Key cards.
internal const val it_bluecard = 0
internal const val it_yellowcard = 1
internal const val it_redcard = 2
internal const val it_blueskull = 3
internal const val it_yellowskull = 4
internal const val it_redskull = 5
internal const val NUMCARDS = 6

// weapontype_t: The defined weapons, including a marker indicating user has not changed weapon.
internal const val wp_fist = 0
internal const val wp_pistol = 1
internal const val wp_shotgun = 2
internal const val wp_chaingun = 3
internal const val wp_missile = 4
internal const val wp_plasma = 5
internal const val wp_bfg = 6
internal const val wp_chainsaw = 7
internal const val wp_supershotgun = 8
internal const val NUMWEAPONS = 9
internal const val wp_nochange = 9  // No pending weapon change.

// ammotype_t: Ammunition types defined.
internal const val am_clip = 0   // Pistol / chaingun ammo.
internal const val am_shell = 1  // Shotgun / double barreled shotgun.
internal const val am_cell = 2   // Plasma rifle, BFG.
internal const val am_misl = 3   // Missile launcher.
internal const val NUMAMMO = 4
internal const val am_noammo = 4 // Unlimited for chainsaw / fist.

// powertype_t: Power up artifacts.
internal const val pw_invulnerability = 0
internal const val pw_strength = 1
internal const val pw_invisibility = 2
internal const val pw_ironfeet = 3
internal const val pw_allmap = 4
internal const val pw_infrared = 5
internal const val NUMPOWERS = 6

// Power up durations: how many seconds till expiration, assuming TICRATE is 35 ticks/second.
internal const val INVULNTICS = 30 * TICRATE
internal const val INVISTICS = 60 * TICRATE
internal const val INFRATICS = 120 * TICRATE
internal const val IRONTICS = 60 * TICRATE

// DOOM keyboard definition.
