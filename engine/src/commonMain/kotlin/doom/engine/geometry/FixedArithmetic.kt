
package doom.engine.geometry

import doom.engine.resources.MAXINT
import doom.engine.resources.MININT

import kotlin.math.abs

internal const val FRACBITS = 16
internal const val FRACUNIT: FixedPoint = 1 shl FRACBITS

internal fun fixedMul(a: FixedPoint, b: FixedPoint): FixedPoint =
    ((a.toLong() * b.toLong()) shr FRACBITS).toInt()

internal fun fixedDiv(a: FixedPoint, b: FixedPoint): FixedPoint {
    if ((abs(a) shr 14) >= abs(b)) {
        return if ((a xor b) < 0) MININT else MAXINT
    }
    return fixedDiv2(a, b)
}

internal fun fixedDiv2(a: FixedPoint, b: FixedPoint): FixedPoint =
    ((a.toLong() shl FRACBITS) / b.toLong()).toInt()
