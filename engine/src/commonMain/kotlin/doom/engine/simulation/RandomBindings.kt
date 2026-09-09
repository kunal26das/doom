package doom.engine.simulation

import doom.engine.core.DoomEngineCore

internal val DoomEngineCore.prndindex: Int get() = random.gameplayIndex
internal val DoomEngineCore.rndindex: Int get() = random.presentationIndex
internal fun DoomEngineCore.pRandom(): Int = random.nextGameplay()
internal fun DoomEngineCore.mRandom(): Int = random.nextPresentation()
internal fun DoomEngineCore.mClearRandom() = random.reset()
