package com.kunal26das.doom.data.audio

import kotlin.concurrent.Volatile

internal class DmxSoundChannel(
    val sfxId: Int,
    val data: ByteArray,
    val dataOfs: Int,
    val dataEnd: Int,
    val start: Int,
    val step: Long,
) {
    // Position in 16.16 fixed point, owned by the audio thread.
    var position = 0L
    @Volatile var leftGain = 0f   // per-sample multiplier for (s-128)
    @Volatile var rightGain = 0f
    @Volatile var finished = false
}
