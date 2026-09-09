package doom.engine.rendering

import doom.engine.resources.i16
import doom.engine.resources.i32

internal object PatchFormat {
    fun width(patch: ByteArray): Int = patch.i16(0)
    fun height(patch: ByteArray): Int = patch.i16(2)
    fun leftOffset(patch: ByteArray): Int = patch.i16(4)
    fun topOffset(patch: ByteArray): Int = patch.i16(6)
    fun columnOffset(patch: ByteArray, column: Int): Int = patch.i32(8 + column * 4)
}
