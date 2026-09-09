// Port of linuxdoom-1.10 st_lib.c/st_lib.h -- the status bar widget code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Binary Icon widget
internal class st_binicon_t {
    // center-justified location of icon
    var x = 0
    var y = 0

    // last icon value (C int oldval = 0)
    var oldval = false

    // pointer to current icon status (C boolean* --> accessor lambda)
    var `val`: () -> Boolean = { false }

    // pointer to boolean
    //  stating whether to update icon (C boolean* --> accessor lambda)
    var on: () -> Boolean = { false }

    var p: ByteArray = ByteArray(0)  // icon
    var data = 0                     // user data
}
