
package doom.engine.world.specials

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_SWTCHN
import doom.engine.configuration.mCheckParm
import doom.engine.configuration.myargv
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.gExitLevel
import doom.engine.gameplay.gSecretExitLevel
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MT_BFG
import doom.engine.gameplay.actors.MT_BRUISERSHOT
import doom.engine.gameplay.actors.MT_HEADSHOT
import doom.engine.gameplay.actors.MT_PLASMA
import doom.engine.gameplay.actors.MT_ROCKET
import doom.engine.gameplay.actors.MT_TROOPSHOT
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.interactions.pDamageMobj
import doom.engine.gameplay.player.CF_GODMODE
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.PW_IRONFEET
import doom.engine.gameplay.totalsecret
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.resources.rCheckTextureNumForName
import doom.engine.rendering.resources.rFlatNumForName
import doom.engine.rendering.resources.rTextureNumForName
import doom.engine.rendering.resources.flattranslation
import doom.engine.rendering.resources.texturetranslation
import doom.engine.resources.MAXINT
import doom.engine.resources.wCheckNumForName
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pRandom
import doom.engine.simulation.leveltime
import doom.engine.world.ML_TWOSIDED
import doom.engine.world.MapLine
import doom.engine.world.MapSide
import doom.engine.world.Sector
import doom.engine.world.lighting.evLightTurnOn
import doom.engine.world.lighting.evStartLightStrobing
import doom.engine.world.lighting.evTurnTagLightsOff
import doom.engine.world.lighting.FASTDARK
import doom.engine.world.lighting.pSpawnFireFlicker
import doom.engine.world.lighting.pSpawnGlowingLight
import doom.engine.world.lighting.pSpawnLightFlash
import doom.engine.world.lighting.pSpawnStrobeFlash
import doom.engine.world.lighting.SLOWDARK
import doom.engine.world.lines
import doom.engine.world.movers.evBuildStairs
import doom.engine.world.movers.evCeilingCrushStop
import doom.engine.world.movers.evDoCeiling
import doom.engine.world.movers.evDoDoor
import doom.engine.world.movers.evDoFloor
import doom.engine.world.movers.evDoPlat
import doom.engine.world.movers.evStopPlat
import doom.engine.world.movers.FLOORSPEED
import doom.engine.world.movers.FloorMover
import doom.engine.world.movers.MAXCEILINGS
import doom.engine.world.movers.MAXPLATS
import doom.engine.world.movers.pSpawnDoorCloseIn30
import doom.engine.world.movers.pSpawnDoorRaiseIn5Mins
import doom.engine.world.movers.tMoveFloor
import doom.engine.world.movers.activeceilings
import doom.engine.world.movers.activeplats
import doom.engine.world.movers.BLAZE_CLOSE
import doom.engine.world.movers.BLAZE_DWUS
import doom.engine.world.movers.BLAZE_OPEN
import doom.engine.world.movers.BLAZE_RAISE
import doom.engine.world.movers.BUILD8
import doom.engine.world.movers.CLOSE
import doom.engine.world.movers.CLOSE30_THEN_OPEN
import doom.engine.world.movers.CRUSH_AND_RAISE
import doom.engine.world.movers.DONUT_RAISE
import doom.engine.world.movers.DOWN_WAIT_UP_STAY
import doom.engine.world.movers.FAST_CRUSH_AND_RAISE
import doom.engine.world.movers.LOWER_AND_CHANGE
import doom.engine.world.movers.LOWER_AND_CRUSH
import doom.engine.world.movers.LOWER_FLOOR
import doom.engine.world.movers.LOWER_FLOOR_TO_LOWEST
import doom.engine.world.movers.NORMAL
import doom.engine.world.movers.OPEN
import doom.engine.world.movers.PERPETUAL_RAISE
import doom.engine.world.movers.RAISE_FLOOR
import doom.engine.world.movers.RAISE_FLOOR24
import doom.engine.world.movers.RAISE_FLOOR24_AND_CHANGE
import doom.engine.world.movers.RAISE_FLOOR_CRUSH
import doom.engine.world.movers.RAISE_FLOOR_TO_NEAREST
import doom.engine.world.movers.RAISE_FLOOR_TURBO
import doom.engine.world.movers.RAISE_TO_HIGHEST
import doom.engine.world.movers.RAISE_TO_NEAREST_AND_CHANGE
import doom.engine.world.movers.RAISE_TO_TEXTURE
import doom.engine.world.movers.SILENT_CRUSH_AND_RAISE
import doom.engine.world.movers.TURBO16
import doom.engine.world.movers.TURBO_LOWER
import doom.engine.world.numlines
import doom.engine.world.numsectors
import doom.engine.world.sectors
import doom.engine.world.sides


