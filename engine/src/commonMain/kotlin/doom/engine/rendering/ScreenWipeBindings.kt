
package doom.engine.rendering

import doom.engine.core.DoomEngineCore

internal const val WIPE_COLOR_X_FORM = 0
internal const val WIPE_MELT = 1
internal const val WIPE_NUMWIPES = 2

internal fun DoomEngineCore.wipeStartScreen() = screenWipe.captureStart()

internal fun DoomEngineCore.wipeEndScreen() = screenWipe.captureEnd()

internal fun DoomEngineCore.wipeScreenWipe(wipeno: Int, ticks: Int): Boolean =
    screenWipe.advance(wipeno, ticks)
