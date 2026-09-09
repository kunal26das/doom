
package doom.engine.geometry

internal const val FINEANGLES = 8192
internal const val FINEMASK = FINEANGLES - 1
internal const val ANGLETOFINESHIFT = 19

internal const val ANG45: BinaryAngle = 0x20000000u
internal const val ANG90: BinaryAngle = 0x40000000u
internal const val ANG180: BinaryAngle = 0x80000000u
internal const val ANG270: BinaryAngle = 0xc0000000u

internal const val SLOPERANGE = 2048
internal const val SLOPEBITS = 11
internal const val DBITS = FRACBITS - SLOPEBITS

internal fun slopeDiv(num: UInt, den: UInt): Int {
    if (den < 512u) return SLOPERANGE
    val ans = ((num shl 3) / (den shr 8)).toInt()
    return if (ans <= SLOPERANGE) ans else SLOPERANGE
}
