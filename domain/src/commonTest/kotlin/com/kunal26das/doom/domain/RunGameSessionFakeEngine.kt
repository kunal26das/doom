package com.kunal26das.doom.domain

internal class RunGameSessionFakeEngine : GameEngine {
    override var quitRequested = false
    var startCalls = 0
    var stepCalls = 0
    var closeCalls = 0
    var receivedWads: List<ByteArray>? = null
    val inputs = mutableListOf<GameInput>()
    var onStart: () -> Unit = {}
    var onStep: () -> Unit = {}
    var onClose: () -> Unit = {}
    private var onFrame: (GameFrame) -> Unit = {}

    override fun start(wads: List<ByteArray>, onFrame: (GameFrame) -> Unit) {
        startCalls++
        receivedWads = wads
        this.onFrame = onFrame
        onStart()
    }

    override fun step() {
        stepCalls++
        onStep()
    }

    override fun postInput(input: GameInput) {
        inputs += input
    }

    override fun close() {
        closeCalls++
        onClose()
    }

    fun emit(frame: GameFrame) = onFrame(frame)
}
