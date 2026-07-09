// Port of linuxdoom-1.10 p_floor.c -- Floor animation: raising stairs.
// (Also owns the P_FLOOR thinker struct + floor_e/stair_e/result_e consts
//  and FLOORSPEED from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: floor_e
// lower floor to highest surrounding floor
const val lowerFloor = 0

// lower floor to lowest surrounding floor
const val lowerFloorToLowest = 1

// lower floor to highest surrounding floor VERY FAST
const val turboLower = 2

// raise floor to lowest surrounding CEILING
const val raiseFloor = 3

// raise floor to next highest surrounding floor
const val raiseFloorToNearest = 4

// raise floor to shortest height texture around it
const val raiseToTexture = 5

// lower floor to lowest surrounding floor
//  and change floorpic
const val lowerAndChange = 6

const val raiseFloor24 = 7
const val raiseFloor24AndChange = 8
const val raiseFloorCrush = 9

// raise to next highest floor, turbo-speed
const val raiseFloorTurbo = 10
const val donutRaise = 11
const val raiseFloor512 = 12

// From p_spec.h: stair_e
const val build8 = 0   // slowly build by 8
const val turbo16 = 1  // quickly build by 16

// From p_spec.h: floormove_t
class floormove_t : thinker_t() {
    var type = 0  // floor_e
    var crush = false
    var sector: sector_t? = null
    var direction = 0
    var newspecial = 0
    var texture = 0  // C: short
    var floordestheight: fixed_t = 0
    var speed: fixed_t = 0
}

const val FLOORSPEED = FRACUNIT

// From p_spec.h: result_e
const val ok = 0
const val crushed = 1
const val pastdest = 2

//
// FLOORS
//

//
// Move a plane (floor or ceiling) and check for crushing
//
fun T_MovePlane(
    sector: sector_t,
    speed: fixed_t,
    dest: fixed_t,
    crush: Boolean,
    floorOrCeiling: Int,
    direction: Int,
): Int {
    var flag: Boolean
    var lastpos: fixed_t

    when (floorOrCeiling) {
        0 -> {
            // FLOOR
            when (direction) {
                -1 -> {
                    // DOWN
                    if (sector.floorheight - speed < dest) {
                        lastpos = sector.floorheight
                        sector.floorheight = dest
                        flag = P_ChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            P_ChangeSector(sector, crush)
                            // return crushed;
                        }
                        return pastdest
                    } else {
                        lastpos = sector.floorheight
                        sector.floorheight -= speed
                        flag = P_ChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            P_ChangeSector(sector, crush)
                            return crushed
                        }
                    }
                }

                1 -> {
                    // UP
                    if (sector.floorheight + speed > dest) {
                        lastpos = sector.floorheight
                        sector.floorheight = dest
                        flag = P_ChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            P_ChangeSector(sector, crush)
                            // return crushed;
                        }
                        return pastdest
                    } else {
                        // COULD GET CRUSHED
                        lastpos = sector.floorheight
                        sector.floorheight += speed
                        flag = P_ChangeSector(sector, crush)
                        if (flag == true) {
                            if (crush == true)
                                return crushed
                            sector.floorheight = lastpos
                            P_ChangeSector(sector, crush)
                            return crushed
                        }
                    }
                }
            }
        }

        1 -> {
            // CEILING
            when (direction) {
                -1 -> {
                    // DOWN
                    if (sector.ceilingheight - speed < dest) {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight = dest
                        flag = P_ChangeSector(sector, crush)

                        if (flag == true) {
                            sector.ceilingheight = lastpos
                            P_ChangeSector(sector, crush)
                            // return crushed;
                        }
                        return pastdest
                    } else {
                        // COULD GET CRUSHED
                        lastpos = sector.ceilingheight
                        sector.ceilingheight -= speed
                        flag = P_ChangeSector(sector, crush)

                        if (flag == true) {
                            if (crush == true)
                                return crushed
                            sector.ceilingheight = lastpos
                            P_ChangeSector(sector, crush)
                            return crushed
                        }
                    }
                }

                1 -> {
                    // UP
                    if (sector.ceilingheight + speed > dest) {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight = dest
                        flag = P_ChangeSector(sector, crush)
                        if (flag == true) {
                            sector.ceilingheight = lastpos
                            P_ChangeSector(sector, crush)
                            // return crushed;
                        }
                        return pastdest
                    } else {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight += speed
                        flag = P_ChangeSector(sector, crush)
                        // UNUSED (#if 0 in vanilla)
                        // if (flag == true) {
                        //     sector.ceilingheight = lastpos
                        //     P_ChangeSector(sector, crush)
                        //     return crushed
                        // }
                    }
                }
            }
        }
    }
    return ok
}

