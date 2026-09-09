// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal class state_t(
    val sprite: Int,       // spritenum_t
    val frame: Int,        // may include FF_FULLBRIGHT
    var tics: Int,         // mutated by -fast (G_InitNew)
    val actionName: String?,
    val nextstate: Int,    // statenum_t
    val misc1: Int = 0,
    val misc2: Int = 0,
) {
    var action: ActionF? = null  // resolved from actionName by InfoResolveActions
    var index: Int = 0           // == statenum; C computes (st - states)
}
