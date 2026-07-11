// Port of linuxdoom-1.10 g_game.c -- game state, tic commands, demos, save/load.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// C: doomdef.h defines VERSION 110 (linuxdoom). This port deliberately uses 109
// (DOS v1.9) so the IWAD's built-in DEMO1/DEMO2/DEMO3 headers (recorded with
// version 109) pass the version check and play back -- deviation for fidelity.
const val VERSION = 109

private const val SAVEGAMESIZE = 0x2c000
private const val SAVESTRINGSIZE = 24


var gameaction = ga_nothing
var gamestate = GS_LEVEL
var gameskill = sk_baby
var respawnmonsters = false
var gameepisode = 0
var gamemap = 0

var paused = false
var sendpause = false           // send a pause event next tic
var sendsave = false            // send a save event next tic
var usergame = false            // ok to save / end game

var timingdemo = false          // if true, exit with report on completion
var nodrawers = false           // for comparative timing purposes
var noblit = false              // for comparative timing purposes
var starttime = 0               // for comparative timing purposes

var viewactive = false

var deathmatch = 0              // only if started as net death (-altdeath sets 2)
var netgame = false             // only true if packets are broadcast
val playeringame = BooleanArray(MAXPLAYERS)
val players = Array(MAXPLAYERS) { player_t() }

var consoleplayer = 0           // player taking events and displaying
var displayplayer = 0           // view being displayed
var gametic = 0
var levelstarttic = 0           // gametic at level start
var totalkills = 0              // for intermission
var totalitems = 0
var totalsecret = 0

var demoname = ""
var demorecording = false
var demoplayback = false
var netdemo = false
var demobuffer: ByteArray? = null
var demo_p = 0                  // C: byte* demo_p -> index into demobuffer
var demoend = 0                 // C: byte* demoend -> index into demobuffer
var singledemo = false          // quit after playing a demo from cmdline

var precache = true             // if true, load all graphics at start

val wminfo = wbstartstruct_t()  // parms for world map / intermission

val consistancy = Array(MAXPLAYERS) { ShortArray(BACKUPTICS) }

var savebuffer = ByteArray(0)   // C: byte* savebuffer (save_p cursor lives in p_saveg.kt)


//
// controls (have defaults)
// (C zero-inits these; the m_misc.c defaults table values are used as the
//  Kotlin initializers so the bindings work even before M_LoadDefaults.)
//
var key_right = KEY_RIGHTARROW
var key_left = KEY_LEFTARROW

var key_up = KEY_UPARROW
var key_down = KEY_DOWNARROW
var key_strafeleft = ','.code
var key_straferight = '.'.code
var key_fire = KEY_RCTRL
var key_use = ' '.code
var key_strafe = KEY_RALT
var key_speed = KEY_RSHIFT

var mousebfire = 0
var mousebstrafe = 1
var mousebforward = 2

var joybfire = 0
var joybstrafe = 1
var joybuse = 3
var joybspeed = 2


// #define MAXPLMOVE (forwardmove[1])
private val MAXPLMOVE: Int get() = forwardmove[1]

const val TURBOTHRESHOLD = 0x32

val forwardmove = intArrayOf(0x19, 0x32)       // fixed_t
val sidemove = intArrayOf(0x18, 0x28)          // fixed_t
val angleturn = intArrayOf(640, 1280, 320)     // + slow turn

private const val SLOWTURNTICS = 6

const val NUMKEYS = 256

val gamekeydown = BooleanArray(NUMKEYS)
var turnheld = 0                // for accelerative turning

val mousearray = BooleanArray(4)

/** C: boolean* mousebuttons = &mousearray[1]; -- allow [-1] */
object mousebuttons {
    operator fun get(i: Int): Boolean = mousearray[i + 1]
    operator fun set(i: Int, v: Boolean) { mousearray[i + 1] = v }
}

// mouse values are used once
var mousex = 0
var mousey = 0

var dclicktime = 0
var dclickstate = 0
var dclicks = 0
var dclicktime2 = 0
var dclickstate2 = 0
var dclicks2 = 0

// joystick values are repeated
var joyxmove = 0
var joyymove = 0
val joyarray = BooleanArray(5)

/** C: boolean* joybuttons = &joyarray[1]; -- allow [-1] */
object joybuttons {
    operator fun get(i: Int): Boolean = joyarray[i + 1]
    operator fun set(i: Int, v: Boolean) { joyarray[i + 1] = v }
}

var savegameslot = 0
var savedescription = ""


const val BODYQUESIZE = 32

val bodyque = arrayOfNulls<mobj_t>(BODYQUESIZE)
var bodyqueslot = 0

// void* statcopy -- for statistics driver: dropped in the port.


fun G_CmdChecksum(cmd: ticcmd_t): Int {
    // C sums sizeof(ticcmd_t)/4 - 1 == 1 int of the struct's memory, i.e. the
    // first four bytes (forwardmove, sidemove, angleturn low/high) little-endian.
    var sum = 0
    sum += (cmd.forwardmove and 0xff) or
        ((cmd.sidemove and 0xff) shl 8) or
        ((cmd.angleturn and 0xffff) shl 16)

    return sum
}


