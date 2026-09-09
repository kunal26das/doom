
package doom.engine.gameplay

import doom.engine.KEY_F12
import doom.engine.KEY_PAUSE
import doom.engine.audio.sPauseSound
import doom.engine.audio.sResumeSound
import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_TELEPT
import doom.engine.automap.amResponder
import doom.engine.automap.amStop
import doom.engine.automap.amTicker
import doom.engine.automap.automapactive
import doom.engine.capture.mScreenShot
import doom.engine.configuration.mCheckParm
import doom.engine.configuration.myargc
import doom.engine.configuration.myargv
import doom.engine.core.dAdvanceDemo
import doom.engine.core.dPageTicker
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.core.iGetTime
import doom.engine.core.iQuit
import doom.engine.core.mReadFile
import doom.engine.core.mWriteFile
import doom.engine.core.fastparm
import doom.engine.core.nomonsters
import doom.engine.core.respawnparm
import doom.engine.core.singletics
import doom.engine.core.wipegamestate
import doom.engine.finale.fResponder
import doom.engine.finale.fStartFinale
import doom.engine.finale.fTicker
import doom.engine.gameplay.actors.MF_SHADOW
import doom.engine.gameplay.actors.MT_BRUISERSHOT
import doom.engine.gameplay.actors.MT_HEADSHOT
import doom.engine.gameplay.actors.MT_TFOG
import doom.engine.gameplay.actors.MT_TROOPSHOT
import doom.engine.gameplay.actors.pRemoveMobj
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.gameplay.actors.pSpawnPlayer
import doom.engine.gameplay.actors.S_SARG_PAIN2
import doom.engine.gameplay.actors.S_SARG_RUN1
import doom.engine.gameplay.actors.mobjinfo
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.interactions.maxammo
import doom.engine.gameplay.player.NUMPSPRITES
import doom.engine.gameplay.player.PST_DEAD
import doom.engine.gameplay.player.PST_LIVE
import doom.engine.gameplay.player.PST_REBORN
import doom.engine.geometry.ANG45
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.hud.huResponder
import doom.engine.hud.huTicker
import doom.engine.hud.huDequeueChatChar
import doom.engine.hud.playerNames
import doom.engine.input.BTS_PAUSE
import doom.engine.input.BTS_SAVEGAME
import doom.engine.input.BTS_SAVEMASK
import doom.engine.input.BTS_SAVESHIFT
import doom.engine.input.BT_ATTACK
import doom.engine.input.BT_CHANGE
import doom.engine.input.BT_SPECIAL
import doom.engine.input.BT_SPECIALMASK
import doom.engine.input.BT_USE
import doom.engine.input.BT_WEAPONSHIFT
import doom.engine.input.EngineEvent
import doom.engine.input.TicCommand
import doom.engine.input.EV_JOYSTICK
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_KEYUP
import doom.engine.input.EV_MOUSE
import doom.engine.intermission.wiStart
import doom.engine.intermission.wiTicker
import doom.engine.menu.mStartControlPanel
import doom.engine.menu.mouseSensitivity
import doom.engine.rendering.rExecuteSetViewSize
import doom.engine.rendering.rFillBackScreen
import doom.engine.rendering.SKYFLATNAME
import doom.engine.rendering.resources.rFlatNumForName
import doom.engine.rendering.resources.rTextureNumForName
import doom.engine.rendering.setsizeneeded
import doom.engine.rendering.skyflatnum
import doom.engine.rendering.skytexture
import doom.engine.resources.GGSAVED
import doom.engine.resources.SAVEGAMENAME
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.wCheckNumForName
import doom.engine.resources.i8
import doom.engine.resources.str
import doom.engine.resources.u8
import doom.engine.savegame.pArchivePlayers
import doom.engine.savegame.pArchiveSpecials
import doom.engine.savegame.pArchiveThinkers
import doom.engine.savegame.pArchiveWorld
import doom.engine.savegame.pUnArchivePlayers
import doom.engine.savegame.pUnArchiveSpecials
import doom.engine.savegame.pUnArchiveThinkers
import doom.engine.savegame.pUnArchiveWorld
import doom.engine.savegame.saveP
import doom.engine.simulation.BACKUPTICS
import doom.engine.simulation.mClearRandom
import doom.engine.simulation.pRandom
import doom.engine.simulation.pTicker
import doom.engine.simulation.leveltime
import doom.engine.simulation.maketic
import doom.engine.simulation.rndindex
import doom.engine.simulation.ticdup
import doom.engine.statusbar.stResponder
import doom.engine.statusbar.stTicker
import doom.engine.world.MAXHEALTH
import doom.engine.world.MapThingSpawn
import doom.engine.world.pSetupLevel
import doom.engine.world.collision.pCheckPosition
import doom.engine.world.collision.findSubsector
import doom.engine.world.deathmatchP
import doom.engine.world.deathmatchstarts
import doom.engine.world.playerstarts

internal const val VERSION = 109

private const val SAVEGAMESIZE = 0x2c000
private const val SAVESTRINGSIZE = 24

internal var DoomEngineCore.gameaction
    get() = stateGameSession.gameaction
    set(value) { stateGameSession.gameaction = value }
internal var DoomEngineCore.gamestate
    get() = stateGameSession.gamestate
    set(value) { stateGameSession.gamestate = value }
internal var DoomEngineCore.gameskill
    get() = stateGameSession.gameskill
    set(value) { stateGameSession.gameskill = value }
internal var DoomEngineCore.respawnmonsters
    get() = stateGameSession.respawnmonsters
    set(value) { stateGameSession.respawnmonsters = value }
internal var DoomEngineCore.gameepisode
    get() = stateGameSession.gameepisode
    set(value) { stateGameSession.gameepisode = value }
internal var DoomEngineCore.gamemap
    get() = stateGameSession.gamemap
    set(value) { stateGameSession.gamemap = value }

internal var DoomEngineCore.paused
    get() = stateGameSession.paused
    set(value) { stateGameSession.paused = value }
internal var DoomEngineCore.sendpause
    get() = stateGameSession.sendpause
    set(value) { stateGameSession.sendpause = value }
