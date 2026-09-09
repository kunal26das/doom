// Port of linuxdoom-1.10 m_cheat.c/m_cheat.h -- cheat sequence checking.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// CHEAT SEQUENCE PACKAGE
//

/** The C SCRAMBLE macro from m_cheat.h (identical bit shuffle). */
internal fun DoomEngineCore.SCRAMBLE(a: Int): Int =
    ((a and 1) shl 7) + ((a and 2) shl 5) + (a and 4) + ((a and 8) shl 1) +
        ((a and 16) shr 1) + (a and 32) + ((a and 64) shr 5) + ((a and 128) shr 7)


private var DoomEngineCore.firsttime
    get() = stateCheatDecoder.firsttime
    set(value) { stateCheatDecoder.firsttime = value }
private val DoomEngineCore.cheat_xlate_table
    get() = stateCheatDecoder.cheat_xlate_table

//
// Called in st_stuff module, which handles the input.
// Returns a 1 if the cheat was successful, 0 if failed.
//
internal fun DoomEngineCore.cht_CheckCheat(cht: cheatseq_t, key: Int): Int {
    var rc = 0

    if (firsttime != 0) {
        firsttime = 0
        for (i in 0 until 256) cheat_xlate_table[i] = SCRAMBLE(i)
    }

    if (cht.p == -1)
        cht.p = 0  // initialize if first time

    if (cht.sequence[cht.p] == 0) {
        // C: *(cht->p++) = key;
        cht.sequence[cht.p] = key and 0xff
        cht.p++
    } else if (cheat_xlate_table[key and 0xff] == cht.sequence[cht.p]) {
        cht.p++
    } else {
        cht.p = 0
    }

    if (cht.sequence[cht.p] == 1) {
        cht.p++
    } else if (cht.sequence[cht.p] == 0xff) {  // end of sequence character
        cht.p = 0
        rc = 1
    }

    return rc
}

internal fun DoomEngineCore.cht_GetParam(cht: cheatseq_t, buffer: CharArray) {
    var p = 0
    var bufi = 0
    var c: Int

    // C: while (*(p++) != 1);
    while (true) {
        val v = cht.sequence[p]
        p++
        if (v == 1) break
    }

    do {
        c = cht.sequence[p]
        // C: *(buffer++) = c;
        buffer[bufi] = c.toChar()
        bufi++
        // C: *(p++) = 0;
        cht.sequence[p] = 0
        p++
    } while (c != 0 && cht.sequence[p] != 0xff)

    if (cht.sequence[p] == 0xff)
        buffer[bufi] = 0.toChar()
}
