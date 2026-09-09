
package doom.engine.world.collision

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_NOWAY
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MF_DROPOFF
import doom.engine.gameplay.actors.MF_DROPPED
import doom.engine.gameplay.actors.MF_FLOAT
import doom.engine.gameplay.actors.MF_MISSILE
import doom.engine.gameplay.actors.MF_NOBLOOD
import doom.engine.gameplay.actors.MF_NOCLIP
import doom.engine.gameplay.actors.MF_PICKUP
import doom.engine.gameplay.actors.MF_SHOOTABLE
import doom.engine.gameplay.actors.MF_SKULLFLY
import doom.engine.gameplay.actors.MF_SOLID
import doom.engine.gameplay.actors.MF_SPECIAL
import doom.engine.gameplay.actors.MF_TELEPORT
import doom.engine.gameplay.actors.MT_BLOOD
import doom.engine.gameplay.actors.MT_BRUISER
import doom.engine.gameplay.actors.MT_CYBORG
import doom.engine.gameplay.actors.MT_KNIGHT
import doom.engine.gameplay.actors.MT_PLAYER
import doom.engine.gameplay.actors.MT_SPIDER
import doom.engine.gameplay.actors.pRemoveMobj
import doom.engine.gameplay.actors.pSetMobjState
import doom.engine.gameplay.actors.pSpawnBlood
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.gameplay.actors.pSpawnPuff
import doom.engine.gameplay.actors.S_GIBS
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.interactions.pDamageMobj
import doom.engine.gameplay.interactions.pTouchSpecialThing
import doom.engine.gameplay.player.Player
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BOXBOTTOM
import doom.engine.geometry.BOXLEFT
import doom.engine.geometry.BOXRIGHT
import doom.engine.geometry.BOXTOP
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.rendering.ST_HORIZONTAL
import doom.engine.rendering.ST_VERTICAL
import doom.engine.rendering.skyflatnum
import doom.engine.rendering.validcount
import doom.engine.simulation.pRandom
import doom.engine.simulation.leveltime
import doom.engine.world.MAPBLOCKSHIFT
import doom.engine.world.MAXRADIUS
import doom.engine.world.ML_BLOCKING
import doom.engine.world.ML_BLOCKMONSTERS
import doom.engine.world.ML_TWOSIDED
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.USERANGE
import doom.engine.world.bmaporgx
import doom.engine.world.bmaporgy
import doom.engine.world.specials.pCrossSpecialLine
import doom.engine.world.specials.pShootSpecialLine
import doom.engine.world.specials.pUseSpecialLine
import doom.engine.world.visibility.pCheckSight
import doom.engine.world.visibility.bottomslope
import doom.engine.world.visibility.topslope

import kotlin.math.abs

internal val DoomEngineCore.tmbbox
    get() = stateCollision.tmbbox
internal var DoomEngineCore.tmthing: Actor?
    get() = stateCollision.tmthing
    set(value) { stateCollision.tmthing = value }
internal var DoomEngineCore.tmflags
    get() = stateCollision.tmflags
    set(value) { stateCollision.tmflags = value }
internal var DoomEngineCore.tmx: FixedPoint
    get() = stateCollision.tmx
    set(value) { stateCollision.tmx = value }
internal var DoomEngineCore.tmy: FixedPoint
    get() = stateCollision.tmy
    set(value) { stateCollision.tmy = value }

internal var DoomEngineCore.floatok
    get() = stateCollision.floatok
    set(value) { stateCollision.floatok = value }

internal var DoomEngineCore.tmfloorz: FixedPoint
    get() = stateCollision.tmfloorz
    set(value) { stateCollision.tmfloorz = value }
internal var DoomEngineCore.tmceilingz: FixedPoint
    get() = stateCollision.tmceilingz
    set(value) { stateCollision.tmceilingz = value }
internal var DoomEngineCore.tmdropoffz: FixedPoint
    get() = stateCollision.tmdropoffz
    set(value) { stateCollision.tmdropoffz = value }

