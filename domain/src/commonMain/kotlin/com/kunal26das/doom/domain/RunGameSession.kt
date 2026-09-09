package com.kunal26das.doom.domain

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex

/**
 * Loads game data, drives one engine session and releases its resources on every
 * exit path. A completed or cancelled session may be started again sequentially.
 * Calls to [run] and [postInput] must use the engine's game thread.
 */
class RunGameSession(
    private val wads: WadRepository,
    private val engine: GameEngine,
) {
    private val runGuard = Mutex()
    private var initialized = false

    suspend fun run(clock: FrameClock, onFrame: (GameFrame) -> Unit) {
        check(runGuard.tryLock()) { "A game session is already running" }
        var failure: Throwable? = null
        try {
            currentCoroutineContext().ensureActive()
            val gameData = wads.load()
            currentCoroutineContext().ensureActive()
            engine.start(gameData, onFrame)
            currentCoroutineContext().ensureActive()
            initialized = true
            while (!engine.quitRequested) {
                clock.awaitFrame()
                currentCoroutineContext().ensureActive()
                if (!engine.quitRequested) engine.step()
            }
            currentCoroutineContext().ensureActive()
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            initialized = false
            try {
                engine.close()
            } catch (cleanupError: Throwable) {
                if (failure == null) throw cleanupError
                if (cleanupError !== failure) failure.addSuppressed(cleanupError)
            } finally {
                runGuard.unlock()
            }
        }
    }

    fun postInput(input: GameInput) {
        if (initialized && !engine.quitRequested) engine.postInput(input)
    }
}
