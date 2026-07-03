package com.kunal26das.doom

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive

@OptIn(ExperimentalForeignApi::class)
actual fun startAudioOutput(render: (FloatArray) -> Unit): Boolean {
    try {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        session.setActive(true, error = null)

        val engine = AVAudioEngine()
        val player = AVAudioPlayerNode()
        val format = AVAudioFormat(sampleRate = OUTPUT_RATE.toDouble(), channels = 2u)
        engine.attachNode(player)
        engine.connect(player, engine.mainMixerNode, format)
        engine.startAndReturnError(null)
        player.play()

        val frames = 1024
        val floats = FloatArray(frames * 2)

        // Double-buffered pull: each completion schedules the next buffer.
        fun scheduleNext() {
            render(floats)
            val buf = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = frames.toUInt())
            buf.frameLength = frames.toUInt()
            val ch = buf.floatChannelData
            val left = ch!![0]!!
            val right = ch[1]!!
            for (i in 0 until frames) {
                left[i] = floats[i * 2]
                right[i] = floats[i * 2 + 1]
            }
            player.scheduleBuffer(buf) { scheduleNext() }
        }
        scheduleNext()
        scheduleNext()
        scheduleNext()
        return true
    } catch (e: Exception) {
        println("audio unavailable: ${e.message}")
        return false
    }
}
