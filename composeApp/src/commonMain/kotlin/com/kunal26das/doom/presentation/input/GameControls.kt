package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.GameKeyInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.KEY_BACKSPACE
import com.kunal26das.doom.domain.KEY_DOWNARROW
import com.kunal26das.doom.domain.KEY_ENTER
import com.kunal26das.doom.domain.KEY_EQUALS
import com.kunal26das.doom.domain.KEY_ESCAPE
import com.kunal26das.doom.domain.KEY_F1
import com.kunal26das.doom.domain.KEY_F10
import com.kunal26das.doom.domain.KEY_F11
import com.kunal26das.doom.domain.KEY_F12
import com.kunal26das.doom.domain.KEY_F2
import com.kunal26das.doom.domain.KEY_F3
import com.kunal26das.doom.domain.KEY_F4
import com.kunal26das.doom.domain.KEY_F5
import com.kunal26das.doom.domain.KEY_F6
import com.kunal26das.doom.domain.KEY_F7
import com.kunal26das.doom.domain.KEY_F8
import com.kunal26das.doom.domain.KEY_F9
import com.kunal26das.doom.domain.KEY_LEFTARROW
import com.kunal26das.doom.domain.KEY_MINUS
import com.kunal26das.doom.domain.KEY_PAUSE
import com.kunal26das.doom.domain.KEY_RALT
import com.kunal26das.doom.domain.KEY_RCTRL
import com.kunal26das.doom.domain.KEY_RIGHTARROW
import com.kunal26das.doom.domain.KEY_RSHIFT
import com.kunal26das.doom.domain.KEY_TAB
import com.kunal26das.doom.domain.KEY_UPARROW

fun doomKeyFor(e: KeyEvent): Int = when (e.key) {
    Key.DirectionRight -> KEY_RIGHTARROW
    Key.DirectionLeft -> KEY_LEFTARROW
    Key.DirectionUp -> KEY_UPARROW
    Key.DirectionDown -> KEY_DOWNARROW
    Key.Escape -> KEY_ESCAPE
    Key.Enter, Key.NumPadEnter -> KEY_ENTER
    Key.Tab -> KEY_TAB
    Key.Backspace -> KEY_BACKSPACE
    Key.CtrlLeft, Key.CtrlRight -> KEY_RCTRL
    Key.ShiftLeft, Key.ShiftRight -> KEY_RSHIFT
    Key.AltLeft, Key.AltRight -> KEY_RALT
    Key.Spacebar -> ' '.code
    Key.Minus -> KEY_MINUS
    Key.Equals -> KEY_EQUALS
    Key.F1 -> KEY_F1
    Key.F2 -> KEY_F2
    Key.F3 -> KEY_F3
    Key.F4 -> KEY_F4
    Key.F5 -> KEY_F5
    Key.F6 -> KEY_F6
    Key.F7 -> KEY_F7
    Key.F8 -> KEY_F8
    Key.F9 -> KEY_F9
    Key.F10 -> KEY_F10
    Key.F11 -> KEY_F11
    Key.F12 -> KEY_F12
    Key.Break -> KEY_PAUSE
    Key.A -> 'a'.code
    Key.B -> 'b'.code
    Key.C -> 'c'.code
    Key.D -> 'd'.code
    Key.E -> 'e'.code
    Key.F -> 'f'.code
    Key.G -> 'g'.code
    Key.H -> 'h'.code
    Key.I -> 'i'.code
    Key.J -> 'j'.code
    Key.K -> 'k'.code
    Key.L -> 'l'.code
    Key.M -> 'm'.code
    Key.N -> 'n'.code
    Key.O -> 'o'.code
    Key.P -> 'p'.code
    Key.Q -> 'q'.code
    Key.R -> 'r'.code
    Key.S -> 's'.code
    Key.T -> 't'.code
    Key.U -> 'u'.code
    Key.V -> 'v'.code
    Key.W -> 'w'.code
    Key.X -> 'x'.code
    Key.Y -> 'y'.code
    Key.Z -> 'z'.code
    Key.Zero -> '0'.code
    Key.One -> '1'.code
    Key.Two -> '2'.code
    Key.Three -> '3'.code
    Key.Four -> '4'.code
    Key.Five -> '5'.code
    Key.Six -> '6'.code
    Key.Seven -> '7'.code
    Key.Eight -> '8'.code
    Key.Nine -> '9'.code
    Key.Comma -> ','.code
    Key.Period -> '.'.code
    Key.Slash -> '/'.code
    Key.Semicolon -> ';'.code
    Key.Apostrophe -> '\''.code
    Key.LeftBracket -> '['.code
    Key.RightBracket -> ']'.code
    Key.Grave -> '`'.code
    else -> {
        val cp = e.utf16CodePoint
        if (cp in 32..126) cp.lowercaseAscii() else 0
    }
}

private fun Int.lowercaseAscii(): Int = if (this in 65..90) this + 32 else this

fun handleKeyEvent(e: KeyEvent, onInput: (GameInput) -> Unit): Boolean {
    val code = doomKeyFor(e)
    if (code == 0) return false
    when (e.type) {
        KeyEventType.KeyDown -> onInput(GameKeyInput(code, true))
        KeyEventType.KeyUp -> onInput(GameKeyInput(code, false))
        else -> return false
    }
    return true
}
