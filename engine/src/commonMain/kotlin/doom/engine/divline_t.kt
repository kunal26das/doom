// Port of linuxdoom-1.10 p_maputl.c -- movement/collision utility functions,
// as used by function in p_map.c. BLOCKMAP Iterator functions, and some
// PIT_* functions to use for iteration. (Also owns divline_t/intercept_t
// and the traverser machinery from p_local.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


//
// P_MAPUTL types (from p_local.h)
//
internal class divline_t {
    var x: fixed_t = 0
    var y: fixed_t = 0
    var dx: fixed_t = 0
    var dy: fixed_t = 0
}
