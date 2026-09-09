
package doom.engine.rendering

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.geometry.FixedPoint

internal class PlaneRendererState {
    var floorfunc: ((Int, Int) -> Unit)? = null

    var ceilingfunc: ((Int, Int) -> Unit)? = null

    val visplanes by lazy(LazyThreadSafetyMode.NONE) { Array(MAXVISPLANES) { VisiblePlane() } }

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

    var planeheight: FixedPoint = 0

    val yslope by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val distscale by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENWIDTH) }

    var basexscale: FixedPoint = 0

    var baseyscale: FixedPoint = 0

    val cachedheight by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cacheddistance by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cachedxstep by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }

    val cachedystep by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENHEIGHT) }
}