internal const val MO_TELEPORTMAN = 14

internal const val TOP = 0
internal const val MIDDLE = 1
internal const val BOTTOM = 2

internal const val MAXSWITCHES = 50

internal const val MAXBUTTONS = 16

internal const val BUTTONTIME = 35

internal val DoomEngineCore.buttonlist
    get() = stateSectorSpecial.buttonlist

internal const val MAXANIMS = 32


internal val DoomEngineCore.animdefs
    get() = stateSectorSpecial.animdefs

internal val DoomEngineCore.anims
    get() = stateSectorSpecial.anims
internal var DoomEngineCore.lastanim
    get() = stateSectorSpecial.lastanim
    set(value) { stateSectorSpecial.lastanim = value }

internal const val MAXLINEANIMS = 64

internal fun DoomEngineCore.pInitPicAnims() {
    var i: Int

    lastanim = 0
    i = 0
    while (animdefs[i].istexture != -1) {
        if (animdefs[i].istexture != 0) {
            if (rCheckTextureNumForName(animdefs[i].startname) == -1) {
                i++
                continue
            }

            anims[lastanim].picnum = rTextureNumForName(animdefs[i].endname)
            anims[lastanim].basepic = rTextureNumForName(animdefs[i].startname)
        } else {
            if (wCheckNumForName(animdefs[i].startname) == -1) {
                i++
                continue
            }

            anims[lastanim].picnum = rFlatNumForName(animdefs[i].endname)
            anims[lastanim].basepic = rFlatNumForName(animdefs[i].startname)
        }

        anims[lastanim].istexture = animdefs[i].istexture != 0
        anims[lastanim].numpics = anims[lastanim].picnum - anims[lastanim].basepic + 1

        if (anims[lastanim].numpics < 2)
            iError("P_InitPicAnims: bad cycle from ${animdefs[i].startname} to ${animdefs[i].endname}")

        anims[lastanim].speed = animdefs[i].speed
        lastanim++
        i++
    }
}


internal fun DoomEngineCore.getSide(currentSector: Int, line: Int, side: Int): MapSide {
    return sides[(sectors[currentSector].lines[line])!!.sidenum[side]]
}

internal fun DoomEngineCore.getSector(currentSector: Int, line: Int, side: Int): Sector {
    return sides[(sectors[currentSector].lines[line])!!.sidenum[side]].sector!!
}

internal fun DoomEngineCore.twoSided(sector: Int, line: Int): Int {
    return (sectors[sector].lines[line])!!.flags and ML_TWOSIDED
}

internal fun DoomEngineCore.getNextSector(line: MapLine, sec: Sector): Sector? {
    if ((line.flags and ML_TWOSIDED) == 0)
        return null

    if (line.frontsector === sec)
        return line.backsector

    return line.frontsector
}

internal fun DoomEngineCore.pFindLowestFloorSurrounding(sec: Sector): FixedPoint {
    var i: Int
    var check: MapLine
    var other: Sector?
    var floor: FixedPoint = sec.floorheight

    i = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        other = getNextSector(check, sec)

        if (other == null) {
            i++
            continue
        }

        if (other.floorheight < floor)
            floor = other.floorheight
        i++
    }
    return floor
}

internal fun DoomEngineCore.pFindHighestFloorSurrounding(sec: Sector): FixedPoint {
    var i: Int
    var check: MapLine
    var other: Sector?
    var floor: FixedPoint = -500 * FRACUNIT

    i = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        other = getNextSector(check, sec)

        if (other == null) {
            i++
            continue
        }

        if (other.floorheight > floor)
            floor = other.floorheight
        i++
    }
    return floor
}


internal const val MAX_ADJOINING_SECTORS = 20

