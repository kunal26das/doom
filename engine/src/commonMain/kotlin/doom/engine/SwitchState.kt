// Port of linuxdoom-1.10 p_switch.c -- switches, buttons. Two-state animation. Exits.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class SwitchState {
    val alphSwitchList by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        // Doom shareware episode 1 switches
        switchlist_t("SW1BRCOM", "SW2BRCOM", 1),
        switchlist_t("SW1BRN1", "SW2BRN1", 1),
        switchlist_t("SW1BRN2", "SW2BRN2", 1),
        switchlist_t("SW1BRNGN", "SW2BRNGN", 1),
        switchlist_t("SW1BROWN", "SW2BROWN", 1),
        switchlist_t("SW1COMM", "SW2COMM", 1),
        switchlist_t("SW1COMP", "SW2COMP", 1),
        switchlist_t("SW1DIRT", "SW2DIRT", 1),
        switchlist_t("SW1EXIT", "SW2EXIT", 1),
        switchlist_t("SW1GRAY", "SW2GRAY", 1),
        switchlist_t("SW1GRAY1", "SW2GRAY1", 1),
        switchlist_t("SW1METAL", "SW2METAL", 1),
        switchlist_t("SW1PIPE", "SW2PIPE", 1),
        switchlist_t("SW1SLAD", "SW2SLAD", 1),
        switchlist_t("SW1STARG", "SW2STARG", 1),
        switchlist_t("SW1STON1", "SW2STON1", 1),
        switchlist_t("SW1STON2", "SW2STON2", 1),
        switchlist_t("SW1STONE", "SW2STONE", 1),
        switchlist_t("SW1STRTN", "SW2STRTN", 1),

        // Doom registered episodes 2&3 switches
        switchlist_t("SW1BLUE", "SW2BLUE", 2),
        switchlist_t("SW1CMT", "SW2CMT", 2),
        switchlist_t("SW1GARG", "SW2GARG", 2),
        switchlist_t("SW1GSTON", "SW2GSTON", 2),
        switchlist_t("SW1HOT", "SW2HOT", 2),
        switchlist_t("SW1LION", "SW2LION", 2),
        switchlist_t("SW1SATYR", "SW2SATYR", 2),
        switchlist_t("SW1SKIN", "SW2SKIN", 2),
        switchlist_t("SW1VINE", "SW2VINE", 2),
        switchlist_t("SW1WOOD", "SW2WOOD", 2),

        // Doom II switches
        switchlist_t("SW1PANEL", "SW2PANEL", 3),
        switchlist_t("SW1ROCK", "SW2ROCK", 3),
        switchlist_t("SW1MET2", "SW2MET2", 3),
        switchlist_t("SW1WDMET", "SW2WDMET", 3),
        switchlist_t("SW1BRIK", "SW2BRIK", 3),
        switchlist_t("SW1MOD1", "SW2MOD1", 3),
        switchlist_t("SW1ZIM", "SW2ZIM", 3),
        switchlist_t("SW1STON6", "SW2STON6", 3),
        switchlist_t("SW1TEK", "SW2TEK", 3),
        switchlist_t("SW1MARB", "SW2MARB", 3),
        switchlist_t("SW1SKULL", "SW2SKULL", 3),

        switchlist_t("\u0000", "\u0000", 0),
    ) }

    val switchlist by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXSWITCHES * 2) }

    var numswitches = 0
}
