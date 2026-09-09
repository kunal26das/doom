// Port of linuxdoom-1.10 p_map.c -- movement, collision handling.
// Shooting and aiming.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.geometry.FixedGeometry

import kotlin.math.abs

internal val DoomEngineCore.tmbbox
    get() = stateCollision.tmbbox
internal var DoomEngineCore.tmthing: mobj_t?
    get() = stateCollision.tmthing
    set(value) { stateCollision.tmthing = value }
internal var DoomEngineCore.tmflags
    get() = stateCollision.tmflags
    set(value) { stateCollision.tmflags = value }
internal var DoomEngineCore.tmx: fixed_t
    get() = stateCollision.tmx
    set(value) { stateCollision.tmx = value }
internal var DoomEngineCore.tmy: fixed_t
    get() = stateCollision.tmy
    set(value) { stateCollision.tmy = value }

internal var DoomEngineCore.floatok
    get() = stateCollision.floatok
    set(value) { stateCollision.floatok = value }

internal var DoomEngineCore.tmfloorz: fixed_t
    get() = stateCollision.tmfloorz
    set(value) { stateCollision.tmfloorz = value }
internal var DoomEngineCore.tmceilingz: fixed_t
    get() = stateCollision.tmceilingz
    set(value) { stateCollision.tmceilingz = value }
internal var DoomEngineCore.tmdropoffz: fixed_t
    get() = stateCollision.tmdropoffz
    set(value) { stateCollision.tmdropoffz = value }

internal var DoomEngineCore.ceilingline: line_t?
    get() = stateCollision.ceilingline
    set(value) { stateCollision.ceilingline = value }

// keep track of special lines as they are hit,
// but don't process them until the move is proven valid
internal const val MAXSPECIALCROSS = 8

internal val DoomEngineCore.spechit
    get() = stateCollision.spechit
internal var DoomEngineCore.numspechit
    get() = stateCollision.numspechit
    set(value) { stateCollision.numspechit = value }

//
// TELEPORT MOVE
//

//
// PIT_StompThing
//
internal fun DoomEngineCore.PIT_StompThing(thing: mobj_t): Boolean {
    if (thing.flags and MF_SHOOTABLE == 0)
        return true

    val blockdist = thing.radius + tmthing!!.radius

    if (abs(thing.x - tmx) >= blockdist
        || abs(thing.y - tmy) >= blockdist
    ) {
        // didn't hit it
        return true
    }

    // don't clip against self
    if (thing === tmthing)
        return true

    // monsters don't stomp things except on boss level
    if (tmthing!!.player == null && gamemap != 30)
        return false

    P_DamageMobj(thing, tmthing, tmthing, 10000)

    return true
}

//
// P_TeleportMove
//
internal fun DoomEngineCore.P_TeleportMove(thing: mobj_t, x: fixed_t, y: fixed_t): Boolean {
    // kill anything occupying the position
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

    // The base floor/ceiling is from the subsector
    // that contains the point.
    // Any contacted lines the step closer together
    // will adjust them.
    tmdropoffz = newsubsec.sector!!.floorheight
    tmfloorz = tmdropoffz
    tmceilingz = newsubsec.sector!!.ceilingheight

    validcount++
    numspechit = 0

    // stomp on any things contacted
    val xl = (tmbbox[BOXLEFT] - bmaporgx - MAXRADIUS) shr MAPBLOCKSHIFT
    val xh = (tmbbox[BOXRIGHT] - bmaporgx + MAXRADIUS) shr MAPBLOCKSHIFT
    val yl = (tmbbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) shr MAPBLOCKSHIFT
    val yh = (tmbbox[BOXTOP] - bmaporgy + MAXRADIUS) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!P_BlockThingsIterator(bx, by, { argument0 -> PIT_StompThing(argument0) }))
                return false

    // the move is ok,
    // so link the thing into its new position
    P_UnsetThingPosition(thing)

    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz
    thing.x = x
    thing.y = y

    P_SetThingPosition(thing)

    return true
}

//
// MOVEMENT ITERATOR FUNCTIONS
//

