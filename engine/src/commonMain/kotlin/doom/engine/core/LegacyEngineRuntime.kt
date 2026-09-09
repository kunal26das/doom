package doom.engine.core

import doom.engine.DoomInput
import doom.engine.DoomJoystickInput
import doom.engine.DoomKeyInput
import doom.engine.DoomMetrics
import doom.engine.DoomMouseInput
import doom.engine.audio.sChangeMusic
import doom.engine.audio.sDetachHostAudio
import doom.engine.audio.sHostMusicId
import doom.engine.audio.sPauseSound
import doom.engine.audio.sResumeSound
import doom.engine.audio.sStopChannel
import doom.engine.audio.hostMusicLooping
import doom.engine.audio.numChannels
import doom.engine.configuration.mSaveDefaults
import doom.engine.gameplay.dclicks
import doom.engine.gameplay.dclicks2
import doom.engine.gameplay.dclickstate
import doom.engine.gameplay.dclickstate2
import doom.engine.gameplay.dclicktime
import doom.engine.gameplay.dclicktime2
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamekeydown
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamestate
import doom.engine.gameplay.gametic
import doom.engine.gameplay.joyarray
import doom.engine.gameplay.joyxmove
import doom.engine.gameplay.joyymove
import doom.engine.gameplay.mousearray
import doom.engine.gameplay.mousex
import doom.engine.gameplay.mousey
import doom.engine.gameplay.turnheld
import doom.engine.input.EngineEvent
import doom.engine.input.EV_JOYSTICK
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_KEYUP
import doom.engine.input.EV_MOUSE
import doom.engine.runtime.EngineAudioSession
import doom.engine.runtime.EngineExecution
import doom.engine.runtime.EngineInput
import doom.engine.runtime.EngineSettings
import doom.engine.runtime.MusicPlayback
import doom.engine.simulation.leveltime

internal class LegacyEngineRuntime(private val core: DoomEngineCore) :
    EngineExecution, EngineInput, EngineAudioSession, EngineSettings {

    override val metrics: DoomMetrics
        get() = with(core) { DoomMetrics(gametic, leveltime, gamestate, gameepisode, gamemap) }

    override fun boot(wads: List<ByteArray>, args: List<String>) = core.dDoomMain(wads, args)

    override fun step(singleTic: Boolean) {
        val previous = core.singletics
        if (singleTic) core.singletics = true
        try { core.dDoomStep() } finally { core.singletics = previous }
    }

    override fun post(event: DoomInput) {
        val legacyEvent = when (event) {
            is DoomKeyInput -> EngineEvent(if (event.pressed) EV_KEYDOWN else EV_KEYUP, event.code)
            is DoomMouseInput -> EngineEvent(EV_MOUSE, event.buttons, event.deltaX, event.deltaY)
            is DoomJoystickInput -> EngineEvent(EV_JOYSTICK, event.buttons, event.deltaX, event.deltaY)
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
        sPauseSound()
        for (channel in 0 until numChannels) sStopChannel(channel)
    }

    override fun currentMusic(): MusicPlayback? =
        core.sHostMusicId()?.let { MusicPlayback(it, core.hostMusicLooping) }

    override fun detach() = core.sDetachHostAudio()
    override fun resume(music: MusicPlayback?) = with(core) {
        music?.let { sChangeMusic(it.song, it.looping) }
        sResumeSound()
    }

    override fun save() = core.mSaveDefaults()
}
