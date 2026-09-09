// Port of linuxdoom-1.10 m_cheat.c/m_cheat.h -- cheat sequence checking.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class CheatDecoderState {
    var firsttime = 1

    val cheat_xlate_table by lazy(LazyThreadSafetyMode.NONE) { IntArray(256) }
}