//
// PIT_CheckLine
// Adjusts tmfloorz and tmceilingz as lines are contacted
//
internal fun DoomEngineCore.PIT_CheckLine(ld: line_t): Boolean {
    if (tmbbox[BOXRIGHT] <= ld.bbox[BOXLEFT]
        || tmbbox[BOXLEFT] >= ld.bbox[BOXRIGHT]
        || tmbbox[BOXTOP] <= ld.bbox[BOXBOTTOM]
        || tmbbox[BOXBOTTOM] >= ld.bbox[BOXTOP]
    )
        return true

    if (P_BoxOnLineSide(tmbbox, ld) != -1)
        return true

    // A line has been hit

    // The moving thing's destination position will cross
    // the given line.
    // If this should not be allowed, return false.
    // If the line is special, keep track of it
    // to process later if the move is proven ok.
    // NOTE: specials are NOT sorted by order,
    // so two special lines that are only 8 pixels apart
    // could be crossed in either order.

    if (ld.backsector == null)
        return false    // one sided line

    if (tmthing!!.flags and MF_MISSILE == 0) {
        if (ld.flags and ML_BLOCKING != 0)
            return false    // explicitly blocking everything

        if (tmthing!!.player == null && ld.flags and ML_BLOCKMONSTERS != 0)
            return false    // block monsters only
    }

    // set openrange, opentop, openbottom
    P_LineOpening(ld)

    // adjust floor / ceiling heights
    if (opentop < tmceilingz) {
        tmceilingz = opentop
        ceilingline = ld
    }

    if (openbottom > tmfloorz)
        tmfloorz = openbottom

    if (lowfloor < tmdropoffz)
        tmdropoffz = lowfloor

    // if contacted a special line, add it to the list
    if (ld.special != 0) {
        spechit[numspechit] = ld
        numspechit++
    }

    return true
}

//
// PIT_CheckThing
//
internal fun DoomEngineCore.PIT_CheckThing(thing: mobj_t): Boolean {
    if (thing.flags and (MF_SOLID or MF_SPECIAL or MF_SHOOTABLE) == 0)
        return true

    val blockdist = thing.radius + tmthing!!.radius

    if (abs(thing.x - tmx) >= blockdist
        || abs(thing.y - tmy) >= blockdist
    ) {
        // didn't hit it
        return true
    }

    // don't clip against self
    if (thing === tmthing)
        return true

    // check for skulls slamming into things
    if (tmthing!!.flags and MF_SKULLFLY != 0) {
        val damage = ((P_Random() % 8) + 1) * tmthing!!.info!!.damage

        P_DamageMobj(thing, tmthing, tmthing, damage)

        tmthing!!.flags = tmthing!!.flags and MF_SKULLFLY.inv()
        tmthing!!.momx = 0
        tmthing!!.momy = 0
        tmthing!!.momz = 0

        P_SetMobjState(tmthing!!, tmthing!!.info!!.spawnstate)

        return false    // stop moving
    }

    // missiles can hit other things
    if (tmthing!!.flags and MF_MISSILE != 0) {
        // see if it went over / under
        if (tmthing!!.z > thing.z + thing.height)
            return true    // overhead
        if (tmthing!!.z + tmthing!!.height < thing.z)
            return true    // underneath

        if (tmthing!!.target != null && (
                tmthing!!.target!!.type == thing.type ||
                (tmthing!!.target!!.type == MT_KNIGHT && thing.type == MT_BRUISER) ||
                (tmthing!!.target!!.type == MT_BRUISER && thing.type == MT_KNIGHT))
        ) {
            // Don't hit same species as originator.
            if (thing === tmthing!!.target)
                return true

            if (thing.type != MT_PLAYER) {
                // Explode, but do no damage.
                // Let players missile other players.
                return false
            }
        }

        if (thing.flags and MF_SHOOTABLE == 0) {
            // didn't do any damage
            return thing.flags and MF_SOLID == 0
        }

        // damage / explode
        val damage = ((P_Random() % 8) + 1) * tmthing!!.info!!.damage
        P_DamageMobj(thing, tmthing, tmthing!!.target, damage)

        // don't traverse any more
        return false
    }

    // check for special pickup
    if (thing.flags and MF_SPECIAL != 0) {
        val solid = thing.flags and MF_SOLID != 0
        if (tmflags and MF_PICKUP != 0) {
            // can remove thing
            P_TouchSpecialThing(thing, tmthing!!)
        }
        return !solid
    }

    return thing.flags and MF_SOLID == 0
}

