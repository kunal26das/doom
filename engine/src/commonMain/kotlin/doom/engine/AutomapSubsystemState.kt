// Port of linuxdoom-1.10 am_map.c/am_map.h -- the automap code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class AutomapSubsystemState {
    val INITSCALEMTOF by lazy(LazyThreadSafetyMode.NONE) { (0.2 * FRACUNIT).toInt() }

    val M_ZOOMIN by lazy(LazyThreadSafetyMode.NONE) { (1.02 * FRACUNIT).toInt() }

    val M_ZOOMOUT by lazy(LazyThreadSafetyMode.NONE) { (FRACUNIT / 1.02).toInt() }

    val player_arrow: Array<mline_t> by lazy(LazyThreadSafetyMode.NONE) { run {
        val R = (8 * PLAYERRADIUS) / 7
        arrayOf(
            mline_t(-R + R / 8, 0, R, 0), // -----
            mline_t(R, 0, R - R / 2, R / 4),  // ----->
            mline_t(R, 0, R - R / 2, -R / 4),
            mline_t(-R + R / 8, 0, -R - R / 8, R / 4), // >---->
            mline_t(-R + R / 8, 0, -R - R / 8, -R / 4),
            mline_t(-R + 3 * R / 8, 0, -R + R / 8, R / 4), // >>--->
            mline_t(-R + 3 * R / 8, 0, -R + R / 8, -R / 4),
        )
    } }

    val NUMPLYRLINES by lazy(LazyThreadSafetyMode.NONE) { player_arrow.size }

    val cheat_player_arrow: Array<mline_t> by lazy(LazyThreadSafetyMode.NONE) { run {
        val R = (8 * PLAYERRADIUS) / 7
        arrayOf(
            mline_t(-R + R / 8, 0, R, 0), // -----
            mline_t(R, 0, R - R / 2, R / 6),  // ----->
            mline_t(R, 0, R - R / 2, -R / 6),
            mline_t(-R + R / 8, 0, -R - R / 8, R / 6), // >----->
            mline_t(-R + R / 8, 0, -R - R / 8, -R / 6),
            mline_t(-R + 3 * R / 8, 0, -R + R / 8, R / 6), // >>----->
            mline_t(-R + 3 * R / 8, 0, -R + R / 8, -R / 6),
            mline_t(-R / 2, 0, -R / 2, -R / 6), // >>-d--->
            mline_t(-R / 2, -R / 6, -R / 2 + R / 6, -R / 6),
            mline_t(-R / 2 + R / 6, -R / 6, -R / 2 + R / 6, R / 4),
            mline_t(-R / 6, 0, -R / 6, -R / 6), // >>-dd-->
            mline_t(-R / 6, -R / 6, 0, -R / 6),
            mline_t(0, -R / 6, 0, R / 4),
            mline_t(R / 6, R / 4, R / 6, -R / 7), // >>-ddt->
            mline_t(R / 6, -R / 7, R / 6 + R / 32, -R / 7 - R / 32),
            mline_t(R / 6 + R / 32, -R / 7 - R / 32, R / 6 + R / 10, -R / 7),
        )
    } }

    val NUMCHEATPLYRLINES by lazy(LazyThreadSafetyMode.NONE) { cheat_player_arrow.size }

    val triangle_guy: Array<mline_t> by lazy(LazyThreadSafetyMode.NONE) { run {
        val R = FRACUNIT
        arrayOf(
            mline_t((-0.867 * R).toInt(), (-0.5 * R).toInt(), (0.867 * R).toInt(), (-0.5 * R).toInt()),
            mline_t((0.867 * R).toInt(), (-0.5 * R).toInt(), 0, R),
            mline_t(0, R, (-0.867 * R).toInt(), (-0.5 * R).toInt()),
        )
    } }

    val NUMTRIANGLEGUYLINES by lazy(LazyThreadSafetyMode.NONE) { triangle_guy.size }

    val thintriangle_guy: Array<mline_t> by lazy(LazyThreadSafetyMode.NONE) { run {
        val R = FRACUNIT
        arrayOf(
            mline_t((-0.5 * R).toInt(), (-0.7 * R).toInt(), R, 0),
            mline_t(R, 0, (-0.5 * R).toInt(), (0.7 * R).toInt()),
            mline_t((-0.5 * R).toInt(), (0.7 * R).toInt(), (-0.5 * R).toInt(), (-0.7 * R).toInt()),
        )
    } }

    val NUMTHINTRIANGLEGUYLINES by lazy(LazyThreadSafetyMode.NONE) { thintriangle_guy.size }

    var cheating = 0

    var grid = 0

    var leveljuststarted = 1

    var automapactive = false

    var finit_width = SCREENWIDTH

    var finit_height = SCREENHEIGHT - 32

    var f_x = 0

    var f_y = 0

    var f_w = 0

    var f_h = 0

    var lightlev = 0

    var fb = ByteArray(0)

    var amclock = 0

    val m_paninc by lazy(LazyThreadSafetyMode.NONE) { mpoint_t() }

    var mtof_zoommul: fixed_t = 0

    var ftom_zoommul: fixed_t = 0

    var m_x: fixed_t = 0

    var m_y: fixed_t = 0

    var m_x2: fixed_t = 0

    var m_y2: fixed_t = 0

    var m_w: fixed_t = 0

    var m_h: fixed_t = 0

    var min_x: fixed_t = 0

    var min_y: fixed_t = 0

    var max_x: fixed_t = 0

    var max_y: fixed_t = 0

    var max_w: fixed_t = 0

    var max_h: fixed_t = 0

    var min_w: fixed_t = 0

    var min_h: fixed_t = 0

    var min_scale_mtof: fixed_t = 0

    var max_scale_mtof: fixed_t = 0

    var old_m_w: fixed_t = 0

    var old_m_h: fixed_t = 0

    var old_m_x: fixed_t = 0

    var old_m_y: fixed_t = 0

    val f_oldloc by lazy(LazyThreadSafetyMode.NONE) { mpoint_t() }

    var scale_mtof: fixed_t = INITSCALEMTOF

    var scale_ftom: fixed_t = 0

    lateinit var plr: player_t

    val marknums by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<ByteArray>(10) }

    val markpoints by lazy(LazyThreadSafetyMode.NONE) { Array(AM_NUMMARKPOINTS) { mpoint_t() } }

    var markpointnum = 0

    var followplayer = 1

    val cheat_amap_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0xb2, 0x26, 0x26, 0x2e, 0xff) }

    val cheat_amap by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_amap_seq, 0) }

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

    val fl by lazy(LazyThreadSafetyMode.NONE) { fline_t() }

    val l by lazy(LazyThreadSafetyMode.NONE) { mline_t() }

    val their_colors by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(GREENS, GRAYS, BROWNS, REDS) }
}
