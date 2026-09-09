package com.kunal26das.doom

import android.content.Context
import android.util.AtomicFile
import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
import java.io.FileNotFoundException

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    AndroidFileRepository(context.applicationContext)

private class AndroidFileRepository(private val context: Context) : FileRepository {
    override fun read(name: String): ByteArray? = try {
        // AtomicFile restores the last complete version after an interrupted write.
        AtomicFile(context.filesDir.resolve(name)).readFully()
    } catch (_: FileNotFoundException) {
        null
    }

    override fun write(name: String, data: ByteArray) {
        val file = AtomicFile(context.filesDir.resolve(name))
        val output = file.startWrite()
        try {
            output.write(data)
            file.finishWrite(output)
        } catch (failure: Throwable) {
            file.failWrite(output)
            throw failure
        }
    }
}
