package com.kunal26das.doom

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import kotlin.test.Test
import kotlin.test.assertTrue

class FrameBitmapConverterTest {
    @Test
    fun renderingAnotherScreenDoesNotOverwriteTheFirstScreensRetainedImage() {
        val firstScreen = FrameBitmapConverter()
        val secondScreen = FrameBitmapConverter()
        val firstColor = 0xff123456.toInt()
        val image = firstScreen.convert(IntArray(SCREENWIDTH * SCREENHEIGHT) { firstColor })

        for (color in intArrayOf(0xffabcdef.toInt(), 0xff654321.toInt(), 0xfffedcba.toInt(), 0xff112233.toInt())) {
            secondScreen.convert(IntArray(SCREENWIDTH * SCREENHEIGHT) { color })
        }

        val retainedPixels = IntArray(SCREENWIDTH * SCREENHEIGHT)
        image.readPixels(retainedPixels)
        assertTrue(
            retainedPixels.all { it == firstColor },
            "A screen's retained image must not share mutable bitmap storage with another screen",
        )
    }
}
