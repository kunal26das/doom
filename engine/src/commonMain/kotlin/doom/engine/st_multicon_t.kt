// Port of linuxdoom-1.10 st_lib.c/st_lib.h -- the status bar widget code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Multiple Icon widget
internal class st_multicon_t {
    // center-justified location of icons
    var x = 0
    var y = 0

    // last icon number
    var oldinum = 0

    // pointer to current icon (C int* --> accessor lambda)
    var inum: () -> Int = { 0 }

    // pointer to boolean stating
    //  whether to update icon (C boolean* --> accessor lambda)
    var on: () -> Boolean = { false }

    // list of icons
    var p: Array<ByteArray> = emptyArray()

    // user data
    var data = 0
}