internal var DoomEngineCore.ceilingline: MapLine?
    get() = stateCollision.ceilingline
    set(value) { stateCollision.ceilingline = value }

internal const val MAXSPECIALCROSS = 8

internal val DoomEngineCore.spechit
    get() = stateCollision.spechit
internal var DoomEngineCore.numspechit
    get() = stateCollision.numspechit
    set(value) { stateCollision.numspechit = value }


internal fun DoomEngineCore.pitStompThing(thing: Actor): Boolean {
    if (thing.flags and MF_SHOOTABLE == 0)
        return true

    val blockdist = thing.radius + tmthing!!.radius

    if (abs(thing.x - tmx) >= blockdist
        || abs(thing.y - tmy) >= blockdist
    ) {
        return true
    }

    if (thing === tmthing)
        return true

    if (tmthing!!.player == null && gamemap != 30)
        return false

    pDamageMobj(thing, tmthing, tmthing, 10000)

    return true
}

internal fun DoomEngineCore.pTeleportMove(thing: Actor, x: FixedPoint, y: FixedPoint): Boolean {
    tmthing = thing
    tmflags = thing.flags

    tmx = x
    tmy = y

    tmbbox[BOXTOP] = y + tmthing!!.radius
    tmbbox[BOXBOTTOM] = y - tmthing!!.radius
    tmbbox[BOXRIGHT] = x + tmthing!!.radius
    tmbbox[BOXLEFT] = x - tmthing!!.radius

    val newsubsec = findSubsector(x, y)
    ceilingline = null

    tmdropoffz = newsubsec.sector!!.floorheight
    tmfloorz = tmdropoffz
    tmceilingz = newsubsec.sector!!.ceilingheight

    validcount++
    numspechit = 0

    val xl = (tmbbox[BOXLEFT] - bmaporgx - MAXRADIUS) shr MAPBLOCKSHIFT
    val xh = (tmbbox[BOXRIGHT] - bmaporgx + MAXRADIUS) shr MAPBLOCKSHIFT
    val yl = (tmbbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) shr MAPBLOCKSHIFT
    val yh = (tmbbox[BOXTOP] - bmaporgy + MAXRADIUS) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!pBlockThingsIterator(bx, by, { argument0 -> pitStompThing(argument0) }))
                return false

    pUnsetThingPosition(thing)

    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz
    thing.x = x
    thing.y = y

    pSetThingPosition(thing)

    return true
}


internal fun DoomEngineCore.pitCheckLine(ld: MapLine): Boolean {
    if (tmbbox[BOXRIGHT] <= ld.bbox[BOXLEFT]
        || tmbbox[BOXLEFT] >= ld.bbox[BOXRIGHT]
        || tmbbox[BOXTOP] <= ld.bbox[BOXBOTTOM]
        || tmbbox[BOXBOTTOM] >= ld.bbox[BOXTOP]
    )
        return true

    if (pBoxOnLineSide(tmbbox, ld) != -1)
        return true



    if (ld.backsector == null)
        return false

    if (tmthing!!.flags and MF_MISSILE == 0) {
        if (ld.flags and ML_BLOCKING != 0)
            return false

        if (tmthing!!.player == null && ld.flags and ML_BLOCKMONSTERS != 0)
            return false
    }

    pLineOpening(ld)

    if (opentop < tmceilingz) {
        tmceilingz = opentop
        ceilingline = ld
    }

    if (openbottom > tmfloorz)
        tmfloorz = openbottom

    if (lowfloor < tmdropoffz)
        tmdropoffz = lowfloor

    if (ld.special != 0) {
        spechit[numspechit] = ld
        numspechit++
    }

    return true
}

