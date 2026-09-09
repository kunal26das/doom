package com.kunal26das.doom.data

import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.GameLaunchSelection
import java.io.File
import kotlin.test.*

class GameFileRepositoryTest {
    @Test
    fun savesAreIsolatedByGameAndImportedContent() {
        val files = GameFileRepositoryMemoryFiles()
        val shareware = GameFileRepository(files, BuiltInGameSelection)
        val wad = File(checkNotNull(System.getProperty("doom.test.wad"))).readBytes()
        val imported = GameFileRepository(files, ImportedGameSelection("doom.wad", wad))
        val renamed = GameFileRepository(files, ImportedGameSelection("renamed.wad", wad))
        val different = GameFileRepository(files, ImportedGameSelection("doom.wad", wad + byteArrayOf(0)))
        shareware.write("doomsav0.dsg", byteArrayOf(1))
        imported.write("doomsav0.dsg", byteArrayOf(3))
        assertContentEquals(byteArrayOf(1), shareware.read("doomsav0.dsg"))
        assertContentEquals(byteArrayOf(1), files.read("doomsav0.dsg"), "Existing shareware saves must still work")
        assertContentEquals(byteArrayOf(3), renamed.read("doomsav0.dsg"))
        assertNull(different.read("doomsav0.dsg"))
    }

}
