// Port of linuxdoom-1.10 doomtype.h + m_swap byte-order helpers.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal const val MAXINT = 0x7fffffff
internal const val MININT = -0x7fffffff - 1
internal const val MAXCHAR = 0x7f
internal const val MINCHAR = -0x80
internal const val MAXSHORT = 0x7fff
internal const val MINSHORT = -0x8000

// Little-endian lump readers (replace m_swap.c SHORT()/LONG() + struct reads).
internal fun ByteArray.u8(off: Int): Int = this[off].toInt() and 0xFF
internal fun ByteArray.i8(off: Int): Int = this[off].toInt()
internal fun ByteArray.u16(off: Int): Int = u8(off) or (u8(off + 1) shl 8)
internal fun ByteArray.i16(off: Int): Int = u16(off).toShort().toInt()
internal fun ByteArray.i32(off: Int): Int =
    u8(off) or (u8(off + 1) shl 8) or (u8(off + 2) shl 16) or (u8(off + 3) shl 24)

/** Fixed-length, NUL-truncated ASCII string (e.g. 8-char lump names). */
internal fun ByteArray.str(off: Int, len: Int): String {
    val sb = StringBuilder(len)
    for (i in 0 until len) {
        val c = u8(off + i)
        if (c == 0) break
        sb.append(c.toChar())
    }
    return sb.toString()
}
