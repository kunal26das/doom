
package doom.engine.world.movers

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_PSTOP
import doom.engine.audio.SFX_STNMOV
import doom.engine.core.DoomEngineCore
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.resources.textureheight
import doom.engine.resources.MAXINT
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRemoveThinker
import doom.engine.simulation.leveltime
import doom.engine.world.ML_TWOSIDED
import doom.engine.world.MapLine
import doom.engine.world.MapSide
import doom.engine.world.Sector
import doom.engine.world.collision.pChangeSector
import doom.engine.world.sectors
import doom.engine.world.specials.pFindHighestFloorSurrounding
import doom.engine.world.specials.pFindLowestCeilingSurrounding
import doom.engine.world.specials.pFindLowestFloorSurrounding
import doom.engine.world.specials.pFindNextHighestFloor
import doom.engine.world.specials.pFindSectorFromLineTag
import doom.engine.world.specials.getSector
import doom.engine.world.specials.getSide
import doom.engine.world.specials.twoSided

internal const val LOWER_FLOOR = 0

internal const val LOWER_FLOOR_TO_LOWEST = 1

internal const val TURBO_LOWER = 2

internal const val RAISE_FLOOR = 3

internal const val RAISE_FLOOR_TO_NEAREST = 4

internal const val RAISE_TO_TEXTURE = 5

internal const val LOWER_AND_CHANGE = 6

internal const val RAISE_FLOOR24 = 7
internal const val RAISE_FLOOR24_AND_CHANGE = 8
internal const val RAISE_FLOOR_CRUSH = 9

internal const val RAISE_FLOOR_TURBO = 10
internal const val DONUT_RAISE = 11
internal const val RAISE_FLOOR512 = 12

internal const val BUILD8 = 0
internal const val TURBO16 = 1

internal const val FLOORSPEED = FRACUNIT

internal const val OK = 0
internal const val CRUSHED = 1
internal const val PASTDEST = 2


internal fun DoomEngineCore.tMovePlane(
    sector: Sector,
    speed: FixedPoint,
    dest: FixedPoint,
    crush: Boolean,
    floorOrCeiling: Int,
    direction: Int,
): Int {
    val flag: Boolean
    val lastpos: FixedPoint

    when (floorOrCeiling) {
        0 -> {
            when (direction) {
                -1 -> {
                    if (sector.floorheight - speed < dest) {
                        lastpos = sector.floorheight
                        sector.floorheight = dest
                        flag = pChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            pChangeSector(sector, crush)
                        }
                        return PASTDEST
                    } else {
                        lastpos = sector.floorheight
                        sector.floorheight -= speed
                        flag = pChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            pChangeSector(sector, crush)
                            return CRUSHED
                        }
                    }
                }

                1 -> {
                    if (sector.floorheight + speed > dest) {
                        lastpos = sector.floorheight
                        sector.floorheight = dest
                        flag = pChangeSector(sector, crush)
                        if (flag == true) {
                            sector.floorheight = lastpos
                            pChangeSector(sector, crush)
                        }
                        return PASTDEST
                    } else {
                        lastpos = sector.floorheight
                        sector.floorheight += speed
                        flag = pChangeSector(sector, crush)
                        if (flag == true) {
                            if (crush == true)
                                return CRUSHED
                            sector.floorheight = lastpos
                            pChangeSector(sector, crush)
                            return CRUSHED
                        }
                    }
                }
            }
        }

        1 -> {
            when (direction) {
                -1 -> {
                    if (sector.ceilingheight - speed < dest) {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight = dest
                        flag = pChangeSector(sector, crush)

                        if (flag == true) {
                            sector.ceilingheight = lastpos
                            pChangeSector(sector, crush)
                        }
                        return PASTDEST
                    } else {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight -= speed
                        flag = pChangeSector(sector, crush)

                        if (flag == true) {
                            if (crush == true)
                                return CRUSHED
                            sector.ceilingheight = lastpos
                            pChangeSector(sector, crush)
                            return CRUSHED
                        }
                    }
                }

                1 -> {
                    if (sector.ceilingheight + speed > dest) {
                        lastpos = sector.ceilingheight
                        sector.ceilingheight = dest
                        flag = pChangeSector(sector, crush)
                        if (flag == true) {
                            sector.ceilingheight = lastpos
                            pChangeSector(sector, crush)
                        }
                        return PASTDEST
                    } else {
                        sector.ceilingheight += speed
                        pChangeSector(sector, crush)
                    }
                }
            }
        }
    }
    return OK
}

