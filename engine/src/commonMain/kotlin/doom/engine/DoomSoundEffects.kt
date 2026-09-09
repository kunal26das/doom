package doom.engine

public interface DoomSoundEffects {
    public fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int): Int
    public fun stopSound(handle: Int)
    public fun soundIsPlaying(handle: Int): Boolean
    public fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int)
}
