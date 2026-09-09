package com.kunal26das.doom

import org.khronos.webgl.Float32Array

internal external class AudioBuffer {
    fun copyToChannel(source: Float32Array, channelNumber: Int)
}
