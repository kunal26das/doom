package com.kunal26das.doom.data

import com.kunal26das.doom.domain.GameKeyInput

import com.kunal26das.doom.AudioOutput
import com.kunal26das.doom.data.audio.DmxSoundDriver
import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.GameInput
import doom.engine.*
import java.io.File
import kotlin.test.*

/** Real engines share this JVM: process isolation must not hide leaked game state. */
class DoomEngineAdapterIntegrationTest {
    private val wad by lazy { File(checkNotNull(System.getProperty("doom.test.wad"))).readBytes() }

    @Test
    fun independentAdaptersRenderAndSuspendWithoutAffectingEachOther() {
        val first = DoomEngineAdapterFixture()
        val second = DoomEngineAdapterFixture()
        try {
            first.start(wad)
            second.start(wad)
            first.advance(120)
            second.advance(120)
            assertTrue(first.frames > 0 && second.frames > 0)
            val firstTic = first.engine.metrics.gametic
            val secondTic = second.engine.metrics.gametic
            first.adapter.close()
            first.adapter.close()
            assertEquals(1, first.closed)
            assertTrue(first.files.writes.isNotEmpty())
            first.clock += 3500
            first.engine.step()
            assertEquals(firstTic, first.engine.metrics.gametic)
            second.advance(3)
            assertTrue(second.engine.metrics.gametic > secondTic)
            first.adapter.start(emptyList()) { first.frames++ }
            assertEquals(firstTic, first.engine.metrics.gametic)
            first.advance(1)
            assertTrue(first.engine.metrics.gametic > firstTic)
            assertTrue(first.engine.metrics.gametic <= firstTic + 2, "Background time was counted")
            assertEquals(2, first.opened)
        } finally { first.adapter.close(); second.adapter.close() }
    }

    @Test
    fun quitThroughRealInputAllowsAFreshGameInTheSameProcess() {
        val first = DoomEngineAdapterFixture()
        try {
            first.start(wad)
            first.advance(120)
            first.adapter.postInput(GameKeyInput(KEY_F10, true))
            first.advance(1)
            first.adapter.postInput(GameKeyInput(KEY_F10, false))
            first.adapter.postInput(GameKeyInput('y'.code, true))
            first.advance(1)
            assertTrue(first.adapter.quitRequested)
            first.adapter.close()
            assertFailsWith<IllegalStateException> { first.start(wad) }
            val fresh = DoomEngineAdapterFixture()
            try { fresh.start(wad); fresh.advance(120); assertFalse(fresh.adapter.quitRequested); assertTrue(fresh.frames > 0) }
            finally { fresh.adapter.close() }
        } finally { first.adapter.close() }
    }

    @Test
    fun invalidWadDoesNotPoisonAnotherGame() {
        val broken = DoomEngineAdapterFixture()
        assertFails { broken.start(byteArrayOf(0)) }
        broken.adapter.close()
        assertEquals(0, broken.opened)
        assertFailsWith<IllegalStateException> { broken.start(wad) }
        val fresh = DoomEngineAdapterFixture()
        try { fresh.start(wad); fresh.advance(120); assertTrue(fresh.frames > 0) }
        finally { fresh.adapter.close() }
    }

    @Test
    fun storageAndAudioShutdownFailuresStillCloseExactlyOnce() {
        val storageFailure = IllegalStateException("Settings could not be written")
        val audioFailure = IllegalArgumentException("Audio did not shut down")
        val files = object : FileRepository {
            override fun read(name: String): ByteArray? = null
            override fun write(name: String, data: ByteArray) { throw storageFailure }
        }
        var closed = 0
        val adapter = DoomEngineAdapter(files, ::DmxSoundDriver) {
            object : AudioOutput { override fun close() { closed++; throw audioFailure } }
        }
        adapter.start(listOf(wad)) {}
        val failure = assertFails { adapter.close() }
        assertSame(storageFailure, failure)
        assertTrue(failure.suppressedExceptions.any { it === audioFailure })
        adapter.close()
        assertEquals(1, closed)
        val fresh = DoomEngineAdapterFixture()
        try { fresh.start(wad); fresh.advance(120); assertTrue(fresh.frames > 0) }
        finally { fresh.adapter.close() }
    }

