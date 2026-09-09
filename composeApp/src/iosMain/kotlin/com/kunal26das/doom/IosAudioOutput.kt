package com.kunal26das.doom

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSLock
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput? {
    var output: IosAudioOutput? = null
    return try {
        IosAudioOutput(render).also {
            output = it
            it.start()
        }
    } catch (e: Exception) {
        output?.close()
        println("audio unavailable: ${e.message}")
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosAudioOutput(private val render: (FloatArray) -> Unit) : AudioOutput {
    private val session = AVAudioSession.sharedInstance()
    private val engine = AVAudioEngine()
    private val player = AVAudioPlayerNode()
    private val format = AVAudioFormat(standardFormatWithSampleRate = OUTPUT_RATE.toDouble(), channels = 2u)
    private val lock = NSLock()
    private val frames = 1024
    private val samples = FloatArray(frames * 2)
    private var closed = false
    private var attached = false
    private var sessionActive = false

    fun start() {
        check(session.setCategory(AVAudioSessionCategoryAmbient, error = null)) { "Audio session category failed" }
        check(session.setActive(true, error = null)) { "Audio session activation failed" }
        sessionActive = true
        engine.attachNode(player)
        attached = true
        engine.connect(player, engine.mainMixerNode, format)
        check(engine.startAndReturnError(null)) { "Audio engine did not start" }
        repeat(3) { scheduleNext() }
        player.play()
    }

    private fun scheduleNext() {
        lock.lock()
        try {
            if (closed) return
            render(samples)
            val buffer = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = frames.toUInt())
            buffer.frameLength = frames.toUInt()
            val channels = checkNotNull(buffer.floatChannelData) { "Audio buffer has no float channels" }
            val left = checkNotNull(channels[0])
            val right = checkNotNull(channels[1])
            for (index in 0 until frames) {
                left[index] = samples[index * 2]
                right[index] = samples[index * 2 + 1]
            }
            player.scheduleBuffer(buffer) {
                try {
                    scheduleNext()
                } catch (e: Exception) {
                    println("audio stopped: ${e.message}")
                    dispatch_async(dispatch_get_main_queue()) { close() }
                }
            }
        } finally {
            lock.unlock()
        }
    }

    override fun close() {
        lock.lock()
        val alreadyClosed = closed
        closed = true
        lock.unlock()
        if (alreadyClosed) return
        runCatching { player.stop() }
        runCatching { engine.stop() }
        if (attached) runCatching { engine.detachNode(player) }
        if (sessionActive) runCatching { session.setActive(false, error = null) }
    }
}
