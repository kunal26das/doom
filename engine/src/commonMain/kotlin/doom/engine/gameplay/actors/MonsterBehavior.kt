
package doom.engine.gameplay.actors

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_BAREXP
import doom.engine.audio.SFX_BGDTH1
import doom.engine.audio.SFX_BGDTH2
import doom.engine.audio.SFX_BGSIT1
import doom.engine.audio.SFX_BGSIT2
import doom.engine.audio.SFX_BOSCUB
import doom.engine.audio.SFX_BOSDTH
import doom.engine.audio.SFX_BOSPIT
import doom.engine.audio.SFX_BOSPN
import doom.engine.audio.SFX_BOSSIT
import doom.engine.audio.SFX_BSPWLK
import doom.engine.audio.SFX_CLAW
import doom.engine.audio.SFX_DBCLS
import doom.engine.audio.SFX_DBLOAD
import doom.engine.audio.SFX_DBOPN
import doom.engine.audio.SFX_FLAME
import doom.engine.audio.SFX_FLAMST
import doom.engine.audio.SFX_HOOF
import doom.engine.audio.SFX_MANATK
import doom.engine.audio.SFX_METAL
import doom.engine.audio.SFX_PDIEHI
import doom.engine.audio.SFX_PISTOL
import doom.engine.audio.SFX_PLDETH
import doom.engine.audio.SFX_PODTH1
import doom.engine.audio.SFX_PODTH2
import doom.engine.audio.SFX_PODTH3
import doom.engine.audio.SFX_POSIT1
import doom.engine.audio.SFX_POSIT2
import doom.engine.audio.SFX_POSIT3
import doom.engine.audio.SFX_SHOTGN
import doom.engine.audio.SFX_SKEPCH
import doom.engine.audio.SFX_SKESWG
import doom.engine.audio.SFX_SLOP
import doom.engine.audio.SFX_TELEPT
import doom.engine.audio.SFX_VILATK
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.core.fastparm
import doom.engine.gameplay.gExitLevel
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gameskill
import doom.engine.gameplay.gametic
import doom.engine.gameplay.interactions.pDamageMobj
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.SK_EASY
import doom.engine.gameplay.SK_NIGHTMARE
import doom.engine.gameplay.weapons.aReFire
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG270
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.rendering.validcount
import doom.engine.simulation.pRandom
import doom.engine.simulation.Thinker
import doom.engine.simulation.thinkercap
import doom.engine.world.FLOATSPEED
import doom.engine.world.MAPBLOCKSHIFT
import doom.engine.world.MAXRADIUS
import doom.engine.world.MELEERANGE
import doom.engine.world.MISSILERANGE
import doom.engine.world.ML_SOUNDBLOCK
import doom.engine.world.ML_TWOSIDED
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.bmaporgx
import doom.engine.world.bmaporgy
import doom.engine.world.collision.pAimLineAttack
import doom.engine.world.collision.pAproxDistance
import doom.engine.world.collision.pBlockThingsIterator
import doom.engine.world.collision.pCheckPosition
import doom.engine.world.collision.pLineAttack
import doom.engine.world.collision.pLineOpening
import doom.engine.world.collision.pRadiusAttack
import doom.engine.world.collision.pSetThingPosition
import doom.engine.world.collision.pTeleportMove
import doom.engine.world.collision.pTryMove
import doom.engine.world.collision.pUnsetThingPosition
import doom.engine.world.collision.floatok
import doom.engine.world.collision.numspechit
import doom.engine.world.collision.openrange
import doom.engine.world.collision.spechit
import doom.engine.world.collision.tmfloorz
import doom.engine.world.movers.evDoDoor
import doom.engine.world.movers.evDoFloor
import doom.engine.world.movers.BLAZE_OPEN
import doom.engine.world.movers.LOWER_FLOOR_TO_LOWEST
import doom.engine.world.movers.OPEN
import doom.engine.world.movers.RAISE_TO_TEXTURE
import doom.engine.world.sides
import doom.engine.world.specials.pUseSpecialLine
import doom.engine.world.visibility.pCheckSight

import kotlin.math.abs

internal const val DI_EAST = 0
internal const val DI_NORTHEAST = 1
internal const val DI_NORTH = 2
internal const val DI_NORTHWEST = 3
internal const val DI_WEST = 4
internal const val DI_SOUTHWEST = 5
internal const val DI_SOUTH = 6
internal const val DI_SOUTHEAST = 7
internal const val DI_NODIR = 8
internal const val NUMDIRS = 9

internal val DoomEngineCore.opposite
    get() = stateMonsterBehavior.opposite

internal val DoomEngineCore.diags
    get() = stateMonsterBehavior.diags



internal var DoomEngineCore.soundtarget: Actor?
    get() = stateMonsterBehavior.soundtarget
    set(value) { stateMonsterBehavior.soundtarget = value }

internal fun DoomEngineCore.pRecursiveSound(sec: Sector, soundblocks: Int) {
    var i: Int
    var check: MapLine
    var other: Sector?

    if (sec.validcount == validcount
        && sec.soundtraversed <= soundblocks + 1) {
        return
    }

    sec.validcount = validcount
    sec.soundtraversed = soundblocks + 1
    sec.soundtarget = soundtarget

    i = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        if ((check.flags and ML_TWOSIDED) == 0) {
            i++
            continue
        }

        pLineOpening(check)

        if (openrange <= 0) {
            i++
            continue
        }

        if (sides[check.sidenum[0]].sector === sec)
            other = sides[check.sidenum[1]].sector
        else
            other = sides[check.sidenum[0]].sector

        if ((check.flags and ML_SOUNDBLOCK) != 0) {
            if (soundblocks == 0)
                pRecursiveSound(other!!, 1)
        } else
            pRecursiveSound(other!!, soundblocks)

        i++
    }
}

internal fun DoomEngineCore.pNoiseAlert(target: Actor, emmiter: Actor) {
    soundtarget = target
    validcount++
    pRecursiveSound(emmiter.subsector!!.sector!!, 0)
}