internal fun DoomEngineCore.pFindNextHighestFloor(sec: Sector, currentheight: Int): FixedPoint {
    var i: Int
    var h: Int
    var min: Int
    var check: MapLine
    var other: Sector?
    val height: FixedPoint = currentheight

    val heightlist = IntArray(MAX_ADJOINING_SECTORS)

    i = 0
    h = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        other = getNextSector(check, sec)

        if (other == null) {
            i++
            continue
        }

        if (other.floorheight > height) {
            heightlist[h] = other.floorheight
            h++
        }

        if (h >= MAX_ADJOINING_SECTORS) {
            println("Sector with more than 20 adjoining sectors")
            break
        }
        i++
    }

    if (h == 0)
        return currentheight

    min = heightlist[0]

    i = 1
    while (i < h) {
        if (heightlist[i] < min)
            min = heightlist[i]
        i++
    }

    return min
}

internal fun DoomEngineCore.pFindLowestCeilingSurrounding(sec: Sector): FixedPoint {
    var i: Int
    var check: MapLine
    var other: Sector?
    var height: FixedPoint = MAXINT

    i = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        other = getNextSector(check, sec)

        if (other == null) {
            i++
            continue
        }

        if (other.ceilingheight < height)
            height = other.ceilingheight
        i++
    }
    return height
}

internal fun DoomEngineCore.pFindHighestCeilingSurrounding(sec: Sector): FixedPoint {
    var i: Int
    var check: MapLine
    var other: Sector?
    var height: FixedPoint = 0

    i = 0
    while (i < sec.linecount) {
        check = sec.lines[i]!!
        other = getNextSector(check, sec)

        if (other == null) {
            i++
            continue
        }

        if (other.ceilingheight > height)
            height = other.ceilingheight
        i++
    }
    return height
}

internal fun DoomEngineCore.pFindSectorFromLineTag(line: MapLine, start: Int): Int {
    var i: Int

    i = start + 1
    while (i < numsectors) {
        if (sectors[i].tag == line.tag)
            return i
        i++
    }

    return -1
}

internal fun DoomEngineCore.pFindMinSurroundingLight(sector: Sector, max: Int): Int {
    var i: Int
    var min: Int
    var line: MapLine
    var check: Sector?

    min = max
    i = 0
    while (i < sector.linecount) {
        line = sector.lines[i]!!
        check = getNextSector(line, sector)

        if (check == null) {
            i++
            continue
        }

        if (check.lightlevel < min)
            min = check.lightlevel
        i++
    }
    return min
}


