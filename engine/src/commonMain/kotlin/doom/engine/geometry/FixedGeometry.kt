// Fixed-point/octant algorithms from DOOM. Original (C) id Software, GPL-2.0.
@file:Suppress("NAME_SHADOWING")

package doom.engine.geometry

import doom.engine.ANG180
import doom.engine.ANG270
import doom.engine.ANG90
import doom.engine.ANGLETOFINESHIFT
import doom.engine.DBITS
import doom.engine.FRACBITS
import doom.engine.FixedDiv
import doom.engine.FixedMul
import doom.engine.SlopeDiv
import doom.engine.angle_t
import doom.engine.finesine
import doom.engine.fixed_t
import doom.engine.node_t
import doom.engine.seg_t
import doom.engine.tantoangle
import kotlin.math.abs

/** Pure world geometry shared by gameplay and rendering; never changes camera state. */
internal object FixedGeometry {
    fun angleBetween(originX: fixed_t, originY: fixed_t, x: fixed_t, y: fixed_t): angle_t {
        var x = x
        var y = y

        x -= originX
        y -= originY

        if ((x == 0) && (y == 0))
            return 0u

        if (x >= 0) {
            // x >=0
            if (y >= 0) {
                // y>= 0

                if (x > y) {
                    // octant 0
                    return tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    // octant 1
                    return ANG90 - 1u - tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            } else {
                // y<0
                y = -y

                if (x > y) {
                    // octant 8
                    return 0u - tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    // octant 7
                    return ANG270 + tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            }
        } else {
            // x<0
            x = -x

            if (y >= 0) {
                // y>= 0
                if (x > y) {
                    // octant 3
                    return ANG180 - 1u - tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    // octant 2
                    return ANG90 + tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            } else {
                // y<0
                y = -y

                if (x > y) {
                    // octant 4
                    return ANG180 + tantoangle[SlopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    // octant 5
                    return ANG270 - 1u - tantoangle[SlopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            }
        }
    }

    fun distanceBetween(originX: fixed_t, originY: fixed_t, x: fixed_t, y: fixed_t): fixed_t {
        val angle: Int
        var dx: fixed_t
        var dy: fixed_t
        val temp: fixed_t
        val dist: fixed_t

        dx = abs(x - originX)
        dy = abs(y - originY)

        if (dy > dx) {
            temp = dx
            dx = dy
            dy = temp
        }

        // The view can coincide with a wall vertex. FixedDiv(0, 0) saturates,
        // which is not a valid tangent-table index; the geometric distance is zero.
        if (dx == 0) return 0

        angle = ((tantoangle[FixedDiv(dy, dx) shr DBITS].toUInt() + ANG90) shr ANGLETOFINESHIFT).toInt()

        // use as cosine
        dist = FixedDiv(dx, finesine[angle])

        return dist
    }

    fun sideOfNode(x: fixed_t, y: fixed_t, node: node_t): Int {
        val dx: fixed_t
        val dy: fixed_t
        val left: fixed_t
        val right: fixed_t

        if (node.dx == 0) {
            if (x <= node.x)
                return if (node.dy > 0) 1 else 0

            return if (node.dy < 0) 1 else 0
        }
        if (node.dy == 0) {
            if (y <= node.y)
                return if (node.dx < 0) 1 else 0

            return if (node.dx > 0) 1 else 0
        }

        dx = (x - node.x)
        dy = (y - node.y)

        // Try to quickly decide by looking at sign bits.
        if (((node.dy xor node.dx xor dx xor dy) and 0x80000000.toInt()) != 0) {
            if (((node.dy xor dx) and 0x80000000.toInt()) != 0) {
                // (left is negative)
                return 1
            }
            return 0
        }

        left = FixedMul(node.dy shr FRACBITS, dx)
        right = FixedMul(dy, node.dx shr FRACBITS)

        if (right < left) {
            // front side
            return 0
        }
        // back side
        return 1
    }

    fun sideOfSegment(x: fixed_t, y: fixed_t, line: seg_t): Int {
        val lx: fixed_t
        val ly: fixed_t
        val ldx: fixed_t
        val ldy: fixed_t
        val dx: fixed_t
        val dy: fixed_t
        val left: fixed_t
        val right: fixed_t

        lx = line.v1.x
        ly = line.v1.y

        ldx = line.v2.x - lx
        ldy = line.v2.y - ly

        if (ldx == 0) {
            if (x <= lx)
                return if (ldy > 0) 1 else 0

            return if (ldy < 0) 1 else 0
        }
        if (ldy == 0) {
            if (y <= ly)
                return if (ldx < 0) 1 else 0

            return if (ldx > 0) 1 else 0
        }

        dx = (x - lx)
        dy = (y - ly)

        // Try to quickly decide by looking at sign bits.
        if (((ldy xor ldx xor dx xor dy) and 0x80000000.toInt()) != 0) {
            if (((ldy xor dx) and 0x80000000.toInt()) != 0) {
                // (left is negative)
                return 1
            }
            return 0
        }

        left = FixedMul(ldy shr FRACBITS, dx)
        right = FixedMul(dy, ldx shr FRACBITS)

        if (right < left) {
            // front side
            return 0
        }
        // back side
        return 1
    }
}
