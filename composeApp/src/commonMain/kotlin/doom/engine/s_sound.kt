// Port of linuxdoom-1.10 s_sound.c -- high-level sound/music handling
// (channel allocation, distance attenuation, per-level music selection).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING",
    "SENSELESS_COMPARISON", "KotlinConstantConditions", "ktlint")

package doom.engine

import kotlin.math.abs

// Purpose?
val snd_prefixen = charArrayOf(
    'P', 'P', 'A', 'S', 'S', 'S', 'M', 'M', 'M', 'S', 'S', 'S')

const val S_MAX_VOLUME = 127

// when to clip out sounds
// Does not fit the large outdoor areas.
const val S_CLIPPING_DIST: fixed_t = 1200 * 0x10000

// Distance tp origin when sounds should be maxed out.
// This should relate to movement clipping resolution
// (see BLOCKMAP handling).
// Originally: (200*0x10000).
const val S_CLOSE_DIST: fixed_t = 160 * 0x10000

const val S_ATTENUATOR = (S_CLIPPING_DIST - S_CLOSE_DIST) shr FRACBITS

// Adjustable by menu.
// (C: #define NORM_VOLUME snd_MaxVolume -- referenced a nonexistent symbol,
//  never used; not ported.)

const val NORM_PITCH = 128
const val NORM_PRIORITY = 64
const val NORM_SEP = 128

const val S_PITCH_PERTURB = 1
const val S_STEREO_SWING: fixed_t = 96 * 0x10000

// percent attenuation from front to back
const val S_IFRACVOL = 30

const val NA = 0
const val S_NUMCHANNELS = 2


class channel_t {
    // sound information (if null, channel avail.)
    var sfxinfo: sfxinfo_t? = null

    // origin of sound
    var origin: soundorigin_t? = null

    // handle of the sound being played
    var handle = 0
}


// the set of channels available
private var channels: Array<channel_t> = emptyArray()

// These are not used, but should be (menu).
// Maximum volume of a sound effect.
// Internal default is max out of 0-15.
var snd_SfxVolume = 15

// Maximum volume of music. Useless so far.
var snd_MusicVolume = 15


// whether songs are mus_paused
private var mus_paused = false

// music currently being played
private var mus_playing: musicinfo_t? = null

// following is set
//  by the defaults code in M_misc:
// number of channels available
var numChannels = 0

private var nextcleanup = 0


//
// Initializes sound stuff, including volume
// Sets channels, SFX and music volume,
//  allocates channel buffer, sets S_sfx lookup.
//
fun S_Init(sfxVolume: Int, musicVolume: Int) {
    var i: Int

    println("S_Init: default sfx volume $sfxVolume")

    // Whatever these did with DMX, these are rather dummies now.
    // (I_SetChannels was a platform no-op; not present in i_sound.kt.)

    S_SetSfxVolume(sfxVolume)
    // No music with Linux - another dummy.
    S_SetMusicVolume(musicVolume)

    // Allocating the internal channels for mixing
    // (the maximum numer of sounds rendered
    // simultaneously) within zone memory.
    channels = Array(numChannels) { channel_t() }

    // Free all channels for use
    i = 0
    while (i < numChannels) {
        channels[i].sfxinfo = null
        i++
    }

    // no sounds are playing, and they are not mus_paused
    mus_paused = false

    // Note that sounds have not been cached (yet).
    i = 1
    while (i < NUMSFX) {
        S_sfx[i].usefulness = -1
        S_sfx[i].lumpnum = S_sfx[i].usefulness
        i++
    }
}


