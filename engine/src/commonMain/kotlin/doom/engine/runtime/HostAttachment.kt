package doom.engine.runtime

import doom.engine.DoomHost
import doom.engine.DoomMusic
import doom.engine.DoomSoundEffects
import doom.engine.DoomStorage
import doom.engine.DoomVideo

/**
 * The single owner of replaceable host references. Services receive only the
 * port they use; detaching drops every attached platform object together.
 */
internal class HostAttachment(initial: DoomHost = DoomHost()) : DoomStorage, DoomVideo {
    private var current = initial.copy(clock = null)

    val soundEffects = object : DoomSoundEffects {
        override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int) =
            current.sound?.startSound(id, data, vol, sep, pitch, priority) ?: -1
        override fun stopSound(handle: Int) { current.sound?.stopSound(handle) }
        override fun soundIsPlaying(handle: Int) = current.sound?.soundIsPlaying(handle) ?: false
        override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {
            current.sound?.updateSoundParams(handle, vol, sep, pitch)
        }
    }

    val music = object : DoomMusic {
        override fun setMusicVolume(volume: Int) { current.sound?.setMusicVolume(volume) }
        override fun registerSong(data: ByteArray) = current.sound?.registerSong(data) ?: 0
        override fun playSong(handle: Int, looping: Boolean) { current.sound?.playSong(handle, looping) }
        override fun pauseSong(handle: Int) { current.sound?.pauseSong(handle) }
        override fun resumeSong(handle: Int) { current.sound?.resumeSong(handle) }
        override fun stopSong(handle: Int) { current.sound?.stopSong(handle) }
        override fun unregisterSong(handle: Int) { current.sound?.unregisterSong(handle) }
    }

    val isDetached: Boolean get() = current.storage == null && current.video == null && current.sound == null

    fun attach(host: DoomHost) { current = host.copy(clock = null) }
    fun detach() { current = DoomHost() }

    override fun read(name: String): ByteArray? = current.storage?.read(name)
    override fun write(name: String, data: ByteArray) { current.storage?.write(name, data) }
    override fun present(argb: IntArray) { current.video?.present(argb) }
}
