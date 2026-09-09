package com.kunal26das.doom.domain

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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RunGameSessionTest {
    @Test
    fun loadsDataBeforeStartingAndOnlyStepsOnFramesUntilQuit() = runTest {
        val events = mutableListOf<String>()
        val data = listOf(byteArrayOf(1, 2, 3))
        val frame = GameFrame(1, 1, intArrayOf(42))
        val frames = mutableListOf<GameFrame>()
        val engine = RunGameSessionFakeEngine().apply {
            onStart = { events += "start" }
            onStep = {
                events += "step"
                emit(frame)
                quitRequested = true
            }
        }
        val session = RunGameSession(WadRepository {
            events += "load"
            data
        }, engine)

        session.run(FrameClock { events += "frame" }, frames::add)

        assertEquals(listOf("load", "start", "frame", "step"), events)
        assertSame(data, engine.receivedWads)
        assertEquals(listOf(frame), frames)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun quitAtStartupNeverWaitsOrSteps() = runTest {
        val engine = RunGameSessionFakeEngine().apply { onStart = { quitRequested = true } }

        session(engine).run(FrameClock { error("Unexpected frame wait") }) {}

        assertEquals(0, engine.stepCalls)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun quitDuringAFrameWaitDoesNotStepAgain() = runTest {
        val engine = RunGameSessionFakeEngine()

        session(engine).run(FrameClock { engine.quitRequested = true }) {}

        assertEquals(0, engine.stepCalls)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun inputIsAcceptedOnlyAfterInitializationAndBeforeQuitOrCleanup() = runTest {
        val loaded = CompletableDeferred<List<ByteArray>>()
        val frameClock = Channel<Unit>(Channel.UNLIMITED)
        val engine = RunGameSessionFakeEngine()
        val session = RunGameSession(WadRepository { loaded.await() }, engine)
        val key = GameKeyInput(32, true)
        session.postInput(key)
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            session.run(FrameClock { frameClock.receive() }) {}
        }
        session.postInput(key)
        assertTrue(engine.inputs.isEmpty())

        loaded.complete(emptyList())
        testScheduler.runCurrent()
        session.postInput(key)
        session.postInput(GameMouseInput(8))
        assertEquals(listOf(key, GameMouseInput(8)), engine.inputs)

        engine.quitRequested = true
        session.postInput(key)
        frameClock.send(Unit)
        running.join()
        session.postInput(key)
        assertEquals(2, engine.inputs.size)
    }

    @Test
    fun overlappingRunIsRejectedWithoutClosingTheActiveEngine() = runTest {
        val engine = RunGameSessionFakeEngine()
        val session = session(engine)
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            session.run(FrameClock { awaitCancellation() }) {}
        }

        assertFailsWith<IllegalStateException> {
            session.run(FrameClock { error("Unexpected frame wait") }) {}
        }
        assertEquals(1, engine.startCalls)
        assertEquals(0, engine.closeCalls)

        running.cancelAndJoin()
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun cancellationWhileLoadingStillClosesAndPermitsASequentialRun() = runTest {
        val engine = RunGameSessionFakeEngine()
        var loads = 0
        val session = RunGameSession(WadRepository {
            if (++loads == 1) awaitCancellation()
            emptyList()
        }, engine)
        var cancellationRethrown = false
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                session.run(FrameClock { error("Unexpected frame wait") }) {}
            } catch (cancelled: CancellationException) {
                cancellationRethrown = true
                throw cancelled
            }
        }

        running.cancelAndJoin()
        assertTrue(cancellationRethrown)
        assertEquals(0, engine.startCalls)
        assertEquals(1, engine.closeCalls)

        engine.onStart = { engine.quitRequested = true }
        session.run(FrameClock { error("Unexpected frame wait") }) {}
        assertEquals(1, engine.startCalls)
        assertEquals(2, engine.closeCalls)
    }

    @Test
    fun cancellationWhileWaitingForAFrameClosesAndDisablesInput() = runTest {
        val engine = RunGameSessionFakeEngine()
        val session = session(engine)
        val running = launch(start = CoroutineStart.UNDISPATCHED) {
            session.run(FrameClock { awaitCancellation() }) {}
        }

        running.cancelAndJoin()
        session.postInput(GameKeyInput(32, false))

        assertEquals(1, engine.closeCalls)
        assertEquals(0, engine.stepCalls)
        assertTrue(engine.inputs.isEmpty())
    }

    @Test
    fun loadingAndStartupFailuresBothCloseTheEngine() = runTest {
        val loadingFailure = IllegalStateException("Missing WAD")
        val loadingEngine = RunGameSessionFakeEngine()
        val failedLoading = RunGameSession(WadRepository { throw loadingFailure }, loadingEngine)

        assertSame(loadingFailure, assertFailsWith<IllegalStateException> {
            failedLoading.run(FrameClock {}) {}
        })
        assertEquals(0, loadingEngine.startCalls)
        assertEquals(1, loadingEngine.closeCalls)

        val startupFailure = IllegalArgumentException("Invalid WAD")
        val startupEngine = RunGameSessionFakeEngine().apply { onStart = { throw startupFailure } }
        assertSame(startupFailure, assertFailsWith<IllegalArgumentException> {
            session(startupEngine).run(FrameClock {}) {}
        })
        assertEquals(1, startupEngine.closeCalls)
    }

    @Test
    fun renderFailureClosesAndRetainsTheOriginalFailureWhenCleanupAlsoThrows() = runTest {
        val renderFailure = IllegalStateException("Frame conversion failed")
        val cleanupFailure = IllegalArgumentException("Audio shutdown failed")
        val engine = RunGameSessionFakeEngine().apply {
            onStep = { emit(GameFrame(1, 1, intArrayOf(1))) }
            onClose = { throw cleanupFailure }
        }
        val session = session(engine)

        val thrown = assertFailsWith<IllegalStateException> {
            session.run(FrameClock {}) { throw renderFailure }
        }

        assertSame(renderFailure, thrown)
        assertEquals(listOf(cleanupFailure), thrown.suppressedExceptions)
        assertEquals(1, engine.closeCalls)
        session.postInput(GameMouseInput(1))
        assertTrue(engine.inputs.isEmpty())

        engine.onClose = {}
        engine.onStart = { engine.quitRequested = true }
        session.run(FrameClock {}) {}
        assertEquals(2, engine.closeCalls)
    }

    @Test
    fun frameClockFailureClosesTheEngine() = runTest {
        val failure = IllegalStateException("Display is gone")
        val engine = RunGameSessionFakeEngine()

        assertSame(failure, assertFailsWith<IllegalStateException> {
            session(engine).run(FrameClock { throw failure }) {}
        })

        assertEquals(1, engine.closeCalls)
        assertEquals(0, engine.stepCalls)
    }

    @Test
    fun cleanupFailureIsReportedWhenThereIsNoEarlierFailure() = runTest {
        val failure = IllegalStateException("Could not close audio")
        val engine = RunGameSessionFakeEngine().apply {
            onStart = { quitRequested = true }
            onClose = { throw failure }
        }

        assertSame(failure, assertFailsWith<IllegalStateException> {
            session(engine).run(FrameClock {}) {}
        })
        assertFalse(engine.closeCalls == 0)
    }

    private fun session(engine: RunGameSessionFakeEngine) = RunGameSession(WadRepository { emptyList() }, engine)

}
