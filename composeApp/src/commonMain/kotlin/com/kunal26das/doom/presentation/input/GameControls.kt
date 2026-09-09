package com.kunal26das.doom.presentation.input

import com.kunal26das.doom.domain.GameKeyInput
import com.kunal26das.doom.domain.GameMouseInput

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/** Compose key -> DOOM keycode (0 = unmapped). Letters map to lowercase ASCII (cheats!). */
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

/** Handles a Compose KeyEvent by posting the DOOM event; returns consumed. */
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

/**
 * On-screen controls for phones/tablets:
 *  - left virtual stick: forward/back + strafe (arrow up/down + , .)
 *  - right drag area: turn (mouse-style)
 *  - FIRE (Ctrl+Enter), USE (space), WPN cycle (1-7), ESC, MAP, RUN toggle, Y/N
 */
@Composable
fun TouchControls(onInput: (GameInput) -> Unit, modifier: Modifier = Modifier) {
    val sendInput by rememberUpdatedState(onInput)
    val controller = remember { TouchInputController { sendInput(it) } }
    DisposableEffect(controller) { onDispose { controller.releaseAll() } }
    fun press(code: Int) = controller.press(code)
    fun release(code: Int) = controller.release(code)
    Box(modifier.fillMaxSize()) {
        // Right half: turning via horizontal drag (DOOM mouse turn).
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var acc = 0f
                    detectDragGestures(
                        onDragStart = { acc = 0f },
                        onDrag = { change, drag ->
                            if (change.position.x > size.width / 2) {
                                acc += drag.x
                                val step = (acc * 2).toInt()
                                if (step != 0) {
                                    acc -= step / 2f
                                    sendInput(GameMouseInput(step))
                                }
                            }
                        },
                    )
                }
        )

        // Left virtual stick.
        VirtualStick(
            controller,
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 28.dp)
        )

        // Right-side buttons.
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HoldButton("WPN", 52.dp, onPress = { controller.pressNextWeapon() }, onRelease = { controller.releaseWeaponKey() })
                HoldButton("USE", 60.dp, onPress = { press(' '.code) }, onRelease = { release(' '.code) })
            }
            HoldButton(
                "FIRE", 84.dp,
                onPress = { press(KEY_RCTRL); press(KEY_ENTER) },
                onRelease = { release(KEY_RCTRL); release(KEY_ENTER) },
            )
        }

        // Top bar: ESC / MAP / RUN / Y / N small buttons.
        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HoldButton("Y", 36.dp, onPress = { press('y'.code) }, onRelease = { release('y'.code) })
            HoldButton("N", 36.dp, onPress = { press('n'.code) }, onRelease = { release('n'.code) })
            ToggleButton("RUN", 44.dp, onToggle = { on -> if (on) press(KEY_RSHIFT) else release(KEY_RSHIFT) })
            HoldButton("MAP", 44.dp, onPress = { press(KEY_TAB) }, onRelease = { release(KEY_TAB) })
            HoldButton("ESC", 44.dp, onPress = { press(KEY_ESCAPE) }, onRelease = { release(KEY_ESCAPE) })
        }
    }
}

@Composable
private fun VirtualStick(controller: TouchInputController, modifier: Modifier) {
    val pressed = remember { mutableSetOf<Int>() }

    fun setKey(code: Int, want: Boolean) {
        if (want && code !in pressed) { pressed.add(code); controller.press(code) }
        if (!want && code in pressed) { pressed.remove(code); controller.release(code) }
    }

    fun update(v: Offset?) {
        val dead = 18f
        val x = v?.x ?: 0f
        val y = v?.y ?: 0f
        setKey(KEY_UPARROW, y < -dead)
        setKey(KEY_DOWNARROW, y > dead)
        setKey(','.code, x < -dead)  // strafe left
        setKey('.'.code, x > dead)   // strafe right
    }

    Box(
        modifier
            .size(140.dp)
            .background(Color(0x33FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos -> update(pos - Offset(size.width / 2f, size.height / 2f)) },
                    onDrag = { change, _ -> update(change.position - Offset(size.width / 2f, size.height / 2f)) },
                    onDragEnd = { update(null) },
                    onDragCancel = { update(null) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(56.dp).background(Color(0x55FFFFFF), CircleShape))
    }
}

@Composable
private fun HoldButton(label: String, size: androidx.compose.ui.unit.Dp, onPress: () -> Unit, onRelease: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .background(Color(0x44FFFFFF), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onPress()
                    try {
                        waitForUpOrCancellation()
                    } finally {
                        onRelease()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color(0xCCFFFFFF), fontSize = 12.sp)
    }
}

@Composable
private fun ToggleButton(label: String, size: androidx.compose.ui.unit.Dp, onToggle: (Boolean) -> Unit) {
    val on = remember { arrayOf(false) }
    Box(
        Modifier
            .size(size)
            .background(Color(0x44FFFFFF), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    on[0] = !on[0]
                    onToggle(on[0])
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color(0xCCFFFFFF), fontSize = 11.sp)
    }
}
