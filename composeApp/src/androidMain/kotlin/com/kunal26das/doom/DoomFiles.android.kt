package com.kunal26das.doom

import android.content.Context

lateinit var doomAppContext: Context

actual fun platformReadFile(name: String): ByteArray? {
    val f = doomAppContext.filesDir.resolve(name)
    return if (f.exists()) f.readBytes() else null
}

actual fun platformWriteFile(name: String, data: ByteArray) {
    doomAppContext.filesDir.resolve(name).writeBytes(data)
}
