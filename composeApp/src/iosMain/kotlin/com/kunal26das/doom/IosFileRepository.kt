package com.kunal26das.doom

import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSCocoaErrorDomain
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileReadNoSuchFileError
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    IosFileRepository()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosFileRepository(
    private val directory: String = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true,
    ).first() as String,
) : FileRepository {
    private fun docPath(name: String): String = "$directory/$name"

    override fun read(name: String): ByteArray? {
        val data = memScoped {
            val failure = alloc<ObjCObjectVar<NSError?>>()
            failure.value = null
            NSData.dataWithContentsOfFile(docPath(name), 0uL, failure.ptr) ?: run {
                val cause = failure.value
                if (cause != null && cause.domain == NSCocoaErrorDomain && cause.code == NSFileReadNoSuchFileError) {
                    return null
                }
                error("Could not read DOOM file: $name (${cause?.localizedDescription ?: "Unknown read error"})")
            }
        }
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
