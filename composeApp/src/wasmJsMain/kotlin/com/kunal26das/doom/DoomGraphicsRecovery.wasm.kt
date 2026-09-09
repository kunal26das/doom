@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom

internal fun installGraphicsRecovery(): Unit = js("""{
    globalThis.DoomGraphicsRecovery.install();
}""")
