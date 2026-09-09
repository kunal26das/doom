// Port of linuxdoom-1.10 st_lib.c/st_lib.h -- the status bar widget code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Percent widget ("child" of number widget,
//  or, more precisely, contains a number widget.)
internal class st_percent_t {
    // number information
    val n = st_number_t()

    // percent sign graphic
    var p: ByteArray = ByteArray(0)
}