internal fun DoomEngineCore.pCheckMeleeRange(actor: Actor): Boolean {
    val pl: Actor
    val dist: FixedPoint

    if (actor.target == null)
        return false

    pl = actor.target!!
    dist = pAproxDistance(pl.x - actor.x, pl.y - actor.y)

    if (dist >= MELEERANGE - 20 * FRACUNIT + pl.info!!.radius)
        return false

    if (!pCheckSight(actor, actor.target!!))
        return false

    return true
}

internal fun DoomEngineCore.pCheckMissileRange(actor: Actor): Boolean {
    var dist: FixedPoint

    if (!pCheckSight(actor, actor.target!!))
        return false

    if ((actor.flags and MF_JUSTHIT) != 0) {
        actor.flags = actor.flags and MF_JUSTHIT.inv()
        return true
    }

    if (actor.reactiontime != 0)
        return false

    dist = pAproxDistance(actor.x - actor.target!!.x,
        actor.y - actor.target!!.y) - 64 * FRACUNIT

    if (actor.info!!.meleestate == 0)
        dist -= 128 * FRACUNIT

    dist = dist shr 16

    if (actor.type == MT_VILE) {
        if (dist > 14 * 64)
            return false
    }

    if (actor.type == MT_UNDEAD) {
        if (dist < 196)
            return false
        dist = dist shr 1
    }

    if (actor.type == MT_CYBORG
        || actor.type == MT_SPIDER
        || actor.type == MT_SKULL) {
        dist = dist shr 1
    }

    if (dist > 200)
        dist = 200

    if (actor.type == MT_CYBORG && dist > 160)
        dist = 160

    if (pRandom() < dist)
        return false

    return true
}

internal val DoomEngineCore.xspeed
    get() = stateMonsterBehavior.xspeed
internal val DoomEngineCore.yspeed
    get() = stateMonsterBehavior.yspeed


internal fun DoomEngineCore.pMove(actor: Actor): Boolean {
    val tryx: FixedPoint
    val tryy: FixedPoint

    var ld: MapLine

    val tryOk: Boolean
    var good: Boolean

    if (actor.movedir == DI_NODIR)
        return false

    if (actor.movedir.toUInt() >= 8u)
        iError("Weird actor->movedir!")

    tryx = actor.x + actor.info!!.speed * xspeed[actor.movedir]
    tryy = actor.y + actor.info!!.speed * yspeed[actor.movedir]

    tryOk = pTryMove(actor, tryx, tryy)

    if (!tryOk) {
        if ((actor.flags and MF_FLOAT) != 0 && floatok) {
            if (actor.z < tmfloorz)
                actor.z += FLOATSPEED
            else
                actor.z -= FLOATSPEED

            actor.flags = actor.flags or MF_INFLOAT
            return true
        }

        if (numspechit == 0)
            return false

        actor.movedir = DI_NODIR
        good = false
        while (true) {
            val numspechitWas = numspechit
            numspechit--
            if (numspechitWas == 0)
                break
            ld = spechit[numspechit]!!
            if (pUseSpecialLine(actor, ld, 0))
                good = true
        }
        return good
    } else {
        actor.flags = actor.flags and MF_INFLOAT.inv()
    }

    if ((actor.flags and MF_FLOAT) == 0)
        actor.z = actor.floorz
    return true
}

internal fun DoomEngineCore.pTryWalk(actor: Actor): Boolean {
    if (!pMove(actor)) {
        return false
    }

    actor.movecount = pRandom() and 15
    return true
}

internal fun DoomEngineCore.pNewChaseDir(actor: Actor) {
    val deltax: FixedPoint
    val deltay: FixedPoint

    val d = IntArray(3)

    var tdir: Int
    val olddir: Int

    val turnaround: Int

    if (actor.target == null)
        iError("P_NewChaseDir: called with no target")

    olddir = actor.movedir
    turnaround = opposite[olddir]

    deltax = actor.target!!.x - actor.x
    deltay = actor.target!!.y - actor.y

    if (deltax > 10 * FRACUNIT)
        d[1] = DI_EAST
    else if (deltax < -10 * FRACUNIT)
        d[1] = DI_WEST
    else
        d[1] = DI_NODIR

    if (deltay < -10 * FRACUNIT)
        d[2] = DI_SOUTH
    else if (deltay > 10 * FRACUNIT)
        d[2] = DI_NORTH
    else
        d[2] = DI_NODIR

    if (d[1] != DI_NODIR
        && d[2] != DI_NODIR) {
        actor.movedir = diags[((if (deltay < 0) 1 else 0) shl 1) + (if (deltax > 0) 1 else 0)]
        if (actor.movedir != turnaround && pTryWalk(actor))
            return
    }

    if (pRandom() > 200
        || abs(deltay) > abs(deltax)) {
        tdir = d[1]
        d[1] = d[2]
        d[2] = tdir
    }

    if (d[1] == turnaround)
        d[1] = DI_NODIR
    if (d[2] == turnaround)
        d[2] = DI_NODIR

    if (d[1] != DI_NODIR) {
        actor.movedir = d[1]
        if (pTryWalk(actor)) {
            return
        }
    }

    if (d[2] != DI_NODIR) {
        actor.movedir = d[2]

        if (pTryWalk(actor))
            return
    }

    if (olddir != DI_NODIR) {
        actor.movedir = olddir

        if (pTryWalk(actor))
            return
    }

    if ((pRandom() and 1) != 0) {
        tdir = DI_EAST
        while (tdir <= DI_SOUTHEAST) {
            if (tdir != turnaround) {
                actor.movedir = tdir

                if (pTryWalk(actor))
                    return
            }
            tdir++
        }
    } else {
        tdir = DI_SOUTHEAST
        while (tdir != (DI_EAST - 1)) {
            if (tdir != turnaround) {
                actor.movedir = tdir

                if (pTryWalk(actor))
                    return
            }
            tdir--
        }
    }

    if (turnaround != DI_NODIR) {
        actor.movedir = turnaround
        if (pTryWalk(actor))
            return
    }

    actor.movedir = DI_NODIR
}

