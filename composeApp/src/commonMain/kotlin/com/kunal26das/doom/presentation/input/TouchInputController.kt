package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.GameKeyInput

import com.kunal26das.doom.domain.GameInput

/** Each screen owns its held buttons and weapon cycle; none survive disposal. */
class TouchInputController(private val onInput: (GameInput) -> Unit) {
    private val held = mutableSetOf<Int>()
    private var weaponCycle = 0
    private var weaponKey: Int? = null

    fun press(code: Int) {
        if (held.add(code)) onInput(GameKeyInput(code, true))
    }

    fun release(code: Int) {
        if (held.remove(code)) onInput(GameKeyInput(code, false))
    }

    fun pressNextWeapon() {
        releaseWeaponKey()
        weaponCycle = weaponCycle % 7 + 1
        weaponKey = '0'.code + weaponCycle
        press(weaponKey!!)
    }

    fun releaseWeaponKey() {
        weaponKey?.let(::release)
        weaponKey = null
    }

    fun releaseAll() {
        held.toList().forEach(::release)
        weaponKey = null
    }
}
