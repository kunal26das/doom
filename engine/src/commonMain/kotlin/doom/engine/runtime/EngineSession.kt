package doom.engine.runtime

import doom.engine.DoomHost
import doom.engine.DoomInput
import doom.engine.DoomMetrics

import kotlin.concurrent.Volatile

internal class EngineSession(
    private val execution: EngineExecution,
    private val input: EngineInput,
    private val audio: EngineAudioSession,
    private val settings: EngineSettings,
    private val clock: EngineClock,
    private val host: HostAttachment,
    private val quit: QuitSignal,
) {
    @Volatile private var phase = EngineSessionPhase.CREATED
    @Volatile private var attachment = EngineAttachmentState.ACTIVE
    private var pendingMusic: MusicPlayback? = null
    var isInitialized = false
        private set

    val metrics: DoomMetrics get() = execution.metrics
    val quitRequested: Boolean get() = quit.requested

    fun boot(wads: List<ByteArray>, args: List<String>) {
        check(phase == EngineSessionPhase.CREATED) { "Each DoomEngine boots once; create a new engine for another game" }
        phase = EngineSessionPhase.BOOTING
        guarded {
            execution.boot(wads, args)
            isInitialized = true
            phase = EngineSessionPhase.INITIALIZED
        }
    }

    fun step(singleTic: Boolean = false) {
        check(phase == EngineSessionPhase.INITIALIZED) { "Engine is not running" }
        if (attachment == EngineAttachmentState.ACTIVE && !quitRequested) guarded { execution.step(singleTic) }
    }

    fun post(event: DoomInput) {
        if (phase != EngineSessionPhase.CLOSED && phase != EngineSessionPhase.FAILED && attachment == EngineAttachmentState.ACTIVE && !quitRequested)
            input.post(event)
    }

    fun pause() {
        if (phase == EngineSessionPhase.CLOSED || attachment != EngineAttachmentState.ACTIVE) return
        attachment = EngineAttachmentState.PAUSED
        clock.pause()
        input.clear()
        if (isInitialized && phase != EngineSessionPhase.FAILED) audio.pause()
    }

    fun detach() {
        if (phase == EngineSessionPhase.CLOSED || attachment == EngineAttachmentState.DETACHED) return
        val cleanup = EngineCleanup()
        cleanup.attempt { pause() }
        if (isInitialized && phase != EngineSessionPhase.FAILED) {
            cleanup.attempt { pendingMusic = audio.currentMusic() }
            cleanup.attempt { settings.save() }
            cleanup.attempt { audio.detach() }
        }
        attachment = EngineAttachmentState.DETACHED
        cleanup.attempt { input.clear() }
        host.detach()
        cleanup.throwIfFailed()
    }

    fun resume(replacement: DoomHost? = null) {
        check(phase != EngineSessionPhase.CLOSED && phase != EngineSessionPhase.FAILED) { "Engine is disposed or failed" }
        guarded {
            if (replacement != null) {
                if (attachment != EngineAttachmentState.DETACHED) detach()
                host.attach(replacement)
            }
            if (attachment != EngineAttachmentState.ACTIVE) {
                input.clear()
                clock.resume()
                attachment = EngineAttachmentState.ACTIVE
                if (isInitialized) audio.resume(pendingMusic)
                pendingMusic = null
            }
        }
    }

    fun close() {
        if (phase == EngineSessionPhase.CLOSED) return
        val cleanup = EngineCleanup()
        cleanup.attempt { detach() }
        phase = EngineSessionPhase.CLOSED
        cleanup.attempt { input.close() }
        cleanup.throwIfFailed()
    }

    private inline fun guarded(action: () -> Unit) {
        try { action() } catch (failure: Throwable) {
            phase = EngineSessionPhase.FAILED
            val cleanup = EngineCleanup(failure)
            cleanup.attempt { pause() }
            cleanup.attempt { audio.detach() }
            cleanup.attempt { input.clear() }
            host.detach()
            attachment = EngineAttachmentState.DETACHED
            throw failure
        }
    }
}
