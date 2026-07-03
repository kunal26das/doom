// Port of linuxdoom-1.10 d_event.h -- event handling, game actions, buttons.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// evtype_t
const val ev_keydown = 0
const val ev_keyup = 1
const val ev_mouse = 2
const val ev_joystick = 3

/** Event structure. data1: keys / mouse-joystick buttons; data2/data3: mouse-joystick x/y. */
class event_t(
    val type: Int = 0,
    val data1: Int = 0,
    val data2: Int = 0,
    val data3: Int = 0,
)

// gameaction_t
const val ga_nothing = 0
const val ga_loadlevel = 1
const val ga_newgame = 2
const val ga_loadgame = 3
const val ga_savegame = 4
const val ga_playdemo = 5
const val ga_completed = 6
const val ga_victory = 7
const val ga_worlddone = 8
const val ga_screenshot = 9

// buttoncode_t
const val BT_ATTACK = 1        // Press "Fire".
const val BT_USE = 2           // Use button, to open doors, activate switches.
const val BT_SPECIAL = 128     // Flag: game events, not really buttons.
const val BT_SPECIALMASK = 3
const val BT_CHANGE = 4        // Flag, weapon change pending; if true, the next 3 bits hold weapon num.
const val BT_WEAPONMASK = 8 + 16 + 32
const val BT_WEAPONSHIFT = 3

const val BTS_PAUSE = 1        // Pause the game.
const val BTS_SAVEGAME = 2     // Save the game at each console.
const val BTS_SAVEMASK = 4 + 8 + 16  // Savegame slot numbers occupy the second byte of buttons.
const val BTS_SAVESHIFT = 2
