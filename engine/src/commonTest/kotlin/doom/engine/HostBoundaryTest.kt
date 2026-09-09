package doom.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class HostBoundaryTest {
    private val core = DoomEngineCore()
    @Test
    fun inputBurstsPreserveReleasesAcrossRingWraps() = with(core) {
        gamemode = shareware
        gamestate = GS_INTERMISSION
        demoplayback = false
        menuactive = false
        gameaction = ga_nothing
        for (count in listOf(64, 65, 128, 513)) {
            inputQueue.clear()
            gamekeydown.fill(false)
            gamekeydown['w'.code] = true
            I_PostEvent(event_t(ev_keyup, 'w'.code))
            repeat(count - 2) { I_PostEvent(event_t(ev_keyup, 'x'.code)) }
            I_PostEvent(event_t(ev_keydown, 'w'.code))
            I_StartTic()
            D_ProcessEvents()
            assertTrue(gamekeydown['w'.code], "Last press must survive $count events")

            I_PostEvent(event_t(ev_keyup, 'w'.code))
            repeat(count - 1) { I_PostEvent(event_t(ev_keyup, 'x'.code)) }
            I_StartTic()
            D_ProcessEvents()
            assertFalse(gamekeydown['w'.code], "Release must survive $count events")
            assertEquals(0, inputQueue.pendingCount)
        }
        gamekeydown.fill(false)
    }

    @Test
    fun pausesFreezeClockWithoutCatchUpAndAreIdempotent() {
        var ticks = 10
        with(DoomEngineCore(DoomClock { ticks })) {
            assertEquals(10, I_GetTime())
            I_PauseTime()
            ticks = 40
            I_PauseTime()
            assertEquals(10, I_GetTime())
            I_ResumeTime()
            I_ResumeTime()
            assertEquals(10, I_GetTime())
            ticks = 45
            assertEquals(15, I_GetTime())
            I_PauseTime()
            ticks = 90
            assertEquals(15, I_GetTime())
            I_ResumeTime()
            ticks = 91
            assertEquals(16, I_GetTime())
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
                assertFailsWith<DoomError>(args.toString()) { M_ValidateArguments(false) }
            }
            myargv = listOf("doom", "-warp", "32")
            M_ValidateArguments(true)
            assertFailsWith<DoomError> { M_ValidateArguments(false) }
            myargv = listOf("doom", "-warp", "4", "9", "-skill", "5", "-turbo", "-nomonsters")
            M_ValidateArguments(false)
        } finally {
            myargv = previous
        }
    }
}
