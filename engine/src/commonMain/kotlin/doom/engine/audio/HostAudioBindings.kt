
package doom.engine.audio

import doom.engine.core.DoomEngineCore
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wGetNumForName

internal fun DoomEngineCore.iStartSound(id: Int, vol: Int, sep: Int, pitch: Int, priority: Int): Int {
    val sfx = sSfx[id]
    if (sfx.lumpnum == -1) sfx.lumpnum = iGetSfxLumpNum(sfx)
    val data = wCacheLumpNum(sfx.lumpnum)
    return host.soundEffects.startSound(id, data, vol, sep, pitch, priority)
}

internal fun DoomEngineCore.iStopSound(handle: Int) {
    host.soundEffects.stopSound(handle)
}

internal fun DoomEngineCore.iSoundIsPlaying(handle: Int): Boolean = host.soundEffects.soundIsPlaying(handle)

internal fun DoomEngineCore.iUpdateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {
    host.soundEffects.updateSoundParams(handle, vol, sep, pitch)
}

internal fun DoomEngineCore.iSetMusicVolume(volume: Int) {
    host.music.setMusicVolume(volume)
}

internal fun DoomEngineCore.iGetSfxLumpNum(sfx: SoundEffectDefinition): Int {
    val name = if (sfx.link != -1) sSfx[sfx.link].name else sfx.name
    return wGetNumForName("ds$name")
}

internal fun DoomEngineCore.iRegisterSong(data: ByteArray): Int = host.music.registerSong(data)
internal fun DoomEngineCore.iPlaySong(handle: Int, looping: Boolean) { host.music.playSong(handle, looping) }
internal fun DoomEngineCore.iPauseSong(handle: Int) { host.music.pauseSong(handle) }
internal fun DoomEngineCore.iResumeSong(handle: Int) { host.music.resumeSong(handle) }
internal fun DoomEngineCore.iStopSong(handle: Int) { host.music.stopSong(handle) }
internal fun DoomEngineCore.iUnRegisterSong(handle: Int) { host.music.unregisterSong(handle) }
