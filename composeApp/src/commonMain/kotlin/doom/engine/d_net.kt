// Port of linuxdoom-1.10 d_net.c + d_net.h -- DOOM network game communication
// and protocol, all OS independend parts.
// Networking is NOT ported: single local player only, netgame always false.
// NetUpdate/TryRunTics keep the vanilla names and 35 Hz timing; the structure
// follows chocolate-doom's d_loop simplification (ticdup 1, no packets).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

// Max computers/players in a game.
const val MAXNETNODES = 8

// Networking and tick handling related.
const val BACKUPTICS = 12

//
// NETWORKING
//
// gametic is the tic about to (or currently being) run
// maketic is the tick that hasn't had control made for it yet
//
val localcmds = Array(BACKUPTICS) { ticcmd_t() }

val netcmds = Array(MAXPLAYERS) { Array(BACKUPTICS) { ticcmd_t() } }

var maketic = 0
var ticdup = 1

//
// NetUpdate
// Builds ticcmds for console player,
// sends out a packet
//
var gametime = 0

fun NetUpdate() {
    // check time
    val nowtime = I_GetTime() / ticdup
    val newtics = nowtime - gametime
    gametime = nowtime

    if (newtics <= 0)  // nothing new to update
        return

    // build new ticcmds for console player
    // (single player: build straight into netcmds[consoleplayer];
    //  vanilla built into localcmds and looped them back as a packet)
    var i = 0
    while (i < newtics) {
        I_StartTic()
        D_ProcessEvents()
        if (maketic - gametic / ticdup >= BACKUPTICS / 2 - 1)
            break  // can't hold any more

        G_BuildTiccmd(netcmds[consoleplayer][maketic % BACKUPTICS])
        maketic++
        i++
    }
}

//
// D_QuitNetGame
// Called before quitting to leave a net game
// without hanging the other players
//
fun D_QuitNetGame() {
    // no-op: networking not ported, nothing to notify
}

//
// TryRunTics
//
private var oldentertics = 0

fun TryRunTics() {
    // get real tics
    val entertic = I_GetTime() / ticdup
    val realtics = entertic - oldentertics
    oldentertics = entertic

    // get available tics
    NetUpdate()

    // decide how many tics to run
    var counts = maketic - gametic
    if (counts > 4)
        counts = 4  // cap catch-up; vanilla DOS blocked waiting for new tics instead

    // run the count tics
    while (counts > 0) {
        counts--

        if (advancedemo)
            D_DoAdvanceDemo()
        M_Ticker()
        G_Ticker()
        gametic++
    }
}
