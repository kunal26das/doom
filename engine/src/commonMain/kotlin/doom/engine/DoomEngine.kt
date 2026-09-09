package doom.engine

import doom.engine.core.DoomEngineCore
import doom.engine.core.LegacyEngineRuntime
import doom.engine.runtime.EngineSession

public class DoomEngine(host: DoomHost = DoomHost()) {
    internal val core = DoomEngineCore(host.clock)
    private val session: EngineSession

    init {
        core.host.attach(host)
        val runtime = LegacyEngineRuntime(core)
        session = EngineSession(runtime, runtime, runtime, runtime, core.clock, core.host, core.quit)
    }

    public val isInitialized: Boolean get() = session.isInitialized
    public val quitRequested: Boolean get() = session.quitRequested
    public val metrics: DoomMetrics get() = session.metrics

    public fun boot(wads: List<ByteArray>, args: List<String> = emptyList()): Unit = session.boot(wads, args)
    public fun step(): Unit = session.step()
    public fun stepSingleTic(): Unit = session.step(singleTic = true)
    public fun postInput(input: DoomInput): Unit = session.post(input)
    public fun pause(): Unit = session.pause()
    public fun detach(): Unit = session.detach()
    public fun resume(host: DoomHost? = null): Unit = session.resume(host)
    public fun close(): Unit = session.close()
}
