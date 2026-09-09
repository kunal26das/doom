
package doom.engine.rendering

import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FixedPoint

internal class WallRendererState {
    var segtextured = false

    var markfloor = false

    var markceiling = false

    var maskedtexture = false

    var toptexture = 0

    var bottomtexture = 0

    var midtexture = 0

    var rwNormalangle: BinaryAngle = 0u

    var rwAngle1 = 0

    var rwX = 0

    var rwStopx = 0

    var rwCenterangle: BinaryAngle = 0u

    var rwOffset: FixedPoint = 0

    var rwDistance: FixedPoint = 0

    var rwScale: FixedPoint = 0

    var rwScalestep: FixedPoint = 0

    var rwMidtexturemid: FixedPoint = 0

    var rwToptexturemid: FixedPoint = 0

    var rwBottomtexturemid: FixedPoint = 0

    var worldtop = 0

    var worldbottom = 0

    var worldhigh = 0

    var worldlow = 0

    var pixhigh: FixedPoint = 0

    var pixlow: FixedPoint = 0

    var pixhighstep: FixedPoint = 0

    var pixlowstep: FixedPoint = 0

    var topfrac: FixedPoint = 0

    var topstep: FixedPoint = 0

    var bottomfrac: FixedPoint = 0

    var bottomstep: FixedPoint = 0

    var walllights: IntArray = IntArray(0)

    var maskedtexturecol: ShortArray = ShortArray(0)

    var maskedtexturecolBase = 0
}