internal fun DoomEngineCore.pLookForPlayers(actor: Actor, allaround: Boolean): Boolean {
    var c: Int
    val stop: Int

    c = 0
    stop = (actor.lastlook - 1) and 3

    while (true) {
        run {
            if (!playeringame[actor.lastlook])
                return@run

            val cWas = c
            c++
            if (cWas == 2
                || actor.lastlook == stop) {
                return false
            }

            val player = players[actor.lastlook]

            if (player.health <= 0)
                return@run

            if (!pCheckSight(actor, player.mo!!))
                return@run

            if (!allaround) {
                val an: BinaryAngle = FixedGeometry.angleBetween(actor.x,
                    actor.y,
                    player.mo!!.x,
                    player.mo!!.y) - actor.angle

                if (an > ANG90 && an < ANG270) {
                    val dist: FixedPoint = pAproxDistance(player.mo!!.x - actor.x,
                        player.mo!!.y - actor.y)
                    if (dist > MELEERANGE)
                        return@run
                }
            }

            actor.target = player.mo
            return true
        }
        actor.lastlook = (actor.lastlook + 1) and 3
    }

}

internal fun DoomEngineCore.aKeenDie(mo: Actor) {
    var th: Thinker
    val junk = MapLine()

    aFall(mo)

    th = thinkercap.next!!
    while (th !== thinkercap) {
        if (th is Actor && !th.removed) {
            val mo2: Actor = th
            if (mo2 !== mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                return
            }
        }
        th = th.next!!
    }

    junk.tag = 666
    evDoDoor(junk, OPEN)
}


internal fun DoomEngineCore.aLook(actor: Actor) {
    val targ: Actor?

    actor.threshold = 0
    targ = actor.subsector!!.sector!!.soundtarget

    var seeyou = false
    if (targ != null
        && (targ.flags and MF_SHOOTABLE) != 0) {
        actor.target = targ

        if ((actor.flags and MF_AMBUSH) != 0) {
            if (pCheckSight(actor, actor.target!!))
                seeyou = true
        } else
            seeyou = true
    }

    if (!seeyou) {
        if (!pLookForPlayers(actor, false))
            return
    }

    if (actor.info!!.seesound != 0) {
        val sound: Int

        when (actor.info!!.seesound) {
            SFX_POSIT1, SFX_POSIT2, SFX_POSIT3 ->
                sound = SFX_POSIT1 + pRandom() % 3

            SFX_BGSIT1, SFX_BGSIT2 ->
                sound = SFX_BGSIT1 + pRandom() % 2

            else ->
                sound = actor.info!!.seesound
        }

        if (actor.type == MT_SPIDER
            || actor.type == MT_CYBORG) {
            sStartSound(null, sound)
        } else
            sStartSound(actor, sound)
    }

    pSetMobjState(actor, actor.info!!.seestate)
}

internal fun DoomEngineCore.aChase(actor: Actor) {
    val delta: Int

    if (actor.reactiontime != 0)
        actor.reactiontime--

    if (actor.threshold != 0) {
        if (actor.target == null
            || actor.target!!.health <= 0) {
            actor.threshold = 0
        } else
            actor.threshold--
    }

    if (actor.movedir < 8) {
        actor.angle = actor.angle and (7 shl 29).toUInt()
        delta = actor.angle.toInt() - (actor.movedir shl 29)

        if (delta > 0)
            actor.angle -= ANG90 / 2u
        else if (delta < 0)
            actor.angle += ANG90 / 2u
    }

    if (actor.target == null
        || (actor.target!!.flags and MF_SHOOTABLE) == 0) {
        if (pLookForPlayers(actor, true))
            return

        pSetMobjState(actor, actor.info!!.spawnstate)
        return
    }

    if ((actor.flags and MF_JUSTATTACKED) != 0) {
        actor.flags = actor.flags and MF_JUSTATTACKED.inv()
        if (gameskill != SK_NIGHTMARE && !fastparm)
            pNewChaseDir(actor)
        return
    }

    if (actor.info!!.meleestate != 0
        && pCheckMeleeRange(actor)) {
        if (actor.info!!.attacksound != 0)
            sStartSound(actor, actor.info!!.attacksound)

        pSetMobjState(actor, actor.info!!.meleestate)
        return
    }

    if (actor.info!!.missilestate != 0) {
        var nomissile = false
        if (gameskill < SK_NIGHTMARE
            && !fastparm && actor.movecount != 0) {
            nomissile = true
        }

        if (!nomissile && !pCheckMissileRange(actor))
            nomissile = true

        if (!nomissile) {
            pSetMobjState(actor, actor.info!!.missilestate)
            actor.flags = actor.flags or MF_JUSTATTACKED
            return
        }
    }

    if (netgame
        && actor.threshold == 0
        && !pCheckSight(actor, actor.target!!)) {
        if (pLookForPlayers(actor, true))
            return
    }

    actor.movecount--
    if (actor.movecount < 0
        || !pMove(actor)) {
        pNewChaseDir(actor)
    }

    if (actor.info!!.activesound != 0
        && pRandom() < 3) {
        sStartSound(actor, actor.info!!.activesound)
    }
}

internal fun DoomEngineCore.aFaceTarget(actor: Actor) {
    if (actor.target == null)
        return

    actor.flags = actor.flags and MF_AMBUSH.inv()

    actor.angle = FixedGeometry.angleBetween(actor.x,
        actor.y,
        actor.target!!.x,
        actor.target!!.y)

    if ((actor.target!!.flags and MF_SHADOW) != 0)
        actor.angle += ((pRandom() - pRandom()) shl 21).toUInt()
}

internal fun DoomEngineCore.aPosAttack(actor: Actor) {
    var angle: Int
    val damage: Int
    val slope: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)
    angle = actor.angle.toInt()
    slope = pAimLineAttack(actor, angle.toUInt(), MISSILERANGE)

    sStartSound(actor, SFX_PISTOL)
    angle += (pRandom() - pRandom()) shl 20
    damage = ((pRandom() % 5) + 1) * 3
    pLineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
}

internal fun DoomEngineCore.aSPosAttack(actor: Actor) {
    var i: Int
    var angle: Int
    val bangle: Int
    var damage: Int
    val slope: Int

    if (actor.target == null)
        return

    sStartSound(actor, SFX_SHOTGN)
    aFaceTarget(actor)
    bangle = actor.angle.toInt()
    slope = pAimLineAttack(actor, bangle.toUInt(), MISSILERANGE)

    i = 0
    while (i < 3) {
        angle = bangle + ((pRandom() - pRandom()) shl 20)
        damage = ((pRandom() % 5) + 1) * 3
        pLineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
        i++
    }
}

