// Port of linuxdoom-1.10 hu_lib.c/hu_lib.h -- heads-up text and input code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Input Text Line widget
//  (child of Text Line widget)
internal class hu_itext_t {
    val l = hu_textline_t()               // text line to input on

    // left margin past which I am not to delete characters
    var lm = 0

    // pointer to boolean stating whether to update window
    // (C boolean* --> read-only accessor lambda)
    var on: () -> Boolean = { false }
    var laston = false                    // last value of *->on;
}
