package doom.engine.resources

import doom.engine.DoomError
import doom.engine.core.DoomEngineCore

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class WadValidationTest {
    private val core = DoomEngineCore()
    @Test
    fun validArchivesKeepOverridesCaseInsensitiveNamesAndEmptyMarkers() = with(core) {
        wInitMultipleFiles(listOf(wad("test", byteArrayOf(1, 2)), wad("TEST", byteArrayOf(3))))
        assertEquals(1, wCheckNumForName("TeSt"))
        assertContentEquals(byteArrayOf(3), wCacheLumpName("test"))
        wInitMultipleFiles(listOf(wad("F_START", byteArrayOf())))
        assertEquals(0, wLumpLength(0))
        assertContentEquals(byteArrayOf(), wCacheLumpNum(0))
    }

    @Test
    fun invalidFilesCannotReplacePreviouslyLoadedArchiveOrCache() = with(core) {
        wInitMultipleFiles(listOf(wad("GOOD", byteArrayOf(7))))
        val original = lumpinfo
        val cached = wCacheLumpName("GOOD")
        for (invalid in invalidArchives()) {
            assertFailsWith<DoomError> { wInitMultipleFiles(listOf(wad("NEW", byteArrayOf(9)), invalid)) }
            assertSame(original, lumpinfo)
            assertSame(cached, wCacheLumpName("GOOD"))
            assertEquals(-1, wCheckNumForName("NEW"))
        }
        assertFailsWith<DoomError> { wInitMultipleFiles(emptyList()) }
        assertSame(original, lumpinfo)
    }

    @Test
    fun invalidLumpIndexesProduceEngineErrors() = with(core) {
        wInitMultipleFiles(listOf(wad("TEST", byteArrayOf(1))))
        for (index in listOf(-1, Int.MIN_VALUE, 1, Int.MAX_VALUE)) {
            assertFailsWith<DoomError> { wLumpLength(index) }
            assertFailsWith<DoomError> { wCacheLumpNum(index) }
        }
    }

    private fun invalidArchives(): List<ByteArray> = buildList {
        add(byteArrayOf())
        add("IWAD".encodeToByteArray())
        add(wad("X", byteArrayOf(1)).apply { this[0] = 'Z'.code.toByte() })
        add(wad("X", byteArrayOf(1)).apply { put32(4, -1) })
        add(wad("X", byteArrayOf(1)).apply { put32(4, Int.MAX_VALUE) })
        add(wad("X", byteArrayOf(1)).apply { put32(8, -1) })
        add(wad("X", byteArrayOf(1)).apply { put32(8, Int.MAX_VALUE) })
        add(wad("X", byteArrayOf(1)).copyOf(28))
        add(wad("X", byteArrayOf(1)).apply { put32(12, -1) })
        add(wad("X", byteArrayOf(1)).apply { put32(16, -1) })
        add(wad("X", byteArrayOf(1)).apply { put32(16, 2) })
        add(wad("X", byteArrayOf(1)).apply { put32(12, Int.MAX_VALUE); put32(16, Int.MAX_VALUE) })
    }

    private fun wad(name: String, data: ByteArray): ByteArray = ByteArray(28 + data.size).apply {
        "PWAD".encodeToByteArray().copyInto(this)
        put32(4, 1)
        put32(8, 12)
        put32(12, 28)
        put32(16, data.size)
        name.encodeToByteArray().copyInto(this, 20)
        data.copyInto(this, 28)
    }

    private fun ByteArray.put32(offset: Int, value: Int) {
        for (byte in 0..3) this[offset + byte] = (value ushr (byte * 8)).toByte()
    }
}
