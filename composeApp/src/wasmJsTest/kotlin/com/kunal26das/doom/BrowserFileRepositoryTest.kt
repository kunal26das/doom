package com.kunal26das.doom

import com.kunal26das.doom.data.GameFileRepository
import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection
import kotlinx.browser.localStorage
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BrowserFileRepositoryTest {
    private lateinit var keyPrefix: String

    @BeforeTest
    fun createStorageNamespace() {
        keyPrefix = "doom-storage-test:${Random.nextLong().toULong()}:${Random.nextLong().toULong()}:"
    }

    @AfterTest
    fun removeTestStorage() {
        for (index in localStorage.length - 1 downTo 0) {
            val key = localStorage.key(index) ?: continue
            if (key.startsWith(keyPrefix)) localStorage.removeItem(key)
        }
    }

    @Test
    fun missingSaveReturnsNull() {
        assertNull(BrowserFileRepository(keyPrefix).read("doomsav0.dsg"))
    }

    @Test
    fun everyByteValueSurvivesCreatingAnotherRepository() {
        val bytes = ByteArray(256) { it.toByte() }
        BrowserFileRepository(keyPrefix).write("doomsav0.dsg", bytes)

        assertContentEquals(bytes, BrowserFileRepository(keyPrefix).read("doomsav0.dsg"))
    }

    @Test
    fun realisticSaveSurvivesCreatingAnotherRepository() {
        val bytes = ByteArray(180_224) { (it * 31).toByte() }
        BrowserFileRepository(keyPrefix).write("doomsav0.dsg", bytes)

        assertContentEquals(bytes, BrowserFileRepository(keyPrefix).read("doomsav0.dsg"))
    }

    @Test
    fun shorterOverwriteDoesNotKeepPreviousTrailingBytes() {
        val files = BrowserFileRepository(keyPrefix)
        files.write("doomsav0.dsg", ByteArray(16_384) { 42 })
        val replacement = byteArrayOf(0, 1, -1, 127, -128)

        files.write("doomsav0.dsg", replacement)

        assertContentEquals(replacement, BrowserFileRepository(keyPrefix).read("doomsav0.dsg"))
    }

    @Test
    fun emptySaveRoundTripsAndReplacesAnExistingFile() {
        val files = BrowserFileRepository(keyPrefix)
        files.write("doomsav0.dsg", ByteArray(128) { 7 })

        files.write("doomsav0.dsg", byteArrayOf())

        assertContentEquals(byteArrayOf(), BrowserFileRepository(keyPrefix).read("doomsav0.dsg"))
        assertEquals("", localStorage.getItem(keyPrefix + "doomsav0.dsg"))
    }

    @Test
    fun updatingOneSlotPreservesOtherSlots() {
        val files = BrowserFileRepository(keyPrefix)
        files.write("doomsav0.dsg", byteArrayOf(1))
        files.write("doomsav1.dsg", byteArrayOf(2))

        files.write("doomsav0.dsg", byteArrayOf(3))

        val reopened = BrowserFileRepository(keyPrefix)
        assertContentEquals(byteArrayOf(3), reopened.read("doomsav0.dsg"))
        assertContentEquals(byteArrayOf(2), reopened.read("doomsav1.dsg"))
    }

    @Test
    fun gameNamespacesPersistIndependentlyAndFollowImportedContent() {
        val files = BrowserFileRepository(keyPrefix)
        val wad = minimalWad()
        val demo = GameFileRepository(files, BuiltInGameSelection)
        val imported = GameFileRepository(files, ImportedGameSelection("doom.wad", wad))
        demo.write("doomsav0.dsg", byteArrayOf(1))
        imported.write("doomsav0.dsg", byteArrayOf(2))

        val reopened = BrowserFileRepository(keyPrefix)
        val renamed = GameFileRepository(reopened, ImportedGameSelection("renamed.wad", wad))
        val different = GameFileRepository(reopened, ImportedGameSelection("doom.wad", wad + byteArrayOf(1)))

        assertContentEquals(byteArrayOf(1), GameFileRepository(reopened, BuiltInGameSelection).read("doomsav0.dsg"))
        assertContentEquals(byteArrayOf(2), renamed.read("doomsav0.dsg"))
        assertNull(different.read("doomsav0.dsg"))
    }

    @Test
    fun corruptEncodingReportsFailureWithoutChangingOtherSaves() {
        val corruptKey = keyPrefix + "doomsav0.dsg"
        localStorage.setItem(corruptKey, "!not-base64!")
        val files = BrowserFileRepository(keyPrefix)
        files.write("doomsav1.dsg", byteArrayOf(7))

        assertFailsWith<IllegalArgumentException> { files.read("doomsav0.dsg") }

        assertEquals("!not-base64!", localStorage.getItem(corruptKey))
        assertContentEquals(byteArrayOf(7), BrowserFileRepository(keyPrefix).read("doomsav1.dsg"))
    }

    private fun minimalWad(): ByteArray {
        val names = listOf("PLAYPAL", "COLORMAP", "TEXTURE1", "PNAMES", "M_DOOM", "E1M1")
        return ByteArray(12 + names.size * 16).apply {
            "IWAD".encodeToByteArray().copyInto(this)
            this[4] = names.size.toByte()
            this[8] = 12
            names.forEachIndexed { index, name ->
                val entry = 12 + index * 16
                this[entry] = 12
                name.encodeToByteArray().copyInto(this, entry + 8)
            }
        }
    }
}
