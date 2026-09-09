
package doom.engine.world.movers

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_BDCLS
import doom.engine.audio.SFX_BDOPN
import doom.engine.audio.SFX_DORCLS
import doom.engine.audio.SFX_DOROPN
import doom.engine.audio.SFX_OOF
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.IT_BLUECARD
import doom.engine.gameplay.IT_BLUESKULL
import doom.engine.gameplay.IT_REDCARD
import doom.engine.gameplay.IT_REDSKULL
import doom.engine.gameplay.IT_YELLOWCARD
import doom.engine.gameplay.IT_YELLOWSKULL
import doom.engine.gameplay.player.Player
import doom.engine.geometry.FRACUNIT
import doom.engine.resources.PD_BLUEK
import doom.engine.resources.PD_BLUEO
import doom.engine.resources.PD_REDK
import doom.engine.resources.PD_REDO
import doom.engine.resources.PD_YELLOWK
import doom.engine.resources.PD_YELLOWO
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRemoveThinker
import doom.engine.simulation.Thinker
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.sectors
import doom.engine.world.sides
import doom.engine.world.specials.pFindLowestCeilingSurrounding
import doom.engine.world.specials.pFindSectorFromLineTag

internal const val NORMAL = 0
internal const val CLOSE30_THEN_OPEN = 1
internal const val CLOSE = 2
internal const val OPEN = 3
internal const val RAISE_IN5_MINS = 4
internal const val BLAZE_RAISE = 5
internal const val BLAZE_OPEN = 6
internal const val BLAZE_CLOSE = 7

internal const val VDOORSPEED = FRACUNIT * 2
internal const val VDOORWAIT = 150



internal fun DoomEngineCore.tVerticalDoor(door: VerticalDoor) {
    val res: Int

    when (door.direction) {
        0 -> {
            door.topcountdown--
            if (door.topcountdown == 0) {
                when (door.type) {
                    BLAZE_RAISE -> {
                        door.direction = -1
                        sStartSound(door.sector!!.soundorg, SFX_BDCLS)
                    }

                    NORMAL -> {
                        door.direction = -1
                        sStartSound(door.sector!!.soundorg, SFX_DORCLS)
                    }

                    CLOSE30_THEN_OPEN -> {
                        door.direction = 1
                        sStartSound(door.sector!!.soundorg, SFX_DOROPN)
                    }

                    else -> {}
                }
            }
        }

        2 -> {
            door.topcountdown--
            if (door.topcountdown == 0) {
                when (door.type) {
                    RAISE_IN5_MINS -> {
                        door.direction = 1
                        door.type = NORMAL
                        sStartSound(door.sector!!.soundorg, SFX_DOROPN)
                    }

                    else -> {}
                }
            }
        }

        -1 -> {
            res = tMovePlane(door.sector!!,
                door.speed,
                door.sector!!.floorheight,
                false, 1, door.direction)
            if (res == PASTDEST) {
                when (door.type) {
                    BLAZE_RAISE,
                    BLAZE_CLOSE -> {
                        door.sector!!.specialdata = null
                        pRemoveThinker(door)
                        sStartSound(door.sector!!.soundorg, SFX_BDCLS)
                    }

                    NORMAL,
                    CLOSE -> {
                        door.sector!!.specialdata = null
                        pRemoveThinker(door)
                    }

                    CLOSE30_THEN_OPEN -> {
                        door.direction = 0
                        door.topcountdown = 35 * 30
                    }

                    else -> {}
                }
            } else if (res == CRUSHED) {
                when (door.type) {
                    BLAZE_CLOSE,
                    CLOSE -> {
                    }

                    else -> {
                        door.direction = 1
                        sStartSound(door.sector!!.soundorg, SFX_DOROPN)
                    }
                }
            }
        }

        1 -> {
            res = tMovePlane(door.sector!!,
                door.speed,
                door.topheight,
                false, 1, door.direction)

            if (res == PASTDEST) {
                when (door.type) {
                    BLAZE_RAISE,
                    NORMAL -> {
                        door.direction = 0
                        door.topcountdown = door.topwait
                    }

                    CLOSE30_THEN_OPEN,
                    BLAZE_OPEN,
                    OPEN -> {
                        door.sector!!.specialdata = null
                        pRemoveThinker(door)
                    }

                    else -> {}
                }
            }
        }
    }
}


