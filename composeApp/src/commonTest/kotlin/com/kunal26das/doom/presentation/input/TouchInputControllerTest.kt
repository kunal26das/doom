package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.GameKeyInput

import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.KEY_RCTRL
import com.kunal26das.doom.domain.KEY_UPARROW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TouchInputControllerTest {
    @Test
    fun holdingAButtonEmitsOnlyOnePressAndOneRelease() {
        val inputs = mutableListOf<GameInput>()
        val controller = TouchInputController(inputs::add)

        controller.press(KEY_RCTRL)
        controller.press(KEY_RCTRL)
        controller.release(KEY_RCTRL)
        controller.release(KEY_RCTRL)

        assertEquals(listOf(key(KEY_RCTRL, true), key(KEY_RCTRL, false)), inputs)
    }

    @Test
    fun releasingAnUnheldButtonDoesNotReleaseAnotherInputSource() {
        val inputs = mutableListOf<GameInput>()
        val controller = TouchInputController(inputs::add)

        controller.release(KEY_UPARROW)
        controller.releaseWeaponKey()
        controller.releaseAll()

        assertTrue(inputs.isEmpty())
    }

    @Test
    fun disposingControlsReleasesEveryHeldButtonExactlyOnce() {
        val inputs = mutableListOf<GameInput>()
        val controller = TouchInputController(inputs::add)
        controller.press(KEY_UPARROW)
        controller.press(KEY_RCTRL)
        controller.pressNextWeapon()
        inputs.clear()

        controller.releaseAll()
        controller.releaseAll()
        controller.releaseWeaponKey()
        controller.release(KEY_RCTRL)

        assertEquals(
            setOf(key(KEY_UPARROW, false), key(KEY_RCTRL, false), key('1'.code, false)),
            inputs.toSet(),
        )
        assertEquals(3, inputs.size)
    }

    @Test
    fun switchingWeaponWhileHeldReleasesThePreviousWeaponFirst() {
        val inputs = mutableListOf<GameInput>()
        val controller = TouchInputController(inputs::add)

        controller.pressNextWeapon()
        controller.pressNextWeapon()
        controller.releaseWeaponKey()

        assertEquals(
            listOf(key('1'.code, true), key('1'.code, false), key('2'.code, true), key('2'.code, false)),
            inputs,
        )
    }

    @Test
    fun weaponCycleWrapsFromSevenBackToOne() {
        val inputs = mutableListOf<GameInput>()
        val controller = TouchInputController(inputs::add)

        repeat(8) {
            controller.pressNextWeapon()
            controller.releaseWeaponKey()
        }

        val expectedCodes = "12345671".map(Char::code)
        assertEquals(expectedCodes.flatMap { listOf(key(it, true), key(it, false)) }, inputs)
    }

    @Test
    fun recreatedControlsHaveIndependentHeldKeysAndWeaponSelection() {
        val firstInputs = mutableListOf<GameInput>()
        val first = TouchInputController(firstInputs::add)
        val secondInputs = mutableListOf<GameInput>()
        val second = TouchInputController(secondInputs::add)

        first.press(KEY_UPARROW)
        first.pressNextWeapon()
        first.pressNextWeapon()
        second.press(KEY_UPARROW)
        second.pressNextWeapon()
        first.releaseAll()

        assertEquals(listOf(key(KEY_UPARROW, true), key('1'.code, true)), secondInputs)
        second.releaseAll()
        assertEquals(4, secondInputs.size)
    }

    private fun key(code: Int, pressed: Boolean): GameInput = GameKeyInput(code, pressed)
}
