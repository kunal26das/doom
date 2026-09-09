// Port of linuxdoom-1.10 p_ceilng.c -- Ceiling aninmation (lowering, crushing, raising).
// (Also owns the P_CEILNG thinker struct + ceiling_e consts and
//  CEILSPEED/CEILWAIT/MAXCEILINGS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: ceiling_e
internal const val lowerToFloor = 0
internal const val raiseToHighest = 1
internal const val lowerAndCrush = 2
internal const val crushAndRaise = 3
internal const val fastCrushAndRaise = 4
internal const val silentCrushAndRaise = 5


internal const val CEILSPEED = FRACUNIT
internal const val CEILWAIT = 150
internal const val MAXCEILINGS = 30

//
// CEILINGS
//

internal var DoomEngineCore.activeceilings
    get() = stateCeiling.activeceilings
    set(value) { stateCeiling.activeceilings = value }

//
// T_MoveCeiling
//

internal fun DoomEngineCore.T_MoveCeiling(ceiling: ceiling_t) {
    val res: Int

    when (ceiling.direction) {
        0 -> {
            // IN STASIS
        }

        1 -> {
            // UP
            res = T_MovePlane(ceiling.sector!!,
                ceiling.speed,
                ceiling.topheight,
                false, 1, ceiling.direction)

            if ((leveltime and 7) == 0) {
                when (ceiling.type) {
                    silentCrushAndRaise -> {}
                    else -> {
                        S_StartSound(ceiling.sector!!.soundorg, sfx_stnmov)
                        // ?
                    }
                }
            }

            if (res == pastdest) {
                when (ceiling.type) {
                    raiseToHighest ->
                        P_RemoveActiveCeiling(ceiling)

                    silentCrushAndRaise -> {
                        S_StartSound(ceiling.sector!!.soundorg, sfx_pstop)
                        // (C falls through into crushAndRaise)
                        ceiling.direction = -1
                    }

                    fastCrushAndRaise,
                    crushAndRaise ->
                        ceiling.direction = -1

                    else -> {}
                }
            }
        }

        -1 -> {
            // DOWN
            res = T_MovePlane(ceiling.sector!!,
                ceiling.speed,
                ceiling.bottomheight,
                ceiling.crush, 1, ceiling.direction)

            if ((leveltime and 7) == 0) {
                when (ceiling.type) {
                    silentCrushAndRaise -> {}
                    else ->
                        S_StartSound(ceiling.sector!!.soundorg, sfx_stnmov)
                }
            }

            if (res == pastdest) {
                when (ceiling.type) {
                    silentCrushAndRaise -> {
                        S_StartSound(ceiling.sector!!.soundorg, sfx_pstop)
                        // (C falls through into crushAndRaise)
                        ceiling.speed = CEILSPEED
                        ceiling.direction = 1
                    }

                    crushAndRaise -> {
                        ceiling.speed = CEILSPEED
                        // (C falls through into fastCrushAndRaise)
                        ceiling.direction = 1
                    }

                    fastCrushAndRaise ->
                        ceiling.direction = 1

                    lowerAndCrush,
                    lowerToFloor ->
                        P_RemoveActiveCeiling(ceiling)

                    else -> {}
                }
            } else {  // ( res != pastdest )
                if (res == crushed) {
                    when (ceiling.type) {
                        silentCrushAndRaise,
                        crushAndRaise,
                        lowerAndCrush ->
                            ceiling.speed = CEILSPEED / 8

                        else -> {}
                    }
                }
            }
        }
    }
}

//
// EV_DoCeiling
// Move a ceiling up/down and all around!
//
internal fun DoomEngineCore.EV_DoCeiling(line: line_t, type: Int): Int {
    var secnum: Int
    var rtn: Int
    var sec: sector_t
    var ceiling: ceiling_t

    secnum = -1
    rtn = 0

    //	Reactivate in-stasis ceilings...for certain types.
    when (type) {
        fastCrushAndRaise,
        silentCrushAndRaise,
        crushAndRaise ->
            P_ActivateInStasisCeiling(line)

        else -> {}
    }

    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]
        if (sec.specialdata != null)
            continue

        // new door thinker
        rtn = 1
        ceiling = ceiling_t()
        P_AddThinker(ceiling)
        sec.specialdata = ceiling
        ceiling.function = { th -> T_MoveCeiling(th as ceiling_t) }
        ceiling.sector = sec
        ceiling.crush = false

        when (type) {
            fastCrushAndRaise -> {
                ceiling.crush = true
                ceiling.topheight = sec.ceilingheight
                ceiling.bottomheight = sec.floorheight + (8 * FRACUNIT)
                ceiling.direction = -1
                ceiling.speed = CEILSPEED * 2
            }

            silentCrushAndRaise,
            crushAndRaise,
            // (C falls through into lowerAndCrush/lowerToFloor)
            lowerAndCrush,
            lowerToFloor -> {
                if (type == silentCrushAndRaise || type == crushAndRaise) {
                    ceiling.crush = true
                    ceiling.topheight = sec.ceilingheight
                }
                ceiling.bottomheight = sec.floorheight
                if (type != lowerToFloor)
                    ceiling.bottomheight += 8 * FRACUNIT
                ceiling.direction = -1
                ceiling.speed = CEILSPEED
            }

            raiseToHighest -> {
                ceiling.topheight = P_FindHighestCeilingSurrounding(sec)
                ceiling.direction = 1
                ceiling.speed = CEILSPEED
            }
        }

        ceiling.tag = sec.tag
        ceiling.type = type
        P_AddActiveCeiling(ceiling)
    }
    return rtn
}

//
// Add an active ceiling
//
internal fun DoomEngineCore.P_AddActiveCeiling(c: ceiling_t) {
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

//
// Remove a ceiling's thinker
//
internal fun DoomEngineCore.P_RemoveActiveCeiling(c: ceiling_t) {
    var i: Int

    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] === c) {
            activeceilings[i]!!.sector!!.specialdata = null
            P_RemoveThinker(activeceilings[i]!!)
            activeceilings[i] = null
            break
        }
        i++
    }
}

//
// Restart a ceiling that's in-stasis
//
internal fun DoomEngineCore.P_ActivateInStasisCeiling(line: line_t) {
    var i: Int

    i = 0
    while (i < MAXCEILINGS) {
        if (activeceilings[i] != null
            && (activeceilings[i]!!.tag == line.tag)
            && (activeceilings[i]!!.direction == 0)) {
            activeceilings[i]!!.direction = activeceilings[i]!!.olddirection
            activeceilings[i]!!.function =
                { th -> T_MoveCeiling(th as ceiling_t) }
        }
        i++
    }
}

//
// EV_CeilingCrushStop
// Stop a ceiling from crushing!
//
internal fun DoomEngineCore.EV_CeilingCrushStop(line: line_t): Int {
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
            activeceilings[i]!!.direction = 0  // in-stasis
            rtn = 1
        }
        i++
    }

    return rtn
}
