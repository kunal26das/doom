package com.kunal26das.doom.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.kunal26das.doom.FrameBitmapConverter

internal class FramePresenter {
    private val converter = FrameBitmapConverter()
    private var lastImage: ImageBitmap? = null

    fun present(pixels: IntArray): ImageBitmap? {
        try {
            lastImage = converter.convert(pixels)
        } catch (_: RuntimeException) {
        }
        return lastImage
    }
}
