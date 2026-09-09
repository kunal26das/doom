package com.kunal26das.doom.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.kunal26das.doom.FrameBitmapConverter

/** Preserve the last good image when a platform upload briefly fails. */
internal class FramePresenter {
    private val converter = FrameBitmapConverter()
    private var lastImage: ImageBitmap? = null

    fun present(pixels: IntArray): ImageBitmap? {
        try {
            lastImage = converter.convert(pixels)
        } catch (_: RuntimeException) {
            // A transient graphics failure should drop a frame, not the game session.
        }
        return lastImage
    }
}
