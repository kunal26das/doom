
package doom.engine.gameplay.actors

import doom.engine.audio.sStartSound
import doom.engine.audio.sStopSound
import doom.engine.audio.SFX_ITMBK
import doom.engine.audio.SFX_OOF
import doom.engine.audio.SFX_TELEPT
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.core.nomonsters
import doom.engine.gameplay.gPlayerReborn
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.MTF_AMBUSH
import doom.engine.gameplay.NUMCARDS
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.gameskill
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.CF_NOMOMENTUM
import doom.engine.gameplay.player.PST_LIVE
import doom.engine.gameplay.player.PST_REBORN
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.respawnmonsters
import doom.engine.gameplay.SK_BABY
import doom.engine.gameplay.SK_NIGHTMARE
import doom.engine.gameplay.totalitems
import doom.engine.gameplay.totalkills
import doom.engine.gameplay.weapons.pSetupPsprites
import doom.engine.geometry.ANG45
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.hud.huStart
import doom.engine.rendering.skyflatnum
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRandom
import doom.engine.simulation.pRemoveThinker
import doom.engine.simulation.leveltime
import doom.engine.statusbar.stStart
import doom.engine.world.FLOATSPEED
import doom.engine.world.GRAVITY
import doom.engine.world.ITEMQUESIZE
import doom.engine.world.MAXMOVE
import doom.engine.world.MELEERANGE
import doom.engine.world.MapThingSpawn
import doom.engine.world.ONCEILINGZ
import doom.engine.world.ONFLOORZ
import doom.engine.world.Subsector
import doom.engine.world.VIEWHEIGHT
import doom.engine.world.collision.pAimLineAttack
import doom.engine.world.collision.pAproxDistance
import doom.engine.world.collision.pCheckPosition
import doom.engine.world.collision.pSetThingPosition
import doom.engine.world.collision.pSlideMove
import doom.engine.world.collision.pTryMove
import doom.engine.world.collision.pUnsetThingPosition
import doom.engine.world.collision.attackrange
import doom.engine.world.collision.ceilingline
import doom.engine.world.collision.findSubsector
import doom.engine.world.collision.linetarget
import doom.engine.world.deathmatchP
import doom.engine.world.deathmatchstarts
import doom.engine.world.playerstarts

internal var DoomEngineCore.test
    get() = stateActorSpawn.test
    set(value) { stateActorSpawn.test = value }

internal fun DoomEngineCore.pSetMobjState(mobj: Actor, stateIndex: Int): Boolean {
    var state = stateIndex
    var st: StateDefinition

    do {
        if (state == S_NULL) {
            mobj.state = null
            pRemoveMobj(mobj)
            return false
        }

        st = states[state]
        mobj.state = st
        mobj.tics = st.tics
        mobj.sprite = st.sprite
        mobj.frame = st.frame

        st.action?.mobjFun?.invoke(mobj)

        state = st.nextstate
    } while (mobj.tics == 0)

    return true
}

internal fun DoomEngineCore.pExplodeMissile(mo: Actor) {
    mo.momx = 0
    mo.momy = 0
    mo.momz = 0

    pSetMobjState(mo, mobjinfo[mo.type].deathstate)

    mo.tics -= pRandom() and 3

    if (mo.tics < 1)
        mo.tics = 1

    mo.flags = mo.flags and MF_MISSILE.inv()

    if (mo.info!!.deathsound != 0)
        sStartSound(mo, mo.info!!.deathsound)
}

internal const val STOPSPEED = 0x1000
internal const val FRICTION = 0xe800

