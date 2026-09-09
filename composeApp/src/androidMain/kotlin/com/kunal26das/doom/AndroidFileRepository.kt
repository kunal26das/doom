package com.kunal26das.doom

import android.content.Context
import android.util.AtomicFile
import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    AndroidFileRepository(context.applicationContext)

internal class AndroidFileRepository(
    private val context: Context,
    private val atomicFile: (File) -> AtomicFile = ::AtomicFile,
) : FileRepository {
    override fun read(name: String): ByteArray? {
        val file = atomicFile(context.filesDir.resolve(name))
        return try {
            file.readFully()
        } catch (failure: FileNotFoundException) {
            if (file.baseFile.exists()) throw failure
            null
        }
    }

    override fun write(name: String, data: ByteArray) {
        val file = atomicFile(context.filesDir.resolve(name))
        if (file.baseFile.exists() && !file.baseFile.isFile) {
            throw IOException("Save destination is not a file: $name")
        }
        val output = file.startWrite()
        try {
            output.write(data)
            output.fd.sync()
            file.finishWrite(output)
        } catch (failure: Throwable) {
            file.failWrite(output)
            throw failure
        }
        if (!file.readFully().contentEquals(data)) {
            throw IOException("Could not verify saved file: $name")
        }
    }
}
