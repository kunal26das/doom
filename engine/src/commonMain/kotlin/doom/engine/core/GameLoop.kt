
package doom.engine.core

import doom.engine.audio.sInit
import doom.engine.audio.sStartMusic
import doom.engine.audio.sUpdateSounds
import doom.engine.audio.MUS_DM2TTL
import doom.engine.audio.MUS_INTRO
import doom.engine.audio.sndMusicVolume
import doom.engine.audio.sndSfxVolume
import doom.engine.automap.amDrawer
import doom.engine.automap.automapactive
import doom.engine.configuration.mCheckParm
import doom.engine.configuration.mLoadDefaults
import doom.engine.configuration.mValidateArguments
import doom.engine.configuration.myargc
import doom.engine.configuration.myargv
import doom.engine.finale.fDrawer
import doom.engine.gameplay.GS_DEMOSCREEN
import doom.engine.gameplay.GS_FINALE
import doom.engine.gameplay.GS_INTERMISSION
import doom.engine.gameplay.GS_LEVEL
import doom.engine.gameplay.gBeginRecording
import doom.engine.gameplay.gDeferedPlayDemo
import doom.engine.gameplay.gInitNew
import doom.engine.gameplay.gLoadGame
import doom.engine.gameplay.gRecordDemo
import doom.engine.gameplay.gResponder
import doom.engine.gameplay.gTimeDemo
import doom.engine.gameplay.VERSION
import doom.engine.gameplay.actors.infoResolveActions
import doom.engine.gameplay.actors.pRegisterEnemyActions
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.demorecording
import doom.engine.gameplay.displayplayer
import doom.engine.gameplay.DOOM
import doom.engine.gameplay.DOOM2
import doom.engine.gameplay.forwardmove
import doom.engine.gameplay.GA_LOADGAME
import doom.engine.gameplay.GA_NOTHING
import doom.engine.gameplay.gameaction
import doom.engine.gameplay.gamemission
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gamestate
import doom.engine.gameplay.gametic
import doom.engine.gameplay.INDETERMINED
import doom.engine.gameplay.netgame
import doom.engine.gameplay.nodrawers
import doom.engine.gameplay.paused
import doom.engine.gameplay.player.PST_LIVE
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.REGISTERED
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.sidemove
import doom.engine.gameplay.singledemo
import doom.engine.gameplay.SK_MEDIUM
import doom.engine.gameplay.usergame
import doom.engine.gameplay.viewactive
import doom.engine.gameplay.weapons.pRegisterPsprActions
import doom.engine.hud.huDrawer
import doom.engine.hud.huErase
import doom.engine.hud.huInit
import doom.engine.input.EngineEvent
import doom.engine.intermission.wiDrawer
import doom.engine.menu.mDrawer
import doom.engine.menu.mInit
import doom.engine.menu.mResponder
import doom.engine.menu.inhelpscreens
import doom.engine.menu.menuactive
import doom.engine.rendering.iFinishUpdate
import doom.engine.rendering.iInitGraphics
import doom.engine.rendering.iSetPalette
import doom.engine.rendering.iStartFrame
import doom.engine.rendering.iUpdateNoBlit
import doom.engine.rendering.rDrawViewBorder
import doom.engine.rendering.rExecuteSetViewSize
import doom.engine.rendering.rFillBackScreen
import doom.engine.rendering.rInit
import doom.engine.rendering.rRenderPlayerView
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.vDrawPatchDirect
import doom.engine.rendering.vInit
import doom.engine.rendering.scaledviewwidth
import doom.engine.rendering.setsizeneeded
import doom.engine.rendering.viewheight
import doom.engine.rendering.viewwindowx
import doom.engine.rendering.viewwindowy
import doom.engine.rendering.wipeEndScreen
import doom.engine.rendering.WIPE_MELT
import doom.engine.rendering.wipeScreenWipe
import doom.engine.rendering.wipeStartScreen
import doom.engine.resources.D_DEVSTR
import doom.engine.resources.SAVEGAMENAME
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.wCheckNumForName
import doom.engine.resources.wInitMultipleFiles
import doom.engine.simulation.ticdup
import doom.engine.statusbar.stDrawer
import doom.engine.statusbar.stInit
import doom.engine.world.pInit


internal var DoomEngineCore.devparm
    get() = stateGameLoop.devparm
    set(value) { stateGameLoop.devparm = value }
