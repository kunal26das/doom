package com.kunal26das.doom

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.posix.chmod
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class IosFileRepositoryTest {
    private lateinit var directory: String

    @BeforeTest
    fun createStorageDirectory() {
        directory = "${NSTemporaryDirectory()}doom-storage-test-${NSUUID().UUIDString}"
        check(NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null))
    }

    @AfterTest
    fun removeStorageDirectory() {
        check(NSFileManager.defaultManager.removeItemAtPath(directory, null))
    }

    @Test
    fun missingSaveReturnsNull() {
        assertNull(IosFileRepository(directory).read("doomsav0.dsg"))
    }

    @Test
    fun directoryAtSavePathReportsReadFailure() {
        check(NSFileManager.defaultManager.createDirectoryAtPath("$directory/doomsav0.dsg", true, null, null))

        val failure = assertFailsWith<IllegalStateException> {
            IosFileRepository(directory).read("doomsav0.dsg")
        }

        assertTrue(failure.message.orEmpty().contains("Could not read DOOM file"))
    }

    @Test
    fun unreadableSaveReportsFailureAndKeepsItsBytes() {
        val files = IosFileRepository(directory)
        files.write("doomsav0.dsg", byteArrayOf(6))
        val path = "$directory/doomsav0.dsg"
        check(chmod(path, 0u) == 0)
        try {
            val failure = assertFailsWith<IllegalStateException> { files.read("doomsav0.dsg") }
            assertTrue(failure.message.orEmpty().contains("Could not read DOOM file"))
        } finally {
            check(chmod(path, 384u) == 0)
        }

        assertContentEquals(byteArrayOf(6), IosFileRepository(directory).read("doomsav0.dsg"))
    }

    @Test
    fun binarySaveSurvivesCreatingAnotherRepository() {
        val bytes = ByteArray(180_224) { (it * 31).toByte() }
        IosFileRepository(directory).write("doomsav0.dsg", bytes)

        assertContentEquals(bytes, IosFileRepository(directory).read("doomsav0.dsg"))
    }

    @Test
    fun shorterOverwriteDoesNotKeepPreviousTrailingBytes() {
        val files = IosFileRepository(directory)
        files.write("doomsav0.dsg", ByteArray(16_384) { 42 })
        val replacement = byteArrayOf(0, 1, -1, 127, -128)

        files.write("doomsav0.dsg", replacement)

        assertContentEquals(replacement, IosFileRepository(directory).read("doomsav0.dsg"))
    }

    @Test
    fun emptySaveRoundTripsAndTruncatesAnExistingFile() {
        val files = IosFileRepository(directory)
        files.write("doomsav0.dsg", ByteArray(128) { 7 })

        files.write("doomsav0.dsg", byteArrayOf())

        assertContentEquals(byteArrayOf(), IosFileRepository(directory).read("doomsav0.dsg"))
    }

    @Test
    fun updatingOneSlotPreservesOtherSlots() {
        val files = IosFileRepository(directory)
        files.write("doomsav0.dsg", byteArrayOf(1))
        files.write("doomsav1.dsg", byteArrayOf(2))

        files.write("doomsav0.dsg", byteArrayOf(3))

        val reopened = IosFileRepository(directory)
        assertContentEquals(byteArrayOf(3), reopened.read("doomsav0.dsg"))
        assertContentEquals(byteArrayOf(2), reopened.read("doomsav1.dsg"))
    }

    @Test
    fun failedWriteReportsFailureAndPreservesExistingSaves() {
        val files = IosFileRepository(directory)
        files.write("doomsav0.dsg", byteArrayOf(5))
        val inaccessible = IosFileRepository("$directory/missing-directory")

        val failure = assertFailsWith<IllegalStateException> {
            inaccessible.write("doomsav0.dsg", byteArrayOf(8))
        }

        assertTrue(failure.message.orEmpty().contains("Could not write DOOM file"))
        assertContentEquals(byteArrayOf(5), IosFileRepository(directory).read("doomsav0.dsg"))
    }
}