// C: i_system.c owns I_BaseTiccmd (returns a static, never-written, all-zero
// ticcmd_t). The KMP platform layer (i_system.kt) does not provide it, so the
// empty base command lives here next to its only caller.
private val emptycmd = ticcmd_t()
fun I_BaseTiccmd(): ticcmd_t = emptycmd


//
// G_BuildTiccmd
// Builds a ticcmd from all of the available inputs
// or reads it from the demo buffer.
// If recording a demo, write it out
//
fun G_BuildTiccmd(cmd: ticcmd_t) {
    val base = I_BaseTiccmd()   // empty, or external driver
    cmd.copyFrom(base)

    cmd.consistancy =
        consistancy[consoleplayer][maketic % BACKUPTICS].toInt()


    val strafe = gamekeydown[key_strafe] || mousebuttons[mousebstrafe] ||
        joybuttons[joybstrafe]
    val speed = if (gamekeydown[key_speed] || joybuttons[joybspeed]) 1 else 0

    var forward = 0
    var side = 0

    // use two stage accelerative turning
    // on the keyboard and joystick
    if (joyxmove < 0 ||
        joyxmove > 0 ||
        gamekeydown[key_right] ||
        gamekeydown[key_left])
        turnheld += ticdup
    else
        turnheld = 0

    val tspeed: Int
    if (turnheld < SLOWTURNTICS)
        tspeed = 2              // slow turn
    else
        tspeed = speed

    // let movement keys cancel each other out
    if (strafe) {
        if (gamekeydown[key_right]) {
            // fprintf(stderr, "strafe right\n");
            side += sidemove[speed]
        }
        if (gamekeydown[key_left]) {
            //	fprintf(stderr, "strafe left\n");
            side -= sidemove[speed]
        }
        if (joyxmove > 0)
            side += sidemove[speed]
        if (joyxmove < 0)
            side -= sidemove[speed]
    } else {
        if (gamekeydown[key_right])
            cmd.angleturn -= angleturn[tspeed]
        if (gamekeydown[key_left])
            cmd.angleturn += angleturn[tspeed]
        if (joyxmove > 0)
            cmd.angleturn -= angleturn[tspeed]
        if (joyxmove < 0)
            cmd.angleturn += angleturn[tspeed]
    }

    if (gamekeydown[key_up]) {
        // fprintf(stderr, "up\n");
        forward += forwardmove[speed]
    }
    if (gamekeydown[key_down]) {
        // fprintf(stderr, "down\n");
        forward -= forwardmove[speed]
    }
    if (joyymove < 0)
        forward += forwardmove[speed]
    if (joyymove > 0)
        forward -= forwardmove[speed]
    if (gamekeydown[key_straferight])
        side += sidemove[speed]
    if (gamekeydown[key_strafeleft])
        side -= sidemove[speed]

    // buttons
    cmd.chatchar = HU_dequeueChatChar()

    if (gamekeydown[key_fire] || mousebuttons[mousebfire] ||
        joybuttons[joybfire])
        cmd.buttons = cmd.buttons or BT_ATTACK

    if (gamekeydown[key_use] || joybuttons[joybuse]) {
        cmd.buttons = cmd.buttons or BT_USE
        // clear double clicks if hit use button
        dclicks = 0
    }

    // chainsaw overrides
    for (i in 0 until NUMWEAPONS - 1)
        if (gamekeydown['1'.code + i]) {
            cmd.buttons = cmd.buttons or BT_CHANGE
            cmd.buttons = cmd.buttons or (i shl BT_WEAPONSHIFT)
            break
        }

    // mouse
    if (mousebuttons[mousebforward])
        forward += forwardmove[speed]

    // forward double click
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

    // strafe double click
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

    if (forward > MAXPLMOVE)
        forward = MAXPLMOVE
    else if (forward < -MAXPLMOVE)
        forward = -MAXPLMOVE
    if (side > MAXPLMOVE)
        side = MAXPLMOVE
    else if (side < -MAXPLMOVE)
        side = -MAXPLMOVE

    cmd.forwardmove += forward
    cmd.sidemove += side

    // special buttons
    if (sendpause) {
        sendpause = false
        cmd.buttons = BT_SPECIAL or BTS_PAUSE
    }

    if (sendsave) {
        sendsave = false
        cmd.buttons = BT_SPECIAL or BTS_SAVEGAME or (savegameslot shl BTS_SAVESHIFT)
    }
}


