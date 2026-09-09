
package doom.engine.configuration

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError

internal var DoomEngineCore.myargv: List<String>
    get() = startupArguments.values
    set(value) { startupArguments.replace(value) }
internal val DoomEngineCore.myargc: Int get() = startupArguments.size

internal fun DoomEngineCore.mCheckParm(check: String): Int = startupArguments.indexOf(check)

internal fun DoomEngineCore.mValidateArguments(commercialGame: Boolean) =
    startupArguments.validate(commercialGame, ::iError)
