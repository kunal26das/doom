package com.kunal26das.doom.presentation.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HoldButton(
    label: String,
    size: Dp,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    selected: Boolean = false,
) {
    val press by rememberUpdatedState(onPress)
    val release by rememberUpdatedState(onRelease)
    var held by remember { mutableStateOf(false) }
    Box(
        Modifier.size(size)
            .background(if (held || selected) Color(0x99D6A15A) else Color(0x44FFFFFF), CircleShape)
            .pointerInput(Unit) {
                trackTouchContact(
                    onStart = { held = true; press() },
                    onMove = {},
                    onEnd = { held = false; release() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color(0xEEFFFFFF), fontSize = 12.sp)
    }
}
