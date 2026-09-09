// Port of linuxdoom-1.10 p_saveg.c -- archiving: SaveGame I/O.
// Vanilla memcpy's whole structs into the save buffer; this port follows
// chocolate-doom's p_saveg.c and reads/writes every struct field-by-field
// (endian-safe, pointer-free) while keeping the vanilla savegame layout of
// records. `save_p` is an Int cursor into `savebuffer` (owned by g_game.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class PSavegState {
    var save_p = 0

    val saveg_restored_action: (thinker_t) -> Unit by lazy(LazyThreadSafetyMode.NONE) { { } }
}
