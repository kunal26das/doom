
package doom.engine.world.visibility

import doom.engine.geometry.DividingLine
import doom.engine.geometry.FixedPoint

internal class SightTraceState {
    var sightzstart: FixedPoint = 0

    var topslope: FixedPoint = 0

    var bottomslope: FixedPoint = 0

    val strace by lazy(LazyThreadSafetyMode.NONE) { DividingLine() }

    var t2x: FixedPoint = 0

    var t2y: FixedPoint = 0

    val sightcounts by lazy(LazyThreadSafetyMode.NONE) { IntArray(2) }
}
