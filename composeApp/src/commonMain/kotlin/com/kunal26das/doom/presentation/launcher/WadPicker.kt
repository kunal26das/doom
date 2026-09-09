package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable

@Composable
expect fun rememberWadPicker(
    onSelected: (String, ByteArray) -> Unit,
    onError: (String) -> Unit,
): () -> Unit
