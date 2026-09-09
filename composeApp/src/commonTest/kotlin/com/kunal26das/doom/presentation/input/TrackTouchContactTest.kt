@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kunal26das.doom.presentation.input

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class TrackTouchContactTest {
    @Test
    fun touchingWithoutDraggingImmediatelyStartsAndConsumesTheContact() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)
        val down = change(1, previousPressed = false)

        scope.send(down)
        runCurrent()

        assertEquals(listOf("start"), actions)
        assertTrue(down.isConsumed)
        job.cancelAndJoin()
        assertEquals(listOf("start", "end"), actions)
    }

    @Test
    fun draggingOutsideTheControlRemainsActiveUntilItsPointerLifts() = runTest {
        val scope = FakeTouchPointerScope()
        val input: PointerInputScope = scope
        val positions = mutableListOf<Offset>()
        var releases = 0
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            input.trackTouchContact(positions::add, positions::add) { releases++ }
        }

        scope.send(change(1, previousPressed = false))
        scope.send(change(1, position = Offset(-80f, 180f)))
        runCurrent()

        assertEquals(listOf(Offset(50f, 50f), Offset(-80f, 180f)), positions)
        assertEquals(0, releases)

        scope.send(change(1, pressed = false, position = Offset(-80f, 180f)))
        runCurrent()
        job.cancelAndJoin()
        assertEquals(1, releases)
    }

    @Test
    fun independentControlsReleaseAndRepressWhileTheOtherContactStaysHeld() = runTest {
        val stick = FakeTouchPointerScope()
        val fire = FakeTouchPointerScope()
        val stickActions = mutableListOf<String>()
        val fireActions = mutableListOf<String>()
        val stickJob = track(stick, stickActions)
        val fireJob = track(fire, fireActions)

        stick.send(change(1, previousPressed = false))
        fire.send(change(2, previousPressed = false))
        runCurrent()
        assertEquals(listOf("start"), stickActions)
        assertEquals(listOf("start"), fireActions)

        fire.send(change(2, pressed = false))
        fire.send(change(3, previousPressed = false))
        stick.send(change(1, position = Offset(80f, 10f)))
        runCurrent()
        assertEquals(listOf("start", "move"), stickActions)
        assertEquals(listOf("start", "end", "start"), fireActions)

        fireJob.cancelAndJoin()
        assertEquals(listOf("start", "move"), stickActions)
        stickJob.cancelAndJoin()
        assertEquals(listOf("start", "move", "end"), stickActions)
        assertEquals(listOf("start", "end", "start", "end"), fireActions)
    }

    @Test
    fun liftingTheOwnerDoesNotAdoptASecondAlreadyHeldContact() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)

        scope.send(change(1, previousPressed = false))
        scope.send(change(1), change(2, previousPressed = false))
        scope.send(change(1, pressed = false), change(2))
        scope.send(change(2, position = Offset(80f, 80f)))
        runCurrent()

        assertEquals(listOf("start", "move", "end"), actions)
        job.cancelAndJoin()
        assertEquals(listOf("start", "move", "end"), actions)
    }

    @Test
    fun aFreshContactStartsWhileAnIgnoredContactIsStillHeld() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)

        scope.send(change(1, previousPressed = false))
        scope.send(change(1), change(2, previousPressed = false))
        scope.send(change(1, pressed = false), change(2))
        scope.send(change(2), change(3, previousPressed = false))
        runCurrent()

        assertEquals(listOf("start", "move", "end", "start"), actions)
        job.cancelAndJoin()
        assertEquals(listOf("start", "move", "end", "start", "end"), actions)
    }

    @Test
    fun releaseAndFreshPressInTheSameEventDoNotLoseTheNewContact() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)

        scope.send(change(1, previousPressed = false))
        scope.send(change(1, pressed = false), change(2, previousPressed = false))
        runCurrent()

        assertEquals(listOf("start", "end", "start"), actions)
        job.cancelAndJoin()
        assertEquals(listOf("start", "end", "start", "end"), actions)
    }

    @Test
    fun consumedCancellationReleasesTheOwnerExactlyOnce() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)

        scope.send(change(1, previousPressed = false))
        scope.send(change(1, pressed = false, consumed = true))
        runCurrent()
        job.cancelAndJoin()

        assertEquals(listOf("start", "end"), actions)
    }

    @Test
    fun aMissingOwnerReleasesWithoutAdoptingAnotherHeldPointer() = runTest {
        val scope = FakeTouchPointerScope()
        val actions = mutableListOf<String>()
        val job = track(scope, actions)

        scope.send(change(1, previousPressed = false))
        scope.send(change(2))
        runCurrent()
        job.cancelAndJoin()

        assertEquals(listOf("start", "end"), actions)
    }

    private fun CoroutineScope.track(scope: PointerInputScope, actions: MutableList<String>) =
        launch(start = CoroutineStart.UNDISPATCHED) {
            scope.trackTouchContact(
                onStart = { actions.add("start") },
                onMove = { actions.add("move") },
                onEnd = { actions.add("end") },
            )
        }

    private fun change(
        id: Long,
        pressed: Boolean = true,
        previousPressed: Boolean = true,
        position: Offset = Offset(50f, 50f),
        consumed: Boolean = false,
    ) = PointerInputChange(
        id = PointerId(id),
        uptimeMillis = 16L,
        position = position,
        pressed = pressed,
        previousUptimeMillis = 0L,
        previousPosition = Offset(50f, 50f),
        previousPressed = previousPressed,
        isInitiallyConsumed = consumed,
    )
}
