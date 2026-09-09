package com.kunal26das.doom

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import com.kunal26das.doom.startup.initializeApplication
import io.github.kunal26das.startup.DefaultContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    installGraphicsRecovery()
    val dependencies = initializeApplication(DefaultContext)
    ComposeViewport(document.body!!) {
        App(dependencies)
    }
}
