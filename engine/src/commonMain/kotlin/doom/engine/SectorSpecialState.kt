// Port of linuxdoom-1.10 p_spec.c -- implements special effects:
// Texture animation, height or lighting changes according to adjacent
// sectors, respective utility functions, etc.
// Line Tag handling. Line and Sector triggers.
// (Also owns from p_spec.h: MO_TELEPORTMAN, button_t/bwhere_e/BUTTONTIME/
//  MAXBUTTONS + buttonlist, and the levelTimer globals.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class SectorSpecialState {
    val buttonlist by lazy(LazyThreadSafetyMode.NONE) { Array(MAXBUTTONS) { button_t() } }

    val animdefs by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        animdef_t(false, "NUKAGE3", "NUKAGE1", 8),
        animdef_t(false, "FWATER4", "FWATER1", 8),
        animdef_t(false, "SWATER4", "SWATER1", 8),
        animdef_t(false, "LAVA4", "LAVA1", 8),
        animdef_t(false, "BLOOD3", "BLOOD1", 8),

        // DOOM II flat animations.
        animdef_t(false, "RROCK08", "RROCK05", 8),
        animdef_t(false, "SLIME04", "SLIME01", 8),
        animdef_t(false, "SLIME08", "SLIME05", 8),
        animdef_t(false, "SLIME12", "SLIME09", 8),

        animdef_t(true, "BLODGR4", "BLODGR1", 8),
        animdef_t(true, "SLADRIP3", "SLADRIP1", 8),

        animdef_t(true, "BLODRIP4", "BLODRIP1", 8),
        animdef_t(true, "FIREWALL", "FIREWALA", 8),
        animdef_t(true, "GSTFONT3", "GSTFONT1", 8),
        animdef_t(true, "FIRELAVA", "FIRELAV3", 8),
        animdef_t(true, "FIREMAG3", "FIREMAG1", 8),
        animdef_t(true, "FIREBLU2", "FIREBLU1", 8),
        animdef_t(true, "ROCKRED3", "ROCKRED1", 8),

        animdef_t(true, "BFALL4", "BFALL1", 8),
        animdef_t(true, "SFALL4", "SFALL1", 8),
        animdef_t(true, "WFALL4", "WFALL1", 8),
        animdef_t(true, "DBRAIN4", "DBRAIN1", 8),

        animdef_t(-1),
    ) }

    val anims by lazy(LazyThreadSafetyMode.NONE) { Array(MAXANIMS) { anim_t() } }

    var lastanim = 0

    var levelTimer = false

    var levelTimeCount = 0

    var numlinespecials = 0

    val linespeciallist by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<line_t>(MAXLINEANIMS) }
}
