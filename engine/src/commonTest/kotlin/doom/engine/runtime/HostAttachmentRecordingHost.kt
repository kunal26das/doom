package doom.engine.runtime

import doom.engine.DoomHost
import doom.engine.DoomStorage
import doom.engine.DoomVideo
import doom.engine.ISoundDriver

internal class HostAttachmentRecordingHost(private val handle: Int) : DoomStorage, DoomVideo, ISoundDriver {
    val calls = mutableListOf<String>()
    val bytes = byteArrayOf(handle.toByte())
    var writtenBytes: ByteArray? = null
    var presentedFrame: IntArray? = null
    fun ports() = DoomHost(storage = this, video = this, sound = this)
    override fun read(name: String): ByteArray { calls += "read:$name"; return bytes }
    override fun write(name: String, data: ByteArray) { calls += "write:$name"; writtenBytes = data }
    override fun present(argb: IntArray) { calls += "present"; presentedFrame = argb }
    override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
        calls += "start:$id:$vol:$sep:$pitch:$priority"
        return handle
    }
    override fun stopSound(handle: Int) { calls += "stop:$handle" }
    override fun soundIsPlaying(handle: Int): Boolean { calls += "playing:$handle"; return true }
    override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) { calls += "update:$handle:$vol:$sep:$pitch" }
    override fun setMusicVolume(volume: Int) { calls += "volume:$volume" }
    override fun registerSong(data: ByteArray): Int { calls += "register"; return handle }
    override fun playSong(handle: Int, looping: Boolean) { calls += "playSong:$handle:$looping" }
    override fun pauseSong(handle: Int) { calls += "pauseSong:$handle" }
    override fun resumeSong(handle: Int) { calls += "resumeSong:$handle" }
    override fun stopSong(handle: Int) { calls += "stopSong:$handle" }
    override fun unregisterSong(handle: Int) { calls += "unregisterSong:$handle" }
}
