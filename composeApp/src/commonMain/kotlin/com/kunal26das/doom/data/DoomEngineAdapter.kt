package com.kunal26das.doom.data

import com.kunal26das.doom.domain.GameKeyInput
import com.kunal26das.doom.domain.GameMouseInput

import com.kunal26das.doom.AudioOutput
import com.kunal26das.doom.data.audio.AudioMixer
import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.GameEngine
import com.kunal26das.doom.domain.GameFrame
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.startAudioOutput
import doom.engine.*

/**
 * Adapts the independent engine to the application's ports. One adapter owns one
 * game; lifecycle suspension detaches its host and a later start resumes it.
 * All calls run on the game thread; only the mixer runs on an audio thread.
 */
class DoomEngineAdapter(
    private val files: FileRepository,
    private val createMixer: () -> AudioMixer,
    private val clock: DoomClock? = null,
    private val createEngine: (DoomHost) -> DoomEngine = ::DoomEngine,
    private val openAudio: ((FloatArray) -> Unit) -> AudioOutput? = ::startAudioOutput,
) : GameEngine {
    private var engine: DoomEngine? = null
    private var audio: AudioOutput? = null
    private var attached = false
    private var started = false
    private var failed = false
    override val quitRequested: Boolean get() = engine?.quitRequested == true

    override fun start(wads: List<ByteArray>, onFrame: (GameFrame) -> Unit) {
        check(!attached) { "Game session is already attached" }
        check(!failed && !quitRequested) { "This game has ended; choose a game to start again" }
        attached = true
        try {
            val mixer = createMixer()
            val host = DoomHost(
                clock = clock,
                storage = object : DoomStorage {
                    override fun read(name: String): ByteArray? = files.read(name)
                    override fun write(name: String, data: ByteArray) = files.write(name, data)
                },
                video = DoomVideo { onFrame(GameFrame(SCREENWIDTH, SCREENHEIGHT, it)) },
                sound = mixer,
            )
            val current = engine
            if (current == null) {
                createEngine(host).also { engine = it }.boot(wads)
            } else {
                current.resume(host)
            }
            // Audio is optional. Platform implementations report unavailability as null.
            audio = openAudio(mixer::render)
            started = true
        } catch (failure: Throwable) {
            failed = true
            try { close() } catch (cleanup: Throwable) {
                if (cleanup !== failure) failure.addSuppressed(cleanup)
            }
            throw failure
        }
    }

    override fun step() {
        check(started && !failed) { "Game session has not started" }
        try { checkNotNull(engine).step() } catch (failure: Throwable) {
            failed = true
            throw failure
        }
    }

    override fun postInput(input: GameInput) {
        if (!started || failed || quitRequested) return
        val event = when (input) {
            is GameKeyInput -> if (input.code in 1..255) {
                DoomKeyInput(input.code, input.pressed)
            } else return
            is GameMouseInput -> DoomMouseInput(input.deltaX, input.deltaY)
        }
        engine?.postInput(event)
    }

    override fun close() {
        if (!attached) return
        attached = false
        started = false
        var failure: Throwable? = null
        try { engine?.detach() } catch (error: Throwable) { failure = error }
        try { audio?.close() } catch (error: Throwable) {
            if (failure == null) failure = error else if (failure !== error) failure.addSuppressed(error)
        } finally { audio = null }
        failure?.let { throw it }
    }
}
