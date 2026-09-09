package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable

/** Opens a local file picker. Cancellation leaves the current launcher state unchanged. */
@Composable
expect fun rememberWadPicker(
    onSelected: (String, ByteArray) -> Unit,
    onError: (String) -> Unit,
): () -> Unit
