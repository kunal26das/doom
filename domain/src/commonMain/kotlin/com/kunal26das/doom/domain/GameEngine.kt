package com.kunal26das.doom.domain

interface GameEngine {
    fun start(wads: List<ByteArray>, onFrame: (GameFrame) -> Unit)
    fun step()
    fun postInput(input: GameInput)
    val quitRequested: Boolean
    fun close()
}
