package doom.engine.configuration

import doom.engine.DoomHost
import doom.engine.DoomStorage
import doom.engine.KEY_DOWNARROW
import doom.engine.KEY_UPARROW
import doom.engine.audio.numChannels
import doom.engine.audio.sndMusicVolume
import doom.engine.audio.sndSfxVolume
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.joybuse
import doom.engine.gameplay.keyDown
import doom.engine.gameplay.keyUp
import doom.engine.gameplay.mousebfire
import doom.engine.hud.chatMacros
import doom.engine.menu.detailLevel
import doom.engine.menu.mouseSensitivity
import doom.engine.menu.screenblocks
import doom.engine.rendering.usegamma

import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigValidationTest {
    private val core = DoomEngineCore()
    @Test
    fun invalidPersistedIndexesAndAllocationSizesKeepDefaults() = withConfig("""
        usegamma 5
        key_up 999
        key_down -1
        mouseb_fire 3
        joyb_use 4
        screenblocks 0
        detaillevel 7
        snd_channels 2147483647
        sfx_volume -8
        music_volume 999
        mouse_sensitivity -1
    """.trimIndent()) {
        assertEquals(0, usegamma)
        assertEquals(KEY_UPARROW, keyUp)
        assertEquals(KEY_DOWNARROW, keyDown)
        assertEquals(0, mousebfire)
        assertEquals(3, joybuse)
        assertEquals(9, screenblocks)
        assertEquals(0, detailLevel)
        assertEquals(3, numChannels)
        assertEquals(8, sndSfxVolume)
        assertEquals(8, sndMusicVolume)
        assertEquals(5, mouseSensitivity)
    }

    @Test
    fun validLimitsAndDisabledButtonsRoundTrip() = withConfig("""
        usegamma 4
        key_up 255
        mouseb_fire -1
        joyb_use -1
        screenblocks 11
        detaillevel 1
        snd_channels 0
        sfx_volume 15
        mouse_sensitivity 9
        chatmacro0 "hello world"
    """.trimIndent()) {
        assertEquals(4, usegamma)
        assertEquals(255, keyUp)
        assertEquals(-1, mousebfire)
        assertEquals(-1, joybuse)
        assertEquals(11, screenblocks)
        assertEquals(1, detailLevel)
        assertEquals(0, numChannels)
        assertEquals(15, sndSfxVolume)
        assertEquals(9, mouseSensitivity)
        assertEquals("hello world", chatMacros[0])
        var saved = byteArrayOf()
        host.attach(DoomHost(storage = object : DoomStorage {
            override fun read(name: String): ByteArray = saved
            override fun write(name: String, data: ByteArray) { saved = data }
        }))
        mSaveDefaults()
        mLoadDefaults()
        assertEquals(-1, mousebfire)
        assertEquals(4, usegamma)
        assertEquals("hello world", chatMacros[0])
    }

    private fun withConfig(text: String, assertions: DoomEngineCore.() -> Unit) = with(core) {
        val previousArgs = myargv
        try {
            myargv = listOf("doom")
            host.attach(DoomHost(storage = object : DoomStorage {
                override fun read(name: String): ByteArray = text.encodeToByteArray()
                override fun write(name: String, data: ByteArray) {}
            }))
            mLoadDefaults()
            assertions()
        } finally {
            host.detach()
            mLoadDefaults()
            myargv = previousArgs
        }
    }
}
