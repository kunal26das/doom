
package doom.engine.automap

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.cheats.CheatSequence
import doom.engine.gameplay.player.Player
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FixedPoint
import doom.engine.world.PLAYERRADIUS

internal class AutomapSubsystemState {
    val initscalemtof by lazy(LazyThreadSafetyMode.NONE) { (0.2 * FRACUNIT).toInt() }

    val mZOOMIN by lazy(LazyThreadSafetyMode.NONE) { (1.02 * FRACUNIT).toInt() }

    val mZOOMOUT by lazy(LazyThreadSafetyMode.NONE) { (FRACUNIT / 1.02).toInt() }

    val playerArrow: Array<AutomapWorldLine> by lazy(LazyThreadSafetyMode.NONE) { run {
        val r = (8 * PLAYERRADIUS) / 7
        arrayOf(
            AutomapWorldLine(-r + r / 8, 0, r, 0),
            AutomapWorldLine(r, 0, r - r / 2, r / 4),
            AutomapWorldLine(r, 0, r - r / 2, -r / 4),
            AutomapWorldLine(-r + r / 8, 0, -r - r / 8, r / 4),
            AutomapWorldLine(-r + r / 8, 0, -r - r / 8, -r / 4),
            AutomapWorldLine(-r + 3 * r / 8, 0, -r + r / 8, r / 4),
            AutomapWorldLine(-r + 3 * r / 8, 0, -r + r / 8, -r / 4),
        )
    } }

    val numplyrlines by lazy(LazyThreadSafetyMode.NONE) { playerArrow.size }

    val cheatPlayerArrow: Array<AutomapWorldLine> by lazy(LazyThreadSafetyMode.NONE) { run {
        val r = (8 * PLAYERRADIUS) / 7
        arrayOf(
            AutomapWorldLine(-r + r / 8, 0, r, 0),
            AutomapWorldLine(r, 0, r - r / 2, r / 6),
            AutomapWorldLine(r, 0, r - r / 2, -r / 6),
            AutomapWorldLine(-r + r / 8, 0, -r - r / 8, r / 6),
            AutomapWorldLine(-r + r / 8, 0, -r - r / 8, -r / 6),
            AutomapWorldLine(-r + 3 * r / 8, 0, -r + r / 8, r / 6),
            AutomapWorldLine(-r + 3 * r / 8, 0, -r + r / 8, -r / 6),
            AutomapWorldLine(-r / 2, 0, -r / 2, -r / 6),
            AutomapWorldLine(-r / 2, -r / 6, -r / 2 + r / 6, -r / 6),
            AutomapWorldLine(-r / 2 + r / 6, -r / 6, -r / 2 + r / 6, r / 4),
            AutomapWorldLine(-r / 6, 0, -r / 6, -r / 6),
            AutomapWorldLine(-r / 6, -r / 6, 0, -r / 6),
            AutomapWorldLine(0, -r / 6, 0, r / 4),
            AutomapWorldLine(r / 6, r / 4, r / 6, -r / 7),
            AutomapWorldLine(r / 6, -r / 7, r / 6 + r / 32, -r / 7 - r / 32),
            AutomapWorldLine(r / 6 + r / 32, -r / 7 - r / 32, r / 6 + r / 10, -r / 7),
        )
    } }

    val numcheatplyrlines by lazy(LazyThreadSafetyMode.NONE) { cheatPlayerArrow.size }

    val triangleGuy: Array<AutomapWorldLine> by lazy(LazyThreadSafetyMode.NONE) { run {
        val r = FRACUNIT
        arrayOf(
            AutomapWorldLine((-0.867 * r).toInt(), (-0.5 * r).toInt(), (0.867 * r).toInt(), (-0.5 * r).toInt()),
            AutomapWorldLine((0.867 * r).toInt(), (-0.5 * r).toInt(), 0, r),
            AutomapWorldLine(0, r, (-0.867 * r).toInt(), (-0.5 * r).toInt()),
        )
    } }

    val numtriangleguylines by lazy(LazyThreadSafetyMode.NONE) { triangleGuy.size }

    val thintriangleGuy: Array<AutomapWorldLine> by lazy(LazyThreadSafetyMode.NONE) { run {
        val r = FRACUNIT
        arrayOf(
            AutomapWorldLine((-0.5 * r).toInt(), (-0.7 * r).toInt(), r, 0),
            AutomapWorldLine(r, 0, (-0.5 * r).toInt(), (0.7 * r).toInt()),
            AutomapWorldLine((-0.5 * r).toInt(), (0.7 * r).toInt(), (-0.5 * r).toInt(), (-0.7 * r).toInt()),
        )
    } }

    val numthintriangleguylines by lazy(LazyThreadSafetyMode.NONE) { thintriangleGuy.size }

    var cheating = 0

    var grid = 0

    var leveljuststarted = 1

    var automapactive = false

    var finitWidth = SCREENWIDTH

    var finitHeight = SCREENHEIGHT - 32

    var fX = 0

    var fY = 0

    var fW = 0

    var fH = 0

    var lightlev = 0

    var fb = ByteArray(0)

    var amclock = 0

    val mPaninc by lazy(LazyThreadSafetyMode.NONE) { AutomapWorldPoint() }

    var mtofZoommul: FixedPoint = 0

    var ftomZoommul: FixedPoint = 0

    var mX: FixedPoint = 0

    var mY: FixedPoint = 0

    var mX2: FixedPoint = 0

    var mY2: FixedPoint = 0

    var mW: FixedPoint = 0

    var mH: FixedPoint = 0

    var minX: FixedPoint = 0

    var minY: FixedPoint = 0

    var maxX: FixedPoint = 0

    var maxY: FixedPoint = 0

    var maxW: FixedPoint = 0

    var maxH: FixedPoint = 0

    var minW: FixedPoint = 0

    var minH: FixedPoint = 0

    var minScaleMtof: FixedPoint = 0

    var maxScaleMtof: FixedPoint = 0

    var oldMW: FixedPoint = 0

    var oldMH: FixedPoint = 0

    var oldMX: FixedPoint = 0

    var oldMY: FixedPoint = 0

    val fOldloc by lazy(LazyThreadSafetyMode.NONE) { AutomapWorldPoint() }

    var scaleMtof: FixedPoint = initscalemtof

    var scaleFtom: FixedPoint = 0

    lateinit var plr: Player

    val marknums by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<ByteArray>(10) }

    val markpoints by lazy(LazyThreadSafetyMode.NONE) { Array(AM_NUMMARKPOINTS) { AutomapWorldPoint() } }

    var markpointnum = 0

    var followplayer = 1

    val cheatAmapSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0xb2, 0x26, 0x26, 0x2e, 0xff) }

    val cheatAmap by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatAmapSeq, 0) }

    var stopped = true

    var lastlevel = -1

    var lastepisode = -1

    var cheatstate = 0

    var bigstate = 0

    var buffer = ""

    var nexttic = 0

    val litelevels by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0, 4, 7, 10, 12, 14, 15, 15) }

    var litelevelscnt = 0

    var fuck = 0

    val fl by lazy(LazyThreadSafetyMode.NONE) { AutomapScreenLine() }

    val l by lazy(LazyThreadSafetyMode.NONE) { AutomapWorldLine() }

    val theirColors by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(GREENS, GRAYS, BROWNS, REDS) }
}
