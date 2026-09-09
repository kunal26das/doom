package com.kunal26das.doom.domain

/** A rendered frame that owns a copy of the engine's reusable pixel buffer. */
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

    /** This snapshot belongs to its consumer; changes cannot affect engine memory. */
    val pixels: IntArray = pixels.copyOf()
}
