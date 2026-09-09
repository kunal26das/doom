// Port of linuxdoom-1.10 m_menu.c -- DOOM selection menu, options, episode etc.
// Sliders and icons. Kinda widget stuff.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


//
// MENU TYPEDEFS
//
internal class menuitem_t(
    // 0 = no cursor here, 1 = ok, 2 = arrows ok
    var status: Int,
    var name: String,
    // choice = menu item #.
    // if status = 2,
    //   choice=0:leftarrow,1:rightarrow
    var routine: ((Int) -> Unit)?,
    // hotkey in menu
    var alphaKey: Int = 0,
)
