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
    var position = 0L
    @Volatile var leftGain = 0f
    @Volatile var rightGain = 0f
    @Volatile var finished = false
}