//
// MOVEMENT CLIPPING
//

//
// P_CheckPosition
// This is purely informative, nothing is modified
// (except things picked up).
//
// in:
//  a mobj_t (can be valid or invalid)
//  a position to be checked
//   (doesn't need to be related to the mobj_t->x,y)
//
// during:
//  special things are touched if MF_PICKUP
//  early out on solid lines?
//
// out:
//  newsubsec
//  floorz
//  ceilingz
//  tmdropoffz
//   the lowest point contacted
//   (monsters won't move to a dropoff)
//  speciallines[]
//  numspeciallines
//
internal fun DoomEngineCore.P_CheckPosition(thing: mobj_t, x: fixed_t, y: fixed_t): Boolean {
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

    // The base floor / ceiling is from the subsector
    // that contains the point.
    // Any contacted lines the step closer together
    // will adjust them.
    tmdropoffz = newsubsec.sector!!.floorheight
    tmfloorz = tmdropoffz
    tmceilingz = newsubsec.sector!!.ceilingheight

    validcount++
    numspechit = 0

    if (tmflags and MF_NOCLIP != 0)
        return true

    // Check things first, possibly picking things up.
    // The bounding box is extended by MAXRADIUS
    // because mobj_ts are grouped into mapblocks
    // based on their origin point, and can overlap
    // into adjacent blocks by up to MAXRADIUS units.
    var xl = (tmbbox[BOXLEFT] - bmaporgx - MAXRADIUS) shr MAPBLOCKSHIFT
    var xh = (tmbbox[BOXRIGHT] - bmaporgx + MAXRADIUS) shr MAPBLOCKSHIFT
    var yl = (tmbbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) shr MAPBLOCKSHIFT
    var yh = (tmbbox[BOXTOP] - bmaporgy + MAXRADIUS) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!P_BlockThingsIterator(bx, by, { argument0 -> PIT_CheckThing(argument0) }))
                return false

    // check lines
    xl = (tmbbox[BOXLEFT] - bmaporgx) shr MAPBLOCKSHIFT
    xh = (tmbbox[BOXRIGHT] - bmaporgx) shr MAPBLOCKSHIFT
    yl = (tmbbox[BOXBOTTOM] - bmaporgy) shr MAPBLOCKSHIFT
    yh = (tmbbox[BOXTOP] - bmaporgy) shr MAPBLOCKSHIFT

    for (bx in xl..xh)
        for (by in yl..yh)
            if (!P_BlockLinesIterator(bx, by, { argument0 -> PIT_CheckLine(argument0) }))
                return false

    return true
}

//
// P_TryMove
// Attempt to move to a new position,
// crossing special lines unless MF_TELEPORT is set.
//
internal fun DoomEngineCore.P_TryMove(thing: mobj_t, x: fixed_t, y: fixed_t): Boolean {
    floatok = false
    if (!P_CheckPosition(thing, x, y))
        return false    // solid wall or thing

    if (thing.flags and MF_NOCLIP == 0) {
        if (tmceilingz - tmfloorz < thing.height)
            return false    // doesn't fit

        floatok = true

        if (thing.flags and MF_TELEPORT == 0
            && tmceilingz - thing.z < thing.height
        )
            return false    // mobj must lower itself to fit

        if (thing.flags and MF_TELEPORT == 0
            && tmfloorz - thing.z > 24 * FRACUNIT
        )
            return false    // too big a step up

        if (thing.flags and (MF_DROPOFF or MF_FLOAT) == 0
            && tmfloorz - tmdropoffz > 24 * FRACUNIT
        )
            return false    // don't stand over a dropoff
    }

    // the move is ok,
    // so link the thing into its new position
    P_UnsetThingPosition(thing)

    val oldx = thing.x
    val oldy = thing.y
    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz
    thing.x = x
    thing.y = y

    P_SetThingPosition(thing)

    // if any special lines were hit, do the effect
    if (thing.flags and (MF_TELEPORT or MF_NOCLIP) == 0) {
        // C: while (numspechit--)
        while (numspechit != 0) {
            numspechit--
            // see if the line was crossed
            val ld = spechit[numspechit]!!
            val side = P_PointOnLineSide(thing.x, thing.y, ld)
            val oldside = P_PointOnLineSide(oldx, oldy, ld)
            if (side != oldside) {
                if (ld.special != 0)
                    P_CrossSpecialLine(ld.index, oldside, thing)
            }
        }
        numspechit = -1    // the C post-decrement leaves it at -1
    }

    return true
}

