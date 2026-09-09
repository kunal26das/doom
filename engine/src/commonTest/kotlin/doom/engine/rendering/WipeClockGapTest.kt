package doom.engine.rendering

import doom.engine.DoomClock
import doom.engine.DoomHost
import doom.engine.DoomVideo
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.dDoomStep
import doom.engine.core.DoomEngineCore
import doom.engine.core.wipeActive

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WipeClockGapTest {
    @Test
    fun backwardAndRepeatedSamplesKeepTheWipePendingAndLaterFramesContinue() {
        var now = 10
        var frames = 0
        val core = DoomEngineCore(DoomClock { now })
        core.host.attach(DoomHost(video = DoomVideo { frames++ }))
        core.frameBuffers[0].fill(1)
        core.wipeStartScreen()
        core.frameBuffers[0].fill(2)
        core.wipeEndScreen()
        core.stateGameLoop.wipeActive = true
        core.stateGameLoop.wipestart = now
        val initial = core.frameBuffers[0].copyOf()

        core.dDoomStep()
        now = 0
        core.dDoomStep()
        assertTrue(core.wipeActive)
        assertEquals(0, frames)
        assertContentEquals(initial, core.frameBuffers[0])

        now = 1
        core.dDoomStep()
        assertEquals(1, frames, "Rebase a backward clock sample instead of waiting for the old epoch")
        assertTrue(core.wipeActive)

        now = Int.MAX_VALUE - 1
        core.dDoomStep()
        assertEquals(2, frames)
        assertContentEquals(ByteArray(SCREENWIDTH * SCREENHEIGHT) { 2 }, core.frameBuffers[0])
        assertTrue(core.wipeActive, "A call that changed pixels retains vanilla's completion delay")
        now++
        core.dDoomStep()
        assertEquals(3, frames)
        assertFalse(core.wipeActive)
    }
}
