package doom.engine.runtime

internal interface EngineAudioSession {
    fun pause()
    fun currentMusic(): MusicPlayback?
    fun detach()
    fun resume(music: MusicPlayback?)
}