//
// P_ThingHeightClip
// Takes a valid thing and adjusts the thing->floorz,
// thing->ceilingz, and possibly thing->z.
// This is called for all nearby monsters
// whenever a sector changes height.
// If the thing doesn't fit,
// the z will be set to the lowest value
// and false will be returned.
//
internal fun DoomEngineCore.P_ThingHeightClip(thing: mobj_t): Boolean {
    val onfloor = (thing.z == thing.floorz)

    P_CheckPosition(thing, thing.x, thing.y)
    // what about stranding a monster partially off an edge?

    thing.floorz = tmfloorz
    thing.ceilingz = tmceilingz

    if (onfloor) {
        // walking monsters rise and fall with the floor
        thing.z = thing.floorz
    } else {
        // don't adjust a floating monster unless forced to
        if (thing.z + thing.height > thing.ceilingz)
            thing.z = thing.ceilingz - thing.height
    }

    if (thing.ceilingz - thing.floorz < thing.height)
        return false

    return true
}

internal var DoomEngineCore.bestslidefrac: fixed_t
    get() = stateCollision.bestslidefrac
    set(value) { stateCollision.bestslidefrac = value }
internal var DoomEngineCore.secondslidefrac: fixed_t
    get() = stateCollision.secondslidefrac
    set(value) { stateCollision.secondslidefrac = value }

internal var DoomEngineCore.bestslideline: line_t?
    get() = stateCollision.bestslideline
    set(value) { stateCollision.bestslideline = value }
internal var DoomEngineCore.secondslideline: line_t?
    get() = stateCollision.secondslideline
    set(value) { stateCollision.secondslideline = value }

internal var DoomEngineCore.slidemo: mobj_t?
    get() = stateCollision.slidemo
    set(value) { stateCollision.slidemo = value }

internal var DoomEngineCore.tmxmove: fixed_t
    get() = stateCollision.tmxmove
    set(value) { stateCollision.tmxmove = value }
internal var DoomEngineCore.tmymove: fixed_t
    get() = stateCollision.tmymove
    set(value) { stateCollision.tmymove = value }

//
// P_HitSlideLine
// Adjusts the xmove / ymove
// so that the next move will slide along the wall.
//
internal fun DoomEngineCore.P_HitSlideLine(ld: line_t) {
    if (ld.slopetype == ST_HORIZONTAL) {
        tmymove = 0
        return
    }

    if (ld.slopetype == ST_VERTICAL) {
        tmxmove = 0
        return
    }

    val side = P_PointOnLineSide(slidemo!!.x, slidemo!!.y, ld)

    var lineangle: angle_t = FixedGeometry.angleBetween(0, 0, ld.dx, ld.dy)

    if (side == 1)
        lineangle += ANG180

    val moveangle: angle_t = FixedGeometry.angleBetween(0, 0, tmxmove, tmymove)
    var deltaangle: angle_t = moveangle - lineangle

    if (deltaangle > ANG180)
        deltaangle += ANG180
    //	I_Error ("SlideLine: ang>ANG180");

    val lineangle_fine = (lineangle shr ANGLETOFINESHIFT).toInt()
    val deltaangle_fine = (deltaangle shr ANGLETOFINESHIFT).toInt()

    val movelen = P_AproxDistance(tmxmove, tmymove)
    val newlen = FixedMul(movelen, finecosine[deltaangle_fine])

    tmxmove = FixedMul(newlen, finecosine[lineangle_fine])
    tmymove = FixedMul(newlen, finesine[lineangle_fine])
}

