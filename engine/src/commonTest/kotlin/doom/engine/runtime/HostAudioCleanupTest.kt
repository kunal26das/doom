package doom.engine.runtime

import doom.engine.DoomHost
import doom.engine.ISoundDriver
import doom.engine.audio.MusicTrack
import doom.engine.audio.sDetachHostAudio
import doom.engine.core.DoomEngineCore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HostAudioCleanupTest {
    @Test
    fun repeatedDriverFailureKeepsItsIdentityAndStillClearsEveryMusicHandle() {
        val failure = IllegalStateException("audio device unavailable")
        val operations = mutableListOf<String>()
        val driver = object : ISoundDriver {
            override fun startSound(id: Int, data: ByteArray, vol: Int, sep: Int, pitch: Int, priority: Int) = -1
            override fun stopSound(handle: Int) {}
            override fun soundIsPlaying(handle: Int) = false
            override fun updateSoundParams(handle: Int, vol: Int, sep: Int, pitch: Int) {}
            override fun setMusicVolume(volume: Int) {}
            override fun registerSong(data: ByteArray) = 0
            override fun playSong(handle: Int, looping: Boolean) {}
            override fun pauseSong(handle: Int) {}
            override fun resumeSong(handle: Int) {}
            override fun stopSong(handle: Int) { operations += "stop:$handle"; throw failure }
            override fun unregisterSong(handle: Int) { operations += "unregister:$handle"; throw failure }
        }
        val core = DoomEngineCore()
        val song = MusicTrack("test").apply { handle = 42; data = byteArrayOf(1, 2, 3) }
        core.stateSoundPlayback.musPlaying = song
        core.stateSoundPlayback.musPaused = true
        core.host.attach(DoomHost(sound = driver))

        assertSame(failure, assertFails { core.sDetachHostAudio() })
        assertEquals(listOf("stop:42", "unregister:42"), operations)
        assertTrue(failure.suppressedExceptions.isEmpty())
        assertNull(song.data)
        assertEquals(0, song.handle)
        assertNull(core.stateSoundPlayback.musPlaying)
        assertFalse(core.stateSoundPlayback.musPaused)
        core.sDetachHostAudio()
        assertEquals(2, operations.size)
    }
}
