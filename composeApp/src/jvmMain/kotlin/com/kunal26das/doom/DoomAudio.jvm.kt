package com.kunal26das.doom

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

actual fun startAudioOutput(render: (FloatArray) -> Unit): Boolean {
    val format = AudioFormat(OUTPUT_RATE.toFloat(), 16, 2, true, false)
    val line: SourceDataLine = try {
        AudioSystem.getSourceDataLine(format).apply {
            open(format, 8192)
            start()
        }
    } catch (e: Exception) {
        println("audio unavailable: ${e.message}")
        return false
    }

    val frames = 1024
    val floats = FloatArray(frames * 2)
    val bytes = ByteArray(frames * 4)
    val thread = Thread {
        while (true) {
            render(floats)
            var o = 0
            for (f in floats) {
                val s = (f * 32767f).toInt().coerceIn(-32768, 32767)
                bytes[o++] = (s and 0xFF).toByte()
                bytes[o++] = ((s shr 8) and 0xFF).toByte()
            }
            line.write(bytes, 0, bytes.size)
        }
    }
    thread.isDaemon = true
    thread.name = "doom-audio"
    thread.start()
    return true
}
