// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/**
 * State action. Vanilla's actionf_t union: either an (mobj_t*) action used by
 * map objects, or a (player_t*, pspdef_t*) action used by player weapon sprites.
 */
internal class ActionF(
    val name: String,
    val mobjFun: ((mobj_t) -> Unit)? = null,
    val pspFun: ((player_t, pspdef_t) -> Unit)? = null,
)
