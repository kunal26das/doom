// Port of linuxdoom-1.10 d_event.h -- event handling, game actions, buttons.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** Event structure. data1: keys / mouse-joystick buttons; data2/data3: mouse-joystick x/y. */
internal class event_t(
    val type: Int = 0,
    val data1: Int = 0,
    val data2: Int = 0,
    val data3: Int = 0,
)
