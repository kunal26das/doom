@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.kunal26das.doom.domain.MAX_WAD_BYTES
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import kotlin.js.JsAny

@Composable
actual fun rememberWadDropState(
    enabled: Boolean,
    onSelected: (String, ByteArray) -> Unit,
    onError: (String) -> Unit,
): WadDropState {
    var dragging by remember { mutableStateOf(false) }
    val selected by rememberUpdatedState(onSelected)
    val failed by rememberUpdatedState(onError)
    val target = remember {
        createWadDropTarget(
            MAX_WAD_BYTES,
            { name, bytes -> selected(name, bytes.toByteArray()) },
            { message -> failed(message) },
            { active -> dragging = active },
        )
    }
    DisposableEffect(target) { onDispose { disposeWadDropTarget(target) } }
    DisposableEffect(target, enabled) {
        setWadDropEnabled(target, enabled)
        onDispose { setWadDropEnabled(target, false) }
    }
    return WadDropState(supported = true, isDragging = enabled && dragging)
}

private fun createWadDropTarget(
    limit: Int,
    onSelected: (String, Int8Array) -> Unit,
    onError: (String) -> Unit,
    onDraggingChanged: (Boolean) -> Unit,
): JsAny = js("""{
    return globalThis.DoomWadFiles.createDropTarget(limit, onSelected, onError, onDraggingChanged);
}""")

private fun setWadDropEnabled(handle: JsAny, enabled: Boolean): Unit = js("""{
    handle.setEnabled(enabled);
}""")

private fun disposeWadDropTarget(handle: JsAny): Unit = js("""{
    handle.dispose();
}""")
