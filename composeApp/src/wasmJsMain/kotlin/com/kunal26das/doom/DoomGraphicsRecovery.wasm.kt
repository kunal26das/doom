@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom

/** Keep the reload action visible when the browser loses Compose's graphics surface. */
internal fun installGraphicsRecovery(): Unit = js("""{
    globalThis.DoomGraphicsRecovery.install();
}""")
