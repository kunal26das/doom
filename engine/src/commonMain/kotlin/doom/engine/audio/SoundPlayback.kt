
package doom.engine.audio

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.players
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wGetNumForName
import doom.engine.simulation.mRandom

import kotlin.math.abs

internal val DoomEngineCore.sndPrefixen
    get() = stateSoundPlayback.sndPrefixen

internal const val S_MAX_VOLUME = 127

internal const val S_CLIPPING_DIST: FixedPoint = 1200 * 0x10000

internal const val S_CLOSE_DIST: FixedPoint = 160 * 0x10000

internal const val S_ATTENUATOR = (S_CLIPPING_DIST - S_CLOSE_DIST) shr FRACBITS


internal const val NORM_PITCH = 128
internal const val NORM_PRIORITY = 64
internal const val NORM_SEP = 128

internal const val S_PITCH_PERTURB = 1
internal const val S_STEREO_SWING: FixedPoint = 96 * 0x10000

internal const val S_IFRACVOL = 30

internal const val NA = 0
internal const val S_NUMCHANNELS = 2

private var DoomEngineCore.channels: Array<SoundChannel>
    get() = stateSoundPlayback.channels
    set(value) { stateSoundPlayback.channels = value }

internal var DoomEngineCore.sndSfxVolume
    get() = stateSoundPlayback.sndSfxVolume
    set(value) { stateSoundPlayback.sndSfxVolume = value }

internal var DoomEngineCore.sndMusicVolume
    get() = stateSoundPlayback.sndMusicVolume
    set(value) { stateSoundPlayback.sndMusicVolume = value }

private var DoomEngineCore.musPaused
    get() = stateSoundPlayback.musPaused
    set(value) { stateSoundPlayback.musPaused = value }

private var DoomEngineCore.musPlaying: MusicTrack?
    get() = stateSoundPlayback.musPlaying
    set(value) { stateSoundPlayback.musPlaying = value }

internal var DoomEngineCore.numChannels
    get() = stateSoundPlayback.numChannels
    set(value) { stateSoundPlayback.numChannels = value }

private var DoomEngineCore.nextcleanup
    get() = stateSoundPlayback.nextcleanup
    set(value) { stateSoundPlayback.nextcleanup = value }

internal fun DoomEngineCore.sInit(sfxVolume: Int, musicVolume: Int) {
    var i: Int

    println("S_Init: default sfx volume $sfxVolume")


    sSetSfxVolume(sfxVolume)
    sSetMusicVolume(musicVolume)

    channels = Array(numChannels) { SoundChannel() }

    i = 0
    while (i < numChannels) {
        channels[i].sfxinfo = null
        i++
    }

    musPaused = false

    i = 1
    while (i < NUMSFX) {
        sSfx[i].usefulness = -1
        sSfx[i].lumpnum = sSfx[i].usefulness
        i++
    }
}

internal fun DoomEngineCore.sStart() {
    var cnum: Int
    val mnum: Int

    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo != null)
            sStopChannel(cnum)
        cnum++
    }

    musPaused = false

    if (gamemode == COMMERCIAL)
        mnum = MUS_RUNNIN + gamemap - 1
    else {
        val spmus = intArrayOf(

            MUS_E3M4,
            MUS_E3M2,
            MUS_E3M3,
            MUS_E1M5,
            MUS_E2M7,
            MUS_E2M4,
            MUS_E2M6,
            MUS_E2M5,
            MUS_E1M9,
        )

        if (gameepisode < 4)
            mnum = MUS_E1M1 + (gameepisode - 1) * 9 + gamemap - 1
        else
            mnum = spmus[gamemap - 1]
    }


    sChangeMusic(mnum, 1)

    nextcleanup = 15
}

