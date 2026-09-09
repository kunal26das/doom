// Port of linuxdoom-1.10 p_sight.c -- LineOfSight/Visibility checks,
// uses REJECT Lookup Table.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class PSightState {
    var sightzstart: fixed_t = 0

    var topslope: fixed_t = 0

    var bottomslope: fixed_t = 0

    val strace by lazy(LazyThreadSafetyMode.NONE) { divline_t() }

    var t2x: fixed_t = 0

    var t2y: fixed_t = 0

    val sightcounts by lazy(LazyThreadSafetyMode.NONE) { IntArray(2) }
}
