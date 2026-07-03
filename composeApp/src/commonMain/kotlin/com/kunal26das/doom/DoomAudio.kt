package com.kunal26das.doom

import doom.engine.ISoundDriver
import kotlin.concurrent.Volatile
import kotlin.math.pow

/**
 * Starts the platform PCM output. `render` is called from the platform's audio
 * thread (or timer on wasm) to fill an interleaved stereo Float32 buffer at
 * [OUTPUT_RATE]; returns false if audio is unavailable.
 */
expect fun startAudioOutput(render: (FloatArray) -> Unit): Boolean

const val OUTPUT_RATE = 44100

/**
 * Software SFX mixer replicating linuxdoom-1.10 i_sound.c semantics:
 *  - steptable: 2^((pitch-128)/64) pitch stepping
 *  - vol_lookup: (vol * (sample-128) * 256) / 127 into a 16-bit accumulator
 *  - x^2 stereo separation: left = vol - vol*sep^2/65536 (sep 1..256)
 * DS* lumps are DMX format: u16 format(3), u16 rate, u32 length, 16 pad bytes,
 * 8-bit unsigned samples, 16 pad bytes.
 *
 * Music is handled by an OPL synth registered separately (see DoomMusic);
 * if none is attached the game simply plays without music.
 */
class DmxSoundDriver : ISoundDriver {

    private class Chan(
        val sfxId: Int,
        val data: ByteArray,
        val dataOfs: Int,
        val dataEnd: Int,
        val start: Int,
    ) {
        // step/position in 16.16 fixed point, audio-thread only after publish
        var position = 0L
        var step = 0L
        @Volatile var leftGain = 0f   // per-sample multiplier for (s-128)
        @Volatile var rightGain = 0f
        @Volatile var finished = false
    }

    private val channels = arrayOfNulls<Chan>(NUM_CHANNELS)
    private val handles = IntArray(NUM_CHANNELS) { -1 }
    private var handleNums = 0
    private var startCounter = 0

    override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
        if (data.size < 24) return -1
        val rate = data.u16le(2)
        val len = data.i32le(4)
        val sampleCount = minOf(len - 32, data.size - 24)
        if (sampleCount <= 0) return -1

        val chan = Chan(id, data, 24, 24 + sampleCount, startCounter++)
        // steptable semantics + resample lump rate to output rate
        val pitchStep = 2.0.pow((pitch - 128) / 64.0)
        chan.step = (pitchStep * rate / OUTPUT_RATE * 65536.0).toLong()
        setGains(chan, vol, sep)

        // slot selection: reuse a finished slot, else steal the oldest (vanilla-ish)
        var slot = -1
        for (i in 0 until NUM_CHANNELS) {
            val c = channels[i]
            if (c == null || c.finished) { slot = i; break }
        }
        if (slot == -1) {
            slot = 0
            for (i in 1 until NUM_CHANNELS) {
                if ((channels[i]?.start ?: 0) < (channels[slot]?.start ?: 0)) slot = i
            }
        }
        val handle = handleNums++ and 0xFFFF
        channels[slot] = chan
        handles[slot] = handle
        return handle
    }

    private fun setGains(chan: Chan, vol: Int, sep: Int) {
        // vanilla: seperation += 1; leftvol = vol - vol*sep^2/65536; sep -= 257; right likewise
        var s = sep + 1
        val leftvol = (vol - ((vol * s * s) shr 16)).coerceIn(0, 127)
        s -= 257
        val rightvol = (vol - ((vol * s * s) shr 16)).coerceIn(0, 127)
        // vol_lookup[v*256+s] = (v*(s-128)*256)/127 scaled out of 16-bit range
        chan.leftGain = (leftvol * 256f / 127f) / 32768f
        chan.rightGain = (rightvol * 256f / 127f) / 32768f
    }

    private fun slotOf(handle: Int): Int {
        for (i in 0 until NUM_CHANNELS) if (handles[i] == handle && channels[i] != null) return i
        return -1
    }

    override fun stopSound(handle: Int) {
        val i = slotOf(handle)
        if (i >= 0) channels[i]?.finished = true
    }

    override fun soundIsPlaying(handle: Int): Boolean {
        val i = slotOf(handle)
        return i >= 0 && channels[i]?.finished == false
    }

    override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {
        val i = slotOf(handle)
        if (i >= 0) channels[i]?.let { setGains(it, vol, sep) }
    }

    /** Audio-thread entry: fill interleaved stereo float buffer. */
    fun render(out: FloatArray) {
        out.fill(0f)
        musicBackend?.render(out)
        for (i in 0 until NUM_CHANNELS) {
            val c = channels[i] ?: continue
            if (c.finished) continue
            var pos = c.position
            val step = c.step
            var o = 0
            while (o < out.size) {
                val idx = c.dataOfs + (pos shr 16).toInt()
                if (idx >= c.dataEnd) { c.finished = true; break }
                val s = (c.data[idx].toInt() and 0xFF) - 128
                out[o] += s * c.leftGain
                out[o + 1] += s * c.rightGain
                pos += step
                o += 2
            }
            c.position = pos
        }
        // final clamp
        for (i in out.indices) {
            val v = out[i]
            out[i] = if (v > 1f) 1f else if (v < -1f) -1f else v
        }
    }

    // ---- music: delegated to an optional OPL synthesizer ----
    var musicBackend: MusicBackend? = null

    override fun setMusicVolume(volume: Int) { musicBackend?.setVolume(volume) }
    override fun registerSong(data: ByteArray): Int = musicBackend?.registerSong(data) ?: 0
    override fun playSong(handle: Int, looping: Boolean) { musicBackend?.playSong(handle, looping) }
    override fun pauseSong(handle: Int) { musicBackend?.pauseSong(handle) }
    override fun resumeSong(handle: Int) { musicBackend?.resumeSong(handle) }
    override fun stopSong(handle: Int) { musicBackend?.stopSong(handle) }
    override fun unregisterSong(handle: Int) { musicBackend?.unregisterSong(handle) }

    companion object {
        const val NUM_CHANNELS = 8
    }
}

interface MusicBackend {
    fun setVolume(volume: Int)
    fun registerSong(data: ByteArray): Int
    fun playSong(handle: Int, looping: Boolean)
    fun pauseSong(handle: Int)
    fun resumeSong(handle: Int)
    fun stopSong(handle: Int)
    fun unregisterSong(handle: Int)
    /** Mix music samples (additive) into the interleaved stereo buffer. */
    fun render(out: FloatArray)
}

// tiny local LE readers (engine ones are in doom.engine, kept separate on purpose)
private fun ByteArray.u16le(off: Int): Int =
    (this[off].toInt() and 0xFF) or ((this[off + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.i32le(off: Int): Int =
    (this[off].toInt() and 0xFF) or ((this[off + 1].toInt() and 0xFF) shl 8) or
        ((this[off + 2].toInt() and 0xFF) shl 16) or ((this[off + 3].toInt() and 0xFF) shl 24)