internal fun DoomEngineCore.pitCheckThing(thing: Actor): Boolean {
    if (thing.flags and (MF_SOLID or MF_SPECIAL or MF_SHOOTABLE) == 0)
        return true

    val blockdist = thing.radius + tmthing!!.radius

    if (abs(thing.x - tmx) >= blockdist
        || abs(thing.y - tmy) >= blockdist
    ) {
        return true
    }

    if (thing === tmthing)
        return true

    if (tmthing!!.flags and MF_SKULLFLY != 0) {
        val damage = ((pRandom() % 8) + 1) * tmthing!!.info!!.damage

        pDamageMobj(thing, tmthing, tmthing, damage)

        tmthing!!.flags = tmthing!!.flags and MF_SKULLFLY.inv()
        tmthing!!.momx = 0
        tmthing!!.momy = 0
        tmthing!!.momz = 0

        pSetMobjState(tmthing!!, tmthing!!.info!!.spawnstate)

        return false
    }

    if (tmthing!!.flags and MF_MISSILE != 0) {
        if (tmthing!!.z > thing.z + thing.height)
            return true
        if (tmthing!!.z + tmthing!!.height < thing.z)
            return true

        if (tmthing!!.target != null && (
                tmthing!!.target!!.type == thing.type ||
                (tmthing!!.target!!.type == MT_KNIGHT && thing.type == MT_BRUISER) ||
                (tmthing!!.target!!.type == MT_BRUISER && thing.type == MT_KNIGHT))
        ) {
            if (thing === tmthing!!.target)
                return true

            if (thing.type != MT_PLAYER) {
                return false
            }
        }

        if (thing.flags and MF_SHOOTABLE == 0) {
            return thing.flags and MF_SOLID == 0
        }

        val damage = ((pRandom() % 8) + 1) * tmthing!!.info!!.damage
        pDamageMobj(thing, tmthing, tmthing!!.target, damage)

        return false
    }

    if (thing.flags and MF_SPECIAL != 0) {
        val solid = thing.flags and MF_SOLID != 0
        if (tmflags and MF_PICKUP != 0) {
            pTouchSpecialThing(thing, tmthing!!)
        }
        return !solid
    }

    return thing.flags and MF_SOLID == 0
}


internal fun DoomEngineCore.pCheckPosition(thing: Actor, x: FixedPoint, y: FixedPoint): Boolean {
    tmthing = thing
    tmflags = thing.flags

    tmx = x
    tmy = y

    tmbbox[BOXTOP] = y + tmthing!!.radius
    tmbbox[BOXBOTTOM] = y - tmthing!!.radius
    tmbbox[BOXRIGHT] = x + tmthing!!.radius
    tmbbox[BOXLEFT] = x - tmthing!!.radius

    val newsubsec = findSubsector(x, y)
    ceilingline = null

    tmdropoffz = newsubsec.sector!!.floorheight
    tmfloorz = tmdropoffz
    tmceilingz = newsubsec.sector!!.ceilingheight

    validcount++
    numspechit = 0

    if (tmflags and MF_NOCLIP != 0)
        return true

    var xl = (tmbbox[BOXLEFT] - bmaporgx - MAXRADIUS) shr MAPBLOCKSHIFT
    var xh = (tmbbox[BOXRIGHT] - bmaporgx + MAXRADIUS) shr MAPBLOCKSHIFT
    var yl = (tmbbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) shr MAPBLOCKSHIFT
    var yh = (tmbbox[BOXTOP] - bmaporgy + MAXRADIUS) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!pBlockThingsIterator(bx, by, { argument0 -> pitCheckThing(argument0) }))
                return false

    xl = (tmbbox[BOXLEFT] - bmaporgx) shr MAPBLOCKSHIFT
    xh = (tmbbox[BOXRIGHT] - bmaporgx) shr MAPBLOCKSHIFT
    yl = (tmbbox[BOXBOTTOM] - bmaporgy) shr MAPBLOCKSHIFT
    yh = (tmbbox[BOXTOP] - bmaporgy) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!pBlockLinesIterator(bx, by, { argument0 -> pitCheckLine(argument0) }))
                return false

    return true
}

