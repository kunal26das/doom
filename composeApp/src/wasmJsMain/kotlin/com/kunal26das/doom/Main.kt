package com.kunal26das.doom

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.browser.document
import com.kunal26das.doom.startup.initializeApplication
import io.github.kunal26das.startup.DefaultContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    installGraphicsRecovery()
    val dependencies = initializeApplication(DefaultContext)
    ComposeViewport(document.body!!) {
        val gameLifecycle = remember { BrowserGameLifecycleOwner() }
        DisposableEffect(gameLifecycle) {
            onDispose { gameLifecycle.close() }
        }
        CompositionLocalProvider(LocalLifecycleOwner provides gameLifecycle) {
            App(dependencies)
        }
    }
}
