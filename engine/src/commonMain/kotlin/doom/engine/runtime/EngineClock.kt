package doom.engine.runtime

import doom.engine.DoomClock
import doom.engine.TICRATE
import kotlin.time.TimeSource

/** One immutable source and epoch for the session, excluding suspended time. */
internal class EngineClock(source: DoomClock? = null) : DoomClock {
    private val source = source ?: run {
        val boot = TimeSource.Monotonic.markNow()
        DoomClock { ((boot.elapsedNow().inWholeMilliseconds * TICRATE) / 1000).toInt() }
    }
    private var pausedAt: Int? = null
    private var pausedTics = 0

    override fun ticks(): Int = (pausedAt ?: source.ticks()) - pausedTics
    fun pause() { if (pausedAt == null) pausedAt = source.ticks() }
    fun resume() {
        val start = pausedAt ?: return
        pausedTics += source.ticks() - start
        pausedAt = null
    }
}
