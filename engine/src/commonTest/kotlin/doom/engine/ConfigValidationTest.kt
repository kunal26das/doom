package doom.engine

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
        assertEquals(KEY_UPARROW, key_up)
        assertEquals(KEY_DOWNARROW, key_down)
        assertEquals(0, mousebfire)
        assertEquals(3, joybuse)
        assertEquals(9, screenblocks)
        assertEquals(0, detailLevel)
        assertEquals(3, numChannels)
        assertEquals(8, snd_SfxVolume)
        assertEquals(8, snd_MusicVolume)
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
        assertEquals(255, key_up)
        assertEquals(-1, mousebfire)
        assertEquals(-1, joybuse)
        assertEquals(11, screenblocks)
        assertEquals(1, detailLevel)
        assertEquals(0, numChannels)
        assertEquals(15, snd_SfxVolume)
        assertEquals(9, mouseSensitivity)
        assertEquals("hello world", chat_macros[0])
        var saved = byteArrayOf()
        host.attach(DoomHost(storage = object : DoomStorage {
            override fun read(name: String): ByteArray = saved
            override fun write(name: String, data: ByteArray) { saved = data }
        }))
        M_SaveDefaults()
        M_LoadDefaults()
        assertEquals(-1, mousebfire)
        assertEquals(4, usegamma)
        assertEquals("hello world", chat_macros[0])
    }

    private fun withConfig(text: String, assertions: DoomEngineCore.() -> Unit) = with(core) {
        val previousArgs = myargv
        try {
            myargv = listOf("doom")
            host.attach(DoomHost(storage = object : DoomStorage {
                override fun read(name: String): ByteArray = text.encodeToByteArray()
                override fun write(name: String, data: ByteArray) {}
            }))
            M_LoadDefaults()
            assertions()
        } finally {
            host.detach()
            M_LoadDefaults()
            myargv = previousArgs
        }
    }
}
