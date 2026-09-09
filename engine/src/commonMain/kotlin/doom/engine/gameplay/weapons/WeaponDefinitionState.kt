
package doom.engine.gameplay.weapons

import doom.engine.gameplay.actors.S_BFG
import doom.engine.gameplay.actors.S_BFG1
import doom.engine.gameplay.actors.S_BFGDOWN
import doom.engine.gameplay.actors.S_BFGFLASH1
import doom.engine.gameplay.actors.S_BFGUP
import doom.engine.gameplay.actors.S_CHAIN
import doom.engine.gameplay.actors.S_CHAIN1
import doom.engine.gameplay.actors.S_CHAINDOWN
import doom.engine.gameplay.actors.S_CHAINFLASH1
import doom.engine.gameplay.actors.S_CHAINUP
import doom.engine.gameplay.actors.S_DSGUN
import doom.engine.gameplay.actors.S_DSGUN1
import doom.engine.gameplay.actors.S_DSGUNDOWN
import doom.engine.gameplay.actors.S_DSGUNFLASH1
import doom.engine.gameplay.actors.S_DSGUNUP
import doom.engine.gameplay.actors.S_MISSILE
import doom.engine.gameplay.actors.S_MISSILE1
import doom.engine.gameplay.actors.S_MISSILEDOWN
import doom.engine.gameplay.actors.S_MISSILEFLASH1
import doom.engine.gameplay.actors.S_MISSILEUP
import doom.engine.gameplay.actors.S_NULL
import doom.engine.gameplay.actors.S_PISTOL
import doom.engine.gameplay.actors.S_PISTOL1
import doom.engine.gameplay.actors.S_PISTOLDOWN
import doom.engine.gameplay.actors.S_PISTOLFLASH
import doom.engine.gameplay.actors.S_PISTOLUP
import doom.engine.gameplay.actors.S_PLASMA
import doom.engine.gameplay.actors.S_PLASMA1
import doom.engine.gameplay.actors.S_PLASMADOWN
import doom.engine.gameplay.actors.S_PLASMAFLASH1
import doom.engine.gameplay.actors.S_PLASMAUP
import doom.engine.gameplay.actors.S_PUNCH
import doom.engine.gameplay.actors.S_PUNCH1
import doom.engine.gameplay.actors.S_PUNCHDOWN
import doom.engine.gameplay.actors.S_PUNCHUP
import doom.engine.gameplay.actors.S_SAW
import doom.engine.gameplay.actors.S_SAW1
import doom.engine.gameplay.actors.S_SAWDOWN
import doom.engine.gameplay.actors.S_SAWUP
import doom.engine.gameplay.actors.S_SGUN
import doom.engine.gameplay.actors.S_SGUN1
import doom.engine.gameplay.actors.S_SGUNDOWN
import doom.engine.gameplay.actors.S_SGUNFLASH1
import doom.engine.gameplay.actors.S_SGUNUP
import doom.engine.gameplay.AM_CELL
import doom.engine.gameplay.AM_CLIP
import doom.engine.gameplay.AM_MISL
import doom.engine.gameplay.AM_NOAMMO
import doom.engine.gameplay.AM_SHELL

internal class WeaponDefinitionState {
    val weaponinfo: Array<WeaponDefinition> by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        WeaponDefinition(
            AM_NOAMMO,
            S_PUNCHUP,
            S_PUNCHDOWN,
            S_PUNCH,
            S_PUNCH1,
            S_NULL
        ),
        WeaponDefinition(
            AM_CLIP,
            S_PISTOLUP,
            S_PISTOLDOWN,
            S_PISTOL,
            S_PISTOL1,
            S_PISTOLFLASH
        ),
        WeaponDefinition(
            AM_SHELL,
            S_SGUNUP,
            S_SGUNDOWN,
            S_SGUN,
            S_SGUN1,
            S_SGUNFLASH1
        ),
        WeaponDefinition(
            AM_CLIP,
            S_CHAINUP,
            S_CHAINDOWN,
            S_CHAIN,
            S_CHAIN1,
            S_CHAINFLASH1
        ),
        WeaponDefinition(
            AM_MISL,
            S_MISSILEUP,
            S_MISSILEDOWN,
            S_MISSILE,
            S_MISSILE1,
            S_MISSILEFLASH1
        ),
        WeaponDefinition(
            AM_CELL,
            S_PLASMAUP,
            S_PLASMADOWN,
            S_PLASMA,
            S_PLASMA1,
            S_PLASMAFLASH1
        ),
        WeaponDefinition(
            AM_CELL,
            S_BFGUP,
            S_BFGDOWN,
            S_BFG,
            S_BFG1,
            S_BFGFLASH1
        ),
        WeaponDefinition(
            AM_NOAMMO,
            S_SAWUP,
            S_SAWDOWN,
            S_SAW,
            S_SAW1,
            S_NULL
        ),
        WeaponDefinition(
            AM_SHELL,
            S_DSGUNUP,
            S_DSGUNDOWN,
            S_DSGUN,
            S_DSGUN1,
            S_DSGUNFLASH1
        ),
    ) }
}
