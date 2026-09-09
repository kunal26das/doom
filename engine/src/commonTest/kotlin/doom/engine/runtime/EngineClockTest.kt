package doom.engine.runtime

import doom.engine.DoomClock
import kotlin.test.Test
import kotlin.test.assertEquals

class EngineClockTest {
    @Test
    fun repeatedPauseResumeExcludesBackgroundTimeWithoutChangingItsSource() {
        var raw = 10
        var reads = 0
        val clock = EngineClock(DoomClock { reads++; raw })
        assertEquals(10, clock.ticks())
        clock.pause()
        val readsWhenPaused = reads
        raw = 100
        clock.pause()
        assertEquals(10, clock.ticks())
        assertEquals(readsWhenPaused, reads, "A paused clock does not sample its source")
        clock.resume()
        clock.resume()
        assertEquals(10, clock.ticks())
        raw = 105
        assertEquals(15, clock.ticks())
        clock.pause()
        raw = 200
        assertEquals(15, clock.ticks())
        clock.resume()
        raw = 201
        assertEquals(16, clock.ticks())
    }
}
