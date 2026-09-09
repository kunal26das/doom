package com.kunal26das.doom

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kunal26das.doom.startup.initializeApplication
import io.github.kunal26das.startup.DefaultContext

fun main() {
    val dependencies = initializeApplication(DefaultContext)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DOOM",
            state = rememberWindowState(width = 960.dp, height = 720.dp),
        ) {
            App(dependencies)
        }
    }
}
