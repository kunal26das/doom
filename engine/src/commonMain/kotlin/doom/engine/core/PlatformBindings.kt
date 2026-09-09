
package doom.engine.core

import doom.engine.DoomError

internal fun DoomEngineCore.iGetTime(): Int = clock.ticks()
internal fun DoomEngineCore.iPauseTime() = clock.pause()
internal fun DoomEngineCore.iResumeTime() = clock.resume()

internal fun iError(error: String): Nothing {
    println("Error: $error")
    throw DoomError(error)
}

internal fun DoomEngineCore.iQuit() { quit.request() }

internal fun DoomEngineCore.mReadFile(name: String): ByteArray? = host.read(name)
internal fun DoomEngineCore.mWriteFile(name: String, data: ByteArray) = host.write(name, data)
