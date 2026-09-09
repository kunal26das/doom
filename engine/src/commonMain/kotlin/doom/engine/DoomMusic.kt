package doom.engine

public interface DoomMusic {
    public fun setMusicVolume(volume: Int): Unit
    public fun registerSong(data: ByteArray): Int
    public fun playSong(handle: Int, looping: Boolean): Unit
    public fun pauseSong(handle: Int): Unit
    public fun resumeSong(handle: Int): Unit
    public fun stopSong(handle: Int): Unit
    public fun unregisterSong(handle: Int): Unit
}
