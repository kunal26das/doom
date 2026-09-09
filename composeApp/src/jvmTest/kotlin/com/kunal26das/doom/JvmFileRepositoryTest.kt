package com.kunal26das.doom

import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmFileRepositoryTest {
    @Test
    fun binarySaveSurvivesRepositoryRecreationAndDoesNotAliasInputOrOutput() = inStorage { directory ->
        val original = ByteArray(65_793) { it.toByte() }
        val input = original.copyOf()
        JvmFileRepository(directory).write("doomsav0.dsg", input)
        input.fill(0)

        val recreated = JvmFileRepository(directory)
        val loaded = requireNotNull(recreated.read("doomsav0.dsg"))
        assertContentEquals(original, loaded)
        loaded.fill(0)
        assertContentEquals(original, recreated.read("doomsav0.dsg"))
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
    }

    @Test
    fun overwriteTruncatesPreviousSaveAndEmptyFilesRemainDistinctFromMissingFiles() = inStorage { directory ->
        val files = JvmFileRepository(directory)
        assertNull(files.read("missing.dsg"))
        files.write("doomsav0.dsg", ByteArray(4_096) { -1 })
        files.write("doomsav0.dsg", byteArrayOf(0, -128, 127))
        assertContentEquals(byteArrayOf(0, -128, 127), JvmFileRepository(directory).read("doomsav0.dsg"))
        files.write("doomsav0.dsg", byteArrayOf())
        assertContentEquals(byteArrayOf(), JvmFileRepository(directory).read("doomsav0.dsg"))
        assertTrue(File(directory, "doomsav0.dsg").isFile)
    }

    @Test
    fun invalidDestinationPropagatesFailureAndCleansTemporaryFiles() = inStorage { directory ->
        val destination = File(directory, "doomsav0.dsg").apply { mkdirs() }
        val existing = File(destination, "keep.dat").apply { writeBytes(byteArrayOf(5, 6)) }
        val files = JvmFileRepository(directory)

        assertFailsWith<IOException> { files.write("doomsav0.dsg", byteArrayOf(1, 2)) }

        assertContentEquals(byteArrayOf(5, 6), existing.readBytes())
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
        assertFailsWith<IOException> { files.read("doomsav0.dsg") }
    }

    @Test
    fun blockedStorageDirectoryPropagatesFailureWithoutReplacingIt() = inStorage { directory ->
        val blocked = File(directory, "blocked").apply { writeBytes(byteArrayOf(9)) }
        val files = JvmFileRepository(blocked)

        assertFailsWith<IllegalStateException> { files.write("doomsav0.dsg", byteArrayOf(1)) }
        assertFailsWith<IllegalStateException> { files.read("doomsav0.dsg") }
        assertContentEquals(byteArrayOf(9), blocked.readBytes())
    }

    @Test
    fun constructionDoesNotCreateStorageBeforeFirstAccess() = inStorage { directory ->
        val storage = File(directory, "new-storage")
        val files = JvmFileRepository(storage)
        assertFalse(storage.exists())
        files.write("default.cfg", byteArrayOf(3))
        assertContentEquals(byteArrayOf(3), JvmFileRepository(storage).read("default.cfg"))
    }

    private fun inStorage(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("doom-jvm-storage-test-").toFile()
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