//
// MOVE A FLOOR TO IT'S DESTINATION (UP OR DOWN)
//
fun T_MoveFloor(floor: floormove_t) {
    val res: Int

    res = T_MovePlane(floor.sector!!,
        floor.speed,
        floor.floordestheight,
        floor.crush, 0, floor.direction)

    if ((leveltime and 7) == 0)
        S_StartSound(floor.sector!!.soundorg, sfx_stnmov)

    if (res == pastdest) {
        floor.sector!!.specialdata = null

        if (floor.direction == 1) {
            when (floor.type) {
                donutRaise -> {
                    floor.sector!!.special = floor.newspecial
                    floor.sector!!.floorpic = floor.texture
                }

                else -> {}
            }
        } else if (floor.direction == -1) {
            when (floor.type) {
                lowerAndChange -> {
                    floor.sector!!.special = floor.newspecial
                    floor.sector!!.floorpic = floor.texture
                }

                else -> {}
            }
        }
        P_RemoveThinker(floor)

        S_StartSound(floor.sector!!.soundorg, sfx_pstop)
    }
}

//
// HANDLE FLOOR TYPES
//
fun EV_DoFloor(line: line_t, floortype: Int): Int {
    var secnum: Int
    var rtn: Int
    var i: Int
    var sec: sector_t
    var floor: floormove_t

    secnum = -1
    rtn = 0
    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]

        // ALREADY MOVING?  IF SO, KEEP GOING...
        if (sec.specialdata != null)
            continue

        // new floor thinker
        rtn = 1
        floor = floormove_t()
        P_AddThinker(floor)
        sec.specialdata = floor
        floor.function = { th -> T_MoveFloor(th as floormove_t) }
        floor.type = floortype
        floor.crush = false

        when (floortype) {
            lowerFloor -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    P_FindHighestFloorSurrounding(sec)
            }

            lowerFloorToLowest -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    P_FindLowestFloorSurrounding(sec)
            }

            turboLower -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED * 4
                floor.floordestheight =
                    P_FindHighestFloorSurrounding(sec)
                if (floor.floordestheight != sec.floorheight)
                    floor.floordestheight += 8 * FRACUNIT
            }

            raiseFloorCrush,  // (C falls through into raiseFloor)
            raiseFloor -> {
                if (floortype == raiseFloorCrush)
                    floor.crush = true
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    P_FindLowestCeilingSurrounding(sec)
                if (floor.floordestheight > sec.ceilingheight)
                    floor.floordestheight = sec.ceilingheight
                floor.floordestheight -= (8 * FRACUNIT) *
                    (if (floortype == raiseFloorCrush) 1 else 0)
            }

            raiseFloorTurbo -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED * 4
                floor.floordestheight =
                    P_FindNextHighestFloor(sec, sec.floorheight)
            }

            raiseFloorToNearest -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    P_FindNextHighestFloor(sec, sec.floorheight)
            }

            raiseFloor24 -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    24 * FRACUNIT
            }

            raiseFloor512 -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    512 * FRACUNIT
            }

            raiseFloor24AndChange -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    24 * FRACUNIT
                sec.floorpic = line.frontsector!!.floorpic
                sec.special = line.frontsector!!.special
            }

            raiseToTexture -> {
                var minsize = MAXINT
                var side: side_t

                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                i = 0
                while (i < sec.linecount) {
                    if (twoSided(secnum, i) != 0) {
                        side = getSide(secnum, i, 0)
                        if (side.bottomtexture >= 0)
                            if (textureheight[side.bottomtexture] <
                                minsize)
                                minsize =
                                    textureheight[side.bottomtexture]
                        side = getSide(secnum, i, 1)
                        if (side.bottomtexture >= 0)
                            if (textureheight[side.bottomtexture] <
                                minsize)
                                minsize =
                                    textureheight[side.bottomtexture]
                    }
                    i++
                }
                floor.floordestheight =
                    floor.sector!!.floorheight + minsize
            }

            lowerAndChange -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    P_FindLowestFloorSurrounding(sec)
                floor.texture = sec.floorpic

                i = 0
                while (i < sec.linecount) {
                    if (twoSided(secnum, i) != 0) {
                        if (getSide(secnum, i, 0).sector!!.index == secnum) {
                            sec = getSector(secnum, i, 1)

                            if (sec.floorheight == floor.floordestheight) {
                                floor.texture = sec.floorpic
                                floor.newspecial = sec.special
                                break
                            }
                        } else {
                            sec = getSector(secnum, i, 0)

                            if (sec.floorheight == floor.floordestheight) {
                                floor.texture = sec.floorpic
                                floor.newspecial = sec.special
                                break
                            }
                        }
                    }
                    i++
                }
            }

            else -> {}
        }
    }
    return rtn
}