//
// G_DoLoadLevel
//
fun G_DoLoadLevel() {
    // Set the sky map.
    // First thing, we have a dummy sky texture name,
    //  a flat. The data is in the WAD only because
    //  we look for an actual index, instead of simply
    //  setting one.
    skyflatnum = R_FlatNumForName(SKYFLATNAME)

    // DOOM determines the sky texture to be used
    // depending on the current episode, and the game version.
    // (vanilla compares gamemode against the pack_* *mission* constants;
    //  kept verbatim -- the numeric values happen to line up.)
    if ((gamemode == commercial) ||
        (gamemode == pack_tnt) ||
        (gamemode == pack_plut)) {
        skytexture = R_TextureNumForName("SKY3")
        if (gamemap < 12)
            skytexture = R_TextureNumForName("SKY1")
        else
            if (gamemap < 21)
                skytexture = R_TextureNumForName("SKY2")
    }

    levelstarttic = gametic     // for time calculation

    if (wipegamestate == GS_LEVEL)
        wipegamestate = -1      // force a wipe

    gamestate = GS_LEVEL

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i] && players[i].playerstate == PST_DEAD)
            players[i].playerstate = PST_REBORN
        players[i].frags.fill(0)
    }

    P_SetupLevel(gameepisode, gamemap, 0, gameskill)
    displayplayer = consoleplayer  // view the guy you are playing
    starttime = I_GetTime()
    gameaction = ga_nothing
    // Z_CheckHeap ();

    // clear cmd building stuff
    gamekeydown.fill(false)
    joyxmove = 0
    joyymove = 0
    mousex = 0
    mousey = 0
    sendpause = false
    sendsave = false
    paused = false
    // memset (mousebuttons/joybuttons, 0, sizeof(pointer)) -- vanilla bug:
    // sizeof a boolean* is 4 bytes == exactly one 4-byte boolean, so only the
    // first button of each array gets cleared. Replicated verbatim.
    mousebuttons[0] = false
    joybuttons[0] = false
}


//
// G_Responder
// Get info needed to make ticcmd_ts for the players.
//
fun G_Responder(ev: event_t): Boolean {
    // allow spy mode changes even during the demo
    if (gamestate == GS_LEVEL && ev.type == ev_keydown &&
        ev.data1 == KEY_F12 && (singledemo || deathmatch == 0)) {
        // spy mode
        do {
            displayplayer++
            if (displayplayer == MAXPLAYERS)
                displayplayer = 0
        } while (!playeringame[displayplayer] && displayplayer != consoleplayer)
        return true
    }

    // any other key pops up menu if in demos
    if (gameaction == ga_nothing && !singledemo &&
        (demoplayback || gamestate == GS_DEMOSCREEN)) {
        if (ev.type == ev_keydown ||
            (ev.type == ev_mouse && ev.data1 != 0) ||
            (ev.type == ev_joystick && ev.data1 != 0)) {
            M_StartControlPanel()
            return true
        }
        return false
    }

    if (gamestate == GS_LEVEL) {
        if (HU_Responder(ev))
            return true         // chat ate the event
        if (ST_Responder(ev))
            return true         // status window ate it
        if (AM_Responder(ev))
            return true         // automap ate it
    }

    if (gamestate == GS_FINALE) {
        if (F_Responder(ev))
            return true         // finale ate the event
    }

    when (ev.type) {
        ev_keydown -> {
            if (ev.data1 == KEY_PAUSE) {
                sendpause = true
                return true
            }
            if (ev.data1 < NUMKEYS)
                gamekeydown[ev.data1] = true
            return true         // eat key down events
        }

        ev_keyup -> {
            if (ev.data1 < NUMKEYS)
                gamekeydown[ev.data1] = false
            return false        // always let key up events filter down
        }

        ev_mouse -> {
            mousebuttons[0] = (ev.data1 and 1) != 0
            mousebuttons[1] = (ev.data1 and 2) != 0
            mousebuttons[2] = (ev.data1 and 4) != 0
            mousex = ev.data2 * (mouseSensitivity + 5) / 10
            mousey = ev.data3 * (mouseSensitivity + 5) / 10
            return true         // eat events
        }

        ev_joystick -> {
            joybuttons[0] = (ev.data1 and 1) != 0
            joybuttons[1] = (ev.data1 and 2) != 0
            joybuttons[2] = (ev.data1 and 4) != 0
            joybuttons[3] = (ev.data1 and 8) != 0
            joyxmove = ev.data2
            joyymove = ev.data3
            return true         // eat events
        }

        else -> {}
    }

    return false
}


