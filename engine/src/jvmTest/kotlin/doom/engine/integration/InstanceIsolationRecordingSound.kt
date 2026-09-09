package doom.engine.integration

import doom.engine.ISoundDriver

internal class InstanceIsolationRecordingSound : ISoundDriver {
    var songsPlayed = 0
    var songsStopped = 0
    var failPause = false
    override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int) = 1
    override fun stopSound(handle: Int) {}
    override fun soundIsPlaying(handle: Int) = false
    override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {}
    override fun setMusicVolume(volume: Int) {}
    override fun registerSong(data: ByteArray) = 1
    override fun playSong(handle: Int, looping: Boolean) { songsPlayed++ }
    override fun pauseSong(handle: Int) { if (failPause) error("sound unavailable") }
    override fun resumeSong(handle: Int) {}
    override fun stopSong(handle: Int) { songsStopped++ }
    override fun unregisterSong(handle: Int) {}
}
