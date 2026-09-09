package com.kunal26das.doom

/**
 * Starts the platform PCM output. `render` is called from the platform's audio
 * thread (or timer on wasm) to fill an interleaved stereo Float32 buffer at
 * [OUTPUT_RATE]; returns null if audio is unavailable. The caller owns the
 * returned output and must close it when the game session ends.
 */
expect fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput?


const val OUTPUT_RATE = 44100