internal fun DoomEngineCore.evDoLockedDoor(line: MapLine, type: Int, thing: Actor): Int {
    val p = thing.player ?: return 0

    when (line.special) {
        99,
        133 -> {
            if (!p.cards[IT_BLUECARD] && !p.cards[IT_BLUESKULL]) {
                p.message = PD_BLUEO
                sStartSound(null, SFX_OOF)
                return 0
            }
        }

        134,
        135 -> {
            if (!p.cards[IT_REDCARD] && !p.cards[IT_REDSKULL]) {
                p.message = PD_REDO
                sStartSound(null, SFX_OOF)
                return 0
            }
        }

        136,
        137 -> {
            if (!p.cards[IT_YELLOWCARD] &&
                !p.cards[IT_YELLOWSKULL]) {
                p.message = PD_YELLOWO
                sStartSound(null, SFX_OOF)
                return 0
            }
        }
    }

    return evDoDoor(line, type)
}

internal fun DoomEngineCore.evDoDoor(line: MapLine, type: Int): Int {
    var secnum: Int
    var rtn: Int
    var sec: Sector
    var door: VerticalDoor

    secnum = -1
    rtn = 0

    while (true) {
        secnum = pFindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]
        if (sec.specialdata != null)
            continue

        rtn = 1
        door = VerticalDoor()
        pAddThinker(door)
        sec.specialdata = door

        door.function = { th -> tVerticalDoor(th as VerticalDoor) }
        door.sector = sec
        door.type = type
        door.topwait = VDOORWAIT
        door.speed = VDOORSPEED

        when (type) {
            BLAZE_CLOSE -> {
                door.topheight = pFindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.direction = -1
                door.speed = VDOORSPEED * 4
                sStartSound(door.sector!!.soundorg, SFX_BDCLS)
            }

            CLOSE -> {
                door.topheight = pFindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.direction = -1
                sStartSound(door.sector!!.soundorg, SFX_DORCLS)
            }

            CLOSE30_THEN_OPEN -> {
                door.topheight = sec.ceilingheight
                door.direction = -1
                sStartSound(door.sector!!.soundorg, SFX_DORCLS)
            }

            BLAZE_RAISE,
            BLAZE_OPEN -> {
                door.direction = 1
                door.topheight = pFindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                door.speed = VDOORSPEED * 4
                if (door.topheight != sec.ceilingheight)
                    sStartSound(door.sector!!.soundorg, SFX_BDOPN)
            }

            NORMAL,
            OPEN -> {
                door.direction = 1
                door.topheight = pFindLowestCeilingSurrounding(sec)
                door.topheight -= 4 * FRACUNIT
                if (door.topheight != sec.ceilingheight)
                    sStartSound(door.sector!!.soundorg, SFX_DOROPN)
            }

            else -> {}
        }
    }
    return rtn
}

private fun DoomEngineCore.vldoorDirectionAliasedRead(th: Thinker): Int {
    return when (th) {
        is VerticalDoor -> th.direction
        is PlatformMover -> th.wait
        is CeilingMover -> th.speed
        is FloorMover -> th.newspecial
        else -> 0
    }
}

private fun DoomEngineCore.vldoorDirectionAliasedWrite(th: Thinker, value: Int) {
    when (th) {
        is VerticalDoor -> th.direction = value
        is PlatformMover -> th.wait = value
        is CeilingMover -> th.speed = value
        is FloorMover -> th.newspecial = value
        else -> {}
    }
}

