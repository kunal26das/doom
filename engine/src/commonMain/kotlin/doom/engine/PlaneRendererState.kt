// Port of linuxdoom-1.10 r_plane.c -- Here is a core component: drawing the
// floors and ceilings, while maintaining a per column clipping list only.
// Moreover, the sky areas have to be determined.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class PlaneRendererState {
    var floorfunc: ((Int, Int) -> Unit)? = null

    var ceilingfunc: ((Int, Int) -> Unit)? = null

    val visplanes by lazy(LazyThreadSafetyMode.NONE) { Array(MAXVISPLANES) { visplane_t() } }

    var lastvisplane = 0

    var floorplane = -1

    var ceilingplane = -1

    val openings by lazy(LazyThreadSafetyMode.NONE) { ShortArray(MAXOPENINGS) }

    var lastopening = 0

    val floorclip by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val ceilingclip by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val spanstart by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val spanstop by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    var planezlight: IntArray = IntArray(0)

    var planeheight: fixed_t = 0

    val yslope by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val distscale by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENWIDTH) }

    var basexscale: fixed_t = 0

    var baseyscale: fixed_t = 0

    val cachedheight by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cacheddistance by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cachedxstep by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cachedystep by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }
}
