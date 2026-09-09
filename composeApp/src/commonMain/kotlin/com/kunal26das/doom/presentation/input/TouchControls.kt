package com.kunal26das.doom.presentation.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.GameMouseInput
import com.kunal26das.doom.domain.KEY_ENTER
import com.kunal26das.doom.domain.KEY_ESCAPE
import com.kunal26das.doom.domain.KEY_RCTRL
import com.kunal26das.doom.domain.KEY_RSHIFT
import com.kunal26das.doom.domain.KEY_TAB

@Composable
fun TouchControls(onInput: (GameInput) -> Unit, modifier: Modifier = Modifier) {
    val sendInput by rememberUpdatedState(onInput)
    val controller = remember { TouchInputController { sendInput(it) } }
    DisposableEffect(controller) { onDispose { controller.releaseAll() } }
    Box(modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                var previous = Offset.Zero
                var accumulated = 0f
                var turning = false
                trackTouchContact(
                    onStart = {
                        previous = it
                        accumulated = 0f
                        turning = it.x > size.width / 2f
                    },
                    onMove = {
                        if (turning) {
                            accumulated += it.x - previous.x
                            val step = (accumulated * 2).toInt()
                            if (step != 0) {
                                accumulated -= step / 2f
                                sendInput(GameMouseInput(step))
                            }
                        }
                        previous = it
                    },
                    onEnd = { turning = false },
                )
            },
        )
        VirtualStick(
            controller,
            Modifier.align(Alignment.BottomStart).padding(start = 28.dp, bottom = 28.dp),
        )
        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HoldButton("WPN", 52.dp, controller::pressNextWeapon, controller::releaseWeaponKey)
                HoldButton("USE", 60.dp, { controller.press(' '.code) }, { controller.release(' '.code) })
            }
            HoldButton(
                "FIRE", 84.dp,
                onPress = { controller.press(KEY_RCTRL); controller.press(KEY_ENTER) },
                onRelease = { controller.release(KEY_RCTRL); controller.release(KEY_ENTER) },
            )
        }
        Row(
            Modifier.align(Alignment.TopEnd).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HoldButton("Y", 36.dp, { controller.press('y'.code) }, { controller.release('y'.code) })
            HoldButton("N", 36.dp, { controller.press('n'.code) }, { controller.release('n'.code) })
            ToggleButton("RUN", 44.dp) { on ->
                if (on) controller.press(KEY_RSHIFT) else controller.release(KEY_RSHIFT)
            }
            HoldButton("MAP", 44.dp, { controller.press(KEY_TAB) }, { controller.release(KEY_TAB) })
            HoldButton("ESC", 44.dp, { controller.press(KEY_ESCAPE) }, { controller.release(KEY_ESCAPE) })
        }
    }
}
