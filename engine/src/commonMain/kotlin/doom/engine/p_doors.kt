// Port of linuxdoom-1.10 p_doors.c -- Door animation code (opening/closing).
// (Also owns the P_DOORS thinker struct + vldoor_e consts from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING",
    "SENSELESS_COMPARISON", "ktlint")

package doom.engine

// From p_spec.h: vldoor_e
internal const val normal = 0
internal const val close30ThenOpen = 1
internal const val close = 2
internal const val open = 3
internal const val raiseIn5Mins = 4
internal const val blazeRaise = 5
internal const val blazeOpen = 6
internal const val blazeClose = 7


internal const val VDOORSPEED = FRACUNIT * 2
internal const val VDOORWAIT = 150

// (The vanilla "#if 0" sliding door code -- slideFrameNames,
//  P_InitSlidingDoorFrames, T_SlidingDoor, EV_SlidingDoor --
//  is compiled out in linuxdoom-1.10 and skipped here too.)

//
// VERTICAL DOORS
//

//
// T_VerticalDoor
//
internal fun DoomEngineCore.T_VerticalDoor(door: vldoor_t) {
    val res: Int

    when (door.direction) {
        0 -> {
            // WAITING
            door.topcountdown--
            if (door.topcountdown == 0) {
                when (door.type) {
                    blazeRaise -> {
                        door.direction = -1  // time to go back down
                        S_StartSound(door.sector!!.soundorg, sfx_bdcls)
                    }

                    normal -> {
                        door.direction = -1  // time to go back down
                        S_StartSound(door.sector!!.soundorg, sfx_dorcls)
                    }

                    close30ThenOpen -> {
                        door.direction = 1
                        S_StartSound(door.sector!!.soundorg, sfx_doropn)
                    }

                    else -> {}
                }
            }
        }

        2 -> {
            //  INITIAL WAIT
            door.topcountdown--
            if (door.topcountdown == 0) {
                when (door.type) {
                    raiseIn5Mins -> {
                        door.direction = 1
                        door.type = normal
                        S_StartSound(door.sector!!.soundorg, sfx_doropn)
                    }

                    else -> {}
                }
            }
        }

        -1 -> {
            // DOWN
            res = T_MovePlane(door.sector!!,
                door.speed,
                door.sector!!.floorheight,
                false, 1, door.direction)
            if (res == pastdest) {
                when (door.type) {
                    blazeRaise,
                    blazeClose -> {
                        door.sector!!.specialdata = null
                        P_RemoveThinker(door)  // unlink and free
                        S_StartSound(door.sector!!.soundorg, sfx_bdcls)
                    }

                    normal,
                    close -> {
                        door.sector!!.specialdata = null
                        P_RemoveThinker(door)  // unlink and free
                    }

                    close30ThenOpen -> {
                        door.direction = 0
                        door.topcountdown = 35 * 30
                    }

                    else -> {}
                }
            } else if (res == crushed) {
                when (door.type) {
                    blazeClose,
                    close -> {
                        // DO NOT GO BACK UP!
                    }

                    else -> {
                        door.direction = 1
                        S_StartSound(door.sector!!.soundorg, sfx_doropn)
                    }
                }
            }
        }

        1 -> {
            // UP
            res = T_MovePlane(door.sector!!,
                door.speed,
                door.topheight,
                false, 1, door.direction)

            if (res == pastdest) {
                when (door.type) {
                    blazeRaise,
                    normal -> {
                        door.direction = 0  // wait at top
                        door.topcountdown = door.topwait
                    }

                    close30ThenOpen,
                    blazeOpen,
                    open -> {
                        door.sector!!.specialdata = null
                        P_RemoveThinker(door)  // unlink and free
                    }

                    else -> {}
                }
            }
        }
    }
}

//
// EV_DoLockedDoor
// Move a locked door up/down
//

internal fun DoomEngineCore.EV_DoLockedDoor(line: line_t, type: Int, thing: mobj_t): Int {
    val p: player_t?

    p = thing.player

    if (p == null)
        return 0

    when (line.special) {
        99,  // Blue Lock
        133 -> {
            if (p == null)
                return 0
            if (!p.cards[it_bluecard] && !p.cards[it_blueskull]) {
                p.message = PD_BLUEO
                S_StartSound(null, sfx_oof)
                return 0
            }
        }

        134,  // Red Lock
        135 -> {
            if (p == null)
                return 0
            if (!p.cards[it_redcard] && !p.cards[it_redskull]) {
                p.message = PD_REDO
                S_StartSound(null, sfx_oof)
                return 0
            }
        }

        136,  // Yellow Lock
        137 -> {
            if (p == null)
                return 0
            if (!p.cards[it_yellowcard] &&
                !p.cards[it_yellowskull]) {
                p.message = PD_YELLOWO
                S_StartSound(null, sfx_oof)
                return 0
            }
        }
    }

    return EV_DoDoor(line, type)
}

