package com.kunal26das.doom

import kotlinx.browser.window

actual val isTouchPlatform: Boolean
    get() = window.navigator.maxTouchPoints > 0
