@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.js.JsAny

internal class BrowserGameLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    private var closed = false
    private val observer = observePageLifecycle { state ->
        if (!closed) {
            registry.currentState = when (state) {
                "resumed" -> Lifecycle.State.RESUMED
                "started" -> Lifecycle.State.STARTED
                else -> Lifecycle.State.CREATED
            }
        }
    }

    override val lifecycle: Lifecycle get() = registry

    fun close() {
        if (closed) return
        closed = true
        disposePageLifecycle(observer)
        registry.currentState = Lifecycle.State.DESTROYED
    }
}

private fun observePageLifecycle(onState: (String) -> Unit): JsAny = js("""{
    return globalThis.DoomPageLifecycle.observe(onState);
}""")

private fun disposePageLifecycle(observer: JsAny): Unit = js("""{
    observer.dispose();
}""")
