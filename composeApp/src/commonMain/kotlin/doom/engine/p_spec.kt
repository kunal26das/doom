// Port of linuxdoom-1.10 p_spec.c -- implements special effects:
// Texture animation, height or lighting changes according to adjacent
// sectors, respective utility functions, etc.
// Line Tag handling. Line and Sector triggers.
// (Also owns from p_spec.h: MO_TELEPORTMAN, button_t/bwhere_e/BUTTONTIME/
//  MAXBUTTONS + buttonlist, and the levelTimer globals.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// End-level timer (-TIMER option)
// (declared in p_spec.h, defined below with P_UpdateSpecials)
//

//      Define values for map objects
const val MO_TELEPORTMAN = 14

// From p_spec.h: P_SWITCH button types.
// bwhere_e
const val top = 0
const val middle = 1
const val bottom = 2

class button_t {
    var line: line_t? = null
    var where = 0            // bwhere_e
    var btexture = 0
    var btimer = 0
    // C: mobj_t* soundorg -- always assigned (mobj_t*)&sector->soundorg,
    // i.e. really the sector's degenmobj_t sound origin.
    var soundorg: degenmobj_t? = null
}

// max # of wall switches in a level
const val MAXSWITCHES = 50

// 4 players, 4 buttons each at once, max.
const val MAXBUTTONS = 16

// 1 second, in ticks.
const val BUTTONTIME = 35

val buttonlist = Array(MAXBUTTONS) { button_t() }

//
// Animating textures and planes
// There is another anim_t used in wi_stuff, unrelated.
//
class anim_t {
    var istexture = false
    var picnum = 0
    var basepic = 0
    var numpics = 0
    var speed = 0
}

//
//      source animation definition
//
class animdef_t {
    var istexture = 0   // C boolean; if false, it is a flat. -1 terminates the list.
    var endname = ""
    var startname = ""
    var speed = 0

    constructor(istexture: Boolean, endname: String, startname: String, speed: Int) {
        this.istexture = if (istexture) 1 else 0
        this.endname = endname
        this.startname = startname
        this.speed = speed
    }

    // the {-1} table terminator
    constructor(istexture: Int) {
        this.istexture = istexture
    }
}

const val MAXANIMS = 32

//
// P_InitPicAnims
//

// Floor/ceiling animation sequences,
//  defined by first and last frame,
//  i.e. the flat (64x64 tile) name to
//  be used.
// The full animation sequence is given
//  using all the flats between the start
//  and end entry, in the order found in
//  the WAD file.
//
val animdefs = arrayOf(
    animdef_t(false, "NUKAGE3", "NUKAGE1", 8),
    animdef_t(false, "FWATER4", "FWATER1", 8),
    animdef_t(false, "SWATER4", "SWATER1", 8),
    animdef_t(false, "LAVA4", "LAVA1", 8),
    animdef_t(false, "BLOOD3", "BLOOD1", 8),

    // DOOM II flat animations.
    animdef_t(false, "RROCK08", "RROCK05", 8),
    animdef_t(false, "SLIME04", "SLIME01", 8),
    animdef_t(false, "SLIME08", "SLIME05", 8),
    animdef_t(false, "SLIME12", "SLIME09", 8),

    animdef_t(true, "BLODGR4", "BLODGR1", 8),
    animdef_t(true, "SLADRIP3", "SLADRIP1", 8),

    animdef_t(true, "BLODRIP4", "BLODRIP1", 8),
    animdef_t(true, "FIREWALL", "FIREWALA", 8),
    animdef_t(true, "GSTFONT3", "GSTFONT1", 8),
    animdef_t(true, "FIRELAVA", "FIRELAV3", 8),
    animdef_t(true, "FIREMAG3", "FIREMAG1", 8),
    animdef_t(true, "FIREBLU2", "FIREBLU1", 8),
    animdef_t(true, "ROCKRED3", "ROCKRED1", 8),

    animdef_t(true, "BFALL4", "BFALL1", 8),
    animdef_t(true, "SFALL4", "SFALL1", 8),
    animdef_t(true, "WFALL4", "WFALL1", 8),
    animdef_t(true, "DBRAIN4", "DBRAIN1", 8),

    animdef_t(-1),
)

