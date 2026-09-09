package doom.engine.integration

import doom.engine.DoomClock
import doom.engine.DoomEngine
import doom.engine.DoomError
import doom.engine.DoomHost
import doom.engine.DoomStorage
import doom.engine.DoomVideo
import doom.engine.TICRATE
import doom.engine.audio.sHostMusicId
import doom.engine.audio.sSfx
import doom.engine.core.dPostEvent
import doom.engine.core.DoomEngineCore
import doom.engine.core.iGetTime
import doom.engine.gameplay.actors.mobjinfo
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.forwardmove
import doom.engine.gameplay.nodrawers
import doom.engine.hud.chatMacros
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.menu.mainMenu
import doom.engine.regression.DemoVerificationProcess
import doom.engine.simulation.pRandom
import doom.engine.simulation.prndindex

import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstanceIsolationTest {
    private val wad: ByteArray get() = File(requireNotNull(System.getProperty("doom.test.wad"))).readBytes()

    @Test
    fun mutableDefinitionsConfigInputAndRandomStreamsBelongToTheirEngine() {
        val first = DoomEngineCore()
        val second = DoomEngineCore()
        assertNotSame(first.states, second.states)
        assertNotSame(first.states[0], second.states[0])
        assertNotSame(first.mobjinfo[0], second.mobjinfo[0])
        assertNotSame(first.sSfx[1], second.sSfx[1])
        assertNotSame(first.mainMenu, second.mainMenu)
        val originalTics = second.states[0].tics
        first.states[0].tics = 789
        first.forwardmove[0] = 777
        first.sSfx[1].lumpnum = 45
        first.chatMacros[0] = "first engine"
        first.pRandom()
        first.dPostEvent(EngineEvent(EV_KEYDOWN, 'w'.code))
        assertEquals(originalTics, second.states[0].tics)
        assertEquals(0x19, second.forwardmove[0])
        assertEquals(-1, second.sSfx[1].lumpnum)
        assertEquals(0, second.prndindex)
        assertEquals(0, second.inputQueue.pendingCount)
        assertTrue(second.chatMacros[0] != "first engine")
    }

    @Test
    fun allThreeDemosCanRunInterleavedInOneProcessWithOriginalHashes() {
        val expected = listOf(
            1711 to "9cd19bf3230c9a3ad59a819edb8d406d88956f613ca9415eda7e1a395a4c0dc2",
            2348 to "c6dd6098e7a5cea5ba4c83cdb92d08f0b87ee31f257ea1012e04024b700d8790",
            3864 to "854727b2cd9c6206c4ea6e2d2825bbfede71371f0be3241c940be65e8769f79b",
        )
        val clocks = IntArray(3)
        val engines = List(3) { index -> DoomEngine(DoomHost(clock = DoomClock { clocks[index] })) }
        val digests = List(3) { MessageDigest.getInstance("SHA-256") }
        try {
            engines.forEachIndexed { index, engine ->
                engine.boot(listOf(wad), listOf("-playdemo", "DEMO${index + 1}"))
                engine.core.nodrawers = true
            }
            repeat(6000) {
                engines.forEachIndexed { index, engine ->
                    if (!engine.quitRequested) {
                        clocks[index]++
                        engine.stepSingleTic()
                        if (engine.metrics.gametic % TICRATE == 0 || engine.quitRequested)
                            with(DemoVerificationProcess) { digests[index].checkpoint(engine.core) }
                    }
                }
            }
            engines.forEachIndexed { index, engine ->
                assertTrue(engine.quitRequested, "DEMO${index + 1} end marker")
                assertEquals(expected[index].first, engine.metrics.gametic)
                val hash = digests[index].digest().joinToString("") { "%02x".format(it) }
                assertEquals(expected[index].second, hash, "Interleaved DEMO${index + 1}")
            }
        } finally { engines.forEach { it.close() } }
    }

    @Test
    fun aFailedEngineDoesNotPreventFreshBootInTheSameProcess() {
        val broken = DoomEngine()
        assertFailsWith<DoomError> { broken.boot(listOf(byteArrayOf(1, 2, 3))) }
        broken.close()
        var ticks = 0
        val fresh = DoomEngine(DoomHost(clock = DoomClock { ticks }))
        try {
            fresh.boot(listOf(wad), listOf("-warp", "1", "1"))
            fresh.core.nodrawers = true
            repeat(120) { ticks++; fresh.stepSingleTic() }
            assertEquals(120, fresh.metrics.gametic)
            assertEquals(1, fresh.metrics.episode)
            assertEquals(1, fresh.metrics.map)
        } finally { fresh.close() }
    }

    @Test
    fun failedPersistenceStillDropsHostAndResumesMusicWithNewDriver() {
        var ticks = 0
        val oldSound = InstanceIsolationRecordingSound()
        val storage = object : DoomStorage {
            override fun read(name: String): ByteArray? = null
            override fun write(name: String, data: ByteArray) { error("storage unavailable") }
        }
        val engine = DoomEngine(DoomHost(DoomClock { ticks }, storage, DoomVideo {}, oldSound))
        try {
            engine.boot(listOf(wad), listOf("-warp", "1", "1"))
            val before = engine.metrics
            val error = assertFailsWith<IllegalStateException> { engine.detach() }
            assertEquals("storage unavailable", error.message)
            assertTrue(engine.core.host.isDetached)
            assertNull(engine.core.sHostMusicId())
            engine.detach()
            ticks = 1000
            val replacement = InstanceIsolationRecordingSound()
            engine.resume(DoomHost(sound = replacement))
            assertEquals(before, engine.metrics)
            assertEquals(0, engine.core.iGetTime())
            assertEquals(1, replacement.songsPlayed)
            assertTrue(oldSound.songsStopped > 0)
        } finally { engine.close() }
    }

    @Test
    fun throwingAudioDuringPauseStillReleasesEveryHostReference() {
        val sound = InstanceIsolationRecordingSound()
        val engine = DoomEngine(DoomHost(sound = sound, video = DoomVideo {}))
        try {
            engine.boot(listOf(wad), listOf("-warp", "1", "1"))
            sound.failPause = true
            assertFailsWith<IllegalStateException> { engine.detach() }
            assertTrue(engine.core.host.isDetached)
            assertNull(engine.core.sHostMusicId())
            assertTrue(sound.songsStopped > 0)
        } finally { engine.close() }
    }

}