internal fun DoomEngineCore.tMoveFloor(floor: FloorMover) {
    val res: Int

    res = tMovePlane(floor.sector!!,
        floor.speed,
        floor.floordestheight,
        floor.crush, 0, floor.direction)

    if ((leveltime and 7) == 0)
        sStartSound(floor.sector!!.soundorg, SFX_STNMOV)

    if (res == PASTDEST) {
        floor.sector!!.specialdata = null

        if (floor.direction == 1) {
            when (floor.type) {
                DONUT_RAISE -> {
                    floor.sector!!.special = floor.newspecial
                    floor.sector!!.floorpic = floor.texture
                }

                else -> {}
            }
        } else if (floor.direction == -1) {
            when (floor.type) {
                LOWER_AND_CHANGE -> {
                    floor.sector!!.special = floor.newspecial
                    floor.sector!!.floorpic = floor.texture
                }

                else -> {}
            }
        }
        pRemoveThinker(floor)

        sStartSound(floor.sector!!.soundorg, SFX_PSTOP)
    }
}

internal fun DoomEngineCore.evDoFloor(line: MapLine, floortype: Int): Int {
    var secnum: Int
    var rtn: Int
    var i: Int
    var sec: Sector
    var floor: FloorMover

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
        floor = FloorMover()
        pAddThinker(floor)
        sec.specialdata = floor
        floor.function = { th -> tMoveFloor(th as FloorMover) }
        floor.type = floortype
        floor.crush = false

        when (floortype) {
            LOWER_FLOOR -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    pFindHighestFloorSurrounding(sec)
            }

            LOWER_FLOOR_TO_LOWEST -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    pFindLowestFloorSurrounding(sec)
            }

            TURBO_LOWER -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED * 4
                floor.floordestheight =
                    pFindHighestFloorSurrounding(sec)
                if (floor.floordestheight != sec.floorheight)
                    floor.floordestheight += 8 * FRACUNIT
            }

            RAISE_FLOOR_CRUSH,
            RAISE_FLOOR -> {
                if (floortype == RAISE_FLOOR_CRUSH)
                    floor.crush = true
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    pFindLowestCeilingSurrounding(sec)
                if (floor.floordestheight > sec.ceilingheight)
                    floor.floordestheight = sec.ceilingheight
                floor.floordestheight -= (8 * FRACUNIT) *
                    (if (floortype == RAISE_FLOOR_CRUSH) 1 else 0)
            }

            RAISE_FLOOR_TURBO -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED * 4
                floor.floordestheight =
                    pFindNextHighestFloor(sec, sec.floorheight)
            }

            RAISE_FLOOR_TO_NEAREST -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    pFindNextHighestFloor(sec, sec.floorheight)
            }

            RAISE_FLOOR24 -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    24 * FRACUNIT
            }

            RAISE_FLOOR512 -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    512 * FRACUNIT
            }

            RAISE_FLOOR24_AND_CHANGE -> {
                floor.direction = 1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight = floor.sector!!.floorheight +
                    24 * FRACUNIT
                sec.floorpic = line.frontsector!!.floorpic
                sec.special = line.frontsector!!.special
            }

            RAISE_TO_TEXTURE -> {
                var minsize = MAXINT
                var side: MapSide

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

            LOWER_AND_CHANGE -> {
                floor.direction = -1
                floor.sector = sec
                floor.speed = FLOORSPEED
                floor.floordestheight =
                    pFindLowestFloorSurrounding(sec)
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

internal fun DoomEngineCore.evBuildStairs(line: MapLine, type: Int): Int {
    var secnum: Int
    var height: Int
    var i: Int
    var newsecnum: Int
    var texture: Int
    var ok: Int
    var rtn: Int

    var sec: Sector
    var tsec: Sector

    var floor: FloorMover

    var stairsize: FixedPoint = 0
    var speed: FixedPoint = 0

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
        floor = FloorMover()
        pAddThinker(floor)
        sec.specialdata = floor
        floor.function = { th -> tMoveFloor(th as FloorMover) }
        floor.direction = 1
        floor.sector = sec
        when (type) {
            BUILD8 -> {
                speed = FLOORSPEED / 4
                stairsize = 8 * FRACUNIT
            }

            TURBO16 -> {
                speed = FLOORSPEED * 4
                stairsize = 16 * FRACUNIT
            }
        }
        floor.speed = speed
        height = sec.floorheight + stairsize
        floor.floordestheight = height

        texture = sec.floorpic

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
                floor = FloorMover()

                pAddThinker(floor)

                sec.specialdata = floor
                floor.function = { th -> tMoveFloor(th as FloorMover) }
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
