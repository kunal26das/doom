
package doom.engine.world.movers

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_PSTART
import doom.engine.audio.SFX_PSTOP
import doom.engine.audio.SFX_STNMOV
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.geometry.FRACUNIT
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRandom
import doom.engine.simulation.pRemoveThinker
import doom.engine.simulation.leveltime
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.sectors
import doom.engine.world.sides
import doom.engine.world.specials.pFindHighestFloorSurrounding
import doom.engine.world.specials.pFindLowestFloorSurrounding
import doom.engine.world.specials.pFindNextHighestFloor
import doom.engine.world.specials.pFindSectorFromLineTag

internal const val UP = 0
internal const val DOWN = 1
internal const val WAITING = 2
internal const val IN_STASIS = 3

internal const val PERPETUAL_RAISE = 0
internal const val DOWN_WAIT_UP_STAY = 1
internal const val RAISE_AND_CHANGE = 2
internal const val RAISE_TO_NEAREST_AND_CHANGE = 3
internal const val BLAZE_DWUS = 4

internal const val PLATWAIT = 3
internal const val PLATSPEED = FRACUNIT
internal const val MAXPLATS = 30

internal var DoomEngineCore.activeplats
    get() = statePlatformMotion.activeplats
    set(value) { statePlatformMotion.activeplats = value }

internal fun DoomEngineCore.tPlatRaise(plat: PlatformMover) {
    val res: Int

    when (plat.status) {
        UP -> {
            res = tMovePlane(plat.sector!!,
                plat.speed,
                plat.high,
                plat.crush, 0, 1)

            if (plat.type == RAISE_AND_CHANGE
                || plat.type == RAISE_TO_NEAREST_AND_CHANGE) {
                if ((leveltime and 7) == 0)
                    sStartSound(plat.sector!!.soundorg, SFX_STNMOV)
            }

            if (res == CRUSHED && (!plat.crush)) {
                plat.count = plat.wait
                plat.status = DOWN
                sStartSound(plat.sector!!.soundorg, SFX_PSTART)
            } else {
                if (res == PASTDEST) {
                    plat.count = plat.wait
                    plat.status = WAITING
                    sStartSound(plat.sector!!.soundorg, SFX_PSTOP)

                    when (plat.type) {
                        BLAZE_DWUS,
                        DOWN_WAIT_UP_STAY ->
                            pRemoveActivePlat(plat)

                        RAISE_AND_CHANGE,
                        RAISE_TO_NEAREST_AND_CHANGE ->
                            pRemoveActivePlat(plat)

                        else -> {}
                    }
                }
            }
        }

        DOWN -> {
            res = tMovePlane(plat.sector!!, plat.speed, plat.low, false, 0, -1)

            if (res == PASTDEST) {
                plat.count = plat.wait
                plat.status = WAITING
                sStartSound(plat.sector!!.soundorg, SFX_PSTOP)
            }
        }

        WAITING -> {
            plat.count--
            if (plat.count == 0) {
                if (plat.sector!!.floorheight == plat.low)
                    plat.status = UP
                else
                    plat.status = DOWN
                sStartSound(plat.sector!!.soundorg, SFX_PSTART)
            }
        }

        IN_STASIS -> {}
    }
}

internal fun DoomEngineCore.evDoPlat(line: MapLine, type: Int, amount: Int): Int {
    var plat: PlatformMover
    var secnum: Int
    var rtn: Int
    var sec: Sector

    secnum = -1
    rtn = 0

    when (type) {
        PERPETUAL_RAISE ->
            pActivateInStasis(line.tag)

        else -> {}
    }

    while (true) {
        secnum = pFindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]

        if (sec.specialdata != null)
            continue

        rtn = 1
        plat = PlatformMover()
        pAddThinker(plat)

        plat.type = type
        plat.sector = sec
        plat.sector!!.specialdata = plat
        plat.function = { th -> tPlatRaise(th as PlatformMover) }
        plat.crush = false
        plat.tag = line.tag

        when (type) {
            RAISE_TO_NEAREST_AND_CHANGE -> {
                plat.speed = PLATSPEED / 2
                sec.floorpic = sides[line.sidenum[0]].sector!!.floorpic
                plat.high = pFindNextHighestFloor(sec, sec.floorheight)
                plat.wait = 0
                plat.status = UP
                sec.special = 0

                sStartSound(sec.soundorg, SFX_STNMOV)
            }

            RAISE_AND_CHANGE -> {
                plat.speed = PLATSPEED / 2
                sec.floorpic = sides[line.sidenum[0]].sector!!.floorpic
                plat.high = sec.floorheight + amount * FRACUNIT
                plat.wait = 0
                plat.status = UP

                sStartSound(sec.soundorg, SFX_STNMOV)
            }

            DOWN_WAIT_UP_STAY -> {
                plat.speed = PLATSPEED * 4
                plat.low = pFindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = sec.floorheight
                plat.wait = 35 * PLATWAIT
                plat.status = DOWN
                sStartSound(sec.soundorg, SFX_PSTART)
            }

            BLAZE_DWUS -> {
                plat.speed = PLATSPEED * 8
                plat.low = pFindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = sec.floorheight
                plat.wait = 35 * PLATWAIT
                plat.status = DOWN
                sStartSound(sec.soundorg, SFX_PSTART)
            }

            PERPETUAL_RAISE -> {
                plat.speed = PLATSPEED
                plat.low = pFindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = pFindHighestFloorSurrounding(sec)

                if (plat.high < sec.floorheight)
                    plat.high = sec.floorheight

                plat.wait = 35 * PLATWAIT
                plat.status = pRandom() and 1

                sStartSound(sec.soundorg, SFX_PSTART)
            }
        }
        pAddActivePlat(plat)
    }
    return rtn
}

internal fun DoomEngineCore.pActivateInStasis(tag: Int) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (activeplats[i] != null
            && activeplats[i]!!.tag == tag
            && activeplats[i]!!.status == IN_STASIS) {
            activeplats[i]!!.status = activeplats[i]!!.oldstatus
            activeplats[i]!!.function =
                { th -> tPlatRaise(th as PlatformMover) }
        }
        i++
    }
}

internal fun DoomEngineCore.evStopPlat(line: MapLine) {
    var j: Int

    j = 0
    while (j < MAXPLATS) {
        if (activeplats[j] != null
            && (activeplats[j]!!.status != IN_STASIS)
            && (activeplats[j]!!.tag == line.tag)) {
            activeplats[j]!!.oldstatus = activeplats[j]!!.status
            activeplats[j]!!.status = IN_STASIS
            activeplats[j]!!.function = null
        }
        j++
    }
}

internal fun DoomEngineCore.pAddActivePlat(plat: PlatformMover) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (activeplats[i] == null) {
            activeplats[i] = plat
            return
        }
        i++
    }
    iError("P_AddActivePlat: no more plats!")
}

internal fun DoomEngineCore.pRemoveActivePlat(plat: PlatformMover) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (plat === activeplats[i]) {
            activeplats[i]!!.sector!!.specialdata = null
            pRemoveThinker(activeplats[i]!!)
            activeplats[i] = null

            return
        }
        i++
    }
    iError("P_RemoveActivePlat: can't find plat!")
}
