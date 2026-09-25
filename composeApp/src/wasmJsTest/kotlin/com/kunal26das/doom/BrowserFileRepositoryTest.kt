package com.kunal26das.doom

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
    fun corruptEncodingReportsFailureWithoutChangingOtherSaves() {
        val corruptKey = keyPrefix + "doomsav0.dsg"
        localStorage.setItem(corruptKey, "!not-base64!")
        val files = BrowserFileRepository(keyPrefix)
        files.write("doomsav1.dsg", byteArrayOf(7))

        assertFailsWith<IllegalArgumentException> { files.read("doomsav0.dsg") }

        assertEquals("!not-base64!", localStorage.getItem(corruptKey))
        assertContentEquals(byteArrayOf(7), BrowserFileRepository(keyPrefix).read("doomsav1.dsg"))
    }
}
