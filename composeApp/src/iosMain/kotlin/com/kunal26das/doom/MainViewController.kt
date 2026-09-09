package com.kunal26das.doom

import androidx.compose.ui.window.ComposeUIViewController
import com.kunal26das.doom.startup.initializeApplication
import io.github.kunal26das.startup.DefaultContext
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val dependencies = initializeApplication(DefaultContext)
    return ComposeUIViewController { App(dependencies) }
}
