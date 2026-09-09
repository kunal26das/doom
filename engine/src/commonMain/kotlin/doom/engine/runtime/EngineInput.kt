package doom.engine.runtime

import doom.engine.DoomInput

internal interface EngineInput {
    fun post(event: DoomInput)
    fun clear()
    fun close()
}