//
// Per level startup code.
// Kills playing sounds at start of level,
//  determines music if any, changes music.
//
fun S_Start() {
    var cnum: Int
    val mnum: Int

    // kill all playing sounds at start of level
    //  (trust me - a good idea)
    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo != null)
            S_StopChannel(cnum)
        cnum++
    }

    // start new music for the level
    mus_paused = false

    if (gamemode == commercial)
        mnum = mus_runnin + gamemap - 1
    else {
        val spmus = intArrayOf(
            // Song - Who? - Where?

            mus_e3m4,  // American     e4m1
            mus_e3m2,  // Romero       e4m2
            mus_e3m3,  // Shawn        e4m3
            mus_e1m5,  // American     e4m4
            mus_e2m7,  // Tim          e4m5
            mus_e2m4,  // Romero       e4m6
            mus_e2m6,  // J.Anderson   e4m7 CHIRON.WAD
            mus_e2m5,  // Shawn        e4m8
            mus_e1m9,  // Tim          e4m9
        )

        if (gameepisode < 4)
            mnum = mus_e1m1 + (gameepisode - 1) * 9 + gamemap - 1
        else
            mnum = spmus[gamemap - 1]
    }

    // HACK FOR COMMERCIAL
    //  if (commercial && mnum > mus_e3m9)
    //      mnum -= mus_e3m9;

    S_ChangeMusic(mnum, 1)

    nextcleanup = 15
}


fun S_StartSoundAtVolume(origin_p: soundorigin_t?, sfx_id: Int, volume: Int) {
    var volume = volume
    val rc: Int
    var sep: Int
    var pitch: Int
    val priority: Int
    val sfx: sfxinfo_t
    val cnum: Int

    val origin = origin_p

    // Debug.
    /*fprintf( stderr,
             "S_StartSoundAtVolume: playing sound %d (%s)\n",
             sfx_id, S_sfx[sfx_id].name );*/

    // check for bogus sound #
    if (sfx_id < 1 || sfx_id > NUMSFX)
        I_Error("Bad sfx #: $sfx_id")

    sfx = S_sfx[sfx_id]

    // Initialize sound parameters
    if (sfx.link != -1) {
        pitch = sfx.pitch
        priority = sfx.priority
        volume += sfx.volume

        if (volume < 1)
            return

        if (volume > snd_SfxVolume)
            volume = snd_SfxVolume
    } else {
        pitch = NORM_PITCH
        priority = NORM_PRIORITY
    }


    // Check to see if it is audible,
    //  and if not, modify the params
    if (origin != null && origin !== players[consoleplayer].mo) {
        // (vol, sep, pitch) out-params for S_AdjustSoundParams
        val vsp = intArrayOf(volume, 0, pitch)
        rc = S_AdjustSoundParams(players[consoleplayer].mo!!, origin, vsp)
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

    // hacks to vary the sfx pitches
    if (sfx_id >= sfx_sawup
        && sfx_id <= sfx_sawhit) {
        pitch += 8 - (M_Random() and 15)

        if (pitch < 0)
            pitch = 0
        else if (pitch > 255)
            pitch = 255
    } else if (sfx_id != sfx_itemup
        && sfx_id != sfx_tink) {
        pitch += 16 - (M_Random() and 31)

        if (pitch < 0)
            pitch = 0
        else if (pitch > 255)
            pitch = 255
    }

    // kill old sound
    S_StopSound(origin)

    // try to find a channel
    cnum = S_getChannel(origin, sfx)

    if (cnum < 0)
        return

    //
    // This is supposed to handle the loading/caching.
    // For some odd reason, the caching is done nearly
    //  each time the sound is needed?
    //

    // get lumpnum if necessary
    if (sfx.lumpnum < 0)
        sfx.lumpnum = I_GetSfxLumpNum(sfx)

    // (C: "cache data if necessary" block -- the actual caching lives in
    //  W_CacheLumpNum inside I_StartSound; sfxinfo_t carries no data field.)

    // increase the usefulness
    if (sfx.usefulness++ < 0)
        sfx.usefulness = 1

    // Assigns the handle to one of the channels in the
    //  mix/output buffer.
    channels[cnum].handle = I_StartSound(sfx_id,
        /*sfx->data,*/
        volume,
        sep,
        pitch,
        priority)
}

fun S_StartSound(origin: soundorigin_t?, sfx_id: Int) {
    S_StartSoundAtVolume(origin, sfx_id, snd_SfxVolume)

    // UNUSED. We had problems, had we not?
    // (#ifdef SAWDEBUG block not ported.)
}


fun S_StopSound(origin: soundorigin_t?) {
    var cnum: Int

    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo != null && channels[cnum].origin === origin) {
            S_StopChannel(cnum)
            break
        }
        cnum++
    }
}