internal fun DoomEngineCore.pTryMove(thing: Actor, x: FixedPoint, y: FixedPoint): Boolean {
    floatok = false
    if (!pCheckPosition(thing, x, y))
        return false

    if (thing.flags and MF_NOCLIP == 0) {
        if (tmceilingz - tmfloorz < thing.height)
            return false

        floatok = true

        if (thing.flags and MF_TELEPORT == 0
            && tmceilingz - thing.z < thing.height
        )
            return false

        if (thing.flags and MF_TELEPORT == 0
            && tmfloorz - thing.z > 24 * FRACUNIT
        )
            return false

        if (thing.flags and (MF_DROPOFF or MF_FLOAT) == 0
            && tmfloorz - tmdropoffz > 24 * FRACUNIT
        )
            return false
    }

    pUnsetThingPosition(thing)

    val oldx = thing.x
    val oldy = thing.y
    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz
    thing.x = x
    thing.y = y

    pSetThingPosition(thing)

    if (thing.flags and (MF_TELEPORT or MF_NOCLIP) == 0) {
        while (numspechit != 0) {
            numspechit--
            val ld = spechit[numspechit]!!
            val side = pPointOnLineSide(thing.x, thing.y, ld)
            val oldside = pPointOnLineSide(oldx, oldy, ld)
            if (side != oldside) {
                if (ld.special != 0)
                    pCrossSpecialLine(ld.index, oldside, thing)
            }
        }
        numspechit = -1
    }

    return true
}

internal fun DoomEngineCore.pThingHeightClip(thing: Actor): Boolean {
    val onfloor = (thing.z == thing.floorz)

    pCheckPosition(thing, thing.x, thing.y)

    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz

    if (onfloor) {
        thing.z = thing.floorz
    } else {
        if (thing.z + thing.height > thing.ceilingz)
            thing.z = thing.ceilingz - thing.height
    }

    if (thing.ceilingz - thing.floorz < thing.height)
        return false

    return true
}

internal var DoomEngineCore.bestslidefrac: FixedPoint
    get() = stateCollision.bestslidefrac
    set(value) { stateCollision.bestslidefrac = value }
internal var DoomEngineCore.secondslidefrac: FixedPoint
    get() = stateCollision.secondslidefrac
    set(value) { stateCollision.secondslidefrac = value }

internal var DoomEngineCore.bestslideline: MapLine?
    get() = stateCollision.bestslideline
    set(value) { stateCollision.bestslideline = value }
internal var DoomEngineCore.secondslideline: MapLine?
    get() = stateCollision.secondslideline
    set(value) { stateCollision.secondslideline = value }

internal var DoomEngineCore.slidemo: Actor?
    get() = stateCollision.slidemo
    set(value) { stateCollision.slidemo = value }

internal var DoomEngineCore.tmxmove: FixedPoint
    get() = stateCollision.tmxmove
    set(value) { stateCollision.tmxmove = value }
internal var DoomEngineCore.tmymove: FixedPoint
    get() = stateCollision.tmymove
    set(value) { stateCollision.tmymove = value }

internal fun DoomEngineCore.pHitSlideLine(ld: MapLine) {
    if (ld.slopetype == ST_HORIZONTAL) {
        tmymove = 0
        return
    }

    if (ld.slopetype == ST_VERTICAL) {
        tmxmove = 0
        return
    }

    val side = pPointOnLineSide(slidemo!!.x, slidemo!!.y, ld)

    var lineangle: BinaryAngle = FixedGeometry.angleBetween(0, 0, ld.dx, ld.dy)

    if (side == 1)
        lineangle += ANG180

    val moveangle: BinaryAngle = FixedGeometry.angleBetween(0, 0, tmxmove, tmymove)
    var deltaangle: BinaryAngle = moveangle - lineangle

    if (deltaangle > ANG180)
        deltaangle += ANG180

    val lineangleFine = (lineangle shr ANGLETOFINESHIFT).toInt()
    val deltaangleFine = (deltaangle shr ANGLETOFINESHIFT).toInt()

    val movelen = pAproxDistance(tmxmove, tmymove)
    val newlen = fixedMul(movelen, FineCosineTable[deltaangleFine])

    tmxmove = fixedMul(newlen, FineCosineTable[lineangleFine])
    tmymove = fixedMul(newlen, finesine[lineangleFine])
}