internal fun DoomEngineCore.pXYMovement(mo: Actor) {
    var ptryx: FixedPoint
    var ptryy: FixedPoint
    val player: Player?
    var xmove: FixedPoint
    var ymove: FixedPoint

    if (mo.momx == 0 && mo.momy == 0) {
        if ((mo.flags and MF_SKULLFLY) != 0) {
            mo.flags = mo.flags and MF_SKULLFLY.inv()
            mo.momx = 0
            mo.momy = 0
            mo.momz = 0

            pSetMobjState(mo, mo.info!!.spawnstate)
        }
        return
    }

    player = mo.player

    if (mo.momx > MAXMOVE)
        mo.momx = MAXMOVE
    else if (mo.momx < -MAXMOVE)
        mo.momx = -MAXMOVE

    if (mo.momy > MAXMOVE)
        mo.momy = MAXMOVE
    else if (mo.momy < -MAXMOVE)
        mo.momy = -MAXMOVE

    xmove = mo.momx
    ymove = mo.momy

    do {
        if (xmove > MAXMOVE / 2 || ymove > MAXMOVE / 2) {
            ptryx = mo.x + xmove / 2
            ptryy = mo.y + ymove / 2
            xmove = xmove shr 1
            ymove = ymove shr 1
        } else {
            ptryx = mo.x + xmove
            ptryy = mo.y + ymove
            xmove = 0
            ymove = 0
        }

        if (!pTryMove(mo, ptryx, ptryy)) {
            if (mo.player != null) {
                pSlideMove(mo)
            } else if ((mo.flags and MF_MISSILE) != 0) {
                if (ceilingline != null &&
                    ceilingline!!.backsector != null &&
                    ceilingline!!.backsector!!.ceilingpic == skyflatnum) {
                    pRemoveMobj(mo)
                    return
                }
                pExplodeMissile(mo)
            } else {
                mo.momx = 0
                mo.momy = 0
            }
        }
    } while (xmove != 0 || ymove != 0)

    if (player != null && (player.cheats and CF_NOMOMENTUM) != 0) {
        mo.momx = 0
        mo.momy = 0
        return
    }

    if ((mo.flags and (MF_MISSILE or MF_SKULLFLY)) != 0)
        return

    if (mo.z > mo.floorz)
        return

    if ((mo.flags and MF_CORPSE) != 0) {
        if (mo.momx > FRACUNIT / 4
            || mo.momx < -FRACUNIT / 4
            || mo.momy > FRACUNIT / 4
            || mo.momy < -FRACUNIT / 4) {
            if (mo.floorz != mo.subsector!!.sector!!.floorheight)
                return
        }
    }

    if (mo.momx > -STOPSPEED
        && mo.momx < STOPSPEED
        && mo.momy > -STOPSPEED
        && mo.momy < STOPSPEED
        && (player == null
            || (player.cmd.forwardmove == 0
                && player.cmd.sidemove == 0))) {
        if (player != null && (player.mo!!.state!!.index - S_PLAY_RUN1).toUInt() < 4u)
            pSetMobjState(player.mo!!, S_PLAY)

        mo.momx = 0
        mo.momy = 0
    } else {
        mo.momx = fixedMul(mo.momx, FRICTION)
        mo.momy = fixedMul(mo.momy, FRICTION)
    }
}

internal fun DoomEngineCore.pZMovement(mo: Actor) {
    val dist: FixedPoint
    val delta: FixedPoint

    if (mo.player != null && mo.z < mo.floorz) {
        mo.player!!.viewheight -= mo.floorz - mo.z

        mo.player!!.deltaviewheight = (VIEWHEIGHT - mo.player!!.viewheight) shr 3
    }

    mo.z += mo.momz

    if ((mo.flags and MF_FLOAT) != 0
        && mo.target != null) {
        if ((mo.flags and MF_SKULLFLY) == 0
            && (mo.flags and MF_INFLOAT) == 0) {
            dist = pAproxDistance(mo.x - mo.target!!.x,
                mo.y - mo.target!!.y)

            delta = (mo.target!!.z + (mo.height shr 1)) - mo.z

            if (delta < 0 && dist < -(delta * 3))
                mo.z -= FLOATSPEED
            else if (delta > 0 && dist < (delta * 3))
                mo.z += FLOATSPEED
        }
    }

    if (mo.z <= mo.floorz) {

        if ((mo.flags and MF_SKULLFLY) != 0) {
            mo.momz = -mo.momz
        }

        if (mo.momz < 0) {
            if (mo.player != null
                && mo.momz < -GRAVITY * 8) {
                mo.player!!.deltaviewheight = mo.momz shr 3
                sStartSound(mo, SFX_OOF)
            }
            mo.momz = 0
        }
        mo.z = mo.floorz

        if ((mo.flags and MF_MISSILE) != 0
            && (mo.flags and MF_NOCLIP) == 0) {
            pExplodeMissile(mo)
            return
        }
    } else if ((mo.flags and MF_NOGRAVITY) == 0) {
        if (mo.momz == 0)
            mo.momz = -GRAVITY * 2
        else
            mo.momz -= GRAVITY
    }

    if (mo.z + mo.height > mo.ceilingz) {
        if (mo.momz > 0)
            mo.momz = 0
        mo.z = mo.ceilingz - mo.height

        if ((mo.flags and MF_SKULLFLY) != 0) {
            mo.momz = -mo.momz
        }

        if ((mo.flags and MF_MISSILE) != 0
            && (mo.flags and MF_NOCLIP) == 0) {
            pExplodeMissile(mo)
            return
        }
    }
}

