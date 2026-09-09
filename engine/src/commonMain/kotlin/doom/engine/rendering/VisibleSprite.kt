
package doom.engine.rendering

import doom.engine.geometry.FixedPoint

internal class VisibleSprite {
    var prev: VisibleSprite? = null
    var next: VisibleSprite? = null

    var x1 = 0
    var x2 = 0

    var gx: FixedPoint = 0
    var gy: FixedPoint = 0

    var gz: FixedPoint = 0
    var gzt: FixedPoint = 0

    var startfrac: FixedPoint = 0

    var scale: FixedPoint = 0

    var xiscale: FixedPoint = 0

    var texturemid: FixedPoint = 0
    var patch = 0

    var colormap = -1

    var mobjflags = 0
}