internal fun DoomEngineCore.ptrSlideTraverse(`in`: PathIntercept): Boolean {
    if (!`in`.isaline)
        iError("PTR_SlideTraverse: not a line?")

    val li = `in`.line!!

    if (li.flags and ML_TWOSIDED == 0) {
        if (pPointOnLineSide(slidemo!!.x, slidemo!!.y, li) != 0) {
            return true
        }
    } else {
        pLineOpening(li)

        val blocksMovement = openrange < slidemo!!.height ||
            opentop - slidemo!!.z < slidemo!!.height ||
            openbottom - slidemo!!.z > 24 * FRACUNIT
        if (!blocksMovement) {
            return true
        }
    }

    if (`in`.frac < bestslidefrac) {
        secondslidefrac = bestslidefrac
        secondslideline = bestslideline
        bestslidefrac = `in`.frac
        bestslideline = li
    }

    return false
}

internal fun DoomEngineCore.pSlideMove(mo: Actor) {
    slidemo = mo
    var hitcount = 0

    retry@ while (true) {
        hitcount++
        if (hitcount == 3) {
            if (!pTryMove(mo, mo.x, mo.y + mo.momy))
                pTryMove(mo, mo.x + mo.momx, mo.y)
            return
        }

        val leadx: FixedPoint
        val leady: FixedPoint
        val trailx: FixedPoint
        val traily: FixedPoint

        if (mo.momx > 0) {
            leadx = mo.x + mo.radius
            trailx = mo.x - mo.radius
        } else {
            leadx = mo.x - mo.radius
            trailx = mo.x + mo.radius
        }

        if (mo.momy > 0) {
            leady = mo.y + mo.radius
            traily = mo.y - mo.radius
        } else {
            leady = mo.y - mo.radius
            traily = mo.y + mo.radius
        }

        bestslidefrac = FRACUNIT + 1

        pPathTraverse(leadx, leady, leadx + mo.momx, leady + mo.momy,
            PT_ADDLINES, { argument0 -> ptrSlideTraverse(argument0) })
        pPathTraverse(trailx, leady, trailx + mo.momx, leady + mo.momy,
            PT_ADDLINES, { argument0 -> ptrSlideTraverse(argument0) })
        pPathTraverse(leadx, traily, leadx + mo.momx, traily + mo.momy,
            PT_ADDLINES, { argument0 -> ptrSlideTraverse(argument0) })

        if (bestslidefrac == FRACUNIT + 1) {
            if (!pTryMove(mo, mo.x, mo.y + mo.momy))
                pTryMove(mo, mo.x + mo.momx, mo.y)
            return
        }

        bestslidefrac -= 0x800
        if (bestslidefrac > 0) {
            val newx = fixedMul(mo.momx, bestslidefrac)
            val newy = fixedMul(mo.momy, bestslidefrac)

            if (!pTryMove(mo, mo.x + newx, mo.y + newy)) {
                if (!pTryMove(mo, mo.x, mo.y + mo.momy))
                    pTryMove(mo, mo.x + mo.momx, mo.y)
                return
            }
        }

        bestslidefrac = FRACUNIT - (bestslidefrac + 0x800)

        if (bestslidefrac > FRACUNIT)
            bestslidefrac = FRACUNIT

        if (bestslidefrac <= 0)
            return

        tmxmove = fixedMul(mo.momx, bestslidefrac)
        tmymove = fixedMul(mo.momy, bestslidefrac)

        pHitSlideLine(bestslideline!!)

        mo.momx = tmxmove
        mo.momy = tmymove

        if (!pTryMove(mo, mo.x + tmxmove, mo.y + tmymove)) {
            continue@retry
        }
        return
    }
}

