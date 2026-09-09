
package doom.engine.geometry

import doom.engine.world.BspNode
import doom.engine.world.MapSegment

import kotlin.math.abs

internal object FixedGeometry {
    fun angleBetween(originX: FixedPoint, originY: FixedPoint, pointX: FixedPoint, pointY: FixedPoint): BinaryAngle {
        var x = pointX
        var y = pointY

        x -= originX
        y -= originY

        if ((x == 0) && (y == 0))
            return 0u

        if (x >= 0) {
            if (y >= 0) {

                if (x > y) {
                    return tantoangle[slopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    return ANG90 - 1u - tantoangle[slopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            } else {
                y = -y

                if (x > y) {
                    return 0u - tantoangle[slopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    return ANG270 + tantoangle[slopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            }
        } else {
            x = -x

            if (y >= 0) {
                if (x > y) {
                    return ANG180 - 1u - tantoangle[slopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    return ANG90 + tantoangle[slopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            } else {
                y = -y

                if (x > y) {
                    return ANG180 + tantoangle[slopeDiv(y.toUInt(), x.toUInt())].toUInt()
                } else {
                    return ANG270 - 1u - tantoangle[slopeDiv(x.toUInt(), y.toUInt())].toUInt()
                }
            }
        }
    }

    fun distanceBetween(originX: FixedPoint, originY: FixedPoint, x: FixedPoint, y: FixedPoint): FixedPoint {
        val angle: Int
        var dx: FixedPoint
        var dy: FixedPoint
        val temp: FixedPoint
        val dist: FixedPoint

        dx = abs(x - originX)
        dy = abs(y - originY)

        if (dy > dx) {
            temp = dx
            dx = dy
            dy = temp
        }

        if (dx == 0) return 0

        angle = ((tantoangle[fixedDiv(dy, dx) shr DBITS].toUInt() + ANG90) shr ANGLETOFINESHIFT).toInt()

        dist = fixedDiv(dx, finesine[angle])

        return dist
    }

    fun sideOfNode(x: FixedPoint, y: FixedPoint, node: BspNode): Int {
        val dx: FixedPoint
        val dy: FixedPoint
        val left: FixedPoint
        val right: FixedPoint

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

        if (((node.dy xor node.dx xor dx xor dy) and 0x80000000.toInt()) != 0) {
            if (((node.dy xor dx) and 0x80000000.toInt()) != 0) {
                return 1
            }
            return 0
        }

        left = fixedMul(node.dy shr FRACBITS, dx)
        right = fixedMul(dy, node.dx shr FRACBITS)

        if (right < left) {
            return 0
        }
        return 1
    }

    fun sideOfSegment(x: FixedPoint, y: FixedPoint, line: MapSegment): Int {
        val lx: FixedPoint
        val ly: FixedPoint
        val ldx: FixedPoint
        val ldy: FixedPoint
        val dx: FixedPoint
        val dy: FixedPoint
        val left: FixedPoint
        val right: FixedPoint

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

        if (((ldy xor ldx xor dx xor dy) and 0x80000000.toInt()) != 0) {
            if (((ldy xor dx) and 0x80000000.toInt()) != 0) {
                return 1
            }
            return 0
        }

        left = fixedMul(ldy shr FRACBITS, dx)
        right = fixedMul(dy, ldx shr FRACBITS)

        if (right < left) {
            return 0
        }
        return 1
    }
}
