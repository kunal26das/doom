// Framebuffer and patch algorithms from linuxdoom-1.10 v_video.c.
// Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
package doom.engine.rendering

import doom.engine.i16
import doom.engine.i32

/** Little-endian fields of a trusted DOOM patch lump. */
internal object PatchFormat {
    fun width(patch: ByteArray): Int = patch.i16(0)
    fun height(patch: ByteArray): Int = patch.i16(2)
    fun leftOffset(patch: ByteArray): Int = patch.i16(4)
    fun topOffset(patch: ByteArray): Int = patch.i16(6)
    fun columnOffset(patch: ByteArray, column: Int): Int = patch.i32(8 + column * 4)
}
