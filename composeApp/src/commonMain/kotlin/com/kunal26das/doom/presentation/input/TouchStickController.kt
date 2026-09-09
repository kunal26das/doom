package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.KEY_DOWNARROW
import com.kunal26das.doom.domain.KEY_LEFTARROW
import com.kunal26das.doom.domain.KEY_RIGHTARROW
import com.kunal26das.doom.domain.KEY_UPARROW

class TouchStickController(private val input: TouchInputController) {
    fun move(horizontal: Float, vertical: Float) {
        setKey(KEY_UPARROW, vertical < -0.25f)
        setKey(KEY_DOWNARROW, vertical > 0.25f)
        setKey(KEY_LEFTARROW, horizontal < -0.25f)
        setKey(KEY_RIGHTARROW, horizontal > 0.25f)
    }

    fun release() {
        move(0f, 0f)
    }

    private fun setKey(code: Int, pressed: Boolean) {
        if (pressed) input.press(code) else input.release(code)
    }
}
