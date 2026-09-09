
package doom.engine.cheats

import doom.engine.core.DoomEngineCore


internal fun DoomEngineCore.scramble(a: Int): Int =
    ((a and 1) shl 7) + ((a and 2) shl 5) + (a and 4) + ((a and 8) shl 1) +
        ((a and 16) shr 1) + (a and 32) + ((a and 64) shr 5) + ((a and 128) shr 7)

private var DoomEngineCore.firsttime
    get() = stateCheatDecoder.firsttime
    set(value) { stateCheatDecoder.firsttime = value }
private val DoomEngineCore.cheatXlateTable
    get() = stateCheatDecoder.cheatXlateTable

internal fun DoomEngineCore.chtCheckCheat(cht: CheatSequence, key: Int): Int {
    var rc = 0

    if (firsttime != 0) {
        firsttime = 0
        for (i in 0 until 256) cheatXlateTable[i] = scramble(i)
    }

    if (cht.p == -1)
        cht.p = 0

    if (cht.sequence[cht.p] == 0) {
        cht.sequence[cht.p] = key and 0xff
        cht.p++
    } else if (cheatXlateTable[key and 0xff] == cht.sequence[cht.p]) {
        cht.p++
    } else {
        cht.p = 0
    }

    if (cht.sequence[cht.p] == 1) {
        cht.p++
    } else if (cht.sequence[cht.p] == 0xff) {
        cht.p = 0
        rc = 1
    }

    return rc
}

internal fun DoomEngineCore.chtGetParam(cht: CheatSequence, buffer: CharArray) {
    var p = 0
    var bufi = 0
    var c: Int

    while (true) {
        val v = cht.sequence[p]
        p++
        if (v == 1) break
    }

    do {
        c = cht.sequence[p]
        buffer[bufi] = c.toChar()
        bufi++
        cht.sequence[p] = 0
        p++
    } while (c != 0 && cht.sequence[p] != 0xff)

    if (cht.sequence[p] == 0xff)
        buffer[bufi] = 0.toChar()
}