internal var DoomEngineCore.sendsave
    get() = stateGameSession.sendsave
    set(value) { stateGameSession.sendsave = value }
internal var DoomEngineCore.usergame
    get() = stateGameSession.usergame
    set(value) { stateGameSession.usergame = value }

internal var DoomEngineCore.timingdemo
    get() = stateGameSession.timingdemo
    set(value) { stateGameSession.timingdemo = value }
internal var DoomEngineCore.nodrawers
    get() = stateGameSession.nodrawers
    set(value) { stateGameSession.nodrawers = value }
internal var DoomEngineCore.noblit
    get() = stateGameSession.noblit
    set(value) { stateGameSession.noblit = value }
internal var DoomEngineCore.starttime
    get() = stateGameSession.starttime
    set(value) { stateGameSession.starttime = value }

internal var DoomEngineCore.viewactive
    get() = stateGameSession.viewactive
    set(value) { stateGameSession.viewactive = value }

internal var DoomEngineCore.deathmatch
    get() = stateGameSession.deathmatch
    set(value) { stateGameSession.deathmatch = value }
internal var DoomEngineCore.netgame
    get() = stateGameSession.netgame
    set(value) { stateGameSession.netgame = value }
internal val DoomEngineCore.playeringame
    get() = stateGameSession.playeringame
internal val DoomEngineCore.players
    get() = stateGameSession.players

internal var DoomEngineCore.consoleplayer
    get() = stateGameSession.consoleplayer
    set(value) { stateGameSession.consoleplayer = value }
internal var DoomEngineCore.displayplayer
    get() = stateGameSession.displayplayer
    set(value) { stateGameSession.displayplayer = value }
internal var DoomEngineCore.gametic
    get() = stateGameSession.gametic
    set(value) { stateGameSession.gametic = value }
internal var DoomEngineCore.levelstarttic
    get() = stateGameSession.levelstarttic
    set(value) { stateGameSession.levelstarttic = value }
internal var DoomEngineCore.totalkills
    get() = stateGameSession.totalkills
    set(value) { stateGameSession.totalkills = value }
internal var DoomEngineCore.totalitems
    get() = stateGameSession.totalitems
    set(value) { stateGameSession.totalitems = value }
internal var DoomEngineCore.totalsecret
    get() = stateGameSession.totalsecret
    set(value) { stateGameSession.totalsecret = value }

internal var DoomEngineCore.demoname
    get() = stateGameSession.demoname
    set(value) { stateGameSession.demoname = value }
internal var DoomEngineCore.demorecording
    get() = stateGameSession.demorecording
    set(value) { stateGameSession.demorecording = value }
internal var DoomEngineCore.demoplayback
    get() = stateGameSession.demoplayback
    set(value) { stateGameSession.demoplayback = value }
internal var DoomEngineCore.netdemo
    get() = stateGameSession.netdemo
    set(value) { stateGameSession.netdemo = value }
internal var DoomEngineCore.demobuffer: ByteArray?
    get() = stateGameSession.demobuffer
    set(value) { stateGameSession.demobuffer = value }
internal var DoomEngineCore.demoP
    get() = stateGameSession.demoP
    set(value) { stateGameSession.demoP = value }
internal var DoomEngineCore.demoend
    get() = stateGameSession.demoend
    set(value) { stateGameSession.demoend = value }
internal var DoomEngineCore.singledemo
    get() = stateGameSession.singledemo
    set(value) { stateGameSession.singledemo = value }

internal var DoomEngineCore.precache
    get() = stateGameSession.precache
    set(value) { stateGameSession.precache = value }

internal val DoomEngineCore.wminfo
    get() = stateGameSession.wminfo

internal val DoomEngineCore.consistancy
    get() = stateGameSession.consistancy

internal var DoomEngineCore.savebuffer
    get() = stateGameSession.savebuffer
    set(value) { stateGameSession.savebuffer = value }

internal var DoomEngineCore.keyRight
    get() = stateGameSession.keyRight
    set(value) { stateGameSession.keyRight = value }
internal var DoomEngineCore.keyLeft
    get() = stateGameSession.keyLeft
    set(value) { stateGameSession.keyLeft = value }

internal var DoomEngineCore.keyUp
    get() = stateGameSession.keyUp
    set(value) { stateGameSession.keyUp = value }
internal var DoomEngineCore.keyDown
    get() = stateGameSession.keyDown
    set(value) { stateGameSession.keyDown = value }
internal var DoomEngineCore.keyStrafeleft
    get() = stateGameSession.keyStrafeleft
    set(value) { stateGameSession.keyStrafeleft = value }
internal var DoomEngineCore.keyStraferight
    get() = stateGameSession.keyStraferight
    set(value) { stateGameSession.keyStraferight = value }
internal var DoomEngineCore.keyFire
    get() = stateGameSession.keyFire
    set(value) { stateGameSession.keyFire = value }
internal var DoomEngineCore.keyUse
    get() = stateGameSession.keyUse
    set(value) { stateGameSession.keyUse = value }
internal var DoomEngineCore.keyStrafe
    get() = stateGameSession.keyStrafe
    set(value) { stateGameSession.keyStrafe = value }
internal var DoomEngineCore.keySpeed
    get() = stateGameSession.keySpeed
    set(value) { stateGameSession.keySpeed = value }

internal var DoomEngineCore.mousebfire
    get() = stateGameSession.mousebfire
    set(value) { stateGameSession.mousebfire = value }
internal var DoomEngineCore.mousebstrafe
    get() = stateGameSession.mousebstrafe
    set(value) { stateGameSession.mousebstrafe = value }
internal var DoomEngineCore.mousebforward
    get() = stateGameSession.mousebforward
    set(value) { stateGameSession.mousebforward = value }

internal var DoomEngineCore.joybfire
    get() = stateGameSession.joybfire
    set(value) { stateGameSession.joybfire = value }
internal var DoomEngineCore.joybstrafe
    get() = stateGameSession.joybstrafe
    set(value) { stateGameSession.joybstrafe = value }
internal var DoomEngineCore.joybuse
    get() = stateGameSession.joybuse
    set(value) { stateGameSession.joybuse = value }
