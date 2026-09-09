// Port of linuxdoom-1.10 p_switch.c -- switches, buttons. Two-state animation. Exits.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


internal val DoomEngineCore.alphSwitchList
    get() = stateSwitch.alphSwitchList

internal val DoomEngineCore.switchlist
    get() = stateSwitch.switchlist
internal var DoomEngineCore.numswitches
    get() = stateSwitch.numswitches
    set(value) { stateSwitch.numswitches = value }

//
// P_InitSwitchList
// Only called at game initialization.
//
internal fun DoomEngineCore.P_InitSwitchList() {
    var i: Int
    var index: Int
    var episode: Int

    episode = 1

    if (gamemode == registered)
        episode = 2
    else
        if (gamemode == commercial)
            episode = 3

    index = 0
    i = 0
    while (i < MAXSWITCHES) {
        if (alphSwitchList[i].episode == 0) {
            numswitches = index / 2
            switchlist[index] = -1
            break
        }

        if (alphSwitchList[i].episode <= episode) {
            switchlist[index] = R_TextureNumForName(alphSwitchList[i].name1)
            index++
            switchlist[index] = R_TextureNumForName(alphSwitchList[i].name2)
            index++
        }
        i++
    }
}

//
// Start a button counting down till it turns off.
//
internal fun DoomEngineCore.P_StartButton(line: line_t, w: Int, texture: Int, time: Int) {
    var i: Int

    // See if button is already pressed
    i = 0
    while (i < MAXBUTTONS) {
        if (buttonlist[i].btimer != 0
            && buttonlist[i].line === line) {
            return
        }
        i++
    }

    i = 0
    while (i < MAXBUTTONS) {
        if (buttonlist[i].btimer == 0) {
            buttonlist[i].line = line
            buttonlist[i].where = w
            buttonlist[i].btexture = texture
            buttonlist[i].btimer = time
            buttonlist[i].soundorg = line.frontsector!!.soundorg
            return
        }
        i++
    }

    I_Error("P_StartButton: no button slots left!")
}

//
// Function that changes wall texture.
// Tell it if switch is ok to use again (1=yes, it's a button).
//
internal fun DoomEngineCore.P_ChangeSwitchTexture(line: line_t, useAgain: Int) {
    val texTop: Int
    val texMid: Int
    val texBot: Int
    var i: Int
    var sound: Int

    if (useAgain == 0)
        line.special = 0

    texTop = sides[line.sidenum[0]].toptexture
    texMid = sides[line.sidenum[0]].midtexture
    texBot = sides[line.sidenum[0]].bottomtexture

    sound = sfx_swtchn

    // EXIT SWITCH?
    if (line.special == 11)
        sound = sfx_swtchx

    i = 0
    while (i < numswitches * 2) {
        if (switchlist[i] == texTop) {
            // (vanilla quirk: C uses buttonlist->soundorg, i.e. always
            //  buttonlist[0]'s origin, whatever it currently holds)
            S_StartSound(buttonlist[0].soundorg, sound)
            sides[line.sidenum[0]].toptexture = switchlist[i xor 1]

            if (useAgain != 0)
                P_StartButton(line, top, switchlist[i], BUTTONTIME)

            return
        } else {
            if (switchlist[i] == texMid) {
                S_StartSound(buttonlist[0].soundorg, sound)
                sides[line.sidenum[0]].midtexture = switchlist[i xor 1]

                if (useAgain != 0)
                    P_StartButton(line, middle, switchlist[i], BUTTONTIME)

                return
            } else {
                if (switchlist[i] == texBot) {
                    S_StartSound(buttonlist[0].soundorg, sound)
                    sides[line.sidenum[0]].bottomtexture = switchlist[i xor 1]

                    if (useAgain != 0)
                        P_StartButton(line, bottom, switchlist[i], BUTTONTIME)

                    return
                }
            }
        }
        i++
    }
}

