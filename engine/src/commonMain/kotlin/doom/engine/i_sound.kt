// KMP replacement for linuxdoom-1.10 i_sound.c -- sound/music driver interface.
// s_sound.kt (the ported high-level logic) calls these; a platform/common
// mixer implements ISoundDriver. Null driver = silence, engine runs fine.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "UNUSED_PARAMETER", "ktlint")

package doom.engine

internal fun DoomEngineCore.I_StartSound(id: Int, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
    val sfx = S_sfx[id]
    if (sfx.lumpnum == -1) sfx.lumpnum = I_GetSfxLumpNum(sfx)
    val data = W_CacheLumpNum(sfx.lumpnum)
    return host.soundEffects.startSound(id, data, vol, sep, pitch, priority)
}

internal fun DoomEngineCore.I_StopSound(handle: Int) {
    host.soundEffects.stopSound(handle)
}

internal fun DoomEngineCore.I_SoundIsPlaying(handle: Int): Boolean = host.soundEffects.soundIsPlaying(handle)

internal fun DoomEngineCore.I_UpdateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {
    host.soundEffects.updateSoundParams(handle, vol, sep, pitch)
}

internal fun DoomEngineCore.I_SetMusicVolume(volume: Int) {
    host.music.setMusicVolume(volume)
}

/** Retrieve the raw data lump index for sound descriptor. */
internal fun DoomEngineCore.I_GetSfxLumpNum(sfx: sfxinfo_t): Int {
    val name = if (sfx.link != -1) S_sfx[sfx.link].name else sfx.name
    return W_GetNumForName("ds$name")
}

internal fun DoomEngineCore.I_RegisterSong(data: ByteArray): Int = host.music.registerSong(data)
internal fun DoomEngineCore.I_PlaySong(handle: Int, looping: Boolean) { host.music.playSong(handle, looping) }
internal fun DoomEngineCore.I_PauseSong(handle: Int) { host.music.pauseSong(handle) }
internal fun DoomEngineCore.I_ResumeSong(handle: Int) { host.music.resumeSong(handle) }
internal fun DoomEngineCore.I_StopSong(handle: Int) { host.music.stopSong(handle) }
internal fun DoomEngineCore.I_UnRegisterSong(handle: Int) { host.music.unregisterSong(handle) }