//
// G_Ticker
// Make ticcmd_ts for the players.
//
fun G_Ticker() {
    // do player reborns if needed
    for (i in 0 until MAXPLAYERS)
        if (playeringame[i] && players[i].playerstate == PST_REBORN)
            G_DoReborn(i)

    // do things to change the game state
    while (gameaction != ga_nothing) {
        when (gameaction) {
            ga_loadlevel -> G_DoLoadLevel()
            ga_newgame -> G_DoNewGame()
            ga_loadgame -> G_DoLoadGame()
            ga_savegame -> G_DoSaveGame()
            ga_playdemo -> G_DoPlayDemo()
            ga_completed -> G_DoCompleted()
            ga_victory -> F_StartFinale()
            ga_worlddone -> G_DoWorldDone()
            ga_screenshot -> {
                M_ScreenShot()
                gameaction = ga_nothing
            }
            ga_nothing -> {}
        }
    }

    // get commands, check consistancy,
    // and build new consistancy check
    val buf = (gametic / ticdup) % BACKUPTICS

    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            val cmd = players[i].cmd

            cmd.copyFrom(netcmds[i][buf])

            if (demoplayback)
                G_ReadDemoTiccmd(cmd)
            if (demorecording)
                G_WriteDemoTiccmd(cmd)

            // check for turbo cheats
            if (cmd.forwardmove > TURBOTHRESHOLD &&
                (gametic and 31) == 0 && ((gametic shr 5) and 3) == i) {
                players[consoleplayer].message = "${player_names[i]} is turbo!"
            }

            if (netgame && !netdemo && gametic % ticdup == 0) {
                if (gametic > BACKUPTICS &&
                    consistancy[i][buf].toInt() != cmd.consistancy) {
                    I_Error("consistency failure (${cmd.consistancy} should be ${consistancy[i][buf]})")
                }
                if (players[i].mo != null)
                    consistancy[i][buf] = players[i].mo!!.x.toShort()
                else
                    consistancy[i][buf] = rndindex.toShort()
            }
        }
    }

    // check for special buttons
    for (i in 0 until MAXPLAYERS) {
        if (playeringame[i]) {
            if ((players[i].cmd.buttons and BT_SPECIAL) != 0) {
                when (players[i].cmd.buttons and BT_SPECIALMASK) {
                    BTS_PAUSE -> {
                        paused = !paused
                        if (paused)
                            S_PauseSound()
                        else
                            S_ResumeSound()
                    }

                    BTS_SAVEGAME -> {
                        if (savedescription.isEmpty())
                            savedescription = "NET GAME"
                        savegameslot =
                            (players[i].cmd.buttons and BTS_SAVEMASK) shr BTS_SAVESHIFT
                        gameaction = ga_savegame
                    }
                }
            }
        }
    }

    // do main actions
    when (gamestate) {
        GS_LEVEL -> {
            P_Ticker()
            ST_Ticker()
            AM_Ticker()
            HU_Ticker()
        }

        GS_INTERMISSION -> WI_Ticker()

        GS_FINALE -> F_Ticker()

        GS_DEMOSCREEN -> D_PageTicker()
    }
}


//
// PLAYER STRUCTURE FUNCTIONS
// also see P_SpawnPlayer in P_Things
//

//
// G_InitPlayer
// Called at the start.
// Called by the game initialization functions.
//
fun G_InitPlayer(player: Int) {
    // set up the saved info
    val p = players[player]

    // clear everything else to defaults
    G_PlayerReborn(player)
}


//
// G_PlayerFinishLevel
// Can when a player completes a level.
//
fun G_PlayerFinishLevel(player: Int) {
    val p = players[player]

    p.powers.fill(0)
    p.cards.fill(false)
    p.mo!!.flags = p.mo!!.flags and MF_SHADOW.inv()  // cancel invisibility
    p.extralight = 0            // cancel gun flashes
    p.fixedcolormap = 0         // cancel ir gogles
    p.damagecount = 0           // no palette changes
    p.bonuscount = 0
}


//
// G_PlayerReborn
// Called after a player dies
// almost everything is cleared and initialized
//
fun G_PlayerReborn(player: Int) {
    val frags = IntArray(MAXPLAYERS)
    players[player].frags.copyInto(frags)
    val killcount = players[player].killcount
    val itemcount = players[player].itemcount
    val secretcount = players[player].secretcount

    val p = players[player]
    // memset (p, 0, sizeof(*p)); -- clear the struct in place (other objects
    // hold references to this player_t, exactly like C pointers into players[])
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
    p.attackdown = true         // don't do anything immediately
    p.playerstate = PST_LIVE
    p.health = MAXHEALTH
    p.readyweapon = wp_pistol
    p.pendingweapon = wp_pistol
    p.weaponowned[wp_fist] = true
    p.weaponowned[wp_pistol] = true
    p.ammo[am_clip] = 50

    for (i in 0 until NUMAMMO)
        p.maxammo[i] = maxammo[i]
}

