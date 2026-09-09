package com.kunal26das.doom.domain

class GameFrame(
    val width: Int,
    val height: Int,
    pixels: IntArray,
) {
    init {
        require(width > 0 && height > 0) { "Frame dimensions must be positive" }
        require(width.toLong() * height == pixels.size.toLong()) {
            "Pixel count must match the frame dimensions"
        }
    }

    val pixels: IntArray = pixels.copyOf()
}