//
// BUILD A STAIRCASE!
//
fun EV_BuildStairs(line: line_t, type: Int): Int {
    var secnum: Int
    var height: Int
    var i: Int
    var newsecnum: Int
    var texture: Int
    var ok: Int
    var rtn: Int

    var sec: sector_t
    var tsec: sector_t

    var floor: floormove_t

    var stairsize: fixed_t = 0
    var speed: fixed_t = 0

    secnum = -1
    rtn = 0
    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]

        // ALREADY MOVING?  IF SO, KEEP GOING...
        if (sec.specialdata != null)
            continue

        // new floor thinker
        rtn = 1
        floor = floormove_t()
        P_AddThinker(floor)
        sec.specialdata = floor
        floor.function = { th -> T_MoveFloor(th as floormove_t) }
        floor.direction = 1
        floor.sector = sec
        when (type) {
            build8 -> {
                speed = FLOORSPEED / 4
                stairsize = 8 * FRACUNIT
            }

            turbo16 -> {
                speed = FLOORSPEED * 4
                stairsize = 16 * FRACUNIT
            }
        }
        floor.speed = speed
        height = sec.floorheight + stairsize
        floor.floordestheight = height

        texture = sec.floorpic

        // Find next sector to raise
        // 1.	Find 2-sided line with same sector side[0]
        // 2.	Other side is the next sector to raise
        do {
            ok = 0
            i = 0
            while (i < sec.linecount) {
                if ((sec.lines[i]!!.flags and ML_TWOSIDED) == 0) {
                    i++
                    continue
                }

                tsec = sec.lines[i]!!.frontsector!!
                newsecnum = tsec.index

                if (secnum != newsecnum) {
                    i++
                    continue
                }

                tsec = sec.lines[i]!!.backsector!!
                newsecnum = tsec.index

                if (tsec.floorpic != texture) {
                    i++
                    continue
                }

                height += stairsize

                if (tsec.specialdata != null) {
                    i++
                    continue
                }

                sec = tsec
                secnum = newsecnum
                floor = floormove_t()

                P_AddThinker(floor)

                sec.specialdata = floor
                floor.function = { th -> T_MoveFloor(th as floormove_t) }
                floor.direction = 1
                floor.sector = sec
                floor.speed = speed
                floor.floordestheight = height
                ok = 1
                break
            }
        } while (ok != 0)
    }
    return rtn
}