//
// G_CheckSpot
// Returns false if the player cannot be respawned
// at the given mapthing_t spot
// because something is occupying it
//
fun G_CheckSpot(playernum: Int, mthing: mapthing_t): Boolean {
    if (players[playernum].mo == null) {
        // first spawn of level, before corpses
        for (i in 0 until playernum)
            if (players[i].mo!!.x == mthing.x shl FRACBITS &&
                players[i].mo!!.y == mthing.y shl FRACBITS)
                return false
        return true
    }

    val x: fixed_t = mthing.x shl FRACBITS
    val y: fixed_t = mthing.y shl FRACBITS

    if (!P_CheckPosition(players[playernum].mo!!, x, y))
        return false

    // flush an old corpse if needed
    if (bodyqueslot >= BODYQUESIZE)
        P_RemoveMobj(bodyque[bodyqueslot % BODYQUESIZE]!!)
    bodyque[bodyqueslot % BODYQUESIZE] = players[playernum].mo
    bodyqueslot++

    // spawn a teleport fog
    val ss = R_PointInSubsector(x, y)
    val an = ((ANG45 * (mthing.angle / 45).toUInt()) shr ANGLETOFINESHIFT).toInt()

    val mo = P_SpawnMobj(x + 20 * finecosine[an], y + 20 * finesine[an],
        ss.sector!!.floorheight,
        MT_TFOG)

    if (players[consoleplayer].viewz != 1)
        S_StartSound(mo, sfx_telept)  // don't start sound on first frame

    return true
}


//
// G_DeathMatchSpawnPlayer
// Spawns a player at one of the random death match spots
// called at level load and each death
//
fun G_DeathMatchSpawnPlayer(playernum: Int) {
    val selections = deathmatch_p  // C: deathmatch_p - deathmatchstarts
    if (selections < 4)
        I_Error("Only $selections deathmatch spots, 4 required")

    for (j in 0 until 20) {
        val i = P_Random() % selections
        if (G_CheckSpot(playernum, deathmatchstarts[i])) {
            deathmatchstarts[i].type = playernum + 1
            P_SpawnPlayer(deathmatchstarts[i])
            return
        }
    }

    // no good spot, so the player will probably get stuck
    P_SpawnPlayer(playerstarts[playernum])
}

//
// G_DoReborn
//
fun G_DoReborn(playernum: Int) {
    if (!netgame) {
        // reload the level from scratch
        gameaction = ga_loadlevel
    } else {
        // respawn at the start

        // first dissasociate the corpse
        players[playernum].mo!!.player = null

        // spawn at random spot if in death match
        if (deathmatch != 0) {
            G_DeathMatchSpawnPlayer(playernum)
            return
        }

        if (G_CheckSpot(playernum, playerstarts[playernum])) {
            P_SpawnPlayer(playerstarts[playernum])
            return
        }

        // try to spawn at one of the other players spots
        for (i in 0 until MAXPLAYERS) {
            if (G_CheckSpot(playernum, playerstarts[i])) {
                playerstarts[i].type = playernum + 1  // fake as other player
                P_SpawnPlayer(playerstarts[i])
                playerstarts[i].type = i + 1          // restore
                return
            }
            // he's going to be inside something.  Too bad.
        }
        P_SpawnPlayer(playerstarts[playernum])
    }
}


fun G_ScreenShot() {
    gameaction = ga_screenshot
}


// DOOM Par Times
val pars = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(0, 30, 75, 120, 90, 165, 180, 180, 30, 165),
    intArrayOf(0, 90, 90, 90, 120, 90, 360, 240, 30, 170),
    intArrayOf(0, 90, 45, 90, 150, 90, 90, 165, 30, 135)
)

// DOOM II Par Times
val cpars = intArrayOf(
    30, 90, 120, 120, 90, 150, 120, 120, 270, 90,        //  1-10
    210, 150, 150, 150, 210, 150, 420, 150, 210, 150,    // 11-20
    240, 150, 180, 150, 150, 300, 330, 420, 300, 180,    // 21-30
    120, 30                                              // 31-32
)


//
// G_DoCompleted
//
var secretexit = false

fun G_ExitLevel() {
    secretexit = false
    gameaction = ga_completed
}

// Here's for the german edition.
fun G_SecretExitLevel() {
    // IF NO WOLF3D LEVELS, NO SECRET EXIT!
    if ((gamemode == commercial) &&
        (W_CheckNumForName("map31") < 0))
        secretexit = false
    else
        secretexit = true
    gameaction = ga_completed
}

fun G_DoCompleted() {
    gameaction = ga_nothing

    for (i in 0 until MAXPLAYERS)
        if (playeringame[i])
            G_PlayerFinishLevel(i)  // take away cards and stuff

    if (automapactive)
        AM_Stop()

    if (gamemode != commercial)
        when (gamemap) {
            8 -> {
                gameaction = ga_victory
                return
            }
            9 -> {
                for (i in 0 until MAXPLAYERS)
                    players[i].didsecret = true
            }
        }

    //#if 0  Hmmm - why?
    if ((gamemap == 8) &&
        (gamemode != commercial)) {
        // victory
        gameaction = ga_victory
        return
    }

    if ((gamemap == 9) &&
        (gamemode != commercial)) {
        // exit secret level
        for (i in 0 until MAXPLAYERS)
            players[i].didsecret = true
    }
    //#endif


    wminfo.didsecret = players[consoleplayer].didsecret
    wminfo.epsd = gameepisode - 1
    wminfo.last = gamemap - 1

    // wminfo.next is 0 biased, unlike gamemap
    if (gamemode == commercial) {
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
            wminfo.next = 8     // go to secret level
        else if (gamemap == 9) {
            // returning from secret level
            when (gameepisode) {
                1 -> wminfo.next = 3
                2 -> wminfo.next = 5
                3 -> wminfo.next = 6
                4 -> wminfo.next = 2
            }
        } else
            wminfo.next = gamemap  // go to next level
    }

    wminfo.maxkills = totalkills
    wminfo.maxitems = totalitems
    wminfo.maxsecret = totalsecret
    wminfo.maxfrags = 0
    if (gamemode == commercial)
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

    // if (statcopy) memcpy (statcopy, &wminfo, sizeof(wminfo)); -- statistics
    // driver dropped in the port.

    WI_Start(wminfo)
}