internal fun DoomEngineCore.aCPosAttack(actor: Actor) {
    val angle: Int
    val bangle: Int
    val damage: Int
    val slope: Int

    if (actor.target == null)
        return

    sStartSound(actor, SFX_SHOTGN)
    aFaceTarget(actor)
    bangle = actor.angle.toInt()
    slope = pAimLineAttack(actor, bangle.toUInt(), MISSILERANGE)

    angle = bangle + ((pRandom() - pRandom()) shl 20)
    damage = ((pRandom() % 5) + 1) * 3
    pLineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
}

internal fun DoomEngineCore.aCPosRefire(actor: Actor) {
    aFaceTarget(actor)

    if (pRandom() < 40)
        return

    if (actor.target == null
        || actor.target!!.health <= 0
        || !pCheckSight(actor, actor.target!!)) {
        pSetMobjState(actor, actor.info!!.seestate)
    }
}

internal fun DoomEngineCore.aSpidRefire(actor: Actor) {
    aFaceTarget(actor)

    if (pRandom() < 10)
        return

    if (actor.target == null
        || actor.target!!.health <= 0
        || !pCheckSight(actor, actor.target!!)) {
        pSetMobjState(actor, actor.info!!.seestate)
    }
}

internal fun DoomEngineCore.aBspiAttack(actor: Actor) {
    if (actor.target == null)
        return

    aFaceTarget(actor)

    pSpawnMissile(actor, actor.target!!, MT_ARACHPLAZ)
}

internal fun DoomEngineCore.aTroopAttack(actor: Actor) {
    val damage: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)
    if (pCheckMeleeRange(actor)) {
        sStartSound(actor, SFX_CLAW)
        damage = (pRandom() % 8 + 1) * 3
        pDamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    pSpawnMissile(actor, actor.target!!, MT_TROOPSHOT)
}

internal fun DoomEngineCore.aSargAttack(actor: Actor) {
    val damage: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)
    if (pCheckMeleeRange(actor)) {
        damage = ((pRandom() % 10) + 1) * 4
        pDamageMobj(actor.target!!, actor, actor, damage)
    }
}

internal fun DoomEngineCore.aHeadAttack(actor: Actor) {
    val damage: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)
    if (pCheckMeleeRange(actor)) {
        damage = (pRandom() % 6 + 1) * 10
        pDamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    pSpawnMissile(actor, actor.target!!, MT_HEADSHOT)
}

internal fun DoomEngineCore.aCyberAttack(actor: Actor) {
    if (actor.target == null)
        return

    aFaceTarget(actor)
    pSpawnMissile(actor, actor.target!!, MT_ROCKET)
}

internal fun DoomEngineCore.aBruisAttack(actor: Actor) {
    val damage: Int

    if (actor.target == null)
        return

    if (pCheckMeleeRange(actor)) {
        sStartSound(actor, SFX_CLAW)
        damage = (pRandom() % 8 + 1) * 10
        pDamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    pSpawnMissile(actor, actor.target!!, MT_BRUISERSHOT)
}

internal fun DoomEngineCore.aSkelMissile(actor: Actor) {
    val mo: Actor

    if (actor.target == null)
        return

    aFaceTarget(actor)
    actor.z += 16 * FRACUNIT
    mo = pSpawnMissile(actor, actor.target!!, MT_TRACER)
    actor.z -= 16 * FRACUNIT

    mo.x += mo.momx
    mo.y += mo.momy
    mo.tracer = actor.target
}

internal var DoomEngineCore.traceangle
    get() = stateMonsterBehavior.traceangle
    set(value) { stateMonsterBehavior.traceangle = value }

internal fun DoomEngineCore.aTracer(actor: Actor) {
    var exact: BinaryAngle
    var dist: FixedPoint
    val slope: FixedPoint
    val dest: Actor?
    val th: Actor

    if ((gametic and 3) != 0)
        return

    pSpawnPuff(actor.x, actor.y, actor.z)

    th = pSpawnMobj(actor.x - actor.momx,
        actor.y - actor.momy,
        actor.z, MT_SMOKE)

    th.momz = FRACUNIT
    th.tics -= pRandom() and 3
    if (th.tics < 1)
        th.tics = 1

    dest = actor.tracer

    if (dest == null || dest.health <= 0)
        return

    exact = FixedGeometry.angleBetween(actor.x,
        actor.y,
        dest.x,
        dest.y)

    if (exact != actor.angle) {
        if (exact - actor.angle > 0x80000000u) {
            actor.angle -= traceangle.toUInt()
            if (exact - actor.angle < 0x80000000u)
                actor.angle = exact
        } else {
            actor.angle += traceangle.toUInt()
            if (exact - actor.angle > 0x80000000u)
                actor.angle = exact
        }
    }

    exact = actor.angle shr ANGLETOFINESHIFT
    actor.momx = fixedMul(actor.info!!.speed, FineCosineTable[exact.toInt()])
    actor.momy = fixedMul(actor.info!!.speed, finesine[exact.toInt()])

    dist = pAproxDistance(dest.x - actor.x,
        dest.y - actor.y)

    dist = dist / actor.info!!.speed

    if (dist < 1)
        dist = 1
    slope = (dest.z + 40 * FRACUNIT - actor.z) / dist

    if (slope < actor.momz)
        actor.momz -= FRACUNIT / 8
    else
        actor.momz += FRACUNIT / 8
}

internal fun DoomEngineCore.aSkelWhoosh(actor: Actor) {
    if (actor.target == null)
        return
    aFaceTarget(actor)
    sStartSound(actor, SFX_SKESWG)
}

internal fun DoomEngineCore.aSkelFist(actor: Actor) {
    val damage: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)

    if (pCheckMeleeRange(actor)) {
        damage = ((pRandom() % 10) + 1) * 6
        sStartSound(actor, SFX_SKEPCH)
        pDamageMobj(actor.target!!, actor, actor, damage)
    }
}

