package com.kunal26das.doom

interface AudioOutput {
    /** Stops playback and releases platform resources. Safe to call more than once. */
    fun close()
}