//
// G_WorldDone
//
fun G_WorldDone() {
    gameaction = ga_worlddone

    if (secretexit)
        players[consoleplayer].didsecret = true

    if (gamemode == commercial) {
        // (case 15/31 fall through to the finale only on a secret exit)
        when (gamemap) {
            15, 31 ->
                if (secretexit)
                    F_StartFinale()
            6, 11, 20, 30 ->
                F_StartFinale()
        }
    }
}

fun G_DoWorldDone() {
    gamestate = GS_LEVEL
    gamemap = wminfo.next + 1
    G_DoLoadLevel()
    gameaction = ga_nothing
    viewactive = true
}


//
// G_InitFromSavegame
// Can be called by the startup code or the menu task.
//

var savename = ""

fun G_LoadGame(name: String) {
    savename = name
    gameaction = ga_loadgame
}

private const val VERSIONSIZE = 16


fun G_DoLoadGame() {
    gameaction = ga_nothing

    savebuffer = M_ReadFile(savename)
        ?: I_Error("Couldn't read file $savename")  // C: I_Error inside M_ReadFile
    save_p = SAVESTRINGSIZE  // skip the description field

    val vcheck = "version $VERSION"
    if (savebuffer.str(save_p, VERSIONSIZE) != vcheck)
        return                  // bad version
    save_p += VERSIONSIZE

    gameskill = savebuffer.u8(save_p); save_p++
    gameepisode = savebuffer.u8(save_p); save_p++
    gamemap = savebuffer.u8(save_p); save_p++
    for (i in 0 until MAXPLAYERS) {
        playeringame[i] = savebuffer.u8(save_p) != 0
        save_p++
    }

    // load a base level
    G_InitNew(gameskill, gameepisode, gamemap)

    // get the times
    val a = savebuffer.u8(save_p); save_p++
    val b = savebuffer.u8(save_p); save_p++
    val c = savebuffer.u8(save_p); save_p++
    leveltime = (a shl 16) + (b shl 8) + c

    // dearchive all the modifications
    P_UnArchivePlayers()
    P_UnArchiveWorld()
    P_UnArchiveThinkers()
    P_UnArchiveSpecials()

    if (savebuffer.u8(save_p) != 0x1d)
        I_Error("Bad savegame")

    // done
    // Z_Free (savebuffer);

    if (setsizeneeded)
        R_ExecuteSetViewSize()

    // draw the pattern into the back screen
    R_FillBackScreen()
}


//
// G_SaveGame
// Called by the menu task.
// Description is a 24 byte text string
//
fun G_SaveGame(slot: Int, description: String) {
    savegameslot = slot
    savedescription = description
    sendsave = true
}

fun G_DoSaveGame() {
    val name: String
    if (M_CheckParm("-cdrom") != 0)
        name = "c:\\doomdata\\$SAVEGAMENAME${savegameslot}.dsg"
    else
        name = "$SAVEGAMENAME${savegameslot}.dsg"
    val description = savedescription

    // C: save_p = savebuffer = screens[1]+0x4000 (scratch memory); allocated here.
    savebuffer = ByteArray(SAVEGAMESIZE)
    save_p = 0

    // memcpy (save_p, description, SAVESTRINGSIZE);
    for (i in 0 until SAVESTRINGSIZE)
        savebuffer[save_p + i] =
            if (i < description.length) description[i].code.toByte() else 0
    save_p += SAVESTRINGSIZE
    val name2 = "version $VERSION"
    for (i in 0 until VERSIONSIZE)
        savebuffer[save_p + i] =
            if (i < name2.length) name2[i].code.toByte() else 0
    save_p += VERSIONSIZE

    savebuffer[save_p] = gameskill.toByte(); save_p++
    savebuffer[save_p] = gameepisode.toByte(); save_p++
    savebuffer[save_p] = gamemap.toByte(); save_p++
    for (i in 0 until MAXPLAYERS) {
        savebuffer[save_p] = (if (playeringame[i]) 1 else 0).toByte()
        save_p++
    }
    savebuffer[save_p] = (leveltime shr 16).toByte(); save_p++
    savebuffer[save_p] = (leveltime shr 8).toByte(); save_p++
    savebuffer[save_p] = leveltime.toByte(); save_p++

    P_ArchivePlayers()
    P_ArchiveWorld()
    P_ArchiveThinkers()
    P_ArchiveSpecials()

    savebuffer[save_p] = 0x1d; save_p++  // consistancy marker

    val length = save_p  // save_p - savebuffer
    if (length > SAVEGAMESIZE)
        I_Error("Savegame buffer overrun")
    M_WriteFile(name, savebuffer.copyOf(length))
    gameaction = ga_nothing
    savedescription = ""

    players[consoleplayer].message = GGSAVED

    // draw the pattern into the back screen
    R_FillBackScreen()
}