internal var DoomEngineCore.corpsehit: Actor?
    get() = stateMonsterBehavior.corpsehit
    set(value) { stateMonsterBehavior.corpsehit = value }
internal var DoomEngineCore.vileobj: Actor?
    get() = stateMonsterBehavior.vileobj
    set(value) { stateMonsterBehavior.vileobj = value }
internal var DoomEngineCore.viletryx: FixedPoint
    get() = stateMonsterBehavior.viletryx
    set(value) { stateMonsterBehavior.viletryx = value }
internal var DoomEngineCore.viletryy: FixedPoint
    get() = stateMonsterBehavior.viletryy
    set(value) { stateMonsterBehavior.viletryy = value }

internal fun DoomEngineCore.pitVileCheck(thing: Actor): Boolean {
    val maxdist: Int
    val check: Boolean

    if ((thing.flags and MF_CORPSE) == 0)
        return true

    if (thing.tics != -1)
        return true

    if (thing.info!!.raisestate == S_NULL)
        return true

    maxdist = thing.info!!.radius + mobjinfo[MT_VILE].radius

    if (abs(thing.x - viletryx) > maxdist
        || abs(thing.y - viletryy) > maxdist)
        return true

    corpsehit = thing
    corpsehit!!.momx = 0
    corpsehit!!.momy = 0
    corpsehit!!.height = corpsehit!!.height shl 2
    check = pCheckPosition(corpsehit!!, corpsehit!!.x, corpsehit!!.y)
    corpsehit!!.height = corpsehit!!.height shr 2

    if (!check)
        return true

    return false
}

internal fun DoomEngineCore.aVileChase(actor: Actor) {
    val xl: Int
    val xh: Int
    val yl: Int
    val yh: Int

    var bx: Int
    var by: Int


    if (actor.movedir != DI_NODIR) {
        viletryx =
            actor.x + actor.info!!.speed * xspeed[actor.movedir]
        viletryy =
            actor.y + actor.info!!.speed * yspeed[actor.movedir]

        xl = (viletryx - bmaporgx - MAXRADIUS * 2) shr MAPBLOCKSHIFT
        xh = (viletryx - bmaporgx + MAXRADIUS * 2) shr MAPBLOCKSHIFT
        yl = (viletryy - bmaporgy - MAXRADIUS * 2) shr MAPBLOCKSHIFT
        yh = (viletryy - bmaporgy + MAXRADIUS * 2) shr MAPBLOCKSHIFT

        vileobj = actor
        bx = xl
        while (bx <= xh) {
            by = yl
            while (by <= yh) {
                if (!pBlockThingsIterator(bx, by, { argument0 -> pitVileCheck(argument0) })) {
                    val temp: Actor? = actor.target
                    actor.target = corpsehit
                    aFaceTarget(actor)
                    actor.target = temp

                    pSetMobjState(actor, S_VILE_HEAL1)
                    sStartSound(corpsehit, SFX_SLOP)
                    val info: ActorDefinition = corpsehit!!.info!!

                    pSetMobjState(corpsehit!!, info.raisestate)
                    corpsehit!!.height = corpsehit!!.height shl 2
                    corpsehit!!.flags = info.flags
                    corpsehit!!.health = info.spawnhealth
                    corpsehit!!.target = null

                    return
                }
                by++
            }
            bx++
        }
    }

    aChase(actor)
}

internal fun DoomEngineCore.aVileStart(actor: Actor) {
    sStartSound(actor, SFX_VILATK)
}


internal fun DoomEngineCore.aStartFire(actor: Actor) {
    sStartSound(actor, SFX_FLAMST)
    aFire(actor)
}

internal fun DoomEngineCore.aFireCrackle(actor: Actor) {
    sStartSound(actor, SFX_FLAME)
    aFire(actor)
}

internal fun DoomEngineCore.aFire(actor: Actor) {
    val dest: Actor?
    val an: Int

    dest = actor.tracer
    if (dest == null)
        return

    if (!pCheckSight(actor.target!!, dest))
        return

    an = (dest.angle shr ANGLETOFINESHIFT).toInt()

    pUnsetThingPosition(actor)
    actor.x = dest.x + fixedMul(24 * FRACUNIT, FineCosineTable[an])
    actor.y = dest.y + fixedMul(24 * FRACUNIT, finesine[an])
    actor.z = dest.z
    pSetThingPosition(actor)
}

internal fun DoomEngineCore.aVileTarget(actor: Actor) {
    val fog: Actor

    if (actor.target == null)
        return

    aFaceTarget(actor)

    fog = pSpawnMobj(actor.target!!.x,
        actor.target!!.x,
        actor.target!!.z, MT_FIRE)

    actor.tracer = fog
    fog.target = actor
    fog.tracer = actor.target
    aFire(fog)
}

internal fun DoomEngineCore.aVileAttack(actor: Actor) {
    val fire: Actor?
    val an: Int

    if (actor.target == null)
        return

    aFaceTarget(actor)

    if (!pCheckSight(actor, actor.target!!))
        return

    sStartSound(actor, SFX_BAREXP)
    pDamageMobj(actor.target!!, actor, actor, 20)
    actor.target!!.momz = 1000 * FRACUNIT / actor.target!!.info!!.mass

    an = (actor.angle shr ANGLETOFINESHIFT).toInt()

    fire = actor.tracer

    if (fire == null)
        return

    fire.x = actor.target!!.x - fixedMul(24 * FRACUNIT, FineCosineTable[an])
    fire.y = actor.target!!.y - fixedMul(24 * FRACUNIT, finesine[an])
    pRadiusAttack(fire, actor, 70)
}

internal val DoomEngineCore.fatspread: BinaryAngle
    get() = stateMonsterBehavior.fatspread

internal fun DoomEngineCore.aFatRaise(actor: Actor) {
    aFaceTarget(actor)
    sStartSound(actor, SFX_MANATK)
}

internal fun DoomEngineCore.aFatAttack1(actor: Actor) {
    val mo: Actor
    val an: Int

    aFaceTarget(actor)
    actor.angle += fatspread
    pSpawnMissile(actor, actor.target!!, MT_FATSHOT)

    mo = pSpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle += fatspread
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = fixedMul(mo.info!!.speed, FineCosineTable[an])
    mo.momy = fixedMul(mo.info!!.speed, finesine[an])
}

