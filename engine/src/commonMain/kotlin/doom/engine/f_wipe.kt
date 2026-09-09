// Compatibility entry points for the independently owned screen-wipe object.
// Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
@file:Suppress("FunctionName", "unused")

package doom.engine

internal const val wipe_ColorXForm = 0
internal const val wipe_Melt = 1
internal const val wipe_NUMWIPES = 2

internal fun DoomEngineCore.wipe_StartScreen(x: Int, y: Int, width: Int, height: Int): Int =
    screenWipe.captureStart(x, y, width, height)

internal fun DoomEngineCore.wipe_EndScreen(x: Int, y: Int, width: Int, height: Int): Int =
    screenWipe.captureEnd(x, y, width, height)

internal fun DoomEngineCore.wipe_ScreenWipe(wipeno: Int, x: Int, y: Int, width: Int, height: Int, ticks: Int): Boolean =
    screenWipe.advance(wipeno, x, y, width, height, ticks)
