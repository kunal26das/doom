// Port of linuxdoom-1.10 p_plats.c -- Plats (i.e. elevator platforms) code,
// raising/lowering.
// (Also owns the P_PLATS thinker struct + plat_e/plattype_e consts and
//  PLATWAIT/PLATSPEED/MAXPLATS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// From p_spec.h: plat_e
const val up = 0
const val down = 1
const val waiting = 2
const val in_stasis = 3

// From p_spec.h: plattype_e
const val perpetualRaise = 0
const val downWaitUpStay = 1
const val raiseAndChange = 2
const val raiseToNearestAndChange = 3
const val blazeDWUS = 4

// From p_spec.h: plat_t
class plat_t : thinker_t() {
    var sector: sector_t? = null
    var speed: fixed_t = 0
    var low: fixed_t = 0
    var high: fixed_t = 0
    var wait = 0
    var count = 0
    var status = 0  // plat_e
    var oldstatus = 0  // plat_e
    var crush = false
    var tag = 0
    var type = 0  // plattype_e
}

const val PLATWAIT = 3
const val PLATSPEED = FRACUNIT
const val MAXPLATS = 30

var activeplats = arrayOfNulls<plat_t>(MAXPLATS)

//
// Move a plat up and down
//
fun T_PlatRaise(plat: plat_t) {
    val res: Int

    when (plat.status) {
        up -> {
            res = T_MovePlane(plat.sector!!,
                plat.speed,
                plat.high,
                plat.crush, 0, 1)

            if (plat.type == raiseAndChange
                || plat.type == raiseToNearestAndChange) {
                if ((leveltime and 7) == 0)
                    S_StartSound(plat.sector!!.soundorg, sfx_stnmov)
            }

            if (res == crushed && (!plat.crush)) {
                plat.count = plat.wait
                plat.status = down
                S_StartSound(plat.sector!!.soundorg, sfx_pstart)
            } else {
                if (res == pastdest) {
                    plat.count = plat.wait
                    plat.status = waiting
                    S_StartSound(plat.sector!!.soundorg, sfx_pstop)

                    when (plat.type) {
                        blazeDWUS,
                        downWaitUpStay ->
                            P_RemoveActivePlat(plat)

                        raiseAndChange,
                        raiseToNearestAndChange ->
                            P_RemoveActivePlat(plat)

                        else -> {}
                    }
                }
            }
        }

        down -> {
            res = T_MovePlane(plat.sector!!, plat.speed, plat.low, false, 0, -1)

            if (res == pastdest) {
                plat.count = plat.wait
                plat.status = waiting
                S_StartSound(plat.sector!!.soundorg, sfx_pstop)
            }
        }

        waiting -> {
            plat.count--
            if (plat.count == 0) {
                if (plat.sector!!.floorheight == plat.low)
                    plat.status = up
                else
                    plat.status = down
                S_StartSound(plat.sector!!.soundorg, sfx_pstart)
            }
            // (C falls through into in_stasis: break -- nothing more happens)
        }

        in_stasis -> {}
    }
}

//
// Do Platforms
//  "amount" is only used for SOME platforms.
//
fun EV_DoPlat(line: line_t, type: Int, amount: Int): Int {
    var plat: plat_t
    var secnum: Int
    var rtn: Int
    var sec: sector_t

    secnum = -1
    rtn = 0

    //	Activate all <type> plats that are in_stasis
    when (type) {
        perpetualRaise ->
            P_ActivateInStasis(line.tag)

        else -> {}
    }

    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]

        if (sec.specialdata != null)
            continue

        // Find lowest & highest floors around sector
        rtn = 1
        plat = plat_t()
        P_AddThinker(plat)

        plat.type = type
        plat.sector = sec
        plat.sector!!.specialdata = plat
        plat.function = { th -> T_PlatRaise(th as plat_t) }
        plat.crush = false
        plat.tag = line.tag

        when (type) {
            raiseToNearestAndChange -> {
                plat.speed = PLATSPEED / 2
                sec.floorpic = sides[line.sidenum[0]].sector!!.floorpic
                plat.high = P_FindNextHighestFloor(sec, sec.floorheight)
                plat.wait = 0
                plat.status = up
                // NO MORE DAMAGE, IF APPLICABLE
                sec.special = 0

                S_StartSound(sec.soundorg, sfx_stnmov)
            }

            raiseAndChange -> {
                plat.speed = PLATSPEED / 2
                sec.floorpic = sides[line.sidenum[0]].sector!!.floorpic
                plat.high = sec.floorheight + amount * FRACUNIT
                plat.wait = 0
                plat.status = up

                S_StartSound(sec.soundorg, sfx_stnmov)
            }

            downWaitUpStay -> {
                plat.speed = PLATSPEED * 4
                plat.low = P_FindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = sec.floorheight
                plat.wait = 35 * PLATWAIT
                plat.status = down
                S_StartSound(sec.soundorg, sfx_pstart)
            }

            blazeDWUS -> {
                plat.speed = PLATSPEED * 8
                plat.low = P_FindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = sec.floorheight
                plat.wait = 35 * PLATWAIT
                plat.status = down
                S_StartSound(sec.soundorg, sfx_pstart)
            }

            perpetualRaise -> {
                plat.speed = PLATSPEED
                plat.low = P_FindLowestFloorSurrounding(sec)

                if (plat.low > sec.floorheight)
                    plat.low = sec.floorheight

                plat.high = P_FindHighestFloorSurrounding(sec)

                if (plat.high < sec.floorheight)
                    plat.high = sec.floorheight

                plat.wait = 35 * PLATWAIT
                plat.status = P_Random() and 1

                S_StartSound(sec.soundorg, sfx_pstart)
            }
        }
        P_AddActivePlat(plat)
    }
    return rtn
}

fun P_ActivateInStasis(tag: Int) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (activeplats[i] != null
            && activeplats[i]!!.tag == tag
            && activeplats[i]!!.status == in_stasis) {
            activeplats[i]!!.status = activeplats[i]!!.oldstatus
            activeplats[i]!!.function =
                { th -> T_PlatRaise(th as plat_t) }
        }
        i++
    }
}

fun EV_StopPlat(line: line_t) {
    var j: Int

    j = 0
    while (j < MAXPLATS) {
        if (activeplats[j] != null
            && (activeplats[j]!!.status != in_stasis)
            && (activeplats[j]!!.tag == line.tag)) {
            activeplats[j]!!.oldstatus = activeplats[j]!!.status
            activeplats[j]!!.status = in_stasis
            activeplats[j]!!.function = null
        }
        j++
    }
}

fun P_AddActivePlat(plat: plat_t) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (activeplats[i] == null) {
            activeplats[i] = plat
            return
        }
        i++
    }
    I_Error("P_AddActivePlat: no more plats!")
}

fun P_RemoveActivePlat(plat: plat_t) {
    var i: Int

    i = 0
    while (i < MAXPLATS) {
        if (plat === activeplats[i]) {
            activeplats[i]!!.sector!!.specialdata = null
            P_RemoveThinker(activeplats[i]!!)
            activeplats[i] = null

            return
        }
        i++
    }
    I_Error("P_RemoveActivePlat: can't find plat!")
}
