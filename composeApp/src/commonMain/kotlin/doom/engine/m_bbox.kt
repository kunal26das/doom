// Port of linuxdoom-1.10 m_bbox.c -- bounding boxes.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "unused", "MagicNumber", "ktlint")

package doom.engine

const val BOXTOP = 0
const val BOXBOTTOM = 1
const val BOXLEFT = 2
const val BOXRIGHT = 3

fun M_ClearBox(box: IntArray) {
    box[BOXTOP] = MININT
    box[BOXRIGHT] = MININT
    box[BOXBOTTOM] = MAXINT
    box[BOXLEFT] = MAXINT
}

fun M_AddToBox(box: IntArray, x: fixed_t, y: fixed_t) {
    if (x < box[BOXLEFT]) box[BOXLEFT] = x
    else if (x > box[BOXRIGHT]) box[BOXRIGHT] = x
    if (y < box[BOXBOTTOM]) box[BOXBOTTOM] = y
    else if (y > box[BOXTOP]) box[BOXTOP] = y
}
