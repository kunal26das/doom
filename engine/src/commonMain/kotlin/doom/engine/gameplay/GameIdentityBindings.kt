
package doom.engine.gameplay

import doom.engine.core.DoomEngineCore

internal var DoomEngineCore.gamemode
    get() = stateGameIdentity.gamemode
    set(value) { stateGameIdentity.gamemode = value }
internal var DoomEngineCore.gamemission
    get() = stateGameIdentity.gamemission
    set(value) { stateGameIdentity.gamemission = value }

internal var DoomEngineCore.language
    get() = stateGameIdentity.language
    set(value) { stateGameIdentity.language = value }
