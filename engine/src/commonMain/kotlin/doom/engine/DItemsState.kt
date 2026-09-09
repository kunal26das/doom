// Port of linuxdoom-1.10 d_items.c -- items: key cards, artifacts, weapon, ammunition.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class DItemsState {
    val weaponinfo: Array<weaponinfo_t> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        weaponinfo_t(
            // fist
            am_noammo,
            S_PUNCHUP,
            S_PUNCHDOWN,
            S_PUNCH,
            S_PUNCH1,
            S_NULL
        ),
        weaponinfo_t(
            // pistol
            am_clip,
            S_PISTOLUP,
            S_PISTOLDOWN,
            S_PISTOL,
            S_PISTOL1,
            S_PISTOLFLASH
        ),
        weaponinfo_t(
            // shotgun
            am_shell,
            S_SGUNUP,
            S_SGUNDOWN,
            S_SGUN,
            S_SGUN1,
            S_SGUNFLASH1
        ),
        weaponinfo_t(
            // chaingun
            am_clip,
            S_CHAINUP,
            S_CHAINDOWN,
            S_CHAIN,
            S_CHAIN1,
            S_CHAINFLASH1
        ),
        weaponinfo_t(
            // missile launcher
            am_misl,
            S_MISSILEUP,
            S_MISSILEDOWN,
            S_MISSILE,
            S_MISSILE1,
            S_MISSILEFLASH1
        ),
        weaponinfo_t(
            // plasma rifle
            am_cell,
            S_PLASMAUP,
            S_PLASMADOWN,
            S_PLASMA,
            S_PLASMA1,
            S_PLASMAFLASH1
        ),
        weaponinfo_t(
            // bfg 9000
            am_cell,
            S_BFGUP,
            S_BFGDOWN,
            S_BFG,
            S_BFG1,
            S_BFGFLASH1
        ),
        weaponinfo_t(
            // chainsaw
            am_noammo,
            S_SAWUP,
            S_SAWDOWN,
            S_SAW,
            S_SAW1,
            S_NULL
        ),
        weaponinfo_t(
            // super shotgun
            am_shell,
            S_DSGUNUP,
            S_DSGUNDOWN,
            S_DSGUN,
            S_DSGUN1,
            S_DSGUNFLASH1
        ),
    ) }
}
