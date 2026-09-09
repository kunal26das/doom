
package doom.engine.world

import doom.engine.geometry.FixedPoint

internal class BspNode {
    var x: FixedPoint = 0
    var y: FixedPoint = 0
    var dx: FixedPoint = 0
    var dy: FixedPoint = 0

    val bbox = Array(2) { IntArray(4) }

    val children = IntArray(2)
}
