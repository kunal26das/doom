
package doom.engine.world.movers

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_PSTOP
import doom.engine.audio.SFX_STNMOV
import doom.engine.core.DoomEngineCore
import doom.engine.geometry.FRACUNIT
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRemoveThinker
import doom.engine.simulation.leveltime
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.sectors
import doom.engine.world.specials.pFindHighestCeilingSurrounding
import doom.engine.world.specials.pFindSectorFromLineTag

internal const val LOWER_TO_FLOOR = 0
internal const val RAISE_TO_HIGHEST = 1
internal const val LOWER_AND_CRUSH = 2
internal const val CRUSH_AND_RAISE = 3
internal const val FAST_CRUSH_AND_RAISE = 4
internal const val SILENT_CRUSH_AND_RAISE = 5

internal const val CEILSPEED = FRACUNIT
internal const val CEILWAIT = 150
internal const val MAXCEILINGS = 30


internal var DoomEngineCore.activeceilings
    get() = stateCeiling.activeceilings
    set(value) { stateCeiling.activeceilings = value }


internal fun DoomEngineCore.tMoveCeiling(ceiling: CeilingMover) {
    val res: Int

    when (ceiling.direction) {
        0 -> {
        }

        1 -> {
            res = tMovePlane(ceiling.sector!!,
                ceiling.speed,
                ceiling.topheight,
                false, 1, ceiling.direction)

            if ((leveltime and 7) == 0) {
                when (ceiling.type) {
                    SILENT_CRUSH_AND_RAISE -> {}
                    else -> {
                        sStartSound(ceiling.sector!!.soundorg, SFX_STNMOV)
                    }
                }
            }

            if (res == PASTDEST) {
                when (ceiling.type) {
                    RAISE_TO_HIGHEST ->
                        pRemoveActiveCeiling(ceiling)

                    SILENT_CRUSH_AND_RAISE -> {
                        sStartSound(ceiling.sector!!.soundorg, SFX_PSTOP)
                        ceiling.direction = -1
                    }

                    FAST_CRUSH_AND_RAISE,
                    CRUSH_AND_RAISE ->
                        ceiling.direction = -1

                    else -> {}
                }
            }
        }

        -1 -> {
            res = tMovePlane(ceiling.sector!!,
                ceiling.speed,
                ceiling.bottomheight,
                ceiling.crush, 1, ceiling.direction)

            if ((leveltime and 7) == 0) {
                when (ceiling.type) {
                    SILENT_CRUSH_AND_RAISE -> {}
                    else ->
                        sStartSound(ceiling.sector!!.soundorg, SFX_STNMOV)
                }
            }

            if (res == PASTDEST) {
                when (ceiling.type) {
                    SILENT_CRUSH_AND_RAISE -> {
                        sStartSound(ceiling.sector!!.soundorg, SFX_PSTOP)
                        ceiling.speed = CEILSPEED
                        ceiling.direction = 1
                    }

                    CRUSH_AND_RAISE -> {
                        ceiling.speed = CEILSPEED
                        ceiling.direction = 1
                    }

                    FAST_CRUSH_AND_RAISE ->
                        ceiling.direction = 1

                    LOWER_AND_CRUSH,
                    LOWER_TO_FLOOR ->
                        pRemoveActiveCeiling(ceiling)

                    else -> {}
                }
            } else {
                if (res == CRUSHED) {
                    when (ceiling.type) {
                        SILENT_CRUSH_AND_RAISE,
                        CRUSH_AND_RAISE,
                        LOWER_AND_CRUSH ->
                            ceiling.speed = CEILSPEED / 8

                        else -> {}
                    }
                }
            }
        }
    }
}

internal fun DoomEngineCore.evDoCeiling(line: MapLine, type: Int): Int {
    var secnum: Int
    var rtn: Int
    var sec: Sector
    var ceiling: CeilingMover

    secnum = -1
    rtn = 0

    when (type) {
        FAST_CRUSH_AND_RAISE,
        SILENT_CRUSH_AND_RAISE,
        CRUSH_AND_RAISE ->
            pActivateInStasisCeiling(line)

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
        ceiling = CeilingMover()
        pAddThinker(ceiling)
        sec.specialdata = ceiling
        ceiling.function = { th -> tMoveCeiling(th as CeilingMover) }
        ceiling.sector = sec
        ceiling.crush = false

        when (type) {
            FAST_CRUSH_AND_RAISE -> {
                ceiling.crush = true
                ceiling.topheight = sec.ceilingheight
                ceiling.bottomheight = sec.floorheight + (8 * FRACUNIT)
                ceiling.direction = -1
                ceiling.speed = CEILSPEED * 2
            }

            SILENT_CRUSH_AND_RAISE,
            CRUSH_AND_RAISE,
            LOWER_AND_CRUSH,
            LOWER_TO_FLOOR -> {
                if (type == SILENT_CRUSH_AND_RAISE || type == CRUSH_AND_RAISE) {
                    ceiling.crush = true
                    ceiling.topheight = sec.ceilingheight
                }
                ceiling.bottomheight = sec.floorheight
                if (type != LOWER_TO_FLOOR)
                    ceiling.bottomheight += 8 * FRACUNIT
                ceiling.direction = -1
                ceiling.speed = CEILSPEED
            }

            RAISE_TO_HIGHEST -> {
                ceiling.topheight = pFindHighestCeilingSurrounding(sec)
                ceiling.direction = 1
                ceiling.speed = CEILSPEED
            }
        }

        ceiling.tag = sec.tag
        ceiling.type = type
        pAddActiveCeiling(ceiling)
    }
    return rtn
}

internal fun DoomEngineCore.pAddActiveCeiling(c: CeilingMover) {
    var i: Int

    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] == null) {
            activeceilings[i] = c
            return
        }
        i++
    }
}

internal fun DoomEngineCore.pRemoveActiveCeiling(c: CeilingMover) {
    var i: Int

    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] === c) {
            activeceilings[i]!!.sector!!.specialdata = null
            pRemoveThinker(activeceilings[i]!!)
            activeceilings[i] = null
            break
        }
        i++
    }
}

internal fun DoomEngineCore.pActivateInStasisCeiling(line: MapLine) {
    var i: Int

    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] != null
            && (activeceilings[i]!!.tag == line.tag)
            && (activeceilings[i]!!.direction == 0)) {
            activeceilings[i]!!.direction = activeceilings[i]!!.olddirection
            activeceilings[i]!!.function =
                { th -> tMoveCeiling(th as CeilingMover) }
        }
        i++
    }
}

internal fun DoomEngineCore.evCeilingCrushStop(line: MapLine): Int {
    var i: Int
    var rtn: Int

    rtn = 0
    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] != null
            && (activeceilings[i]!!.tag == line.tag)
            && (activeceilings[i]!!.direction != 0)) {
            activeceilings[i]!!.olddirection = activeceilings[i]!!.direction
            activeceilings[i]!!.function = null
            activeceilings[i]!!.direction = 0
            rtn = 1
        }
        i++
    }

    return rtn
}
