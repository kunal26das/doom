// Deterministic DOOM random streams. Original LUT (C) id Software, GPL-2.0.
package doom.engine

internal val DoomEngineCore.prndindex: Int get() = random.gameplayIndex
internal val DoomEngineCore.rndindex: Int get() = random.presentationIndex
internal fun DoomEngineCore.P_Random(): Int = random.nextGameplay()
internal fun DoomEngineCore.M_Random(): Int = random.nextPresentation()
internal fun DoomEngineCore.M_ClearRandom() = random.reset()
