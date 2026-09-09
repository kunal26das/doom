// Port of linuxdoom-1.10 r_things.c -- refresh of things, i.e. objects
// represented by sprites.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine


internal class maskdraw_t {
    var x1 = 0
    var x2 = 0

    var column = 0
    var topclip = 0
    var bottomclip = 0
}
