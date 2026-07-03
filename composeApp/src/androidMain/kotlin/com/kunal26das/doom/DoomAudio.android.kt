package com.kunal26das.doom

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

actual fun startAudioOutput(render: (FloatArray) -> Unit): Boolean {
    val minBuf = AudioTrack.getMinBufferSize(
        OUTPUT_RATE,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_FLOAT,
    )
    val track = try {
        AudioTrack.Builder()
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
            .setBufferSizeInBytes(maxOf(minBuf, 16384))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    } catch (e: Exception) {
        println("audio unavailable: ${e.message}")
        return false
    }
    track.play()

    val frames = 1024
    val floats = FloatArray(frames * 2)
    val thread = Thread {
        while (true) {
            render(floats)
            track.write(floats, 0, floats.size, AudioTrack.WRITE_BLOCKING)
        }
    }
    thread.isDaemon = true
    thread.name = "doom-audio"
    thread.start()
    return true
}
