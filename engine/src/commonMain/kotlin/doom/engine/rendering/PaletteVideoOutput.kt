package doom.engine.rendering

import doom.engine.DoomVideo
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH

internal class PaletteVideoOutput(
    private val video: DoomVideo,
    pixelCount: Int = SCREENWIDTH * SCREENHEIGHT,
) {
    private val palette = IntArray(256)
    private val frame = IntArray(pixelCount)
    private var selectedGamma = 0
    val gamma: Int get() = selectedGamma

    fun setGamma(value: Int) { selectedGamma = value }

    fun setPalette(rgb: ByteArray, offset: Int, gammaTable: IntArray) {
        for (index in palette.indices) {
            val source = offset + index * 3
            val red = gammaTable[rgb[source].toInt() and 0xff]
            val green = gammaTable[rgb[source + 1].toInt() and 0xff]
            val blue = gammaTable[rgb[source + 2].toInt() and 0xff]
            palette[index] = (0xff shl 24) or (red shl 16) or (green shl 8) or blue
        }
    }

    fun present(indexedPixels: ByteArray) {
        for (index in frame.indices) {
            frame[index] = palette[indexedPixels[index].toInt() and 0xff]
        }
        video.present(frame)
    }
}
