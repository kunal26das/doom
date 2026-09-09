@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.kunal26das.doom.domain.MAX_WAD_BYTES
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import kotlin.js.JsAny

@Composable
actual fun rememberWadPicker(onSelected: (String, ByteArray) -> Unit, onError: (String) -> Unit): () -> Unit {
    val selected by rememberUpdatedState(onSelected)
    val failed by rememberUpdatedState(onError)
    val picker = remember {
        createWadPicker(
            MAX_WAD_BYTES,
            { name, bytes -> selected(name, bytes.toByteArray()) },
            { message -> failed(message) },
        )
    }
    DisposableEffect(picker) { onDispose { disposeWadPicker(picker) } }
    return {
        try {
            openWadPicker(picker)
        } catch (_: Exception) {
            failed("The file picker could not open. Please try again.")
        }
    }
}

private fun createWadPicker(
    limit: Int,
    onSelected: (String, Int8Array) -> Unit,
    onError: (String) -> Unit,
): JsAny = js("""{
    return globalThis.DoomWadFiles.createPicker(limit, onSelected, onError);
}""")

private fun openWadPicker(handle: JsAny): Unit = js("""{
    handle.open();
}""")

private fun disposeWadPicker(handle: JsAny): Unit = js("""{
    handle.dispose();
}""")
