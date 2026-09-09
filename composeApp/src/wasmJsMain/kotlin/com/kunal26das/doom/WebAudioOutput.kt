package com.kunal26das.doom

import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.Float32Array
import org.khronos.webgl.set
import org.w3c.dom.events.Event

actual fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput? {
    val output = WebAudioOutput(render)
    return try {
        output.start()
        output
    } catch (e: Exception) {
        output.close()
        println("audio unavailable: ${e.message}")
        null
    }
}

/** Queues approximately 150 ms of PCM after a user gesture unlocks audio. */
private class WebAudioOutput(private val render: (FloatArray) -> Unit) : AudioOutput {
    private var context: AudioContext? = null
    private var timer: Int? = null
    private var scheduledUntil = 0.0
    private var closed = false
    private val frames = 2048
    private val samples = FloatArray(frames * 2)
    private val left = Float32Array(frames)
    private val right = Float32Array(frames)
    private val gestureEvents = listOf("keydown", "pointerdown", "touchstart")
    private val onGesture: (Event) -> Unit = { unlock() }

    fun start() {
        gestureEvents.forEach { document.addEventListener(it, onGesture) }
    }

    private fun pump() {
        if (closed) return
        val currentContext = context ?: return
        if (scheduledUntil < currentContext.currentTime) scheduledUntil = currentContext.currentTime + 0.05
        while (scheduledUntil - currentContext.currentTime < 0.15) {
            render(samples)
            for (index in 0 until frames) {
                left[index] = samples[index * 2]
                right[index] = samples[index * 2 + 1]
            }
            val buffer = currentContext.createBuffer(2, frames, OUTPUT_RATE.toFloat())
            buffer.copyToChannel(left, 0)
            buffer.copyToChannel(right, 1)
            val source = currentContext.createBufferSource()
            source.buffer = buffer
            source.connect(currentContext.destination)
            source.start(scheduledUntil)
            scheduledUntil += frames.toDouble() / OUTPUT_RATE
        }
    }

    private fun unlock() {
        if (closed) return
        try {
            if (context == null) {
                context = AudioContext()
                timer = window.setInterval({
                    try {
                        pump()
                    } catch (e: Exception) {
                        println("audio stopped: ${e.message}")
                        close()
                    }
                    null
                }, 40)
            }
            context?.let { if (it.state == "suspended") it.resume() }
        } catch (e: Exception) {
            println("audio unavailable: ${e.message}")
            close()
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        gestureEvents.forEach { document.removeEventListener(it, onGesture) }
        timer?.let { window.clearInterval(it) }
        timer = null
        runCatching { context?.close() }
        context = null
    }
}
