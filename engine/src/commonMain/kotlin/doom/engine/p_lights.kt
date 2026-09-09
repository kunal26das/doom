// Port of linuxdoom-1.10 p_lights.c -- handle Sector base lighting effects.
// Muzzle flash?
// (Also owns the P_LIGHTS thinker structs + light constants from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


internal const val GLOWSPEED = 8
internal const val STROBEBRIGHT = 5
internal const val FASTDARK = 15
internal const val SLOWDARK = 35

//
// FIRELIGHT FLICKER
//

//
// T_FireFlicker
//
internal fun DoomEngineCore.T_FireFlicker(flick: fireflicker_t) {
    val amount: Int

    flick.count--
    if (flick.count != 0)
        return

    amount = (P_Random() and 3) * 16

    if (flick.sector!!.lightlevel - amount < flick.minlight)
        flick.sector!!.lightlevel = flick.minlight
    else
        flick.sector!!.lightlevel = flick.maxlight - amount

    flick.count = 4
}

//
// P_SpawnFireFlicker
//
internal fun DoomEngineCore.P_SpawnFireFlicker(sector: sector_t) {
    val flick: fireflicker_t

    // Note that we are resetting sector attributes.
    // Nothing special about it during gameplay.
    sector.special = 0

    flick = fireflicker_t()

    P_AddThinker(flick)

    flick.function = { th -> T_FireFlicker(th as fireflicker_t) }
    flick.sector = sector
    flick.maxlight = sector.lightlevel
    flick.minlight = P_FindMinSurroundingLight(sector, sector.lightlevel) + 16
    flick.count = 4
}

//
// BROKEN LIGHT FLASHING
//

//
// T_LightFlash
// Do flashing lights.
//
internal fun DoomEngineCore.T_LightFlash(flash: lightflash_t) {
    flash.count--
    if (flash.count != 0)
        return

    if (flash.sector!!.lightlevel == flash.maxlight) {
        flash.sector!!.lightlevel = flash.minlight
        flash.count = (P_Random() and flash.mintime) + 1
    } else {
        flash.sector!!.lightlevel = flash.maxlight
        flash.count = (P_Random() and flash.maxtime) + 1
    }
}

//
// P_SpawnLightFlash
// After the map has been loaded, scan each sector
// for specials that spawn thinkers
//
internal fun DoomEngineCore.P_SpawnLightFlash(sector: sector_t) {
    val flash: lightflash_t

    // nothing special about it during gameplay
    sector.special = 0

    flash = lightflash_t()

    P_AddThinker(flash)

    flash.function = { th -> T_LightFlash(th as lightflash_t) }
    flash.sector = sector
    flash.maxlight = sector.lightlevel

    flash.minlight = P_FindMinSurroundingLight(sector, sector.lightlevel)
    flash.maxtime = 64
    flash.mintime = 7
    flash.count = (P_Random() and flash.maxtime) + 1
}

//
// STROBE LIGHT FLASHING
//

//
// T_StrobeFlash
//
internal fun DoomEngineCore.T_StrobeFlash(flash: strobe_t) {
    flash.count--
    if (flash.count != 0)
        return

    if (flash.sector!!.lightlevel == flash.minlight) {
        flash.sector!!.lightlevel = flash.maxlight
        flash.count = flash.brighttime
    } else {
        flash.sector!!.lightlevel = flash.minlight
        flash.count = flash.darktime
    }
}

//
// P_SpawnStrobeFlash
// After the map has been loaded, scan each sector
// for specials that spawn thinkers
//
internal fun DoomEngineCore.P_SpawnStrobeFlash(sector: sector_t, fastOrSlow: Int, inSync: Int) {
    val flash: strobe_t

    flash = strobe_t()

    P_AddThinker(flash)

    flash.sector = sector
    flash.darktime = fastOrSlow
    flash.brighttime = STROBEBRIGHT
    flash.function = { th -> T_StrobeFlash(th as strobe_t) }
    flash.maxlight = sector.lightlevel
    flash.minlight = P_FindMinSurroundingLight(sector, sector.lightlevel)

    if (flash.minlight == flash.maxlight)
        flash.minlight = 0

    // nothing special about it during gameplay
    sector.special = 0

    if (inSync == 0)
        flash.count = (P_Random() and 7) + 1
    else
        flash.count = 1
}

//
// Start strobing lights (usually from a trigger)
//
internal fun DoomEngineCore.EV_StartLightStrobing(line: line_t) {
    var secnum: Int
    var sec: sector_t

    secnum = -1
    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]
        if (sec.specialdata != null)
            continue

        P_SpawnStrobeFlash(sec, SLOWDARK, 0)
    }
}

//
// TURN LINE'S TAG LIGHTS OFF
//
internal fun DoomEngineCore.EV_TurnTagLightsOff(line: line_t) {
    var min: Int
    var tsec: sector_t?
    var templine: line_t

    for (j in 0 until numsectors) {
        val sector = sectors[j]
        if (sector.tag == line.tag) {
            min = sector.lightlevel
            for (i in 0 until sector.linecount) {
                templine = sector.lines[i]!!
                tsec = getNextSector(templine, sector)
                if (tsec == null)
                    continue
                if (tsec.lightlevel < min)
                    min = tsec.lightlevel
            }
            sector.lightlevel = min
        }
    }
}

//
// TURN LINE'S TAG LIGHTS ON
//
internal fun DoomEngineCore.EV_LightTurnOn(line: line_t, bright: Int) {
    var bright = bright
    var temp: sector_t?
    var templine: line_t

    for (i in 0 until numsectors) {
        val sector = sectors[i]
        if (sector.tag == line.tag) {
            // bright = 0 means to search
            // for highest light level
            // surrounding sector
            if (bright == 0) {
                for (j in 0 until sector.linecount) {
                    templine = sector.lines[j]!!
                    temp = getNextSector(templine, sector)

                    if (temp == null)
                        continue

                    if (temp.lightlevel > bright)
                        bright = temp.lightlevel
                }
            }
            sector.lightlevel = bright
        }
    }
}

//
// Spawn glowing light
//

internal fun DoomEngineCore.T_Glow(g: glow_t) {
    when (g.direction) {
        -1 -> {
            // DOWN
            g.sector!!.lightlevel -= GLOWSPEED
            if (g.sector!!.lightlevel <= g.minlight) {
                g.sector!!.lightlevel += GLOWSPEED
                g.direction = 1
            }
        }

        1 -> {
            // UP
            g.sector!!.lightlevel += GLOWSPEED
            if (g.sector!!.lightlevel >= g.maxlight) {
                g.sector!!.lightlevel -= GLOWSPEED
                g.direction = -1
            }
        }
    }
}

internal fun DoomEngineCore.P_SpawnGlowingLight(sector: sector_t) {
    val g: glow_t

    g = glow_t()

    P_AddThinker(g)

    g.sector = sector
    g.minlight = P_FindMinSurroundingLight(sector, sector.lightlevel)
    g.maxlight = sector.lightlevel
    g.function = { th -> T_Glow(th as glow_t) }
    g.direction = -1

    sector.special = 0
}