internal fun DoomEngineCore.sStartSoundAtVolume(originP: SoundOrigin?, sfxId: Int, requestedVolume: Int) {
    var volume = requestedVolume
    val rc: Int
    var sep: Int
    var pitch: Int
    val priority: Int
    val sfx: SoundEffectDefinition
    val cnum: Int

    val origin = originP


    if (sfxId < 1 || sfxId > NUMSFX)
        iError("Bad sfx #: $sfxId")

    sfx = sSfx[sfxId]

    if (sfx.link != -1) {
        pitch = sfx.pitch
        priority = sfx.priority
        volume += sfx.volume

        if (volume < 1)
            return

        if (volume > sndSfxVolume)
            volume = sndSfxVolume
    } else {
        pitch = NORM_PITCH
        priority = NORM_PRIORITY
    }

    if (origin != null && origin !== players[consoleplayer].mo) {
        val vsp = intArrayOf(volume, 0, pitch)
        rc = sAdjustSoundParams(players[consoleplayer].mo!!, origin, vsp)
        volume = vsp[0]
        sep = vsp[1]
        pitch = vsp[2]

        if (origin.x == players[consoleplayer].mo!!.x
            && origin.y == players[consoleplayer].mo!!.y) {
            sep = NORM_SEP
        }

        if (rc == 0)
            return
    } else {
        sep = NORM_SEP
    }

    if (sfxId >= SFX_SAWUP
        && sfxId <= SFX_SAWHIT) {
        pitch += 8 - (mRandom() and 15)

        if (pitch < 0)
            pitch = 0
        else if (pitch > 255)
            pitch = 255
    } else if (sfxId != SFX_ITEMUP
        && sfxId != SFX_TINK) {
        pitch += 16 - (mRandom() and 31)

        if (pitch < 0)
            pitch = 0
        else if (pitch > 255)
            pitch = 255
    }

    sStopSound(origin)

    cnum = sGetChannel(origin, sfx)

    if (cnum < 0)
        return


    if (sfx.lumpnum < 0)
        sfx.lumpnum = iGetSfxLumpNum(sfx)


    if (sfx.usefulness++ < 0)
        sfx.usefulness = 1

    channels[cnum].handle = iStartSound(sfxId,
        volume,
        sep,
        pitch,
        priority)
}

internal fun DoomEngineCore.sStartSound(origin: SoundOrigin?, sfxId: Int) {
    sStartSoundAtVolume(origin, sfxId, sndSfxVolume)

}

internal fun DoomEngineCore.sStopSound(origin: SoundOrigin?) {
    var cnum: Int

    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo != null && channels[cnum].origin === origin) {
            sStopChannel(cnum)
            break
        }
        cnum++
    }
}

internal fun DoomEngineCore.sPauseSound() {
    if (musPlaying != null && !musPaused) {
        iPauseSong(musPlaying!!.handle)
        musPaused = true
    }
}

internal fun DoomEngineCore.sResumeSound() {
    if (musPlaying != null && musPaused) {
        iResumeSong(musPlaying!!.handle)
        musPaused = false
    }
}

internal fun DoomEngineCore.sUpdateSounds(listenerP: Actor?) {
    var audible: Int
    var cnum: Int
    var volume: Int
    var sep: Int
    var pitch: Int

    val listener = listenerP


    cnum = 0
    while (cnum < numChannels) {
        val c = channels[cnum]
        val sfx = c.sfxinfo

        if (sfx != null) {
            if (iSoundIsPlaying(c.handle)) {
                volume = sndSfxVolume
                pitch = NORM_PITCH
                sep = NORM_SEP

                if (sfx.link != -1) {
                    pitch = sfx.pitch
                    volume += sfx.volume
                    if (volume < 1) {
                        sStopChannel(cnum)
                        cnum++
                        continue
                    } else if (volume > sndSfxVolume) {
                        volume = sndSfxVolume
                    }
                }

                if (c.origin != null && listenerP !== c.origin) {
                    val vsp = intArrayOf(volume, sep, pitch)
                    audible = sAdjustSoundParams(listener!!,
                        c.origin!!,
                        vsp)
                    volume = vsp[0]
                    sep = vsp[1]
                    pitch = vsp[2]

                    if (audible == 0) {
                        sStopChannel(cnum)
                    } else
                        iUpdateSoundParams(c.handle, volume, sep, pitch)
                }
            } else {
                sStopChannel(cnum)
            }
        }
        cnum++
    }
}

internal fun DoomEngineCore.sSetMusicVolume(volume: Int) {
    if (volume < 0 || volume > 127) {
        iError("Attempt to set music volume at $volume")
    }

    iSetMusicVolume(127)
    iSetMusicVolume(volume)
    sndMusicVolume = volume
}

internal fun DoomEngineCore.sSetSfxVolume(volume: Int) {
    if (volume < 0 || volume > 127)
        iError("Attempt to set sfx volume at $volume")

    sndSfxVolume = volume
}

internal fun DoomEngineCore.sStartMusic(mId: Int) {
    sChangeMusic(mId, 0)
}

internal fun DoomEngineCore.sChangeMusic(musicnum: Int, looping: Int) {
    hostMusicLooping = looping
    val music: MusicTrack

    if ((musicnum <= MUS_NONE)
        || (musicnum >= NUMMUSIC)) {
        iError("Bad music number $musicnum")
    } else
        music = sMusic[musicnum]

    if (musPlaying === music)
        return

    sStopMusic()

    if (music.lumpnum == -1) {
        val namebuf = "d_${music.name}"
        music.lumpnum = wGetNumForName(namebuf)
    }

    music.data = wCacheLumpNum(music.lumpnum)
    music.handle = iRegisterSong(music.data!!)

    iPlaySong(music.handle, looping != 0)

    musPlaying = music
}

