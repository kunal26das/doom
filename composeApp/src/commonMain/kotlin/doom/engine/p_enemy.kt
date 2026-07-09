// Port of linuxdoom-1.10 p_enemy.c -- enemy thinking, AI.
// Action Pointer Functions that are associated with states/frames.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import kotlin.math.abs

// dirtype_t
const val DI_EAST = 0
const val DI_NORTHEAST = 1
const val DI_NORTH = 2
const val DI_NORTHWEST = 3
const val DI_WEST = 4
const val DI_SOUTHWEST = 5
const val DI_SOUTH = 6
const val DI_SOUTHEAST = 7
const val DI_NODIR = 8
const val NUMDIRS = 9

//
// P_NewChaseDir related LUT.
//
val opposite = intArrayOf(
    DI_WEST, DI_SOUTHWEST, DI_SOUTH, DI_SOUTHEAST,
    DI_EAST, DI_NORTHEAST, DI_NORTH, DI_NORTHWEST, DI_NODIR
)

val diags = intArrayOf(
    DI_NORTHWEST, DI_NORTHEAST, DI_SOUTHWEST, DI_SOUTHEAST
)

//
// ENEMY THINKING
// Enemies are allways spawned
// with targetplayer = -1, threshold = 0
// Most monsters are spawned unaware of all players,
// but some can be made preaware
//

//
// Called by P_NoiseAlert.
// Recursively traverse adjacent sectors,
// sound blocking lines cut off traversal.
//

var soundtarget: mobj_t? = null

fun P_RecursiveSound(sec: sector_t, soundblocks: Int) {
    var i: Int
    var check: line_t
    var other: sector_t?

    // wake up all monsters in this sector
    if (sec.validcount == validcount
        && sec.soundtraversed <= soundblocks + 1) {
        return  // already flooded
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

        P_LineOpening(check)

        if (openrange <= 0) {
            i++
            continue  // closed door
        }

        if (sides[check.sidenum[0]].sector === sec)
            other = sides[check.sidenum[1]].sector
        else
            other = sides[check.sidenum[0]].sector

        if ((check.flags and ML_SOUNDBLOCK) != 0) {
            if (soundblocks == 0)
                P_RecursiveSound(other!!, 1)
        } else
            P_RecursiveSound(other!!, soundblocks)

        i++
    }
}

//
// P_NoiseAlert
// If a monster yells at a player,
// it will alert other monsters to the player.
//
fun P_NoiseAlert(target: mobj_t, emmiter: mobj_t) {
    soundtarget = target
    validcount++
    P_RecursiveSound(emmiter.subsector!!.sector!!, 0)
}

//
// P_CheckMeleeRange
//
fun P_CheckMeleeRange(actor: mobj_t): Boolean {
    val pl: mobj_t
    val dist: fixed_t

    if (actor.target == null)
        return false

    pl = actor.target!!
    dist = P_AproxDistance(pl.x - actor.x, pl.y - actor.y)

    if (dist >= MELEERANGE - 20 * FRACUNIT + pl.info!!.radius)
        return false

    if (!P_CheckSight(actor, actor.target!!))
        return false

    return true
}

