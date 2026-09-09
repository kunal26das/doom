// Port of linuxdoom-1.10 p_maputl.c -- movement/collision utility functions,
// as used by function in p_map.c. BLOCKMAP Iterator functions, and some
// PIT_* functions to use for iteration. (Also owns divline_t/intercept_t
// and the traverser machinery from p_local.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class TraversalState {
    var opentop: fixed_t = 0

    var openbottom: fixed_t = 0

    var openrange: fixed_t = 0

    var lowfloor: fixed_t = 0

    val intercepts by lazy(LazyThreadSafetyMode.NONE) { Array(MAXINTERCEPTS) { intercept_t() } }

    var intercept_p = 0

    val trace by lazy(LazyThreadSafetyMode.NONE) { divline_t() }

    var earlyout = false

    var ptflags = 0
}