internal fun DoomEngineCore.aFatAttack2(actor: Actor) {
    val mo: Actor
    val an: Int

    aFaceTarget(actor)
    actor.angle -= fatspread
    pSpawnMissile(actor, actor.target!!, MT_FATSHOT)

    mo = pSpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle -= fatspread * 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = fixedMul(mo.info!!.speed, FineCosineTable[an])
    mo.momy = fixedMul(mo.info!!.speed, finesine[an])
}

internal fun DoomEngineCore.aFatAttack3(actor: Actor) {
    var mo: Actor
    var an: Int

    aFaceTarget(actor)

    mo = pSpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle -= fatspread / 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = fixedMul(mo.info!!.speed, FineCosineTable[an])
    mo.momy = fixedMul(mo.info!!.speed, finesine[an])

    mo = pSpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle += fatspread / 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = fixedMul(mo.info!!.speed, FineCosineTable[an])
    mo.momy = fixedMul(mo.info!!.speed, finesine[an])
}

internal const val SKULLSPEED = 20 * FRACUNIT

internal fun DoomEngineCore.aSkullAttack(actor: Actor) {
    val dest: Actor
    val an: BinaryAngle
    var dist: Int

    if (actor.target == null)
        return

    dest = actor.target!!
    actor.flags = actor.flags or MF_SKULLFLY

    sStartSound(actor, actor.info!!.attacksound)
    aFaceTarget(actor)
    an = actor.angle shr ANGLETOFINESHIFT
    actor.momx = fixedMul(SKULLSPEED, FineCosineTable[an.toInt()])
    actor.momy = fixedMul(SKULLSPEED, finesine[an.toInt()])
    dist = pAproxDistance(dest.x - actor.x, dest.y - actor.y)
    dist = dist / SKULLSPEED

    if (dist < 1)
        dist = 1
    actor.momz = (dest.z + (dest.height shr 1) - actor.z) / dist
}

internal fun DoomEngineCore.aPainShootSkull(actor: Actor, angle: BinaryAngle) {
    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint

    val newmobj: Actor
    val an: BinaryAngle
    val prestep: Int
    var count: Int
    var currentthinker: Thinker

    count = 0

    currentthinker = thinkercap.next!!
    while (currentthinker !== thinkercap) {
        if (currentthinker is Actor && !currentthinker.removed
            && currentthinker.type == MT_SKULL)
            count++
        currentthinker = currentthinker.next!!
    }

    if (count > 20)
        return

    an = angle shr ANGLETOFINESHIFT

    prestep =
        4 * FRACUNIT + 3 * (actor.info!!.radius + mobjinfo[MT_SKULL].radius) / 2

    x = actor.x + fixedMul(prestep, FineCosineTable[an.toInt()])
    y = actor.y + fixedMul(prestep, finesine[an.toInt()])
    z = actor.z + 8 * FRACUNIT

    newmobj = pSpawnMobj(x, y, z, MT_SKULL)

    if (!pTryMove(newmobj, newmobj.x, newmobj.y)) {
        pDamageMobj(newmobj, actor, actor, 10000)
        return
    }

    newmobj.target = actor.target
    aSkullAttack(newmobj)
}

internal fun DoomEngineCore.aPainAttack(actor: Actor) {
    if (actor.target == null)
        return

    aFaceTarget(actor)
    aPainShootSkull(actor, actor.angle)
}

internal fun DoomEngineCore.aPainDie(actor: Actor) {
    aFall(actor)
    aPainShootSkull(actor, actor.angle + ANG90)
    aPainShootSkull(actor, actor.angle + ANG180)
    aPainShootSkull(actor, actor.angle + ANG270)
}

internal fun DoomEngineCore.aScream(actor: Actor) {
    val sound: Int

    when (actor.info!!.deathsound) {
        0 ->
            return

        SFX_PODTH1, SFX_PODTH2, SFX_PODTH3 ->
            sound = SFX_PODTH1 + pRandom() % 3

        SFX_BGDTH1, SFX_BGDTH2 ->
            sound = SFX_BGDTH1 + pRandom() % 2

        else ->
            sound = actor.info!!.deathsound
    }

    if (actor.type == MT_SPIDER
        || actor.type == MT_CYBORG) {
        sStartSound(null, sound)
    } else
        sStartSound(actor, sound)
}

internal fun DoomEngineCore.aXScream(actor: Actor) {
    sStartSound(actor, SFX_SLOP)
}

internal fun DoomEngineCore.aPain(actor: Actor) {
    if (actor.info!!.painsound != 0)
        sStartSound(actor, actor.info!!.painsound)
}

internal fun DoomEngineCore.aFall(actor: Actor) {
    actor.flags = actor.flags and MF_SOLID.inv()

}

internal fun DoomEngineCore.aExplode(thingy: Actor) {
    pRadiusAttack(thingy, thingy.target, 128)
}

internal fun DoomEngineCore.aBossDeath(mo: Actor) {
    var th: Thinker
    val junk = MapLine()
    var i: Int

    if (gamemode == COMMERCIAL) {
        if (gamemap != 7)
            return

        if ((mo.type != MT_FATSO)
            && (mo.type != MT_BABY))
            return
    } else {
        when (gameepisode) {
            1 -> {
                if (gamemap != 8)
                    return

                if (mo.type != MT_BRUISER)
                    return
            }

            2 -> {
                if (gamemap != 8)
                    return

                if (mo.type != MT_CYBORG)
                    return
            }

            3 -> {
                if (gamemap != 8)
                    return

                if (mo.type != MT_SPIDER)
                    return
            }

            4 -> {
                when (gamemap) {
                    6 -> {
                        if (mo.type != MT_CYBORG)
                            return
                    }

                    8 -> {
                        if (mo.type != MT_SPIDER)
                            return
                    }

                    else ->
                        return
                }
            }

            else -> {
                if (gamemap != 8)
                    return
            }
        }
    }

    i = 0
    while (i < MAXPLAYERS) {
        if (playeringame[i] && players[i].health > 0)
            break
        i++
    }

    if (i == MAXPLAYERS)
        return

    th = thinkercap.next!!
    while (th !== thinkercap) {
        if (th is Actor && !th.removed) {
            val mo2: Actor = th
            if (mo2 !== mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                return
            }
        }
        th = th.next!!
    }

    if (gamemode == COMMERCIAL) {
        if (gamemap == 7) {
            if (mo.type == MT_FATSO) {
                junk.tag = 666
                evDoFloor(junk, LOWER_FLOOR_TO_LOWEST)
                return
            }

            if (mo.type == MT_BABY) {
                junk.tag = 667
                evDoFloor(junk, RAISE_TO_TEXTURE)
                return
            }
        }
    } else {
        when (gameepisode) {
            1 -> {
                junk.tag = 666
                evDoFloor(junk, LOWER_FLOOR_TO_LOWEST)
                return
            }

            4 -> {
                when (gamemap) {
                    6 -> {
                        junk.tag = 666
                        evDoDoor(junk, BLAZE_OPEN)
                        return
                    }

                    8 -> {
                        junk.tag = 666
                        evDoFloor(junk, LOWER_FLOOR_TO_LOWEST)
                        return
                    }
                }
            }
        }
    }

    gExitLevel()
}

