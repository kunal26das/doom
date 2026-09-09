// Port of linuxdoom-1.10 p_mobj.c -- moving object handling. Spawn functions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.geometry.FixedGeometry

internal var DoomEngineCore.test
    get() = stateActorSpawn.test
    set(value) { stateActorSpawn.test = value }

internal fun DoomEngineCore.P_SetMobjState(mobj: mobj_t, state: Int): Boolean {
    var state = state
    var st: state_t

    do {
        if (state == S_NULL) {
            mobj.state = null  // (state_t*) S_NULL
            P_RemoveMobj(mobj)
            return false
        }

        st = states[state]
        mobj.state = st
        mobj.tics = st.tics
        mobj.sprite = st.sprite
        mobj.frame = st.frame

        // Modified handling.
        // Call action functions when the state is set
        st.action?.mobjFun?.invoke(mobj)

        state = st.nextstate
    } while (mobj.tics == 0)

    return true
}

//
// P_ExplodeMissile
//
internal fun DoomEngineCore.P_ExplodeMissile(mo: mobj_t) {
    mo.momx = 0
    mo.momy = 0
    mo.momz = 0

    P_SetMobjState(mo, mobjinfo[mo.type].deathstate)

    mo.tics -= P_Random() and 3

    if (mo.tics < 1)
        mo.tics = 1

    mo.flags = mo.flags and MF_MISSILE.inv()

    if (mo.info!!.deathsound != 0)
        S_StartSound(mo, mo.info!!.deathsound)
}

//
// P_XYMovement
//
internal const val STOPSPEED = 0x1000
internal const val FRICTION = 0xe800

