// Port of linuxdoom-1.10 f_finale.c -- game completion, final screen animation.
// (F_DrawPatchFlipped is the vanilla v_video.c V_DrawPatchFlipped, kept local
// here because the cast drawer is its only caller.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class FinaleState {
    var finalestage = 0

    var finalecount = 0

    var e1text = E1TEXT

    var e2text = E2TEXT

    var e3text = E3TEXT

    var e4text = E4TEXT

    var c1text = C1TEXT

    var c2text = C2TEXT

    var c3text = C3TEXT

    var c4text = C4TEXT

    var c5text = C5TEXT

    var c6text = C6TEXT

    var p1text = P1TEXT

    var p2text = P2TEXT

    var p3text = P3TEXT

    var p4text = P4TEXT

    var p5text = P5TEXT

    var p6text = P6TEXT

    var t1text = T1TEXT

    var t2text = T2TEXT

    var t3text = T3TEXT

    var t4text = T4TEXT

    var t5text = T5TEXT

    var t6text = T6TEXT

    var finaletext = ""

    var finaleflat = ""

    val castorder by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        castinfo_t(CC_ZOMBIE, MT_POSSESSED),
        castinfo_t(CC_SHOTGUN, MT_SHOTGUY),
        castinfo_t(CC_HEAVY, MT_CHAINGUY),
        castinfo_t(CC_IMP, MT_TROOP),
        castinfo_t(CC_DEMON, MT_SERGEANT),
        castinfo_t(CC_LOST, MT_SKULL),
        castinfo_t(CC_CACO, MT_HEAD),
        castinfo_t(CC_HELL, MT_KNIGHT),
        castinfo_t(CC_BARON, MT_BRUISER),
        castinfo_t(CC_ARACH, MT_BABY),
        castinfo_t(CC_PAIN, MT_PAIN),
        castinfo_t(CC_REVEN, MT_UNDEAD),
        castinfo_t(CC_MANCU, MT_FATSO),
        castinfo_t(CC_ARCH, MT_VILE),
        castinfo_t(CC_SPIDER, MT_SPIDER),
        castinfo_t(CC_CYBER, MT_CYBORG),
        castinfo_t(CC_HERO, MT_PLAYER),

        castinfo_t(null, 0),
    ) }

    var castnum = 0

    var casttics = 0

    lateinit var caststate: state_t

    var castdeath = false

    var castframes = 0

    var castonmelee = 0

    var castattacking = false

    var laststage = 0
}
