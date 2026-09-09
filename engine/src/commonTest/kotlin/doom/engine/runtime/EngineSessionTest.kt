package doom.engine.runtime

import doom.engine.DoomClock
import doom.engine.DoomHost
import doom.engine.DoomKeyInput
import doom.engine.DoomStorage
import doom.engine.DoomVideo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EngineSessionTest {
    @Test
    fun bootIsSingleUseAndOnlyInitializedSessionsCanAdvance() {
        val fixture = EngineSessionFixture()
        assertFailsWith<IllegalStateException> { fixture.session.step() }
        assertFalse(fixture.session.isInitialized)
        val resources = listOf(byteArrayOf(1, 2))
        val arguments = listOf("-skill", "3")
        fixture.session.boot(resources, arguments)
        assertTrue(fixture.session.isInitialized)
        assertSame(resources, fixture.bootResources)
        assertSame(arguments, fixture.bootArguments)
        assertSame(fixture.metrics, fixture.session.metrics)
        fixture.session.step()
        fixture.session.step(singleTic = true)
        assertEquals(listOf(false, true), fixture.steps)
        assertFailsWith<IllegalStateException> { fixture.boot() }
        fixture.session.close()
        assertFailsWith<IllegalStateException> { fixture.boot() }
        assertFailsWith<IllegalStateException> { fixture.session.step() }
        assertEquals(1, fixture.calls.count { it == "boot" })
    }

    @Test
    fun pauseResumeQuitAndCloseGateInputAndExecution() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        val event = DoomKeyInput(10, true)
        fixture.session.post(event)
        fixture.session.step()
        fixture.session.pause()
        fixture.session.pause()
        fixture.session.post(event)
        fixture.session.step()
        assertEquals(1, fixture.inputEvents.size)
        assertEquals(1, fixture.steps.size)
        assertEquals(1, fixture.calls.count { it == "pauseAudio" })

        fixture.session.resume()
        fixture.session.post(event)
        fixture.session.step()
        assertEquals(2, fixture.inputEvents.size)
        assertEquals(2, fixture.steps.size)
        fixture.quit.request()
        assertTrue(fixture.session.quitRequested)
        fixture.session.post(event)
        fixture.session.step()
        assertEquals(2, fixture.inputEvents.size)
        assertEquals(2, fixture.steps.size)
        fixture.session.close()
        fixture.session.close()
        fixture.session.post(event)
        assertEquals(2, fixture.inputEvents.size)
        assertEquals(1, fixture.calls.count { it == "closeInput" })
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
    }

    @Test
    fun detachPreservesMusicAndClockEpochAcrossRepeatedCallsAndAReplacementHost() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        fixture.music = MusicPlayback(12, 1)
        fixture.session.detach()
        val callsAfterDetach = fixture.calls.toList()
        fixture.session.detach()
        assertEquals(callsAfterDetach, fixture.calls)
        assertTrue(fixture.host.isDetached)
        fixture.ticks = 1000
        assertEquals(10, fixture.clock.ticks())
        fixture.session.step()
        fixture.session.post(DoomKeyInput(1, true))
        assertTrue(fixture.steps.isEmpty())
        assertTrue(fixture.inputEvents.isEmpty())

        fixture.session.resume(DoomHost(
            clock = DoomClock { error("Replacement hosts cannot replace the session clock") },
            video = DoomVideo {},
        ))
        assertFalse(fixture.host.isDetached)
        assertEquals(listOf<MusicPlayback?>(MusicPlayback(12, 1)), fixture.resumedMusic)
        assertEquals(10, fixture.clock.ticks())
        fixture.ticks++
        assertEquals(11, fixture.clock.ticks())
        fixture.session.pause()
        fixture.session.resume()
        assertEquals(listOf(MusicPlayback(12, 1), null), fixture.resumedMusic)
    }

    @Test
    fun replacingAnActiveHostReleasesItsAudioBeforeBindingTheReplacement() = assertHostReplacement(paused = false)

    @Test
    fun replacingAPausedHostReleasesItsAudioBeforeBindingTheReplacement() = assertHostReplacement(paused = true)

    private fun assertHostReplacement(paused: Boolean) {
        val fixture = EngineSessionFixture()
        val oldBytes = byteArrayOf(1)
        val replacementBytes = byteArrayOf(2)
        fun storage(bytes: ByteArray) = object : DoomStorage {
            override fun read(name: String): ByteArray = bytes
            override fun write(name: String, data: ByteArray) {}
        }
        fixture.host.attach(DoomHost(storage = storage(oldBytes)))
        fixture.boot()
        fixture.music = MusicPlayback(17, 1)
        if (paused) {
            fixture.session.pause()
            fixture.ticks = 500
        }
        fixture.onCall["detachAudio"] = {
            assertSame(oldBytes, fixture.host.read("probe"), "Old driver cleanup must still reach the old host")
            fixture.ticks = 1000
        }
        fixture.onCall["resumeAudio"] = {
            assertSame(replacementBytes, fixture.host.read("probe"), "Music restoration must reach the replacement host")
        }

        fixture.session.resume(DoomHost(
            clock = DoomClock { error("A replacement host cannot change the clock") },
            storage = storage(replacementBytes),
        ))

        assertEquals(1, fixture.calls.count { it == "pauseAudio" })
        assertEquals(1, fixture.calls.count { it == "save" })
        assertEquals(1, fixture.calls.count { it == "detachAudio" })
        assertEquals(listOf<MusicPlayback?>(MusicPlayback(17, 1)), fixture.resumedMusic)
        assertEquals(10, fixture.clock.ticks())
        fixture.ticks++
        assertEquals(11, fixture.clock.ticks())
        fixture.session.step()
        assertEquals(1, fixture.steps.size)
        fixture.session.resume()
        assertEquals(1, fixture.calls.count { it == "detachAudio" })
        assertEquals(1, fixture.resumedMusic.size)
    }

    @Test
    fun failedOldHostCleanupDuringReplacementIsTerminalAndNeverStartsNewAudio() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        val failure = IllegalStateException("Old host settings could not be saved")
        fixture.failures["save"] = failure

        assertSame(failure, assertFails { fixture.session.resume(DoomHost(video = DoomVideo {})) })
        assertTrue(fixture.host.isDetached)
        assertTrue(fixture.resumedMusic.isEmpty())
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
        assertFailsWith<IllegalStateException> { fixture.session.step() }
    }

    @Test
    fun detachContinuesCleanupAfterAudioAndSettingsFailuresAndRetainsPendingMusic() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        fixture.music = MusicPlayback(3, 0)
        val pauseFailure = IllegalStateException("pause failed")
        val saveFailure = IllegalArgumentException("save failed")
        val detachFailure = IllegalStateException("detach failed")
        fixture.failures["pauseAudio"] = pauseFailure
        fixture.failures["save"] = saveFailure
        fixture.failures["detachAudio"] = detachFailure

        assertSame(pauseFailure, assertFails { fixture.session.detach() })
        assertEquals(listOf(saveFailure, detachFailure), pauseFailure.suppressedExceptions)
        assertTrue(fixture.host.isDetached)
        assertTrue("clearInput" in fixture.calls)
        val callsAfterDetach = fixture.calls.toList()
        fixture.session.detach()
        assertEquals(callsAfterDetach, fixture.calls)

        fixture.failures.clear()
        fixture.session.resume(DoomHost(video = DoomVideo {}))
        assertEquals(listOf<MusicPlayback?>(MusicPlayback(3, 0)), fixture.resumedMusic)
        fixture.session.step()
        assertEquals(1, fixture.steps.size)
    }

    @Test
    fun failedBootIsTerminalAndReleasesPartiallyStartedResources() {
        val fixture = EngineSessionFixture()
        val failure = IllegalStateException("invalid resources")
        fixture.failures["boot"] = failure
        assertSame(failure, assertFails { fixture.boot() })
        assertFalse(fixture.session.isInitialized)
        assertTrue(fixture.host.isDetached)
        assertTrue("detachAudio" in fixture.calls)
        assertFalse("save" in fixture.calls)
        fixture.session.post(DoomKeyInput(1, true))
        assertTrue(fixture.inputEvents.isEmpty())
        assertFailsWith<IllegalStateException> { fixture.boot() }
        assertFailsWith<IllegalStateException> { fixture.session.step() }
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
        fixture.session.close()
        fixture.session.close()
        assertEquals(1, fixture.calls.count { it == "closeInput" })
    }

    @Test
    fun executionFailureRemainsPrimaryWhenMultipleCleanupCapabilitiesFail() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        val failure = IllegalStateException("simulation failed")
        val inputFailure = IllegalArgumentException("input cleanup failed")
        val audioFailure = IllegalStateException("audio cleanup failed")
        fixture.failures["step"] = failure
        fixture.failures["clearInput"] = inputFailure
        fixture.failures["detachAudio"] = audioFailure

        assertSame(failure, assertFails { fixture.session.step() })
        assertTrue(failure.suppressedExceptions.any { it === inputFailure })
        assertTrue(failure.suppressedExceptions.any { it === audioFailure })
        assertTrue(fixture.host.isDetached)
        fixture.session.post(DoomKeyInput(1, true))
        assertTrue(fixture.inputEvents.isEmpty())
        assertFailsWith<IllegalStateException> { fixture.session.step() }
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
    }

    @Test
    fun failedAudioResumeBecomesTerminalAndReleasesTheReplacementHost() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        fixture.music = MusicPlayback(9, 1)
        fixture.session.detach()
        val failure = IllegalStateException("replacement audio failed")
        val cleanupFailure = IllegalStateException("replacement audio cleanup failed")
        fixture.failures["resumeAudio"] = failure
        fixture.failures["detachAudio"] = cleanupFailure

        assertSame(failure, assertFails { fixture.session.resume(DoomHost(video = DoomVideo {})) })
        assertEquals(listOf(cleanupFailure), failure.suppressedExceptions)
        assertTrue(fixture.host.isDetached)
        assertEquals(listOf<MusicPlayback?>(MusicPlayback(9, 1)), fixture.resumedMusic)
        assertFailsWith<IllegalStateException> { fixture.session.step() }
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
        fixture.session.post(DoomKeyInput(1, true))
        assertTrue(fixture.inputEvents.isEmpty())
        fixture.session.close()
        assertEquals(1, fixture.calls.count { it == "closeInput" })
    }

    @Test
    fun closePreservesDetachFailureAndSuppressesInputCloseFailureOnlyOnce() {
        val fixture = EngineSessionFixture()
        fixture.boot()
        val saveFailure = IllegalStateException("settings could not be saved")
        val closeFailure = IllegalArgumentException("input could not be closed")
        fixture.failures["save"] = saveFailure
        fixture.failures["closeInput"] = closeFailure

        assertSame(saveFailure, assertFails { fixture.session.close() })
        assertEquals(listOf(closeFailure), saveFailure.suppressedExceptions)
        assertTrue(fixture.host.isDetached)
        fixture.session.close()
        assertEquals(1, fixture.calls.count { it == "closeInput" })
        assertFailsWith<IllegalStateException> { fixture.session.resume() }
    }

}
