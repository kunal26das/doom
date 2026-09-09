package com.kunal26das.doom.data.audio

import com.kunal26das.doom.OUTPUT_RATE
import kotlin.concurrent.Volatile
import kotlin.math.pow

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
class DmxSoundDriver : AudioMixer {

    // The game thread publishes a fresh array; the audio thread never observes
    // an array being modified or a partially initialized channel.
    @Volatile private var channels = arrayOfNulls<DmxSoundChannel>(NUM_CHANNELS)
    private val handles = IntArray(NUM_CHANNELS) { -1 }
    private var handleNums = 0
    private var startCounter = 0

    override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
        if (data.size < 41 || data.u16le(0) != 3) return -1
        val rate = data.u16le(2)
        val len = data.i32le(4)
        if (rate == 0 || len <= 32 || len > data.size - 8) return -1
        val sampleCount = len - 32

        // steptable semantics + resample lump rate to output rate
        val pitchStep = 2.0.pow((pitch - 128) / 64.0)
        val step = (pitchStep * rate / OUTPUT_RATE * 65536.0).toLong()
        if (step <= 0) return -1
        val chan = DmxSoundChannel(id, data, 24, 24 + sampleCount, startCounter++, step)
        setGains(chan, vol, sep)

        val currentChannels = channels
        // slot selection: reuse a finished slot, else steal the oldest (vanilla-ish)
        var slot = -1
        for (i in 0 until NUM_CHANNELS) {
            val c = currentChannels[i]
            if (c == null || c.finished) { slot = i; break }
        }
        if (slot == -1) {
            slot = 0
            for (i in 1 until NUM_CHANNELS) {
                if ((currentChannels[i]?.start ?: 0) < (currentChannels[slot]?.start ?: 0)) slot = i
            }
        }
        val handle = handleNums++ and 0xFFFF
        channels = currentChannels.copyOf().also { it[slot] = chan }
        handles[slot] = handle
        return handle
    }

    private fun setGains(chan: DmxSoundChannel, vol: Int, sep: Int) {
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
    override fun render(out: FloatArray) {
        require(out.size % 2 == 0) { "Audio output must contain interleaved stereo pairs" }
        out.fill(0f)
        musicBackend?.render(out)
        val currentChannels = channels
        for (i in 0 until NUM_CHANNELS) {
            val c = currentChannels[i] ?: continue
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
    @Volatile var musicBackend: MusicBackend? = null

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


// tiny local LE readers (engine ones are in doom.engine, kept separate on purpose)
private fun ByteArray.u16le(off: Int): Int =
    (this[off].toInt() and 0xFF) or ((this[off + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.i32le(off: Int): Int =
    (this[off].toInt() and 0xFF) or ((this[off + 1].toInt() and 0xFF) shl 8) or
        ((this[off + 2].toInt() and 0xFF) shl 16) or ((this[off + 3].toInt() and 0xFF) shl 24)