internal fun DoomEngineCore.pCrossSpecialLine(linenum: Int, side: Int, thing: Actor) {
    val line: MapLine
    var ok: Int

    line = lines[linenum]

    if (thing.player == null) {
        when (thing.type) {
            MT_ROCKET,
            MT_PLASMA,
            MT_BFG,
            MT_TROOPSHOT,
            MT_HEADSHOT,
            MT_BRUISERSHOT ->
                return

            else -> {}
        }

        ok = 0
        when (line.special) {
            39,
            97,
            125,
            126,
            4,
            10,
            88 ->
                ok = 1
        }
        if (ok == 0)
            return
    }

    when (line.special) {
        2 -> {
            evDoDoor(line, OPEN)
            line.special = 0
        }

        3 -> {
            evDoDoor(line, CLOSE)
            line.special = 0
        }

        4 -> {
            evDoDoor(line, NORMAL)
            line.special = 0
        }

        5 -> {
            evDoFloor(line, RAISE_FLOOR)
            line.special = 0
        }

        6 -> {
            evDoCeiling(line, FAST_CRUSH_AND_RAISE)
            line.special = 0
        }

        8 -> {
            evBuildStairs(line, BUILD8)
            line.special = 0
        }

        10 -> {
            evDoPlat(line, DOWN_WAIT_UP_STAY, 0)
            line.special = 0
        }

        12 -> {
            evLightTurnOn(line, 0)
            line.special = 0
        }

        13 -> {
            evLightTurnOn(line, 255)
            line.special = 0
        }

        16 -> {
            evDoDoor(line, CLOSE30_THEN_OPEN)
            line.special = 0
        }

        17 -> {
            evStartLightStrobing(line)
            line.special = 0
        }

        19 -> {
            evDoFloor(line, LOWER_FLOOR)
            line.special = 0
        }

        22 -> {
            evDoPlat(line, RAISE_TO_NEAREST_AND_CHANGE, 0)
            line.special = 0
        }

        25 -> {
            evDoCeiling(line, CRUSH_AND_RAISE)
            line.special = 0
        }

        30 -> {
            evDoFloor(line, RAISE_TO_TEXTURE)
            line.special = 0
        }

        35 -> {
            evLightTurnOn(line, 35)
            line.special = 0
        }

        36 -> {
            evDoFloor(line, TURBO_LOWER)
            line.special = 0
        }

        37 -> {
            evDoFloor(line, LOWER_AND_CHANGE)
            line.special = 0
        }

        38 -> {
            evDoFloor(line, LOWER_FLOOR_TO_LOWEST)
            line.special = 0
        }

        39 -> {
            evTeleport(line, side, thing)
            line.special = 0
        }

        40 -> {
            evDoCeiling(line, RAISE_TO_HIGHEST)
            evDoFloor(line, LOWER_FLOOR_TO_LOWEST)
            line.special = 0
        }

        44 -> {
            evDoCeiling(line, LOWER_AND_CRUSH)
            line.special = 0
        }

        52 -> {
            gExitLevel()
        }

        53 -> {
            evDoPlat(line, PERPETUAL_RAISE, 0)
            line.special = 0
        }

        54 -> {
            evStopPlat(line)
            line.special = 0
        }

        56 -> {
            evDoFloor(line, RAISE_FLOOR_CRUSH)
            line.special = 0
        }

        57 -> {
            evCeilingCrushStop(line)
            line.special = 0
        }

        58 -> {
            evDoFloor(line, RAISE_FLOOR24)
            line.special = 0
        }

        59 -> {
            evDoFloor(line, RAISE_FLOOR24_AND_CHANGE)
            line.special = 0
        }

        104 -> {
            evTurnTagLightsOff(line)
            line.special = 0
        }

        108 -> {
            evDoDoor(line, BLAZE_RAISE)
            line.special = 0
        }

        109 -> {
            evDoDoor(line, BLAZE_OPEN)
            line.special = 0
        }

        100 -> {
            evBuildStairs(line, TURBO16)
            line.special = 0
        }

        110 -> {
            evDoDoor(line, BLAZE_CLOSE)
            line.special = 0
        }

        119 -> {
            evDoFloor(line, RAISE_FLOOR_TO_NEAREST)
            line.special = 0
        }

        121 -> {
            evDoPlat(line, BLAZE_DWUS, 0)
            line.special = 0
        }

        124 -> {
            gSecretExitLevel()
        }

        125 -> {
            if (thing.player == null) {
                evTeleport(line, side, thing)
                line.special = 0
            }
        }

        130 -> {
            evDoFloor(line, RAISE_FLOOR_TURBO)
            line.special = 0
        }

        141 -> {
            evDoCeiling(line, SILENT_CRUSH_AND_RAISE)
            line.special = 0
        }

        72 -> {
            evDoCeiling(line, LOWER_AND_CRUSH)
        }

        73 -> {
            evDoCeiling(line, CRUSH_AND_RAISE)
        }

        74 -> {
            evCeilingCrushStop(line)
        }

        75 -> {
            evDoDoor(line, CLOSE)
        }

        76 -> {
            evDoDoor(line, CLOSE30_THEN_OPEN)
        }

        77 -> {
            evDoCeiling(line, FAST_CRUSH_AND_RAISE)
        }

        79 -> {
            evLightTurnOn(line, 35)
        }

        80 -> {
            evLightTurnOn(line, 0)
        }

        81 -> {
            evLightTurnOn(line, 255)
        }

        82 -> {
            evDoFloor(line, LOWER_FLOOR_TO_LOWEST)
        }

        83 -> {
            evDoFloor(line, LOWER_FLOOR)
        }

        84 -> {
            evDoFloor(line, LOWER_AND_CHANGE)
        }

        86 -> {
            evDoDoor(line, OPEN)
        }

        87 -> {
            evDoPlat(line, PERPETUAL_RAISE, 0)
        }

        88 -> {
            evDoPlat(line, DOWN_WAIT_UP_STAY, 0)
        }

        89 -> {
            evStopPlat(line)
        }

        90 -> {
            evDoDoor(line, NORMAL)
        }

        91 -> {
            evDoFloor(line, RAISE_FLOOR)
        }

        92 -> {
            evDoFloor(line, RAISE_FLOOR24)
        }

        93 -> {
            evDoFloor(line, RAISE_FLOOR24_AND_CHANGE)
        }

        94 -> {
            evDoFloor(line, RAISE_FLOOR_CRUSH)
        }

        95 -> {
            evDoPlat(line, RAISE_TO_NEAREST_AND_CHANGE, 0)
        }

        96 -> {
            evDoFloor(line, RAISE_TO_TEXTURE)
        }

        97 -> {
            evTeleport(line, side, thing)
        }

        98 -> {
            evDoFloor(line, TURBO_LOWER)
        }

        105 -> {
            evDoDoor(line, BLAZE_RAISE)
        }

        106 -> {
            evDoDoor(line, BLAZE_OPEN)
        }

        107 -> {
            evDoDoor(line, BLAZE_CLOSE)
        }

        120 -> {
            evDoPlat(line, BLAZE_DWUS, 0)
        }

        126 -> {
            if (thing.player == null)
                evTeleport(line, side, thing)
        }

        128 -> {
            evDoFloor(line, RAISE_FLOOR_TO_NEAREST)
        }

        129 -> {
            evDoFloor(line, RAISE_FLOOR_TURBO)
        }
    }
}