internal fun DoomEngineCore.aHoof(mo: Actor) {
    sStartSound(mo, SFX_HOOF)
    aChase(mo)
}

internal fun DoomEngineCore.aMetal(mo: Actor) {
    sStartSound(mo, SFX_METAL)
    aChase(mo)
}

internal fun DoomEngineCore.aBabyMetal(mo: Actor) {
    sStartSound(mo, SFX_BSPWLK)
    aChase(mo)
}

internal fun DoomEngineCore.aOpenShotgun2(player: Player) {
    sStartSound(player.mo, SFX_DBOPN)
}

internal fun DoomEngineCore.aLoadShotgun2(player: Player) {
    sStartSound(player.mo, SFX_DBLOAD)
}

internal fun DoomEngineCore.aCloseShotgun2(player: Player) {
    sStartSound(player.mo, SFX_DBCLS)
    aReFire(player)
}

internal val DoomEngineCore.braintargets
    get() = stateMonsterBehavior.braintargets
internal var DoomEngineCore.numbraintargets
    get() = stateMonsterBehavior.numbraintargets
    set(value) { stateMonsterBehavior.numbraintargets = value }
internal var DoomEngineCore.braintargeton
    get() = stateMonsterBehavior.braintargeton
    set(value) { stateMonsterBehavior.braintargeton = value }

internal fun DoomEngineCore.aBrainAwake() {
    var thinker: Thinker

    numbraintargets = 0
    braintargeton = 0

    thinker = thinkercap.next!!
    while (thinker !== thinkercap) {
        if (thinker is Actor && !thinker.removed) {
            val m: Actor = thinker

            if (m.type == MT_BOSSTARGET) {
                braintargets[numbraintargets] = m
                numbraintargets++
            }
        }
        thinker = thinker.next!!
    }

    sStartSound(null, SFX_BOSSIT)
}

internal fun DoomEngineCore.aBrainPain() {
    sStartSound(null, SFX_BOSPN)
}

internal fun DoomEngineCore.aBrainScream(mo: Actor) {
    var x: Int
    var y: Int
    var z: Int
    var th: Actor

    x = mo.x - 196 * FRACUNIT
    while (x < mo.x + 320 * FRACUNIT) {
        y = mo.y - 320 * FRACUNIT
        z = 128 + pRandom() * 2 * FRACUNIT
        th = pSpawnMobj(x, y, z, MT_ROCKET)
        th.momz = pRandom() * 512

        pSetMobjState(th, S_BRAINEXPLODE1)

        th.tics -= pRandom() and 7
        if (th.tics < 1)
            th.tics = 1

        x += FRACUNIT * 8
    }

    sStartSound(null, SFX_BOSDTH)
}

internal fun DoomEngineCore.aBrainExplode(mo: Actor) {
    val x: Int
    val y: Int
    val z: Int
    val th: Actor

    x = mo.x + (pRandom() - pRandom()) * 2048
    y = mo.y
    z = 128 + pRandom() * 2 * FRACUNIT
    th = pSpawnMobj(x, y, z, MT_ROCKET)
    th.momz = pRandom() * 512

    pSetMobjState(th, S_BRAINEXPLODE1)

    th.tics -= pRandom() and 7
    if (th.tics < 1)
        th.tics = 1
}

internal fun DoomEngineCore.aBrainDie() {
    gExitLevel()
}

private var DoomEngineCore.easy
    get() = stateMonsterBehavior.easy
    set(value) { stateMonsterBehavior.easy = value }

internal fun DoomEngineCore.aBrainSpit(mo: Actor) {
    val targ: Actor
    val newmobj: Actor

    easy = easy xor 1
    if (gameskill <= SK_EASY && (easy == 0))
        return

    targ = braintargets[braintargeton]!!
    braintargeton = (braintargeton + 1) % numbraintargets

    newmobj = pSpawnMissile(mo, targ, MT_SPAWNSHOT)
    newmobj.target = targ
    newmobj.reactiontime =
        ((targ.y - mo.y) / newmobj.momy) / newmobj.state!!.tics

    sStartSound(null, SFX_BOSPIT)
}

internal fun DoomEngineCore.aSpawnSound(mo: Actor) {
    sStartSound(mo, SFX_BOSCUB)
    aSpawnFly(mo)
}

internal fun DoomEngineCore.aSpawnFly(mo: Actor) {
    val newmobj: Actor
    val fog: Actor
    val targ: Actor
    val r: Int
    val type: Int

    mo.reactiontime--
    if (mo.reactiontime != 0)
        return

    targ = mo.target!!

    fog = pSpawnMobj(targ.x, targ.y, targ.z, MT_SPAWNFIRE)
    sStartSound(fog, SFX_TELEPT)

    r = pRandom()

    if (r < 50)
        type = MT_TROOP
    else if (r < 90)
        type = MT_SERGEANT
    else if (r < 120)
        type = MT_SHADOWS
    else if (r < 130)
        type = MT_PAIN
    else if (r < 160)
        type = MT_HEAD
    else if (r < 162)
        type = MT_VILE
    else if (r < 172)
        type = MT_UNDEAD
    else if (r < 192)
        type = MT_BABY
    else if (r < 222)
        type = MT_FATSO
    else if (r < 246)
        type = MT_KNIGHT
    else
        type = MT_BRUISER

    newmobj = pSpawnMobj(targ.x, targ.y, targ.z, type)
    if (pLookForPlayers(newmobj, true))
        pSetMobjState(newmobj, newmobj.info!!.seestate)

    pTeleportMove(newmobj, newmobj.x, newmobj.y)

    pRemoveMobj(mo)
}

