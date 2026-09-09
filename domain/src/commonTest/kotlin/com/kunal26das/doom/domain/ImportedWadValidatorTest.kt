package com.kunal26das.doom.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ImportedWadValidatorTest {
    @Test
    fun identifiesEpisodeAndNumberedBaseGames() {
        assertEquals(
            ImportedWadInformation(WadMapStyle.Episodes, 3),
            ImportedWadValidator.validate(wad("E1M1", "E1M2", "E2M1")),
        )
        assertEquals(
            ImportedWadInformation(WadMapStyle.NumberedMaps, 2),
            ImportedWadValidator.validate(wad("MAP01", "MAP02", "MAPINFO")),
        )
    }

    @Test
    fun importedSelectionOwnsItsBytesAndOnlyExposesTheFileName() {
        val original = wad("E1M1")
        val imported = ImportedGameSelection("/private/games/doom.wad", original)
        original.fill(0)

        assertEquals("doom.wad", imported.name)
        assertEquals(1, ImportedWadValidator.validate(imported.bytes).mapCount)
    }

    @Test
    fun addOnsHaveASpecificActionableError() {
        val addon = wad("MAP01").also { it[0] = 'P'.code.toByte() }
        val error = assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(addon) }
        assertTrue(error.message.orEmpty().contains("add-on"))
    }

    @Test
    fun rejectsUnrelatedFilesAndWadsWithoutDoomAssetsOrMaps() {
        assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(byteArrayOf(1, 2)) }
        assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(ByteArray(12)) }
        assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(wad("E1M1", assets = false)) }
        assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(wad("LEVEL01")) }
    }

    @Test
    fun validatesSignedDirectoryBoundsWithoutIntegerOverflow() {
        val negativeCount = wad("MAP01").also { it.writeInt(4, -1) }
        val overflowCount = wad("MAP01").also { it.writeInt(4, Int.MAX_VALUE) }
        val negativeOffset = wad("MAP01").also { it.writeInt(8, -1) }
        val truncatedDirectory = wad("MAP01").dropLast(1).toByteArray()
        val overlappingHeader = wad("MAP01").also { it.writeInt(8, 0) }
        listOf(negativeCount, overflowCount, negativeOffset, truncatedDirectory, overlappingHeader).forEach {
            assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(it) }
        }
    }

    @Test
    fun rejectsLumpsWhoseDataFallsOutsideTheFile() {
        val negativeOffset = wad("MAP01").also { it.writeInt(12, -1) }
        val negativeSize = wad("MAP01").also { it.writeInt(16, -1) }
        val overflow = wad("MAP01").also {
            it.writeInt(12, Int.MAX_VALUE)
            it.writeInt(16, Int.MAX_VALUE)
        }
        listOf(negativeOffset, negativeSize, overflow).forEach {
            assertFailsWith<IllegalArgumentException> { ImportedWadValidator.validate(it) }
        }
    }

    @Test
    fun rejectsOversizedImportsBeforeReadingTheHeader() {
        val error = assertFailsWith<IllegalArgumentException> {
            ImportedWadValidator.validate(ByteArray(MAX_WAD_BYTES + 1))
        }
        assertTrue(error.message.orEmpty().contains("64 MB"))
    }

    @Test
    fun boundsDirectoryWorkBeforeAllocatingNames() {
        val error = assertFailsWith<IllegalArgumentException> {
            ImportedWadValidator.validate(wad("E1M1").also { it.writeInt(4, 65_537) })
        }
        assertTrue(error.message.orEmpty().contains("too many entries"))
    }

    private fun wad(vararg maps: String, assets: Boolean = true): ByteArray {
        val names = (if (assets) listOf("PLAYPAL", "COLORMAP", "TEXTURE1", "PNAMES", "M_DOOM") else emptyList()) + maps
        return ByteArray(12 + names.size * 16).also { bytes ->
            "IWAD".forEachIndexed { index, char -> bytes[index] = char.code.toByte() }
            bytes.writeInt(4, names.size)
            bytes.writeInt(8, 12)
            names.forEachIndexed { index, name ->
                name.forEachIndexed { character, char -> bytes[12 + index * 16 + 8 + character] = char.code.toByte() }
            }
        }
    }

    private fun ByteArray.writeInt(offset: Int, value: Int) {
        repeat(4) { this[offset + it] = (value ushr (8 * it)).toByte() }
    }
}