//
// PTR_SlideTraverse
//
internal fun DoomEngineCore.PTR_SlideTraverse(`in`: intercept_t): Boolean {
    if (!`in`.isaline)
        I_Error("PTR_SlideTraverse: not a line?")

    val li = `in`.line!!

    // goto isblocking -> flag; behavior identical to the C gotos
    var isblocking = false

    if (li.flags and ML_TWOSIDED == 0) {
        if (P_PointOnLineSide(slidemo!!.x, slidemo!!.y, li) != 0) {
            // don't hit the back side
            return true
        }
        isblocking = true
    } else {
        // set openrange, opentop, openbottom
        P_LineOpening(li)

        if (openrange < slidemo!!.height)
            isblocking = true    // doesn't fit
        else if (opentop - slidemo!!.z < slidemo!!.height)
            isblocking = true    // mobj is too high
        else if (openbottom - slidemo!!.z > 24 * FRACUNIT)
            isblocking = true    // too big a step up
        else {
            // this line doesn't block movement
            return true
        }
    }

    // the line does block movement,
    // see if it is closer than best so far
    // isblocking:
    if (`in`.frac < bestslidefrac) {
        secondslidefrac = bestslidefrac
        secondslideline = bestslideline
        bestslidefrac = `in`.frac
        bestslideline = li
    }

    return false    // stop
}

//
// P_SlideMove
// The momx / momy move is bad, so try to slide
// along a wall.
// Find the first line hit, move flush to it,
// and slide along it
//
// This is a kludgy mess.
//
internal fun DoomEngineCore.P_SlideMove(mo: mobj_t) {
    slidemo = mo
    var hitcount = 0

    retry@ while (true) {
        hitcount++
        if (hitcount == 3) {
            // goto stairstep: don't loop forever
            if (!P_TryMove(mo, mo.x, mo.y + mo.momy))
                P_TryMove(mo, mo.x + mo.momx, mo.y)
            return
        }

        // trace along the three leading corners
        val leadx: fixed_t
        val leady: fixed_t
        val trailx: fixed_t
        val traily: fixed_t

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

        P_PathTraverse(leadx, leady, leadx + mo.momx, leady + mo.momy,
            PT_ADDLINES, { argument0 -> PTR_SlideTraverse(argument0) })
        P_PathTraverse(trailx, leady, trailx + mo.momx, leady + mo.momy,
            PT_ADDLINES, { argument0 -> PTR_SlideTraverse(argument0) })
        P_PathTraverse(leadx, traily, leadx + mo.momx, traily + mo.momy,
            PT_ADDLINES, { argument0 -> PTR_SlideTraverse(argument0) })

        // move up to the wall
        if (bestslidefrac == FRACUNIT + 1) {
            // the move most have hit the middle, so stairstep
            // stairstep:
            if (!P_TryMove(mo, mo.x, mo.y + mo.momy))
                P_TryMove(mo, mo.x + mo.momx, mo.y)
            return
        }

        // fudge a bit to make sure it doesn't hit
        bestslidefrac -= 0x800
        if (bestslidefrac > 0) {
            val newx = FixedMul(mo.momx, bestslidefrac)
            val newy = FixedMul(mo.momy, bestslidefrac)

            if (!P_TryMove(mo, mo.x + newx, mo.y + newy)) {
                // goto stairstep
                if (!P_TryMove(mo, mo.x, mo.y + mo.momy))
                    P_TryMove(mo, mo.x + mo.momx, mo.y)
                return
            }
        }

        // Now continue along the wall.
        // First calculate remainder.
        bestslidefrac = FRACUNIT - (bestslidefrac + 0x800)

        if (bestslidefrac > FRACUNIT)
            bestslidefrac = FRACUNIT

        if (bestslidefrac <= 0)
            return

        tmxmove = FixedMul(mo.momx, bestslidefrac)
        tmymove = FixedMul(mo.momy, bestslidefrac)

        P_HitSlideLine(bestslideline!!)    // clip the moves

        mo.momx = tmxmove
        mo.momy = tmymove

        if (!P_TryMove(mo, mo.x + tmxmove, mo.y + tmymove)) {
            continue@retry
        }
        return
    }
}

internal var DoomEngineCore.linetarget: mobj_t?
    get() = stateCollision.linetarget
    set(value) { stateCollision.linetarget = value }
internal var DoomEngineCore.shootthing: mobj_t?
    get() = stateCollision.shootthing
    set(value) { stateCollision.shootthing = value }

