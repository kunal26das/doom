// Port of linuxdoom-1.10 r_bsp.c -- BSP traversal, handling of LineSegs
// for rendering.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class BspRendererState {
    var curline: seg_t? = null

    var sidedef: side_t? = null

    var linedef: line_t? = null

    var frontsector: sector_t? = null

    var backsector: sector_t? = null

    val drawsegs by lazy(LazyThreadSafetyMode.NONE) { Array(MAXDRAWSEGS) { drawseg_t() } }

    var ds_p = 0

    var newend = 0

    val solidsegs by lazy(LazyThreadSafetyMode.NONE) { Array(MAXSEGS) { cliprange_t() } }

    val checkcoord by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(3, 0, 2, 1),
        intArrayOf(3, 0, 2, 0),
        intArrayOf(3, 1, 2, 0),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(2, 0, 2, 1),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(3, 1, 3, 0),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(2, 0, 3, 1),
        intArrayOf(2, 1, 3, 1),
        intArrayOf(2, 1, 3, 0),
        intArrayOf(0, 0, 0, 0),
    ) }
}
