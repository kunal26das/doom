// Port of linuxdoom-1.10 d_event.h -- event handling, game actions, buttons.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// evtype_t
internal const val ev_keydown = 0
internal const val ev_keyup = 1
internal const val ev_mouse = 2
internal const val ev_joystick = 3


// gameaction_t
internal const val ga_nothing = 0
internal const val ga_loadlevel = 1
internal const val ga_newgame = 2
internal const val ga_loadgame = 3
internal const val ga_savegame = 4
internal const val ga_playdemo = 5
internal const val ga_completed = 6
internal const val ga_victory = 7
internal const val ga_worlddone = 8
internal const val ga_screenshot = 9

// buttoncode_t
internal const val BT_ATTACK = 1        // Press "Fire".
internal const val BT_USE = 2           // Use button, to open doors, activate switches.
internal const val BT_SPECIAL = 128     // Flag: game events, not really buttons.
internal const val BT_SPECIALMASK = 3
internal const val BT_CHANGE = 4        // Flag, weapon change pending; if true, the next 3 bits hold weapon num.
internal const val BT_WEAPONMASK = 8 + 16 + 32
internal const val BT_WEAPONSHIFT = 3

internal const val BTS_PAUSE = 1        // Pause the game.
internal const val BTS_SAVEGAME = 2     // Save the game at each console.
internal const val BTS_SAVEMASK = 4 + 8 + 16  // Savegame slot numbers occupy the second byte of buttons.
internal const val BTS_SAVESHIFT = 2
