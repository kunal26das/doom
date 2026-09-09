
package doom.engine.finale

import doom.engine.gameplay.actors.MT_BABY
import doom.engine.gameplay.actors.MT_BRUISER
import doom.engine.gameplay.actors.MT_CHAINGUY
import doom.engine.gameplay.actors.MT_CYBORG
import doom.engine.gameplay.actors.MT_FATSO
import doom.engine.gameplay.actors.MT_HEAD
import doom.engine.gameplay.actors.MT_KNIGHT
import doom.engine.gameplay.actors.MT_PAIN
import doom.engine.gameplay.actors.MT_PLAYER
import doom.engine.gameplay.actors.MT_POSSESSED
import doom.engine.gameplay.actors.MT_SERGEANT
import doom.engine.gameplay.actors.MT_SHOTGUY
import doom.engine.gameplay.actors.MT_SKULL
import doom.engine.gameplay.actors.MT_SPIDER
import doom.engine.gameplay.actors.MT_TROOP
import doom.engine.gameplay.actors.MT_UNDEAD
import doom.engine.gameplay.actors.MT_VILE
import doom.engine.gameplay.actors.StateDefinition
import doom.engine.resources.C1TEXT
import doom.engine.resources.C2TEXT
import doom.engine.resources.C3TEXT
import doom.engine.resources.C4TEXT
import doom.engine.resources.C5TEXT
import doom.engine.resources.C6TEXT
import doom.engine.resources.CC_ARACH
import doom.engine.resources.CC_ARCH
import doom.engine.resources.CC_BARON
import doom.engine.resources.CC_CACO
import doom.engine.resources.CC_CYBER
import doom.engine.resources.CC_DEMON
import doom.engine.resources.CC_HEAVY
import doom.engine.resources.CC_HELL
import doom.engine.resources.CC_HERO
import doom.engine.resources.CC_IMP
import doom.engine.resources.CC_LOST
import doom.engine.resources.CC_MANCU
import doom.engine.resources.CC_PAIN
import doom.engine.resources.CC_REVEN
import doom.engine.resources.CC_SHOTGUN
import doom.engine.resources.CC_SPIDER
import doom.engine.resources.CC_ZOMBIE
import doom.engine.resources.E1TEXT
import doom.engine.resources.E2TEXT
import doom.engine.resources.E3TEXT
import doom.engine.resources.E4TEXT
import doom.engine.resources.P1TEXT
import doom.engine.resources.P2TEXT
import doom.engine.resources.P3TEXT
import doom.engine.resources.P4TEXT
import doom.engine.resources.P5TEXT
import doom.engine.resources.P6TEXT
import doom.engine.resources.T1TEXT
import doom.engine.resources.T2TEXT
import doom.engine.resources.T3TEXT
import doom.engine.resources.T4TEXT
import doom.engine.resources.T5TEXT
import doom.engine.resources.T6TEXT

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
        CastMember(CC_ZOMBIE, MT_POSSESSED),
        CastMember(CC_SHOTGUN, MT_SHOTGUY),
        CastMember(CC_HEAVY, MT_CHAINGUY),
        CastMember(CC_IMP, MT_TROOP),
        CastMember(CC_DEMON, MT_SERGEANT),
        CastMember(CC_LOST, MT_SKULL),
        CastMember(CC_CACO, MT_HEAD),
        CastMember(CC_HELL, MT_KNIGHT),
        CastMember(CC_BARON, MT_BRUISER),
        CastMember(CC_ARACH, MT_BABY),
        CastMember(CC_PAIN, MT_PAIN),
        CastMember(CC_REVEN, MT_UNDEAD),
        CastMember(CC_MANCU, MT_FATSO),
        CastMember(CC_ARCH, MT_VILE),
        CastMember(CC_SPIDER, MT_SPIDER),
        CastMember(CC_CYBER, MT_CYBORG),
        CastMember(CC_HERO, MT_PLAYER),

        CastMember(null, 0),
    ) }

    var castnum = 0

    var casttics = 0

    lateinit var caststate: StateDefinition

    var castdeath = false

    var castframes = 0

    var castonmelee = 0

    var castattacking = false

    var laststage = 0
}
