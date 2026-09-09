package doom.engine.runtime

import doom.engine.DoomClock
import doom.engine.DoomHost
import doom.engine.DoomMetrics
import doom.engine.DoomStorage
import doom.engine.DoomInput

internal class EngineSessionFixture {
    val calls = mutableListOf<String>()
    val failures = mutableMapOf<String, Throwable>()
    val onCall = mutableMapOf<String, () -> Unit>()
    val inputEvents = mutableListOf<DoomInput>()
    val steps = mutableListOf<Boolean>()
    val resumedMusic = mutableListOf<MusicPlayback?>()
    val metrics = DoomMetrics(1, 2, 3, 4, 5)
    var bootResources: List<ByteArray>? = null
    var bootArguments: List<String>? = null
    var music: MusicPlayback? = null
    var ticks = 10
    val clock = EngineClock(DoomClock { ticks })
    val quit = QuitSignal()
    val host = HostAttachment(DoomHost(storage = object : DoomStorage {
        override fun read(name: String): ByteArray? = null
        override fun write(name: String, data: ByteArray) {}
    }))

    private fun record(operation: String) {
        calls += operation
        onCall[operation]?.invoke()
        failures[operation]?.let { throw it }
    }

    private val execution = object : EngineExecution {
        override val metrics: DoomMetrics get() = this@EngineSessionFixture.metrics
        override fun boot(wads: List<ByteArray>, args: List<String>) {
            bootResources = wads
            bootArguments = args
            record("boot")
        }
        override fun step(singleTic: Boolean) { record("step"); steps += singleTic }
    }
    private val input = object : EngineInput {
        override fun post(event: DoomInput) { record("postInput"); inputEvents += event }
        override fun clear() = record("clearInput")
        override fun close() = record("closeInput")
    }
    private val audio = object : EngineAudioSession {
        override fun pause() = record("pauseAudio")
        override fun currentMusic(): MusicPlayback? { record("currentMusic"); return music }
        override fun detach() { try { record("detachAudio") } finally { music = null } }
        override fun resume(music: MusicPlayback?) {
            resumedMusic += music
            record("resumeAudio")
            if (music != null) this@EngineSessionFixture.music = music
        }
    }
    val session = EngineSession(execution, input, audio, EngineSettings { record("save") }, clock, host, quit)
    fun boot() = session.boot(emptyList(), emptyList())
}
