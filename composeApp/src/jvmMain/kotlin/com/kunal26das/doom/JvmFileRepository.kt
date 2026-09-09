package com.kunal26das.doom

import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    JvmFileRepository()

internal class JvmFileRepository(
    private val directory: File = File(System.getProperty("user.home"), ".doom-kmp"),
) : FileRepository {
    private val dir: File by lazy {
        directory.apply {
            check(isDirectory || mkdirs()) { "Could not create DOOM storage directory" }
        }
    }

    override fun read(name: String): ByteArray? {
        val file = File(dir, name)
        return if (file.exists()) file.readBytes() else null
    }

    override fun write(name: String, data: ByteArray) {
        val target = File(dir, name).toPath()
        val temporary = Files.createTempFile(target.parent, ".doom-", ".tmp")
        try {
            FileOutputStream(temporary.toFile()).use { output ->
                output.write(data)
                output.fd.sync()
            }
            try {
                Files.move(temporary, target, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, target, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
