package doom.engine.resources

import doom.engine.DoomError
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class WadArchiveTest {
    @Test
    fun archivesShareReadOnlyImagesButOwnTheirDirectoriesAndCaches() {
        val image = wad("SHARED", byteArrayOf(4, 5))
        val first = WadArchive().apply { load(listOf(image)) }
        val second = WadArchive().apply { load(listOf(image)) }
        val firstBytes = first.read("SHARED")
        val secondBytes = second.read("SHARED")

        assertNotSame(firstBytes, secondBytes)
        assertSame(firstBytes, first.read(0))
        assertSame(secondBytes, second.read(0))

        first.load(listOf(wad("NEXT", byteArrayOf(8))))

        assertEquals(-1, first.find("SHARED"))
        assertContentEquals(byteArrayOf(8), first.read("NEXT"))
        assertEquals(0, second.find("SHARED"))
        assertEquals(-1, second.find("NEXT"))
        assertContentEquals(byteArrayOf(4, 5), second.read(0))
        assertSame(secondBytes, second.read(0))
    }

    @Test
    fun reloadingReplacesCacheAndLeavesPublishedMetadataUnchanged() {
        val archive = WadArchive()
        archive.load(listOf(wad("ORIGINAL", byteArrayOf(1))))
        val originalDirectory = archive.directory
        val originalBytes = archive.read(0)

        archive.load(listOf(wad("REPLACED", byteArrayOf(2, 3))))

        assertEquals(listOf(LumpInfo("ORIGINAL", 1)), originalDirectory)
        assertEquals(listOf(LumpInfo("REPLACED", 2)), archive.directory)
        assertNotSame(originalDirectory, archive.directory)
        assertNotSame(originalBytes, archive.read(0))
        assertContentEquals(byteArrayOf(2, 3), archive.read(0))
        assertEquals(2, archive.length(0))
    }

    @Test
    fun indexedReadsKeepOverriddenAndEmptyLumpsIndependent() {
        val archive = WadArchive().apply {
            load(listOf(wad("SHARED", byteArrayOf(1)), wad("SHARED", byteArrayOf(2)), wad("EMPTY", byteArrayOf())))
        }
        val original = archive.read(0)
        val overridden = archive.read(1)
        val empty = archive.read(2)

        assertContentEquals(byteArrayOf(1), original)
        assertContentEquals(byteArrayOf(2), overridden)
        assertContentEquals(byteArrayOf(), empty)
        assertSame(original, archive.read(0))
        assertSame(overridden, archive.read("SHARED"))
        assertSame(empty, archive.read("EMPTY"))

        archive.load(listOf(wad("NEXT", byteArrayOf(3))))

        assertEquals(1, archive.size)
        assertContentEquals(byteArrayOf(3), archive.read(0))
        assertEquals("W_CacheLumpNum: invalid lump 1 (count 1)", assertFailsWith<DoomError> { archive.read(1) }.message)
        assertEquals("W_CacheLumpNum: invalid lump -1 (count 1)", assertFailsWith<DoomError> { archive.read(-1) }.message)
    }

    @Test
    fun failedReloadPreservesPublishedDirectoryAndCachedBytes() {
        val archive = WadArchive().apply { load(listOf(wad("ORIGINAL", byteArrayOf(4, 5)))) }
        val directory = archive.directory
        val cached = archive.read(0)

        assertEquals(
            "WAD header is truncated",
            assertFailsWith<DoomError> {
                archive.load(listOf(wad("PARTIAL", byteArrayOf(9)), ByteArray(11)))
            }.message,
        )

        assertSame(directory, archive.directory)
        assertEquals(1, archive.size)
        assertEquals(-1, archive.find("PARTIAL"))
        assertEquals(0, archive.find("ORIGINAL"))
        assertSame(cached, archive.read(0))
        assertContentEquals(byteArrayOf(4, 5), archive.read("ORIGINAL"))

        assertFailsWith<DoomError> { archive.load(emptyList()) }

        assertSame(directory, archive.directory)
        assertSame(cached, archive.read("ORIGINAL"))
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
        repeat(4) { this[offset + it] = (value ushr (it * 8)).toByte() }
    }
}