internal var DoomEngineCore.joybspeed
    get() = stateGameSession.joybspeed
    set(value) { stateGameSession.joybspeed = value }

private val DoomEngineCore.maxplmove: Int get() = forwardmove[1]

internal const val TURBOTHRESHOLD = 0x32

internal val DoomEngineCore.forwardmove
    get() = stateGameSession.forwardmove
internal val DoomEngineCore.sidemove
    get() = stateGameSession.sidemove
internal val DoomEngineCore.angleturn
    get() = stateGameSession.angleturn

private const val SLOWTURNTICS = 6

internal const val NUMKEYS = 256

internal val DoomEngineCore.gamekeydown
    get() = stateGameSession.gamekeydown
internal var DoomEngineCore.turnheld
    get() = stateGameSession.turnheld
    set(value) { stateGameSession.turnheld = value }

internal val DoomEngineCore.mousearray
    get() = stateGameSession.mousearray

internal val DoomEngineCore.mousebuttons get() = stateGameSession.mousebuttons

internal var DoomEngineCore.mousex
    get() = stateGameSession.mousex
    set(value) { stateGameSession.mousex = value }
internal var DoomEngineCore.mousey
    get() = stateGameSession.mousey
    set(value) { stateGameSession.mousey = value }

internal var DoomEngineCore.dclicktime
    get() = stateGameSession.dclicktime
    set(value) { stateGameSession.dclicktime = value }
internal var DoomEngineCore.dclickstate
    get() = stateGameSession.dclickstate
    set(value) { stateGameSession.dclickstate = value }
internal var DoomEngineCore.dclicks
    get() = stateGameSession.dclicks
    set(value) { stateGameSession.dclicks = value }
internal var DoomEngineCore.dclicktime2
    get() = stateGameSession.dclicktime2
    set(value) { stateGameSession.dclicktime2 = value }
internal var DoomEngineCore.dclickstate2
    get() = stateGameSession.dclickstate2
    set(value) { stateGameSession.dclickstate2 = value }
internal var DoomEngineCore.dclicks2
    get() = stateGameSession.dclicks2
    set(value) { stateGameSession.dclicks2 = value }

internal var DoomEngineCore.joyxmove
    get() = stateGameSession.joyxmove
    set(value) { stateGameSession.joyxmove = value }
internal var DoomEngineCore.joyymove
    get() = stateGameSession.joyymove
    set(value) { stateGameSession.joyymove = value }
internal val DoomEngineCore.joyarray
    get() = stateGameSession.joyarray

internal val DoomEngineCore.joybuttons get() = stateGameSession.joybuttons

internal var DoomEngineCore.savegameslot
    get() = stateGameSession.savegameslot
    set(value) { stateGameSession.savegameslot = value }
internal var DoomEngineCore.savedescription
    get() = stateGameSession.savedescription
    set(value) { stateGameSession.savedescription = value }

internal const val BODYQUESIZE = 32

internal val DoomEngineCore.bodyque
    get() = stateGameSession.bodyque
internal var DoomEngineCore.bodyqueslot
    get() = stateGameSession.bodyqueslot
    set(value) { stateGameSession.bodyqueslot = value }


internal fun DoomEngineCore.gCmdChecksum(cmd: TicCommand): Int {
    var sum = 0
    sum += (cmd.forwardmove and 0xff) or
        ((cmd.sidemove and 0xff) shl 8) or
        ((cmd.angleturn and 0xffff) shl 16)

    return sum
}

private val DoomEngineCore.emptycmd
    get() = stateGameSession.emptycmd
internal fun DoomEngineCore.iBaseTiccmd(): TicCommand = emptycmd

internal fun DoomEngineCore.gBuildTiccmd(cmd: TicCommand) {
    val base = iBaseTiccmd()
    cmd.copyFrom(base)

    cmd.consistancy =
        consistancy[consoleplayer][maketic % BACKUPTICS].toInt()

    val strafe = gamekeydown[keyStrafe] || mousebuttons[mousebstrafe] ||
        joybuttons[joybstrafe]
    val speed = if (gamekeydown[keySpeed] || joybuttons[joybspeed]) 1 else 0

    var forward = 0
    var side = 0

    if (joyxmove < 0 ||
        joyxmove > 0 ||
        gamekeydown[keyRight] ||
        gamekeydown[keyLeft])
        turnheld += ticdup
    else
        turnheld = 0

    val tspeed: Int
    if (turnheld < SLOWTURNTICS)
        tspeed = 2
    else
        tspeed = speed

    if (strafe) {
        if (gamekeydown[keyRight]) {
            side += sidemove[speed]
        }
        if (gamekeydown[keyLeft]) {
            side -= sidemove[speed]
        }
        if (joyxmove > 0)
            side += sidemove[speed]
        if (joyxmove < 0)
            side -= sidemove[speed]
    } else {
        if (gamekeydown[keyRight])
            cmd.angleturn -= angleturn[tspeed]
        if (gamekeydown[keyLeft])
            cmd.angleturn += angleturn[tspeed]
        if (joyxmove > 0)
            cmd.angleturn -= angleturn[tspeed]
        if (joyxmove < 0)
            cmd.angleturn += angleturn[tspeed]
    }

    if (gamekeydown[keyUp]) {
        forward += forwardmove[speed]
    }
    if (gamekeydown[keyDown]) {
        forward -= forwardmove[speed]
    }
    if (joyymove < 0)
        forward += forwardmove[speed]
    if (joyymove > 0)
        forward -= forwardmove[speed]
    if (gamekeydown[keyStraferight])
        side += sidemove[speed]
    if (gamekeydown[keyStrafeleft])
        side -= sidemove[speed]

    cmd.chatchar = huDequeueChatChar()

    if (gamekeydown[keyFire] || mousebuttons[mousebfire] ||
        joybuttons[joybfire])
        cmd.buttons = cmd.buttons or BT_ATTACK

    if (gamekeydown[keyUse] || joybuttons[joybuse]) {
        cmd.buttons = cmd.buttons or BT_USE
        dclicks = 0
    }

    for (i in 0 until NUMWEAPONS - 1)
        if (gamekeydown['1'.code + i]) {
            cmd.buttons = cmd.buttons or BT_CHANGE
            cmd.buttons = cmd.buttons or (i shl BT_WEAPONSHIFT)
            break
        }

    if (mousebuttons[mousebforward])
        forward += forwardmove[speed]

    if ((if (mousebuttons[mousebforward]) 1 else 0) != dclickstate && dclicktime > 1) {
        dclickstate = if (mousebuttons[mousebforward]) 1 else 0
        if (dclickstate != 0)
            dclicks++
        if (dclicks == 2) {
            cmd.buttons = cmd.buttons or BT_USE
            dclicks = 0
        } else
            dclicktime = 0
    } else {
        dclicktime += ticdup
        if (dclicktime > 20) {
            dclicks = 0
            dclickstate = 0
        }
    }

    val bstrafe =
        mousebuttons[mousebstrafe] ||
        joybuttons[joybstrafe]
    if ((if (bstrafe) 1 else 0) != dclickstate2 && dclicktime2 > 1) {
        dclickstate2 = if (bstrafe) 1 else 0
        if (dclickstate2 != 0)
            dclicks2++
        if (dclicks2 == 2) {
            cmd.buttons = cmd.buttons or BT_USE
            dclicks2 = 0
        } else
            dclicktime2 = 0
    } else {
        dclicktime2 += ticdup
        if (dclicktime2 > 20) {
            dclicks2 = 0
            dclickstate2 = 0
        }
    }

    forward += mousey
    if (strafe)
        side += mousex * 2
    else
        cmd.angleturn -= mousex * 0x8

    mousex = 0
    mousey = 0

    if (forward > maxplmove)
        forward = maxplmove
    else if (forward < -maxplmove)
        forward = -maxplmove
    if (side > maxplmove)
        side = maxplmove
    else if (side < -maxplmove)
        side = -maxplmove

    cmd.forwardmove += forward
    cmd.sidemove += side

    if (sendpause) {
        sendpause = false
        cmd.buttons = BT_SPECIAL or BTS_PAUSE
    }

    if (sendsave) {
        sendsave = false
        cmd.buttons = BT_SPECIAL or BTS_SAVEGAME or (savegameslot shl BTS_SAVESHIFT)
    }
}

