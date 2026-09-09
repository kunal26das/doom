package com.kunal26das.doom.presentation.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun VirtualStick(input: TouchInputController, modifier: Modifier) {
    val controller = remember(input) { TouchStickController(input) }
    var displacement by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .size(140.dp)
            .background(Color(0x33FFFFFF), CircleShape)
            .semantics { contentDescription = "Move forward or backward and turn left or right" }
            .pointerInput(controller) {
                fun move(position: Offset) {
                    val radius = size.width / 2f
                    val offset = position - Offset(radius, size.height / 2f)
                    controller.move(offset.x / radius, offset.y / radius)
                    val travel = radius - 28.dp.toPx()
                    displacement = offset * (travel / offset.getDistance().coerceAtLeast(travel))
                }
                trackTouchContact(
                    onStart = ::move,
                    onMove = ::move,
                    onEnd = {
                        displacement = Offset.Zero
                        controller.release()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.offset { IntOffset(displacement.x.roundToInt(), displacement.y.roundToInt()) }
                .size(56.dp).background(Color(0x99FFFFFF), CircleShape),
        )
    }
}