internal var DoomEngineCore.linetarget: Actor?
    get() = stateCollision.linetarget
    set(value) { stateCollision.linetarget = value }
internal var DoomEngineCore.shootthing: Actor?
    get() = stateCollision.shootthing
    set(value) { stateCollision.shootthing = value }

internal var DoomEngineCore.shootz: FixedPoint
    get() = stateCollision.shootz
    set(value) { stateCollision.shootz = value }

internal var DoomEngineCore.laDamage
    get() = stateCollision.laDamage
    set(value) { stateCollision.laDamage = value }
internal var DoomEngineCore.attackrange: FixedPoint
    get() = stateCollision.attackrange
    set(value) { stateCollision.attackrange = value }

internal var DoomEngineCore.aimslope: FixedPoint
    get() = stateCollision.aimslope
    set(value) { stateCollision.aimslope = value }


internal fun DoomEngineCore.ptrAimTraverse(`in`: PathIntercept): Boolean {
    if (`in`.isaline) {
        val li = `in`.line!!

        if (li.flags and ML_TWOSIDED == 0)
            return false

        pLineOpening(li)

        if (openbottom >= opentop)
            return false

        val dist = fixedMul(attackrange, `in`.frac)

        if (li.frontsector!!.floorheight != li.backsector!!.floorheight) {
            val slope = fixedDiv(openbottom - shootz, dist)
            if (slope > bottomslope)
                bottomslope = slope
        }

        if (li.frontsector!!.ceilingheight != li.backsector!!.ceilingheight) {
            val slope = fixedDiv(opentop - shootz, dist)
            if (slope < topslope)
                topslope = slope
        }

        if (topslope <= bottomslope)
            return false

        return true
    }

    val th = `in`.thing!!
    if (th === shootthing)
        return true

    if (th.flags and MF_SHOOTABLE == 0)
        return true

    val dist = fixedMul(attackrange, `in`.frac)
    var thingtopslope = fixedDiv(th.z + th.height - shootz, dist)

    if (thingtopslope < bottomslope)
        return true

    var thingbottomslope = fixedDiv(th.z - shootz, dist)

    if (thingbottomslope > topslope)
        return true

    if (thingtopslope > topslope)
        thingtopslope = topslope

    if (thingbottomslope < bottomslope)
        thingbottomslope = bottomslope

    aimslope = (thingtopslope + thingbottomslope) / 2
    linetarget = th

    return false
}

internal fun DoomEngineCore.ptrShootTraverse(`in`: PathIntercept): Boolean {
    if (`in`.isaline) {
        val li = `in`.line!!

        if (li.special != 0)
            pShootSpecialLine(shootthing!!, li)

        if (li.flags and ML_TWOSIDED != 0) {
            var hitline = false
            pLineOpening(li)

            val dist = fixedMul(attackrange, `in`.frac)

            if (li.frontsector!!.floorheight != li.backsector!!.floorheight) {
                val slope = fixedDiv(openbottom - shootz, dist)
                if (slope > aimslope)
                    hitline = true
            }

            if (!hitline
                && li.frontsector!!.ceilingheight != li.backsector!!.ceilingheight
            ) {
                val slope = fixedDiv(opentop - shootz, dist)
                if (slope < aimslope)
                    hitline = true
            }

            if (!hitline) {
                return true
            }
        }

        val frac = `in`.frac - fixedDiv(4 * FRACUNIT, attackrange)
        val x = trace.x + fixedMul(trace.dx, frac)
        val y = trace.y + fixedMul(trace.dy, frac)
        val z = shootz + fixedMul(aimslope, fixedMul(frac, attackrange))

        if (li.frontsector!!.ceilingpic == skyflatnum) {
            if (z > li.frontsector!!.ceilingheight)
                return false

            if (li.backsector != null && li.backsector!!.ceilingpic == skyflatnum)
                return false
        }

        pSpawnPuff(x, y, z)

        return false
    }

    val th = `in`.thing!!
    if (th === shootthing)
        return true

    if (th.flags and MF_SHOOTABLE == 0)
        return true

    val dist = fixedMul(attackrange, `in`.frac)
    val thingtopslope = fixedDiv(th.z + th.height - shootz, dist)

    if (thingtopslope < aimslope)
        return true

    val thingbottomslope = fixedDiv(th.z - shootz, dist)

    if (thingbottomslope > aimslope)
        return true

    val frac = `in`.frac - fixedDiv(10 * FRACUNIT, attackrange)

    val x = trace.x + fixedMul(trace.dx, frac)
    val y = trace.y + fixedMul(trace.dy, frac)
    val z = shootz + fixedMul(aimslope, fixedMul(frac, attackrange))

    if (`in`.thing!!.flags and MF_NOBLOOD != 0)
        pSpawnPuff(x, y, z)
    else
        pSpawnBlood(x, y, z, laDamage)

    if (laDamage != 0)
        pDamageMobj(th, shootthing, shootthing, laDamage)

    return false
}

