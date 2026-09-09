package com.kunal26das.doom.data

import com.kunal26das.doom.AudioOutput
import com.kunal26das.doom.data.audio.DmxSoundDriver
import doom.engine.DoomClock
import doom.engine.DoomEngine
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import kotlin.test.assertEquals

internal class DoomEngineAdapterFixture {
    var clock = 100
    var opened = 0
    var closed = 0
    var frames = 0
    val files = DoomEngineAdapterMemoryFiles()
    lateinit var engine: DoomEngine
    val adapter = DoomEngineAdapter(
        files, ::DmxSoundDriver, DoomClock { clock }, { host -> DoomEngine(host).also { engine = it } },
    ) {
        opened++
        object : AudioOutput { override fun close() { closed++ } }
    }
    fun start(wad: ByteArray) = adapter.start(listOf(wad)) { frame ->
        assertEquals(SCREENWIDTH, frame.width)
        assertEquals(SCREENHEIGHT, frame.height)
        assertEquals(SCREENWIDTH * SCREENHEIGHT, frame.pixels.size)
        frames++
    }
    fun advance(count: Int) { repeat(count) { clock++; adapter.step() } }
}
