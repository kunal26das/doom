package com.kunal26das.doom

import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

actual fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput? {
    var line: SourceDataLine? = null
    var output: JvmAudioOutput? = null
    return try {
        val format = AudioFormat(OUTPUT_RATE.toFloat(), 16, 2, true, false)
        val openedLine = AudioSystem.getSourceDataLine(format)
        line = openedLine
        openedLine.open(format, 8192)
        openedLine.start()
        JvmAudioOutput(openedLine, render).also {
            output = it
            it.start()
        }
    } catch (e: Exception) {
        if (output != null) output.close() else runCatching { line?.close() }
        println("audio unavailable: ${e.message}")
        null
    }
}

private class JvmAudioOutput(
    private val line: SourceDataLine,
    private val render: (FloatArray) -> Unit,
) : AudioOutput {
    private val running = AtomicBoolean(true)
    private val thread = Thread({ pump() }, "doom-audio").apply { isDaemon = true }

    fun start() = thread.start()

    private fun pump() {
        val floats = FloatArray(1024 * 2)
        val bytes = ByteArray(1024 * 4)
        try {
            while (running.get()) {
                render(floats)
                var index = 0
                for (sample in floats) {
                    val value = (sample * 32767f).toInt().coerceIn(-32768, 32767)
                    bytes[index++] = (value and 0xFF).toByte()
                    bytes[index++] = ((value shr 8) and 0xFF).toByte()
                }
                var offset = 0
                while (running.get() && offset < bytes.size) {
                    val written = line.write(bytes, offset, bytes.size - offset)
                    check(written > 0) { "Audio device stopped accepting samples" }
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
        runCatching { line.stop() }
        runCatching { line.flush() }
        runCatching { line.close() }
        thread.interrupt()
    }
}