internal fun DoomEngineCore.pAimLineAttack(t1: Actor, angleValue: BinaryAngle, distance: FixedPoint): FixedPoint {
    val angle = (angleValue shr ANGLETOFINESHIFT).toInt()
    shootthing = t1

    val x2 = t1.x + (distance shr FRACBITS) * FineCosineTable[angle]
    val y2 = t1.y + (distance shr FRACBITS) * finesine[angle]
    shootz = t1.z + (t1.height shr 1) + 8 * FRACUNIT

    topslope = 100 * FRACUNIT / 160
    bottomslope = -100 * FRACUNIT / 160

    attackrange = distance
    linetarget = null

    pPathTraverse(t1.x, t1.y,
        x2, y2,
        PT_ADDLINES or PT_ADDTHINGS,
        { argument0 -> ptrAimTraverse(argument0) })

    if (linetarget != null)
        return aimslope

    return 0
}

internal fun DoomEngineCore.pLineAttack(
    t1: Actor,
    angleValue: BinaryAngle,
    distance: FixedPoint,
    slope: FixedPoint,
    damage: Int,
) {
    val angle = (angleValue shr ANGLETOFINESHIFT).toInt()
    shootthing = t1
    laDamage = damage
    val x2 = t1.x + (distance shr FRACBITS) * FineCosineTable[angle]
    val y2 = t1.y + (distance shr FRACBITS) * finesine[angle]
    shootz = t1.z + (t1.height shr 1) + 8 * FRACUNIT
    attackrange = distance
    aimslope = slope

    pPathTraverse(t1.x, t1.y,
        x2, y2,
        PT_ADDLINES or PT_ADDTHINGS,
        { argument0 -> ptrShootTraverse(argument0) })
}

internal var DoomEngineCore.usething: Actor?
    get() = stateCollision.usething
    set(value) { stateCollision.usething = value }

internal fun DoomEngineCore.ptrUseTraverse(`in`: PathIntercept): Boolean {
    if (`in`.line!!.special == 0) {
        pLineOpening(`in`.line!!)
        if (openrange <= 0) {
            sStartSound(usething, SFX_NOWAY)

            return false
        }
        return true
    }

    var side = 0
    if (pPointOnLineSide(usething!!.x, usething!!.y, `in`.line!!) == 1)
        side = 1


    pUseSpecialLine(usething!!, `in`.line!!, side)

    return false
}