internal fun DoomEngineCore.sStopMusic() {
    if (musPlaying != null) {
        if (musPaused)
            iResumeSong(musPlaying!!.handle)

        iStopSong(musPlaying!!.handle)
        iUnRegisterSong(musPlaying!!.handle)

        musPlaying!!.data = null
        musPlaying = null
    }
}

internal fun DoomEngineCore.sStopChannel(cnum: Int) {
    var i: Int
    val c = channels[cnum]

    val sfxinfo = c.sfxinfo
    if (sfxinfo != null) {
        if (iSoundIsPlaying(c.handle)) {
            iStopSound(c.handle)
        }

        i = 0
        while (i < numChannels) {
            if (cnum != i
                && sfxinfo === channels[i].sfxinfo) {
                break
            }
            i++
        }

        sfxinfo.usefulness--

        c.sfxinfo = null
    }
}

internal fun DoomEngineCore.sAdjustSoundParams(listener: Actor, source: SoundOrigin, vsp: IntArray): Int {
    var approxDist: FixedPoint
    val adx: FixedPoint
    val ady: FixedPoint
    var angle: BinaryAngle

    adx = abs(listener.x - source.x)
    ady = abs(listener.y - source.y)

    approxDist = adx + ady - ((if (adx < ady) adx else ady) shr 1)

    if (gamemap != 8
        && approxDist > S_CLIPPING_DIST) {
        return 0
    }

    angle = FixedGeometry.angleBetween(listener.x,
        listener.y,
        source.x,
        source.y)

    if (angle > listener.angle)
        angle -= listener.angle
    else
        angle += (0xffffffffu - listener.angle)

    angle = angle shr ANGLETOFINESHIFT

    vsp[1] = 128 - (fixedMul(S_STEREO_SWING, finesine[angle.toInt()]) shr FRACBITS)

    if (approxDist < S_CLOSE_DIST) {
        vsp[0] = sndSfxVolume
    } else if (gamemap == 8) {
        if (approxDist > S_CLIPPING_DIST)
            approxDist = S_CLIPPING_DIST

        vsp[0] = 15 + ((sndSfxVolume - 15)
                * ((S_CLIPPING_DIST - approxDist) shr FRACBITS)) / S_ATTENUATOR
    } else {
        vsp[0] = (sndSfxVolume
                * ((S_CLIPPING_DIST - approxDist) shr FRACBITS)) / S_ATTENUATOR
    }

    return if (vsp[0] > 0) 1 else 0
}

internal fun DoomEngineCore.sGetChannel(origin: SoundOrigin?, sfxinfo: SoundEffectDefinition): Int {
    var cnum: Int

    val c: SoundChannel

    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo == null)
            break
        else if (origin != null && channels[cnum].origin === origin) {
            sStopChannel(cnum)
            break
        }
        cnum++
    }

    if (cnum == numChannels) {
        cnum = 0
        while (cnum < numChannels) {
            if (channels[cnum].sfxinfo!!.priority >= sfxinfo.priority) break
            cnum++
        }

        if (cnum == numChannels) {
            return -1
        } else {
            sStopChannel(cnum)
        }
    }

    c = channels[cnum]

    c.sfxinfo = sfxinfo
    c.origin = origin

    return cnum
}

internal var DoomEngineCore.hostMusicLooping: Int
    get() = stateSoundPlayback.hostMusicLooping
    set(value) { stateSoundPlayback.hostMusicLooping = value }

internal fun DoomEngineCore.sHostMusicId(): Int? {
    val music = musPlaying ?: return null
    return sMusic.indexOf(music).takeIf { it > MUS_NONE }
}

internal fun DoomEngineCore.sDetachHostAudio() {
    var failure: Throwable? = null
    fun attempt(action: () -> Unit) {
        try { action() } catch (error: Throwable) {
            val previous = failure
            if (previous == null) failure = error else if (previous !== error) previous.addSuppressed(error)
        }
    }
    for (channel in channels) {
        if (channel.sfxinfo != null) attempt { iStopSound(channel.handle) }
        channel.sfxinfo = null
        channel.origin = null
    }
    musPlaying?.let { music ->
        attempt { iStopSong(music.handle) }
        attempt { iUnRegisterSong(music.handle) }
        music.data = null
        music.handle = 0
    }
    musPlaying = null
    musPaused = false
    failure?.let { throw it }
}
