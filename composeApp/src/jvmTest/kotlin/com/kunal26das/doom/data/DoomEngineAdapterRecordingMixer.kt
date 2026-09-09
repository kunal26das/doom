package com.kunal26das.doom.data

import com.kunal26das.doom.data.audio.AudioMixer

internal class DoomEngineAdapterRecordingMixer(private val output: Float) : AudioMixer {
    var lastRendered: FloatArray? = null
    override fun render(out: FloatArray) { lastRendered = out; out.fill(output) }
    override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int) = 0
    override fun stopSound(handle: Int) {}
    override fun soundIsPlaying(handle: Int) = false
    override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {}
    override fun setMusicVolume(volume: Int) {}
    override fun registerSong(data: ByteArray) = 0
    override fun playSong(handle: Int, looping: Boolean) {}
    override fun pauseSong(handle: Int) {}
    override fun resumeSong(handle: Int) {}
    override fun stopSong(handle: Int) {}
    override fun unregisterSong(handle: Int) {}
}
