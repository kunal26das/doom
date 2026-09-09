
package doom.engine.rendering

import doom.engine.SCREENWIDTH
import doom.engine.gameplay.player.Player
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FINEANGLES
import doom.engine.geometry.FixedPoint

internal class RendererViewState {
    var viewangleoffset = 0

    var validcount = 1

    var fixedcolormap = -1

    var centerx = 0

    var centery = 0

    var centerxfrac: FixedPoint = 0

    var centeryfrac: FixedPoint = 0

    var projection: FixedPoint = 0

    var framecount = 0

    var sscount = 0

    var linecount = 0

    var loopcount = 0

    var viewx: FixedPoint = 0

    var viewy: FixedPoint = 0

    var viewz: FixedPoint = 0

    var viewangle: BinaryAngle = 0u

    var viewcos: FixedPoint = 0

    var viewsin: FixedPoint = 0

    var viewplayer: Player? = null

    var detailshift = 0

    var clipangle: BinaryAngle = 0u

    val viewangletox by lazy(LazyThreadSafetyMode.NONE) { IntArray(FINEANGLES / 2) }

    val xtoviewangle by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENWIDTH + 1) }

    val scalelight by lazy(LazyThreadSafetyMode.NONE) { Array(LIGHTLEVELS) { IntArray(MAXLIGHTSCALE) } }

    val scalelightfixed by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXLIGHTSCALE) }

    val zlight by lazy(LazyThreadSafetyMode.NONE) { Array(LIGHTLEVELS) { IntArray(MAXLIGHTZ) } }

    var extralight = 0

    lateinit var colfunc: () -> Unit

    lateinit var basecolfunc: () -> Unit

    lateinit var fuzzcolfunc: () -> Unit

    lateinit var transcolfunc: () -> Unit

    lateinit var spanfunc: () -> Unit

    var setsizeneeded = false

    var setblocks = 0

    var setdetail = 0
}