internal fun DoomEngineCore.P_XYMovement(mo: mobj_t) {
    var ptryx: fixed_t
    var ptryy: fixed_t
    val player: player_t?
    var xmove: fixed_t
    var ymove: fixed_t

    if (mo.momx == 0 && mo.momy == 0) {
        if ((mo.flags and MF_SKULLFLY) != 0) {
            // the skull slammed into something
            mo.flags = mo.flags and MF_SKULLFLY.inv()
            mo.momx = 0
            mo.momy = 0
            mo.momz = 0

            P_SetMobjState(mo, mo.info!!.spawnstate)
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

        if (!P_TryMove(mo, ptryx, ptryy)) {
            // blocked move
            if (mo.player != null) {
                // try to slide along it
                P_SlideMove(mo)
            } else if ((mo.flags and MF_MISSILE) != 0) {
                // explode a missile
                if (ceilingline != null &&
                    ceilingline!!.backsector != null &&
                    ceilingline!!.backsector!!.ceilingpic == skyflatnum) {
                    // Hack to prevent missiles exploding
                    // against the sky.
                    // Does not handle sky floors.
                    P_RemoveMobj(mo)
                    return
                }
                P_ExplodeMissile(mo)
            } else {
                mo.momx = 0
                mo.momy = 0
            }
        }
    } while (xmove != 0 || ymove != 0)

    // slow down
    if (player != null && (player.cheats and CF_NOMOMENTUM) != 0) {
        // debug option for no sliding at all
        mo.momx = 0
        mo.momy = 0
        return
    }

    if ((mo.flags and (MF_MISSILE or MF_SKULLFLY)) != 0)
        return  // no friction for missiles ever

    if (mo.z > mo.floorz)
        return  // no friction when airborne

    if ((mo.flags and MF_CORPSE) != 0) {
        // do not stop sliding
        //  if halfway off a step with some momentum
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
        // if in a walking frame, stop moving
        // (C: (unsigned)((player->mo->state - states) - S_PLAY_RUN1) < 4)
        if (player != null && (player.mo!!.state!!.index - S_PLAY_RUN1).toUInt() < 4u)
            P_SetMobjState(player.mo!!, S_PLAY)

        mo.momx = 0
        mo.momy = 0
    } else {
        mo.momx = FixedMul(mo.momx, FRICTION)
        mo.momy = FixedMul(mo.momy, FRICTION)
    }
}

//
// P_ZMovement
//
internal fun DoomEngineCore.P_ZMovement(mo: mobj_t) {
    val dist: fixed_t
    val delta: fixed_t

    // check for smooth step up
    if (mo.player != null && mo.z < mo.floorz) {
        mo.player!!.viewheight -= mo.floorz - mo.z

        mo.player!!.deltaviewheight = (VIEWHEIGHT - mo.player!!.viewheight) shr 3
    }

    // adjust height
    mo.z += mo.momz

    if ((mo.flags and MF_FLOAT) != 0
        && mo.target != null) {
        // float down towards target if too close
        if ((mo.flags and MF_SKULLFLY) == 0
            && (mo.flags and MF_INFLOAT) == 0) {
            dist = P_AproxDistance(mo.x - mo.target!!.x,
                mo.y - mo.target!!.y)

            delta = (mo.target!!.z + (mo.height shr 1)) - mo.z

            if (delta < 0 && dist < -(delta * 3))
                mo.z -= FLOATSPEED
            else if (delta > 0 && dist < (delta * 3))
                mo.z += FLOATSPEED
        }
    }

    // clip movement
    if (mo.z <= mo.floorz) {
        // hit the floor

        // Note (id):
        //  somebody left this after the setting momz to 0,
        //  kinda useless there.
        if ((mo.flags and MF_SKULLFLY) != 0) {
            // the skull slammed into something
            mo.momz = -mo.momz
        }

        if (mo.momz < 0) {
            if (mo.player != null
                && mo.momz < -GRAVITY * 8) {
                // Squat down.
                // Decrease viewheight for a moment
                // after hitting the ground (hard),
                // and utter appropriate sound.
                mo.player!!.deltaviewheight = mo.momz shr 3
                S_StartSound(mo, sfx_oof)
            }
            mo.momz = 0
        }
        mo.z = mo.floorz

        if ((mo.flags and MF_MISSILE) != 0
            && (mo.flags and MF_NOCLIP) == 0) {
            P_ExplodeMissile(mo)
            return
        }
    } else if ((mo.flags and MF_NOGRAVITY) == 0) {
        if (mo.momz == 0)
            mo.momz = -GRAVITY * 2
        else
            mo.momz -= GRAVITY
    }

    if (mo.z + mo.height > mo.ceilingz) {
        // hit the ceiling
        if (mo.momz > 0)
            mo.momz = 0
        // (C has a detached brace block here -- executes unconditionally.)
        mo.z = mo.ceilingz - mo.height

        if ((mo.flags and MF_SKULLFLY) != 0) {
            // the skull slammed into something
            mo.momz = -mo.momz
        }

        if ((mo.flags and MF_MISSILE) != 0
            && (mo.flags and MF_NOCLIP) == 0) {
            P_ExplodeMissile(mo)
            return
        }
    }
}

//
// P_NightmareRespawn
//
internal fun DoomEngineCore.P_NightmareRespawn(mobj: mobj_t) {
    val x: fixed_t
    val y: fixed_t
    val z: fixed_t
    val ss: subsector_t
    var mo: mobj_t
    val mthing: mapthing_t

    // (C: mobj->spawnpoint is an embedded struct, memset to 0 at spawn;
    //  a mobj spawned without a map thing behaves as an all-zero spawnpoint.)
    mthing = mobj.spawnpoint ?: mapthing_t()

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    // somthing is occupying it's position?
    if (!P_CheckPosition(mobj, x, y))
        return  // no respwan

    // spawn a teleport fog at old spot
    // because of removal of the body?
    mo = P_SpawnMobj(mobj.x,
        mobj.y,
        mobj.subsector!!.sector!!.floorheight, MT_TFOG)
    // initiate teleport sound
    S_StartSound(mo, sfx_telept)

    // spawn a teleport fog at the new spot
    ss = findSubsector(x, y)

    mo = P_SpawnMobj(x, y, ss.sector!!.floorheight, MT_TFOG)

    S_StartSound(mo, sfx_telept)

    // spawn the new monster
    // (mthing = &mobj->spawnpoint; -- bound above)

    // spawn it
    if ((mobj.info!!.flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    // inherit attributes from deceased one
    mo = P_SpawnMobj(x, y, z, mobj.type)
    mo.spawnpoint = mapthing_t().also { it.copyFrom(mthing) }  // struct copy
    mo.angle = ANG45 * (mthing.angle / 45).toUInt()

    if ((mthing.options and MTF_AMBUSH) != 0)
        mo.flags = mo.flags or MF_AMBUSH

    mo.reactiontime = 18

    // remove the old monster,
    P_RemoveMobj(mobj)
}

//
// P_MobjThinker
//
internal fun DoomEngineCore.P_MobjThinker(mobj: mobj_t) {
    // momentum movement
    if (mobj.momx != 0
        || mobj.momy != 0
        || (mobj.flags and MF_SKULLFLY) != 0) {
        P_XYMovement(mobj)

        // FIXME: decent NOP/NULL/Nil function pointer please.
        if (mobj.removed)
            return  // mobj was removed
    }
    if ((mobj.z != mobj.floorz)
        || mobj.momz != 0) {
        P_ZMovement(mobj)

        // FIXME: decent NOP/NULL/Nil function pointer please.
        if (mobj.removed)
            return  // mobj was removed
    }

    // cycle through states,
    // calling action functions at transitions
    if (mobj.tics != -1) {
        mobj.tics--

        // you can cycle through multiple states in a tic
        if (mobj.tics == 0)
            if (!P_SetMobjState(mobj, mobj.state!!.nextstate))
                return  // freed itself
    } else {
        // check for nightmare respawn
        if ((mobj.flags and MF_COUNTKILL) == 0)
            return

        if (!respawnmonsters)
            return

        mobj.movecount++

        if (mobj.movecount < 12 * 35)
            return

        if ((leveltime and 31) != 0)
            return

        if (P_Random() > 4)
            return

        P_NightmareRespawn(mobj)
    }
}

//
// P_SpawnMobj
//
internal fun DoomEngineCore.P_SpawnMobj(x: fixed_t, y: fixed_t, z: fixed_t, type: Int): mobj_t {
    val mobj: mobj_t
    val st: state_t
    val info: mobjinfo_t

    mobj = mobj_t()  // Z_Malloc + memset 0
    info = mobjinfo[type]

    mobj.type = type
    mobj.info = info
    mobj.x = x
    mobj.y = y
    mobj.radius = info.radius
    mobj.height = info.height
    mobj.flags = info.flags
    mobj.health = info.spawnhealth

    if (gameskill != sk_nightmare)
        mobj.reactiontime = info.reactiontime

    mobj.lastlook = P_Random() % MAXPLAYERS
    // do not set the state with P_SetMobjState,
    // because action routines can not be called yet
    st = states[info.spawnstate]

    mobj.state = st
    mobj.tics = st.tics
    mobj.sprite = st.sprite
    mobj.frame = st.frame

    // set subsector and/or block links
    P_SetThingPosition(mobj)

    mobj.floorz = mobj.subsector!!.sector!!.floorheight
    mobj.ceilingz = mobj.subsector!!.sector!!.ceilingheight

    if (z == ONFLOORZ)
        mobj.z = mobj.floorz
    else if (z == ONCEILINGZ)
        mobj.z = mobj.ceilingz - mobj.info!!.height
    else
        mobj.z = z

    mobj.function = { th -> P_MobjThinker(th as mobj_t) }

    P_AddThinker(mobj)

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

internal fun DoomEngineCore.P_RemoveMobj(mobj: mobj_t) {
    if ((mobj.flags and MF_SPECIAL) != 0
        && (mobj.flags and MF_DROPPED) == 0
        && (mobj.type != MT_INV)
        && (mobj.type != MT_INS)) {
        itemrespawnque[iquehead].copyFrom(mobj.spawnpoint ?: mapthing_t())
        itemrespawntime[iquehead] = leveltime
        iquehead = (iquehead + 1) and (ITEMQUESIZE - 1)

        // lose one off the end?
        if (iquehead == iquetail)
            iquetail = (iquetail + 1) and (ITEMQUESIZE - 1)
    }

    // unlink from sector and block lists
    P_UnsetThingPosition(mobj)

    // stop any playing sound
    S_StopSound(mobj)

    // free block
    P_RemoveThinker(mobj)
}

//
// P_RespawnSpecials
//
internal fun DoomEngineCore.P_RespawnSpecials() {
    val x: fixed_t
    val y: fixed_t
    val z: fixed_t

    val ss: subsector_t
    var mo: mobj_t
    val mthing: mapthing_t

    var i: Int

    // only respawn items in deathmatch
    if (deathmatch != 2)
        return  //

    // nothing left to respawn?
    if (iquehead == iquetail)
        return

    // wait at least 30 seconds
    if (leveltime - itemrespawntime[iquetail] < 30 * 35)
        return

    mthing = itemrespawnque[iquetail]

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    // spawn a teleport fog at the new spot
    ss = findSubsector(x, y)
    mo = P_SpawnMobj(x, y, ss.sector!!.floorheight, MT_IFOG)
    S_StartSound(mo, sfx_itmbk)

    // find which type to spawn
    i = 0
    while (i < NUMMOBJTYPES) {
        if (mthing.type == mobjinfo[i].doomednum)
            break
        i++
    }

    // spawn it
    if ((mobjinfo[i].flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    mo = P_SpawnMobj(x, y, z, i)
    mo.spawnpoint = mapthing_t().also { it.copyFrom(mthing) }  // struct copy
    mo.angle = ANG45 * (mthing.angle / 45).toUInt()

    // pull it from the que
    iquetail = (iquetail + 1) and (ITEMQUESIZE - 1)
}

//
// P_SpawnPlayer
// Called when a player is spawned on the level.
// Most of the player structure stays unchanged
//  between levels.
//
internal fun DoomEngineCore.P_SpawnPlayer(mthing: mapthing_t) {
    val p: player_t
    val x: fixed_t
    val y: fixed_t
    val z: fixed_t

    val mobj: mobj_t

    var i: Int

    // not playing?
    if (!playeringame[mthing.type - 1])
        return

    p = players[mthing.type - 1]

    if (p.playerstate == PST_REBORN)
        G_PlayerReborn(mthing.type - 1)

    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS
    z = ONFLOORZ
    mobj = P_SpawnMobj(x, y, z, MT_PLAYER)

    // set color translations for player sprites
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

    // setup gun psprite
    P_SetupPsprites(p)

    // give all cards in death match mode
    if (deathmatch != 0) {
        i = 0
        while (i < NUMCARDS) {
            p.cards[i] = true
            i++
        }
    }

    if (mthing.type - 1 == consoleplayer) {
        // wake up the status bar
        ST_Start()
        // wake up the heads up text
        HU_Start()
    }
}

//
// P_SpawnMapThing
// The fields of the mapthing should
// already be in host byte order.
//
internal fun DoomEngineCore.P_SpawnMapThing(mthing: mapthing_t) {
    var i: Int
    val bit: Int
    val mobj: mobj_t
    val x: fixed_t
    val y: fixed_t
    val z: fixed_t

    // count deathmatch start positions
    if (mthing.type == 11) {
        // (C: deathmatch_p < &deathmatchstarts[10])
        if (deathmatch_p < 10) {
            deathmatchstarts[deathmatch_p].copyFrom(mthing)
            deathmatch_p++
        }
        return
    }

    // check for players specially
    if (mthing.type <= 4) {
        // save spots for respawning in network games
        playerstarts[mthing.type - 1].copyFrom(mthing)
        if (deathmatch == 0)
            P_SpawnPlayer(mthing)

        return
    }

    // check for apropriate skill level
    if (!netgame && (mthing.options and 16) != 0)
        return

    if (gameskill == sk_baby)
        bit = 1
    else if (gameskill == sk_nightmare)
        bit = 4
    else
        bit = 1 shl (gameskill - 1)

    if ((mthing.options and bit) == 0)
        return

    // find which type to spawn
    i = 0
    while (i < NUMMOBJTYPES) {
        if (mthing.type == mobjinfo[i].doomednum)
            break
        i++
    }

    if (i == NUMMOBJTYPES)
        I_Error("P_SpawnMapThing: Unknown type ${mthing.type} at (${mthing.x}, ${mthing.y})")

    // don't spawn keycards and players in deathmatch
    if (deathmatch != 0 && (mobjinfo[i].flags and MF_NOTDMATCH) != 0)
        return

    // don't spawn any monsters if -nomonsters
    if (nomonsters
        && (i == MT_SKULL
            || (mobjinfo[i].flags and MF_COUNTKILL) != 0)) {
        return
    }

    // spawn it
    x = mthing.x shl FRACBITS
    y = mthing.y shl FRACBITS

    if ((mobjinfo[i].flags and MF_SPAWNCEILING) != 0)
        z = ONCEILINGZ
    else
        z = ONFLOORZ

    mobj = P_SpawnMobj(x, y, z, i)
    mobj.spawnpoint = mapthing_t().also { it.copyFrom(mthing) }  // struct copy

    if (mobj.tics > 0)
        mobj.tics = 1 + (P_Random() % mobj.tics)
    if ((mobj.flags and MF_COUNTKILL) != 0)
        totalkills++
    if ((mobj.flags and MF_COUNTITEM) != 0)
        totalitems++

    mobj.angle = ANG45 * (mthing.angle / 45).toUInt()
    if ((mthing.options and MTF_AMBUSH) != 0)
        mobj.flags = mobj.flags or MF_AMBUSH
}

//
// GAME SPAWN FUNCTIONS
//

//
// P_SpawnPuff
//
// (extern fixed_t attackrange; -- lives in p_map.kt)
internal fun DoomEngineCore.P_SpawnPuff(x: fixed_t, y: fixed_t, z: fixed_t) {
    var z = z
    val th: mobj_t

    z += ((P_Random() - P_Random()) shl 10)

    th = P_SpawnMobj(x, y, z, MT_PUFF)
    th.momz = FRACUNIT
    th.tics -= P_Random() and 3

    if (th.tics < 1)
        th.tics = 1

    // don't make punches spark on the wall
    if (attackrange == MELEERANGE)
        P_SetMobjState(th, S_PUFF3)
}

//
// P_SpawnBlood
//
internal fun DoomEngineCore.P_SpawnBlood(x: fixed_t, y: fixed_t, z: fixed_t, damage: Int) {
    var z = z
    val th: mobj_t

    z += ((P_Random() - P_Random()) shl 10)
    th = P_SpawnMobj(x, y, z, MT_BLOOD)
    th.momz = FRACUNIT * 2
    th.tics -= P_Random() and 3

    if (th.tics < 1)
        th.tics = 1

    if (damage <= 12 && damage >= 9)
        P_SetMobjState(th, S_BLOOD2)
    else if (damage < 9)
        P_SetMobjState(th, S_BLOOD3)
}

//
// P_CheckMissileSpawn
// Moves the missile forward a bit
//  and possibly explodes it right there.
//
internal fun DoomEngineCore.P_CheckMissileSpawn(th: mobj_t) {
    th.tics -= P_Random() and 3
    if (th.tics < 1)
        th.tics = 1

    // move a little forward so an angle can
    // be computed if it immediately explodes
    th.x += (th.momx shr 1)
    th.y += (th.momy shr 1)
    th.z += (th.momz shr 1)

    if (!P_TryMove(th, th.x, th.y))
        P_ExplodeMissile(th)
}

//
// P_SpawnMissile
//
internal fun DoomEngineCore.P_SpawnMissile(source: mobj_t, dest: mobj_t, type: Int): mobj_t {
    val th: mobj_t
    var an: angle_t
    var dist: Int

    th = P_SpawnMobj(source.x,
        source.y,
        source.z + 4 * 8 * FRACUNIT, type)

    if (th.info!!.seesound != 0)
        S_StartSound(th, th.info!!.seesound)

    th.target = source  // where it came from
    an = FixedGeometry.angleBetween(source.x, source.y, dest.x, dest.y)

    // fuzzy player
    if ((dest.flags and MF_SHADOW) != 0)
        an += ((P_Random() - P_Random()) shl 20).toUInt()

    th.angle = an
    an = an shr ANGLETOFINESHIFT
    th.momx = FixedMul(th.info!!.speed, finecosine[an.toInt()])
    th.momy = FixedMul(th.info!!.speed, finesine[an.toInt()])

    dist = P_AproxDistance(dest.x - source.x, dest.y - source.y)
    dist = dist / th.info!!.speed

    if (dist < 1)
        dist = 1

    th.momz = (dest.z - source.z) / dist
    P_CheckMissileSpawn(th)

    return th
}

//
// P_SpawnPlayerMissile
// Tries to aim at a nearby monster
//
internal fun DoomEngineCore.P_SpawnPlayerMissile(source: mobj_t, type: Int) {
    val th: mobj_t
    var an: angle_t

    val x: fixed_t
    val y: fixed_t
    val z: fixed_t
    var slope: fixed_t

    // see which target is to be aimed at
    an = source.angle
    slope = P_AimLineAttack(source, an, 16 * 64 * FRACUNIT)

    if (linetarget == null) {
        an += (1 shl 26).toUInt()
        slope = P_AimLineAttack(source, an, 16 * 64 * FRACUNIT)

        if (linetarget == null) {
            an -= (2 shl 26).toUInt()
            slope = P_AimLineAttack(source, an, 16 * 64 * FRACUNIT)
        }

        if (linetarget == null) {
            an = source.angle
            slope = 0
        }
    }

    x = source.x
    y = source.y
    z = source.z + 4 * 8 * FRACUNIT

    th = P_SpawnMobj(x, y, z, type)

    if (th.info!!.seesound != 0)
        S_StartSound(th, th.info!!.seesound)

    th.target = source
    th.angle = an
    th.momx = FixedMul(th.info!!.speed,
        finecosine[(an shr ANGLETOFINESHIFT).toInt()])
    th.momy = FixedMul(th.info!!.speed,
        finesine[(an shr ANGLETOFINESHIFT).toInt()])
    th.momz = FixedMul(th.info!!.speed, slope)

    P_CheckMissileSpawn(th)
}
