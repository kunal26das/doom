// Port of linuxdoom-1.10 wi_stuff.c/wi_stuff.h -- intermission screens.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class IntermissionState {
    val lnodes: Array<Array<point_t>> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        // Episode 0 World Map
        arrayOf(
            point_t(185, 164),  // location of level 0 (CJ)
            point_t(148, 143),  // location of level 1 (CJ)
            point_t(69, 122),   // location of level 2 (CJ)
            point_t(209, 102),  // location of level 3 (CJ)
            point_t(116, 89),   // location of level 4 (CJ)
            point_t(166, 55),   // location of level 5 (CJ)
            point_t(71, 56),    // location of level 6 (CJ)
            point_t(135, 29),   // location of level 7 (CJ)
            point_t(71, 24),    // location of level 8 (CJ)
        ),

        // Episode 1 World Map should go here
        arrayOf(
            point_t(254, 25),   // location of level 0 (CJ)
            point_t(97, 50),    // location of level 1 (CJ)
            point_t(188, 64),   // location of level 2 (CJ)
            point_t(128, 78),   // location of level 3 (CJ)
            point_t(214, 92),   // location of level 4 (CJ)
            point_t(133, 130),  // location of level 5 (CJ)
            point_t(208, 136),  // location of level 6 (CJ)
            point_t(148, 140),  // location of level 7 (CJ)
            point_t(235, 158),  // location of level 8 (CJ)
        ),

        // Episode 2 World Map should go here
        arrayOf(
            point_t(156, 168),  // location of level 0 (CJ)
            point_t(48, 154),   // location of level 1 (CJ)
            point_t(174, 95),   // location of level 2 (CJ)
            point_t(265, 75),   // location of level 3 (CJ)
            point_t(130, 48),   // location of level 4 (CJ)
            point_t(279, 23),   // location of level 5 (CJ)
            point_t(198, 48),   // location of level 6 (CJ)
            point_t(140, 25),   // location of level 7 (CJ)
            point_t(281, 136),  // location of level 8 (CJ)
        ),
    ) }

    val epsd0animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(224, 104)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(184, 160)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(112, 136)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(72, 112)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(88, 96)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(64, 48)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(192, 40)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(136, 16)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(80, 16)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(64, 24)),
    ) }

    val epsd1animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 1),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 2),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 3),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 4),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 5),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 6),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 7),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 3, point_t(192, 144), 8),
        wianim_t(ANIM_LEVEL, TICRATE / 3, 1, point_t(128, 136), 8),
    ) }

    val epsd2animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(104, 168)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(40, 136)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(160, 96)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(104, 80)),
        wianim_t(ANIM_ALWAYS, TICRATE / 3, 3, point_t(120, 32)),
        wianim_t(ANIM_ALWAYS, TICRATE / 4, 3, point_t(40, 0)),
    ) }

    val NUMANIMS by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        epsd0animinfo.size,
        epsd1animinfo.size,
        epsd2animinfo.size,
    ) }

    val wi_anims by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        epsd0animinfo,
        epsd1animinfo,
        epsd2animinfo,
    ) }

    var acceleratestage = 0

    var me = 0

    var state = StatCount

    lateinit var wbs: wbstartstruct_t

    lateinit var plrs: Array<wbplayerstruct_t>

    var cnt = 0

    var bcnt = 0

    var firstrefresh = 0

    val cnt_kills by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cnt_items by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cnt_secret by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    var cnt_time = 0

    var cnt_par = 0

    var cnt_pause = 0

    var NUMCMAPS = 0

    var bg: ByteArray = ByteArray(0)

    val yah by lazy(LazyThreadSafetyMode.NONE) { Array(2) { ByteArray(0) } }

    var splat: ByteArray = ByteArray(0)

    var percent: ByteArray = ByteArray(0)

    var colon: ByteArray = ByteArray(0)

    val num by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    var wiminus: ByteArray = ByteArray(0)

    var finished: ByteArray = ByteArray(0)

    var entering: ByteArray = ByteArray(0)

    var sp_secret: ByteArray = ByteArray(0)

    var kills: ByteArray = ByteArray(0)

    var secret: ByteArray = ByteArray(0)

    var items: ByteArray = ByteArray(0)

    var frags: ByteArray = ByteArray(0)

    var time: ByteArray = ByteArray(0)

    var par: ByteArray = ByteArray(0)

    var sucks: ByteArray = ByteArray(0)

    var killers: ByteArray = ByteArray(0)

    var victims: ByteArray = ByteArray(0)

    var total: ByteArray = ByteArray(0)

    var star: ByteArray = ByteArray(0)

    var bstar: ByteArray = ByteArray(0)

    val p by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { ByteArray(0) } }

    val bp by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { ByteArray(0) } }

    var lnames: Array<ByteArray> = emptyArray()

    var snl_pointeron = false

    var dm_state = 0

    val dm_frags by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { IntArray(MAXPLAYERS) } }

    val dm_totals by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cnt_frags by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    var dofrags = 0

    var ng_state = 0

    var sp_state = 0
}
