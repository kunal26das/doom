package doom.engine

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
        val song = musicinfo_t("test").apply { handle = 42; data = byteArrayOf(1, 2, 3) }
        core.stateSoundPlayback.mus_playing = song
        core.stateSoundPlayback.mus_paused = true
        core.host.attach(DoomHost(sound = driver))

        assertSame(failure, assertFails { core.S_DetachHostAudio() })
        assertEquals(listOf("stop:42", "unregister:42"), operations)
        assertTrue(failure.suppressedExceptions.isEmpty())
        assertNull(song.data)
        assertEquals(0, song.handle)
        assertNull(core.stateSoundPlayback.mus_playing)
        assertFalse(core.stateSoundPlayback.mus_paused)
        core.S_DetachHostAudio()
        assertEquals(2, operations.size)
    }
}
