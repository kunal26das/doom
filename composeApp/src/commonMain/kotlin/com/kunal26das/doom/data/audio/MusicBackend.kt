package com.kunal26das.doom.data.audio

interface MusicBackend {
    fun setVolume(volume: Int)
    fun registerSong(data: ByteArray): Int
    fun playSong(handle: Int, looping: Boolean)
    fun pauseSong(handle: Int)
    fun resumeSong(handle: Int)
    fun stopSong(handle: Int)
    fun unregisterSong(handle: Int)
    fun render(out: FloatArray)
}
