package com.kunal26das.doom

import java.io.File

private val dir: File by lazy {
    File(System.getProperty("user.home"), ".doom-kmp").apply { mkdirs() }
}

actual fun platformReadFile(name: String): ByteArray? {
    val f = File(dir, name)
    return if (f.exists()) f.readBytes() else null
}

actual fun platformWriteFile(name: String, data: ByteArray) {
    File(dir, name).writeBytes(data)
}