internal fun DoomEngineCore.gDoLoadLevel() {
    skyflatnum = rFlatNumForName(SKYFLATNAME)

    if ((gamemode == COMMERCIAL) ||
        (gamemode == PACK_TNT) ||
        (gamemode == PACK_PLUT)) {
        skytexture = rTextureNumForName("SKY3")
        if (gamemap < 12)
            skytexture = rTextureNumForName("SKY1")
        else
            if (gamemap < 21)
                skytexture = rTextureNumForName("SKY2")
    }

    levelstarttic = gametic

    if (wipegamestate == GS_LEVEL)
        wipegamestate = -1

    gamestate = GS_LEVEL

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i] && players[i].playerstate == PST_DEAD)
            players[i].playerstate = PST_REBORN
        players[i].frags.fill(0)
    }

    pSetupLevel(gameepisode, gamemap)
    displayplayer = consoleplayer
    starttime = iGetTime()
    gameaction = GA_NOTHING

    gamekeydown.fill(false)
    joyxmove = 0
    joyymove = 0
    mousex = 0
    mousey = 0
    sendpause = false
    sendsave = false
    paused = false
    mousebuttons[0] = false
    joybuttons[0] = false
}

internal fun DoomEngineCore.gResponder(ev: EngineEvent): Boolean {
    if (gamestate == GS_LEVEL && ev.type == EV_KEYDOWN &&
        ev.data1 == KEY_F12 && (singledemo || deathmatch == 0)) {
        do {
            displayplayer++
            if (displayplayer == MAXPLAYERS)
                displayplayer = 0
        } while (!playeringame[displayplayer] && displayplayer != consoleplayer)
        return true
    }

    if (gameaction == GA_NOTHING && !singledemo &&
        (demoplayback || gamestate == GS_DEMOSCREEN)) {
        if (ev.type == EV_KEYDOWN ||
            (ev.type == EV_MOUSE && ev.data1 != 0) ||
            (ev.type == EV_JOYSTICK && ev.data1 != 0)) {
            mStartControlPanel()
            return true
        }
        return false
    }

    if (gamestate == GS_LEVEL) {
        if (huResponder(ev))
            return true
        if (stResponder(ev))
            return true
        if (amResponder(ev))
            return true
    }

    if (gamestate == GS_FINALE) {
        if (fResponder(ev))
            return true
    }

    when (ev.type) {
        EV_KEYDOWN -> {
            if (ev.data1 == KEY_PAUSE) {
                sendpause = true
                return true
            }
            if (ev.data1 < NUMKEYS)
                gamekeydown[ev.data1] = true
            return true
        }

        EV_KEYUP -> {
            if (ev.data1 < NUMKEYS)
                gamekeydown[ev.data1] = false
            return false
        }

        EV_MOUSE -> {
            mousebuttons[0] = (ev.data1 and 1) != 0
            mousebuttons[1] = (ev.data1 and 2) != 0
            mousebuttons[2] = (ev.data1 and 4) != 0
            mousex = ev.data2 * (mouseSensitivity + 5) / 10
            mousey = ev.data3 * (mouseSensitivity + 5) / 10
            return true
        }

        EV_JOYSTICK -> {
            joybuttons[0] = (ev.data1 and 1) != 0
            joybuttons[1] = (ev.data1 and 2) != 0
            joybuttons[2] = (ev.data1 and 4) != 0
            joybuttons[3] = (ev.data1 and 8) != 0
            joyxmove = ev.data2
            joyymove = ev.data3
            return true
        }

        else -> {}
    }

    return false
}

