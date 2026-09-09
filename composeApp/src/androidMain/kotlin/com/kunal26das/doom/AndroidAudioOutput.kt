package com.kunal26das.doom

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean

actual fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput? {
    var track: AudioTrack? = null
    var output: AndroidAudioOutput? = null
    return try {
        val minimumBuffer = AudioTrack.getMinBufferSize(
            OUTPUT_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT,
        )
        check(minimumBuffer > 0) { "PCM float stereo output is unsupported" }
        val openedTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(OUTPUT_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minimumBuffer, 16384))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = openedTrack
        check(openedTrack.state == AudioTrack.STATE_INITIALIZED) { "Audio device did not initialize" }
        openedTrack.play()
        AndroidAudioOutput(openedTrack, render).also {
            output = it
            it.start()
        }
    } catch (e: Exception) {
        if (output != null) output.close() else runCatching { track?.release() }
        println("audio unavailable: ${e.message}")
        null
    }
}

private class AndroidAudioOutput(
    private val track: AudioTrack,
    private val render: (FloatArray) -> Unit,
) : AudioOutput {
    private val running = AtomicBoolean(true)
    private val thread = Thread({ pump() }, "doom-audio").apply { isDaemon = true }

    fun start() = thread.start()

    private fun pump() {
        val samples = FloatArray(1024 * 2)
        try {
            while (running.get()) {
                render(samples)
                var offset = 0
                while (running.get() && offset < samples.size) {
                    val written = track.write(samples, offset, samples.size - offset, AudioTrack.WRITE_BLOCKING)
                    check(written > 0) { "Audio device write failed: $written" }
                    offset += written
                }
            }
        } catch (e: Exception) {
            if (running.get()) println("audio stopped: ${e.message}")
        } finally {
            close()
        }
    }

    override fun close() {
        if (!running.compareAndSet(true, false)) return
        // Pause and flush unblock a streaming write before releasing the track.
        runCatching { track.pause() }
        runCatching { track.flush() }
        runCatching { track.stop() }
        runCatching { track.release() }
        thread.interrupt()
    }
}
