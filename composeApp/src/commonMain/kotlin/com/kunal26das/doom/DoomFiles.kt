package com.kunal26das.doom

import doom.engine.I_ReadFileHook
import doom.engine.I_WriteFileHook

/** Platform storage for config + savegames (name -> bytes). */
expect fun platformReadFile(name: String): ByteArray?
expect fun platformWriteFile(name: String, data: ByteArray)

/** Hook the engine's persistence entry points to platform storage. */
fun wireEnginePersistence() {
    I_ReadFileHook = { name -> platformReadFile(name) }
    I_WriteFileHook = { name, data -> platformWriteFile(name, data) }
}