    @Test
    fun audioStartupFailureReleasesHostAndDoesNotPoisonAnotherGame() {
        val audioFailure = IllegalStateException("Audio startup failed")
        val files = DoomEngineAdapterMemoryFiles()
        val adapter = DoomEngineAdapter(files, ::DmxSoundDriver) { throw audioFailure }
        assertSame(audioFailure, assertFails { adapter.start(listOf(wad)) {} })
        assertTrue(files.writes.isNotEmpty())
        adapter.close()
        assertFailsWith<IllegalStateException> { adapter.start(listOf(wad)) {} }
        val fresh = DoomEngineAdapterFixture()
        try { fresh.start(wad); fresh.advance(120); assertTrue(fresh.frames > 0) }
        finally { fresh.adapter.close() }
    }

    @Test
    fun eachAttachmentUsesItsInjectedMixerAndConnectsItsRenderCallback() {
        val mixers = mutableListOf<DoomEngineAdapterRecordingMixer>()
        val renderers = mutableListOf<(FloatArray) -> Unit>()
        var enginesCreated = 0
        var audioClosed = 0
        val adapter = DoomEngineAdapter(
            files = DoomEngineAdapterMemoryFiles(),
            createMixer = { DoomEngineAdapterRecordingMixer((mixers.size + 1) / 10f).also { mixers += it } },
            clock = DoomClock { 0 },
            createEngine = { host ->
                enginesCreated++
                assertSame(mixers.last(), host.sound)
                DoomEngine(host)
            },
        ) { render ->
            renderers += render
            object : AudioOutput { override fun close() { audioClosed++ } }
        }
        try {
            adapter.start(listOf(wad)) {}
            assertEquals(1, mixers.size)
            assertFailsWith<IllegalStateException> { adapter.start(listOf(wad)) {} }
            assertEquals(1, mixers.size, "A duplicate start must not allocate another mixer")
            val firstBuffer = FloatArray(4)
            renderers.single()(firstBuffer)
            assertSame(firstBuffer, mixers[0].lastRendered)
            assertContentEquals(FloatArray(4) { 0.1f }, firstBuffer)

            adapter.close()
            assertEquals(1, audioClosed)
            adapter.start(emptyList()) {}
            assertEquals(2, mixers.size)
            assertNotSame(mixers[0], mixers[1])
            assertEquals(1, enginesCreated, "Resume keeps the simulation while replacing attachment resources")
            val secondBuffer = FloatArray(4)
            renderers[1](secondBuffer)
            assertSame(secondBuffer, mixers[1].lastRendered)
            assertSame(firstBuffer, mixers[0].lastRendered)
            assertContentEquals(FloatArray(4) { 0.2f }, secondBuffer)
        } finally { adapter.close() }
        assertEquals(2, audioClosed)
    }

    @Test
    fun mixerFactoryFailureIsTerminalBeforeOpeningAnEngineOrAudioDevice() {
        val failure = IllegalStateException("Mixer could not be created")
        var attempts = 0
        val files = DoomEngineAdapterMemoryFiles()
        val adapter = DoomEngineAdapter(
            files = files,
            createMixer = { attempts++; throw failure },
            createEngine = { error("An engine must not open after mixer creation fails") },
            openAudio = { error("An audio device must not open after mixer creation fails") },
        )
        assertSame(failure, assertFails { adapter.start(emptyList()) {} })
        adapter.close()
        adapter.close()
        assertFailsWith<IllegalStateException> { adapter.start(emptyList()) {} }
        assertEquals(1, attempts)
        assertTrue(files.writes.isEmpty())
    }

}
