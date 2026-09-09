// Port of linuxdoom-1.10 tables.h/tables.c (lookup data itself is generated
// into gen/TablesGen.kt; this file holds the constants and SlopeDiv).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "unused", "MagicNumber", "ktlint")

package doom.engine


internal const val FINEANGLES = 8192
internal const val FINEMASK = FINEANGLES - 1
internal const val ANGLETOFINESHIFT = 19

internal const val ANG45: angle_t = 0x20000000u
internal const val ANG90: angle_t = 0x40000000u
internal const val ANG180: angle_t = 0x80000000u
internal const val ANG270: angle_t = 0xc0000000u

internal const val SLOPERANGE = 2048
internal const val SLOPEBITS = 11
internal const val DBITS = FRACBITS - SLOPEBITS

internal fun SlopeDiv(num: UInt, den: UInt): Int {
    if (den < 512u) return SLOPERANGE
    val ans = ((num shl 3) / (den shr 8)).toInt()
    return if (ans <= SLOPERANGE) ans else SLOPERANGE
}
