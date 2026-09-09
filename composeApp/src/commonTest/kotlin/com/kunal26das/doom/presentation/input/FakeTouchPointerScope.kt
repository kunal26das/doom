package com.kunal26das.doom.presentation.input

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.IntSize
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FakeTouchPointerScope : PointerInputScope, AwaitPointerEventScope {
    private val events = Channel<PointerEvent>(Channel.UNLIMITED)

    override val density = 1f
    override val fontScale = 1f
    override val size = IntSize(100, 100)
    override val extendedTouchPadding = Size.Zero
    override var currentEvent = PointerEvent(emptyList())
        private set
    override val viewConfiguration = object : ViewConfiguration {
        override val longPressTimeoutMillis = 500L
        override val doubleTapTimeoutMillis = 300L
        override val doubleTapMinTimeMillis = 40L
        override val touchSlop = 18f
    }

    fun send(vararg changes: PointerInputChange) {
        events.trySend(PointerEvent(changes.toList())).getOrThrow()
    }

    override suspend fun awaitPointerEvent(pass: PointerEventPass): PointerEvent {
        check(pass == PointerEventPass.Main)
        return events.receive().also { currentEvent = it }
    }

    override suspend fun <R> awaitPointerEventScope(
        block: suspend AwaitPointerEventScope.() -> R,
    ): R = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { events.cancel() }
        block.startCoroutine(this, object : Continuation<R> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<R>) {
                continuation.resumeWith(result)
            }
        })
    }
}
