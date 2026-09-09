// Port of linuxdoom-1.10 p_inter.c -- handling interactions (i.e., collisions).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class PInterState {
    val maxammo by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(200, 50, 300, 50) }

    val clipammo by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(10, 4, 20, 1) }
}