internal fun DoomEngineCore.pNightmareRespawn(mobj: Actor) {
    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint
    val ss: Subsector
    var mo: Actor
    val mthing: MapThingSpawn

    mthing = mobj.spawnpoint ?: MapThingSpawn()

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    if (!pCheckPosition(mobj, x, y))
        return

    mo = pSpawnMobj(mobj.x,
        mobj.y,
        mobj.subsector!!.sector!!.floorheight, MT_TFOG)
    sStartSound(mo, SFX_TELEPT)

    ss = findSubsector(x, y)

    mo = pSpawnMobj(x, y, ss.sector!!.floorheight, MT_TFOG)

    sStartSound(mo, SFX_TELEPT)


    if ((mobj.info!!.flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    mo = pSpawnMobj(x, y, z, mobj.type)
    mo.spawnpoint = MapThingSpawn().also { it.copyFrom(mthing) }
    mo.angle = ANG45 * (mthing.angle / 45).toUInt()

    if ((mthing.options and MTF_AMBUSH) != 0)
        mo.flags = mo.flags or MF_AMBUSH

    mo.reactiontime = 18

    pRemoveMobj(mobj)
}

internal fun DoomEngineCore.pMobjThinker(mobj: Actor) {
    if (mobj.momx != 0
        || mobj.momy != 0
        || (mobj.flags and MF_SKULLFLY) != 0) {
        pXYMovement(mobj)

        if (mobj.removed)
            return
    }
    if ((mobj.z != mobj.floorz)
        || mobj.momz != 0) {
        pZMovement(mobj)

        if (mobj.removed)
            return
    }

    if (mobj.tics != -1) {
        mobj.tics--

        if (mobj.tics == 0)
            if (!pSetMobjState(mobj, mobj.state!!.nextstate))
                return
    } else {
        if ((mobj.flags and MF_COUNTKILL) == 0)
            return

        if (!respawnmonsters)
            return

        mobj.movecount++

        if (mobj.movecount < 12 * 35)
            return

        if ((leveltime and 31) != 0)
            return

        if (pRandom() > 4)
            return

        pNightmareRespawn(mobj)
    }
}

internal fun DoomEngineCore.pSpawnMobj(x: FixedPoint, y: FixedPoint, z: FixedPoint, type: Int): Actor {
    val mobj: Actor
    val st: StateDefinition
    val info: ActorDefinition

    mobj = Actor()
    info = mobjinfo[type]

    mobj.type = type
    mobj.info = info
    mobj.x = x
    mobj.y = y
    mobj.radius = info.radius
    mobj.height = info.height
    mobj.flags = info.flags
    mobj.health = info.spawnhealth

    if (gameskill != SK_NIGHTMARE)
        mobj.reactiontime = info.reactiontime

    mobj.lastlook = pRandom() % MAXPLAYERS
    st = states[info.spawnstate]

    mobj.state = st
    mobj.tics = st.tics
    mobj.sprite = st.sprite
    mobj.frame = st.frame

    pSetThingPosition(mobj)

    mobj.floorz = mobj.subsector!!.sector!!.floorheight
    mobj.ceilingz = mobj.subsector!!.sector!!.ceilingheight

    if (z == ONFLOORZ)
        mobj.z = mobj.floorz
    else if (z == ONCEILINGZ)
        mobj.z = mobj.ceilingz - mobj.info!!.height
    else
        mobj.z = z

    mobj.function = { th -> pMobjThinker(th as Actor) }

    pAddThinker(mobj)

    return mobj
}

internal val DoomEngineCore.itemrespawnque
    get() = stateActorSpawn.itemrespawnque
internal val DoomEngineCore.itemrespawntime
    get() = stateActorSpawn.itemrespawntime
internal var DoomEngineCore.iquehead
    get() = stateActorSpawn.iquehead
    set(value) { stateActorSpawn.iquehead = value }
internal var DoomEngineCore.iquetail
    get() = stateActorSpawn.iquetail
    set(value) { stateActorSpawn.iquetail = value }

internal fun DoomEngineCore.pRemoveMobj(mobj: Actor) {
    if ((mobj.flags and MF_SPECIAL) != 0
        && (mobj.flags and MF_DROPPED) == 0
        && (mobj.type != MT_INV)
        && (mobj.type != MT_INS)) {
        itemrespawnque[iquehead].copyFrom(mobj.spawnpoint ?: MapThingSpawn())
        itemrespawntime[iquehead] = leveltime
        iquehead = (iquehead + 1) and (ITEMQUESIZE - 1)

        if (iquehead == iquetail)
            iquetail = (iquetail + 1) and (ITEMQUESIZE - 1)
    }

    pUnsetThingPosition(mobj)

    sStopSound(mobj)

    pRemoveThinker(mobj)
}

internal fun DoomEngineCore.pRespawnSpecials() {
    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint

    val ss: Subsector
    var mo: Actor
    val mthing: MapThingSpawn

    var i: Int

    if (deathmatch != 2)
        return

    if (iquehead == iquetail)
        return

    if (leveltime - itemrespawntime[iquetail] < 30 * 35)
        return

    mthing = itemrespawnque[iquetail]

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    ss = findSubsector(x, y)
    mo = pSpawnMobj(x, y, ss.sector!!.floorheight, MT_IFOG)
    sStartSound(mo, SFX_ITMBK)

    i = 0
    while (i < NUMMOBJTYPES) {
        if (mthing.type == mobjinfo[i].doomednum)
            break
        i++
    }

    if ((mobjinfo[i].flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    mo = pSpawnMobj(x, y, z, i)
    mo.spawnpoint = MapThingSpawn().also { it.copyFrom(mthing) }
    mo.angle = ANG45 * (mthing.angle / 45).toUInt()

    iquetail = (iquetail + 1) and (ITEMQUESIZE - 1)
}

internal fun DoomEngineCore.pSpawnPlayer(mthing: MapThingSpawn) {
    val p: Player
    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint

    val mobj: Actor

    var i: Int

    if (!playeringame[mthing.type - 1])
        return

    p = players[mthing.type - 1]

    if (p.playerstate == PST_REBORN)
        gPlayerReborn(mthing.type - 1)

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS
    z = ONFLOORZ
    mobj = pSpawnMobj(x, y, z, MT_PLAYER)

    if (mthing.type > 1)
        mobj.flags = mobj.flags or ((mthing.type - 1) shl MF_TRANSSHIFT)

    mobj.angle = ANG45 * (mthing.angle / 45).toUInt()
    mobj.player = p
    mobj.health = p.health

    p.mo = mobj
    p.playerstate = PST_LIVE
    p.refire = 0
    p.message = null
    p.damagecount = 0
    p.bonuscount = 0
    p.extralight = 0
    p.fixedcolormap = 0
    p.viewheight = VIEWHEIGHT

    pSetupPsprites(p)

    if (deathmatch != 0) {
        i = 0
        while (i < NUMCARDS) {
            p.cards[i] = true
            i++
        }
    }

    if (mthing.type - 1 == consoleplayer) {
        stStart()
        huStart()
    }
}

internal fun DoomEngineCore.pSpawnMapThing(mthing: MapThingSpawn) {
    var i: Int
    val bit: Int
    val mobj: Actor
    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint

    if (mthing.type == 11) {
        if (deathmatchP < 10) {
            deathmatchstarts[deathmatchP].copyFrom(mthing)
            deathmatchP++
        }
        return
    }

    if (mthing.type <= 4) {
        playerstarts[mthing.type - 1].copyFrom(mthing)
        if (deathmatch == 0)
            pSpawnPlayer(mthing)

        return
    }

    if (!netgame && (mthing.options and 16) != 0)
        return

    if (gameskill == SK_BABY)
        bit = 1
    else if (gameskill == SK_NIGHTMARE)
        bit = 4
    else
        bit = 1 shl (gameskill - 1)

    if ((mthing.options and bit) == 0)
        return

    i = 0
    while (i < NUMMOBJTYPES) {
        if (mthing.type == mobjinfo[i].doomednum)
            break
        i++
    }

    if (i == NUMMOBJTYPES)
        iError("P_SpawnMapThing: Unknown type ${mthing.type} at (${mthing.x}, ${mthing.y})")

    if (deathmatch != 0 && (mobjinfo[i].flags and MF_NOTDMATCH) != 0)
        return

    if (nomonsters
        && (i == MT_SKULL
            || (mobjinfo[i].flags and MF_COUNTKILL) != 0)) {
        return
    }

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    if ((mobjinfo[i].flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    mobj = pSpawnMobj(x, y, z, i)
    mobj.spawnpoint = MapThingSpawn().also { it.copyFrom(mthing) }

    if (mobj.tics > 0)
        mobj.tics = 1 + (pRandom() % mobj.tics)
    if ((mobj.flags and MF_COUNTKILL) != 0)
        totalkills++
    if ((mobj.flags and MF_COUNTITEM) != 0)
        totalitems++

    mobj.angle = ANG45 * (mthing.angle / 45).toUInt()
    if ((mthing.options and MTF_AMBUSH) != 0)
        mobj.flags = mobj.flags or MF_AMBUSH
}


internal fun DoomEngineCore.pSpawnPuff(x: FixedPoint, y: FixedPoint, spawnZ: FixedPoint) {
    var z = spawnZ
    val th: Actor

    z += ((pRandom() - pRandom()) shl 10)

    th = pSpawnMobj(x, y, z, MT_PUFF)
    th.momz = FRACUNIT
    th.tics -= pRandom() and 3

    if (th.tics < 1)
        th.tics = 1

    if (attackrange == MELEERANGE)
        pSetMobjState(th, S_PUFF3)
}

internal fun DoomEngineCore.pSpawnBlood(x: FixedPoint, y: FixedPoint, spawnZ: FixedPoint, damage: Int) {
    var z = spawnZ
    val th: Actor

    z += ((pRandom() - pRandom()) shl 10)
    th = pSpawnMobj(x, y, z, MT_BLOOD)
    th.momz = FRACUNIT * 2
    th.tics -= pRandom() and 3

    if (th.tics < 1)
        th.tics = 1

    if (damage <= 12 && damage >= 9)
        pSetMobjState(th, S_BLOOD2)
    else if (damage < 9)
        pSetMobjState(th, S_BLOOD3)
}

internal fun DoomEngineCore.pCheckMissileSpawn(th: Actor) {
    th.tics -= pRandom() and 3
    if (th.tics < 1)
        th.tics = 1

    th.x += (th.momx shr 1)
    th.y += (th.momy shr 1)
    th.z += (th.momz shr 1)

    if (!pTryMove(th, th.x, th.y))
        pExplodeMissile(th)
}

internal fun DoomEngineCore.pSpawnMissile(source: Actor, dest: Actor, type: Int): Actor {
    val th: Actor
    var an: BinaryAngle
    var dist: Int

    th = pSpawnMobj(source.x,
        source.y,
        source.z + 4 * 8 * FRACUNIT, type)

    if (th.info!!.seesound != 0)
        sStartSound(th, th.info!!.seesound)

    th.target = source
    an = FixedGeometry.angleBetween(source.x, source.y, dest.x, dest.y)

    if ((dest.flags and MF_SHADOW) != 0)
        an += ((pRandom() - pRandom()) shl 20).toUInt()

    th.angle = an
    an = an shr ANGLETOFINESHIFT
    th.momx = fixedMul(th.info!!.speed, FineCosineTable[an.toInt()])
    th.momy = fixedMul(th.info!!.speed, finesine[an.toInt()])

    dist = pAproxDistance(dest.x - source.x, dest.y - source.y)
    dist = dist / th.info!!.speed

    if (dist < 1)
        dist = 1

    th.momz = (dest.z - source.z) / dist
    pCheckMissileSpawn(th)

    return th
}

internal fun DoomEngineCore.pSpawnPlayerMissile(source: Actor, type: Int) {
    val th: Actor
    var an: BinaryAngle

    val x: FixedPoint
    val y: FixedPoint
    val z: FixedPoint
    var slope: FixedPoint

    an = source.angle
    slope = pAimLineAttack(source, an, 16 * 64 * FRACUNIT)

    if (linetarget == null) {
        an += (1 shl 26).toUInt()
        slope = pAimLineAttack(source, an, 16 * 64 * FRACUNIT)

        if (linetarget == null) {
            an -= (2 shl 26).toUInt()
            slope = pAimLineAttack(source, an, 16 * 64 * FRACUNIT)
        }

        if (linetarget == null) {
            an = source.angle
            slope = 0
        }
    }

    x = source.x
    y = source.y
    z = source.z + 4 * 8 * FRACUNIT

    th = pSpawnMobj(x, y, z, type)

    if (th.info!!.seesound != 0)
        sStartSound(th, th.info!!.seesound)

    th.target = source
    th.angle = an
    th.momx = fixedMul(th.info!!.speed,
        FineCosineTable[(an shr ANGLETOFINESHIFT).toInt()])
    th.momy = fixedMul(th.info!!.speed,
        finesine[(an shr ANGLETOFINESHIFT).toInt()])
    th.momz = fixedMul(th.info!!.speed, slope)

    pCheckMissileSpawn(th)
}
