
package doom.engine.input

internal const val EV_KEYDOWN = 0
internal const val EV_KEYUP = 1
internal const val EV_MOUSE = 2
internal const val EV_JOYSTICK = 3

internal const val BT_ATTACK = 1
internal const val BT_USE = 2
internal const val BT_SPECIAL = 128
internal const val BT_SPECIALMASK = 3
internal const val BT_CHANGE = 4
internal const val BT_WEAPONMASK = 8 + 16 + 32
internal const val BT_WEAPONSHIFT = 3

internal const val BTS_PAUSE = 1
internal const val BTS_SAVEGAME = 2
internal const val BTS_SAVEMASK = 4 + 8 + 16
internal const val BTS_SAVESHIFT = 2
