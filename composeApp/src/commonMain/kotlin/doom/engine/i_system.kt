// KMP replacement for linuxdoom-1.10 i_system.c -- timing, errors, exit.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "ktlint")

package doom.engine

import kotlin.time.TimeSource

class DoomError(message: String) : Exception(message)

private val timeSource = TimeSource.Monotonic
private val bootMark = timeSource.markNow()

/** returns time in 1/70th second tics -- vanilla comment; actually TICRATE (35) per second. */
fun I_GetTime(): Int {
    val ms = bootMark.elapsedNow().inWholeMilliseconds
    return ((ms * TICRATE) / 1000).toInt()
}

fun I_Error(error: String): Nothing {
    println("Error: $error")
    throw DoomError(error)
}

var I_QuitHook: (() -> Unit)? = null

fun I_Quit() {
    // D_QuitNetGame / M_SaveDefaults / I_ShutdownSound happen in the caller's
    // shutdown path; the host app decides what "exit" means on each platform.
    I_QuitHook?.invoke()
}

// Host-provided persistence (config + savegames). Wired by the platform app
// before D_DoomMain: JVM/Android/iOS -> files, wasm -> localStorage.
var I_ReadFileHook: ((name: String) -> ByteArray?)? = null
var I_WriteFileHook: ((name: String, data: ByteArray) -> Unit)? = null

fun M_ReadFile(name: String): ByteArray? = I_ReadFileHook?.invoke(name)
fun M_WriteFile(name: String, data: ByteArray) {
    I_WriteFileHook?.invoke(name, data)
}
