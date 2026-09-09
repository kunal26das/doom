package doom.engine

import doom.engine.runtime.EngineAudioSession
import doom.engine.runtime.EngineExecution
import doom.engine.runtime.EngineInput
import doom.engine.runtime.EngineSettings
import doom.engine.runtime.MusicPlayback

/** Adapts the original algorithms to the independent session contracts. */
internal class LegacyEngineRuntime(private val core: DoomEngineCore) :
    EngineExecution, EngineInput, EngineAudioSession, EngineSettings {

    override val metrics: DoomMetrics
        get() = with(core) { DoomMetrics(gametic, leveltime, gamestate, gameepisode, gamemap) }

    override fun boot(wads: List<ByteArray>, args: List<String>) = core.D_DoomMain(wads, args)

    override fun step(singleTic: Boolean) {
        val previous = core.singletics
        if (singleTic) core.singletics = true
        try { core.D_DoomStep() } finally { core.singletics = previous }
    }

    override fun post(event: DoomInput) {
        val legacyEvent = when (event) {
            is DoomKeyInput -> event_t(if (event.pressed) ev_keydown else ev_keyup, event.code)
            is DoomMouseInput -> event_t(ev_mouse, event.buttons, event.deltaX, event.deltaY)
            is DoomJoystickInput -> event_t(ev_joystick, event.buttons, event.deltaX, event.deltaY)
        }
        core.inputQueue.post(legacyEvent)
    }
    override fun close() = core.inputQueue.close()
    override fun clear() = with(core) {
        inputQueue.clear()
        gamekeydown.fill(false)
        mousearray.fill(false)
        joyarray.fill(false)
        mousex = 0; mousey = 0
        joyxmove = 0; joyymove = 0
        turnheld = 0
        dclicktime = 0; dclickstate = 0; dclicks = 0
        dclicktime2 = 0; dclickstate2 = 0; dclicks2 = 0
    }

    override fun pause() = with(core) {
        S_PauseSound()
        for (channel in 0 until numChannels) S_StopChannel(channel)
    }

    override fun currentMusic(): MusicPlayback? =
        core.S_HostMusicId()?.let { MusicPlayback(it, core.hostMusicLooping) }

    override fun detach() = core.S_DetachHostAudio()
    override fun resume(music: MusicPlayback?) = with(core) {
        music?.let { S_ChangeMusic(it.song, it.looping) }
        S_ResumeSound()
    }

    override fun save() = core.M_SaveDefaults()
}
