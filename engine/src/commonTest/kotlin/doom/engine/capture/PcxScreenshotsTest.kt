package doom.engine.capture

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PcxScreenshotsTest {
    @Test
    fun knownImagePreservesHeaderEscapesAndPaletteBytes() {
        val palette = ByteArray(768) { (it % 256).toByte() }
        val pcx = PcxEncoder().encode(byteArrayOf(0, 0xbf.toByte(), 0xc0.toByte(), 0xff.toByte()), 2, 2, palette)
        val expectedHeader = ByteArray(128).also {
            it[0] = 0x0a; it[1] = 5; it[2] = 1; it[3] = 8
            it[8] = 1; it[10] = 1; it[12] = 2; it[14] = 2
            it[65] = 1; it[66] = 2; it[68] = 2
        }
        assertContentEquals(expectedHeader, pcx.copyOfRange(0, 128))
        assertContentEquals(byteArrayOf(0, 0xbf.toByte(), 0xc1.toByte(), 0xc0.toByte(), 0xc1.toByte(), 0xff.toByte(), 0x0c), pcx.copyOfRange(128, 135))
        assertContentEquals(palette, pcx.copyOfRange(135, pcx.size))
    }

    @Test
    fun screenshotNamesBelongToOneSessionAndStopAtTheOriginalLimit() {
        val first = PcxScreenshots()
        val second = PcxScreenshots()
        val reject: (String) -> Nothing = { throw IllegalStateException(it) }
        repeat(100) { number ->
            assertEquals("DOOM${number.toString().padStart(2, '0')}.pcx", first.nextFileName(reject))
        }
        repeat(2) {
            val failure = assertFailsWith<IllegalStateException> { first.nextFileName(reject) }
            assertEquals("M_ScreenShot: Couldn't create a PCX", failure.message)
        }
        assertEquals("DOOM00.pcx", second.nextFileName(reject))
    }
}
