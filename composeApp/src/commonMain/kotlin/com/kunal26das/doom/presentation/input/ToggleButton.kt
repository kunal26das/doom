package com.kunal26das.doom.presentation.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp

@Composable
internal fun ToggleButton(label: String, size: Dp, onToggle: (Boolean) -> Unit) {
    var selected by remember { mutableStateOf(false) }
    HoldButton(
        label = label,
        size = size,
        onPress = { selected = !selected; onToggle(selected) },
        onRelease = {},
        selected = selected,
    )
}