internal var DoomEngineCore.shootz: fixed_t
    get() = stateCollision.shootz
    set(value) { stateCollision.shootz = value }

internal var DoomEngineCore.la_damage
    get() = stateCollision.la_damage
    set(value) { stateCollision.la_damage = value }
internal var DoomEngineCore.attackrange: fixed_t
    get() = stateCollision.attackrange
    set(value) { stateCollision.attackrange = value }

internal var DoomEngineCore.aimslope: fixed_t
    get() = stateCollision.aimslope
    set(value) { stateCollision.aimslope = value }

// slopes to top and bottom of target
// (topslope/bottomslope live in p_sight.kt)

//
// PTR_AimTraverse
// Sets linetaget and aimslope when a target is aimed at.
//
internal fun DoomEngineCore.PTR_AimTraverse(`in`: intercept_t): Boolean {
    if (`in`.isaline) {
        val li = `in`.line!!

        if (li.flags and ML_TWOSIDED == 0)
            return false    // stop

        // Crosses a two sided line.
        // A two sided line will restrict
        // the possible target ranges.
        P_LineOpening(li)

        if (openbottom >= opentop)
            return false    // stop

        val dist = FixedMul(attackrange, `in`.frac)

        if (li.frontsector!!.floorheight != li.backsector!!.floorheight) {
            val slope = FixedDiv(openbottom - shootz, dist)
            if (slope > bottomslope)
                bottomslope = slope
        }

        if (li.frontsector!!.ceilingheight != li.backsector!!.ceilingheight) {
            val slope = FixedDiv(opentop - shootz, dist)
            if (slope < topslope)
                topslope = slope
        }

        if (topslope <= bottomslope)
            return false    // stop

        return true    // shot continues
    }

    // shoot a thing
    val th = `in`.thing!!
    if (th === shootthing)
        return true    // can't shoot self

    if (th.flags and MF_SHOOTABLE == 0)
        return true    // corpse or something

    // check angles to see if the thing can be aimed at
    val dist = FixedMul(attackrange, `in`.frac)
    var thingtopslope = FixedDiv(th.z + th.height - shootz, dist)

    if (thingtopslope < bottomslope)
        return true    // shot over the thing

    var thingbottomslope = FixedDiv(th.z - shootz, dist)

    if (thingbottomslope > topslope)
        return true    // shot under the thing

    // this thing can be hit!
    if (thingtopslope > topslope)
        thingtopslope = topslope

    if (thingbottomslope < bottomslope)
        thingbottomslope = bottomslope

    aimslope = (thingtopslope + thingbottomslope) / 2
    linetarget = th

    return false    // don't go any farther
}

