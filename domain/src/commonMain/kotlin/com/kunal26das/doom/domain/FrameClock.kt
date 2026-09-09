package com.kunal26das.doom.domain

/** A cancellable pacing boundary, independent of the display implementation. */
fun interface FrameClock {
    suspend fun awaitFrame()
}
