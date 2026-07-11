// Port of linuxdoom-1.10 m_cheat.c/m_cheat.h -- cheat sequence checking.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// CHEAT SEQUENCE PACKAGE
//

/** The C SCRAMBLE macro from m_cheat.h (identical bit shuffle). */
fun SCRAMBLE(a: Int): Int =
    ((a and 1) shl 7) + ((a and 2) shl 5) + (a and 4) + ((a and 8) shl 1) +
        ((a and 16) shr 1) + (a and 32) + ((a and 64) shr 5) + ((a and 128) shr 7)

/**
 * C: { unsigned char* sequence; unsigned char* p; }
 *
 * `sequence` is a MUTABLE byte buffer (cht_CheckCheat types parameter keys into
 * the 0x00 slots and cht_GetParam reads/clears them again); the IntArray values
 * are the original byte values (0x01 marks the start of a parameter run, 0xff
 * ends the sequence). `p` is an index into `sequence`; -1 stands for the NULL
 * pointer of the C static initializers `{ seq, 0 }` (the `p` constructor
 * argument is that initializer's 0 == NULL).
 */
class cheatseq_t(val sequence: IntArray, p: Int) {
    var p: Int = -1
}

private var firsttime = 1
private val cheat_xlate_table = IntArray(256)

//
// Called in st_stuff module, which handles the input.
// Returns a 1 if the cheat was successful, 0 if failed.
//
fun cht_CheckCheat(cht: cheatseq_t, key: Int): Int {
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

fun cht_GetParam(cht: cheatseq_t, buffer: CharArray) {
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
