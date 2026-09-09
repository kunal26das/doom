// Port of linuxdoom-1.10 m_argv.c -- command line parameter handling.
// The host app can seed myargv (e.g. desktop args, URL query on web).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "ktlint")

package doom.engine

internal var DoomEngineCore.myargv: List<String>
    get() = startupArguments.values
    set(value) { startupArguments.replace(value) }
internal val DoomEngineCore.myargc: Int get() = startupArguments.size

/** Return the option's position after the executable name, or zero when absent. */
internal fun DoomEngineCore.M_CheckParm(check: String): Int = startupArguments.indexOf(check)

/** Preserve the engine error boundary while argument policy stays independently testable. */
internal fun DoomEngineCore.M_ValidateArguments(commercialGame: Boolean) =
    startupArguments.validate(commercialGame, ::I_Error)
