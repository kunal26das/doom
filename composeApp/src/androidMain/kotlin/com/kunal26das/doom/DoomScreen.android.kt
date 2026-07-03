package com.kunal26das.doom

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH

private val bitmaps = Array(2) {
    Bitmap.createBitmap(SCREENWIDTH, SCREENHEIGHT, Bitmap.Config.ARGB_8888)
}
private var flip = 0

actual fun frameToImageBitmap(pixels: IntArray): ImageBitmap {
    flip = flip xor 1
    val bmp = bitmaps[flip]
    bmp.setPixels(pixels, 0, SCREENWIDTH, 0, 0, SCREENWIDTH, SCREENHEIGHT)
    return bmp.asImageBitmap()
}
