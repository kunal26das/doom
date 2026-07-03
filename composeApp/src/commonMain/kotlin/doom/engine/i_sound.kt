// KMP replacement for linuxdoom-1.10 i_sound.c -- sound/music driver interface.
// s_sound.kt (the ported high-level logic) calls these; a platform/common
// mixer implements ISoundDriver. Null driver = silence, engine runs fine.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "UNUSED_PARAMETER", "ktlint")

package doom.engine

interface ISoundDriver {
    /** Starts a sound (data = DS* lump bytes), returns a handle. */
    fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int): Int
    fun stopSound(handle: Int)
    fun soundIsPlaying(handle: Int): Boolean
    fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int)

    fun setMusicVolume(volume: Int)
    /** data = D_* lump bytes (MUS format). */
    fun registerSong(data: ByteArray): Int
    fun playSong(handle: Int, looping: Boolean)
    fun pauseSong(handle: Int)
    fun resumeSong(handle: Int)
    fun stopSong(handle: Int)
    fun unregisterSong(handle: Int)
}

var soundDriver: ISoundDriver? = null

fun I_StartSound(id: Int, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
    val sfx = S_sfx[id]
    if (sfx.lumpnum == -1) sfx.lumpnum = I_GetSfxLumpNum(sfx)
    val data = W_CacheLumpNum(sfx.lumpnum)
    return soundDriver?.startSound(id, data, vol, sep, pitch, priority) ?: -1
}

fun I_StopSound(handle: Int) {
    soundDriver?.stopSound(handle)
}

fun I_SoundIsPlaying(handle: Int): Boolean = soundDriver?.soundIsPlaying(handle) ?: false

fun I_UpdateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {
    soundDriver?.updateSoundParams(handle, vol, sep, pitch)
}

fun I_SetMusicVolume(volume: Int) {
    soundDriver?.setMusicVolume(volume)
}

/** Retrieve the raw data lump index for sound descriptor. */
fun I_GetSfxLumpNum(sfx: sfxinfo_t): Int {
    val name = if (sfx.link != -1) S_sfx[sfx.link].name else sfx.name
    return W_GetNumForName("ds$name")
}

fun I_RegisterSong(data: ByteArray): Int = soundDriver?.registerSong(data) ?: 0
fun I_PlaySong(handle: Int, looping: Boolean) { soundDriver?.playSong(handle, looping) }
fun I_PauseSong(handle: Int) { soundDriver?.pauseSong(handle) }
fun I_ResumeSong(handle: Int) { soundDriver?.resumeSong(handle) }
fun I_StopSong(handle: Int) { soundDriver?.stopSong(handle) }
fun I_UnRegisterSong(handle: Int) { soundDriver?.unregisterSong(handle) }