//
// P_UseSpecialLine
// Called when a thing uses a special line.
// Only the front sides of lines are usable.
//
internal fun DoomEngineCore.P_UseSpecialLine(thing: mobj_t, line: line_t, side: Int): Boolean {
    // Err...
    // Use the back sides of VERY SPECIAL lines...
    if (side != 0) {
        when (line.special) {
            124 -> {
                // Sliding door open&close
                // UNUSED?
            }

            else ->
                return false
        }
    }

    // Switches that other things can activate.
    if (thing.player == null) {
        // never open secret doors
        if ((line.flags and ML_SECRET) != 0)
            return false

        when (line.special) {
            1,      // MANUAL DOOR RAISE
            32,     // MANUAL BLUE
            33,     // MANUAL RED
            34 -> {}    // MANUAL YELLOW

            else ->
                return false
        }
    }

    // do something
    when (line.special) {
        // MANUALS
        1,      // Vertical Door
        26,     // Blue Door/Locked
        27,     // Yellow Door /Locked
        28,     // Red Door /Locked

        31,     // Manual door open
        32,     // Blue locked door open
        33,     // Red locked door open
        34,     // Yellow locked door open

        117,    // Blazing door raise
        118 ->  // Blazing door open
            EV_VerticalDoor(line, thing)

        // UNUSED - Door Slide Open&Close
        // case 124:
        // EV_SlidingDoor (line, thing);
        // break;

        // SWITCHES
        7 -> {
            // Build Stairs
            if (EV_BuildStairs(line, build8) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        9 -> {
            // Change Donut
            if (EV_DoDonut(line) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        11 -> {
            // Exit level
            P_ChangeSwitchTexture(line, 0)
            G_ExitLevel()
        }

        14 -> {
            // Raise Floor 32 and change texture
            if (EV_DoPlat(line, raiseAndChange, 32) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        15 -> {
            // Raise Floor 24 and change texture
            if (EV_DoPlat(line, raiseAndChange, 24) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        18 -> {
            // Raise Floor to next highest floor
            if (EV_DoFloor(line, raiseFloorToNearest) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        20 -> {
            // Raise Plat next highest floor and change texture
            if (EV_DoPlat(line, raiseToNearestAndChange, 0) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        21 -> {
            // PlatDownWaitUpStay
            if (EV_DoPlat(line, downWaitUpStay, 0) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        23 -> {
            // Lower Floor to Lowest
            if (EV_DoFloor(line, lowerFloorToLowest) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        29 -> {
            // Raise Door
            if (EV_DoDoor(line, normal) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        41 -> {
            // Lower Ceiling to Floor
            if (EV_DoCeiling(line, lowerToFloor) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        71 -> {
            // Turbo Lower Floor
            if (EV_DoFloor(line, turboLower) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        49 -> {
            // Ceiling Crush And Raise
            if (EV_DoCeiling(line, crushAndRaise) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        50 -> {
            // Close Door
            if (EV_DoDoor(line, close) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        51 -> {
            // Secret EXIT
            P_ChangeSwitchTexture(line, 0)
            G_SecretExitLevel()
        }

        55 -> {
            // Raise Floor Crush
            if (EV_DoFloor(line, raiseFloorCrush) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        101 -> {
            // Raise Floor
            if (EV_DoFloor(line, raiseFloor) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        102 -> {
            // Lower Floor to Surrounding floor height
            if (EV_DoFloor(line, lowerFloor) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        103 -> {
            // Open Door
            if (EV_DoDoor(line, open) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        111 -> {
            // Blazing Door Raise (faster than TURBO!)
            if (EV_DoDoor(line, blazeRaise) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        112 -> {
            // Blazing Door Open (faster than TURBO!)
            if (EV_DoDoor(line, blazeOpen) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        113 -> {
            // Blazing Door Close (faster than TURBO!)
            if (EV_DoDoor(line, blazeClose) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        122 -> {
            // Blazing PlatDownWaitUpStay
            if (EV_DoPlat(line, blazeDWUS, 0) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        127 -> {
            // Build Stairs Turbo 16
            if (EV_BuildStairs(line, turbo16) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        131 -> {
            // Raise Floor Turbo
            if (EV_DoFloor(line, raiseFloorTurbo) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        133,    // BlzOpenDoor BLUE
        135,    // BlzOpenDoor RED
        137 -> {    // BlzOpenDoor YELLOW
            if (EV_DoLockedDoor(line, blazeOpen, thing) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        140 -> {
            // Raise Floor 512
            if (EV_DoFloor(line, raiseFloor512) != 0)
                P_ChangeSwitchTexture(line, 0)
        }

        // BUTTONS
        42 -> {
            // Close Door
            if (EV_DoDoor(line, close) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        43 -> {
            // Lower Ceiling to Floor
            if (EV_DoCeiling(line, lowerToFloor) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        45 -> {
            // Lower Floor to Surrounding floor height
            if (EV_DoFloor(line, lowerFloor) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        60 -> {
            // Lower Floor to Lowest
            if (EV_DoFloor(line, lowerFloorToLowest) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        61 -> {
            // Open Door
            if (EV_DoDoor(line, open) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        62 -> {
            // PlatDownWaitUpStay
            if (EV_DoPlat(line, downWaitUpStay, 1) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        63 -> {
            // Raise Door
            if (EV_DoDoor(line, normal) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        64 -> {
            // Raise Floor to ceiling
            if (EV_DoFloor(line, raiseFloor) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        66 -> {
            // Raise Floor 24 and change texture
            if (EV_DoPlat(line, raiseAndChange, 24) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        67 -> {
            // Raise Floor 32 and change texture
            if (EV_DoPlat(line, raiseAndChange, 32) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        65 -> {
            // Raise Floor Crush
            if (EV_DoFloor(line, raiseFloorCrush) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        68 -> {
            // Raise Plat to next highest floor and change texture
            if (EV_DoPlat(line, raiseToNearestAndChange, 0) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        69 -> {
            // Raise Floor to next highest floor
            if (EV_DoFloor(line, raiseFloorToNearest) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        70 -> {
            // Turbo Lower Floor
            if (EV_DoFloor(line, turboLower) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        114 -> {
            // Blazing Door Raise (faster than TURBO!)
            if (EV_DoDoor(line, blazeRaise) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        115 -> {
            // Blazing Door Open (faster than TURBO!)
            if (EV_DoDoor(line, blazeOpen) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        116 -> {
            // Blazing Door Close (faster than TURBO!)
            if (EV_DoDoor(line, blazeClose) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        123 -> {
            // Blazing PlatDownWaitUpStay
            if (EV_DoPlat(line, blazeDWUS, 0) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        132 -> {
            // Raise Floor Turbo
            if (EV_DoFloor(line, raiseFloorTurbo) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        99,     // BlzOpenDoor BLUE
        134,    // BlzOpenDoor RED
        136 -> {    // BlzOpenDoor YELLOW
            if (EV_DoLockedDoor(line, blazeOpen, thing) != 0)
                P_ChangeSwitchTexture(line, 1)
        }

        138 -> {
            // Light Turn On
            EV_LightTurnOn(line, 255)
            P_ChangeSwitchTexture(line, 1)
        }

        139 -> {
            // Light Turn Off
            EV_LightTurnOn(line, 35)
            P_ChangeSwitchTexture(line, 1)
        }
    }

    return true
}
