// Port of linuxdoom-1.10 m_argv.c -- command line parameter handling.
// The host app can seed myargv (e.g. desktop args, URL query on web).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "ktlint")

package doom.engine

var myargv: List<String> = emptyList()
val myargc: Int get() = myargv.size

/**
 * Checks for the given parameter in the program's command line arguments.
 * Returns the argument number (1 to argc-1) or 0 if not present.
 */
fun M_CheckParm(check: String): Int {
    for (i in 1 until myargv.size) {
        if (check.equals(myargv[i], ignoreCase = true)) return i
    }
    return 0
}