internal fun DoomEngineCore.pShootSpecialLine(thing: Actor, line: MapLine) {
    var ok: Int

    if (thing.player == null) {
        ok = 0
        when (line.special) {
            46 ->
                ok = 1
        }
        if (ok == 0)
            return
    }

    when (line.special) {
        24 -> {
            evDoFloor(line, RAISE_FLOOR)
            pChangeSwitchTexture(line, 0)
        }

        46 -> {
            evDoDoor(line, OPEN)
            pChangeSwitchTexture(line, 1)
        }

        47 -> {
            evDoPlat(line, RAISE_TO_NEAREST_AND_CHANGE, 0)
            pChangeSwitchTexture(line, 0)
        }
    }
}

internal fun DoomEngineCore.pPlayerInSpecialSector(player: Player) {
    val sector: Sector

    sector = player.mo!!.subsector!!.sector!!

    if (player.mo!!.z != sector.floorheight)
        return

    when (sector.special) {
        5 -> {
            if (player.powers[PW_IRONFEET] == 0)
                if ((leveltime and 0x1f) == 0)
                    pDamageMobj(player.mo!!, null, null, 10)
        }

        7 -> {
            if (player.powers[PW_IRONFEET] == 0)
                if ((leveltime and 0x1f) == 0)
                    pDamageMobj(player.mo!!, null, null, 5)
        }

        16,
        4 -> {
            if (player.powers[PW_IRONFEET] == 0
                || (pRandom() < 5)) {
                if ((leveltime and 0x1f) == 0)
                    pDamageMobj(player.mo!!, null, null, 20)
            }
        }

        9 -> {
            player.secretcount++
            sector.special = 0
        }

        11 -> {
            player.cheats = player.cheats and CF_GODMODE.inv()

            if ((leveltime and 0x1f) == 0)
                pDamageMobj(player.mo!!, null, null, 20)

            if (player.health <= 10)
                gExitLevel()
        }

        else ->
            iError("P_PlayerInSpecialSector: unknown special ${sector.special}")
    }
}

internal var DoomEngineCore.levelTimer
    get() = stateSectorSpecial.levelTimer
    set(value) { stateSectorSpecial.levelTimer = value }
internal var DoomEngineCore.levelTimeCount
    get() = stateSectorSpecial.levelTimeCount
    set(value) { stateSectorSpecial.levelTimeCount = value }

internal fun DoomEngineCore.pUpdateSpecials() {
    var pic: Int
    var i: Int
    var line: MapLine

    if (levelTimer == true) {
        levelTimeCount--
        if (levelTimeCount == 0)
            gExitLevel()
    }

    var a = 0
    while (a < lastanim) {
        val anim = anims[a]
        i = anim.basepic
        while (i < anim.basepic + anim.numpics) {
            pic = anim.basepic + ((leveltime / anim.speed + i) % anim.numpics)
            if (anim.istexture)
                texturetranslation[i] = pic
            else
                flattranslation[i] = pic
            i++
        }
        a++
    }

    i = 0
    while (i < numlinespecials) {
        line = linespeciallist[i]!!
        when (line.special) {
            48 ->
                sides[line.sidenum[0]].textureoffset += FRACUNIT
        }
        i++
    }

    i = 0
    while (i < MAXBUTTONS) {
        if (buttonlist[i].btimer != 0) {
            buttonlist[i].btimer--
            if (buttonlist[i].btimer == 0) {
                when (buttonlist[i].where) {
                    TOP ->
                        sides[buttonlist[i].line!!.sidenum[0]].toptexture =
                            buttonlist[i].btexture

                    MIDDLE ->
                        sides[buttonlist[i].line!!.sidenum[0]].midtexture =
                            buttonlist[i].btexture

                    BOTTOM ->
                        sides[buttonlist[i].line!!.sidenum[0]].bottomtexture =
                            buttonlist[i].btexture
                }
                sStartSound(buttonlist[i].soundorg, SFX_SWTCHN)
                buttonlist[i] = SwitchButton()
            }
        }
        i++
    }
}

