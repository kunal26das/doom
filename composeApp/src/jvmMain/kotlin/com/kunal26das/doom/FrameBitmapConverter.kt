package com.kunal26das.doom

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

actual class FrameBitmapConverter actual constructor() {
    private val info = ImageInfo(SCREENWIDTH, SCREENHEIGHT, ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
    // Publishing alternating bitmaps avoids reusing the immediately preceding image.
    private val bitmaps = Array(2) { Bitmap().apply { allocPixels(info) } }
    private val bytes = ByteArray(SCREENWIDTH * SCREENHEIGHT * 4)
    private var flip = 0

    actual fun convert(pixels: IntArray): ImageBitmap {
        require(pixels.size == SCREENWIDTH * SCREENHEIGHT) { "Unexpected game frame size" }
        var o = 0
        for (v in pixels) {
            bytes[o++] = (v and 0xFF).toByte()          // B
            bytes[o++] = ((v shr 8) and 0xFF).toByte()  // G
            bytes[o++] = ((v shr 16) and 0xFF).toByte() // R
            bytes[o++] = ((v shr 24) and 0xFF).toByte() // A
        }
        flip = flip xor 1
        val bmp = bitmaps[flip]
        bmp.installPixels(info, bytes, SCREENWIDTH * 4)
        return bmp.asComposeImageBitmap()
    }
}
