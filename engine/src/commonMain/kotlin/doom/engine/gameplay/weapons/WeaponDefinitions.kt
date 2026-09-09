
package doom.engine.gameplay.weapons

import doom.engine.core.DoomEngineCore

internal val DoomEngineCore.weaponinfo: Array<WeaponDefinition>
    get() = stateDItems.weaponinfo
