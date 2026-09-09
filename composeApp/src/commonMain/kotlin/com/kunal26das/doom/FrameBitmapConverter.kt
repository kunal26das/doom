package com.kunal26das.doom

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Uploads 320x200 ARGB frames using buffers owned by one presentation instance.
 * Call only from that screen's game/UI thread. Returned images may reuse this
 * converter's buffers; another screen must own a separate converter.
 */
expect class FrameBitmapConverter() {
    fun convert(pixels: IntArray): ImageBitmap
}
