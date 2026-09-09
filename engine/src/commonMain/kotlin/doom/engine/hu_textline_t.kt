// Port of linuxdoom-1.10 hu_lib.c/hu_lib.h -- heads-up text and input code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// Text Line widget
//  (parent of Scrolling Text and Input Text widgets)
internal class hu_textline_t {
    // left-justified position of scrolling text window
    var x = 0
    var y = 0

    var f: Array<ByteArray>? = null       // font (patch_t**; null == C NULL)
    var sc = 0                            // start character
    val l = CharArray(HU_MAXLINELENGTH + 1)  // line of text
    var len = 0                           // current line length

    // whether this line needs to be udpated
    var needsupdate = 0
}