internal fun DoomEngineCore.evDoDonut(line: MapLine): Int {
    var s1: Sector
    var s2: Sector
    var s3: Sector
    var secnum: Int
    var rtn: Int
    var i: Int
    var floor: FloorMover

    secnum = -1
    rtn = 0
    while (true) {
        secnum = pFindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        s1 = sectors[secnum]

        if (s1.specialdata != null)
            continue

        rtn = 1
        s2 = getNextSector(s1.lines[0]!!, s1)!!
        i = 0
        while (i < s2.linecount) {
            if ((((if (s2.lines[i]!!.flags == 0) 1 else 0) and ML_TWOSIDED) != 0) ||
                (s2.lines[i]!!.backsector === s1)) {
                i++
                continue
            }
            s3 = s2.lines[i]!!.backsector!!

            floor = FloorMover()
            pAddThinker(floor)
            s2.specialdata = floor
            floor.function = { th -> tMoveFloor(th as FloorMover) }
            floor.type = DONUT_RAISE
            floor.crush = false
            floor.direction = 1
            floor.sector = s2
            floor.speed = FLOORSPEED / 2
            floor.texture = s3.floorpic
            floor.newspecial = 0
            floor.floordestheight = s3.floorheight

            floor = FloorMover()
            pAddThinker(floor)
            s1.specialdata = floor
            floor.function = { th -> tMoveFloor(th as FloorMover) }
            floor.type = LOWER_FLOOR
            floor.crush = false
            floor.direction = -1
            floor.sector = s1
            floor.speed = FLOORSPEED / 2
            floor.floordestheight = s3.floorheight
            break
        }
    }
    return rtn
}


internal var DoomEngineCore.numlinespecials
    get() = stateSectorSpecial.numlinespecials
    set(value) { stateSectorSpecial.numlinespecials = value }
internal val DoomEngineCore.linespeciallist
    get() = stateSectorSpecial.linespeciallist

internal fun DoomEngineCore.pSpawnSpecials() {
    var sector: Sector
    var i: Int

    levelTimer = false

    i = mCheckParm("-avg")
    if (i != 0 && deathmatch != 0) {
        levelTimer = true
        levelTimeCount = 20 * 60 * 35
    }

    i = mCheckParm("-timer")
    if (i != 0 && deathmatch != 0) {
        val time: Int
        time = (myargv[i + 1].toIntOrNull() ?: 0) * 60 * 35
        levelTimer = true
        levelTimeCount = time
    }

    i = 0
    while (i < numsectors) {
        sector = sectors[i]
        if (sector.special == 0) {
            i++
            continue
        }

        when (sector.special) {
            1 ->
                pSpawnLightFlash(sector)

            2 ->
                pSpawnStrobeFlash(sector, FASTDARK, 0)

            3 ->
                pSpawnStrobeFlash(sector, SLOWDARK, 0)

            4 -> {
                pSpawnStrobeFlash(sector, FASTDARK, 0)
                sector.special = 4
            }

            8 ->
                pSpawnGlowingLight(sector)

            9 ->
                totalsecret++

            10 ->
                pSpawnDoorCloseIn30(sector)

            12 ->
                pSpawnStrobeFlash(sector, SLOWDARK, 1)

            13 ->
                pSpawnStrobeFlash(sector, FASTDARK, 1)

            14 ->
                pSpawnDoorRaiseIn5Mins(sector)

            17 ->
                pSpawnFireFlicker(sector)
        }
        i++
    }

    numlinespecials = 0
    i = 0
    while (i < numlines) {
        when (lines[i].special) {
            48 -> {
                linespeciallist[numlinespecials] = lines[i]
                numlinespecials++
            }
        }
        i++
    }

    i = 0
    while (i < MAXCEILINGS) {
        activeceilings[i] = null
        i++
    }

    i = 0
    while (i < MAXPLATS) {
        activeplats[i] = null
        i++
    }

    i = 0
    while (i < MAXBUTTONS) {
        buttonlist[i] = SwitchButton()
        i++
    }

}