val anims = Array(MAXANIMS) { anim_t() }
var lastanim = 0    // C: anim_t* lastanim -> index into anims

//
//      Animating line specials
//
const val MAXLINEANIMS = 64

fun P_InitPicAnims() {
    var i: Int

    //	Init animation
    lastanim = 0    // lastanim = anims
    i = 0
    while (animdefs[i].istexture != -1) {
        if (animdefs[i].istexture != 0) {
            // different episode ?
            if (R_CheckTextureNumForName(animdefs[i].startname) == -1) {
                i++
                continue
            }

            anims[lastanim].picnum = R_TextureNumForName(animdefs[i].endname)
            anims[lastanim].basepic = R_TextureNumForName(animdefs[i].startname)
        } else {
            if (W_CheckNumForName(animdefs[i].startname) == -1) {
                i++
                continue
            }

            anims[lastanim].picnum = R_FlatNumForName(animdefs[i].endname)
            anims[lastanim].basepic = R_FlatNumForName(animdefs[i].startname)
        }

        anims[lastanim].istexture = animdefs[i].istexture != 0
        anims[lastanim].numpics = anims[lastanim].picnum - anims[lastanim].basepic + 1

        if (anims[lastanim].numpics < 2)
            I_Error("P_InitPicAnims: bad cycle from ${animdefs[i].startname} to ${animdefs[i].endname}")

        anims[lastanim].speed = animdefs[i].speed
        lastanim++
        i++
    }
}

//
// UTILITIES
//

//
// getSide()
// Will return a side_t*
//  given the number of the current sector,
//  the line number, and the side (0/1) that you want.
//
fun getSide(currentSector: Int, line: Int, side: Int): side_t {
    return sides[(sectors[currentSector].lines[line])!!.sidenum[side]]
}

//
// getSector()
// Will return a sector_t*
//  given the number of the current sector,
//  the line number and the side (0/1) that you want.
//
fun getSector(currentSector: Int, line: Int, side: Int): sector_t {
    return sides[(sectors[currentSector].lines[line])!!.sidenum[side]].sector!!
}

//
// twoSided()
// Given the sector number and the line number,
//  it will tell you whether the line is two-sided or not.
//
fun twoSided(sector: Int, line: Int): Int {
    return (sectors[sector].lines[line])!!.flags and ML_TWOSIDED
}

//
// getNextSector()
// Return sector_t * of sector next to current.
// NULL if not two-sided line
//
fun getNextSector(line: line_t, sec: sector_t): sector_t? {
    if ((line.flags and ML_TWOSIDED) == 0)
        return null

    if (line.frontsector === sec)
        return line.backsector

    return line.frontsector
}