//
// P_CheckMissileRange
//
fun P_CheckMissileRange(actor: mobj_t): Boolean {
    var dist: fixed_t

    if (!P_CheckSight(actor, actor.target!!))
        return false

    if ((actor.flags and MF_JUSTHIT) != 0) {
        // the target just hit the enemy,
        // so fight back!
        actor.flags = actor.flags and MF_JUSTHIT.inv()
        return true
    }

    if (actor.reactiontime != 0)
        return false  // do not attack yet

    // OPTIMIZE: get this from a global checksight
    dist = P_AproxDistance(actor.x - actor.target!!.x,
        actor.y - actor.target!!.y) - 64 * FRACUNIT

    if (actor.info!!.meleestate == 0)
        dist -= 128 * FRACUNIT  // no melee attack, so fire more

    dist = dist shr 16

    if (actor.type == MT_VILE) {
        if (dist > 14 * 64)
            return false  // too far away
    }

    if (actor.type == MT_UNDEAD) {
        if (dist < 196)
            return false  // close for fist attack
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

    if (P_Random() < dist)
        return false

    return true
}

//
// P_Move
// Move in the current direction,
// returns false if the move is blocked.
//
val xspeed = intArrayOf(FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000, 0, 47000)
val yspeed = intArrayOf(0, 47000, FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000)

// (extern line_t* spechit[MAXSPECIALCROSS]; extern int numspechit; -- p_map.kt)

fun P_Move(actor: mobj_t): Boolean {
    val tryx: fixed_t
    val tryy: fixed_t

    var ld: line_t

    // warning: 'catch', 'throw', and 'try'
    // are all C++ reserved words
    val try_ok: Boolean
    var good: Boolean

    if (actor.movedir == DI_NODIR)
        return false

    if (actor.movedir.toUInt() >= 8u)
        I_Error("Weird actor->movedir!")

    tryx = actor.x + actor.info!!.speed * xspeed[actor.movedir]
    tryy = actor.y + actor.info!!.speed * yspeed[actor.movedir]

    try_ok = P_TryMove(actor, tryx, tryy)

    if (!try_ok) {
        // open any specials
        if ((actor.flags and MF_FLOAT) != 0 && floatok) {
            // must adjust height
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
            // (C: while (numspechit--))
            val numspechit_was = numspechit
            numspechit--
            if (numspechit_was == 0)
                break
            ld = spechit[numspechit]!!
            // if the special is not a door
            // that can be opened,
            // return false
            if (P_UseSpecialLine(actor, ld, 0))
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

//
// TryWalk
// Attempts to move actor on
// in its current (ob->moveangle) direction.
// If blocked by either a wall or an actor
// returns FALSE
// If move is either clear or blocked only by a door,
// returns TRUE and sets...
// If a door is in the way,
// an OpenDoor call is made to start it opening.
//
fun P_TryWalk(actor: mobj_t): Boolean {
    if (!P_Move(actor)) {
        return false
    }

    actor.movecount = P_Random() and 15
    return true
}

fun P_NewChaseDir(actor: mobj_t) {
    val deltax: fixed_t
    val deltay: fixed_t

    val d = IntArray(3)

    var tdir: Int
    val olddir: Int

    val turnaround: Int

    if (actor.target == null)
        I_Error("P_NewChaseDir: called with no target")

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

    // try direct route
    if (d[1] != DI_NODIR
        && d[2] != DI_NODIR) {
        actor.movedir = diags[((if (deltay < 0) 1 else 0) shl 1) + (if (deltax > 0) 1 else 0)]
        if (actor.movedir != turnaround && P_TryWalk(actor))
            return
    }

    // try other directions
    if (P_Random() > 200
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
        if (P_TryWalk(actor)) {
            // either moved forward or attacked
            return
        }
    }

    if (d[2] != DI_NODIR) {
        actor.movedir = d[2]

        if (P_TryWalk(actor))
            return
    }

    // there is no direct path to the player,
    // so pick another direction.
    if (olddir != DI_NODIR) {
        actor.movedir = olddir

        if (P_TryWalk(actor))
            return
    }

    // randomly determine direction of search
    if ((P_Random() and 1) != 0) {
        tdir = DI_EAST
        while (tdir <= DI_SOUTHEAST) {
            if (tdir != turnaround) {
                actor.movedir = tdir

                if (P_TryWalk(actor))
                    return
            }
            tdir++
        }
    } else {
        tdir = DI_SOUTHEAST
        while (tdir != (DI_EAST - 1)) {
            if (tdir != turnaround) {
                actor.movedir = tdir

                if (P_TryWalk(actor))
                    return
            }
            tdir--
        }
    }

    if (turnaround != DI_NODIR) {
        actor.movedir = turnaround
        if (P_TryWalk(actor))
            return
    }

    actor.movedir = DI_NODIR  // can not move
}

//
// P_LookForPlayers
// If allaround is false, only look 180 degrees in front.
// Returns true if a player is targeted.
//
fun P_LookForPlayers(actor: mobj_t, allaround: Boolean): Boolean {
    var c: Int
    val stop: Int
    val sector: sector_t

    sector = actor.subsector!!.sector!!

    c = 0
    stop = (actor.lastlook - 1) and 3

    while (true) {
        // (C: for ( ; ; actor->lastlook = (actor->lastlook+1)&3 ), continue
        //  runs the increment; return@run below == continue)
        run {
            if (!playeringame[actor.lastlook])
                return@run

            // (C: if (c++ == 2 || actor->lastlook == stop))
            val c_was = c
            c++
            if (c_was == 2
                || actor.lastlook == stop) {
                // done looking
                return false
            }

            val player = players[actor.lastlook]

            if (player.health <= 0)
                return@run  // dead

            if (!P_CheckSight(actor, player.mo!!))
                return@run  // out of sight

            if (!allaround) {
                val an: angle_t = R_PointToAngle2(actor.x,
                    actor.y,
                    player.mo!!.x,
                    player.mo!!.y) - actor.angle

                if (an > ANG90 && an < ANG270) {
                    val dist: fixed_t = P_AproxDistance(player.mo!!.x - actor.x,
                        player.mo!!.y - actor.y)
                    // if real close, react anyway
                    if (dist > MELEERANGE)
                        return@run  // behind back
                }
            }

            actor.target = player.mo
            return true
        }
        actor.lastlook = (actor.lastlook + 1) and 3
    }

    // (C: return false; -- unreachable)
}

//
// A_KeenDie
// DOOM II special, map 32.
// Uses special tag 666.
//
fun A_KeenDie(mo: mobj_t) {
    var th: thinker_t
    val junk = line_t()

    A_Fall(mo)

    // scan the remaining thinkers
    // to see if all Keens are dead
    th = thinkercap.next!!
    while (th !== thinkercap) {
        // (C: if (th->function.acp1 != (actionf_p1)P_MobjThinker) continue;)
        if (th is mobj_t && !th.removed) {
            val mo2: mobj_t = th
            if (mo2 !== mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                // other Keen not dead
                return
            }
        }
        th = th.next!!
    }

    junk.tag = 666
    EV_DoDoor(junk, open)
}

//
// ACTION ROUTINES
//

//
// A_Look
// Stay in state until a player is sighted.
//
fun A_Look(actor: mobj_t) {
    val targ: mobj_t?

    actor.threshold = 0  // any shot will wake up
    targ = actor.subsector!!.sector!!.soundtarget

    var seeyou = false  // (C: goto seeyou)
    if (targ != null
        && (targ.flags and MF_SHOOTABLE) != 0) {
        actor.target = targ

        if ((actor.flags and MF_AMBUSH) != 0) {
            if (P_CheckSight(actor, actor.target!!))
                seeyou = true
        } else
            seeyou = true
    }

    if (!seeyou) {
        if (!P_LookForPlayers(actor, false))
            return
    }

    // go into chase state
    // seeyou:
    if (actor.info!!.seesound != 0) {
        val sound: Int

        when (actor.info!!.seesound) {
            sfx_posit1, sfx_posit2, sfx_posit3 ->
                sound = sfx_posit1 + P_Random() % 3

            sfx_bgsit1, sfx_bgsit2 ->
                sound = sfx_bgsit1 + P_Random() % 2

            else ->
                sound = actor.info!!.seesound
        }

        if (actor.type == MT_SPIDER
            || actor.type == MT_CYBORG) {
            // full volume
            S_StartSound(null, sound)
        } else
            S_StartSound(actor, sound)
    }

    P_SetMobjState(actor, actor.info!!.seestate)
}

//
// A_Chase
// Actor has a melee attack,
// so it tries to close as fast as possible
//
fun A_Chase(actor: mobj_t) {
    val delta: Int

    if (actor.reactiontime != 0)
        actor.reactiontime--

    // modify target threshold
    if (actor.threshold != 0) {
        if (actor.target == null
            || actor.target!!.health <= 0) {
            actor.threshold = 0
        } else
            actor.threshold--
    }

    // turn towards movement direction if not there yet
    if (actor.movedir < 8) {
        actor.angle = actor.angle and (7 shl 29).toUInt()
        // (C: int delta = actor->angle - (actor->movedir << 29))
        delta = actor.angle.toInt() - (actor.movedir shl 29)

        if (delta > 0)
            actor.angle -= ANG90 / 2u
        else if (delta < 0)
            actor.angle += ANG90 / 2u
    }

    if (actor.target == null
        || (actor.target!!.flags and MF_SHOOTABLE) == 0) {
        // look for a new target
        if (P_LookForPlayers(actor, true))
            return  // got a new target

        P_SetMobjState(actor, actor.info!!.spawnstate)
        return
    }

    // do not attack twice in a row
    if ((actor.flags and MF_JUSTATTACKED) != 0) {
        actor.flags = actor.flags and MF_JUSTATTACKED.inv()
        if (gameskill != sk_nightmare && !fastparm)
            P_NewChaseDir(actor)
        return
    }

    // check for melee attack
    if (actor.info!!.meleestate != 0
        && P_CheckMeleeRange(actor)) {
        if (actor.info!!.attacksound != 0)
            S_StartSound(actor, actor.info!!.attacksound)

        P_SetMobjState(actor, actor.info!!.meleestate)
        return
    }

    // check for missile attack
    if (actor.info!!.missilestate != 0) {
        var nomissile = false  // (C: goto nomissile)
        if (gameskill < sk_nightmare
            && !fastparm && actor.movecount != 0) {
            nomissile = true
        }

        if (!nomissile && !P_CheckMissileRange(actor))
            nomissile = true

        if (!nomissile) {
            P_SetMobjState(actor, actor.info!!.missilestate)
            actor.flags = actor.flags or MF_JUSTATTACKED
            return
        }
    }

    // ?
    // nomissile:
    // possibly choose another target
    if (netgame
        && actor.threshold == 0
        && !P_CheckSight(actor, actor.target!!)) {
        if (P_LookForPlayers(actor, true))
            return  // got a new target
    }

    // chase towards player
    actor.movecount--
    if (actor.movecount < 0
        || !P_Move(actor)) {
        P_NewChaseDir(actor)
    }

    // make active sound
    if (actor.info!!.activesound != 0
        && P_Random() < 3) {
        S_StartSound(actor, actor.info!!.activesound)
    }
}

//
// A_FaceTarget
//
fun A_FaceTarget(actor: mobj_t) {
    if (actor.target == null)
        return

    actor.flags = actor.flags and MF_AMBUSH.inv()

    actor.angle = R_PointToAngle2(actor.x,
        actor.y,
        actor.target!!.x,
        actor.target!!.y)

    if ((actor.target!!.flags and MF_SHADOW) != 0)
        actor.angle += ((P_Random() - P_Random()) shl 21).toUInt()
}

//
// A_PosAttack
//
fun A_PosAttack(actor: mobj_t) {
    var angle: Int
    val damage: Int
    val slope: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)
    angle = actor.angle.toInt()  // (C: int angle = actor->angle)
    slope = P_AimLineAttack(actor, angle.toUInt(), MISSILERANGE)

    S_StartSound(actor, sfx_pistol)
    angle += (P_Random() - P_Random()) shl 20
    damage = ((P_Random() % 5) + 1) * 3
    P_LineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
}

fun A_SPosAttack(actor: mobj_t) {
    var i: Int
    var angle: Int
    val bangle: Int
    var damage: Int
    val slope: Int

    if (actor.target == null)
        return

    S_StartSound(actor, sfx_shotgn)
    A_FaceTarget(actor)
    bangle = actor.angle.toInt()
    slope = P_AimLineAttack(actor, bangle.toUInt(), MISSILERANGE)

    i = 0
    while (i < 3) {
        angle = bangle + ((P_Random() - P_Random()) shl 20)
        damage = ((P_Random() % 5) + 1) * 3
        P_LineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
        i++
    }
}

fun A_CPosAttack(actor: mobj_t) {
    val angle: Int
    val bangle: Int
    val damage: Int
    val slope: Int

    if (actor.target == null)
        return

    S_StartSound(actor, sfx_shotgn)
    A_FaceTarget(actor)
    bangle = actor.angle.toInt()
    slope = P_AimLineAttack(actor, bangle.toUInt(), MISSILERANGE)

    angle = bangle + ((P_Random() - P_Random()) shl 20)
    damage = ((P_Random() % 5) + 1) * 3
    P_LineAttack(actor, angle.toUInt(), MISSILERANGE, slope, damage)
}

fun A_CPosRefire(actor: mobj_t) {
    // keep firing unless target got out of sight
    A_FaceTarget(actor)

    if (P_Random() < 40)
        return

    if (actor.target == null
        || actor.target!!.health <= 0
        || !P_CheckSight(actor, actor.target!!)) {
        P_SetMobjState(actor, actor.info!!.seestate)
    }
}

fun A_SpidRefire(actor: mobj_t) {
    // keep firing unless target got out of sight
    A_FaceTarget(actor)

    if (P_Random() < 10)
        return

    if (actor.target == null
        || actor.target!!.health <= 0
        || !P_CheckSight(actor, actor.target!!)) {
        P_SetMobjState(actor, actor.info!!.seestate)
    }
}

fun A_BspiAttack(actor: mobj_t) {
    if (actor.target == null)
        return

    A_FaceTarget(actor)

    // launch a missile
    P_SpawnMissile(actor, actor.target!!, MT_ARACHPLAZ)
}

//
// A_TroopAttack
//
fun A_TroopAttack(actor: mobj_t) {
    val damage: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)
    if (P_CheckMeleeRange(actor)) {
        S_StartSound(actor, sfx_claw)
        damage = (P_Random() % 8 + 1) * 3
        P_DamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    // launch a missile
    P_SpawnMissile(actor, actor.target!!, MT_TROOPSHOT)
}

fun A_SargAttack(actor: mobj_t) {
    val damage: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)
    if (P_CheckMeleeRange(actor)) {
        damage = ((P_Random() % 10) + 1) * 4
        P_DamageMobj(actor.target!!, actor, actor, damage)
    }
}

fun A_HeadAttack(actor: mobj_t) {
    val damage: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)
    if (P_CheckMeleeRange(actor)) {
        damage = (P_Random() % 6 + 1) * 10
        P_DamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    // launch a missile
    P_SpawnMissile(actor, actor.target!!, MT_HEADSHOT)
}

fun A_CyberAttack(actor: mobj_t) {
    if (actor.target == null)
        return

    A_FaceTarget(actor)
    P_SpawnMissile(actor, actor.target!!, MT_ROCKET)
}

fun A_BruisAttack(actor: mobj_t) {
    val damage: Int

    if (actor.target == null)
        return

    if (P_CheckMeleeRange(actor)) {
        S_StartSound(actor, sfx_claw)
        damage = (P_Random() % 8 + 1) * 10
        P_DamageMobj(actor.target!!, actor, actor, damage)
        return
    }

    // launch a missile
    P_SpawnMissile(actor, actor.target!!, MT_BRUISERSHOT)
}

//
// A_SkelMissile
//
fun A_SkelMissile(actor: mobj_t) {
    val mo: mobj_t

    if (actor.target == null)
        return

    A_FaceTarget(actor)
    actor.z += 16 * FRACUNIT  // so missile spawns higher
    mo = P_SpawnMissile(actor, actor.target!!, MT_TRACER)
    actor.z -= 16 * FRACUNIT  // back to normal

    mo.x += mo.momx
    mo.y += mo.momy
    mo.tracer = actor.target
}

var TRACEANGLE = 0xc000000

fun A_Tracer(actor: mobj_t) {
    var exact: angle_t
    var dist: fixed_t
    val slope: fixed_t
    val dest: mobj_t?
    val th: mobj_t

    if ((gametic and 3) != 0)
        return

    // spawn a puff of smoke behind the rocket
    P_SpawnPuff(actor.x, actor.y, actor.z)

    th = P_SpawnMobj(actor.x - actor.momx,
        actor.y - actor.momy,
        actor.z, MT_SMOKE)

    th.momz = FRACUNIT
    th.tics -= P_Random() and 3
    if (th.tics < 1)
        th.tics = 1

    // adjust direction
    dest = actor.tracer

    if (dest == null || dest.health <= 0)
        return

    // change angle
    exact = R_PointToAngle2(actor.x,
        actor.y,
        dest.x,
        dest.y)

    if (exact != actor.angle) {
        if (exact - actor.angle > 0x80000000u) {
            actor.angle -= TRACEANGLE.toUInt()
            if (exact - actor.angle < 0x80000000u)
                actor.angle = exact
        } else {
            actor.angle += TRACEANGLE.toUInt()
            if (exact - actor.angle > 0x80000000u)
                actor.angle = exact
        }
    }

    exact = actor.angle shr ANGLETOFINESHIFT
    actor.momx = FixedMul(actor.info!!.speed, finecosine[exact.toInt()])
    actor.momy = FixedMul(actor.info!!.speed, finesine[exact.toInt()])

    // change slope
    dist = P_AproxDistance(dest.x - actor.x,
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

fun A_SkelWhoosh(actor: mobj_t) {
    if (actor.target == null)
        return
    A_FaceTarget(actor)
    S_StartSound(actor, sfx_skeswg)
}

fun A_SkelFist(actor: mobj_t) {
    val damage: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)

    if (P_CheckMeleeRange(actor)) {
        damage = ((P_Random() % 10) + 1) * 6
        S_StartSound(actor, sfx_skepch)
        P_DamageMobj(actor.target!!, actor, actor, damage)
    }
}

//
// PIT_VileCheck
// Detect a corpse that could be raised.
//
var corpsehit: mobj_t? = null
var vileobj: mobj_t? = null
var viletryx: fixed_t = 0
var viletryy: fixed_t = 0

fun PIT_VileCheck(thing: mobj_t): Boolean {
    val maxdist: Int
    val check: Boolean

    if ((thing.flags and MF_CORPSE) == 0)
        return true  // not a monster

    if (thing.tics != -1)
        return true  // not lying still yet

    if (thing.info!!.raisestate == S_NULL)
        return true  // monster doesn't have a raise state

    maxdist = thing.info!!.radius + mobjinfo[MT_VILE].radius

    if (abs(thing.x - viletryx) > maxdist
        || abs(thing.y - viletryy) > maxdist)
        return true  // not actually touching

    corpsehit = thing
    corpsehit!!.momx = 0
    corpsehit!!.momy = 0
    corpsehit!!.height = corpsehit!!.height shl 2
    check = P_CheckPosition(corpsehit!!, corpsehit!!.x, corpsehit!!.y)
    corpsehit!!.height = corpsehit!!.height shr 2

    if (!check)
        return true  // doesn't fit here

    return false  // got one, so stop checking
}

//
// A_VileChase
// Check for ressurecting a body
//
fun A_VileChase(actor: mobj_t) {
    val xl: Int
    val xh: Int
    val yl: Int
    val yh: Int

    var bx: Int
    var by: Int

    // (C: mobjinfo_t* info; mobj_t* temp; -- declared at first assignment below)

    if (actor.movedir != DI_NODIR) {
        // check for corpses to raise
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
                // Call PIT_VileCheck to check
                // whether object is a corpse
                // that canbe raised.
                if (!P_BlockThingsIterator(bx, by, ::PIT_VileCheck)) {
                    // got one!
                    val temp: mobj_t? = actor.target
                    actor.target = corpsehit
                    A_FaceTarget(actor)
                    actor.target = temp

                    P_SetMobjState(actor, S_VILE_HEAL1)
                    S_StartSound(corpsehit, sfx_slop)
                    val info: mobjinfo_t = corpsehit!!.info!!

                    P_SetMobjState(corpsehit!!, info.raisestate)
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

    // Return to normal attack.
    A_Chase(actor)
}

//
// A_VileStart
//
fun A_VileStart(actor: mobj_t) {
    S_StartSound(actor, sfx_vilatk)
}

//
// A_Fire
// Keep fire in front of player unless out of sight
//

fun A_StartFire(actor: mobj_t) {
    S_StartSound(actor, sfx_flamst)
    A_Fire(actor)
}

fun A_FireCrackle(actor: mobj_t) {
    S_StartSound(actor, sfx_flame)
    A_Fire(actor)
}

fun A_Fire(actor: mobj_t) {
    val dest: mobj_t?
    val an: Int  // (C: unsigned an)

    dest = actor.tracer
    if (dest == null)
        return

    // don't move it if the vile lost sight
    if (!P_CheckSight(actor.target!!, dest))
        return

    an = (dest.angle shr ANGLETOFINESHIFT).toInt()

    P_UnsetThingPosition(actor)
    actor.x = dest.x + FixedMul(24 * FRACUNIT, finecosine[an])
    actor.y = dest.y + FixedMul(24 * FRACUNIT, finesine[an])
    actor.z = dest.z
    P_SetThingPosition(actor)
}

//
// A_VileTarget
// Spawn the hellfire
//
fun A_VileTarget(actor: mobj_t) {
    val fog: mobj_t

    if (actor.target == null)
        return

    A_FaceTarget(actor)

    // (C: passes target->x twice -- vanilla bug, kept)
    fog = P_SpawnMobj(actor.target!!.x,
        actor.target!!.x,
        actor.target!!.z, MT_FIRE)

    actor.tracer = fog
    fog.target = actor
    fog.tracer = actor.target
    A_Fire(fog)
}

//
// A_VileAttack
//
fun A_VileAttack(actor: mobj_t) {
    val fire: mobj_t?
    val an: Int

    if (actor.target == null)
        return

    A_FaceTarget(actor)

    if (!P_CheckSight(actor, actor.target!!))
        return

    S_StartSound(actor, sfx_barexp)
    P_DamageMobj(actor.target!!, actor, actor, 20)
    actor.target!!.momz = 1000 * FRACUNIT / actor.target!!.info!!.mass

    an = (actor.angle shr ANGLETOFINESHIFT).toInt()

    fire = actor.tracer

    if (fire == null)
        return

    // move the fire between the vile and the player
    fire.x = actor.target!!.x - FixedMul(24 * FRACUNIT, finecosine[an])
    fire.y = actor.target!!.y - FixedMul(24 * FRACUNIT, finesine[an])
    P_RadiusAttack(fire, actor, 70)
}

//
// Mancubus attack,
// firing three missiles (bruisers)
// in three different directions?
// Doesn't look like it.
//
val FATSPREAD: angle_t = ANG90 / 8u

fun A_FatRaise(actor: mobj_t) {
    A_FaceTarget(actor)
    S_StartSound(actor, sfx_manatk)
}

fun A_FatAttack1(actor: mobj_t) {
    val mo: mobj_t
    val an: Int

    A_FaceTarget(actor)
    // Change direction  to ...
    actor.angle += FATSPREAD
    P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)

    mo = P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle += FATSPREAD
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = FixedMul(mo.info!!.speed, finecosine[an])
    mo.momy = FixedMul(mo.info!!.speed, finesine[an])
}

fun A_FatAttack2(actor: mobj_t) {
    val mo: mobj_t
    val an: Int

    A_FaceTarget(actor)
    // Now here choose opposite deviation.
    actor.angle -= FATSPREAD
    P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)

    mo = P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle -= FATSPREAD * 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = FixedMul(mo.info!!.speed, finecosine[an])
    mo.momy = FixedMul(mo.info!!.speed, finesine[an])
}

fun A_FatAttack3(actor: mobj_t) {
    var mo: mobj_t
    var an: Int

    A_FaceTarget(actor)

    mo = P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle -= FATSPREAD / 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = FixedMul(mo.info!!.speed, finecosine[an])
    mo.momy = FixedMul(mo.info!!.speed, finesine[an])

    mo = P_SpawnMissile(actor, actor.target!!, MT_FATSHOT)
    mo.angle += FATSPREAD / 2u
    an = (mo.angle shr ANGLETOFINESHIFT).toInt()
    mo.momx = FixedMul(mo.info!!.speed, finecosine[an])
    mo.momy = FixedMul(mo.info!!.speed, finesine[an])
}

//
// SkullAttack
// Fly at the player like a missile.
//
const val SKULLSPEED = 20 * FRACUNIT

fun A_SkullAttack(actor: mobj_t) {
    val dest: mobj_t
    val an: angle_t
    var dist: Int

    if (actor.target == null)
        return

    dest = actor.target!!
    actor.flags = actor.flags or MF_SKULLFLY

    S_StartSound(actor, actor.info!!.attacksound)
    A_FaceTarget(actor)
    an = actor.angle shr ANGLETOFINESHIFT
    actor.momx = FixedMul(SKULLSPEED, finecosine[an.toInt()])
    actor.momy = FixedMul(SKULLSPEED, finesine[an.toInt()])
    dist = P_AproxDistance(dest.x - actor.x, dest.y - actor.y)
    dist = dist / SKULLSPEED

    if (dist < 1)
        dist = 1
    actor.momz = (dest.z + (dest.height shr 1) - actor.z) / dist
}

//
// A_PainShootSkull
// Spawn a lost soul and launch it at the target
//
fun A_PainShootSkull(actor: mobj_t, angle: angle_t) {
    val x: fixed_t
    val y: fixed_t
    val z: fixed_t

    val newmobj: mobj_t
    val an: angle_t
    val prestep: Int
    var count: Int
    var currentthinker: thinker_t

    // count total number of skull currently on the level
    count = 0

    currentthinker = thinkercap.next!!
    while (currentthinker !== thinkercap) {
        if (currentthinker is mobj_t && !currentthinker.removed
            && currentthinker.type == MT_SKULL)
            count++
        currentthinker = currentthinker.next!!
    }

    // if there are allready 20 skulls on the level,
    // don't spit another one
    if (count > 20)
        return

    // okay, there's playe for another one
    an = angle shr ANGLETOFINESHIFT

    prestep =
        4 * FRACUNIT + 3 * (actor.info!!.radius + mobjinfo[MT_SKULL].radius) / 2

    x = actor.x + FixedMul(prestep, finecosine[an.toInt()])
    y = actor.y + FixedMul(prestep, finesine[an.toInt()])
    z = actor.z + 8 * FRACUNIT

    newmobj = P_SpawnMobj(x, y, z, MT_SKULL)

    // Check for movements.
    if (!P_TryMove(newmobj, newmobj.x, newmobj.y)) {
        // kill it immediately
        P_DamageMobj(newmobj, actor, actor, 10000)
        return
    }

    newmobj.target = actor.target
    A_SkullAttack(newmobj)
}

//
// A_PainAttack
// Spawn a lost soul and launch it at the target
//
fun A_PainAttack(actor: mobj_t) {
    if (actor.target == null)
        return

    A_FaceTarget(actor)
    A_PainShootSkull(actor, actor.angle)
}

fun A_PainDie(actor: mobj_t) {
    A_Fall(actor)
    A_PainShootSkull(actor, actor.angle + ANG90)
    A_PainShootSkull(actor, actor.angle + ANG180)
    A_PainShootSkull(actor, actor.angle + ANG270)
}

fun A_Scream(actor: mobj_t) {
    val sound: Int

    when (actor.info!!.deathsound) {
        0 ->
            return

        sfx_podth1, sfx_podth2, sfx_podth3 ->
            sound = sfx_podth1 + P_Random() % 3

        sfx_bgdth1, sfx_bgdth2 ->
            sound = sfx_bgdth1 + P_Random() % 2

        else ->
            sound = actor.info!!.deathsound
    }

    // Check for bosses.
    if (actor.type == MT_SPIDER
        || actor.type == MT_CYBORG) {
        // full volume
        S_StartSound(null, sound)
    } else
        S_StartSound(actor, sound)
}

fun A_XScream(actor: mobj_t) {
    S_StartSound(actor, sfx_slop)
}

fun A_Pain(actor: mobj_t) {
    if (actor.info!!.painsound != 0)
        S_StartSound(actor, actor.info!!.painsound)
}

fun A_Fall(actor: mobj_t) {
    // actor is on ground, it can be walked over
    actor.flags = actor.flags and MF_SOLID.inv()

    // So change this if corpse objects
    // are meant to be obstacles.
}

//
// A_Explode
//
fun A_Explode(thingy: mobj_t) {
    P_RadiusAttack(thingy, thingy.target, 128)
}

//
// A_BossDeath
// Possibly trigger special effects
// if on first boss level
//
fun A_BossDeath(mo: mobj_t) {
    var th: thinker_t
    val junk = line_t()
    var i: Int

    if (gamemode == commercial) {
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

    // make sure there is a player alive for victory
    i = 0
    while (i < MAXPLAYERS) {
        if (playeringame[i] && players[i].health > 0)
            break
        i++
    }

    if (i == MAXPLAYERS)
        return  // no one left alive, so do not end game

    // scan the remaining thinkers to see
    // if all bosses are dead
    th = thinkercap.next!!
    while (th !== thinkercap) {
        // (C: if (th->function.acp1 != (actionf_p1)P_MobjThinker) continue;)
        if (th is mobj_t && !th.removed) {
            val mo2: mobj_t = th
            if (mo2 !== mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                // other boss not dead
                return
            }
        }
        th = th.next!!
    }

    // victory!
    if (gamemode == commercial) {
        if (gamemap == 7) {
            if (mo.type == MT_FATSO) {
                junk.tag = 666
                EV_DoFloor(junk, lowerFloorToLowest)
                return
            }

            if (mo.type == MT_BABY) {
                junk.tag = 667
                EV_DoFloor(junk, raiseToTexture)
                return
            }
        }
    } else {
        when (gameepisode) {
            1 -> {
                junk.tag = 666
                EV_DoFloor(junk, lowerFloorToLowest)
                return
            }

            4 -> {
                when (gamemap) {
                    6 -> {
                        junk.tag = 666
                        EV_DoDoor(junk, blazeOpen)
                        return
                    }

                    8 -> {
                        junk.tag = 666
                        EV_DoFloor(junk, lowerFloorToLowest)
                        return
                    }
                }
            }
        }
    }

    G_ExitLevel()
}

fun A_Hoof(mo: mobj_t) {
    S_StartSound(mo, sfx_hoof)
    A_Chase(mo)
}

fun A_Metal(mo: mobj_t) {
    S_StartSound(mo, sfx_metal)
    A_Chase(mo)
}

fun A_BabyMetal(mo: mobj_t) {
    S_StartSound(mo, sfx_bspwlk)
    A_Chase(mo)
}

fun A_OpenShotgun2(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_dbopn)
}

fun A_LoadShotgun2(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_dbload)
}

fun A_CloseShotgun2(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_dbcls)
    A_ReFire(player, psp)
}

val braintargets = arrayOfNulls<mobj_t>(32)
var numbraintargets = 0
var braintargeton = 0

fun A_BrainAwake(mo: mobj_t) {
    var thinker: thinker_t

    // find all the target spots
    numbraintargets = 0
    braintargeton = 0

    thinker = thinkercap.next!!
    while (thinker !== thinkercap) {
        // (C: if (thinker->function.acp1 != (actionf_p1)P_MobjThinker)
        //  continue; // not a mobj)
        if (thinker is mobj_t && !thinker.removed) {
            val m: mobj_t = thinker

            if (m.type == MT_BOSSTARGET) {
                braintargets[numbraintargets] = m
                numbraintargets++
            }
        }
        thinker = thinker.next!!
    }

    S_StartSound(null, sfx_bossit)
}

fun A_BrainPain(mo: mobj_t) {
    S_StartSound(null, sfx_bospn)
}

fun A_BrainScream(mo: mobj_t) {
    var x: Int
    var y: Int
    var z: Int
    var th: mobj_t

    x = mo.x - 196 * FRACUNIT
    while (x < mo.x + 320 * FRACUNIT) {
        y = mo.y - 320 * FRACUNIT
        z = 128 + P_Random() * 2 * FRACUNIT
        th = P_SpawnMobj(x, y, z, MT_ROCKET)
        th.momz = P_Random() * 512

        P_SetMobjState(th, S_BRAINEXPLODE1)

        th.tics -= P_Random() and 7
        if (th.tics < 1)
            th.tics = 1

        x += FRACUNIT * 8
    }

    S_StartSound(null, sfx_bosdth)
}

fun A_BrainExplode(mo: mobj_t) {
    val x: Int
    val y: Int
    val z: Int
    val th: mobj_t

    x = mo.x + (P_Random() - P_Random()) * 2048
    y = mo.y
    z = 128 + P_Random() * 2 * FRACUNIT
    th = P_SpawnMobj(x, y, z, MT_ROCKET)
    th.momz = P_Random() * 512

    P_SetMobjState(th, S_BRAINEXPLODE1)

    th.tics -= P_Random() and 7
    if (th.tics < 1)
        th.tics = 1
}

fun A_BrainDie(mo: mobj_t) {
    G_ExitLevel()
}

private var easy = 0  // (C: static int easy = 0 in A_BrainSpit)

fun A_BrainSpit(mo: mobj_t) {
    val targ: mobj_t
    val newmobj: mobj_t

    easy = easy xor 1
    if (gameskill <= sk_easy && (easy == 0))
        return

    // shoot a cube at current target
    targ = braintargets[braintargeton]!!
    braintargeton = (braintargeton + 1) % numbraintargets

    // spawn brain missile
    newmobj = P_SpawnMissile(mo, targ, MT_SPAWNSHOT)
    newmobj.target = targ
    newmobj.reactiontime =
        ((targ.y - mo.y) / newmobj.momy) / newmobj.state!!.tics

    S_StartSound(null, sfx_bospit)
}

// travelling cube sound
fun A_SpawnSound(mo: mobj_t) {
    S_StartSound(mo, sfx_boscub)
    A_SpawnFly(mo)
}

fun A_SpawnFly(mo: mobj_t) {
    val newmobj: mobj_t
    val fog: mobj_t
    val targ: mobj_t
    val r: Int
    val type: Int  // mobjtype_t

    // (C: if (--mo->reactiontime) return;)
    mo.reactiontime--
    if (mo.reactiontime != 0)
        return  // still flying

    targ = mo.target!!

    // First spawn teleport fog.
    fog = P_SpawnMobj(targ.x, targ.y, targ.z, MT_SPAWNFIRE)
    S_StartSound(fog, sfx_telept)

    // Randomly select monster to spawn.
    r = P_Random()

    // Probability distribution (kind of :),
    // decreasing likelihood.
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

    newmobj = P_SpawnMobj(targ.x, targ.y, targ.z, type)
    if (P_LookForPlayers(newmobj, true))
        P_SetMobjState(newmobj, newmobj.info!!.seestate)

    // telefrag anything in this spot
    P_TeleportMove(newmobj, newmobj.x, newmobj.y)

    // remove self (i.e., cube).
    P_RemoveMobj(mo)
}

fun A_PlayerScream(mo: mobj_t) {
    // Default death sound.
    var sound = sfx_pldeth

    if ((gamemode == commercial)
        && (mo.health < -50)) {
        // IF THE PLAYER DIES
        // LESS THAN -50% WITHOUT GIBBING
        sound = sfx_pdiehi
    }

    S_StartSound(mo, sound)
}

//
// Register every action function this file defines in the actionMap
// (called by D_DoomMain before InfoResolveActions).
//
fun P_RegisterEnemyActions() {
    registerAction(ActionF("A_KeenDie", mobjFun = { A_KeenDie(it) }))
    registerAction(ActionF("A_Look", mobjFun = { A_Look(it) }))
    registerAction(ActionF("A_Chase", mobjFun = { A_Chase(it) }))
    registerAction(ActionF("A_FaceTarget", mobjFun = { A_FaceTarget(it) }))
    registerAction(ActionF("A_PosAttack", mobjFun = { A_PosAttack(it) }))
    registerAction(ActionF("A_SPosAttack", mobjFun = { A_SPosAttack(it) }))
    registerAction(ActionF("A_CPosAttack", mobjFun = { A_CPosAttack(it) }))
    registerAction(ActionF("A_CPosRefire", mobjFun = { A_CPosRefire(it) }))
    registerAction(ActionF("A_SpidRefire", mobjFun = { A_SpidRefire(it) }))
    registerAction(ActionF("A_BspiAttack", mobjFun = { A_BspiAttack(it) }))
    registerAction(ActionF("A_TroopAttack", mobjFun = { A_TroopAttack(it) }))
    registerAction(ActionF("A_SargAttack", mobjFun = { A_SargAttack(it) }))
    registerAction(ActionF("A_HeadAttack", mobjFun = { A_HeadAttack(it) }))
    registerAction(ActionF("A_CyberAttack", mobjFun = { A_CyberAttack(it) }))
    registerAction(ActionF("A_BruisAttack", mobjFun = { A_BruisAttack(it) }))
    registerAction(ActionF("A_SkelMissile", mobjFun = { A_SkelMissile(it) }))
    registerAction(ActionF("A_Tracer", mobjFun = { A_Tracer(it) }))
    registerAction(ActionF("A_SkelWhoosh", mobjFun = { A_SkelWhoosh(it) }))
    registerAction(ActionF("A_SkelFist", mobjFun = { A_SkelFist(it) }))
    registerAction(ActionF("A_VileChase", mobjFun = { A_VileChase(it) }))
    registerAction(ActionF("A_VileStart", mobjFun = { A_VileStart(it) }))
    registerAction(ActionF("A_StartFire", mobjFun = { A_StartFire(it) }))
    registerAction(ActionF("A_FireCrackle", mobjFun = { A_FireCrackle(it) }))
    registerAction(ActionF("A_Fire", mobjFun = { A_Fire(it) }))
    registerAction(ActionF("A_VileTarget", mobjFun = { A_VileTarget(it) }))
    registerAction(ActionF("A_VileAttack", mobjFun = { A_VileAttack(it) }))
    registerAction(ActionF("A_FatRaise", mobjFun = { A_FatRaise(it) }))
    registerAction(ActionF("A_FatAttack1", mobjFun = { A_FatAttack1(it) }))
    registerAction(ActionF("A_FatAttack2", mobjFun = { A_FatAttack2(it) }))
    registerAction(ActionF("A_FatAttack3", mobjFun = { A_FatAttack3(it) }))
    registerAction(ActionF("A_SkullAttack", mobjFun = { A_SkullAttack(it) }))
    registerAction(ActionF("A_PainAttack", mobjFun = { A_PainAttack(it) }))
    registerAction(ActionF("A_PainDie", mobjFun = { A_PainDie(it) }))
    registerAction(ActionF("A_Scream", mobjFun = { A_Scream(it) }))
    registerAction(ActionF("A_XScream", mobjFun = { A_XScream(it) }))
    registerAction(ActionF("A_Pain", mobjFun = { A_Pain(it) }))
    registerAction(ActionF("A_Fall", mobjFun = { A_Fall(it) }))
    registerAction(ActionF("A_Explode", mobjFun = { A_Explode(it) }))
    registerAction(ActionF("A_BossDeath", mobjFun = { A_BossDeath(it) }))
    registerAction(ActionF("A_Hoof", mobjFun = { A_Hoof(it) }))
    registerAction(ActionF("A_Metal", mobjFun = { A_Metal(it) }))
    registerAction(ActionF("A_BabyMetal", mobjFun = { A_BabyMetal(it) }))
    registerAction(ActionF("A_OpenShotgun2", pspFun = { player, psp -> A_OpenShotgun2(player, psp) }))
    registerAction(ActionF("A_LoadShotgun2", pspFun = { player, psp -> A_LoadShotgun2(player, psp) }))
    registerAction(ActionF("A_CloseShotgun2", pspFun = { player, psp -> A_CloseShotgun2(player, psp) }))
    registerAction(ActionF("A_BrainAwake", mobjFun = { A_BrainAwake(it) }))
    registerAction(ActionF("A_BrainPain", mobjFun = { A_BrainPain(it) }))
    registerAction(ActionF("A_BrainScream", mobjFun = { A_BrainScream(it) }))
    registerAction(ActionF("A_BrainExplode", mobjFun = { A_BrainExplode(it) }))
    registerAction(ActionF("A_BrainDie", mobjFun = { A_BrainDie(it) }))
    registerAction(ActionF("A_BrainSpit", mobjFun = { A_BrainSpit(it) }))
    registerAction(ActionF("A_SpawnSound", mobjFun = { A_SpawnSound(it) }))
    registerAction(ActionF("A_SpawnFly", mobjFun = { A_SpawnFly(it) }))
    registerAction(ActionF("A_PlayerScream", mobjFun = { A_PlayerScream(it) }))
}
