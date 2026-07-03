// Port of linuxdoom-1.10 doomdef.h -- global constants and enums.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// GameMode_t
const val shareware = 0     // DOOM 1 shareware, E1, M9
const val registered = 1    // DOOM 1 registered, E3, M27
const val commercial = 2    // DOOM 2 retail, E1 M34
const val retail = 3        // DOOM 1 retail, E4, M36
const val indetermined = 4  // Well, no IWAD found.

// GameMission_t
const val doom = 0          // DOOM 1
const val doom2 = 1         // DOOM 2
const val pack_tnt = 2      // TNT mission pack
const val pack_plut = 3     // Plutonia pack
const val none = 4

// Language_t
const val english = 0
const val french = 1
const val german = 2
const val unknown = 3

const val SCREENWIDTH = 320
const val SCREENHEIGHT = 200

const val MAXPLAYERS = 4
const val TICRATE = 35

// gamestate_t: The current state of the game: whether we are
// playing, gazing at the intermission screen, the game final animation,
// or a demo.
const val GS_LEVEL = 0
const val GS_INTERMISSION = 1
const val GS_FINALE = 2
const val GS_DEMOSCREEN = 3
const val GS_WIPE = -1  // wipegamestate sentinel

// Difficulty/skill settings/filters.
const val MTF_EASY = 1
const val MTF_NORMAL = 2
const val MTF_HARD = 4
const val MTF_AMBUSH = 8  // Deaf monsters/do not react to sound.

// skill_t
const val sk_baby = 0
const val sk_easy = 1
const val sk_medium = 2
const val sk_hard = 3
const val sk_nightmare = 4

// card_t: Key cards.
const val it_bluecard = 0
const val it_yellowcard = 1
const val it_redcard = 2
const val it_blueskull = 3
const val it_yellowskull = 4
const val it_redskull = 5
const val NUMCARDS = 6

// weapontype_t: The defined weapons, including a marker indicating user has not changed weapon.
const val wp_fist = 0
const val wp_pistol = 1
const val wp_shotgun = 2
const val wp_chaingun = 3
const val wp_missile = 4
const val wp_plasma = 5
const val wp_bfg = 6
const val wp_chainsaw = 7
const val wp_supershotgun = 8
const val NUMWEAPONS = 9
const val wp_nochange = 9  // No pending weapon change.

// ammotype_t: Ammunition types defined.
const val am_clip = 0   // Pistol / chaingun ammo.
const val am_shell = 1  // Shotgun / double barreled shotgun.
const val am_cell = 2   // Plasma rifle, BFG.
const val am_misl = 3   // Missile launcher.
const val NUMAMMO = 4
const val am_noammo = 4 // Unlimited for chainsaw / fist.

// powertype_t: Power up artifacts.
const val pw_invulnerability = 0
const val pw_strength = 1
const val pw_invisibility = 2
const val pw_ironfeet = 3
const val pw_allmap = 4
const val pw_infrared = 5
const val NUMPOWERS = 6

// Power up durations: how many seconds till expiration, assuming TICRATE is 35 ticks/second.
const val INVULNTICS = 30 * TICRATE
const val INVISTICS = 60 * TICRATE
const val INFRATICS = 120 * TICRATE
const val IRONTICS = 60 * TICRATE

// DOOM keyboard definition.
const val KEY_RIGHTARROW = 0xae
const val KEY_LEFTARROW = 0xac
const val KEY_UPARROW = 0xad
const val KEY_DOWNARROW = 0xaf
const val KEY_ESCAPE = 27
const val KEY_ENTER = 13
const val KEY_TAB = 9
const val KEY_F1 = 0x80 + 0x3b
const val KEY_F2 = 0x80 + 0x3c
const val KEY_F3 = 0x80 + 0x3d
const val KEY_F4 = 0x80 + 0x3e
const val KEY_F5 = 0x80 + 0x3f
const val KEY_F6 = 0x80 + 0x40
const val KEY_F7 = 0x80 + 0x41
const val KEY_F8 = 0x80 + 0x42
const val KEY_F9 = 0x80 + 0x43
const val KEY_F10 = 0x80 + 0x44
const val KEY_F11 = 0x80 + 0x57
const val KEY_F12 = 0x80 + 0x58

const val KEY_BACKSPACE = 127
const val KEY_PAUSE = 0xff

const val KEY_EQUALS = 0x3d
const val KEY_MINUS = 0x2d

const val KEY_RSHIFT = 0x80 + 0x36
const val KEY_RCTRL = 0x80 + 0x1d
const val KEY_RALT = 0x80 + 0x38
const val KEY_LALT = KEY_RALT
