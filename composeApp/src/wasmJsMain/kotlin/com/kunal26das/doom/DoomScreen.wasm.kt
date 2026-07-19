package com.kunal26das.doom

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

private val info = ImageInfo(SCREENWIDTH, SCREENHEIGHT, ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
private val bitmaps = Array(2) { Bitmap().apply { allocPixels(info) } }
private val bytes = ByteArray(SCREENWIDTH * SCREENHEIGHT * 4)
private var flip = 0

actual fun frameToImageBitmap(pixels: IntArray): ImageBitmap {
    var o = 0
    for (v in pixels) {
        bytes[o++] = (v and 0xFF).toByte()
        bytes[o++] = ((v shr 8) and 0xFF).toByte()
        bytes[o++] = ((v shr 16) and 0xFF).toByte()
        bytes[o++] = ((v shr 24) and 0xFF).toByte()
    }
    flip = flip xor 1
    val bmp = bitmaps[flip]
    bmp.installPixels(info, bytes, SCREENWIDTH * 4)
    noteFramePresented()
    return bmp.asComposeImageBitmap()
}
