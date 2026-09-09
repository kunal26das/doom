package com.kunal26das.doom

import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
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

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    IosFileRepository()

@OptIn(ExperimentalForeignApi::class)
private class IosFileRepository : FileRepository {
    private fun docPath(name: String): String {
        val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .first() as String
        return "$dir/$name"
    }

    override fun read(name: String): ByteArray? {
        val data = NSData.dataWithContentsOfFile(docPath(name)) ?: return null
        val len = data.length.toInt()
        if (len == 0) return ByteArray(0)
        val bytes = ByteArray(len)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes
    }

    override fun write(name: String, data: ByteArray) {
        val nsData = if (data.isEmpty()) {
            NSData()
        } else {
            data.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), data.size.toULong())
            }
        }
        check(nsData.writeToFile(docPath(name), true)) { "Could not write DOOM file: $name" }
    }
}
