// GENERATED from linuxdoom-1.10 tables.c -- do not edit by hand.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("MagicNumber", "LargeClass", "LongMethod", "MaxLineLength", "ktlint")

package doom.engine

/** In C, finecosine is a pointer alias: &finesine[FINEANGLES/4]. */
internal object finecosine {
    operator fun get(i: Int): Int = finesine[i + 2048]
}