internal fun DoomEngineCore.EV_DoDoor(line: line_t, type: Int): Int {
    var secnum: Int
    var rtn: Int
    var sec: sector_t
    var door: vldoor_t

    secnum = -1
    rtn = 0

    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]
        if (sec.specialdata != null)
            continue

        // new door thinker
        rtn = 1
        door = vldoor_t()
        P_AddThinker(door)
        sec.specialdata = door

        door.function = { th -> T_VerticalDoor(th as vldoor_t) }
        door.sector = sec
        door.type = type
        door.topwait = VDOORWAIT
        door.speed = VDOORSPEED

        when (type) {
            blazeClose -> {
                door.topheight = P_FindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.direction = -1
                door.speed = VDOORSPEED * 4
                S_StartSound(door.sector!!.soundorg, sfx_bdcls)
            }

            close -> {
                door.topheight = P_FindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.direction = -1
                S_StartSound(door.sector!!.soundorg, sfx_dorcls)
            }

            close30ThenOpen -> {
                door.topheight = sec.ceilingheight
                door.direction = -1
                S_StartSound(door.sector!!.soundorg, sfx_dorcls)
            }

            blazeRaise,
            blazeOpen -> {
                door.direction = 1
                door.topheight = P_FindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.speed = VDOORSPEED * 4
                if (door.topheight != sec.ceilingheight)
                    S_StartSound(door.sector!!.soundorg, sfx_bdopn)
            }

            normal,
            open -> {
                door.direction = 1
                door.topheight = P_FindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                if (door.topheight != sec.ceilingheight)
                    S_StartSound(door.sector!!.soundorg, sfx_doropn)
            }

            else -> {}
        }
    }
    return rtn
}

//
// Vanilla stores sector_t.specialdata as a void* and EV_VerticalDoor
// dereferences it as a vldoor_t* regardless of what actually lives there.
// In the 32-bit struct layouts vldoor_t.direction (byte offset 28) overlays
// plat_t.wait, ceiling_t.speed and floormove_t.newspecial; chocolate-doom
// emulates the door/plat cases of this explicitly. These helpers replicate
// the aliased field access for every thinker that can sit in specialdata.
//
private fun DoomEngineCore.vldoor_direction_aliased_read(th: thinker_t): Int {
    return when (th) {
        is vldoor_t -> th.direction
        is plat_t -> th.wait
        is ceiling_t -> th.speed
        is floormove_t -> th.newspecial
        else -> 0
    }
}

private fun DoomEngineCore.vldoor_direction_aliased_write(th: thinker_t, value: Int) {
    when (th) {
        is vldoor_t -> th.direction = value
        is plat_t -> th.wait = value
        is ceiling_t -> th.speed = value
        is floormove_t -> th.newspecial = value
        else -> {}
    }
}