//
// PTR_ShootTraverse
//
internal fun DoomEngineCore.PTR_ShootTraverse(`in`: intercept_t): Boolean {
    if (`in`.isaline) {
        val li = `in`.line!!

        if (li.special != 0)
            P_ShootSpecialLine(shootthing!!, li)

        // goto hitline -> flag; behavior identical to the C gotos
        var hitline = false

        if (li.flags and ML_TWOSIDED == 0)
            hitline = true
        else {
            // crosses a two sided line
            P_LineOpening(li)

            val dist = FixedMul(attackrange, `in`.frac)

            if (li.frontsector!!.floorheight != li.backsector!!.floorheight) {
                val slope = FixedDiv(openbottom - shootz, dist)
                if (slope > aimslope)
                    hitline = true
            }

            if (!hitline
                && li.frontsector!!.ceilingheight != li.backsector!!.ceilingheight
            ) {
                val slope = FixedDiv(opentop - shootz, dist)
                if (slope < aimslope)
                    hitline = true
            }

            if (!hitline) {
                // shot continues
                return true
            }
        }

        // hit line
        // hitline:
        // position a bit closer
        val frac = `in`.frac - FixedDiv(4 * FRACUNIT, attackrange)
        val x = trace.x + FixedMul(trace.dx, frac)
        val y = trace.y + FixedMul(trace.dy, frac)
        val z = shootz + FixedMul(aimslope, FixedMul(frac, attackrange))

        if (li.frontsector!!.ceilingpic == skyflatnum) {
            // don't shoot the sky!
            if (z > li.frontsector!!.ceilingheight)
                return false

            // it's a sky hack wall
            if (li.backsector != null && li.backsector!!.ceilingpic == skyflatnum)
                return false
        }

        // Spawn bullet puffs.
        P_SpawnPuff(x, y, z)

        // don't go any farther
        return false
    }

    // shoot a thing
    val th = `in`.thing!!
    if (th === shootthing)
        return true    // can't shoot self

    if (th.flags and MF_SHOOTABLE == 0)
        return true    // corpse or something

    // check angles to see if the thing can be aimed at
    val dist = FixedMul(attackrange, `in`.frac)
    val thingtopslope = FixedDiv(th.z + th.height - shootz, dist)

    if (thingtopslope < aimslope)
        return true    // shot over the thing

    val thingbottomslope = FixedDiv(th.z - shootz, dist)

    if (thingbottomslope > aimslope)
        return true    // shot under the thing

    // hit thing
    // position a bit closer
    val frac = `in`.frac - FixedDiv(10 * FRACUNIT, attackrange)

    val x = trace.x + FixedMul(trace.dx, frac)
    val y = trace.y + FixedMul(trace.dy, frac)
    val z = shootz + FixedMul(aimslope, FixedMul(frac, attackrange))

    // Spawn bullet puffs or blod spots,
    // depending on target type.
    if (`in`.thing!!.flags and MF_NOBLOOD != 0)
        P_SpawnPuff(x, y, z)
    else
        P_SpawnBlood(x, y, z, la_damage)

    if (la_damage != 0)
        P_DamageMobj(th, shootthing, shootthing, la_damage)

    // don't go any farther
    return false
}

//
// P_AimLineAttack
//
internal fun DoomEngineCore.P_AimLineAttack(t1: mobj_t, angle: angle_t, distance: fixed_t): fixed_t {
    val angle = (angle shr ANGLETOFINESHIFT).toInt()
    shootthing = t1

    val x2 = t1.x + (distance shr FRACBITS) * finecosine[angle]
    val y2 = t1.y + (distance shr FRACBITS) * finesine[angle]
    shootz = t1.z + (t1.height shr 1) + 8 * FRACUNIT

    // can't shoot outside view angles
    topslope = 100 * FRACUNIT / 160
    bottomslope = -100 * FRACUNIT / 160

    attackrange = distance
    linetarget = null

    P_PathTraverse(t1.x, t1.y,
        x2, y2,
        PT_ADDLINES or PT_ADDTHINGS,
        { argument0 -> PTR_AimTraverse(argument0) })

    if (linetarget != null)
        return aimslope

    return 0
}

//
// P_LineAttack
// If damage == 0, it is just a test trace
// that will leave linetarget set.
//
internal fun DoomEngineCore.P_LineAttack(
    t1: mobj_t,
    angle: angle_t,
    distance: fixed_t,
    slope: fixed_t,
    damage: Int,
) {
    val angle = (angle shr ANGLETOFINESHIFT).toInt()
    shootthing = t1
    la_damage = damage
    val x2 = t1.x + (distance shr FRACBITS) * finecosine[angle]
    val y2 = t1.y + (distance shr FRACBITS) * finesine[angle]
    shootz = t1.z + (t1.height shr 1) + 8 * FRACUNIT
    attackrange = distance
    aimslope = slope

    P_PathTraverse(t1.x, t1.y,
        x2, y2,
        PT_ADDLINES or PT_ADDTHINGS,
        { argument0 -> PTR_ShootTraverse(argument0) })
}

internal var DoomEngineCore.usething: mobj_t?
    get() = stateCollision.usething
    set(value) { stateCollision.usething = value }

internal fun DoomEngineCore.PTR_UseTraverse(`in`: intercept_t): Boolean {
    if (`in`.line!!.special == 0) {
        P_LineOpening(`in`.line!!)
        if (openrange <= 0) {
            S_StartSound(usething, sfx_noway)

            // can't use through a wall
            return false
        }
        // not a special line, but keep checking
        return true
    }

    var side = 0
    if (P_PointOnLineSide(usething!!.x, usething!!.y, `in`.line!!) == 1)
        side = 1

    //	return false;		// don't use back side

    P_UseSpecialLine(usething!!, `in`.line!!, side)

    // can't use for than one special line in a row
    return false
}

