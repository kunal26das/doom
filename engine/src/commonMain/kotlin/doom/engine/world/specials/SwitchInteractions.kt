
package doom.engine.world.specials

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_SWTCHN
import doom.engine.audio.SFX_SWTCHX
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.gExitLevel
import doom.engine.gameplay.gSecretExitLevel
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.REGISTERED
import doom.engine.rendering.resources.rTextureNumForName
import doom.engine.world.ML_SECRET
import doom.engine.world.MapLine
import doom.engine.world.lighting.evLightTurnOn
import doom.engine.world.movers.evBuildStairs
import doom.engine.world.movers.evDoCeiling
import doom.engine.world.movers.evDoDoor
import doom.engine.world.movers.evDoFloor
import doom.engine.world.movers.evDoLockedDoor
import doom.engine.world.movers.evDoPlat
import doom.engine.world.movers.evVerticalDoor
import doom.engine.world.movers.BLAZE_CLOSE
import doom.engine.world.movers.BLAZE_DWUS
import doom.engine.world.movers.BLAZE_OPEN
import doom.engine.world.movers.BLAZE_RAISE
import doom.engine.world.movers.BUILD8
import doom.engine.world.movers.CLOSE
import doom.engine.world.movers.CRUSH_AND_RAISE
import doom.engine.world.movers.DOWN_WAIT_UP_STAY
import doom.engine.world.movers.LOWER_FLOOR
import doom.engine.world.movers.LOWER_FLOOR_TO_LOWEST
import doom.engine.world.movers.LOWER_TO_FLOOR
import doom.engine.world.movers.NORMAL
import doom.engine.world.movers.OPEN
import doom.engine.world.movers.RAISE_AND_CHANGE
import doom.engine.world.movers.RAISE_FLOOR
import doom.engine.world.movers.RAISE_FLOOR512
import doom.engine.world.movers.RAISE_FLOOR_CRUSH
import doom.engine.world.movers.RAISE_FLOOR_TO_NEAREST
import doom.engine.world.movers.RAISE_FLOOR_TURBO
import doom.engine.world.movers.RAISE_TO_NEAREST_AND_CHANGE
import doom.engine.world.movers.TURBO16
import doom.engine.world.movers.TURBO_LOWER
import doom.engine.world.sides

internal val DoomEngineCore.alphSwitchList
    get() = stateSwitch.alphSwitchList

internal val DoomEngineCore.switchlist
    get() = stateSwitch.switchlist
internal var DoomEngineCore.numswitches
    get() = stateSwitch.numswitches
    set(value) { stateSwitch.numswitches = value }

internal fun DoomEngineCore.pInitSwitchList() {
    var i: Int
    var index: Int
    var episode: Int

    episode = 1

    if (gamemode == REGISTERED)
        episode = 2
    else
        if (gamemode == COMMERCIAL)
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
            switchlist[index] = rTextureNumForName(alphSwitchList[i].name1)
            index++
            switchlist[index] = rTextureNumForName(alphSwitchList[i].name2)
            index++
        }
        i++
    }
}

internal fun DoomEngineCore.pStartButton(line: MapLine, w: Int, texture: Int, time: Int) {
    var i: Int

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

    iError("P_StartButton: no button slots left!")
}

internal fun DoomEngineCore.pChangeSwitchTexture(line: MapLine, useAgain: Int) {
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

    sound = SFX_SWTCHN

    if (line.special == 11)
        sound = SFX_SWTCHX

    i = 0
    while (i < numswitches * 2) {
        if (switchlist[i] == texTop) {
            sStartSound(buttonlist[0].soundorg, sound)
            sides[line.sidenum[0]].toptexture = switchlist[i xor 1]

            if (useAgain != 0)
                pStartButton(line, TOP, switchlist[i], BUTTONTIME)

            return
        } else {
            if (switchlist[i] == texMid) {
                sStartSound(buttonlist[0].soundorg, sound)
                sides[line.sidenum[0]].midtexture = switchlist[i xor 1]

                if (useAgain != 0)
                    pStartButton(line, MIDDLE, switchlist[i], BUTTONTIME)

                return
            } else {
                if (switchlist[i] == texBot) {
                    sStartSound(buttonlist[0].soundorg, sound)
                    sides[line.sidenum[0]].bottomtexture = switchlist[i xor 1]

                    if (useAgain != 0)
                        pStartButton(line, BOTTOM, switchlist[i], BUTTONTIME)

                    return
                }
            }
        }
        i++
    }
}

