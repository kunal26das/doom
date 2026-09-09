// Port of linuxdoom-1.10 m_fixed.c -- fixed-point (16.16) arithmetic.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "unused", "MagicNumber", "ktlint")

package doom.engine

import kotlin.math.abs


internal const val FRACBITS = 16
internal const val FRACUNIT: fixed_t = 1 shl FRACBITS

internal fun FixedMul(a: fixed_t, b: fixed_t): fixed_t =
    ((a.toLong() * b.toLong()) shr FRACBITS).toInt()

/**
 * FixedDiv with the vanilla overflow guard. The division itself uses the
 * 64-bit form (what the DOS build's assembly did; matches chocolate-doom),
 * NOT the C double fallback.
 */
internal fun FixedDiv(a: fixed_t, b: fixed_t): fixed_t {
    if ((abs(a) shr 14) >= abs(b)) {
        return if ((a xor b) < 0) MININT else MAXINT
    }
    return FixedDiv2(a, b)
}

internal fun FixedDiv2(a: fixed_t, b: fixed_t): fixed_t =
    ((a.toLong() shl FRACBITS) / b.toLong()).toInt()
