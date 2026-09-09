package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.GameKeyInput
import com.kunal26das.doom.domain.KEY_DOWNARROW
import com.kunal26das.doom.domain.KEY_ENTER
import com.kunal26das.doom.domain.KEY_LEFTARROW
import com.kunal26das.doom.domain.KEY_RCTRL
import com.kunal26das.doom.domain.KEY_RIGHTARROW
import com.kunal26das.doom.domain.KEY_UPARROW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TouchStickControllerTest {
    @Test
    fun diagonalMovementTurnsWhileFireRemainsHeld() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)

        input.press(KEY_RCTRL)
        input.press(KEY_ENTER)
        stick.move(horizontal = 0.75f, vertical = -0.75f)

        assertEquals(setOf(KEY_RCTRL, KEY_ENTER, KEY_UPARROW, KEY_RIGHTARROW), heldKeys(inputs))
        assertFalse(inputs.any { it is GameKeyInput && it.code in setOf(','.code, '.'.code) })
        assertEquals(4, inputs.size)
    }

    @Test
    fun releasingAndPressingFireAgainPreservesMovementAndTurning() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)
        stick.move(horizontal = -0.75f, vertical = -0.75f)
        input.press(KEY_RCTRL)
        input.press(KEY_ENTER)

        input.release(KEY_RCTRL)
        input.release(KEY_ENTER)

        assertEquals(setOf(KEY_UPARROW, KEY_LEFTARROW), heldKeys(inputs))

        input.press(KEY_RCTRL)
        input.press(KEY_ENTER)

        assertEquals(setOf(KEY_UPARROW, KEY_LEFTARROW, KEY_RCTRL, KEY_ENTER), heldKeys(inputs))
        assertEquals(
            listOf(GameKeyInput(KEY_UPARROW, true)),
            inputs.filter { it is GameKeyInput && it.code == KEY_UPARROW },
        )
        assertEquals(
            listOf(GameKeyInput(KEY_LEFTARROW, true)),
            inputs.filter { it is GameKeyInput && it.code == KEY_LEFTARROW },
        )
    }

    @Test
    fun releasingStickPreservesFireAndReleasesItsDirectionsOnlyOnce() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)
        input.press(KEY_RCTRL)
        input.press(KEY_ENTER)
        stick.move(horizontal = 0.75f, vertical = -0.75f)
        val beforeRelease = inputs.size

        stick.release()
        stick.release()

        assertEquals(setOf(KEY_RCTRL, KEY_ENTER), heldKeys(inputs))
        assertEquals(
            setOf(GameKeyInput(KEY_UPARROW, false), GameKeyInput(KEY_RIGHTARROW, false)),
            inputs.drop(beforeRelease).toSet(),
        )
        assertEquals(beforeRelease + 2, inputs.size)
    }

    @Test
    fun reversingDirectionReleasesPreviousMovementAndTurnKeys() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)
        stick.move(horizontal = -0.75f, vertical = -0.75f)

        stick.move(horizontal = 0.75f, vertical = 0.75f)

        assertEquals(setOf(KEY_DOWNARROW, KEY_RIGHTARROW), heldKeys(inputs))
        assertEquals(
            listOf(GameKeyInput(KEY_UPARROW, true), GameKeyInput(KEY_UPARROW, false)),
            inputs.filter { it is GameKeyInput && it.code == KEY_UPARROW },
        )
        assertEquals(
            listOf(GameKeyInput(KEY_LEFTARROW, true), GameKeyInput(KEY_LEFTARROW, false)),
            inputs.filter { it is GameKeyInput && it.code == KEY_LEFTARROW },
        )
        assertEquals(6, inputs.size)
    }

    @Test
    fun deadZoneIncludesItsBoundaryAndReturningToItStopsTheStick() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)

        stick.move(horizontal = 0f, vertical = 0f)
        stick.move(horizontal = 0.25f, vertical = -0.25f)
        stick.move(horizontal = -0.25f, vertical = 0.25f)

        assertTrue(inputs.isEmpty())

        stick.move(horizontal = 0.251f, vertical = -0.251f)
        assertEquals(setOf(KEY_UPARROW, KEY_RIGHTARROW), heldKeys(inputs))

        stick.move(horizontal = 0.25f, vertical = -0.25f)

        assertTrue(heldKeys(inputs).isEmpty())
        assertEquals(4, inputs.size)
    }

    @Test
    fun movementWithinTheSameDirectionDoesNotRepeatPresses() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)

        stick.move(horizontal = -0.5f, vertical = 0.5f)
        stick.move(horizontal = -0.75f, vertical = 0.9f)
        stick.move(horizontal = -1f, vertical = 1f)

        assertEquals(setOf(KEY_DOWNARROW, KEY_LEFTARROW), heldKeys(inputs))
        assertEquals(2, inputs.size)
    }

    @Test
    fun disposingAllTouchControlsReleasesStickAndFireWithoutDuplicateReleases() {
        val inputs = mutableListOf<GameInput>()
        val input = TouchInputController(inputs::add)
        val stick = TouchStickController(input)
        stick.move(horizontal = -0.75f, vertical = -0.75f)
        input.press(KEY_RCTRL)
        input.press(KEY_ENTER)

        input.releaseAll()
        stick.release()
        input.release(KEY_RCTRL)
        input.release(KEY_ENTER)
        input.releaseAll()

        assertTrue(heldKeys(inputs).isEmpty())
        assertEquals(8, inputs.size)
        assertEquals(4, inputs.filterIsInstance<GameKeyInput>().count { !it.pressed })
    }

    private fun heldKeys(inputs: List<GameInput>): Set<Int> = buildSet {
        for (input in inputs.filterIsInstance<GameKeyInput>()) {
            if (input.pressed) add(input.code) else remove(input.code)
        }
    }
}
