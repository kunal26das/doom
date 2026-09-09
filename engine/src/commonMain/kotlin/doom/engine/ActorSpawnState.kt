// Port of linuxdoom-1.10 p_mobj.c -- moving object handling. Spawn functions.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class ActorSpawnState {
    var test = 0

    val itemrespawnque by lazy(LazyThreadSafetyMode.NONE) { Array(ITEMQUESIZE) { mapthing_t() } }

    val itemrespawntime by lazy(LazyThreadSafetyMode.NONE) { IntArray(ITEMQUESIZE) }

    var iquehead = 0

    var iquetail = 0
}
