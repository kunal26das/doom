package doom.engine.rendering

import doom.engine.DoomVideo

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PaletteVideoOutputTest {
    @Test
    fun paletteOffsetsGammaCorrectionAndUnsignedIndicesProduceOpaqueArgb() {
        val frames = mutableListOf<IntArray>()
        val output = PaletteVideoOutput(DoomVideo { frames += it.copyOf() }, pixelCount = 4)
        val paletteOffset = 13
        val palette = ByteArray(paletteOffset + 768) { 42 }
        fun color(index: Int, red: Int, green: Int, blue: Int) {
            palette[paletteOffset + index * 3] = red.toByte()
            palette[paletteOffset + index * 3 + 1] = green.toByte()
            palette[paletteOffset + index * 3 + 2] = blue.toByte()
        }
        color(0, 0, 127, 255)
        color(127, 128, 64, 32)
        color(128, 255, 128, 1)
        color(255, 16, 200, 0)

        output.setPalette(palette, paletteOffset, IntArray(256) { 255 - it })
        output.present(byteArrayOf(0, 127, -128, -1))

        assertContentEquals(
            intArrayOf(0xffff8000.toInt(), 0xff7fbfdf.toInt(), 0xff007ffe.toInt(), 0xffef37ff.toInt()),
            frames.single(),
        )
    }

    @Test
    fun aLaterPaletteReplacesColorsWithoutSharingAnotherOutputsPaletteOrFrame() {
        val firstFrames = mutableListOf<IntArray>()
        val secondFrames = mutableListOf<IntArray>()
        val first = PaletteVideoOutput(DoomVideo { firstFrames += it.copyOf() }, pixelCount = 1)
        val second = PaletteVideoOutput(DoomVideo { secondFrames += it.copyOf() }, pixelCount = 1)
        val gamma = IntArray(256) { it }
        val firstPalette = ByteArray(768) { 16 }
        first.setPalette(firstPalette, 0, gamma)
        second.setPalette(ByteArray(768) { 32 }, 0, gamma)
        firstPalette.fill(99)
        first.present(byteArrayOf(-1))
        second.present(byteArrayOf(-1))
        second.setPalette(ByteArray(768) { 48 }, 0, gamma)
        second.present(byteArrayOf(-1))
        first.present(byteArrayOf(-1))

        assertEquals(2, firstFrames.size)
        assertContentEquals(intArrayOf(0xff101010.toInt()), firstFrames[0])
        assertContentEquals(firstFrames[0], firstFrames[1])
        assertContentEquals(intArrayOf(0xff202020.toInt()), secondFrames[0])
        assertContentEquals(intArrayOf(0xff303030.toInt()), secondFrames[1])
    }
}
