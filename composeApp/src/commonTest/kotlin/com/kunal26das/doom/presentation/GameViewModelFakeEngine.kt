package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.GameEngine
import com.kunal26das.doom.domain.GameFrame
import com.kunal26das.doom.domain.GameInput

internal class GameViewModelFakeEngine : GameEngine {
    override var quitRequested = false
    var startCalls = 0
    var closeCalls = 0
    var onStart: () -> Unit = {}
    var onStep: () -> Unit = {}
    var onClose: () -> Unit = {}
    val inputs = mutableListOf<GameInput>()
    private var onFrame: (GameFrame) -> Unit = {}

    override fun start(wads: List<ByteArray>, onFrame: (GameFrame) -> Unit) {
        startCalls++
        this.onFrame = onFrame
        onStart()
    }

    override fun step() = onStep()
    override fun postInput(input: GameInput) {
        inputs += input
    }

    override fun close() {
        closeCalls++
        onClose()
    }

    fun emit(frame: GameFrame) = onFrame(frame)
}
