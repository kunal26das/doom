// KMP replacement for linuxdoom-1.10 i_system.c -- timing, errors, exit.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "ktlint")

package doom.engine

internal fun DoomEngineCore.I_GetTime(): Int = clock.ticks()
internal fun DoomEngineCore.I_PauseTime() = clock.pause()
internal fun DoomEngineCore.I_ResumeTime() = clock.resume()

// C i_system.c: I_Tactile -- UNUSED (no-op stub, kept because P_DamageMobj
// calls it for the local player exactly as vanilla does).
internal fun DoomEngineCore.I_Tactile(on: Int, off: Int, total: Int) {
}

internal fun I_Error(error: String): Nothing {
    println("Error: $error")
    throw DoomError(error)
}

internal fun DoomEngineCore.I_Quit() { quit.request() }

internal fun DoomEngineCore.M_ReadFile(name: String): ByteArray? = host.read(name)
internal fun DoomEngineCore.M_WriteFile(name: String, data: ByteArray) = host.write(name, data)