internal var DoomEngineCore.nomonsters
    get() = stateGameLoop.nomonsters
    set(value) { stateGameLoop.nomonsters = value }
internal var DoomEngineCore.respawnparm
    get() = stateGameLoop.respawnparm
    set(value) { stateGameLoop.respawnparm = value }
internal var DoomEngineCore.fastparm
    get() = stateGameLoop.fastparm
    set(value) { stateGameLoop.fastparm = value }

internal var DoomEngineCore.basedefault
    get() = stateGameLoop.basedefault
    set(value) { stateGameLoop.basedefault = value }

internal var DoomEngineCore.singletics
    get() = stateGameLoop.singletics
    set(value) { stateGameLoop.singletics = value }

internal var DoomEngineCore.modifiedgame
    get() = stateGameLoop.modifiedgame
    set(value) { stateGameLoop.modifiedgame = value }

internal var DoomEngineCore.startskill
    get() = stateGameLoop.startskill
    set(value) { stateGameLoop.startskill = value }
internal var DoomEngineCore.startepisode
    get() = stateGameLoop.startepisode
    set(value) { stateGameLoop.startepisode = value }
internal var DoomEngineCore.startmap
    get() = stateGameLoop.startmap
    set(value) { stateGameLoop.startmap = value }
internal var DoomEngineCore.autostart
    get() = stateGameLoop.autostart
    set(value) { stateGameLoop.autostart = value }

internal var DoomEngineCore.advancedemo
    get() = stateGameLoop.advancedemo
    set(value) { stateGameLoop.advancedemo = value }

internal const val MAXEVENTS = 64

internal fun DoomEngineCore.dPostEvent(ev: EngineEvent) { inputQueue.stage(ev) }

internal fun DoomEngineCore.dProcessEvents() {
    if (gamemode == COMMERCIAL && wCheckNumForName("map01") < 0) {
        inputQueue.clearPending()
        return
    }
    inputQueue.dispatch { event ->
        if (!mResponder(event)) gResponder(event)
    }
}


internal var DoomEngineCore.wipegamestate
    get() = stateGameLoop.wipegamestate
    set(value) { stateGameLoop.wipegamestate = value }

private var DoomEngineCore.viewactivestate
    get() = stateGameLoop.viewactivestate
    set(value) { stateGameLoop.viewactivestate = value }
private var DoomEngineCore.menuactivestate
    get() = stateGameLoop.menuactivestate
    set(value) { stateGameLoop.menuactivestate = value }
private var DoomEngineCore.inhelpscreensstate
    get() = stateGameLoop.inhelpscreensstate
    set(value) { stateGameLoop.inhelpscreensstate = value }
private var DoomEngineCore.fullscreen
    get() = stateGameLoop.fullscreen
    set(value) { stateGameLoop.fullscreen = value }
private var DoomEngineCore.oldgamestate
    get() = stateGameLoop.oldgamestate
    set(value) { stateGameLoop.oldgamestate = value }
private var DoomEngineCore.borderdrawcount
    get() = stateGameLoop.borderdrawcount
    set(value) { stateGameLoop.borderdrawcount = value }

internal var DoomEngineCore.wipeActive
    get() = stateGameLoop.wipeActive
    set(value) { stateGameLoop.wipeActive = value }
private var DoomEngineCore.wipestart
    get() = stateGameLoop.wipestart
    set(value) { stateGameLoop.wipestart = value }

