package doom.engine

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
