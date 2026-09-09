package doom.engine.runtime

import doom.engine.DoomClock
import doom.engine.DoomError
import doom.engine.configuration.mValidateArguments
import doom.engine.configuration.myargv
import doom.engine.core.dProcessEvents
import doom.engine.core.DoomEngineCore
import doom.engine.core.iGetTime
import doom.engine.core.iPauseTime
import doom.engine.core.iResumeTime
import doom.engine.gameplay.GS_INTERMISSION
import doom.engine.gameplay.demoplayback
import doom.engine.gameplay.GA_NOTHING
import doom.engine.gameplay.gameaction
import doom.engine.gameplay.gamekeydown
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gamestate
import doom.engine.gameplay.SHAREWARE
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_KEYUP
import doom.engine.menu.menuactive
import doom.engine.rendering.iPostEvent
import doom.engine.rendering.iStartTic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class HostBoundaryTest {
    private val core = DoomEngineCore()
    @Test
    fun inputBurstsPreserveReleasesAcrossRingWraps() = with(core) {
        gamemode = SHAREWARE
        gamestate = GS_INTERMISSION
        demoplayback = false
        menuactive = false
        gameaction = GA_NOTHING
        for (count in listOf(64, 65, 128, 513)) {
            inputQueue.clear()
            gamekeydown.fill(false)
            gamekeydown['w'.code] = true
            iPostEvent(EngineEvent(EV_KEYUP, 'w'.code))
            repeat(count - 2) { iPostEvent(EngineEvent(EV_KEYUP, 'x'.code)) }
            iPostEvent(EngineEvent(EV_KEYDOWN, 'w'.code))
            iStartTic()
            dProcessEvents()
            assertTrue(gamekeydown['w'.code], "Last press must survive $count events")

            iPostEvent(EngineEvent(EV_KEYUP, 'w'.code))
            repeat(count - 1) { iPostEvent(EngineEvent(EV_KEYUP, 'x'.code)) }
            iStartTic()
            dProcessEvents()
            assertFalse(gamekeydown['w'.code], "Release must survive $count events")
            assertEquals(0, inputQueue.pendingCount)
        }
        gamekeydown.fill(false)
    }

    @Test
    fun pausesFreezeClockWithoutCatchUpAndAreIdempotent() {
        var ticks = 10
        with(DoomEngineCore(DoomClock { ticks })) {
            assertEquals(10, iGetTime())
            iPauseTime()
            ticks = 40
            iPauseTime()
            assertEquals(10, iGetTime())
            iResumeTime()
            iResumeTime()
            assertEquals(10, iGetTime())
            ticks = 45
            assertEquals(15, iGetTime())
            iPauseTime()
            ticks = 90
            assertEquals(15, iGetTime())
            iResumeTime()
            ticks = 91
            assertEquals(16, iGetTime())
        }
    }

    @Test
    fun malformedOptionsAreRejectedBeforeSubsystemStartup() = with(core) {
        val previous = myargv
        try {
            for (args in listOf(
                listOf("-warp", "1"), listOf("-warp", "1", ""),
                listOf("-skill", ""), listOf("-skill", "6"), listOf("-episode"),
                listOf("-loadgame", "-1"), listOf("-record", ""),
                listOf("-maxdemo", "2147483647"), listOf("-turbo", "fast"),
                listOf("-config", "-warp", "1", "1"),
            )) {
                myargv = listOf("doom") + args
                assertFailsWith<DoomError>(args.toString()) { mValidateArguments(false) }
            }
            myargv = listOf("doom", "-warp", "32")
            mValidateArguments(true)
            assertFailsWith<DoomError> { mValidateArguments(false) }
            myargv = listOf("doom", "-warp", "4", "9", "-skill", "5", "-turbo", "-nomonsters")
            mValidateArguments(false)
        } finally {
            myargv = previous
        }
    }
}
