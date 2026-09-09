
package doom.engine.audio

internal class SoundPlaybackState {
    var hostMusicLooping = 0
    val sndPrefixen by lazy(LazyThreadSafetyMode.NONE) { charArrayOf(
        'P', 'P', 'A', 'S', 'S', 'S', 'M', 'M', 'M', 'S', 'S', 'S') }

    var channels: Array<SoundChannel> = emptyArray()

    var sndSfxVolume = 15

    var sndMusicVolume = 15

    var musPaused = false

    var musPlaying: MusicTrack? = null

    var numChannels = 0

    var nextcleanup = 0
}