//
// G_InitNew
// Can be called by the startup code or the menu task,
// consoleplayer, displayplayer, playeringame[] should be set.
//
var d_skill = 0  // skill_t
var d_episode = 0
var d_map = 0

fun G_DeferedInitNew(skill: Int, episode: Int, map: Int) {
    d_skill = skill
    d_episode = episode
    d_map = map
    gameaction = ga_newgame
}


fun G_DoNewGame() {
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
    G_InitNew(d_skill, d_episode, d_map)
    gameaction = ga_nothing
}


fun G_InitNew(skill: Int, episode: Int, map: Int) {
    var skill = skill
    var episode = episode
    var map = map

    if (paused) {
        paused = false
        S_ResumeSound()
    }


    if (skill > sk_nightmare)
        skill = sk_nightmare


    // This was quite messy with SPECIAL and commented parts.
    // Supposedly hacks to make the latest edition work.
    // It might not work properly.
    if (episode < 1)
        episode = 1

    if (gamemode == retail) {
        if (episode > 4)
            episode = 4
    } else if (gamemode == shareware) {
        if (episode > 1)
            episode = 1     // only start episode 1 on shareware
    } else {
        if (episode > 3)
            episode = 3
    }


    if (map < 1)
        map = 1

    if ((map > 9) &&
        (gamemode != commercial))
        map = 9

    M_ClearRandom()

    if (skill == sk_nightmare || respawnparm)
        respawnmonsters = true
    else
        respawnmonsters = false

    if (fastparm || (skill == sk_nightmare && gameskill != sk_nightmare)) {
        for (i in S_SARG_RUN1..S_SARG_PAIN2)
            states[i].tics = states[i].tics shr 1
        mobjinfo[MT_BRUISERSHOT].speed = 20 * FRACUNIT
        mobjinfo[MT_HEADSHOT].speed = 20 * FRACUNIT
        mobjinfo[MT_TROOPSHOT].speed = 20 * FRACUNIT
    } else if (skill != sk_nightmare && gameskill == sk_nightmare) {
        for (i in S_SARG_RUN1..S_SARG_PAIN2)
            states[i].tics = states[i].tics shl 1
        mobjinfo[MT_BRUISERSHOT].speed = 15 * FRACUNIT
        mobjinfo[MT_HEADSHOT].speed = 10 * FRACUNIT
        mobjinfo[MT_TROOPSHOT].speed = 10 * FRACUNIT
    }


    // force players to be initialized upon first level load
    for (i in 0 until MAXPLAYERS)
        players[i].playerstate = PST_REBORN

    usergame = true             // will be set false if a demo
    paused = false
    demoplayback = false
    automapactive = false
    viewactive = true
    gameepisode = episode
    gamemap = map
    gameskill = skill

    viewactive = true

    // set the sky map for the episode
    if (gamemode == commercial) {
        skytexture = R_TextureNumForName("SKY3")
        if (gamemap < 12)
            skytexture = R_TextureNumForName("SKY1")
        else
            if (gamemap < 21)
                skytexture = R_TextureNumForName("SKY2")
    } else
        when (episode) {
            1 ->
                skytexture = R_TextureNumForName("SKY1")
            2 ->
                skytexture = R_TextureNumForName("SKY2")
            3 ->
                skytexture = R_TextureNumForName("SKY3")
            4 ->  // Special Edition sky
                skytexture = R_TextureNumForName("SKY4")
        }

    G_DoLoadLevel()
}


//
// DEMO RECORDING
//
private const val DEMOMARKER = 0x80


fun G_ReadDemoTiccmd(cmd: ticcmd_t) {
    val demobuf = demobuffer!!
    if (demobuf.u8(demo_p) == DEMOMARKER) {
        // end of demo data stream
        G_CheckDemoStatus()
        return
    }
    cmd.forwardmove = demobuf.i8(demo_p); demo_p++   // (signed char)
    cmd.sidemove = demobuf.i8(demo_p); demo_p++      // (signed char)
    // C stores angleturn in a signed short field: values >= 0x8000 go negative.
    cmd.angleturn = (demobuf.u8(demo_p) shl 8).toShort().toInt(); demo_p++
    cmd.buttons = demobuf.u8(demo_p); demo_p++
}