//
// P_FindLowestFloorSurrounding()
// FIND LOWEST FLOOR HEIGHT IN SURROUNDING SECTORS
//
fun P_FindLowestFloorSurrounding(sec: sector_t): fixed_t {
    var i: Int
    var check: line_t
    var other: sector_t?
    var floor: fixed_t = sec.floorheight

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

//
// P_FindHighestFloorSurrounding()
// FIND HIGHEST FLOOR HEIGHT IN SURROUNDING SECTORS
//
fun P_FindHighestFloorSurrounding(sec: sector_t): fixed_t {
    var i: Int
    var check: line_t
    var other: sector_t?
    var floor: fixed_t = -500 * FRACUNIT

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

//
// P_FindNextHighestFloor
// FIND NEXT HIGHEST FLOOR IN SURROUNDING SECTORS
// Note: this should be doable w/o a fixed array.

// 20 adjoining sectors max!
const val MAX_ADJOINING_SECTORS = 20

fun P_FindNextHighestFloor(sec: sector_t, currentheight: Int): fixed_t {
    var i: Int
    var h: Int
    var min: Int
    var check: line_t
    var other: sector_t?
    val height: fixed_t = currentheight

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

        // Check for overflow. Exit.
        if (h >= MAX_ADJOINING_SECTORS) {
            println("Sector with more than 20 adjoining sectors")
            break
        }
        i++
    }

    // Find lowest height in list
    if (h == 0)
        return currentheight

    min = heightlist[0]

    // Range checking?
    i = 1
    while (i < h) {
        if (heightlist[i] < min)
            min = heightlist[i]
        i++
    }

    return min
}

//
// FIND LOWEST CEILING IN THE SURROUNDING SECTORS
//
fun P_FindLowestCeilingSurrounding(sec: sector_t): fixed_t {
    var i: Int
    var check: line_t
    var other: sector_t?
    var height: fixed_t = MAXINT

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

//
// FIND HIGHEST CEILING IN THE SURROUNDING SECTORS
//
fun P_FindHighestCeilingSurrounding(sec: sector_t): fixed_t {
    var i: Int
    var check: line_t
    var other: sector_t?
    var height: fixed_t = 0

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

//
// RETURN NEXT SECTOR # THAT LINE TAG REFERS TO
//
fun P_FindSectorFromLineTag(line: line_t, start: Int): Int {
    var i: Int

    i = start + 1
    while (i < numsectors) {
        if (sectors[i].tag == line.tag)
            return i
        i++
    }

    return -1
}

//
// Find minimum light from an adjacent sector
//
fun P_FindMinSurroundingLight(sector: sector_t, max: Int): Int {
    var i: Int
    var min: Int
    var line: line_t
    var check: sector_t?

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

//
// EVENTS
// Events are operations triggered by using, crossing,
// or shooting special lines, or by timed thinkers.
//

//
// P_CrossSpecialLine - TRIGGER
// Called every time a thing origin is about
//  to cross a line with a non 0 special.
//
fun P_CrossSpecialLine(linenum: Int, side: Int, thing: mobj_t) {
    val line: line_t
    var ok: Int

    line = lines[linenum]

    //	Triggers that other things can activate
    if (thing.player == null) {
        // Things that should NOT trigger specials...
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
            39,     // TELEPORT TRIGGER
            97,     // TELEPORT RETRIGGER
            125,    // TELEPORT MONSTERONLY TRIGGER
            126,    // TELEPORT MONSTERONLY RETRIGGER
            4,      // RAISE DOOR
            10,     // PLAT DOWN-WAIT-UP-STAY TRIGGER
            88 ->   // PLAT DOWN-WAIT-UP-STAY RETRIGGER
                ok = 1
        }
        if (ok == 0)
            return
    }

    // Note: could use some const's here.
    when (line.special) {
        // TRIGGERS.
        // All from here to RETRIGGERS.
        2 -> {
            // Open Door
            EV_DoDoor(line, open)
            line.special = 0
        }

        3 -> {
            // Close Door
            EV_DoDoor(line, close)
            line.special = 0
        }

        4 -> {
            // Raise Door
            EV_DoDoor(line, normal)
            line.special = 0
        }

        5 -> {
            // Raise Floor
            EV_DoFloor(line, raiseFloor)
            line.special = 0
        }

        6 -> {
            // Fast Ceiling Crush & Raise
            EV_DoCeiling(line, fastCrushAndRaise)
            line.special = 0
        }

        8 -> {
            // Build Stairs
            EV_BuildStairs(line, build8)
            line.special = 0
        }

        10 -> {
            // PlatDownWaitUp
            EV_DoPlat(line, downWaitUpStay, 0)
            line.special = 0
        }

        12 -> {
            // Light Turn On - brightest near
            EV_LightTurnOn(line, 0)
            line.special = 0
        }

        13 -> {
            // Light Turn On 255
            EV_LightTurnOn(line, 255)
            line.special = 0
        }

        16 -> {
            // Close Door 30
            EV_DoDoor(line, close30ThenOpen)
            line.special = 0
        }

        17 -> {
            // Start Light Strobing
            EV_StartLightStrobing(line)
            line.special = 0
        }

        19 -> {
            // Lower Floor
            EV_DoFloor(line, lowerFloor)
            line.special = 0
        }

        22 -> {
            // Raise floor to nearest height and change texture
            EV_DoPlat(line, raiseToNearestAndChange, 0)
            line.special = 0
        }

        25 -> {
            // Ceiling Crush and Raise
            EV_DoCeiling(line, crushAndRaise)
            line.special = 0
        }

        30 -> {
            // Raise floor to shortest texture height
            //  on either side of lines.
            EV_DoFloor(line, raiseToTexture)
            line.special = 0
        }

        35 -> {
            // Lights Very Dark
            EV_LightTurnOn(line, 35)
            line.special = 0
        }

        36 -> {
            // Lower Floor (TURBO)
            EV_DoFloor(line, turboLower)
            line.special = 0
        }

        37 -> {
            // LowerAndChange
            EV_DoFloor(line, lowerAndChange)
            line.special = 0
        }

        38 -> {
            // Lower Floor To Lowest
            EV_DoFloor(line, lowerFloorToLowest)
            line.special = 0
        }

        39 -> {
            // TELEPORT!
            EV_Teleport(line, side, thing)
            line.special = 0
        }

        40 -> {
            // RaiseCeilingLowerFloor
            EV_DoCeiling(line, raiseToHighest)
            EV_DoFloor(line, lowerFloorToLowest)
            line.special = 0
        }

        44 -> {
            // Ceiling Crush
            EV_DoCeiling(line, lowerAndCrush)
            line.special = 0
        }

        52 -> {
            // EXIT!
            G_ExitLevel()
        }

        53 -> {
            // Perpetual Platform Raise
            EV_DoPlat(line, perpetualRaise, 0)
            line.special = 0
        }

        54 -> {
            // Platform Stop
            EV_StopPlat(line)
            line.special = 0
        }

        56 -> {
            // Raise Floor Crush
            EV_DoFloor(line, raiseFloorCrush)
            line.special = 0
        }

        57 -> {
            // Ceiling Crush Stop
            EV_CeilingCrushStop(line)
            line.special = 0
        }

        58 -> {
            // Raise Floor 24
            EV_DoFloor(line, raiseFloor24)
            line.special = 0
        }

        59 -> {
            // Raise Floor 24 And Change
            EV_DoFloor(line, raiseFloor24AndChange)
            line.special = 0
        }

        104 -> {
            // Turn lights off in sector(tag)
            EV_TurnTagLightsOff(line)
            line.special = 0
        }

        108 -> {
            // Blazing Door Raise (faster than TURBO!)
            EV_DoDoor(line, blazeRaise)
            line.special = 0
        }

        109 -> {
            // Blazing Door Open (faster than TURBO!)
            EV_DoDoor(line, blazeOpen)
            line.special = 0
        }

        100 -> {
            // Build Stairs Turbo 16
            EV_BuildStairs(line, turbo16)
            line.special = 0
        }

        110 -> {
            // Blazing Door Close (faster than TURBO!)
            EV_DoDoor(line, blazeClose)
            line.special = 0
        }

        119 -> {
            // Raise floor to nearest surr. floor
            EV_DoFloor(line, raiseFloorToNearest)
            line.special = 0
        }

        121 -> {
            // Blazing PlatDownWaitUpStay
            EV_DoPlat(line, blazeDWUS, 0)
            line.special = 0
        }

        124 -> {
            // Secret EXIT
            G_SecretExitLevel()
        }

        125 -> {
            // TELEPORT MonsterONLY
            if (thing.player == null) {
                EV_Teleport(line, side, thing)
                line.special = 0
            }
        }

        130 -> {
            // Raise Floor Turbo
            EV_DoFloor(line, raiseFloorTurbo)
            line.special = 0
        }

        141 -> {
            // Silent Ceiling Crush & Raise
            EV_DoCeiling(line, silentCrushAndRaise)
            line.special = 0
        }

        // RETRIGGERS.  All from here till end.
        72 -> {
            // Ceiling Crush
            EV_DoCeiling(line, lowerAndCrush)
        }

        73 -> {
            // Ceiling Crush and Raise
            EV_DoCeiling(line, crushAndRaise)
        }

        74 -> {
            // Ceiling Crush Stop
            EV_CeilingCrushStop(line)
        }

        75 -> {
            // Close Door
            EV_DoDoor(line, close)
        }

        76 -> {
            // Close Door 30
            EV_DoDoor(line, close30ThenOpen)
        }

        77 -> {
            // Fast Ceiling Crush & Raise
            EV_DoCeiling(line, fastCrushAndRaise)
        }

        79 -> {
            // Lights Very Dark
            EV_LightTurnOn(line, 35)
        }

        80 -> {
            // Light Turn On - brightest near
            EV_LightTurnOn(line, 0)
        }

        81 -> {
            // Light Turn On 255
            EV_LightTurnOn(line, 255)
        }

        82 -> {
            // Lower Floor To Lowest
            EV_DoFloor(line, lowerFloorToLowest)
        }

        83 -> {
            // Lower Floor
            EV_DoFloor(line, lowerFloor)
        }

        84 -> {
            // LowerAndChange
            EV_DoFloor(line, lowerAndChange)
        }

        86 -> {
            // Open Door
            EV_DoDoor(line, open)
        }

        87 -> {
            // Perpetual Platform Raise
            EV_DoPlat(line, perpetualRaise, 0)
        }

        88 -> {
            // PlatDownWaitUp
            EV_DoPlat(line, downWaitUpStay, 0)
        }

        89 -> {
            // Platform Stop
            EV_StopPlat(line)
        }

        90 -> {
            // Raise Door
            EV_DoDoor(line, normal)
        }

        91 -> {
            // Raise Floor
            EV_DoFloor(line, raiseFloor)
        }

        92 -> {
            // Raise Floor 24
            EV_DoFloor(line, raiseFloor24)
        }

        93 -> {
            // Raise Floor 24 And Change
            EV_DoFloor(line, raiseFloor24AndChange)
        }

        94 -> {
            // Raise Floor Crush
            EV_DoFloor(line, raiseFloorCrush)
        }

        95 -> {
            // Raise floor to nearest height
            // and change texture.
            EV_DoPlat(line, raiseToNearestAndChange, 0)
        }

        96 -> {
            // Raise floor to shortest texture height
            // on either side of lines.
            EV_DoFloor(line, raiseToTexture)
        }

        97 -> {
            // TELEPORT!
            EV_Teleport(line, side, thing)
        }

        98 -> {
            // Lower Floor (TURBO)
            EV_DoFloor(line, turboLower)
        }

        105 -> {
            // Blazing Door Raise (faster than TURBO!)
            EV_DoDoor(line, blazeRaise)
        }

        106 -> {
            // Blazing Door Open (faster than TURBO!)
            EV_DoDoor(line, blazeOpen)
        }

        107 -> {
            // Blazing Door Close (faster than TURBO!)
            EV_DoDoor(line, blazeClose)
        }

        120 -> {
            // Blazing PlatDownWaitUpStay.
            EV_DoPlat(line, blazeDWUS, 0)
        }

        126 -> {
            // TELEPORT MonsterONLY.
            if (thing.player == null)
                EV_Teleport(line, side, thing)
        }

        128 -> {
            // Raise To Nearest Floor
            EV_DoFloor(line, raiseFloorToNearest)
        }

        129 -> {
            // Raise Floor Turbo
            EV_DoFloor(line, raiseFloorTurbo)
        }
    }
}

//
// P_ShootSpecialLine - IMPACT SPECIALS
// Called when a thing shoots a special line.
//
fun P_ShootSpecialLine(thing: mobj_t, line: line_t) {
    var ok: Int

    //	Impacts that other things can activate.
    if (thing.player == null) {
        ok = 0
        when (line.special) {
            46 ->
                // OPEN DOOR IMPACT
                ok = 1
        }
        if (ok == 0)
            return
    }

    when (line.special) {
        24 -> {
            // RAISE FLOOR
            EV_DoFloor(line, raiseFloor)
            P_ChangeSwitchTexture(line, 0)
        }

        46 -> {
            // OPEN DOOR
            EV_DoDoor(line, open)
            P_ChangeSwitchTexture(line, 1)
        }

        47 -> {
            // RAISE FLOOR NEAR AND CHANGE
            EV_DoPlat(line, raiseToNearestAndChange, 0)
            P_ChangeSwitchTexture(line, 0)
        }
    }
}

//
// P_PlayerInSpecialSector
// Called every tic frame
//  that the player origin is in a special sector
//
fun P_PlayerInSpecialSector(player: player_t) {
    val sector: sector_t

    sector = player.mo!!.subsector!!.sector!!

    // Falling, not all the way down yet?
    if (player.mo!!.z != sector.floorheight)
        return

    // Has hitten ground.
    when (sector.special) {
        5 -> {
            // HELLSLIME DAMAGE
            if (player.powers[pw_ironfeet] == 0)
                if ((leveltime and 0x1f) == 0)
                    P_DamageMobj(player.mo!!, null, null, 10)
        }

        7 -> {
            // NUKAGE DAMAGE
            if (player.powers[pw_ironfeet] == 0)
                if ((leveltime and 0x1f) == 0)
                    P_DamageMobj(player.mo!!, null, null, 5)
        }

        16,     // SUPER HELLSLIME DAMAGE
        4 -> {  // STROBE HURT
            if (player.powers[pw_ironfeet] == 0
                || (P_Random() < 5)) {
                if ((leveltime and 0x1f) == 0)
                    P_DamageMobj(player.mo!!, null, null, 20)
            }
        }

        9 -> {
            // SECRET SECTOR
            player.secretcount++
            sector.special = 0
        }

        11 -> {
            // EXIT SUPER DAMAGE! (for E1M8 finale)
            player.cheats = player.cheats and CF_GODMODE.inv()

            if ((leveltime and 0x1f) == 0)
                P_DamageMobj(player.mo!!, null, null, 20)

            if (player.health <= 10)
                G_ExitLevel()
        }

        else ->
            I_Error("P_PlayerInSpecialSector: unknown special ${sector.special}")
    }
}

//
// P_UpdateSpecials
// Animate planes, scroll walls, etc.
//
var levelTimer = false
var levelTimeCount = 0

fun P_UpdateSpecials() {
    var pic: Int
    var i: Int
    var line: line_t

    //	LEVEL TIMER
    if (levelTimer == true) {
        levelTimeCount--
        if (levelTimeCount == 0)
            G_ExitLevel()
    }

    //	ANIMATE FLATS AND TEXTURES GLOBALLY
    var a = 0   // C: for (anim = anims ; anim < lastanim ; anim++)
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

    //	ANIMATE LINE SPECIALS
    i = 0
    while (i < numlinespecials) {
        line = linespeciallist[i]!!
        when (line.special) {
            48 ->
                // EFFECT FIRSTCOL SCROLL +
                sides[line.sidenum[0]].textureoffset += FRACUNIT
        }
        i++
    }

    //	DO BUTTONS
    i = 0
    while (i < MAXBUTTONS) {
        if (buttonlist[i].btimer != 0) {
            buttonlist[i].btimer--
            if (buttonlist[i].btimer == 0) {
                when (buttonlist[i].where) {
                    top ->
                        sides[buttonlist[i].line!!.sidenum[0]].toptexture =
                            buttonlist[i].btexture

                    middle ->
                        sides[buttonlist[i].line!!.sidenum[0]].midtexture =
                            buttonlist[i].btexture

                    bottom ->
                        sides[buttonlist[i].line!!.sidenum[0]].bottomtexture =
                            buttonlist[i].btexture
                }
                // (C passes (mobj_t*)&buttonlist[i].soundorg -- the address of
                //  the pointer field itself, i.e. garbage coordinates. Sound
                //  position does not affect the simulation; use the origin
                //  the code intends.)
                S_StartSound(buttonlist[i].soundorg, sfx_swtchn)
                // memset(&buttonlist[i],0,sizeof(button_t));
                buttonlist[i] = button_t()
            }
        }
        i++
    }
}

//
// Special Stuff that can not be categorized
//
fun EV_DoDonut(line: line_t): Int {
    var s1: sector_t
    var s2: sector_t
    var s3: sector_t
    var secnum: Int
    var rtn: Int
    var i: Int
    var floor: floormove_t

    secnum = -1
    rtn = 0
    while (true) {
        secnum = P_FindSectorFromLineTag(line, secnum)
        if (secnum < 0)
            break
        s1 = sectors[secnum]

        // ALREADY MOVING?  IF SO, KEEP GOING...
        if (s1.specialdata != null)
            continue

        rtn = 1
        s2 = getNextSector(s1.lines[0]!!, s1)!!
        i = 0
        while (i < s2.linecount) {
            // (vanilla bug: C's `!s2->lines[i]->flags & ML_TWOSIDED` negates
            //  flags first, so the two-sided check never triggers)
            if ((((if (s2.lines[i]!!.flags == 0) 1 else 0) and ML_TWOSIDED) != 0) ||
                (s2.lines[i]!!.backsector === s1)) {
                i++
                continue
            }
            s3 = s2.lines[i]!!.backsector!!

            //	Spawn rising slime
            floor = floormove_t()
            P_AddThinker(floor)
            s2.specialdata = floor
            floor.function = { th -> T_MoveFloor(th as floormove_t) }
            floor.type = donutRaise
            floor.crush = false
            floor.direction = 1
            floor.sector = s2
            floor.speed = FLOORSPEED / 2
            floor.texture = s3.floorpic
            floor.newspecial = 0
            floor.floordestheight = s3.floorheight

            //	Spawn lowering donut-hole
            floor = floormove_t()
            P_AddThinker(floor)
            s1.specialdata = floor
            floor.function = { th -> T_MoveFloor(th as floormove_t) }
            floor.type = lowerFloor
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

//
// SPECIAL SPAWNING
//

//
// P_SpawnSpecials
// After the map has been loaded, scan for specials
//  that spawn thinkers
//
var numlinespecials = 0     // C: short
val linespeciallist = arrayOfNulls<line_t>(MAXLINEANIMS)

// Parses command line parameters.
fun P_SpawnSpecials() {
    var sector: sector_t
    var i: Int
    var episode: Int

    episode = 1
    if (W_CheckNumForName("texture2") >= 0)
        episode = 2

    // See if -TIMER needs to be used.
    levelTimer = false

    i = M_CheckParm("-avg")
    if (i != 0 && deathmatch != 0) {
        levelTimer = true
        levelTimeCount = 20 * 60 * 35
    }

    i = M_CheckParm("-timer")
    if (i != 0 && deathmatch != 0) {
        val time: Int
        time = (myargv[i + 1].toIntOrNull() ?: 0) * 60 * 35
        levelTimer = true
        levelTimeCount = time
    }

    //	Init special SECTORs.
    i = 0
    while (i < numsectors) {
        sector = sectors[i]
        if (sector.special == 0) {
            i++
            continue
        }

        when (sector.special) {
            1 ->
                // FLICKERING LIGHTS
                P_SpawnLightFlash(sector)

            2 ->
                // STROBE FAST
                P_SpawnStrobeFlash(sector, FASTDARK, 0)

            3 ->
                // STROBE SLOW
                P_SpawnStrobeFlash(sector, SLOWDARK, 0)

            4 -> {
                // STROBE FAST/DEATH SLIME
                P_SpawnStrobeFlash(sector, FASTDARK, 0)
                sector.special = 4
            }

            8 ->
                // GLOWING LIGHT
                P_SpawnGlowingLight(sector)

            9 ->
                // SECRET SECTOR
                totalsecret++

            10 ->
                // DOOR CLOSE IN 30 SECONDS
                P_SpawnDoorCloseIn30(sector)

            12 ->
                // SYNC STROBE SLOW
                P_SpawnStrobeFlash(sector, SLOWDARK, 1)

            13 ->
                // SYNC STROBE FAST
                P_SpawnStrobeFlash(sector, FASTDARK, 1)

            14 ->
                // DOOR RAISE IN 5 MINUTES
                P_SpawnDoorRaiseIn5Mins(sector, i)

            17 ->
                P_SpawnFireFlicker(sector)
        }
        i++
    }

    //	Init line EFFECTs
    numlinespecials = 0
    i = 0
    while (i < numlines) {
        when (lines[i].special) {
            48 -> {
                // EFFECT FIRSTCOL SCROLL+
                linespeciallist[numlinespecials] = lines[i]
                numlinespecials++
            }
        }
        i++
    }

    //	Init other misc stuff
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
        // memset(&buttonlist[i],0,sizeof(button_t));
        buttonlist[i] = button_t()
        i++
    }

    // UNUSED: no horizonal sliders.
    //	P_InitSlidingDoorFrames();
}
