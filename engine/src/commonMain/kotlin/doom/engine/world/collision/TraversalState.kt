
package doom.engine.world.collision

import doom.engine.geometry.DividingLine
import doom.engine.geometry.FixedPoint

internal class TraversalState {
    var opentop: FixedPoint = 0

    var openbottom: FixedPoint = 0

    var openrange: FixedPoint = 0

    var lowfloor: FixedPoint = 0

    val intercepts by lazy(LazyThreadSafetyMode.NONE) { Array(MAXINTERCEPTS) { PathIntercept() } }

    var interceptP = 0

    val trace by lazy(LazyThreadSafetyMode.NONE) { DividingLine() }

    var earlyout = false

    var ptflags = 0
}
