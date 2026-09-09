// Port of linuxdoom-1.10 f_finale.c -- game completion, final screen animation.
// (F_DrawPatchFlipped is the vanilla v_video.c V_DrawPatchFlipped, kept local
// here because the cast drawer is its only caller.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// Final DOOM 2 animation
// Casting by id Software.
//   in order of appearance
//
internal class castinfo_t(
    val name: String?,
    val type: Int, // mobjtype_t
)