//
// Stop and resume music, during game PAUSE.
//
fun S_PauseSound() {
    if (mus_playing != null && !mus_paused) {
        I_PauseSong(mus_playing!!.handle)
        mus_paused = true
    }
}

fun S_ResumeSound() {
    if (mus_playing != null && mus_paused) {
        I_ResumeSong(mus_playing!!.handle)
        mus_paused = false
    }
}


//
// Updates music & sounds
//
fun S_UpdateSounds(listener_p: mobj_t?) {
    var audible: Int
    var cnum: Int
    var volume: Int
    var sep: Int
    var pitch: Int

    val listener = listener_p


    // Clean up unused data.
    // This is currently not done for 16bit (sounds cached static).
    // DOS 8bit remains.
    /*if (gametic > nextcleanup)
    {
        for (i=1 ; i<NUMSFX ; i++)
        {
            if (S_sfx[i].usefulness < 1
                && S_sfx[i].usefulness > -1)
            {
                if (--S_sfx[i].usefulness == -1)
                {
                    Z_ChangeTag(S_sfx[i].data, PU_CACHE);
                    S_sfx[i].data = 0;
                }
            }
        }
        nextcleanup = gametic + 15;
    }*/

    cnum = 0
    while (cnum < numChannels) {
        val c = channels[cnum]
        val sfx = c.sfxinfo

        if (sfx != null) {
            if (I_SoundIsPlaying(c.handle)) {
                // initialize parameters
                volume = snd_SfxVolume
                pitch = NORM_PITCH
                sep = NORM_SEP

                if (sfx.link != -1) {
                    pitch = sfx.pitch
                    volume += sfx.volume
                    if (volume < 1) {
                        S_StopChannel(cnum)
                        cnum++
                        continue
                    } else if (volume > snd_SfxVolume) {
                        volume = snd_SfxVolume
                    }
                }

                // check non-local sounds for distance clipping
                //  or modify their params
                if (c.origin != null && listener_p !== c.origin) {
                    val vsp = intArrayOf(volume, sep, pitch)
                    audible = S_AdjustSoundParams(listener!!,
                        c.origin!!,
                        vsp)
                    volume = vsp[0]
                    sep = vsp[1]
                    pitch = vsp[2]

                    if (audible == 0) {
                        S_StopChannel(cnum)
                    } else
                        I_UpdateSoundParams(c.handle, volume, sep, pitch)
                }
            } else {
                // if channel is allocated but sound has stopped,
                //  free it
                S_StopChannel(cnum)
            }
        }
        cnum++
    }
    // kill music if it is a single-play && finished
    // if (	mus_playing
    //      && !I_QrySongPlaying(mus_playing->handle)
    //      && !mus_paused )
    // S_StopMusic();
}


fun S_SetMusicVolume(volume: Int) {
    if (volume < 0 || volume > 127) {
        I_Error("Attempt to set music volume at $volume")
    }

    I_SetMusicVolume(127)
    I_SetMusicVolume(volume)
    snd_MusicVolume = volume
}


fun S_SetSfxVolume(volume: Int) {
    if (volume < 0 || volume > 127)
        I_Error("Attempt to set sfx volume at $volume")

    snd_SfxVolume = volume
}

//
// Starts some music with the music id found in sounds.h.
//
fun S_StartMusic(m_id: Int) {
    S_ChangeMusic(m_id, 0)
}

fun S_ChangeMusic(musicnum: Int, looping: Int) {
    val music: musicinfo_t

    if ((musicnum <= mus_None)
        || (musicnum >= NUMMUSIC)) {
        I_Error("Bad music number $musicnum")
    } else
        music = S_music[musicnum]

    if (mus_playing === music)
        return

    // shutdown old music
    S_StopMusic()

    // get lumpnum if neccessary
    // (C tests !music->lumpnum; the Kotlin musicinfo_t uses -1 as the
    //  "not yet looked up" sentinel.)
    if (music.lumpnum == -1) {
        val namebuf = "d_${music.name}"
        music.lumpnum = W_GetNumForName(namebuf)
    }

    // load & register it
    music.data = W_CacheLumpNum(music.lumpnum)
    music.handle = I_RegisterSong(music.data!!)

    // play it
    I_PlaySong(music.handle, looping != 0)

    mus_playing = music
}


