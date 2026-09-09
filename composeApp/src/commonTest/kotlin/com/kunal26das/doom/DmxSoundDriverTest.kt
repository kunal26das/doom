package com.kunal26das.doom

import com.kunal26das.doom.data.audio.DmxSoundDriver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DmxSoundDriverTest {
    @Test
    fun mixesPcmSamplesWithoutIncludingDmxPadding() {
        val driver = DmxSoundDriver()
        val handle = driver.startSound(1, dmx(255, 128, 0), 127, 0, 128, 0)
        val output = FloatArray(8)

        driver.render(output)

        assertEquals(127f / 128f, output[0])
        assertEquals(0f, output[2])
        assertEquals(-1f, output[4])
        assertEquals(0f, output[6])
        for (index in 1 until output.size step 2) assertEquals(0f, output[index])
        assertFalse(driver.soundIsPlaying(handle))
    }

    @Test
    fun resamplesAtTheRateInTheDmxHeader() {
        val driver = DmxSoundDriver()
        driver.startSound(1, dmx(255, 0, rate = OUTPUT_RATE / 2), 127, 0, 128, 0)
        val output = FloatArray(8)

        driver.render(output)

        assertEquals(127f / 128f, output[0])
        assertEquals(output[0], output[2])
        assertEquals(-1f, output[4])
        assertEquals(output[4], output[6])
    }

    @Test
    fun rejectsMalformedDmxWithoutOpeningAChannel() {
        val invalid = listOf(
            ByteArray(24),
            dmx(255).also { it[0] = 2 },
            dmx(255, rate = 0),
            dmx(),
            dmx(255).also { it[4] = 100 },
            dmx(255).also { it[7] = 0x80.toByte() },
        )
        for (data in invalid) {
            val driver = DmxSoundDriver()
            assertEquals(-1, driver.startSound(1, data, 127, 0, 128, 0))
            val output = FloatArray(2) { 1f }
            driver.render(output)
            assertTrue(output.all { it == 0f })
        }
    }

    @Test
    fun stoppedSoundProducesSilence() {
        val driver = DmxSoundDriver()
        val handle = driver.startSound(1, dmx(255), 127, 0, 128, 0)
        assertTrue(driver.soundIsPlaying(handle))

        driver.stopSound(handle)
        val output = FloatArray(2)
        driver.render(output)

        assertFalse(driver.soundIsPlaying(handle))
        assertTrue(output.all { it == 0f })
    }

    @Test
    fun stealsTheOldestChannelWhenAllChannelsAreOccupied() {
        val driver = DmxSoundDriver()
        val handles = List(DmxSoundDriver.NUM_CHANNELS + 1) { id ->
            driver.startSound(id, dmx(255), 127, 0, 128, 0)
        }

        assertFalse(driver.soundIsPlaying(handles.first()))
        handles.drop(1).forEach { assertTrue(driver.soundIsPlaying(it)) }
    }

    @Test
    fun requiresCompleteStereoFrames() {
        val driver = DmxSoundDriver()
        assertFailsWith<IllegalArgumentException> { driver.render(FloatArray(3)) }
        driver.render(FloatArray(0))
    }

    private fun dmx(vararg samples: Int, rate: Int = OUTPUT_RATE): ByteArray {
        val length = samples.size + 32
        return ByteArray(length + 8).also { bytes ->
            bytes[0] = 3
            bytes[2] = rate.toByte()
            bytes[3] = (rate shr 8).toByte()
            repeat(4) { bytes[4 + it] = (length shr (it * 8)).toByte() }
            for (index in 8 until bytes.size) bytes[index] = 255.toByte()
            samples.forEachIndexed { index, sample -> bytes[24 + index] = sample.toByte() }
        }
    }
}
