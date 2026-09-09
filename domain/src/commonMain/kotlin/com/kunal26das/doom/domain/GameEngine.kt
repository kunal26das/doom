package com.kunal26das.doom.domain

/**
 * Boundary around the stateful game engine. Calls, including the frame callback,
 * are confined to the caller's game thread. [close] releases session resources
 * and must also be safe when loading or initialization failed.
 */
interface GameEngine {
    fun start(wads: List<ByteArray>, onFrame: (GameFrame) -> Unit)
    fun step()
    fun postInput(input: GameInput)
    val quitRequested: Boolean
    fun close()
}
