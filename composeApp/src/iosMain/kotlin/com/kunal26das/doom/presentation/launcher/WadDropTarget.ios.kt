package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable

@Composable
actual fun rememberWadDropState(
    enabled: Boolean,
    onSelected: (String, ByteArray) -> Unit,
    onError: (String) -> Unit,
): WadDropState = WadDropState()