internal fun DoomEngineCore.gTicker() {
    for (i in 0 until MAXPLAYERS)
        if (playeringame[i] && players[i].playerstate == PST_REBORN)
            gDoReborn(i)

    while (gameaction != GA_NOTHING) {
        when (gameaction) {
            GA_LOADLEVEL -> gDoLoadLevel()
            GA_NEWGAME -> gDoNewGame()
            GA_LOADGAME -> gDoLoadGame()
            GA_SAVEGAME -> gDoSaveGame()
            GA_PLAYDEMO -> gDoPlayDemo()
            GA_COMPLETED -> gDoCompleted()
            GA_VICTORY -> fStartFinale()
            GA_WORLDDONE -> gDoWorldDone()
            GA_SCREENSHOT -> {
                mScreenShot()
                gameaction = GA_NOTHING
            }
            GA_NOTHING -> {}
        }
    }

    val buf = (gametic / ticdup) % BACKUPTICS

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            val cmd = players[i].cmd

            tickScheduler.copyCommand(i, buf, cmd)

            if (demoplayback)
                gReadDemoTiccmd(cmd)
            if (demorecording)
                gWriteDemoTiccmd(cmd)

            if (cmd.forwardmove > TURBOTHRESHOLD &&
                (gametic and 31) == 0 && ((gametic shr 5) and 3) == i) {
                players[consoleplayer].message = "${playerNames[i]} is turbo!"
            }

            if (netgame && !netdemo && gametic % ticdup == 0) {
                if (gametic > BACKUPTICS &&
                    consistancy[i][buf].toInt() != cmd.consistancy) {
                    iError("consistency failure (${cmd.consistancy} should be ${consistancy[i][buf]})")
                }
                if (players[i].mo != null)
                    consistancy[i][buf] = players[i].mo!!.x.toShort()
                else
                    consistancy[i][buf] = rndindex.toShort()
            }
        }
    }

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            if ((players[i].cmd.buttons and BT_SPECIAL) != 0) {
                when (players[i].cmd.buttons and BT_SPECIALMASK) {
                    BTS_PAUSE -> {
                        paused = !paused
                        if (paused)
                            sPauseSound()
                        else
                            sResumeSound()
                    }

                    BTS_SAVEGAME -> {
                        if (savedescription.isEmpty())
                            savedescription = "NET GAME"
                        savegameslot =
                            (players[i].cmd.buttons and BTS_SAVEMASK) shr BTS_SAVESHIFT
                        gameaction = GA_SAVEGAME
                    }
                }
            }
        }
    }

    when (gamestate) {
        GS_LEVEL -> {
            pTicker()
            stTicker()
            amTicker()
            huTicker()
        }

        GS_INTERMISSION -> wiTicker()

        GS_FINALE -> fTicker()

        GS_DEMOSCREEN -> dPageTicker()
    }
}


internal fun DoomEngineCore.gInitPlayer(player: Int) {
    gPlayerReborn(player)
}

internal fun DoomEngineCore.gPlayerFinishLevel(player: Int) {
    val p = players[player]

    p.powers.fill(0)
    p.cards.fill(false)
    p.mo!!.flags = p.mo!!.flags and MF_SHADOW.inv()
    p.extralight = 0
    p.fixedcolormap = 0
    p.damagecount = 0
    p.bonuscount = 0
}

internal fun DoomEngineCore.gPlayerReborn(player: Int) {
    val frags = IntArray(MAXPLAYERS)
    players[player].frags.copyInto(frags)
    val killcount = players[player].killcount
    val itemcount = players[player].itemcount
    val secretcount = players[player].secretcount

    val p = players[player]
    p.mo = null
    p.playerstate = 0
    p.cmd.forwardmove = 0
    p.cmd.sidemove = 0
    p.cmd.angleturn = 0
    p.cmd.consistancy = 0
    p.cmd.chatchar = 0
    p.cmd.buttons = 0
    p.viewz = 0
    p.viewheight = 0
    p.deltaviewheight = 0
    p.bob = 0
    p.health = 0
    p.armorpoints = 0
    p.armortype = 0
    p.powers.fill(0)
    p.cards.fill(false)
    p.backpack = false
    p.frags.fill(0)
    p.readyweapon = 0
    p.pendingweapon = 0
    p.weaponowned.fill(false)
    p.ammo.fill(0)
    p.maxammo.fill(0)
    p.attackdown = false
    p.usedown = false
    p.cheats = 0
    p.refire = 0
    p.killcount = 0
    p.itemcount = 0
    p.secretcount = 0
    p.message = null
    p.damagecount = 0
    p.bonuscount = 0
    p.attacker = null
    p.extralight = 0
    p.fixedcolormap = 0
    p.colormap = 0
    for (i in 0 until NUMPSPRITES) {
        p.psprites[i].state = null
        p.psprites[i].tics = 0
        p.psprites[i].sx = 0
        p.psprites[i].sy = 0
    }
    p.didsecret = false

    frags.copyInto(players[player].frags)
    players[player].killcount = killcount
    players[player].itemcount = itemcount
    players[player].secretcount = secretcount

    p.usedown = true
    p.attackdown = true
    p.playerstate = PST_LIVE
    p.health = MAXHEALTH
    p.readyweapon = WP_PISTOL
    p.pendingweapon = WP_PISTOL
    p.weaponowned[WP_FIST] = true
    p.weaponowned[WP_PISTOL] = true
    p.ammo[AM_CLIP] = 50

    for (i in 0 until NUMAMMO)
        p.maxammo[i] = maxammo[i]
}

internal fun DoomEngineCore.gCheckSpot(playernum: Int, mthing: MapThingSpawn): Boolean {
    if (players[playernum].mo == null) {
        for (i in 0 until playernum)
            if (players[i].mo!!.x == mthing.x shl FRACBITS &&
                players[i].mo!!.y == mthing.y shl FRACBITS)
                return false
        return true
    }

    val x: FixedPoint = mthing.x shl FRACBITS
    val y: FixedPoint = mthing.y shl FRACBITS

    if (!pCheckPosition(players[playernum].mo!!, x, y))
        return false

    if (bodyqueslot >= BODYQUESIZE)
        pRemoveMobj(bodyque[bodyqueslot % BODYQUESIZE]!!)
    bodyque[bodyqueslot % BODYQUESIZE] = players[playernum].mo
    bodyqueslot++

    val ss = findSubsector(x, y)
    val an = ((ANG45 * (mthing.angle / 45).toUInt()) shr ANGLETOFINESHIFT).toInt()

    val mo = pSpawnMobj(x + 20 * FineCosineTable[an], y + 20 * finesine[an],
        ss.sector!!.floorheight,
        MT_TFOG)

    if (players[consoleplayer].viewz != 1)
        sStartSound(mo, SFX_TELEPT)

    return true
}

