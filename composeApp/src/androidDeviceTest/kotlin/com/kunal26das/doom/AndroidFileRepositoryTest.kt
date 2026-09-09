package com.kunal26das.doom

import android.content.Context
import android.util.AtomicFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.IOException
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidFileRepositoryTest {
    @Test
    fun binarySaveSurvivesRepositoryRecreationAndDoesNotAliasCallerBytes() = inStorage { context, directory ->
        val original = ByteArray(65_793) { it.toByte() }
        val input = original.copyOf()
        createPlatformFileRepository(context).write("doomsav0.dsg", input)
        input.fill(0)

        val recreated = createPlatformFileRepository(context)
        val loaded = requireNotNull(recreated.read("doomsav0.dsg"))
        assertContentEquals(original, loaded)
        loaded.fill(0)
        assertContentEquals(original, recreated.read("doomsav0.dsg"))
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
    }

    @Test
    fun overwriteTruncatesSaveAndEmptyFilesRemainDistinctFromMissingFiles() = inStorage { context, directory ->
        val files = createPlatformFileRepository(context)
        assertNull(files.read("missing.dsg"))
        files.write("doomsav0.dsg", ByteArray(4_096) { -1 })
        files.write("doomsav0.dsg", byteArrayOf(0, -128, 127))
        assertContentEquals(byteArrayOf(0, -128, 127), createPlatformFileRepository(context).read("doomsav0.dsg"))
        files.write("doomsav0.dsg", byteArrayOf())
        assertContentEquals(byteArrayOf(), createPlatformFileRepository(context).read("doomsav0.dsg"))
        assertTrue(File(directory, "doomsav0.dsg").isFile)
    }

    @Test
    fun invalidDestinationReportsFailureAndPreservesExistingDirectoryContents() = inStorage { context, directory ->
        val destination = File(directory, "doomsav0.dsg").apply { mkdirs() }
        val existing = File(destination, "keep.dat").apply { writeBytes(byteArrayOf(5, 6)) }
        val files = createPlatformFileRepository(context)

        assertFailsWith<IOException> { files.write("doomsav0.dsg", byteArrayOf(1, 2)) }

        assertContentEquals(byteArrayOf(5, 6), existing.readBytes())
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
        assertFailsWith<IOException> { files.read("doomsav0.dsg") }
    }

    @Test
    fun incompleteOverwriteIsRecoveredWithoutReplacingThePreviousSave() = inStorage { context, directory ->
        val original = byteArrayOf(3, 0, -128, 5)
        val files = createPlatformFileRepository(context)
        files.write("doomsav0.dsg", original)
        AtomicFile(File(directory, "doomsav0.dsg")).startWrite().use { output ->
            output.write(byteArrayOf(9, 9))
            output.fd.sync()
        }

        assertContentEquals(original, createPlatformFileRepository(context).read("doomsav0.dsg"))
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
    }

    @Test
    fun unreadableSaveReportsFailureInsteadOfAppearingMissing() = inStorage { context, directory ->
        val files = createPlatformFileRepository(context)
        files.write("doomsav0.dsg", byteArrayOf(3))
        val saved = File(directory, "doomsav0.dsg")
        assertTrue(saved.setReadable(false, false))
        try {
            assertFailsWith<IOException> { files.read("doomsav0.dsg") }
        } finally {
            saved.setReadable(true, true)
        }
    }

    @Test
    fun verificationReadFailureDoesNotRollBackAnAlreadyCommittedSave() = inStorage { context, directory ->
        val original = byteArrayOf(3, 0, -128, 5)
        val failure = IOException("Verification read failed")
        val files = AndroidFileRepository(context) { UnreadableCommittedAtomicFile(it, failure) }

        assertSame(failure, assertFailsWith<IOException> { files.write("doomsav0.dsg", original) })

        assertContentEquals(original, File(directory, "doomsav0.dsg").readBytes())
        assertEquals(listOf("doomsav0.dsg"), directory.list()?.toList())
    }

    private fun inStorage(block: (Context, File) -> Unit) {
        val application = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(application.cacheDir, "doom-storage-test-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            block(IsolatedStorageContext(application, directory), directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
