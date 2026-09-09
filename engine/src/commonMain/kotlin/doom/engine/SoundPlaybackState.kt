// Port of linuxdoom-1.10 s_sound.c -- high-level sound/music handling
// (channel allocation, distance attenuation, per-level music selection).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING",
    "SENSELESS_COMPARISON", "KotlinConstantConditions", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class SoundPlaybackState {
    var hostMusicLooping = 0
    val snd_prefixen by lazy(LazyThreadSafetyMode.NONE) { charArrayOf(
        'P', 'P', 'A', 'S', 'S', 'S', 'M', 'M', 'M', 'S', 'S', 'S') }

    var channels: Array<channel_t> = emptyArray()

    var snd_SfxVolume = 15

    var snd_MusicVolume = 15

    var mus_paused = false

    var mus_playing: musicinfo_t? = null

    var numChannels = 0

    var nextcleanup = 0
}