internal fun DoomEngineCore.evVerticalDoor(line: MapLine, thing: Actor) {
    val player: Player?
    val sec: Sector
    val door: VerticalDoor
    val side: Int

    side = 0

    player = thing.player

    when (line.special) {
        26,
        32 -> {
            if (player == null)
                return

            if (!player.cards[IT_BLUECARD] && !player.cards[IT_BLUESKULL]) {
                player.message = PD_BLUEK
                sStartSound(null, SFX_OOF)
                return
            }
        }

        27,
        34 -> {
            if (player == null)
                return

            if (!player.cards[IT_YELLOWCARD] &&
                !player.cards[IT_YELLOWSKULL]) {
                player.message = PD_YELLOWK
                sStartSound(null, SFX_OOF)
                return
            }
        }

        28,
        33 -> {
            if (player == null)
                return

            if (!player.cards[IT_REDCARD] && !player.cards[IT_REDSKULL]) {
                player.message = PD_REDK
                sStartSound(null, SFX_OOF)
                return
            }
        }
    }

    if (line.sidenum[side xor 1] == -1)
        iError("EV_VerticalDoor: DR special type on 1-sided linedef")

    sec = sides[line.sidenum[side xor 1]].sector!!

    if (sec.specialdata != null) {
        val sd = sec.specialdata!!
        when (line.special) {
            1,
            26,
            27,
            28,
            117 -> {
                if (vldoorDirectionAliasedRead(sd) == -1)
                    vldoorDirectionAliasedWrite(sd, 1)
                else {
                    if (thing.player == null)
                        return

                    if (sd is VerticalDoor)
                        sd.direction = -1
                    else if (sd is PlatformMover) {
                        sd.wait = -1
                    } else {
                        println("EV_VerticalDoor: Tried to close something that wasn't a door.")

                        vldoorDirectionAliasedWrite(sd, -1)
                    }
                }
                return
            }
        }
    }

    when (line.special) {
        117,
        118 ->
            sStartSound(sec.soundorg, SFX_BDOPN)

        1,
        31 ->
            sStartSound(sec.soundorg, SFX_DOROPN)

        else ->
            sStartSound(sec.soundorg, SFX_DOROPN)
    }

    door = VerticalDoor()
    pAddThinker(door)
    sec.specialdata = door
    door.function = { th -> tVerticalDoor(th as VerticalDoor) }
    door.sector = sec
    door.direction = 1
    door.speed = VDOORSPEED
    door.topwait = VDOORWAIT

    when (line.special) {
        1,
        26,
        27,
        28 ->
            door.type = NORMAL

        31,
        32,
        33,
        34 -> {
            door.type = OPEN
            line.special = 0
        }

        117 -> {
            door.type = BLAZE_RAISE
            door.speed = VDOORSPEED * 4
        }

        118 -> {
            door.type = BLAZE_OPEN
            line.special = 0
            door.speed = VDOORSPEED * 4
        }
    }

    door.topheight = pFindLowestCeilingSurrounding(sec)
    door.topheight -= 4 * FRACUNIT
}

internal fun DoomEngineCore.pSpawnDoorCloseIn30(sec: Sector) {
    val door: VerticalDoor

    door = VerticalDoor()

    pAddThinker(door)

    sec.specialdata = door
    sec.special = 0

    door.function = { th -> tVerticalDoor(th as VerticalDoor) }
    door.sector = sec
    door.direction = 0
    door.type = NORMAL
    door.speed = VDOORSPEED
    door.topcountdown = 30 * 35
}

internal fun DoomEngineCore.pSpawnDoorRaiseIn5Mins(sec: Sector) {
    val door: VerticalDoor

    door = VerticalDoor()

    pAddThinker(door)

    sec.specialdata = door
    sec.special = 0

    door.function = { th -> tVerticalDoor(th as VerticalDoor) }
    door.sector = sec
    door.direction = 2
    door.type = RAISE_IN5_MINS
    door.speed = VDOORSPEED
    door.topheight = pFindLowestCeilingSurrounding(sec)
    door.topheight -= 4 * FRACUNIT
    door.topwait = VDOORWAIT
    door.topcountdown = 5 * 60 * 35
}
