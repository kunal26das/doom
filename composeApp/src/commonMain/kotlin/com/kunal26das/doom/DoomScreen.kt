package com.kunal26das.doom

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Uploads a 320x200 ARGB frame into an ImageBitmap.
 * Reuses/allocates platform bitmaps as each implementation sees fit;
 * called once per rendered frame from the game loop thread.
 */
expect fun frameToImageBitmap(pixels: IntArray): ImageBitmap
