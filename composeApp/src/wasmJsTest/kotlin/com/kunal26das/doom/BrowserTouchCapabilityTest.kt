@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom

import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrowserTouchCapabilityTest {
    @Test
    fun mouseOnlyBrowserDoesNotShowTouchControls() {
        withTouchPoints(0) {
            assertFalse(isTouchPlatform)
        }
    }

    @Test
    fun singleTouchBrowserShowsTouchControls() {
        withTouchPoints(1) {
            assertTrue(isTouchPlatform)
        }
    }

    @Test
    fun multiTouchBrowserShowsTouchControls() {
        withTouchPoints(5) {
            assertTrue(isTouchPlatform)
        }
    }

    @Test
    fun capabilityIsReadAgainAfterItChanges() {
        withTouchPoints(0) {
            assertFalse(isTouchPlatform)
            withTouchPoints(5) {
                assertTrue(isTouchPlatform)
            }
            assertFalse(isTouchPlatform)
        }
    }

    private fun withTouchPoints(count: Int, assertion: () -> Unit) {
        val previous = overrideTouchCapability(count)
        try {
            assertion()
        } finally {
            restoreTouchCapability(previous)
        }
    }
}

private fun overrideTouchCapability(count: Int): JsAny = js("""{
    const previous = { descriptor: Object.getOwnPropertyDescriptor(window.navigator, 'maxTouchPoints') };
    Object.defineProperty(window.navigator, 'maxTouchPoints', { configurable: true, value: count });
    return previous;
}""")

private fun restoreTouchCapability(previous: JsAny): Unit = js("""{
    if (previous.descriptor) {
        Object.defineProperty(window.navigator, 'maxTouchPoints', previous.descriptor);
    } else {
        delete window.navigator.maxTouchPoints;
    }
}""")
