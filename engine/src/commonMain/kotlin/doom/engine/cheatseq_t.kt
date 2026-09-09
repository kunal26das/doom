// Port of linuxdoom-1.10 m_cheat.c/m_cheat.h -- cheat sequence checking.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

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
internal class cheatseq_t(val sequence: IntArray, p: Int) {
    var p: Int = -1
}
