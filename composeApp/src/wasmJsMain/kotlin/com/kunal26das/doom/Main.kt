package com.kunal26das.doom

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    installRenderWatchdog()
    ComposeViewport(document.body!!) {
        App()
    }
}