//
// P_UseLines
// Looks for special lines in front of the player to activate.
//
internal fun DoomEngineCore.P_UseLines(player: player_t) {
    usething = player.mo

    val angle = (player.mo!!.angle shr ANGLETOFINESHIFT).toInt()

    val x1 = player.mo!!.x
    val y1 = player.mo!!.y
    val x2 = x1 + (USERANGE shr FRACBITS) * finecosine[angle]
    val y2 = y1 + (USERANGE shr FRACBITS) * finesine[angle]

    P_PathTraverse(x1, y1, x2, y2, PT_ADDLINES, { argument0 -> PTR_UseTraverse(argument0) })
}

internal var DoomEngineCore.bombsource: mobj_t?
    get() = stateCollision.bombsource
    set(value) { stateCollision.bombsource = value }
internal var DoomEngineCore.bombspot: mobj_t?
    get() = stateCollision.bombspot
    set(value) { stateCollision.bombspot = value }
internal var DoomEngineCore.bombdamage
    get() = stateCollision.bombdamage
    set(value) { stateCollision.bombdamage = value }

//
// PIT_RadiusAttack
// "bombsource" is the creature
// that caused the explosion at "bombspot".
//
internal fun DoomEngineCore.PIT_RadiusAttack(thing: mobj_t): Boolean {
    if (thing.flags and MF_SHOOTABLE == 0)
        return true

    // Boss spider and cyborg
    // take no damage from concussion.
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
        return true    // out of range

    if (P_CheckSight(thing, bombspot!!)) {
        // must be in direct path
        P_DamageMobj(thing, bombspot, bombsource, bombdamage - dist)
    }

    return true
}

//
// P_RadiusAttack
// Source is the creature that caused the explosion at spot.
//
internal fun DoomEngineCore.P_RadiusAttack(spot: mobj_t, source: mobj_t?, damage: Int) {
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
            P_BlockThingsIterator(x, y, { argument0 -> PIT_RadiusAttack(argument0) })
}

internal var DoomEngineCore.crushchange
    get() = stateCollision.crushchange
    set(value) { stateCollision.crushchange = value }
internal var DoomEngineCore.nofit
    get() = stateCollision.nofit
    set(value) { stateCollision.nofit = value }

//
// PIT_ChangeSector
//
internal fun DoomEngineCore.PIT_ChangeSector(thing: mobj_t): Boolean {
    if (P_ThingHeightClip(thing)) {
        // keep checking
        return true
    }

    // crunch bodies to giblets
    if (thing.health <= 0) {
        P_SetMobjState(thing, S_GIBS)

        thing.flags = thing.flags and MF_SOLID.inv()
        thing.height = 0
        thing.radius = 0

        // keep checking
        return true
    }

    // crunch dropped items
    if (thing.flags and MF_DROPPED != 0) {
        P_RemoveMobj(thing)

        // keep checking
        return true
    }

    if (thing.flags and MF_SHOOTABLE == 0) {
        // assume it is bloody gibs or something
        return true
    }

    nofit = true

    if (crushchange && (leveltime and 3) == 0) {
        P_DamageMobj(thing, null, null, 10)

        // spray blood in a random direction
        val mo = P_SpawnMobj(thing.x,
            thing.y,
            thing.z + thing.height / 2, MT_BLOOD)

        mo.momx = (P_Random() - P_Random()) shl 12
        mo.momy = (P_Random() - P_Random()) shl 12
    }

    // keep checking (crush other things)
    return true
}

//
// P_ChangeSector
//
internal fun DoomEngineCore.P_ChangeSector(sector: sector_t, crunch: Boolean): Boolean {
    nofit = false
    crushchange = crunch

    // re-check heights for all things near the moving sector
    for (x in sector.blockbox[BOXLEFT]..sector.blockbox[BOXRIGHT])
        for (y in sector.blockbox[BOXBOTTOM]..sector.blockbox[BOXTOP])
            P_BlockThingsIterator(x, y, { argument0 -> PIT_ChangeSector(argument0) })

    return nofit
}
