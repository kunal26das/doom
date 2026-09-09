
package doom.engine.intermission

import doom.engine.TICRATE
import doom.engine.gameplay.MAXPLAYERS

internal class IntermissionState {
    val lnodes: Array<Array<IntermissionPoint>> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        arrayOf(
            IntermissionPoint(185, 164),
            IntermissionPoint(148, 143),
            IntermissionPoint(69, 122),
            IntermissionPoint(209, 102),
            IntermissionPoint(116, 89),
            IntermissionPoint(166, 55),
            IntermissionPoint(71, 56),
            IntermissionPoint(135, 29),
            IntermissionPoint(71, 24),
        ),

        arrayOf(
            IntermissionPoint(254, 25),
            IntermissionPoint(97, 50),
            IntermissionPoint(188, 64),
            IntermissionPoint(128, 78),
            IntermissionPoint(214, 92),
            IntermissionPoint(133, 130),
            IntermissionPoint(208, 136),
            IntermissionPoint(148, 140),
            IntermissionPoint(235, 158),
        ),

        arrayOf(
            IntermissionPoint(156, 168),
            IntermissionPoint(48, 154),
            IntermissionPoint(174, 95),
            IntermissionPoint(265, 75),
            IntermissionPoint(130, 48),
            IntermissionPoint(279, 23),
            IntermissionPoint(198, 48),
            IntermissionPoint(140, 25),
            IntermissionPoint(281, 136),
        ),
    ) }

    val epsd0animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(224, 104)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(184, 160)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(112, 136)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(72, 112)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(88, 96)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(64, 48)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(192, 40)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(136, 16)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(80, 16)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(64, 24)),
    ) }

    val epsd1animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 1),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 2),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 3),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 4),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 5),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 6),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 7),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 3, IntermissionPoint(192, 144), 8),
        IntermissionAnimation(ANIM_LEVEL, TICRATE / 3, 1, IntermissionPoint(128, 136), 8),
    ) }

    val epsd2animinfo by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(104, 168)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(40, 136)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(160, 96)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(104, 80)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 3, 3, IntermissionPoint(120, 32)),
        IntermissionAnimation(ANIM_ALWAYS, TICRATE / 4, 3, IntermissionPoint(40, 0)),
    ) }

    val numanims by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        epsd0animinfo.size,
        epsd1animinfo.size,
        epsd2animinfo.size,
    ) }

    val wiAnims by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        epsd0animinfo,
        epsd1animinfo,
        epsd2animinfo,
    ) }

    var acceleratestage = 0

    var me = 0

    var state = STAT_COUNT

    lateinit var wbs: IntermissionSummary

    lateinit var plrs: Array<IntermissionPlayerStats>

    var cnt = 0

    var bcnt = 0

    var firstrefresh = 0

    val cntKills by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cntItems by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cntSecret by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    var cntTime = 0

    var cntPar = 0

    var cntPause = 0

    var numcmaps = 0

    var bg: ByteArray = ByteArray(0)

    val yah by lazy(LazyThreadSafetyMode.NONE) { Array(2) { ByteArray(0) } }

    var splat: ByteArray = ByteArray(0)

    var percent: ByteArray = ByteArray(0)

    var colon: ByteArray = ByteArray(0)

    val num by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    var wiminus: ByteArray = ByteArray(0)

    var finished: ByteArray = ByteArray(0)

    var entering: ByteArray = ByteArray(0)

    var spSecret: ByteArray = ByteArray(0)

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

    var snlPointeron = false

    var dmState = 0

    val dmFrags by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { IntArray(MAXPLAYERS) } }

    val dmTotals by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val cntFrags by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    var dofrags = 0

    var ngState = 0

    var spState = 0
}
