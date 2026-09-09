// Port of linuxdoom-1.10 m_menu.c -- DOOM selection menu, options, episode etc.
// Sliders and icons. Kinda widget stuff.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


internal class menu_t(
    var numitems: Int,              // # of menu items
    var prevMenu: menu_t?,          // previous menu
    var menuitems: Array<menuitem_t>, // menu items
    var routine: (() -> Unit)?,     // draw routine
    var x: Int,
    var y: Int,                     // x,y of menu
    var lastOn: Int,                // last item user was on in menu
)