//
// EV_VerticalDoor : open a door manually, no tag value
//
internal fun DoomEngineCore.EV_VerticalDoor(line: line_t, thing: mobj_t) {
    val player: player_t?
    val secnum: Int
    val sec: sector_t
    val door: vldoor_t
    val side: Int

    side = 0  // only front sides can be used

    //	Check for locks
    player = thing.player

    when (line.special) {
        26,  // Blue Lock
        32 -> {
            if (player == null)
                return

            if (!player.cards[it_bluecard] && !player.cards[it_blueskull]) {
                player.message = PD_BLUEK
                S_StartSound(null, sfx_oof)
                return
            }
        }

        27,  // Yellow Lock
        34 -> {
            if (player == null)
                return

            if (!player.cards[it_yellowcard] &&
                !player.cards[it_yellowskull]) {
                player.message = PD_YELLOWK
                S_StartSound(null, sfx_oof)
                return
            }
        }

        28,  // Red Lock
        33 -> {
            if (player == null)
                return

            if (!player.cards[it_redcard] && !player.cards[it_redskull]) {
                player.message = PD_REDK
                S_StartSound(null, sfx_oof)
                return
            }
        }
    }

    // if the sector has an active thinker, use it
    // (vanilla reads sides[-1] here when the DR special is on a one sided
    //  line -- undefined behavior; chocolate-doom turns it into an error.)
    if (line.sidenum[side xor 1] == -1)
        I_Error("EV_VerticalDoor: DR special type on 1-sided linedef")

    sec = sides[line.sidenum[side xor 1]].sector!!
    secnum = sec.index

    if (sec.specialdata != null) {
        val sd = sec.specialdata!!
        when (line.special) {
            1,  // ONLY FOR "RAISE" DOORS, NOT "OPEN"s
            26,
            27,
            28,
            117 -> {
                // When is a door not a door?
                // In Vanilla, door->direction is read and set even though
                // "specialdata" might not actually point at a door
                // (notably a plat in ep1-0500.lmp) -- the access lands in
                // whatever field overlays vldoor_t.direction. Follow
                // chocolate-doom's emulation of that vanilla behavior.
                if (vldoor_direction_aliased_read(sd) == -1)
                    vldoor_direction_aliased_write(sd, 1)  // go back up
                else {
                    if (thing.player == null)
                        return  // JDC: bad guys never close doors

                    if (sd is vldoor_t)
                        sd.direction = -1  // start going down immediately
                    else if (sd is plat_t) {
                        // Erm, this is a plat, not a door.
                        // The direction field in vldoor_t corresponds to
                        // the wait field in plat_t. Let's set that to -1
                        // instead.
                        sd.wait = -1
                    } else {
                        // This isn't a door OR a plat.  Now we're in trouble.
                        println("EV_VerticalDoor: Tried to close something that wasn't a door.")

                        // Try closing it anyway. . .
                        vldoor_direction_aliased_write(sd, -1)
                    }
                }
                return
            }
        }
    }

    // for proper sound
    when (line.special) {
        117,  // BLAZING DOOR RAISE
        118 ->  // BLAZING DOOR OPEN
            S_StartSound(sec.soundorg, sfx_bdopn)

        1,  // NORMAL DOOR SOUND
        31 ->
            S_StartSound(sec.soundorg, sfx_doropn)

        else ->  // LOCKED DOOR SOUND
            S_StartSound(sec.soundorg, sfx_doropn)
    }

    // new door thinker
    door = vldoor_t()
    P_AddThinker(door)
    sec.specialdata = door
    door.function = { th -> T_VerticalDoor(th as vldoor_t) }
    door.sector = sec
    door.direction = 1
    door.speed = VDOORSPEED
    door.topwait = VDOORWAIT

    when (line.special) {
        1,
        26,
        27,
        28 ->
            door.type = normal

        31,
        32,
        33,
        34 -> {
            door.type = open
            line.special = 0
        }

        117 -> {  // blazing door raise
            door.type = blazeRaise
            door.speed = VDOORSPEED * 4
        }

        118 -> {  // blazing door open
            door.type = blazeOpen
            line.special = 0
            door.speed = VDOORSPEED * 4
        }
    }

    // find the top and bottom of the movement range
    door.topheight = P_FindLowestCeilingSurrounding(sec)
    door.topheight -= 4 * FRACUNIT
}

//
// Spawn a door that closes after 30 seconds
//
internal fun DoomEngineCore.P_SpawnDoorCloseIn30(sec: sector_t) {
    val door: vldoor_t

    door = vldoor_t()

    P_AddThinker(door)

    sec.specialdata = door
    sec.special = 0

    door.function = { th -> T_VerticalDoor(th as vldoor_t) }
    door.sector = sec
    door.direction = 0
    door.type = normal
    door.speed = VDOORSPEED
    door.topcountdown = 30 * 35
}

//
// Spawn a door that opens after 5 minutes
//
internal fun DoomEngineCore.P_SpawnDoorRaiseIn5Mins(sec: sector_t, secnum: Int) {
    val door: vldoor_t

    door = vldoor_t()

    P_AddThinker(door)

    sec.specialdata = door
    sec.special = 0

    door.function = { th -> T_VerticalDoor(th as vldoor_t) }
    door.sector = sec
    door.direction = 2
    door.type = raiseIn5Mins
    door.speed = VDOORSPEED
    door.topheight = P_FindLowestCeilingSurrounding(sec)
    door.topheight -= 4 * FRACUNIT
    door.topwait = VDOORWAIT
    door.topcountdown = 5 * 60 * 35
}
