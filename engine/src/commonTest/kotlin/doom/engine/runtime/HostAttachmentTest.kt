package doom.engine.runtime

import doom.engine.DoomHost
import doom.engine.DoomVideo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HostAttachmentTest {
    @Test
    fun retainedCapabilityPortsFollowAttachmentAndBecomeHarmlessAfterDetach() {
        val first = HostAttachmentRecordingHost(7)
        val second = HostAttachmentRecordingHost(9)
        val host = HostAttachment(first.ports())
        val effects = host.soundEffects
        val music = host.music
        val samples = byteArrayOf(1, 2, 3)
        val frame = intArrayOf(10, 20)
        assertFalse(host.isDetached)
        assertSame(first.bytes, host.read("config"))
        host.write("save", samples)
        host.present(frame)
        assertSame(samples, first.writtenBytes)
        assertSame(frame, first.presentedFrame, "Video remains a borrowed frame, without a hidden copy")
        assertEquals(7, effects.startSound(3, samples, 4, 5, 6, 8))
        assertTrue(effects.soundIsPlaying(7))
        effects.updateSoundParams(7, 1, 2, 3)
        effects.stopSound(7)
        music.setMusicVolume(4)
        assertEquals(7, music.registerSong(samples))
        music.playSong(7, true)
        music.pauseSong(7)
        music.resumeSong(7)
        music.stopSong(7)
        music.unregisterSong(7)
        assertEquals(listOf(
            "read:config", "write:save", "present", "start:3:4:5:6:8", "playing:7",
            "update:7:1:2:3", "stop:7", "volume:4", "register", "playSong:7:true",
            "pauseSong:7", "resumeSong:7", "stopSong:7", "unregisterSong:7",
        ), first.calls)
        val oldCalls = first.calls.toList()

        host.detach()
        host.detach()
        assertTrue(host.isDetached)
        assertNull(host.read("config"))
        host.write("save", samples)
        host.present(frame)
        assertEquals(-1, effects.startSound(3, samples, 4, 5, 6, 8))
        assertFalse(effects.soundIsPlaying(7))
        effects.updateSoundParams(7, 1, 2, 3)
        effects.stopSound(7)
        music.setMusicVolume(4)
        assertEquals(0, music.registerSong(samples))
        music.playSong(7, true)
        music.pauseSong(7)
        music.resumeSong(7)
        music.stopSong(7)
        music.unregisterSong(7)
        assertEquals(oldCalls, first.calls)

        host.attach(second.ports())
        assertFalse(host.isDetached)
        assertSame(second.bytes, host.read("config"))
        assertEquals(9, effects.startSound(1, samples, 2, 3, 4, 5))
        assertEquals(9, music.registerSong(samples))
        host.present(frame)
        assertSame(frame, second.presentedFrame)
        assertEquals(oldCalls, first.calls, "No operation may reach the previous host after replacement")
    }

    @Test
    fun hostFailureKeepsItsIdentityForLifecycleCleanupPolicy() {
        val failure = IllegalStateException("video host failed")
        val host = HostAttachment(DoomHost(video = DoomVideo { throw failure }))
        assertSame(failure, assertFails { host.present(intArrayOf(1)) })
        host.detach()
        host.present(intArrayOf(2))
        assertTrue(host.isDetached)
    }

}
