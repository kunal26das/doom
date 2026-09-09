// Port of linuxdoom-1.10 hu_lib.c/hu_lib.h -- heads-up text and input code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class HuLibState {
    var lastautomapactive = true
}
