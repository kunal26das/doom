
package doom.engine.rendering

import doom.engine.geometry.FixedPoint

internal class RasterizerState {
    var viewimage: ByteArray = ByteArray(0)

    var viewwidth = 0

    var scaledviewwidth = 0

    var viewheight = 0

    var viewwindowx = 0

    var viewwindowy = 0

    val ylookup by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXHEIGHT) }

    val columnofs by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXWIDTH) }

    val translations by lazy(LazyThreadSafetyMode.NONE) { Array(3) { ByteArray(256) } }

    var dcColormap = -1

    var dcX = 0

    var dcYl = 0

    var dcYh = 0

    var dcIscale: FixedPoint = 0

    var dcTexturemid: FixedPoint = 0

    var dcSource: ByteArray = ByteArray(0)

    var dcSourceOfs = 0

    var dccount = 0

    val fuzzoffset by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF,
        FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF,
        FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF,
        FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF
    ) }

    var fuzzpos = 0

    var dcTranslation = 0

    var translationtables: ByteArray = ByteArray(0)

    var dsY = 0

    var dsX1 = 0

    var dsX2 = 0

    var dsColormap = -1

    var dsXfrac: FixedPoint = 0

    var dsYfrac: FixedPoint = 0

    var dsXstep: FixedPoint = 0

    var dsYstep: FixedPoint = 0

    var dsSource: ByteArray = ByteArray(0)

    var dsSourceOfs = 0

    var dscount = 0
}