internal fun DoomEngineCore.gDeathMatchSpawnPlayer(playernum: Int) {
    val selections = deathmatchP
    if (selections < 4)
        iError("Only $selections deathmatch spots, 4 required")

    for (j in 0 until 20) {
        val i = pRandom() % selections
        if (gCheckSpot(playernum, deathmatchstarts[i])) {
            deathmatchstarts[i].type = playernum + 1
            pSpawnPlayer(deathmatchstarts[i])
            return
        }
    }

    pSpawnPlayer(playerstarts[playernum])
}

internal fun DoomEngineCore.gDoReborn(playernum: Int) {
    if (!netgame) {
        gameaction = GA_LOADLEVEL
    } else {

        players[playernum].mo!!.player = null

        if (deathmatch != 0) {
            gDeathMatchSpawnPlayer(playernum)
            return
        }

        if (gCheckSpot(playernum, playerstarts[playernum])) {
            pSpawnPlayer(playerstarts[playernum])
            return
        }

        for (i in 0 until MAXPLAYERS) {
            if (gCheckSpot(playernum, playerstarts[i])) {
                playerstarts[i].type = playernum + 1
                pSpawnPlayer(playerstarts[i])
                playerstarts[i].type = i + 1
                return
            }
        }
        pSpawnPlayer(playerstarts[playernum])
    }
}

internal fun DoomEngineCore.gScreenShot() {
    gameaction = GA_SCREENSHOT
}

internal val DoomEngineCore.pars
    get() = stateGameSession.pars

internal val DoomEngineCore.cpars
    get() = stateGameSession.cpars

internal var DoomEngineCore.secretexit
    get() = stateGameSession.secretexit
    set(value) { stateGameSession.secretexit = value }

internal fun DoomEngineCore.gExitLevel() {
    secretexit = false
    gameaction = GA_COMPLETED
}

internal fun DoomEngineCore.gSecretExitLevel() {
    if ((gamemode == COMMERCIAL) &&
        (wCheckNumForName("map31") < 0))
        secretexit = false
    else
        secretexit = true
    gameaction = GA_COMPLETED
}

internal fun DoomEngineCore.gDoCompleted() {
    gameaction = GA_NOTHING

    for (i in 0 until MAXPLAYERS)
        if (playeringame[i])
            gPlayerFinishLevel(i)

    if (automapactive)
        amStop()

    if (gamemode != COMMERCIAL)
        when (gamemap) {
            8 -> {
                gameaction = GA_VICTORY
                return
            }
            9 -> {
                for (i in 0 until MAXPLAYERS)
                    players[i].didsecret = true
            }
        }

    if ((gamemap == 8) &&
        (gamemode != COMMERCIAL)) {
        gameaction = GA_VICTORY
        return
    }

    if ((gamemap == 9) &&
        (gamemode != COMMERCIAL)) {
        for (i in 0 until MAXPLAYERS)
            players[i].didsecret = true
    }

    wminfo.didsecret = players[consoleplayer].didsecret
    wminfo.epsd = gameepisode - 1
    wminfo.last = gamemap - 1

    if (gamemode == COMMERCIAL) {
        if (secretexit)
            when (gamemap) {
                15 -> wminfo.next = 30
                31 -> wminfo.next = 31
            }
        else
            when (gamemap) {
                31, 32 -> wminfo.next = 15
                else -> wminfo.next = gamemap
            }
    } else {
        if (secretexit)
            wminfo.next = 8
        else if (gamemap == 9) {
            when (gameepisode) {
                1 -> wminfo.next = 3
                2 -> wminfo.next = 5
                3 -> wminfo.next = 6
                4 -> wminfo.next = 2
            }
        } else
            wminfo.next = gamemap
    }

    wminfo.maxkills = totalkills
    wminfo.maxitems = totalitems
    wminfo.maxsecret = totalsecret
    wminfo.maxfrags = 0
    if (gamemode == COMMERCIAL)
        wminfo.partime = 35 * cpars[gamemap - 1]
    else
        wminfo.partime = 35 * pars[gameepisode][gamemap]
    wminfo.pnum = consoleplayer

    for (i in 0 until MAXPLAYERS) {
        wminfo.plyr[i].inGame = playeringame[i]
        wminfo.plyr[i].skills = players[i].killcount
        wminfo.plyr[i].sitems = players[i].itemcount
        wminfo.plyr[i].ssecret = players[i].secretcount
        wminfo.plyr[i].stime = leveltime
        players[i].frags.copyInto(wminfo.plyr[i].frags)
    }

    gamestate = GS_INTERMISSION
    viewactive = false
    automapactive = false


    wiStart(wminfo)
}

internal fun DoomEngineCore.gWorldDone() {
    gameaction = GA_WORLDDONE

    if (secretexit)
        players[consoleplayer].didsecret = true

    if (gamemode == COMMERCIAL) {
        when (gamemap) {
            15, 31 ->
                if (secretexit)
                    fStartFinale()
            6, 11, 20, 30 ->
                fStartFinale()
        }
    }
}

internal fun DoomEngineCore.gDoWorldDone() {
    gamestate = GS_LEVEL
    gamemap = wminfo.next + 1
    gDoLoadLevel()
    gameaction = GA_NOTHING
    viewactive = true
}


internal var DoomEngineCore.savename
    get() = stateGameSession.savename
    set(value) { stateGameSession.savename = value }

internal fun DoomEngineCore.gLoadGame(name: String) {
    savename = name
    gameaction = GA_LOADGAME
}

private const val VERSIONSIZE = 16

