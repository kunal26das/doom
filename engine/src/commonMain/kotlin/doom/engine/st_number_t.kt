// Port of linuxdoom-1.10 st_lib.c/st_lib.h -- the status bar widget code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Number widget
internal class st_number_t {
    // upper right-hand corner
    //  of the number (right-justified)
    var x = 0
    var y = 0

    // max # of digits in number
    var width = 0

    // last number value
    var oldnum = 0

    // pointer to current value (C int* --> accessor lambda)
    var num: () -> Int = { 0 }

    // pointer to boolean stating
    //  whether to update number (C boolean* --> accessor lambda)
    var on: () -> Boolean = { false }

    // list of patches for 0-9
    var p: Array<ByteArray> = emptyArray()

    // user data
    var data = 0
}
