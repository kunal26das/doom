package com.kunal26das.doom

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private fun docPath(name: String): String {
    val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .first() as String
    return "$dir/$name"
}

@OptIn(ExperimentalForeignApi::class)
actual fun platformReadFile(name: String): ByteArray? {
    val data = NSData.dataWithContentsOfFile(docPath(name)) ?: return null
    val len = data.length.toInt()
    if (len == 0) return ByteArray(0)
    val bytes = ByteArray(len)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
actual fun platformWriteFile(name: String, data: ByteArray) {
    val nsData = if (data.isEmpty()) {
        NSData()
    } else {
        data.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), data.size.toULong())
        }
    }
    nsData.writeToFile(docPath(name), true)
}
