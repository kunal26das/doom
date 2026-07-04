// Port of linuxdoom-1.10 p_tick.c -- archiving: savegame I/O; thinker, ticker.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

var leveltime = 0

//
// THINKERS
// All thinkers should be allocated by Z_Malloc
// so they can be operated on uniformly.
// The actual structures will vary in size,
// but the first element must be thinker_t.
//

// Both the head and tail of the thinker list.
val thinkercap = thinker_t()

//
// P_InitThinkers
//
fun P_InitThinkers() {
    thinkercap.next = thinkercap
    thinkercap.prev = thinkercap.next
}

//
// P_AddThinker
// Adds a new thinker at the end of the list.
//
fun P_AddThinker(thinker: thinker_t) {
    thinkercap.prev!!.next = thinker
    thinker.next = thinkercap
    thinker.prev = thinkercap.prev
    thinkercap.prev = thinker
}

//
// P_RemoveThinker
// Deallocation is lazy -- it will not actually be freed
// until its thinking turn comes up.
//
fun P_RemoveThinker(thinker: thinker_t) {
    // FIXME: NOP.
    // (C sets thinker->function.acv = (actionf_v)(-1); here the `removed`
    // flag stands in for the -1 sentinel -- see d_think.kt.)
    thinker.removed = true
}

//
// P_AllocateThinker
// Allocates memory and adds a new thinker at the end of the list.
//
fun P_AllocateThinker(thinker: thinker_t) {
}

//
// P_RunThinkers
//
fun P_RunThinkers() {
    var currentthinker: thinker_t

    currentthinker = thinkercap.next!!
    while (currentthinker !== thinkercap) {
        if (currentthinker.removed) {
            // time to remove it
            currentthinker.next!!.prev = currentthinker.prev
            currentthinker.prev!!.next = currentthinker.next
        } else {
            if (currentthinker.function != null)
                currentthinker.function!!(currentthinker)
        }
        currentthinker = currentthinker.next!!
    }
}

//
// P_Ticker
//

fun P_Ticker() {
    var i: Int

    // run the tic
    if (paused)
        return

    // pause if in menu and at least one tic has been run
    if (!netgame
        && menuactive
        && !demoplayback
        && players[consoleplayer].viewz != 1) {
        return
    }

    i = 0
    while (i < MAXPLAYERS) {
        if (playeringame[i])
            P_PlayerThink(players[i])
        i++
    }

    P_RunThinkers()
    P_UpdateSpecials()
    P_RespawnSpecials()

    // for par times
    leveltime++
}
