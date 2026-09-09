
package doom.engine.world.lighting

import doom.engine.core.DoomEngineCore
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRandom
import doom.engine.world.MapLine
import doom.engine.world.Sector
import doom.engine.world.numsectors
import doom.engine.world.sectors
import doom.engine.world.specials.pFindMinSurroundingLight
import doom.engine.world.specials.pFindSectorFromLineTag
import doom.engine.world.specials.getNextSector

internal const val GLOWSPEED = 8
internal const val STROBEBRIGHT = 5
internal const val FASTDARK = 15
internal const val SLOWDARK = 35


internal fun DoomEngineCore.tFireFlicker(flick: FireFlicker) {
    val amount: Int

    flick.count--
    if (flick.count != 0)
        return

    amount = (pRandom() and 3) * 16

    if (flick.sector!!.lightlevel - amount < flick.minlight)
        flick.sector!!.lightlevel = flick.minlight
    else
        flick.sector!!.lightlevel = flick.maxlight - amount

    flick.count = 4
}

internal fun DoomEngineCore.pSpawnFireFlicker(sector: Sector) {
    val flick: FireFlicker

    sector.special = 0

    flick = FireFlicker()

    pAddThinker(flick)

    flick.function = { th -> tFireFlicker(th as FireFlicker) }
    flick.sector = sector
    flick.maxlight = sector.lightlevel
    flick.minlight = pFindMinSurroundingLight(sector, sector.lightlevel) + 16
    flick.count = 4
}


internal fun DoomEngineCore.tLightFlash(flash: LightFlash) {
    flash.count--
    if (flash.count != 0)
        return

    if (flash.sector!!.lightlevel == flash.maxlight) {
        flash.sector!!.lightlevel = flash.minlight
        flash.count = (pRandom() and flash.mintime) + 1
    } else {
        flash.sector!!.lightlevel = flash.maxlight
        flash.count = (pRandom() and flash.maxtime) + 1
    }
}

internal fun DoomEngineCore.pSpawnLightFlash(sector: Sector) {
    val flash: LightFlash

    sector.special = 0

    flash = LightFlash()

    pAddThinker(flash)

    flash.function = { th -> tLightFlash(th as LightFlash) }
    flash.sector = sector
    flash.maxlight = sector.lightlevel

    flash.minlight = pFindMinSurroundingLight(sector, sector.lightlevel)
    flash.maxtime = 64
    flash.mintime = 7
    flash.count = (pRandom() and flash.maxtime) + 1
}


internal fun DoomEngineCore.tStrobeFlash(flash: StrobeLight) {
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

internal fun DoomEngineCore.pSpawnStrobeFlash(sector: Sector, fastOrSlow: Int, inSync: Int) {
    val flash: StrobeLight

    flash = StrobeLight()

    pAddThinker(flash)

    flash.sector = sector
    flash.darktime = fastOrSlow
    flash.brighttime = STROBEBRIGHT
    flash.function = { th -> tStrobeFlash(th as StrobeLight) }
    flash.maxlight = sector.lightlevel
    flash.minlight = pFindMinSurroundingLight(sector, sector.lightlevel)

    if (flash.minlight == flash.maxlight)
        flash.minlight = 0

    sector.special = 0

    if (inSync == 0)
        flash.count = (pRandom() and 7) + 1
    else
        flash.count = 1
}

internal fun DoomEngineCore.evStartLightStrobing(line: MapLine) {
    var secnum: Int
    var sec: Sector

    secnum = -1
    while (true) {
        secnum = pFindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        sec = sectors[secnum]
        if (sec.specialdata != null)
            continue

        pSpawnStrobeFlash(sec, SLOWDARK, 0)
    }
}

internal fun DoomEngineCore.evTurnTagLightsOff(line: MapLine) {
    var min: Int
    var tsec: Sector?
    var templine: MapLine

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

internal fun DoomEngineCore.evLightTurnOn(line: MapLine, requestedBrightness: Int) {
    var bright = requestedBrightness
    var temp: Sector?
    var templine: MapLine

    for (i in 0 until numsectors) {
        val sector = sectors[i]
        if (sector.tag == line.tag) {
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


internal fun DoomEngineCore.tGlow(g: GlowingLight) {
    when (g.direction) {
        -1 -> {
            g.sector!!.lightlevel -= GLOWSPEED
            if (g.sector!!.lightlevel <= g.minlight) {
                g.sector!!.lightlevel += GLOWSPEED
                g.direction = 1
            }
        }

        1 -> {
            g.sector!!.lightlevel += GLOWSPEED
            if (g.sector!!.lightlevel >= g.maxlight) {
                g.sector!!.lightlevel -= GLOWSPEED
                g.direction = -1
            }
        }
    }
}

internal fun DoomEngineCore.pSpawnGlowingLight(sector: Sector) {
    val g: GlowingLight

    g = GlowingLight()

    pAddThinker(g)

    g.sector = sector
    g.minlight = pFindMinSurroundingLight(sector, sector.lightlevel)
    g.maxlight = sector.lightlevel
    g.function = { th -> tGlow(th as GlowingLight) }
    g.direction = -1

    sector.special = 0
}
