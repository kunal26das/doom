package com.kunal26das.doom

import androidx.compose.ui.graphics.ImageBitmap

interface FrameBitmapConverter {
    fun convert(pixels: IntArray): ImageBitmap
}

expect fun FrameBitmapConverter(): FrameBitmapConverter
