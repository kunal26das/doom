package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.GameKeyInput

import com.kunal26das.doom.domain.FrameClock
import com.kunal26das.doom.domain.GameFrame
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.RunGameSession
import com.kunal26das.doom.domain.WadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GameViewModelTest {
    @Test
    fun loadingTransitionsToRunningOnAFrameAndRetainsTheFrameOnStop() = runTest {
        val loaded = CompletableDeferred<List<ByteArray>>()
        val clock = Channel<Unit>(Channel.UNLIMITED)
        val firstFrame = GameFrame(1, 1, intArrayOf(7))
        val engine = GameViewModelFakeEngine().apply { onStep = { emit(firstFrame) } }
        val viewModel = viewModel(engine, WadRepository { loaded.await() })

        assertEquals(GamePhase.Loading, viewModel.state.value.phase)
        assertNull(viewModel.state.value.frame)
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.run(FrameClock { clock.receive() })
        }
        assertEquals(GamePhase.Loading, viewModel.state.value.phase)

        loaded.complete(emptyList())
        clock.send(Unit)
        testScheduler.runCurrent()
        assertEquals(GamePhase.Running, viewModel.state.value.phase)
        assertSame(firstFrame, viewModel.state.value.frame)
        assertNull(viewModel.state.value.error)

        engine.quitRequested = true
        clock.send(Unit)
        running.join()
        assertEquals(GamePhase.Stopped, viewModel.state.value.phase)
        assertSame(firstFrame, viewModel.state.value.frame)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun startupFailureIsVisibleAndCannotBeRestartedOnLifecycleResume() = runTest {
        val engine = GameViewModelFakeEngine().apply { onStart = { error("Invalid game data") } }
        val viewModel = viewModel(engine)

        viewModel.run(FrameClock { error("Unexpected frame wait") })
        assertEquals(GamePhase.Failed, viewModel.state.value.phase)
        assertEquals("Invalid game data", viewModel.state.value.error)
        assertNull(viewModel.state.value.frame)

        viewModel.run(FrameClock { error("Unexpected frame wait") })
        assertEquals(1, engine.startCalls)
        assertEquals(GamePhase.Failed, viewModel.state.value.phase)
    }

    @Test
    fun errorAfterAFrameRemainsVisibleAlongsideTheLastFrame() = runTest {
        val frame = GameFrame(1, 1, intArrayOf(9))
        val engine = GameViewModelFakeEngine().apply {
            onStep = {
                emit(frame)
                error("Engine crashed after rendering")
            }
        }
        val viewModel = viewModel(engine)

        viewModel.run(FrameClock {})

        assertEquals(GamePhase.Failed, viewModel.state.value.phase)
        assertSame(frame, viewModel.state.value.frame)
        assertEquals("Engine crashed after rendering", viewModel.state.value.error)
        assertEquals(1, engine.closeCalls)
        viewModel.run(FrameClock {})
        assertEquals(1, engine.startCalls)
    }

    @Test
    fun frameEmittedDuringFailedStartupDoesNotHideTheError() = runTest {
        val frame = GameFrame(1, 1, intArrayOf(5))
        val engine = GameViewModelFakeEngine().apply {
            onStart = {
                emit(frame)
                error("Initialization failed")
            }
        }
        val viewModel = viewModel(engine)

        viewModel.run(FrameClock { error("Unexpected frame wait") })

        assertEquals(GamePhase.Failed, viewModel.state.value.phase)
        assertSame(frame, viewModel.state.value.frame)
        assertEquals("Initialization failed", viewModel.state.value.error)
    }

    @Test
    fun quitIsTerminalAndDoesNotRestartOnLifecycleResume() = runTest {
        val engine = GameViewModelFakeEngine().apply { onStart = { quitRequested = true } }
        val viewModel = viewModel(engine)

        viewModel.run(FrameClock { error("Unexpected frame wait") })
        viewModel.run(FrameClock { error("Unexpected frame wait") })

        assertEquals(GamePhase.Stopped, viewModel.state.value.phase)
        assertNull(viewModel.state.value.error)
        assertEquals(1, engine.startCalls)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun lifecycleCancellationPreservesFrameAndAllowsResume() = runTest {
        val frame = GameFrame(1, 1, intArrayOf(8))
        val engine = GameViewModelFakeEngine().apply { onStart = { emit(frame) } }
        val viewModel = viewModel(engine)
        var cancellationRethrown = false
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                viewModel.run(FrameClock { awaitCancellation() })
            } catch (cancelled: CancellationException) {
                cancellationRethrown = true
                throw cancelled
            }
        }

        running.cancelAndJoin()
        assertTrue(cancellationRethrown)
        assertEquals(GamePhase.Stopped, viewModel.state.value.phase)
        assertSame(frame, viewModel.state.value.frame)
        assertNull(viewModel.state.value.error)
        assertEquals(1, engine.closeCalls)

        val resumed = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.run(FrameClock { awaitCancellation() })
        }
        assertEquals(2, engine.startCalls)
        assertEquals(GamePhase.Running, viewModel.state.value.phase)
        resumed.cancelAndJoin()
        assertEquals(2, engine.closeCalls)
    }

    @Test
    fun cancellationWhileLoadingDoesNotBecomeAnErrorAndCanResume() = runTest {
        var loads = 0
        val engine = GameViewModelFakeEngine()
        val viewModel = viewModel(engine, WadRepository {
            if (++loads == 1) awaitCancellation()
            emptyList()
        })
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.run(FrameClock { awaitCancellation() })
        }

        running.cancelAndJoin()
        assertEquals(GamePhase.Stopped, viewModel.state.value.phase)
        assertNull(viewModel.state.value.error)
        assertEquals(0, engine.startCalls)
        assertEquals(1, engine.closeCalls)

        engine.onStart = { engine.quitRequested = true }
        viewModel.run(FrameClock { error("Unexpected frame wait") })
        assertEquals(1, engine.startCalls)
        assertEquals(2, engine.closeCalls)
    }

    @Test
    fun inputIsForwardedOnlyDuringAnInitializedSession() = runTest {
        val engine = GameViewModelFakeEngine()
        val viewModel = viewModel(engine)
        val input = GameKeyInput(32, true)
        viewModel.onInput(input)
        assertTrue(engine.inputs.isEmpty())

        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.run(FrameClock { awaitCancellation() })
        }
        viewModel.onInput(input)
        assertEquals(listOf<GameInput>(input), engine.inputs)

        running.cancelAndJoin()
        viewModel.onInput(input)
        assertEquals(listOf<GameInput>(input), engine.inputs)
    }

    @Test
    fun duplicateLifecycleRunDoesNotDisruptTheActiveSession() = runTest {
        val frame = GameFrame(1, 1, intArrayOf(3))
        val engine = GameViewModelFakeEngine().apply { onStart = { emit(frame) } }
        val viewModel = viewModel(engine)
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.run(FrameClock { awaitCancellation() })
        }

        viewModel.run(FrameClock { error("Unexpected frame wait") })

        assertEquals(1, engine.startCalls)
        assertEquals(0, engine.closeCalls)
        assertEquals(GamePhase.Running, viewModel.state.value.phase)
        running.cancelAndJoin()
    }

    @Test
    fun cleanupFailureDoesNotReplaceTheOriginalUserFacingError() = runTest {
        val engine = GameViewModelFakeEngine().apply {
            onStart = { error("Invalid WAD") }
            onClose = { error("Audio shutdown failed") }
        }
        val viewModel = viewModel(engine)

        viewModel.run(FrameClock {})

        assertEquals(GamePhase.Failed, viewModel.state.value.phase)
        assertEquals("Invalid WAD", viewModel.state.value.error)
    }

    private fun viewModel(
        engine: GameViewModelFakeEngine,
        wads: WadRepository = WadRepository { emptyList() },
    ) = GameViewModel(RunGameSession(wads, engine))

}
