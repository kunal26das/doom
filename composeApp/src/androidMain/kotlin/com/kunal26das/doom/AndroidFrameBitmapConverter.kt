package com.kunal26das.doom

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH

actual fun FrameBitmapConverter(): FrameBitmapConverter = AndroidFrameBitmapConverter()

private class AndroidFrameBitmapConverter : FrameBitmapConverter {
    private val bitmaps = Array(2) {
        Bitmap.createBitmap(SCREENWIDTH, SCREENHEIGHT, Bitmap.Config.ARGB_8888)
    }
    private var flip = 0

    override fun convert(pixels: IntArray): ImageBitmap {
        require(pixels.size == SCREENWIDTH * SCREENHEIGHT) { "Unexpected game frame size" }
        flip = flip xor 1
        val bmp = bitmaps[flip]
        bmp.setPixels(pixels, 0, SCREENWIDTH, 0, 0, SCREENWIDTH, SCREENHEIGHT)
        return bmp.asImageBitmap()
    }
}