internal fun DoomEngineCore.gDoLoadGame() {
    gameaction = GA_NOTHING

    savebuffer = mReadFile(savename)
        ?: iError("Couldn't read file $savename")
    saveP = SAVESTRINGSIZE

    val vcheck = "version $VERSION"
    if (savebuffer.str(saveP, VERSIONSIZE) != vcheck)
        return
    saveP += VERSIONSIZE

    gameskill = savebuffer.u8(saveP); saveP++
    gameepisode = savebuffer.u8(saveP); saveP++
    gamemap = savebuffer.u8(saveP); saveP++
    for (i in 0 until MAXPLAYERS) {
        playeringame[i] = savebuffer.u8(saveP) != 0
        saveP++
    }

    gInitNew(gameskill, gameepisode, gamemap)

    val a = savebuffer.u8(saveP); saveP++
    val b = savebuffer.u8(saveP); saveP++
    val c = savebuffer.u8(saveP); saveP++
    leveltime = (a shl 16) + (b shl 8) + c

    pUnArchivePlayers()
    pUnArchiveWorld()
    pUnArchiveThinkers()
    pUnArchiveSpecials()

    if (savebuffer.u8(saveP) != 0x1d)
        iError("Bad savegame")


    if (setsizeneeded)
        rExecuteSetViewSize()

    rFillBackScreen()
}

internal fun DoomEngineCore.gSaveGame(slot: Int, description: String) {
    savegameslot = slot
    savedescription = description
    sendsave = true
}

internal fun DoomEngineCore.gDoSaveGame() {
    val name: String
    if (mCheckParm("-cdrom") != 0)
        name = "c:\\doomdata\\$SAVEGAMENAME${savegameslot}.dsg"
    else
        name = "$SAVEGAMENAME${savegameslot}.dsg"
    val description = savedescription

    savebuffer = ByteArray(SAVEGAMESIZE)
    saveP = 0

    for (i in 0 until SAVESTRINGSIZE)
        savebuffer[saveP + i] =
            if (i < description.length) description[i].code.toByte() else 0
    saveP += SAVESTRINGSIZE
    val name2 = "version $VERSION"
    for (i in 0 until VERSIONSIZE)
        savebuffer[saveP + i] =
            if (i < name2.length) name2[i].code.toByte() else 0
    saveP += VERSIONSIZE

    savebuffer[saveP] = gameskill.toByte(); saveP++
    savebuffer[saveP] = gameepisode.toByte(); saveP++
    savebuffer[saveP] = gamemap.toByte(); saveP++
    for (i in 0 until MAXPLAYERS) {
        savebuffer[saveP] = (if (playeringame[i]) 1 else 0).toByte()
        saveP++
    }
    savebuffer[saveP] = (leveltime shr 16).toByte(); saveP++
    savebuffer[saveP] = (leveltime shr 8).toByte(); saveP++
    savebuffer[saveP] = leveltime.toByte(); saveP++

    pArchivePlayers()
    pArchiveWorld()
    pArchiveThinkers()
    pArchiveSpecials()

    savebuffer[saveP] = 0x1d; saveP++

    val length = saveP
    if (length > SAVEGAMESIZE)
        iError("Savegame buffer overrun")
    mWriteFile(name, savebuffer.copyOf(length))
    gameaction = GA_NOTHING
    savedescription = ""

    players[consoleplayer].message = GGSAVED

    rFillBackScreen()
}

internal var DoomEngineCore.dSkill
    get() = stateGameSession.dSkill
    set(value) { stateGameSession.dSkill = value }
internal var DoomEngineCore.dEpisode
    get() = stateGameSession.dEpisode
    set(value) { stateGameSession.dEpisode = value }
internal var DoomEngineCore.dMap
    get() = stateGameSession.dMap
    set(value) { stateGameSession.dMap = value }

internal fun DoomEngineCore.gDeferedInitNew(skill: Int, episode: Int, map: Int) {
    dSkill = skill
    dEpisode = episode
    dMap = map
    gameaction = GA_NEWGAME
}

internal fun DoomEngineCore.gDoNewGame() {
    demoplayback = false
    netdemo = false
    netgame = false
    deathmatch = 0
    playeringame[1] = false
    playeringame[2] = false
    playeringame[3] = false
    respawnparm = false
    fastparm = false
    nomonsters = false
    consoleplayer = 0
    gInitNew(dSkill, dEpisode, dMap)
    gameaction = GA_NOTHING
}

internal fun DoomEngineCore.gInitNew(requestedSkill: Int, requestedEpisode: Int, requestedMap: Int) {
    var skill = requestedSkill
    var episode = requestedEpisode
    var map = requestedMap

    if (paused) {
        paused = false
        sResumeSound()
    }

    if (skill > SK_NIGHTMARE)
        skill = SK_NIGHTMARE

    if (episode < 1)
        episode = 1

    if (gamemode == RETAIL) {
        if (episode > 4)
            episode = 4
    } else if (gamemode == SHAREWARE) {
        if (episode > 1)
            episode = 1
    } else {
        if (episode > 3)
            episode = 3
    }

    if (map < 1)
        map = 1

    if ((map > 9) &&
        (gamemode != COMMERCIAL))
        map = 9

    mClearRandom()

    if (skill == SK_NIGHTMARE || respawnparm)
        respawnmonsters = true
    else
        respawnmonsters = false

    if (fastparm || (skill == SK_NIGHTMARE && gameskill != SK_NIGHTMARE)) {
        for (i in S_SARG_RUN1..S_SARG_PAIN2)
            states[i].tics = states[i].tics shr 1
        mobjinfo[MT_BRUISERSHOT].speed = 20 * FRACUNIT
        mobjinfo[MT_HEADSHOT].speed = 20 * FRACUNIT
        mobjinfo[MT_TROOPSHOT].speed = 20 * FRACUNIT
    } else if (skill != SK_NIGHTMARE && gameskill == SK_NIGHTMARE) {
        for (i in S_SARG_RUN1..S_SARG_PAIN2)
            states[i].tics = states[i].tics shl 1
        mobjinfo[MT_BRUISERSHOT].speed = 15 * FRACUNIT
        mobjinfo[MT_HEADSHOT].speed = 10 * FRACUNIT
        mobjinfo[MT_TROOPSHOT].speed = 10 * FRACUNIT
    }

    for (i in 0 until MAXPLAYERS)
        players[i].playerstate = PST_REBORN

    usergame = true
    paused = false
    demoplayback = false
    automapactive = false
    viewactive = true
    gameepisode = episode
    gamemap = map
    gameskill = skill

    viewactive = true

    if (gamemode == COMMERCIAL) {
        skytexture = rTextureNumForName("SKY3")
        if (gamemap < 12)
            skytexture = rTextureNumForName("SKY1")
        else
            if (gamemap < 21)
                skytexture = rTextureNumForName("SKY2")
    } else
        when (episode) {
            1 ->
                skytexture = rTextureNumForName("SKY1")
            2 ->
                skytexture = rTextureNumForName("SKY2")
            3 ->
                skytexture = rTextureNumForName("SKY3")
            4 ->
                skytexture = rTextureNumForName("SKY4")
        }

    gDoLoadLevel()
}