fun G_WriteDemoTiccmd(cmd: ticcmd_t) {
    if (gamekeydown['q'.code])  // press q to end demo recording
        G_CheckDemoStatus()
    val demobuf = demobuffer!!
    demobuf[demo_p] = cmd.forwardmove.toByte(); demo_p++
    demobuf[demo_p] = cmd.sidemove.toByte(); demo_p++
    demobuf[demo_p] = ((cmd.angleturn + 128) shr 8).toByte(); demo_p++
    demobuf[demo_p] = cmd.buttons.toByte(); demo_p++
    demo_p -= 4
    if (demo_p > demoend - 16) {
        // no more space
        G_CheckDemoStatus()
        return
    }

    G_ReadDemoTiccmd(cmd)       // make SURE it is exactly the same
}


//
// G_RecordDemo
//
fun G_RecordDemo(name: String) {
    usergame = false
    demoname = "$name.lmp"      // strcpy + strcat ".lmp"
    var maxsize = 0x20000
    val i = M_CheckParm("-maxdemo")
    if (i != 0 && i < myargc - 1)
        maxsize = (myargv[i + 1].toIntOrNull() ?: 0) * 1024
    demobuffer = ByteArray(maxsize)
    demoend = maxsize           // demoend = demobuffer + maxsize

    demorecording = true
}


fun G_BeginRecording() {
    demo_p = 0                  // demo_p = demobuffer
    val demobuf = demobuffer!!

    demobuf[demo_p] = VERSION.toByte(); demo_p++
    demobuf[demo_p] = gameskill.toByte(); demo_p++
    demobuf[demo_p] = gameepisode.toByte(); demo_p++
    demobuf[demo_p] = gamemap.toByte(); demo_p++
    demobuf[demo_p] = deathmatch.toByte(); demo_p++
    demobuf[demo_p] = (if (respawnparm) 1 else 0).toByte(); demo_p++
    demobuf[demo_p] = (if (fastparm) 1 else 0).toByte(); demo_p++
    demobuf[demo_p] = (if (nomonsters) 1 else 0).toByte(); demo_p++
    demobuf[demo_p] = consoleplayer.toByte(); demo_p++

    for (i in 0 until MAXPLAYERS) {
        demobuf[demo_p] = (if (playeringame[i]) 1 else 0).toByte()
        demo_p++
    }
}


//
// G_PlayDemo
//

var defdemoname = ""

fun G_DeferedPlayDemo(name: String) {
    defdemoname = name
    gameaction = ga_playdemo
}

fun G_DoPlayDemo() {
    gameaction = ga_nothing
    demobuffer = W_CacheLumpName(defdemoname)
    demo_p = 0
    val demobuf = demobuffer!!
    val version = demobuf.u8(demo_p); demo_p++
    if (version != VERSION) {
        println("Demo is from a different game version!")
        gameaction = ga_nothing
        return
    }

    val skill = demobuf.u8(demo_p); demo_p++
    val episode = demobuf.u8(demo_p); demo_p++
    val map = demobuf.u8(demo_p); demo_p++
    deathmatch = demobuf.u8(demo_p); demo_p++
    respawnparm = demobuf.u8(demo_p) != 0; demo_p++
    fastparm = demobuf.u8(demo_p) != 0; demo_p++
    nomonsters = demobuf.u8(demo_p) != 0; demo_p++
    consoleplayer = demobuf.u8(demo_p); demo_p++

    for (i in 0 until MAXPLAYERS) {
        playeringame[i] = demobuf.u8(demo_p) != 0
        demo_p++
    }
    if (playeringame[1]) {
        netgame = true
        netdemo = true
    }

    // don't spend a lot of time in loadlevel
    precache = false
    G_InitNew(skill, episode, map)
    precache = true

    usergame = false
    demoplayback = true
}

//
// G_TimeDemo
//
fun G_TimeDemo(name: String) {
    nodrawers = M_CheckParm("-nodraw") != 0
    noblit = M_CheckParm("-noblit") != 0
    timingdemo = true
    singletics = true

    defdemoname = name
    gameaction = ga_playdemo
}


/*
===================
=
= G_CheckDemoStatus
=
= Called after a death or level completion to allow demos to be cleaned up
= Returns true if a new demo loop action will take place
===================
*/

fun G_CheckDemoStatus(): Boolean {
    if (timingdemo) {
        val endtime = I_GetTime()
        I_Error("timed $gametic gametics in ${endtime - starttime} realtics")
    }

    if (demoplayback) {
        if (singledemo)
            I_Quit()            // vanilla exit()s here and never returns

        // Z_ChangeTag (demobuffer, PU_CACHE);
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
        D_AdvanceDemo()
        return true
    }

    if (demorecording) {
        val demobuf = demobuffer!!
        demobuf[demo_p] = DEMOMARKER.toByte(); demo_p++
        M_WriteFile(demoname, demobuf.copyOf(demo_p))
        // Z_Free (demobuffer);
        demorecording = false
        I_Error("Demo $demoname recorded")
    }

    return false
}
