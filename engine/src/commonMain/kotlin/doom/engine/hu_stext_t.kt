// Port of linuxdoom-1.10 hu_lib.c/hu_lib.h -- heads-up text and input code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Scrolling Text window widget
//  (child of Text Line widget)
internal class hu_stext_t {
    val l = Array(HU_MAXLINES) { hu_textline_t() }  // text lines to draw
    var h = 0                             // height in lines
    var cl = 0                            // current line number

    // pointer to boolean stating whether to update window
    // (C boolean* --> read-only accessor lambda)
    var on: () -> Boolean = { false }
    var laston = false                    // last value of *->on.
}
