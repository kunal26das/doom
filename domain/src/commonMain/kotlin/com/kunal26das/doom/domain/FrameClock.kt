package com.kunal26das.doom.domain

fun interface FrameClock {
    suspend fun awaitFrame()
}
