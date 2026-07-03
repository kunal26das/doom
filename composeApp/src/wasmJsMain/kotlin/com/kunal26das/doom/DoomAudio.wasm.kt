package com.kunal26das.doom

import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.Float32Array
import org.khronos.webgl.set

private external class AudioContext {
    val currentTime: Double
    val destination: AudioDestinationNode
    val state: String
    fun createBuffer(numberOfChannels: Int, length: Int, sampleRate: Float): AudioBuffer
    fun createBufferSource(): AudioBufferSourceNode
    fun resume()
}

private external class AudioBuffer {
    fun copyToChannel(source: Float32Array, channelNumber: Int)
}

private external class AudioBufferSourceNode {
    var buffer: AudioBuffer?
    fun connect(destination: AudioDestinationNode)
    fun start(`when`: Double)
}

private external class AudioDestinationNode

/**
 * Web Audio scheduling: a 40ms timer keeps ~150ms of rendered audio queued
 * ahead of AudioContext.currentTime. The context starts on the first user
 * gesture (browser autoplay policy).
 */
actual fun startAudioOutput(render: (FloatArray) -> Unit): Boolean {
    var ctx: AudioContext? = null
    var scheduledUntil = 0.0
    val frames = 2048
    val floats = FloatArray(frames * 2)
    val left = Float32Array(frames)
    val right = Float32Array(frames)

    fun pump() {
        val c = ctx ?: return
        val ahead = 0.15
        if (scheduledUntil < c.currentTime) scheduledUntil = c.currentTime + 0.05
        while (scheduledUntil - c.currentTime < ahead) {
            render(floats)
            for (i in 0 until frames) {
                left[i] = floats[i * 2]
                right[i] = floats[i * 2 + 1]
            }
            val buf = c.createBuffer(2, frames, OUTPUT_RATE.toFloat())
            buf.copyToChannel(left, 0)
            buf.copyToChannel(right, 1)
            val src = c.createBufferSource()
            src.buffer = buf
            src.connect(c.destination)
            src.start(scheduledUntil)
            scheduledUntil += frames.toDouble() / OUTPUT_RATE
        }
    }

    fun unlock() {
        if (ctx == null) {
            ctx = AudioContext()
            window.setInterval({ pump(); null }, 40)
        }
        ctx?.let { if (it.state == "suspended") it.resume() }
    }

    document.addEventListener("keydown", { unlock() })
    document.addEventListener("pointerdown", { unlock() })
    document.addEventListener("touchstart", { unlock() })
    return true
}
