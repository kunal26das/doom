package com.kunal26das.doom.presentation.input

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown

internal suspend fun PointerInputScope.trackTouchContact(
    onStart: (Offset) -> Unit,
    onMove: (Offset) -> Unit,
    onEnd: () -> Unit,
) {
    awaitPointerEventScope {
        var owner: PointerId? = null
        try {
            while (true) {
                val event = awaitPointerEvent()
                val active = owner
                if (active != null) {
                    val change = event.changes.firstOrNull { it.id == active }
                    if (change == null || !change.pressed || change.isConsumed) {
                        owner = null
                        change?.consume()
                        onEnd()
                    } else {
                        onMove(change.position)
                        change.consume()
                    }
                }
                if (owner == null) {
                    val down = event.changes.firstOrNull { it.changedToDown() }
                    if (down != null) {
                        owner = down.id
                        down.consume()
                        onStart(down.position)
                    }
                }
            }
        } finally {
            if (owner != null) onEnd()
        }
    }
}