fun S_StopMusic() {
    if (mus_playing != null) {
        if (mus_paused)
            I_ResumeSong(mus_playing!!.handle)

        I_StopSong(mus_playing!!.handle)
        I_UnRegisterSong(mus_playing!!.handle)

        mus_playing!!.data = null
        mus_playing = null
    }
}


fun S_StopChannel(cnum: Int) {
    var i: Int
    val c = channels[cnum]

    val sfxinfo = c.sfxinfo
    if (sfxinfo != null) {
        // stop the sound playing
        if (I_SoundIsPlaying(c.handle)) {
            I_StopSound(c.handle)
        }

        // check to see
        //  if other channels are playing the sound
        i = 0
        while (i < numChannels) {
            if (cnum != i
                && sfxinfo === channels[i].sfxinfo) {
                break
            }
            i++
        }

        // degrade usefulness of sound data
        sfxinfo.usefulness--

        c.sfxinfo = null
    }
}


//
// Changes volume, stereo-separation, and pitch variables
//  from the norm of a sound effect to be played.
// If the sound is not audible, returns a 0.
// Otherwise, modifies parameters and returns 1.
//
// Out-params: vsp[0]=vol, vsp[1]=sep, vsp[2]=pitch (ports int* vol/sep/pitch).
//
fun S_AdjustSoundParams(listener: mobj_t, source: soundorigin_t, vsp: IntArray): Int {
    var approx_dist: fixed_t
    val adx: fixed_t
    val ady: fixed_t
    var angle: angle_t

    // calculate the distance to sound origin
    //  and clip it if necessary
    adx = abs(listener.x - source.x)
    ady = abs(listener.y - source.y)

    // From _GG1_ p.428. Appox. eucledian distance fast.
    approx_dist = adx + ady - ((if (adx < ady) adx else ady) shr 1)

    if (gamemap != 8
        && approx_dist > S_CLIPPING_DIST) {
        return 0
    }

    // angle of source to listener
    angle = R_PointToAngle2(listener.x,
        listener.y,
        source.x,
        source.y)

    if (angle > listener.angle)
        angle -= listener.angle
    else
        angle += (0xffffffffu - listener.angle)

    angle = angle shr ANGLETOFINESHIFT

    // stereo separation
    vsp[1] = 128 - (FixedMul(S_STEREO_SWING, finesine[angle.toInt()]) shr FRACBITS)

    // volume calculation
    if (approx_dist < S_CLOSE_DIST) {
        vsp[0] = snd_SfxVolume
    } else if (gamemap == 8) {
        if (approx_dist > S_CLIPPING_DIST)
            approx_dist = S_CLIPPING_DIST

        vsp[0] = 15 + ((snd_SfxVolume - 15)
                * ((S_CLIPPING_DIST - approx_dist) shr FRACBITS)) / S_ATTENUATOR
    } else {
        // distance effect
        vsp[0] = (snd_SfxVolume
                * ((S_CLIPPING_DIST - approx_dist) shr FRACBITS)) / S_ATTENUATOR
    }

    return if (vsp[0] > 0) 1 else 0
}


//
// S_getChannel :
//   If none available, return -1.  Otherwise channel #.
//
fun S_getChannel(origin: soundorigin_t?, sfxinfo: sfxinfo_t): Int {
    // channel number to use
    var cnum: Int

    val c: channel_t

    // Find an open channel
    cnum = 0
    while (cnum < numChannels) {
        if (channels[cnum].sfxinfo == null)
            break
        else if (origin != null && channels[cnum].origin === origin) {
            S_StopChannel(cnum)
            break
        }
        cnum++
    }

    // None available
    if (cnum == numChannels) {
        // Look for lower priority
        cnum = 0
        while (cnum < numChannels) {
            if (channels[cnum].sfxinfo!!.priority >= sfxinfo.priority) break
            cnum++
        }

        if (cnum == numChannels) {
            // FUCK!  No lower priority.  Sorry, Charlie.
            return -1
        } else {
            // Otherwise, kick out lower priority.
            S_StopChannel(cnum)
        }
    }

    c = channels[cnum]

    // channel is decided to be cnum.
    c.sfxinfo = sfxinfo
    c.origin = origin

    return cnum
}