private const val DEMOMARKER = 0x80

internal fun DoomEngineCore.gReadDemoTiccmd(cmd: TicCommand) {
    val demobuf = demobuffer!!
    if (demobuf.u8(demoP) == DEMOMARKER) {
        gCheckDemoStatus()
        return
    }
    cmd.forwardmove = demobuf.i8(demoP); demoP++
    cmd.sidemove = demobuf.i8(demoP); demoP++
    cmd.angleturn = (demobuf.u8(demoP) shl 8).toShort().toInt(); demoP++
    cmd.buttons = demobuf.u8(demoP); demoP++
}

internal fun DoomEngineCore.gWriteDemoTiccmd(cmd: TicCommand) {
    if (gamekeydown['q'.code])
        gCheckDemoStatus()
    val demobuf = demobuffer!!
    demobuf[demoP] = cmd.forwardmove.toByte(); demoP++
    demobuf[demoP] = cmd.sidemove.toByte(); demoP++
    demobuf[demoP] = ((cmd.angleturn + 128) shr 8).toByte(); demoP++
    demobuf[demoP] = cmd.buttons.toByte(); demoP++
    demoP -= 4
    if (demoP > demoend - 16) {
        gCheckDemoStatus()
        return
    }

    gReadDemoTiccmd(cmd)
}

internal fun DoomEngineCore.gRecordDemo(name: String) {
    usergame = false
    demoname = "$name.lmp"
    var maxsize = 0x20000
    val i = mCheckParm("-maxdemo")
    if (i != 0 && i < myargc - 1)
        maxsize = (myargv[i + 1].toIntOrNull() ?: 0) * 1024
    demobuffer = ByteArray(maxsize)
    demoend = maxsize

    demorecording = true
}

internal fun DoomEngineCore.gBeginRecording() {
    demoP = 0
    val demobuf = demobuffer!!

    demobuf[demoP] = VERSION.toByte(); demoP++
    demobuf[demoP] = gameskill.toByte(); demoP++
    demobuf[demoP] = gameepisode.toByte(); demoP++
    demobuf[demoP] = gamemap.toByte(); demoP++
    demobuf[demoP] = deathmatch.toByte(); demoP++
    demobuf[demoP] = (if (respawnparm) 1 else 0).toByte(); demoP++
    demobuf[demoP] = (if (fastparm) 1 else 0).toByte(); demoP++
    demobuf[demoP] = (if (nomonsters) 1 else 0).toByte(); demoP++
    demobuf[demoP] = consoleplayer.toByte(); demoP++

    for (i in 0 until MAXPLAYERS) {
        demobuf[demoP] = (if (playeringame[i]) 1 else 0).toByte()
        demoP++
    }
}


internal var DoomEngineCore.defdemoname
    get() = stateGameSession.defdemoname
    set(value) { stateGameSession.defdemoname = value }

internal fun DoomEngineCore.gDeferedPlayDemo(name: String) {
    defdemoname = name
    gameaction = GA_PLAYDEMO
}

internal fun DoomEngineCore.gDoPlayDemo() {
    gameaction = GA_NOTHING
    demobuffer = wCacheLumpName(defdemoname)
    demoP = 0
    val demobuf = demobuffer!!
    val version = demobuf.u8(demoP); demoP++
    if (version != VERSION) {
        println("Demo is from a different game version!")
        gameaction = GA_NOTHING
        return
    }

    val skill = demobuf.u8(demoP); demoP++
    val episode = demobuf.u8(demoP); demoP++
    val map = demobuf.u8(demoP); demoP++
    deathmatch = demobuf.u8(demoP); demoP++
    respawnparm = demobuf.u8(demoP) != 0; demoP++
    fastparm = demobuf.u8(demoP) != 0; demoP++
    nomonsters = demobuf.u8(demoP) != 0; demoP++
    consoleplayer = demobuf.u8(demoP); demoP++

    for (i in 0 until MAXPLAYERS) {
        playeringame[i] = demobuf.u8(demoP) != 0
        demoP++
    }
    if (playeringame[1]) {
        netgame = true
        netdemo = true
    }

    precache = false
    gInitNew(skill, episode, map)
    precache = true

    usergame = false
    demoplayback = true
}

internal fun DoomEngineCore.gTimeDemo(name: String) {
    nodrawers = mCheckParm("-nodraw") != 0
    noblit = mCheckParm("-noblit") != 0
    timingdemo = true
    singletics = true

    defdemoname = name
    gameaction = GA_PLAYDEMO
}


internal fun DoomEngineCore.gCheckDemoStatus(): Boolean {
    if (timingdemo) {
        val endtime = iGetTime()
        iError("timed $gametic gametics in ${endtime - starttime} realtics")
    }

    if (demoplayback) {
        if (singledemo)
            iQuit()

        demoplayback = false
        netdemo = false
        netgame = false
        deathmatch = 0
        playeringame[1] = false
        playeringame[2] = false
        playeringame[3] = false
        respawnparm = false
        fastparm = false
        nomonsters = false
        consoleplayer = 0
        dAdvanceDemo()
        return true
    }

    if (demorecording) {
        val demobuf = demobuffer!!
        demobuf[demoP] = DEMOMARKER.toByte(); demoP++
        mWriteFile(demoname, demobuf.copyOf(demoP))
        demorecording = false
        iError("Demo $demoname recorded")
    }

    return false
}