internal fun DoomEngineCore.dDisplay() {
    val y: Int
    val wipe: Boolean
    var redrawsbar: Boolean

    if (nodrawers)
        return

    redrawsbar = false

    if (setsizeneeded) {
        rExecuteSetViewSize()
        oldgamestate = -1
        borderdrawcount = 3
    }

    if (gamestate != wipegamestate) {
        wipe = true
        wipeStartScreen()
    } else
        wipe = false

    if (gamestate == GS_LEVEL && gametic != 0)
        huErase()

    when (gamestate) {
        GS_LEVEL -> {
            if (gametic != 0) {
                if (automapactive)
                    amDrawer()
                if (wipe || (viewheight != 200 && fullscreen))
                    redrawsbar = true
                if (inhelpscreensstate && !inhelpscreens)
                    redrawsbar = true
                stDrawer(viewheight == 200, redrawsbar)
                fullscreen = viewheight == 200
            }
        }

        GS_INTERMISSION ->
            wiDrawer()

        GS_FINALE ->
            fDrawer()

        GS_DEMOSCREEN ->
            dPageDrawer()
    }

    iUpdateNoBlit()

    if (gamestate == GS_LEVEL && !automapactive && gametic != 0)
        rRenderPlayerView(players[displayplayer])

    if (gamestate == GS_LEVEL && gametic != 0)
        huDrawer()

    if (gamestate != oldgamestate && gamestate != GS_LEVEL)
        iSetPalette(wCacheLumpName("PLAYPAL"))

    if (gamestate == GS_LEVEL && oldgamestate != GS_LEVEL) {
        viewactivestate = false
        rFillBackScreen()
    }

    if (gamestate == GS_LEVEL && !automapactive && scaledviewwidth != 320) {
        if (menuactive || menuactivestate || !viewactivestate)
            borderdrawcount = 3
        if (borderdrawcount != 0) {
            rDrawViewBorder()
            borderdrawcount--
        }
    }

    menuactivestate = menuactive
    viewactivestate = viewactive
    inhelpscreensstate = inhelpscreens
    oldgamestate = gamestate
    wipegamestate = gamestate

    if (paused) {
        if (automapactive)
            y = 4
        else
            y = viewwindowy + 4
        vDrawPatchDirect(viewwindowx + (scaledviewwidth - 68) / 2,
            y, 0, wCacheLumpName("M_PAUSE"))
    }

    mDrawer()

    if (!wipe) {
        iFinishUpdate()
        return
    }

    wipeEndScreen()
    wipeActive = true
    wipestart = iGetTime() - 1
}

internal fun DoomEngineCore.dWipeStep() {
    val nowtime = iGetTime()
    val tics = nowtime - wipestart
    if (tics <= 0) {
        if (tics < 0) wipestart = nowtime
        return
    }
    wipestart = nowtime
    val done = wipeScreenWipe(WIPE_MELT, tics)
    iUpdateNoBlit()
    mDrawer()
    iFinishUpdate()
    if (done)
        wipeActive = false
}

internal fun DoomEngineCore.dDoomStep() {
    if (wipeActive) {
        dWipeStep()
        return
    }

    iStartFrame()

    if (singletics) {
        tickScheduler.advanceSingleTic()
    } else {
        tickScheduler.advanceDueTics()
    }

    sUpdateSounds(players[consoleplayer].mo)

    dDisplay()
}

internal var DoomEngineCore.demosequence
    get() = stateGameLoop.demosequence
    set(value) { stateGameLoop.demosequence = value }
internal var DoomEngineCore.pagetic
    get() = stateGameLoop.pagetic
    set(value) { stateGameLoop.pagetic = value }
internal var DoomEngineCore.pagename
    get() = stateGameLoop.pagename
    set(value) { stateGameLoop.pagename = value }

internal fun DoomEngineCore.dPageTicker() {
    pagetic--
    if (pagetic < 0)
        dAdvanceDemo()
}

internal fun DoomEngineCore.dPageDrawer() {
    vDrawPatch(0, 0, 0, wCacheLumpName(pagename))
}

internal fun DoomEngineCore.dAdvanceDemo() {
    advancedemo = true
}

internal fun DoomEngineCore.dDoAdvanceDemo() {
    players[consoleplayer].playerstate = PST_LIVE
    advancedemo = false
    usergame = false
    paused = false
    gameaction = GA_NOTHING

    if (gamemode == RETAIL)
        demosequence = (demosequence + 1) % 7
    else
        demosequence = (demosequence + 1) % 6

    when (demosequence) {
        0 -> {
            if (gamemode == COMMERCIAL)
                pagetic = 35 * 11
            else
                pagetic = 170
            gamestate = GS_DEMOSCREEN
            pagename = "TITLEPIC"
            if (gamemode == COMMERCIAL)
                sStartMusic(MUS_DM2TTL)
            else
                sStartMusic(MUS_INTRO)
        }
        1 -> gDeferedPlayDemo("demo1")
        2 -> {
            pagetic = 200
            gamestate = GS_DEMOSCREEN
            pagename = "CREDIT"
        }
        3 -> gDeferedPlayDemo("demo2")
        4 -> {
            gamestate = GS_DEMOSCREEN
            if (gamemode == COMMERCIAL) {
                pagetic = 35 * 11
                pagename = "TITLEPIC"
                sStartMusic(MUS_DM2TTL)
            } else {
                pagetic = 200

                if (gamemode == RETAIL)
                    pagename = "CREDIT"
                else
                    pagename = "HELP2"
            }
        }
        5 -> gDeferedPlayDemo("demo3")
        6 -> gDeferedPlayDemo("demo4")
    }
}

