package doom.engine

public interface DoomMusic {
    public fun setMusicVolume(volume: Int)
    public fun registerSong(data: ByteArray): Int
    public fun playSong(handle: Int, looping: Boolean)
    public fun pauseSong(handle: Int)
    public fun resumeSong(handle: Int)
    public fun stopSong(handle: Int)
    public fun unregisterSong(handle: Int)
}
