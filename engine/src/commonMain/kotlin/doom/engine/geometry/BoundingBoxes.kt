
package doom.engine.geometry

import doom.engine.resources.MAXINT
import doom.engine.resources.MININT

internal const val BOXTOP = 0
internal const val BOXBOTTOM = 1
internal const val BOXLEFT = 2
internal const val BOXRIGHT = 3

internal fun mClearBox(box: IntArray) {
    box[BOXTOP] = MININT
    box[BOXRIGHT] = MININT
    box[BOXBOTTOM] = MAXINT
    box[BOXLEFT] = MAXINT
}

internal fun mAddToBox(box: IntArray, x: FixedPoint, y: FixedPoint) {
    if (x < box[BOXLEFT]) box[BOXLEFT] = x
    else if (x > box[BOXRIGHT]) box[BOXRIGHT] = x
    if (y < box[BOXBOTTOM]) box[BOXBOTTOM] = y
    else if (y > box[BOXTOP]) box[BOXTOP] = y
}