internal fun DoomEngineCore.pUseLines(player: Player) {
    usething = player.mo

    val angle = (player.mo!!.angle shr ANGLETOFINESHIFT).toInt()

    val x1 = player.mo!!.x
    val y1 = player.mo!!.y
    val x2 = x1 + (USERANGE shr FRACBITS) * FineCosineTable[angle]
    val y2 = y1 + (USERANGE shr FRACBITS) * finesine[angle]

    pPathTraverse(x1, y1, x2, y2, PT_ADDLINES, { argument0 -> ptrUseTraverse(argument0) })
}

internal var DoomEngineCore.bombsource: Actor?
    get() = stateCollision.bombsource
    set(value) { stateCollision.bombsource = value }
internal var DoomEngineCore.bombspot: Actor?
    get() = stateCollision.bombspot
    set(value) { stateCollision.bombspot = value }
internal var DoomEngineCore.bombdamage
    get() = stateCollision.bombdamage
    set(value) { stateCollision.bombdamage = value }

internal fun DoomEngineCore.pitRadiusAttack(thing: Actor): Boolean {
    if (thing.flags and MF_SHOOTABLE == 0)
        return true

    if (thing.type == MT_CYBORG
        || thing.type == MT_SPIDER
    )
        return true

    val dx = abs(thing.x - bombspot!!.x)
    val dy = abs(thing.y - bombspot!!.y)

    var dist = if (dx > dy) dx else dy
    dist = (dist - thing.radius) shr FRACBITS

    if (dist < 0)
        dist = 0

    if (dist >= bombdamage)
        return true

    if (pCheckSight(thing, bombspot!!)) {
        pDamageMobj(thing, bombspot, bombsource, bombdamage - dist)
    }

    return true
}

internal fun DoomEngineCore.pRadiusAttack(spot: Actor, source: Actor?, damage: Int) {
    val dist = (damage + MAXRADIUS) shl FRACBITS
    val yh = (spot.y + dist - bmaporgy) shr MAPBLOCKSHIFT
    val yl = (spot.y - dist - bmaporgy) shr MAPBLOCKSHIFT
    val xh = (spot.x + dist - bmaporgx) shr MAPBLOCKSHIFT
    val xl = (spot.x - dist - bmaporgx) shr MAPBLOCKSHIFT
    bombspot = spot
    bombsource = source
    bombdamage = damage

    for (y in yl..yh)
        for (x in xl..xh)
            pBlockThingsIterator(x, y, { argument0 -> pitRadiusAttack(argument0) })
}

internal var DoomEngineCore.crushchange
    get() = stateCollision.crushchange
    set(value) { stateCollision.crushchange = value }
internal var DoomEngineCore.nofit
    get() = stateCollision.nofit
    set(value) { stateCollision.nofit = value }

internal fun DoomEngineCore.pitChangeSector(thing: Actor): Boolean {
    if (pThingHeightClip(thing)) {
        return true
    }

    if (thing.health <= 0) {
        pSetMobjState(thing, S_GIBS)

        thing.flags = thing.flags and MF_SOLID.inv()
        thing.height = 0
        thing.radius = 0

        return true
    }

    if (thing.flags and MF_DROPPED != 0) {
        pRemoveMobj(thing)

        return true
    }

    if (thing.flags and MF_SHOOTABLE == 0) {
        return true
    }

    nofit = true

    if (crushchange && (leveltime and 3) == 0) {
        pDamageMobj(thing, null, null, 10)

        val mo = pSpawnMobj(thing.x,
            thing.y,
            thing.z + thing.height / 2, MT_BLOOD)

        mo.momx = (pRandom() - pRandom()) shl 12
        mo.momy = (pRandom() - pRandom()) shl 12
    }

    return true
}

internal fun DoomEngineCore.pChangeSector(sector: Sector, crunch: Boolean): Boolean {
    nofit = false
    crushchange = crunch

    for (x in sector.blockbox[BOXLEFT]..sector.blockbox[BOXRIGHT])
        for (y in sector.blockbox[BOXBOTTOM]..sector.blockbox[BOXTOP])
            pBlockThingsIterator(x, y, { argument0 -> pitChangeSector(argument0) })

    return nofit
}
