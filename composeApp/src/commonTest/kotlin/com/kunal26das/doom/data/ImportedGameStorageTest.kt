package com.kunal26das.doom.data

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.GameLaunchSelection
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ImportedGameStorageTest {
    @Test
    fun freshRepositoryRestoresTheLastSavedGameAndClearRemovesItsBytes() = runTest {
        val disk = ImportedGameMemoryFiles()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FileImportedGameRepository(disk, dispatcher)
        assertNull(repository.load())

        val original = ImportedGameSelection("/private/games/DOOM.WAD", wad("E1M1"))
        repository.save(original)
        val restored = assertNotNull(FileImportedGameRepository(disk, dispatcher).load())
        assertEquals("DOOM.WAD", restored.name)
        assertContentEquals(original.bytes, restored.bytes)

        repository.clear()
        assertNull(repository.load())
        assertEquals(0, disk.record!!.size)
    }

    @Test
    fun replacementStoresNameAndGameTogether() = runTest {
        val disk = ImportedGameMemoryFiles()
        val repository = FileImportedGameRepository(disk, StandardTestDispatcher(testScheduler))
        repository.save(ImportedGameSelection("DOOM.WAD", wad("E1M1")))
        val replacement = ImportedGameSelection("DOOM II 🕹.WAD", wad("MAP01"))
        repository.save(replacement)
        val loaded = assertNotNull(repository.load())
        assertEquals(replacement.name, loaded.name)
        assertContentEquals(replacement.bytes, loaded.bytes)
    }

    @Test
    fun damagedRecordOrWadCannotBecomeAnAutomaticLaunch() = runTest {
        val disk = ImportedGameMemoryFiles()
        val repository = FileImportedGameRepository(disk, StandardTestDispatcher(testScheduler))
        val good = StoredImportedGame.encode(ImportedGameSelection("DOOM.WAD", wad("E1M1")))
        val badNameLength = good.copyOf().also { it[9] = 0x7f }
        val badWad = good.copyOf().also { it[18] = 'P'.code.toByte() }
        for (damaged in listOf(byteArrayOf(1, 2), good.copyOf(12), badNameLength, badWad)) {
            disk.record = damaged
            assertFailsWith<IllegalArgumentException> { repository.load() }
        }
    }

    @Test
    fun storageFailureIsReportedAndDoesNotReplaceTheLastCompleteGame() = runTest {
        val disk = ImportedGameMemoryFiles()
        val repository = FileImportedGameRepository(disk, StandardTestDispatcher(testScheduler))
        repository.save(ImportedGameSelection("DOOM.WAD", wad("E1M1")))
        disk.rejectWrites = true
        assertFailsWith<IllegalStateException> {
            repository.save(ImportedGameSelection("DOOM2.WAD", wad("MAP01")))
        }
        assertEquals("DOOM.WAD", repository.load()!!.name)
    }

    private fun wad(map: String): ByteArray {
        val names = listOf("PLAYPAL", "COLORMAP", "TEXTURE1", "PNAMES", "M_DOOM", map)
        return ByteArray(12 + names.size * 16).also { bytes ->
            "IWAD".encodeToByteArray().copyInto(bytes)
            bytes[4] = names.size.toByte()
            bytes[8] = 12
            names.forEachIndexed { index, name -> name.encodeToByteArray().copyInto(bytes, 12 + index * 16 + 8) }
        }
    }
}