internal fun DoomEngineCore.dStartTitle() {
    gameaction = GA_NOTHING
    demosequence = -1
    dAdvanceDemo()
}

internal fun DoomEngineCore.dIdentifyVersion() {
    if (wCheckNumForName("MAP01") != -1) {
        gamemission = DOOM2
        gamemode = COMMERCIAL
    } else if (wCheckNumForName("E4M1") != -1) {
        gamemission = DOOM
        gamemode = RETAIL
    } else if (wCheckNumForName("E2M1") != -1 || wCheckNumForName("E3M1") != -1) {
        gamemission = DOOM
        gamemode = REGISTERED
    } else if (wCheckNumForName("E1M1") != -1) {
        gamemission = DOOM
        gamemode = SHAREWARE
    } else {
        println("Game mode indeterminate.")
        gamemode = INDETERMINED
    }
}

internal fun DoomEngineCore.dDoomMain(wads: List<ByteArray>, args: List<String>) {
    var p: Int

    myargv = listOf("doom") + args

    modifiedgame = false

    nomonsters = mCheckParm("-nomonsters") != 0
    respawnparm = mCheckParm("-respawn") != 0
    fastparm = mCheckParm("-fast") != 0
    devparm = mCheckParm("-devparm") != 0
    if (mCheckParm("-altdeath") != 0)
        deathmatch = 2
    else if (mCheckParm("-deathmatch") != 0)
        deathmatch = 1

    println("W_Init: Init WADfiles.")
    wInitMultipleFiles(wads)

    dIdentifyVersion()
    mValidateArguments(gamemode == COMMERCIAL)

    val title: String
    when (gamemode) {
        RETAIL ->
            title = "                         " +
                "The Ultimate DOOM Startup v${VERSION / 100}.${VERSION % 100}" +
                "                           "
        SHAREWARE ->
            title = "                            " +
                "DOOM Shareware Startup v${VERSION / 100}.${VERSION % 100}" +
                "                           "
        REGISTERED ->
            title = "                            " +
                "DOOM Registered Startup v${VERSION / 100}.${VERSION % 100}" +
                "                           "
        COMMERCIAL ->
            title = "                         " +
                "DOOM 2: Hell on Earth v${VERSION / 100}.${VERSION % 100}" +
                "                           "
        else ->
            title = "                     " +
                "Public DOOM - v${VERSION / 100}.${VERSION % 100}" +
                "                           "
    }

    println(title)

    if (devparm)
        print(D_DEVSTR)

    p = mCheckParm("-turbo")
    if (p != 0) {
        var scale = 200

        if (p < myargc - 1 && !myargv[p + 1].startsWith('-'))
            scale = myargv[p + 1].toInt()
        if (scale < 10)
            scale = 10
        if (scale > 400)
            scale = 400
        println("turbo scale: $scale%")
        forwardmove[0] = forwardmove[0] * scale / 100
        forwardmove[1] = forwardmove[1] * scale / 100
        sidemove[0] = sidemove[0] * scale / 100
        sidemove[1] = sidemove[1] * scale / 100
    }

    if (wads.size > 1)
        modifiedgame = true

    p = mCheckParm("-playdemo")

    if (p == 0)
        p = mCheckParm("-timedemo")

    if (p != 0 && p < myargc - 1)
        println("Playing demo ${myargv[p + 1]}.lmp.")

    startskill = SK_MEDIUM
    startepisode = 1
    startmap = 1
    autostart = false

    p = mCheckParm("-skill")
    if (p != 0 && p < myargc - 1) {
        startskill = myargv[p + 1].toInt() - 1
        autostart = true
    }

    p = mCheckParm("-episode")
    if (p != 0 && p < myargc - 1) {
        startepisode = myargv[p + 1].toInt()
        startmap = 1
        autostart = true
    }

    p = mCheckParm("-timer")
    if (p != 0 && p < myargc - 1 && deathmatch != 0) {
        val time = myargv[p + 1].toIntOrNull() ?: 0
        print("Levels will end after $time minute")
        if (time > 1)
            print("s")
        println(".")
    }

    p = mCheckParm("-avg")
    if (p != 0 && p < myargc - 1 && deathmatch != 0)
        println("Austin Virtual Gaming: Levels will end after 20 minutes")

    p = mCheckParm("-warp")
    if (p != 0 && p < myargc - 1) {
        if (gamemode == COMMERCIAL)
            startmap = myargv[p + 1].toIntOrNull() ?: 0
        else {
            startepisode = myargv[p + 1].toInt()
            startmap = myargv[p + 2].toInt()
        }
        autostart = true
    }

    if (modifiedgame) {
        val name = arrayOf(
            "e2m1", "e2m2", "e2m3", "e2m4", "e2m5", "e2m6", "e2m7", "e2m8", "e2m9",
            "e3m1", "e3m3", "e3m3", "e3m4", "e3m5", "e3m6", "e3m7", "e3m8", "e3m9",
            "dphoof", "bfgga0", "heada1", "cybra1", "spida1d1"
        )

        if (gamemode == SHAREWARE)
            iError("\nYou cannot -file with the shareware version. Register!")

        if (gamemode == REGISTERED)
            for (i in 0 until 23)
                if (wCheckNumForName(name[i]) < 0)
                    iError("\nThis is not the registered version.")
    }

    if (modifiedgame) {
        println(
            "===========================================================================\n" +
            "ATTENTION:  This version of DOOM has been modified.  If you would like to\n" +
            "get a copy of the original game, call 1-800-IDGAMES or see the readme file.\n" +
            "        You will not receive technical support for modified games.\n" +
            "                      press enter to continue\n" +
            "==========================================================================="
        )
    }

    when (gamemode) {
        SHAREWARE, INDETERMINED ->
            println(
                "===========================================================================\n" +
                "                                Shareware!\n" +
                "==========================================================================="
            )
        REGISTERED, RETAIL, COMMERCIAL ->
            println(
                "===========================================================================\n" +
                "                 Commercial product - do not distribute!\n" +
                "         Please report software piracy to the SPA: 1-800-388-PIR8\n" +
                "==========================================================================="
            )
        else -> {
        }
    }

    println("V_Init: allocate screens.")
    vInit()

    println("M_LoadDefaults: Load system defaults.")
    mLoadDefaults()

    println("M_Init: Init miscellaneous info.")
    mInit()

    print("R_Init: Init DOOM refresh daemon - ")
    rInit()

    pRegisterEnemyActions()
    pRegisterPsprActions()
    infoResolveActions()

    println("\nP_Init: Init Playloop state.")
    pInit()

    println("I_Init: Setting up machine state.")
    iInitGraphics()

    println("D_CheckNetGame: Checking network game status.")
    netgame = false
    consoleplayer = 0
    displayplayer = 0
    playeringame[0] = true
    ticdup = 1

    println("startskill $startskill  deathmatch: $deathmatch  startmap: $startmap  startepisode: $startepisode")

    println("S_Init: Setting up sound.")
    sInit(sndSfxVolume  , sndMusicVolume  )

    println("HU_Init: Setting up heads up display.")
    huInit()

    println("ST_Init: Init status bar.")
    stInit()

    p = mCheckParm("-record")

    if (p != 0 && p < myargc - 1) {
        gRecordDemo(myargv[p + 1])
        autostart = true
    }

    p = mCheckParm("-playdemo")
    if (p != 0 && p < myargc - 1) {
        singledemo = true
        gDeferedPlayDemo(myargv[p + 1])
        if (demorecording)
            gBeginRecording()
        return
    }

    p = mCheckParm("-timedemo")
    if (p != 0 && p < myargc - 1) {
        gTimeDemo(myargv[p + 1])
        if (demorecording)
            gBeginRecording()
        return
    }

    p = mCheckParm("-loadgame")
    if (p != 0 && p < myargc - 1) {
        gLoadGame("$SAVEGAMENAME${myargv[p + 1].toInt()}.dsg")
    }

    if (gameaction != GA_LOADGAME) {
        if (autostart || netgame)
            gInitNew(startskill, startepisode, startmap)
        else
            dStartTitle()
    }

    if (demorecording)
        gBeginRecording()

}