internal fun DoomEngineCore.pUseSpecialLine(thing: Actor, line: MapLine, side: Int): Boolean {
    if (side != 0) {
        when (line.special) {
            124 -> {
            }

            else ->
                return false
        }
    }

    if (thing.player == null) {
        if ((line.flags and ML_SECRET) != 0)
            return false

        when (line.special) {
            1,
            32,
            33,
            34 -> {}

            else ->
                return false
        }
    }

    when (line.special) {
        1,
        26,
        27,
        28,

        31,
        32,
        33,
        34,

        117,
        118 ->
            evVerticalDoor(line, thing)


        7 -> {
            if (evBuildStairs(line, BUILD8) != 0)
                pChangeSwitchTexture(line, 0)
        }

        9 -> {
            if (evDoDonut(line) != 0)
                pChangeSwitchTexture(line, 0)
        }

        11 -> {
            pChangeSwitchTexture(line, 0)
            gExitLevel()
        }

        14 -> {
            if (evDoPlat(line, RAISE_AND_CHANGE, 32) != 0)
                pChangeSwitchTexture(line, 0)
        }

        15 -> {
            if (evDoPlat(line, RAISE_AND_CHANGE, 24) != 0)
                pChangeSwitchTexture(line, 0)
        }

        18 -> {
            if (evDoFloor(line, RAISE_FLOOR_TO_NEAREST) != 0)
                pChangeSwitchTexture(line, 0)
        }

        20 -> {
            if (evDoPlat(line, RAISE_TO_NEAREST_AND_CHANGE, 0) != 0)
                pChangeSwitchTexture(line, 0)
        }

        21 -> {
            if (evDoPlat(line, DOWN_WAIT_UP_STAY, 0) != 0)
                pChangeSwitchTexture(line, 0)
        }

        23 -> {
            if (evDoFloor(line, LOWER_FLOOR_TO_LOWEST) != 0)
                pChangeSwitchTexture(line, 0)
        }

        29 -> {
            if (evDoDoor(line, NORMAL) != 0)
                pChangeSwitchTexture(line, 0)
        }

        41 -> {
            if (evDoCeiling(line, LOWER_TO_FLOOR) != 0)
                pChangeSwitchTexture(line, 0)
        }

        71 -> {
            if (evDoFloor(line, TURBO_LOWER) != 0)
                pChangeSwitchTexture(line, 0)
        }

        49 -> {
            if (evDoCeiling(line, CRUSH_AND_RAISE) != 0)
                pChangeSwitchTexture(line, 0)
        }

        50 -> {
            if (evDoDoor(line, CLOSE) != 0)
                pChangeSwitchTexture(line, 0)
        }

        51 -> {
            pChangeSwitchTexture(line, 0)
            gSecretExitLevel()
        }

        55 -> {
            if (evDoFloor(line, RAISE_FLOOR_CRUSH) != 0)
                pChangeSwitchTexture(line, 0)
        }

        101 -> {
            if (evDoFloor(line, RAISE_FLOOR) != 0)
                pChangeSwitchTexture(line, 0)
        }

        102 -> {
            if (evDoFloor(line, LOWER_FLOOR) != 0)
                pChangeSwitchTexture(line, 0)
        }

        103 -> {
            if (evDoDoor(line, OPEN) != 0)
                pChangeSwitchTexture(line, 0)
        }

        111 -> {
            if (evDoDoor(line, BLAZE_RAISE) != 0)
                pChangeSwitchTexture(line, 0)
        }

        112 -> {
            if (evDoDoor(line, BLAZE_OPEN) != 0)
                pChangeSwitchTexture(line, 0)
        }

        113 -> {
            if (evDoDoor(line, BLAZE_CLOSE) != 0)
                pChangeSwitchTexture(line, 0)
        }

        122 -> {
            if (evDoPlat(line, BLAZE_DWUS, 0) != 0)
                pChangeSwitchTexture(line, 0)
        }

        127 -> {
            if (evBuildStairs(line, TURBO16) != 0)
                pChangeSwitchTexture(line, 0)
        }

        131 -> {
            if (evDoFloor(line, RAISE_FLOOR_TURBO) != 0)
                pChangeSwitchTexture(line, 0)
        }

        133,
        135,
        137 -> {
            if (evDoLockedDoor(line, BLAZE_OPEN, thing) != 0)
                pChangeSwitchTexture(line, 0)
        }

        140 -> {
            if (evDoFloor(line, RAISE_FLOOR512) != 0)
                pChangeSwitchTexture(line, 0)
        }

        42 -> {
            if (evDoDoor(line, CLOSE) != 0)
                pChangeSwitchTexture(line, 1)
        }

        43 -> {
            if (evDoCeiling(line, LOWER_TO_FLOOR) != 0)
                pChangeSwitchTexture(line, 1)
        }

        45 -> {
            if (evDoFloor(line, LOWER_FLOOR) != 0)
                pChangeSwitchTexture(line, 1)
        }

        60 -> {
            if (evDoFloor(line, LOWER_FLOOR_TO_LOWEST) != 0)
                pChangeSwitchTexture(line, 1)
        }

        61 -> {
            if (evDoDoor(line, OPEN) != 0)
                pChangeSwitchTexture(line, 1)
        }

        62 -> {
            if (evDoPlat(line, DOWN_WAIT_UP_STAY, 1) != 0)
                pChangeSwitchTexture(line, 1)
        }

        63 -> {
            if (evDoDoor(line, NORMAL) != 0)
                pChangeSwitchTexture(line, 1)
        }

        64 -> {
            if (evDoFloor(line, RAISE_FLOOR) != 0)
                pChangeSwitchTexture(line, 1)
        }

        66 -> {
            if (evDoPlat(line, RAISE_AND_CHANGE, 24) != 0)
                pChangeSwitchTexture(line, 1)
        }

        67 -> {
            if (evDoPlat(line, RAISE_AND_CHANGE, 32) != 0)
                pChangeSwitchTexture(line, 1)
        }

        65 -> {
            if (evDoFloor(line, RAISE_FLOOR_CRUSH) != 0)
                pChangeSwitchTexture(line, 1)
        }

        68 -> {
            if (evDoPlat(line, RAISE_TO_NEAREST_AND_CHANGE, 0) != 0)
                pChangeSwitchTexture(line, 1)
        }

        69 -> {
            if (evDoFloor(line, RAISE_FLOOR_TO_NEAREST) != 0)
                pChangeSwitchTexture(line, 1)
        }

        70 -> {
            if (evDoFloor(line, TURBO_LOWER) != 0)
                pChangeSwitchTexture(line, 1)
        }

        114 -> {
            if (evDoDoor(line, BLAZE_RAISE) != 0)
                pChangeSwitchTexture(line, 1)
        }

        115 -> {
            if (evDoDoor(line, BLAZE_OPEN) != 0)
                pChangeSwitchTexture(line, 1)
        }

        116 -> {
            if (evDoDoor(line, BLAZE_CLOSE) != 0)
                pChangeSwitchTexture(line, 1)
        }

        123 -> {
            if (evDoPlat(line, BLAZE_DWUS, 0) != 0)
                pChangeSwitchTexture(line, 1)
        }

        132 -> {
            if (evDoFloor(line, RAISE_FLOOR_TURBO) != 0)
                pChangeSwitchTexture(line, 1)
        }

        99,
        134,
        136 -> {
            if (evDoLockedDoor(line, BLAZE_OPEN, thing) != 0)
                pChangeSwitchTexture(line, 1)
        }

        138 -> {
            evLightTurnOn(line, 255)
            pChangeSwitchTexture(line, 1)
        }

        139 -> {
            evLightTurnOn(line, 35)
            pChangeSwitchTexture(line, 1)
        }
    }

    return true
}