internal fun DoomEngineCore.aPlayerScream(mo: Actor) {
    var sound = SFX_PLDETH

    if ((gamemode == COMMERCIAL)
        && (mo.health < -50)) {
        sound = SFX_PDIEHI
    }

    sStartSound(mo, sound)
}

internal fun DoomEngineCore.pRegisterEnemyActions() {
    registerAction(StateAction("A_KeenDie", mobjFun = { aKeenDie(it) }))
    registerAction(StateAction("A_Look", mobjFun = { aLook(it) }))
    registerAction(StateAction("A_Chase", mobjFun = { aChase(it) }))
    registerAction(StateAction("A_FaceTarget", mobjFun = { aFaceTarget(it) }))
    registerAction(StateAction("A_PosAttack", mobjFun = { aPosAttack(it) }))
    registerAction(StateAction("A_SPosAttack", mobjFun = { aSPosAttack(it) }))
    registerAction(StateAction("A_CPosAttack", mobjFun = { aCPosAttack(it) }))
    registerAction(StateAction("A_CPosRefire", mobjFun = { aCPosRefire(it) }))
    registerAction(StateAction("A_SpidRefire", mobjFun = { aSpidRefire(it) }))
    registerAction(StateAction("A_BspiAttack", mobjFun = { aBspiAttack(it) }))
    registerAction(StateAction("A_TroopAttack", mobjFun = { aTroopAttack(it) }))
    registerAction(StateAction("A_SargAttack", mobjFun = { aSargAttack(it) }))
    registerAction(StateAction("A_HeadAttack", mobjFun = { aHeadAttack(it) }))
    registerAction(StateAction("A_CyberAttack", mobjFun = { aCyberAttack(it) }))
    registerAction(StateAction("A_BruisAttack", mobjFun = { aBruisAttack(it) }))
    registerAction(StateAction("A_SkelMissile", mobjFun = { aSkelMissile(it) }))
    registerAction(StateAction("A_Tracer", mobjFun = { aTracer(it) }))
    registerAction(StateAction("A_SkelWhoosh", mobjFun = { aSkelWhoosh(it) }))
    registerAction(StateAction("A_SkelFist", mobjFun = { aSkelFist(it) }))
    registerAction(StateAction("A_VileChase", mobjFun = { aVileChase(it) }))
    registerAction(StateAction("A_VileStart", mobjFun = { aVileStart(it) }))
    registerAction(StateAction("A_StartFire", mobjFun = { aStartFire(it) }))
    registerAction(StateAction("A_FireCrackle", mobjFun = { aFireCrackle(it) }))
    registerAction(StateAction("A_Fire", mobjFun = { aFire(it) }))
    registerAction(StateAction("A_VileTarget", mobjFun = { aVileTarget(it) }))
    registerAction(StateAction("A_VileAttack", mobjFun = { aVileAttack(it) }))
    registerAction(StateAction("A_FatRaise", mobjFun = { aFatRaise(it) }))
    registerAction(StateAction("A_FatAttack1", mobjFun = { aFatAttack1(it) }))
    registerAction(StateAction("A_FatAttack2", mobjFun = { aFatAttack2(it) }))
    registerAction(StateAction("A_FatAttack3", mobjFun = { aFatAttack3(it) }))
    registerAction(StateAction("A_SkullAttack", mobjFun = { aSkullAttack(it) }))
    registerAction(StateAction("A_PainAttack", mobjFun = { aPainAttack(it) }))
    registerAction(StateAction("A_PainDie", mobjFun = { aPainDie(it) }))
    registerAction(StateAction("A_Scream", mobjFun = { aScream(it) }))
    registerAction(StateAction("A_XScream", mobjFun = { aXScream(it) }))
    registerAction(StateAction("A_Pain", mobjFun = { aPain(it) }))
    registerAction(StateAction("A_Fall", mobjFun = { aFall(it) }))
    registerAction(StateAction("A_Explode", mobjFun = { aExplode(it) }))
    registerAction(StateAction("A_BossDeath", mobjFun = { aBossDeath(it) }))
    registerAction(StateAction("A_Hoof", mobjFun = { aHoof(it) }))
    registerAction(StateAction("A_Metal", mobjFun = { aMetal(it) }))
    registerAction(StateAction("A_BabyMetal", mobjFun = { aBabyMetal(it) }))
    registerAction(StateAction("A_OpenShotgun2", pspFun = { player, _ -> aOpenShotgun2(player) }))
    registerAction(StateAction("A_LoadShotgun2", pspFun = { player, _ -> aLoadShotgun2(player) }))
    registerAction(StateAction("A_CloseShotgun2", pspFun = { player, _ -> aCloseShotgun2(player) }))
    registerAction(StateAction("A_BrainAwake", mobjFun = { _ -> aBrainAwake() }))
    registerAction(StateAction("A_BrainPain", mobjFun = { _ -> aBrainPain() }))
    registerAction(StateAction("A_BrainScream", mobjFun = { aBrainScream(it) }))
    registerAction(StateAction("A_BrainExplode", mobjFun = { aBrainExplode(it) }))
    registerAction(StateAction("A_BrainDie", mobjFun = { _ -> aBrainDie() }))
    registerAction(StateAction("A_BrainSpit", mobjFun = { aBrainSpit(it) }))
    registerAction(StateAction("A_SpawnSound", mobjFun = { aSpawnSound(it) }))
    registerAction(StateAction("A_SpawnFly", mobjFun = { aSpawnFly(it) }))
    registerAction(StateAction("A_PlayerScream", mobjFun = { aPlayerScream(it) }))
}
